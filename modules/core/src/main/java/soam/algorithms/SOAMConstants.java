

package soam.algorithms;

public interface SOAMConstants extends GWRConstants {

    /**
     * Whether or not boundary vertices have to be considered stable
     */
    public static final boolean BOUNDARY_IS_STABLE = false;

    /**
     * Learning rate multiplier for stable vertices
     */
    public static final double ETA_S = 0.02d;

    /**
     * If true, the insertion thresholds will be adapted
     * 
     */
    public static final boolean ADAPT_INSERTION_THRESHOLD = true;

    /**
     * Asymptotic, basal level for the insertion threshold decay.
     * 
     * @see SOAM#updateFiringCounters()
     */
    public static final double ALPHA_IT = 0.05d;

    /**
     * Time constant for the for the insertion threshold decay of singular
     * vertices.
     * 
     * @see SOAM#updateFiringCounters()
     */
    public static final double TAU_IT_HABITUATION = 3d;

    /**
     * Time constant for the for the insertion threshold recover of non-singular
     * vertices.
     * 
     * @see SOAM#updateFiringCounters()
     */
    public static final double TAU_IT_DISHABITUATION = 9d;

    /**
     * If true, it enables sampling in the neighborhood of vertices
     * 
     * @see SOAM#nextSample()
     */
    public static final boolean FOCALIZED_SAMPLING = false;

    /**
     * This factor multiplies neighborhood radius in order to obtain the actual
     * focalization radius
     * 
     * @see SOAM#nextSample()
     */
    public static final double FOCALIZATION_RADIUS = 2d;

    /**
     * If true, it forces the detection of off-boundary projection. Used with
     * simplicial adaptation only
     * 
     * @see SOAMConstants#SIMPLICIAL_ADAPTATION
     * @see SOAM#findProjection()
     */
    public static final boolean OFF_BOUNDARY_PROJECTIONS = false;

    /**
     * If true, it enables the adaptation of stable vertices with a rule using
     * simplicial features
     * 
     * @see SOAM#adaptPositions
     */
    public static final boolean SIMPLICIAL_ADAPTATION = false;

    /**
     * If true, it enables the adaptation of stable vertices with (modified)
     * Oja's Rule
     * 
     * @see SOAM#adaptPositions
     */
    public static final boolean OJA_RULE_ADAPTATION = false;

    /**
     * If true, it enables the adaptation of stable vertices using a very
     * simplified version of neural gas rule
     * 
     * @see SOAM#adaptPositions
     */
    public static final boolean NEURAL_GAS_ADAPTATION = false;

    /**
     * If true, it simply stops adaptation of stable vertices
     * 
     * @see SOAM#adaptPositions
     */
    public static final boolean STOP_NEIGHBOR_ADAPTATION = true;

    /**
     * If true, it stops aging vertices for stable vertices
     * 
     * @see SOAM#ageEdges
     */
    public static final boolean STABILIZE_REGULAR_NEIGHBORHOODS = true;

    /**
     * If non-zero, it causes the rapid aging of oldest winners
     * 
     * @see SOAM#ageEdges()
     */
    public static final double OLDEST_WINNER_AGING = 0;

    /**
     * States
     * 
     */
    public static final int ACTIVE = 0;

    public static final int HABITUATED = 1;

    public static final int CONNECTED = 2;

    public static final int SINGULAR = 3;

    public static final int DISK = 4;

    public static final int HALF_DISK = 5;

    public static final int PATCH = 6;

    public static final int BOUNDARY = 7;

}
