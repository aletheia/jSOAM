
package soam.sampling;

import java.util.ArrayList;
import java.util.Random;
import java.util.Vector;
import java.io.File;
import java.awt.image.BufferedImage;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import soam.algorithms.Algorithm;
import soam.algorithms.AlgebraicOperatorsNd;
import soam.utils.PLY;

@SuppressWarnings("unchecked")
public class PLYSampler implements Sampler {
    protected final static double CUBE_SIZE = 256d;

    protected int MAX_REJECTION = 100;

    protected double NOISE_STDEV = 0d;
    protected double NOISE_BOUND = 0d;

    protected static String defaultPath = System.getProperty("user.dir");

    protected File file;

    protected Random random;

    // Vertices
    protected Vector x;
    protected Vector y;
    protected Vector z;

    // Normals
    protected Vector nx;
    protected Vector ny;
    protected Vector nz;

    protected long size;

    protected ArrayList<double[]> pointCloud;

    protected double MOVE_X;
    protected double MOVE_Y;
    protected double MOVE_Z;
    protected double SCALE;
    protected double NORMAL_PROJECTION;

    public boolean initialize(Algorithm algorithm) {

	random = new Random();

	while (true) {
	    JFileChooser fc = new JFileChooser(defaultPath);
	    fc.addChoosableFileFilter(new FileNameExtensionFilter("PLY Files",
		    "ply"));
	    fc.setDialogTitle(this.getClass().getName() + " : PLY file");

	    if (fc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
		file = fc.getSelectedFile();
		defaultPath = file.getAbsolutePath();

		try {
		    PLY plyObj = new PLY();
		    plyObj.loadfile(file.getAbsolutePath());

		    x = (Vector) plyObj.getProperty("x", "vertex");
		    y = (Vector) plyObj.getProperty("y", "vertex");
		    z = (Vector) plyObj.getProperty("z", "vertex");

		    try {
			nx = (Vector) plyObj.getProperty("nx", "vertex");
			ny = (Vector) plyObj.getProperty("ny", "vertex");
			nz = (Vector) plyObj.getProperty("nz", "vertex");
		    } catch (Exception e) {
			// Oh well, no normals
		    }

		    size = x.size();

		} catch (Exception e) {
		    JOptionPane.showMessageDialog(null,
			    "Could not load PLY file \"" + file.getPath()
				    + "\"", "Error", JOptionPane.ERROR_MESSAGE);
		    continue;
		}

		break;

	    } else {
		return false;
	    }
	}

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

	for (int i = 0; i < size; i++) {
	    tmpX = ((Float) x.elementAt(i)).doubleValue();
	    tmpY = ((Float) y.elementAt(i)).doubleValue();
	    tmpZ = ((Float) z.elementAt(i)).doubleValue();

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
			/ Math.max(maxX - minX, Math.max(maxY - minY, maxZ
				- minZ));

	MOVE_X = -(minX + (maxX - minX) / 2);
	MOVE_Y = -(minY + (maxY - minY) / 2);
	MOVE_Z = -(minZ + (maxZ - minZ) / 2);

	NORMAL_PROJECTION = 0d;

	return true;
    }

    public void reset() {
	return;
    }

    public int getDimensions() {
	return 3;
    }

    public String getDescription() {
	return "";
    }

    public BufferedImage getBufferedImage() {
	return null;
    }

    /**
     * Gets the point cloud, as an array of vectors
     * 
     * @return dimension
     */
    public ArrayList<double[]> getPointCloud() {

	if (pointCloud == null) {
	    pointCloud = new ArrayList<double[]>();

	    for (int i = 0; i < size; i++) {
		double[] point = new double[3];

		point[0] = ((Float) x.elementAt(i)).doubleValue();
		point[1] = ((Float) y.elementAt(i)).doubleValue();
		point[2] = ((Float) z.elementAt(i)).doubleValue();

		if (NORMAL_PROJECTION != 0d && nx != null) {
		    point[0] +=
			    (((Float) nx.elementAt(i)).doubleValue())
				    * NORMAL_PROJECTION;
		    point[1] +=
			    (((Float) ny.elementAt(i)).doubleValue())
				    * NORMAL_PROJECTION;
		    point[2] +=
			    (((Float) nz.elementAt(i)).doubleValue())
				    * NORMAL_PROJECTION;
		}

		point[0] = (point[0] + MOVE_X) * SCALE + CUBE_SIZE / 2;
		point[1] = (point[1] + MOVE_Y) * SCALE + CUBE_SIZE / 2;
		point[2] = (point[2] + MOVE_Z) * SCALE + CUBE_SIZE / 2;

		pointCloud.add(point);
	    }
	}

	return pointCloud;
    }

    private double[] basicNextSample() {
	double[] sample = new double[3];

	int i = (int) (random.nextDouble() * size);

	sample[0] = ((Float) x.elementAt(i)).doubleValue();
	sample[1] = ((Float) y.elementAt(i)).doubleValue();
	sample[2] = ((Float) z.elementAt(i)).doubleValue();

	if (NORMAL_PROJECTION != 0d && nx != null) {
	    sample[0] +=
		    (((Float) nx.elementAt(i)).doubleValue())
			    * NORMAL_PROJECTION;
	    sample[1] +=
		    (((Float) ny.elementAt(i)).doubleValue())
			    * NORMAL_PROJECTION;
	    sample[2] +=
		    (((Float) nz.elementAt(i)).doubleValue())
			    * NORMAL_PROJECTION;
	}

	sample[0] = (sample[0] + MOVE_X) * SCALE + CUBE_SIZE / 2;
	sample[1] = (sample[1] + MOVE_Y) * SCALE + CUBE_SIZE / 2;
	sample[2] = (sample[2] + MOVE_Z) * SCALE + CUBE_SIZE / 2;

	return sample;
    }

    public double[] nextSample() {

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

    // Debugging only
    public int points() {
	return x.size();
    }

    // Debugging only
    public double[] pointAt(int i) {
	double[] point = new double[3];

	point[0] = ((Float) x.elementAt(i)).doubleValue();
	point[1] = ((Float) y.elementAt(i)).doubleValue();
	point[2] = ((Float) z.elementAt(i)).doubleValue();

	point[0] = (point[0] + MOVE_X) * SCALE + CUBE_SIZE / 2;
	point[1] = (point[1] + MOVE_Y) * SCALE + CUBE_SIZE / 2;
	point[2] = (point[2] + MOVE_Z) * SCALE + CUBE_SIZE / 2;

	return point;
    }

    public String toString() {
	return this.getClass().getSimpleName() + "(\"" + file.getPath() + "\")";
    }

    public double getNOISE_STDEV() {
	return NOISE_STDEV;
    }

    public void setNOISE_STDEV(double noise_stdev) {
	NOISE_STDEV = noise_stdev;
    }

    public double getNOISE_BOUND() {
	return NOISE_BOUND;
    }

    public void setNOISE_BOUND(double noise_bound) {
	NOISE_BOUND = noise_bound;
    }

    public int getMAX_REJECTION() {
	return MAX_REJECTION;
    }

    public void setMAX_REJECTION(int max_rejection) {
	MAX_REJECTION = max_rejection;
    }

    public double getMOVE_X() {
	return MOVE_X;
    }

    public void setMOVE_X(double move_x) {
	MOVE_X = move_x;
    }

    public double getMOVE_Y() {
	return MOVE_Y;
    }

    public void setMOVE_Y(double move_y) {
	MOVE_Y = move_y;
    }

    public double getMOVE_Z() {
	return MOVE_Z;
    }

    public void setMOVE_Z(double move_z) {
	MOVE_Z = move_z;
    }

    public double getSCALE() {
	return SCALE;
    }

    public void setSCALE(double scale) {
	SCALE = scale;
    }

    public double getNORMAL_PROJECTION() {
	return NORMAL_PROJECTION;
    }

    public void setNORMAL_PROJECTION(double normal_projection) {
	NORMAL_PROJECTION = normal_projection;
    }
}
