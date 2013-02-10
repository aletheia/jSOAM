

package soam.sampling;

import java.util.ArrayList;
import java.awt.image.BufferedImage;

import soam.algorithms.Algorithm;

public interface Sampler {

    public boolean initialize(Algorithm algorithm);

    public void reset();

    public int getDimensions();

    public BufferedImage getBufferedImage();
    
    public ArrayList<double[]> getPointCloud();

    public String getDescription();

    public double[] nextSample();

    public double[] nextSample(final double[] point, double radius);

}
