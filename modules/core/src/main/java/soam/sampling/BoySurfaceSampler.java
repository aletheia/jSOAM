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

import java.util.Random;

public class BoySurfaceSampler implements ComponentSampler {
    protected static final double BASIC_SCALE = 256;
    protected static final double SCALE = BASIC_SCALE / Math.PI;

    protected int dimensions;

    protected Random random;

    public BoySurfaceSampler(int dimensions) {
	if (dimensions < 3) {
	    throw new RuntimeException("Unsupported dimensions: " + dimensions);
	}

	this.dimensions = dimensions;
	random = new Random();
    }

    public double[] nextSample() {
	double u = (random.nextDouble() * Math.PI) - Math.PI / 2;
	double v = (random.nextDouble() * Math.PI) - Math.PI / 2;

	double[] sample = new double[dimensions];

	sample[0] =
		(2 / 3d)
			* (Math.cos(u) * Math.cos(2 * v) + Math.sqrt(2)
				* Math.sin(u) * Math.cos(v)) * Math.cos(u)
			/ (Math.sqrt(2) - Math.sin(2 * u) * Math.sin(3 * v));
	sample[1] =
		(2 / 3d)
			* (Math.cos(u) * Math.sin(2 * v) - Math.sqrt(2)
				* Math.sin(u) * Math.sin(v)) * Math.cos(u)
			/ (Math.sqrt(2) - Math.sin(2 * u) * Math.sin(3 * v));
	sample[2] =
		Math.sqrt(2) * Math.pow(Math.cos(u), 2)
			/ (Math.sqrt(2) - Math.sin(2 * u) * Math.sin(2 * v));

	for (int i = 3; i < dimensions; i++) {
	    sample[i] = (Math.cos(u) + Math.cos(v)) / 2d;
	}

	// Scale & translate
	sample[0] = sample[0] * SCALE + BASIC_SCALE / 2d;
	sample[1] = sample[1] * SCALE + BASIC_SCALE / 2d;
	sample[2] = sample[2] * SCALE + BASIC_SCALE / 4d;

	for (int i = 3; i < dimensions; i++) {
	    sample[i] = sample[i] * BASIC_SCALE;
	}

	return sample;
    }

    public int getDimensions() {
	return dimensions;
    }

    public double getArea() {
	throw new RuntimeException("Method not implemented");
    }
}
