

package soam.algorithms;

public interface GWRConstants {

    /**
     * The activity threshold for the gwr. Roughly, a sensible value could be
     * 1.e-P, where P (pixels) is the maximum activity radius.</br> <b>THIS
     * PARAMETER IS NO LONGER IN USE</b>
     * 
     * @see INSERTION_THRESHOLD
     */
    public final static double ACTIVITY_THRESHOLD = 1.e-8d;

    /**
     * Asymptotic, basal level for the firing counter decay (winning node).
     * NOTE: it just makes no sense to have ALPHA_B different form ALPHA_N.
     * 
     * @see GWR#updateFiringCounters()
     */
    public final static double ALPHA_W = 1.3d;

    /**
     * Asymptotic, basal level for the firing counter decay (neighbor nodes).
     * NOTE: it just makes no sense to have ALPHA_B different form ALPHA_N.
     * 
     * @see GWR#updateFiringCounters()
     */
    public final static double ALPHA_N = 1.3d;

    /**
     * Winner learning rate
     */
    public final static double EPSILON_W = 0.5d;

    /**
     * Neighbors learning rate
     */
    public final static double EPSILON_N = 0.005d;

    /**
     * Initial strength of the firing counter
     * 
     * @see GWR#updateFiringCounters()
     */
    public final static double INITIAL_STRENGTH = 1d;

    /**
     * The distance threshold below which a new vertex is created. Note that in
     * Marsland's paper a more elaborated, exponential formula is used. However,
     * Marsland himself adopts a simpler version in his MATLAB implementation.
     * 
     * @see <a href="http://www-ist.massey.ac.nz/smarsland/GWR.html"/>The Grow *
     *      When Required Network (MATLAB Version)< /a>
     */
    public final static double INSERTION_THRESHOLD = 24d;

    /**
     * Better keep the two constants equal.
     */
    public final static double FIRING_THRESHOLD =
	    (INITIAL_STRENGTH - (1 / ALPHA_W)) * 1.05d;

    /**
     * Basically, the formula below yields a value that is slightly more than
     * the basal level (i.e. when the decay is over, see the paper for details).
     * 
     * @see "Marsland, S. et al., A self-organising network that grows when
     *      required", Eqn. 16, p. 1045, Neural Networks 15 (2002)"
     */
    public final static double HABITUATION = FIRING_THRESHOLD;

    /**
     * The maximum age allowed
     */
    public final static int MAX_EDGE_AGE = 30;

    /**
     * The maximum number of vertices in the mesh
     */
    public final static int MAX_VERTICES = 100000;

    /**
     * Time constant for the firing counter decay (winning node).
     * 
     * @see GWR#updateFiringCounters()
     */
    public final static double TAU_W = 3.33d;

    /**
     * Time constant for the firing counter decay (neighbor nodes).
     * 
     * @see GWR#updateFiringCounters()
     */
    public final static double TAU_N = 14.33d;

}
