

package soam.sampling;

import java.util.Random;

public class ToroidalHelixSampler implements ComponentSampler {
    protected static final double BASIC_SCALE = 256;
    protected static final double SCALE = BASIC_SCALE / 8d;

    protected static final double MINOR_RADIUS = 0.5d;
    protected static final double MAJOR_RADIUS = 3d;

    protected int multiplier;

    protected Random random;

    public ToroidalHelixSampler(int multiplier) {
	if (multiplier < 1) {
	    throw new RuntimeException("Unsupported multiplier: " + multiplier);
	}

	this.multiplier = multiplier;
	random = new Random();
    }

    public double[] nextSample() {
	double t = random.nextDouble() * Math.PI * 2;

	double[] sample = new double[3];

	sample[0] =
		MAJOR_RADIUS * Math.cos(t) + MINOR_RADIUS
			* Math.cos(multiplier * 10 * t) * Math.cos(t);
	sample[1] =
		MAJOR_RADIUS * Math.sin(t) + MINOR_RADIUS
			* Math.cos(multiplier * 10 * t) * Math.sin(t);
	sample[2] = MINOR_RADIUS * Math.sin(multiplier * 10 * t);

	// Scale & translate
	sample[0] = sample[0] * SCALE + BASIC_SCALE / 2d;
	sample[1] = sample[1] * SCALE + BASIC_SCALE / 2d;
	sample[2] = sample[2] * SCALE + BASIC_SCALE / 2d;

	return sample;
    }

    public int getDimensions() {
	return 3;
    }

    public double getArea() {
	throw new RuntimeException("Method not implemented");
    }
}
