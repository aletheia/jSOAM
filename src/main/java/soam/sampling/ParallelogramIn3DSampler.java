
package soam.sampling;

import soam.algorithms.AlgebraicOperatorsNd;

public class ParallelogramIn3DSampler implements ComponentSampler {

    protected double[] base;
    protected double[] v1;
    protected double[] v2;

    public ParallelogramIn3DSampler(double[] base, double[] v1, double[] v2) {

        this.base = base;
        this.v1 = v1;
        this.v2 = v2;
    }

    /**
     * Uniform sampling of a parallelogram
     */
    public double[] nextSample() {
        double[] sample = new double[base.length];

        double a = Math.random();
        double b = Math.random();

        // Sample the parallelogram
        sample[0] = a * v1[0] + b * v2[0];
        sample[1] = a * v1[1] + b * v2[1];
        sample[2] = a * v1[2] + b * v2[2];

        // Translate point
        sample[0] += base[0];
        sample[1] += base[1];
        sample[2] += base[2];

        return sample;
    }

    public int getDimensions() {
	return 3;
    }
    
    /**
     * Use the cross product rule: area = || v1 x v2 ||
     */
    public double getArea() {
        double v1NormSquare = AlgebraicOperatorsNd.normSquare(v1);
        double v2NormSquare = AlgebraicOperatorsNd.normSquare(v2);
        double scalarProduct = AlgebraicOperatorsNd.scalarProduct(v1, v2);
        return Math.sqrt(v1NormSquare * v2NormSquare - scalarProduct
                * scalarProduct);

    }

}
