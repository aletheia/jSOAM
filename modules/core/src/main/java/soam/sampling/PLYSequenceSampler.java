
package soam.sampling;

import java.io.File;
import java.io.FilenameFilter;

import java.util.ArrayList;
import java.util.Random;
import java.util.Vector;

import java.awt.image.BufferedImage;

import javax.swing.*;

import soam.algorithms.AlgebraicOperatorsNd;
import soam.algorithms.Algorithm;
import soam.utils.PLY;

@SuppressWarnings("unchecked")
public class PLYSequenceSampler implements Sampler {
    protected final static double CUBE_SIZE = 256d;

    protected static String defaultPath = System.getProperty("user.dir");

    protected int MAX_REJECTION = 100;

    protected double NOISE_STDEV = 0d;
    protected double NOISE_BOUND = 0d;

    protected int START_AT_FRAME = 1;
    protected int END_AT_FRAME = Integer.MAX_VALUE;

    protected boolean SEQUENCE_SAMPLING = true;
    protected int SAMPLES_PER_INITIAL_FRAME = 100000;
    protected int SAMPLES_PER_FRAME = 10000;

    protected double DISHABILITATION_FRACTION = 0d;

    protected PLY[] sequence;
    protected PLY current;

    protected Vector currentX;
    protected Vector currentY;
    protected Vector currentZ;

    protected long currentSize;

    protected double MOVE_X;
    protected double MOVE_Y;
    protected double MOVE_Z;
    protected double SCALE;

    protected File directory;

    protected Algorithm algorithm;

    protected Random random;

    protected int startAtFrame;
    protected int endAtFrame;
    protected int currentFrame;
    protected int sampleCounter;

    protected ArrayList<double[]> currentPointCloud;

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
		    // Use the last name of the directory as the prefix
		    final String filenamePrefix = directory.getName();

		    FilenameFilter filter = new FilenameFilter() {
			public boolean accept(File tmp, String name) {
			    return name.matches(filenamePrefix + ".*\\.ply");
			}
		    };

		    File[] plyFiles = directory.listFiles(filter);

		    if (plyFiles.length == 0) {
			System.err.println("No PLY files found");
			throw new RuntimeException();
		    }

		    sequence = new PLY[plyFiles.length];

		    for (int i = 0; i < plyFiles.length; i++) {
			PLY plyObj = new PLY();
			plyObj.loadfile(plyFiles[i]);
			sequence[i] = plyObj;
		    }

		    currentPointCloud = new ArrayList<double[]>();

		    reset();

		    // Establish translation and rescaling:
		    // preserve object proportions but make it fit the cubic box
		    double minX = Double.MAX_VALUE;
		    double maxX = Double.MIN_VALUE;
		    double minY = Double.MAX_VALUE;
		    double maxY = Double.MIN_VALUE;
		    double minZ = Double.MAX_VALUE;
		    double maxZ = Double.MIN_VALUE;

		    double tmpX;
		    double tmpY;
		    double tmpZ;

		    for (int i = 0; i < currentSize; i++) {
			tmpX = ((Float) currentX.elementAt(i)).doubleValue();
			tmpY = ((Float) currentY.elementAt(i)).doubleValue();
			tmpZ = ((Float) currentZ.elementAt(i)).doubleValue();

			if (tmpX < minX) {
			    minX = tmpX;
			}
			if (tmpX > maxX) {
			    maxX = tmpX;
			}
			if (tmpY < minY) {
			    minY = tmpY;
			}
			if (tmpY > maxY) {
			    maxY = tmpY;
			}
			if (tmpZ < minZ) {
			    minZ = tmpZ;
			}
			if (tmpZ > maxZ) {
			    maxZ = tmpZ;
			}
		    }

		    SCALE =
			    CUBE_SIZE
				    / Math.max(maxX - minX, Math.max(maxY
					    - minY, maxZ - minZ));

		    MOVE_X = -(minX + (maxX - minX) / 2);
		    MOVE_Y = -(minY + (maxY - minY) / 2);
		    MOVE_Z = -(minZ + (maxZ - minZ) / 2);

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

