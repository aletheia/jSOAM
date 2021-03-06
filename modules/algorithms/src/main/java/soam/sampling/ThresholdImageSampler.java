

package soam.sampling;

import java.awt.Point;
import java.util.ArrayList;

import soam.algorithms.Algorithm;

public class ThresholdImageSampler extends GrayscaleImageSampler {

    protected boolean LESSER_THAN_THRESHOLD = true;
    protected double THRESHOLD = 254d;

    protected ArrayList<Point> points;

    public boolean initialize(Algorithm algorithm) {

	if (!super.initialize(algorithm)) {
	    return false;
	}

	selectPoints();

	return true;
    }

    /**
     * Gets the point cloud, as an array of vectors
     * 
     * @return dimension
     */
    @Override
    public ArrayList<double[]> getPointCloud() {

	if (pointCloud == null) {
	    pointCloud = new ArrayList<double[]>();

	    for (Point aPoint : points) {
		double[] tmp = new double[2];

		tmp[0] = aPoint.x;
		tmp[1] = aPoint.y;

		pointCloud.add(tmp);
	    }
	}

	return pointCloud;
    }

    /**
     * The variable points is vulnerable to race conditions and accesses to it
     * should be synchronized: use the "pause" button, instead ...
     */
    protected void selectPoints() {
	points = new ArrayList<Point>();
	pointCloud = null;

	for (int i = 0; i < raster.getWidth(); i++) {
	    for (int j = 0; j < raster.getHeight(); j++) {
		if (LESSER_THAN_THRESHOLD) {
		    if (raster.getSampleDouble(i, j, 0) < THRESHOLD) {
			points.add(new Point(i, j));
		    }
		} else {
		    if (raster.getSampleDouble(i, j, 0) > THRESHOLD) {
			points.add(new Point(i, j));
		    }
		}
	    }
	}
    }

    public int getDimensions() {
	return 2;
    }

    /**
     * Gets a sample from the image
     * 
     * @return A sampled point
     */
    public double[] nextSample() {
	double[] sample = new double[2];

	// Uniform sampling over image space
	Point point = null;
	if (NEXT_SAMPLE == null) {
	    point = points.get((int) (random.nextDouble() * points.size()));
	} else {
	    // Get the closest point, for debugging
	    long distance = Long.MAX_VALUE;
	    for (Point aPoint : points) {
		long tmp =
			(aPoint.x - NEXT_SAMPLE.x) * (aPoint.x - NEXT_SAMPLE.x)
				+ (aPoint.y - NEXT_SAMPLE.y)
				* (aPoint.y - NEXT_SAMPLE.y);
		if (tmp < distance) {
		    point = aPoint;
		    distance = tmp;
		}
	    }
	    NEXT_SAMPLE = null;
	}

	sample[0] = point.x;
	sample[1] = point.y;

	return sample;
    }

    /**
     * Gets a localized sample from the image
     * 
     * @return A sampled point
     */
    public double[] nextSample(final double[] point, double radius) {
	// Just ignore, for now
	return nextSample();
    }
    
    /**
     * Gets a specific sample from the image
     * 
     * @return A sampled point
     */
    public double[] getSample(double[] sample) {
	throw new RuntimeException("Method not implemented");
    }

    public boolean isLESSER_THAN_THRESHOLD() {
	return LESSER_THAN_THRESHOLD;
    }

    public void setLESSER_THAN_THRESHOLD(boolean lesser_than_threshold) {
	LESSER_THAN_THRESHOLD = lesser_than_threshold;

	selectPoints();
    }

    public double getTHRESHOLD() {
	return THRESHOLD;
    }

    public void setTHRESHOLD(double threshold) {
	THRESHOLD = threshold;

	selectPoints();
    }
}
