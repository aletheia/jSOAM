
package soam.sampling;


public class SphereIn3DSampler implements ComponentSampler {

    protected double[] center;
    protected double radius;

    public SphereIn3DSampler(double[] center, double radius) {

        if (center.length != 3) {
            throw new RuntimeException(
                    "Only a sphere in a 3D space can be sampled by this class.");
        }

        this.center = center;
        this.radius = radius;
    }

    /**
     * Uniform sampling of a 2-sphere in 3D space using the trig method
     * 
     * @see <a
     *      href="http://groups.google.com/group/sci.math.num-analysis/msg/965b969e46d6e582">N-dim
     *      spherical random number drawing</a>
     */
    public double[] nextSample() {
        double[] sample = new double[center.length];

        // Generate a point on a 2-sphere centered at the origin
        sample[2] = (Math.random() * 2 * radius) - radius;

        double t = (Math.random() * 2 * Math.PI);
        double r = Math.sqrt(Math.pow(radius, 2) - Math.pow(sample[2], 2));
        sample[0] = r * Math.cos(t);
        sample[1] = r * Math.sin(t);

        // Translate point
        sample[0] += center[0];
        sample[1] += center[1];
        sample[2] += center[2];
               
        return sample;
    }
    
    public int getDimensions() {
	return 3;
    }

    public double getArea() {
        return 4 * Math.PI * Math.pow(radius, 2);
    }

}
