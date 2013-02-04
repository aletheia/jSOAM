
package soam.sampling;

import java.io.File;
import java.io.FilenameFilter;

import java.util.ArrayList;
import java.util.Random;
import java.util.Vector;

import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;

import javax.swing.*;

import soam.algorithms.Algorithm;
import soam.utils.PGMImageIO;

@SuppressWarnings("unused")
public class PGMSequenceSampler implements Sampler {
    public static final String DISTANCE_FILE_PREFIX = "frame_dis_";
    public static final String REFLECTANCE_FILE_PREFIX = "frame_ref_";

    protected static String defaultPath = System.getProperty("user.dir");

    protected boolean USE_4D_SAMPLES = false;
    protected boolean USE_REFLECTANCE_SAMPLES = false;

    protected int MAX_REJECTION = 100;

    protected int START_AT_FRAME = 1;
    protected int END_AT_FRAME = Integer.MAX_VALUE;

    protected boolean SEQUENCE_SAMPLING = true;
    protected int SAMPLES_PER_INITIAL_FRAME = 100000;
    protected int SAMPLES_PER_FRAME = 10000;

    protected double DISHABILITATION_FRACTION = 0d;

    protected double DISTANCE_SCALE_FACTOR = 1 / 256d;
    protected double REFLECTANCE_SCALE_FACTOR = 1 / 256d;

    protected double DISTANCE_DIFFERENCE_THRESHOLD = -1d;

    protected double REFLECTANCE_UPPER_THRESHOLD = -1d;
    protected double REFLECTANCE_LOWER_THRESHOLD = -1d;

    protected double EXPONENTIAL_SMOOTHING = 0d;

    protected BufferedImage currentDistanceImage;
    protected WritableRaster currentDistanceRaster;

    protected BufferedImage currentReflectanceImage;
    protected Raster currentReflectanceRaster;

    protected ArrayList<double[]> currentPointCloud;

    protected BufferedImage[] distanceImages;
    protected BufferedImage[] reflectanceImages;

    protected File directory;

    protected Algorithm algorithm;

    protected Random random;

    protected int startAtFrame;
    protected int endAtFrame;
    protected int currentFrame;
    protected int sampleCounter;

    public boolean initialize(Algorithm algorithm) {

	this.algorithm = algorithm;

	random = new Random();

	while (true) {
	    JFileChooser fc = new JFileChooser(defaultPath);
	    fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
	    fc.setDialogTitle(this.getClass().getName()
		    + " : Sequence directory");

	    if (fc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
		directory = fc.getSelectedFile();
		defaultPath = directory.getAbsolutePath();

		try {
		    FilenameFilter distanceFilter = new FilenameFilter() {
			public boolean accept(File tmp, String name) {
			    return name.matches(DISTANCE_FILE_PREFIX
				    + ".*\\.pgm");
			}
		    };
		    File[] distanceImageFiles =
			    directory.listFiles(distanceFilter);

		    FilenameFilter reflectanceFilter = new FilenameFilter() {
			public boolean accept(File tmp, String name) {
			    return name.matches(REFLECTANCE_FILE_PREFIX
				    + ".*\\.pgm");
			}
		    };
		    File[] reflectanceImageFiles =
			    directory.listFiles(reflectanceFilter);

		    if (distanceImageFiles.length == 0) {
			System.err.println("No distance image files found");
			throw new RuntimeException();
		    } else if (distanceImageFiles.length != reflectanceImageFiles.length) {
			System.err
				.println("Mismatch in the number of distance and reflectance image files");
			throw new RuntimeException();
		    }

		    distanceImages =
			    new BufferedImage[distanceImageFiles.length];
		    for (int i = 0; i < distanceImageFiles.length; i++) {
			distanceImages[i] =
				PGMImageIO.read(distanceImageFiles[i]);
		    }

		    reflectanceImages =
			    new BufferedImage[reflectanceImageFiles.length];
		    for (int i = 0; i < reflectanceImageFiles.length; i++) {
			reflectanceImages[i] =
				PGMImageIO.read(reflectanceImageFiles[i]);
		    }

		    reset();

		} catch (Exception e) {
		    JOptionPane.showMessageDialog(null,
			    "Could not load sequence \"" + directory.getPath()
				    + "\"", "Error", JOptionPane.ERROR_MESSAGE);
		    continue;
		}

		return true;

	    } else {
		return false;
	    }
	}
    }

