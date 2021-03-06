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

public class GaussianNoise {

    private static Random random = new Random();

    /**
     * Pick a point on an hypersphere of dimension (dim - 1) with uniform
     * probability
     * 
     * @see <a href="http://mathworld.wolfram.com/HyperspherePointPicking.html">
     *      * Hypersphere Point Picking< /a>
     */
    public static double[] randomVersor(int dim) {

	double[] versor = new double[dim];
	double norm = 0;
	for (int i = 0; i < dim; i++) {
	    versor[i] = random.nextDouble() * 2 - 1;
	    norm += versor[i] * versor[i];
	}

	norm = Math.sqrt(norm);

	for (int i = 0; i < dim; i++) {
	    versor[i] /= norm;
	}

	return versor;
    }

    /**
     * Generate a vector with random direction and a length determined by a
     * gaussian distribution, with stdev as its standard deviation
     * 
     * @param dim
     * @param stdev
     * @param bound
     * 
     * @return a double[] or null, if the length exceeds the given bound
     * 
     */
    public static double[] gaussianNoise(int dim, double stdev, double bound) {

	double norm = random.nextGaussian() * stdev;

	if (bound > 0d && norm > bound) {
	    return null;
	}

	double[] vector = randomVersor(dim);
	for (int i = 0; i < dim; i++) {
	    vector[i] *= norm;
	}

	return vector;
    }

    /**
     * Generate a double value determined by a gaussian distribution, with stdev
     * as its standard deviation
     * 
     * @param dim
     * @param stdev
     * @param bound
     * 
     * @return a double value or zero, if the length exceeds the given bound
     * 
     */
    public static double gaussianNoise(double stdev, double bound) {

	double value = random.nextGaussian() * stdev;

	if (bound > 0d && value > bound) {
	    return 0d;
	} else {
	    return value;
	}
    }
}
