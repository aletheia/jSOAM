
package soam.sampling;

import java.awt.image.BufferedImage;
import java.util.ArrayList;

import javax.swing.JOptionPane;

import soam.algorithms.Algorithm;

public class SurfaceInNDSampler implements Sampler {

    protected static String defaultPath = System.getProperty("user.dir");

    protected static String[] options =
	    { "Boy's Surface in 3D", "Boy's Surface in 4D",
		    "Boy's Surface in 5D",

		    "Klein's Bottle in 3D", "Klein's Bottle in 4D",
		    "Klein's Bottle in 5D",

		    "Klein's Bagel in 3D", "Klein's Bagel in 4D",
		    "Klein's Bagel in 5D",

		    "Torus Surface in 3D", "Torus Surface in 4D",
		    "Torus Surface in 5D" };

    protected static ComponentSampler[] optionSamplers;

    protected ComponentSampler surfaceSampler;
    protected String option;

    protected static void initializeSurfaceSamplers() {
	if (optionSamplers != null) {
	    return;
	}

	optionSamplers = new ComponentSampler[12];

	optionSamplers[0] = new BoySurfaceSampler(3);
	optionSamplers[1] = new BoySurfaceSampler(4);
	optionSamplers[2] = new BoySurfaceSampler(5);

	optionSamplers[3] = new KleinBottleSampler(3);
	optionSamplers[4] = new KleinBottleSampler(4);
	optionSamplers[5] = new KleinBottleSampler(5);

	optionSamplers[6] = new KleinBagelSampler(3);
	optionSamplers[7] = new KleinBagelSampler(4);
	optionSamplers[8] = new KleinBagelSampler(5);

	optionSamplers[9] = new TorusSampler(3);
	optionSamplers[10] = new TorusSampler(4);
	optionSamplers[11] = new TorusSampler(5);
    }

    public boolean initialize(Algorithm algorithm) {

	initializeSurfaceSamplers();

	option =
		(String) JOptionPane.showInputDialog(null,
			"Available surfaces:", "Select surface",
			JOptionPane.PLAIN_MESSAGE, null, options, options[0]);

	if (option == null) {
	    return false;
	}

	for (int i = 0; i < options.length; i++) {
	    if (options[i].equals(option)) {
		surfaceSampler = optionSamplers[i];
		break;
	    }
	}

        if (surfaceSampler == null) {
            return false;
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
	return surfaceSampler.getDimensions();
    }

    public String getDescription() {
	return "";
    }    

    public BufferedImage getBufferedImage() {
	return null;
    }

    public ArrayList<double[]> getPointCloud() {
	return null;
    }

    public double[] nextSample() {
	return surfaceSampler.nextSample();
    }

    public double[] nextSample(final double[] point, double radius) {
	throw new RuntimeException("Method not implemented");
    }

    public String toString() {
	return this.getClass().getSimpleName() + "(\"" + option + "\")";
    }
}