    public void reset() {

	startAtFrame = Math.min(START_AT_FRAME - 1, distanceImages.length - 1);
	endAtFrame = Math.min(END_AT_FRAME - 1, distanceImages.length - 1);

	currentFrame = startAtFrame;

	sampleCounter = 0;

	currentDistanceImage = distanceImages[currentFrame];

	algorithm.enableVertexCreation();
	
	if (EXPONENTIAL_SMOOTHING > 0) {
	    currentDistanceRaster = currentDistanceImage.copyData(null);
	} else {
	    currentDistanceRaster = currentDistanceImage.getRaster();
	}

	currentReflectanceImage = reflectanceImages[currentFrame];
	currentReflectanceRaster = currentReflectanceImage.getRaster();

	if (currentPointCloud != null) {
	    synchronized (currentPointCloud) {
		currentPointCloud = new ArrayList<double[]>();
	    }
	} else {
	    currentPointCloud = new ArrayList<double[]>();
	}
    }

    /**
     * Gets the complete dimension of the sample space
     * 
     * @return dimension
     */
    public int getDimensions() {
	if (USE_4D_SAMPLES) {
	    return 4;
	} else {
	    return 3;
	}
    }

    /**
     * Gets the description string
     * 
     * @return dimension
     */
    public String getDescription() {
	return "Frame " + (currentFrame + 1);
    }

    /**
     * Gets the image as BufferedImage
     * 
     * @return BufferedImage
     */
    public BufferedImage getBufferedImage() {
	return currentDistanceImage;
    }

    /**
     * Gets the point cloud
     * 
     * @return the array list of points
     */
    public ArrayList<double[]> getPointCloud() {

	synchronized (currentPointCloud) {

	    if (currentPointCloud.size() == 0) {
		for (int i = 0; i < currentDistanceRaster.getWidth(); i++) {
		    for (int j = 0; j < currentDistanceRaster.getHeight(); j++) {

			double[] point;
			if (USE_4D_SAMPLES) {
			    point = new double[4];
			} else {
			    point = new double[3];
			}

			// Uniform sampling over image space
			point[0] = i;
			point[1] = j;

			loadPoint(point);

			double reflectance;
			if (USE_4D_SAMPLES) {
			    reflectance = point[3];
			} else {
			    reflectance =
				    currentReflectanceRaster.getSampleDouble(
					    (int) point[0], (int) point[1], 0)
					    * REFLECTANCE_SCALE_FACTOR;
			}

			if (isViable(point, reflectance)) {
			    currentPointCloud.add(point);
			}
		    }
		}
	    }

	    return currentPointCloud;
	}

    }

    /**
     * Gets the image as Raster
     * 
     * @return Raster
     */
    public Raster getRaster() {
	return currentDistanceRaster;
    }

    /**
     * Gets a sample from the image
     * 
     * @return A sampled point
     */
    public double[] nextSample() {
	updateSampleCounter();

	if (REFLECTANCE_UPPER_THRESHOLD < 0 && REFLECTANCE_LOWER_THRESHOLD < 0
		&& DISTANCE_DIFFERENCE_THRESHOLD < 0) {
	    return basicNextSample();
	} else {
	    int maxTries = currentDistanceRaster.getWidth() * currentDistanceRaster.getHeight();
	    
	    for (int i = 0; i < maxTries; i++) {
		double[] sample = basicNextSample();

		double reflectance;
		if (USE_4D_SAMPLES) {
		    reflectance = sample[3];
		} else {
		    reflectance =
			    currentReflectanceRaster.getSampleDouble(
				    (int) sample[0], (int) sample[1], 0)
				    * REFLECTANCE_SCALE_FACTOR;
		}

		if (isViable(sample, reflectance)) {
		    return sample;
		}
	    }
	    
	    System.err.println("nextSample() : Could not find a viable sample.");
	    return null;
	}
    }

    /**
     * Gets a localized sample from the image
     * 
     * @return A sampled point
     */
    public double[] nextSample(final double[] point, double radius) {
	updateSampleCounter();

	if (REFLECTANCE_UPPER_THRESHOLD < 0 && REFLECTANCE_LOWER_THRESHOLD < 0
		&& DISTANCE_DIFFERENCE_THRESHOLD < 0) {
	    return basicNextSample(point, radius);
	} else {
	    for (int i = 0; i < MAX_REJECTION; i++) {
		double[] sample = basicNextSample(point, radius);

		double reflectance;
		if (USE_4D_SAMPLES) {
		    reflectance = sample[3];
		} else {
		    reflectance =
			    currentReflectanceRaster.getSampleDouble(
				    (int) sample[0], (int) sample[1], 0)
				    * REFLECTANCE_SCALE_FACTOR;
		}

		if (isViable(sample, reflectance)) {
		    return sample;
		}
	    }

	    return nextSample();
	}
    }

