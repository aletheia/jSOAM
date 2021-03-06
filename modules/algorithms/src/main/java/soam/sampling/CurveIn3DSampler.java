/**
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.

 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.

 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package soam.sampling;

import java.util.ArrayList;
import java.util.Random;
import java.awt.image.BufferedImage;

import javax.swing.JOptionPane;

import soam.algorithms.Algorithm;

public class CurveIn3DSampler implements Sampler {

    protected static String[] options =
	    { "Toroidal Helix 1", "Toroidal Helix 2", "Toroidal Helix 5",
		    "Toroidal Helix 10", };

    protected static ComponentSampler[] optionSamplers;

    protected ComponentSampler componentSampler;
    protected String option;

    protected Random random;

    protected static void initializeCurveSamplers() {
	if (optionSamplers != null) {
	    return;
	}

	optionSamplers = new ComponentSampler[4];

	optionSamplers[0] = new ToroidalHelixSampler(1);
	optionSamplers[1] = new ToroidalHelixSampler(2);
	optionSamplers[2] = new ToroidalHelixSampler(5);
	optionSamplers[3] = new ToroidalHelixSampler(10);

    }

    public boolean initialize(Algorithm algorithm) {

	initializeCurveSamplers();
	random = new Random();

	option =
		(String) JOptionPane.showInputDialog(null, "Available curves:",
			"Select curve", JOptionPane.PLAIN_MESSAGE, null,
			options, options[0]);

	if (option == null) {
	    return false;
	}

	for (int i = 0; i < options.length; i++) {
	    if (options[i].equals(option)) {
		componentSampler = optionSamplers[i];
	    }
	}

	if (componentSampler == null) {
	    return false;
	}

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

    public ArrayList<double[]> getPointCloud() {
	return null;
    }

    public double[] nextSample() {
	return componentSampler.nextSample();
    }

    public double[] nextSample(final double[] point, double radius) {
	throw new RuntimeException("Method not implemented");
    }
}
