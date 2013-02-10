
package soam.sampling;

import java.util.ArrayList;
import java.util.Random;
import java.awt.image.BufferedImage;

import javax.swing.JOptionPane;

import soam.algorithms.Algorithm;

public class SurfaceIn3DSampler implements Sampler {

    protected static String[] options = { "Single sphere",
            "Two parallel planes",
            "Two concentric spheres and two parallel planes" };

    protected static ComponentSampler[][] optionSamplers;

    protected double DISP_X = 0d;
    protected double DISP_Y = 0d;
    protected double DISP_Z = 0d;

    protected double NOISE_STDEV = 0d;
    protected double NOISE_BOUND = 0d;

    protected ComponentSampler[] componentSamplers;
    protected String option;
    protected double[] areas;
    protected double area;

    protected Random random;
    
    protected static void initializeSurfaceSamplers() {
        if (optionSamplers != null) {
            return;
        }
        
        ComponentSampler[] tmp;
        optionSamplers = new ComponentSampler[3][];
        
        double[] c1 = { 135d, 127d, 127d };
        double[] c2 = { 140d, 127d, 127d };
        double[] b1 = { 85d, 64d, 64d };
        double[] b2 = { 72.2d, 64d, 64d };
        double[] v1 = { 0d, 128d, 0d };
        double[] v2 = { 0d, 0d, 128d };

        tmp = new ComponentSampler[1]; 
        tmp[0] = new SphereIn3DSampler(c2, 40d);
        optionSamplers[0] = tmp;

        tmp = new ComponentSampler[2]; 
        tmp[0] = new ParallelogramIn3DSampler(b1, v1, v2);
        tmp[1] = new ParallelogramIn3DSampler(b2, v1, v2);
        optionSamplers[1] = tmp;

        tmp = new ComponentSampler[4]; 
        tmp[0] = new SphereIn3DSampler(c1, 20d);
        tmp[1] = new SphereIn3DSampler(c2, 40d);
        tmp[2] = new ParallelogramIn3DSampler(b1, v1, v2);
        tmp[3] = new ParallelogramIn3DSampler(b2, v1, v2);
        optionSamplers[2] = tmp;
        
    }

    public boolean initialize(Algorithm algorithm) {

	initializeSurfaceSamplers();
	random = new Random();

        option = (String) JOptionPane.showInputDialog(null,
                "Available surfaces:", "Select surface",
                JOptionPane.PLAIN_MESSAGE, null, options, options[0]);

        if (option == null) {
            return false;
        }

        for (int i = 0; i < options.length; i++) {
            if (options[i].equals(option)) {
                componentSamplers = optionSamplers[i];
            }
        }

        if (componentSamplers == null) {
            return false;
        }
        
        areas = new double[componentSamplers.length];
        area = 0d;

        for (int i = 0; i < componentSamplers.length; i++) {
            areas[i] = componentSamplers[i].getArea();
            area += areas[i];
            if (i > 0) {
                areas[i] += areas[i - 1];
            }
        }

        return true;
    }

    public void reset() {
        return;
    }

    /**
     * Gets the complete dimension of the sample space
     * 
     * @return dimension
     */
    public int getDimensions() {
        return 3;
    }

    public String getDescription() {
	return "";
    }
    
    public ArrayList<double[]> getPointCloud() {
	return null;
    }

    public BufferedImage getBufferedImage() {
        return null;
    }

    public double[] nextSample() {

        // Select component
        double choice = random.nextDouble() * area;

        for (int i = 0; i < componentSamplers.length; i++) {
            if (choice < areas[i]) {
                double[] sample = componentSamplers[i].nextSample();
                sample[0] += DISP_X;
                sample[1] += DISP_Y;
                sample[2] += DISP_Z;
                
                // Add noise, if requested
                if (NOISE_STDEV > 0d) {
                    double[] noise = GaussianNoise.gaussianNoise(3, NOISE_STDEV, NOISE_BOUND);
                    if (noise != null) {
                        sample[0] += noise[0];
                        sample[1] += noise[1];
                        sample[2] += noise[2];                	
                    }
                }
                
                return sample;
            }
        }

        throw new RuntimeException("Unexpected MultiComponentSampler error");
    }

    public double[] nextSample(final double[] point, double radius) {
        throw new RuntimeException("Method not implemented");
    }

    public String toString() {
        return this.getClass().getSimpleName() + "(\"" + option + "\")";
    }

    public double getDISP_X() {
        return DISP_X;
    }

    public void setDISP_X(double disp_x) {
        DISP_X = disp_x;
    }

    public double getDISP_Y() {
        return DISP_Y;
    }

    public void setDISP_Y(double disp_y) {
        DISP_Y = disp_y;
    }

    public double getDISP_Z() {
        return DISP_Z;
    }

    public void setDISP_Z(double disp_z) {
        DISP_Z = disp_z;
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
}