    protected void updateSampleCounter() {

	if (!SEQUENCE_SAMPLING) {
	    return;
	}

	sampleCounter++;

	if (currentFrame < endAtFrame) {
	    if ((currentFrame == startAtFrame && sampleCounter > SAMPLES_PER_INITIAL_FRAME)
		    || (currentFrame > startAtFrame && sampleCounter > SAMPLES_PER_FRAME)) {

		++currentFrame;

		sampleCounter = 0;

		if (DISHABILITATION_FRACTION > 0) {
		    algorithm.disableVertexCreation();
		}

		currentDistanceImage = distanceImages[currentFrame];
		if (EXPONENTIAL_SMOOTHING > 0) {
		    Raster raster = currentDistanceImage.getRaster();

		    for (int x = 0; x < currentDistanceRaster.getWidth(); x++) {
			for (int y = 0; y < currentDistanceRaster.getHeight(); y++) {
			    double s =
				    currentDistanceRaster.getSampleDouble(x, y,
					    0);
			    double v = raster.getSampleDouble(x, y, 0);

			    s =
				    (1 - EXPONENTIAL_SMOOTHING) * v
					    + EXPONENTIAL_SMOOTHING * s;

			    currentDistanceRaster.setSample(x, y, 0, s);
			}
		    }

		} else {
		    currentDistanceRaster = currentDistanceImage.getRaster();
		}

		currentReflectanceImage = reflectanceImages[currentFrame];
		currentReflectanceRaster = currentReflectanceImage.getRaster();
	    }
	}
	
	if ((algorithm.getVertexCreationLock()
		&& currentFrame > startAtFrame && sampleCounter > SAMPLES_PER_FRAME
		* DISHABILITATION_FRACTION)) {
	    algorithm.enableVertexCreation();
	}

	synchronized (currentPointCloud) {
	    currentPointCloud = new ArrayList<double[]>();
	}
    }

    protected boolean isViable(double[] sample, double reflectance) {
	boolean reflectance_OK = true;
	boolean distance_OK = true;

	if ((REFLECTANCE_UPPER_THRESHOLD >= 0 && reflectance > REFLECTANCE_UPPER_THRESHOLD)
		|| (REFLECTANCE_LOWER_THRESHOLD >= 0 && reflectance < REFLECTANCE_LOWER_THRESHOLD)) {
	    return false;
	}

	if (reflectance_OK && DISTANCE_DIFFERENCE_THRESHOLD >= 0
		&& currentFrame > 0) {
	    double previousValue =
		    distanceImages[currentFrame - 1].getRaster()
			    .getSampleDouble((int) sample[0], (int) sample[1],
				    0)
			    * DISTANCE_SCALE_FACTOR;

	    if (Math.abs(sample[2] - previousValue) > DISTANCE_DIFFERENCE_THRESHOLD) {

		// sample[2] = findSuitableDistanceValue(sample);
		// if (sample[2] == 0) {
		// distance_OK = false;
		// }
		return false;
	    }
	}

	return true;
    }

    protected double[] basicNextSample() {

	double[] sample;
	if (USE_4D_SAMPLES) {
	    sample = new double[4];
	} else {
	    sample = new double[3];
	}

	// Uniform sampling over image space
	sample[0] =
		random.nextDouble() * (currentDistanceRaster.getWidth() - 1);
	sample[1] =
		random.nextDouble() * (currentDistanceRaster.getHeight() - 1);

	loadPoint(sample);

	return sample;
    }

    protected double[] basicNextSample(final double[] point, double radius) {

	double[] sample;
	if (USE_4D_SAMPLES) {
	    sample = new double[4];
	} else {
	    sample = new double[3];
	}

	// Select a point in the neighborhood of the point
	double angle = random.nextDouble() * 2 * Math.PI;
	double distance = random.nextDouble() * radius;
	sample[0] = point[0] + (distance * Math.cos(angle));
	sample[1] = point[1] + (distance * Math.sin(angle));

	// Enforce image boundaries
	sample[0] =
		Math.min(Math.max(sample[0], 0d),
			(double) currentDistanceRaster.getWidth() - 1);
	sample[1] =
		Math.min(Math.max(sample[1], 0d),
			(double) currentDistanceRaster.getHeight() - 1);

	loadPoint(sample);

	return sample;
    }

