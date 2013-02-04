

package soam.algorithms;

import soam.Mesh;
import soam.sampling.Sampler;


public interface Algorithm {

    public boolean initialize(Sampler sampler);

    public void reset();
    
    public void reset(Mesh mesh);
    
    public void enableVertexCreation();
    
    public void disableVertexCreation();

    public void iteration();
    
    public long getTick();

    public boolean getVertexCreationLock();

    public Sampler sampler();
    
    public Mesh mesh();

    public double[] lastSampledPoint();

    public boolean isConvergent();
    
    public void orientMesh();
}
