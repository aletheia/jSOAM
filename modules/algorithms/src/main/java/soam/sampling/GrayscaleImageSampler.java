/**
 * 
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

import javax.swing.JOptionPane;

import soam.algorithms.Algorithm;

public class GrayscaleImageSampler extends ImageSampler {

    protected double UPPER_THRESHOLD = -1d;
    protected double LOWER_THRESHOLD = -1d;

    public boolean initialize(Algorithm algorithm) {

	if (!super.initialize(algorithm)) {
	    return false;
	} else if (numBands > 1) {
	    JOptionPane.showMessageDialog(null, "\"" + file.getPath()
		    + "\" is not a grayscale image", "Warning",
		    JOptionPane.WARNING_MESSAGE);
	}

	numBands = 1;

	return true;
    }

    /**
     * Gets a sample from the image
     * 
     * @return A sampled point
     */
    public double[] nextSample() {
	if (UPPER_THRESHOLD < 0 && LOWER_THRESHOLD < 0) {
	    return super.nextSample();
	} else {
	    while (true) {
		double[] sample = super.nextSample();
		if ((UPPER_THRESHOLD < 0 || sample[2] <= UPPER_THRESHOLD)
			&& (LOWER_THRESHOLD < 0 || sample[2] >= LOWER_THRESHOLD)) {
		    return sample;
		}
	    }
	}
    }

    /**
     * Gets a localized sample from the image
     * 
     * @return A sampled point
     */
    public double[] nextSample(final double[] point, double radius) {
	if (UPPER_THRESHOLD < 0 && LOWER_THRESHOLD < 0) {
	    return super.nextSample(point, radius);
	} else {
	    while (true) {
		double[] sample = super.nextSample(point, radius);
		if ((UPPER_THRESHOLD < 0 || sample[2] <= UPPER_THRESHOLD)
			&& (LOWER_THRESHOLD < 0 || sample[2] >= LOWER_THRESHOLD)) {
		    return sample;
		}
	    }
	}	
    }

    public int getDimensions() {
	return 3;
    }
    
    public double getUPPER_THRESHOLD() {
        return UPPER_THRESHOLD;
    }

    public void setUPPER_THRESHOLD(double upper_threshold) {
        UPPER_THRESHOLD = upper_threshold;
        
        pointCloud = null;
    }

    public double getLOWER_THRESHOLD() {
        return LOWER_THRESHOLD;
    }

    public void setLOWER_THRESHOLD(double lower_threshold) {
        LOWER_THRESHOLD = lower_threshold;
        
        pointCloud = null;
    }

}