    protected void loadPoint(double[] point) {

	if (USE_REFLECTANCE_SAMPLES) {
	    point[2] =
		    currentReflectanceRaster.getSampleDouble((int) point[0],
			    (int) point[1], 0)
			    * REFLECTANCE_SCALE_FACTOR;
	} else {
	    point[2] =
		    currentDistanceRaster.getSampleDouble((int) point[0],
			    (int) point[1], 0)
			    * DISTANCE_SCALE_FACTOR;
	}

	if (USE_4D_SAMPLES) {
	    point[3] =
		    currentReflectanceRaster.getSampleDouble((int) point[0],
			    (int) point[1], 0)
			    * REFLECTANCE_SCALE_FACTOR;
	}
    }

    private double findSuitableDistanceValue(double[] sample) {
	final int DEPTH = 4;

	int x = (int) sample[0];
	int y = (int) sample[1];
	double tmp = 0;
	int pixels = 0;

	// Look back and forth
	for (int i = -DEPTH; i <= DEPTH; i++) {
	    if (i == 0) {
		continue;
	    }

	    if (isReflectanceWithinThreshold(currentFrame + i, x, y)
		    && isDistanceWithinThreshold(currentFrame + i, x, y)) {
		pixels++;
		tmp +=
			distanceImages[currentFrame + i].getRaster()
				.getSampleDouble(x, y, 0)
				* DISTANCE_SCALE_FACTOR;
	    }
	}

	if (pixels < DEPTH) {
	    return 0d;
	} else {
	    return tmp / (double) pixels;
	}

	// if (currentFrame == 0 || currentFrame == distanceImages.length - 1) {
	// // No way
	// return sample[2];
	// }
	//
	// int x = (int) sample[0];
	// int y = (int) sample[1];
	// double value = 0d;
	//
	// if (!isReflectanceWithinThreshold(currentFrame - 1, x, y)) {
	// // Advancing front
	// value = averageNeighborhood(currentFrame - 1, x, y);
	//
	// } else if (!isReflectanceWithinThreshold(currentFrame + 1, x, y)) {
	// // Retreating front
	// value = averageNeighborhood(currentFrame + 1, x, y);
	//
	// } else {
	// // Who knows
	// value = averageNeighborhood(currentFrame, x, y);
	// }
	//
	// return value == 0d ? sample[2] : value;
    }

    private double averageNeighborhood(int reflectanceFrame, int x, int y) {
	final int RADIUS = 10;
	int pixels = 0;
	double tmp = 0;

	for (int i = -RADIUS; i <= RADIUS; i++) {
	    for (int j = -RADIUS; j <= RADIUS; j++) {
		if (i == 0 && j == 0) {
		    continue;
		} else if (areWithinImage(x + i, y + j)
			&& isReflectanceWithinThreshold(reflectanceFrame,
				x + i, y + j)) {
		    // } else if (areWithinImage(x + i, y + j)
		    // && isReflectanceWithinThreshold(currentFrame, x + i, y +
		    // j)
		    // && isDistanceWithinThreshold(currentFrame, x + i, y + j))
		    // {
		    pixels++;
		    tmp +=
			    currentDistanceRaster.getSampleDouble(x + i, y + j,
				    0);
		}
	    }
	}

	if (pixels > 0) {
	    tmp /= (double) pixels;
	}

	return tmp * DISTANCE_SCALE_FACTOR;
    }

    private boolean isDistanceWithinThreshold(int frame, int x, int y) {
	if (DISTANCE_DIFFERENCE_THRESHOLD < 0) {
	    return true;
	}

	double distance =
		distanceImages[frame].getRaster().getSampleDouble(x, y, 0)
			* DISTANCE_SCALE_FACTOR;

	distance -=
		distanceImages[frame - 1].getRaster().getSampleDouble(x, y, 0)
			* DISTANCE_SCALE_FACTOR;

	return Math.abs(distance) <= DISTANCE_DIFFERENCE_THRESHOLD;
    }

    private boolean isReflectanceWithinThreshold(int frame, int x, int y) {
	double reflectance =
		reflectanceImages[frame].getRaster().getSampleDouble(x, y, 0)
			* REFLECTANCE_SCALE_FACTOR;

	return (REFLECTANCE_UPPER_THRESHOLD < 0 || reflectance <= REFLECTANCE_UPPER_THRESHOLD)
		&& (REFLECTANCE_LOWER_THRESHOLD < 0 || reflectance >= REFLECTANCE_LOWER_THRESHOLD);
    }

    private boolean areWithinImage(int x, int y) {
	return x >= 0 && x < currentDistanceRaster.getWidth() && y >= 0
		&& y < currentDistanceRaster.getHeight();
    }