	startAtFrame = Math.min(START_AT_FRAME - 1, sequence.length - 1);
	endAtFrame = Math.min(END_AT_FRAME - 1, sequence.length - 1);

	currentFrame = startAtFrame;

	sampleCounter = 0;

	current = sequence[currentFrame];

	try {
	    currentX = (Vector) current.getProperty("x", "vertex");
	    currentY = (Vector) current.getProperty("y", "vertex");
	    currentZ = (Vector) current.getProperty("z", "vertex");

	} catch (Exception e) {
	    System.err.println("When initializing PLY sequence");
	    e.printStackTrace();
	}

	currentSize = currentX.size();

	algorithm.enableVertexCreation();
	
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
	return 3;
    }

    /**
     * Gets the point cloud
     * 
     * @return the array list of points
     */
    public ArrayList<double[]> getPointCloud() {

	synchronized (currentPointCloud) {

	    if (currentPointCloud.size() == 0) {
		for (int i = 0; i < currentSize; i++) {
		    double[] point = new double[3];

		    point[0] = ((Float) currentX.elementAt(i)).doubleValue();
		    point[1] = ((Float) currentY.elementAt(i)).doubleValue();
		    point[2] = ((Float) currentZ.elementAt(i)).doubleValue();

		    point[0] = (point[0] + MOVE_X) * SCALE + CUBE_SIZE / 2;
		    point[1] = (point[1] + MOVE_Y) * SCALE + CUBE_SIZE / 2;
		    point[2] = (point[2] + MOVE_Z) * SCALE + CUBE_SIZE / 2;

		    currentPointCloud.add(point);
		}
	    }

	    return currentPointCloud;
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
	return null;
    }

    public double[] nextSample() {
	updateSampleCounter();

	double[] sample = basicNextSample();

	// Add noise, if requested
	if (NOISE_STDEV > 0d) {
	    double[] noise =
		    GaussianNoise.gaussianNoise(3, NOISE_STDEV, NOISE_BOUND);
	    if (noise != null) {
		sample[0] += noise[0];
		sample[1] += noise[1];
		sample[2] += noise[2];
	    }
	}

	return sample;
    }

    public double[] nextSample(final double[] point, double radius) {
	updateSampleCounter();

	double radiusSquare = radius * radius;
	double[] sample = null;
	for (int i = 0; i < MAX_REJECTION; i++) {
	    sample = basicNextSample();
	    if (AlgebraicOperatorsNd.normSquare(point, sample) < radiusSquare) {
		break;
	    }
	}

	// Add noise, if requested
	if (NOISE_STDEV > 0d) {
	    double[] noise =
		    GaussianNoise.gaussianNoise(3, NOISE_STDEV, NOISE_BOUND);
	    if (noise != null) {
		sample[0] += noise[0];
		sample[1] += noise[1];
		sample[2] += noise[2];
	    }
	}

	return sample;
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

		current = sequence[currentFrame];
		try {
		    currentX = (Vector) current.getProperty("x", "vertex");
		    currentY = (Vector) current.getProperty("y", "vertex");
		    currentZ = (Vector) current.getProperty("z", "vertex");

		    currentSize = currentX.size();

		} catch (Exception e) {
		    System.err.println("When advancing PLY sequence");
		    e.printStackTrace();
		}
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

    protected double[] basicNextSample() {

	double[] sample = new double[3];

	int i = (int) (random.nextDouble() * currentSize);

	sample[0] = ((Float) currentX.elementAt(i)).doubleValue();
	sample[1] = ((Float) currentY.elementAt(i)).doubleValue();
	sample[2] = ((Float) currentZ.elementAt(i)).doubleValue();

	sample[0] = (sample[0] + MOVE_X) * SCALE + CUBE_SIZE / 2;
	sample[1] = (sample[1] + MOVE_Y) * SCALE + CUBE_SIZE / 2;
	sample[2] = (sample[2] + MOVE_Z) * SCALE + CUBE_SIZE / 2;

	return sample;
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

    public double getDISHABILITATION_FRACTION() {
	return DISHABILITATION_FRACTION;
    }

    public void setDISHABILITATION_FRACTION(double dishabilitation_fraction) {
	DISHABILITATION_FRACTION = dishabilitation_fraction;
    }

}