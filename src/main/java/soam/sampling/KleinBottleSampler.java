
package soam.sampling;

import java.util.Random;

public class KleinBottleSampler implements ComponentSampler {
    protected static final double BASIC_SCALE = 256;
    protected static final double SCALE = BASIC_SCALE / (Math.PI * 4);

    protected int dimensions;

    protected Random random;

    public KleinBottleSampler(int dimensions) {
	if (dimensions < 3) {
	    throw new RuntimeException("Unsupported dimensions: " + dimensions);
	}

	this.dimensions = dimensions;
	random = new Random();

    }

    public double[] nextSample() {

	double u = random.nextDouble() * 2 * Math.PI;
	double v = random.nextDouble() * 2 * Math.PI;

	double[] sample = new double[dimensions];

	sample[0] =
		(3 * (1 + Math.sin(v)) + 2 * (1 - Math.cos(v) / 2d)
			* Math.cos(u))
			* Math.cos(v);
	sample[1] =
		(4 + 2 * (1 - Math.cos(v) / 2d) * Math.cos(u)) * Math.sin(v);
	sample[2] = -2 * (1 - Math.cos(v) / 2d) * Math.sin(u);

	for (int i = 3; i < dimensions; i++) {
	    sample[i] = (0.02d * Math.cos(v) + 1.98d * Math.cos(u)) / 2d;
	}

	// Scale & translate
	sample[0] = sample[0] * SCALE + BASIC_SCALE / 2d;
	sample[1] = sample[1] * SCALE + BASIC_SCALE / 2d;
	sample[2] = sample[2] * SCALE + BASIC_SCALE / 2d;

	for (int i = 3; i < dimensions; i++) {
	    sample[i] = sample[i] * BASIC_SCALE / 2d + BASIC_SCALE / 2d;
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