    public String toString() {
	return this.getClass().getSimpleName() + "(\"" + directory.getPath()
		+ "\")";
    }

    public int getSTART_AT_FRAME() {
	return START_AT_FRAME;
    }

    public void setSTART_AT_FRAME(int start_at_frame) {
	START_AT_FRAME = start_at_frame;

	reset();
    }

    public int getEND_AT_FRAME() {
	return END_AT_FRAME;
    }

    public void setEND_AT_FRAME(int end_at_frame) {
	END_AT_FRAME = end_at_frame;

	reset();
    }

    public boolean isUSE_4D_SAMPLES() {
	return USE_4D_SAMPLES;
    }

    public void setUSE_4D_SAMPLES(boolean use_4d_samples) {
	USE_4D_SAMPLES = use_4d_samples;

	reset();
    }

    public double getDISTANCE_SCALE_FACTOR() {
	return DISTANCE_SCALE_FACTOR;
    }

    public void setDISTANCE_SCALE_FACTOR(double distance_scale_factor) {
	DISTANCE_SCALE_FACTOR = distance_scale_factor;
    }

    public double getREFLECTANCE_SCALE_FACTOR() {
	return REFLECTANCE_SCALE_FACTOR;
    }

    public void setREFLECTANCE_SCALE_FACTOR(double reflectance_scale_factor) {
	REFLECTANCE_SCALE_FACTOR = reflectance_scale_factor;
    }

    public double getREFLECTANCE_UPPER_THRESHOLD() {
	return REFLECTANCE_UPPER_THRESHOLD;
    }

    public void setREFLECTANCE_UPPER_THRESHOLD(
	    double reflectance_upper_threshold) {
	REFLECTANCE_UPPER_THRESHOLD = reflectance_upper_threshold;
    }

    public double getREFLECTANCE_LOWER_THRESHOLD() {
	return REFLECTANCE_LOWER_THRESHOLD;
    }

    public void setREFLECTANCE_LOWER_THRESHOLD(
	    double reflectance_lower_threshold) {
	REFLECTANCE_LOWER_THRESHOLD = reflectance_lower_threshold;
    }

    public int getSAMPLES_PER_INITIAL_FRAME() {
	return SAMPLES_PER_INITIAL_FRAME;
    }

    public void setSAMPLES_PER_INITIAL_FRAME(int samples_per_initial_frame) {
	SAMPLES_PER_INITIAL_FRAME = samples_per_initial_frame;
    }

    public int getSAMPLES_PER_FRAME() {
	return SAMPLES_PER_FRAME;
    }

    public void setSAMPLES_PER_FRAME(int samples_per_frame) {
	SAMPLES_PER_FRAME = samples_per_frame;
    }

    public boolean isSEQUENCE_SAMPLING() {
	return SEQUENCE_SAMPLING;
    }

    public void setSEQUENCE_SAMPLING(boolean sequence_sampling) {
	SEQUENCE_SAMPLING = sequence_sampling;
    }

    public int getMAX_REJECTION() {
	return MAX_REJECTION;
    }

    public void setMAX_REJECTION(int max_rejection) {
	MAX_REJECTION = max_rejection;
    }

    public boolean isUSE_REFLECTANCE_SAMPLES() {
	return USE_REFLECTANCE_SAMPLES;
    }

    public void setUSE_REFLECTANCE_SAMPLES(boolean use_reflectance_samples) {
	USE_REFLECTANCE_SAMPLES = use_reflectance_samples;
    }

    public double getDISTANCE_DIFFERENCE_THRESHOLD() {
	return DISTANCE_DIFFERENCE_THRESHOLD;
    }

    public void setDISTANCE_DIFFERENCE_THRESHOLD(
	    double distance_difference_threshold) {
	DISTANCE_DIFFERENCE_THRESHOLD = distance_difference_threshold;
    }

    public double getEXPONENTIAL_SMOOTHING() {
	return EXPONENTIAL_SMOOTHING;
    }

    public void setEXPONENTIAL_SMOOTHING(double exponential_smoothing) {
	EXPONENTIAL_SMOOTHING = exponential_smoothing;

	// Sampler MUST be reset: otherwise rasters will be overwritten
	reset();
    }

    public double getDISHABILITATION_FRACTION() {
	return DISHABILITATION_FRACTION;
    }

    public void setDISHABILITATION_FRACTION(double dishabilitation_fraction) {
	DISHABILITATION_FRACTION = dishabilitation_fraction;
    }

}