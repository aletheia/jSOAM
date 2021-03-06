

package soam.algorithms;

import java.util.Iterator;

import soam.algorithms.GWRConstants;
import soam.sampling.Sampler;
import soam.Edge;
import soam.Mesh;
import soam.Vertex;

/**
 * The basic implementation of the Grow-When-Required (GWR) algorithm
 * 
 * @see "Marsland, S., et al., A self-organising network that grows when
 *      required, <i>Neural Networks</i>, 15 (2002)"
 * 
 * 
 */
public class GWR implements Algorithm {

    protected static double HABITUATION = GWRConstants.HABITUATION;

    protected int MAX_VERTICES = GWRConstants.MAX_VERTICES;

    protected double ALPHA_W = GWRConstants.ALPHA_W;
    protected double ALPHA_N = GWRConstants.ALPHA_N;
    protected double EPSILON_W = GWRConstants.EPSILON_W;
    protected double EPSILON_N = GWRConstants.EPSILON_N;
    protected double INITIAL_STRENGTH = GWRConstants.INITIAL_STRENGTH;
    protected double TAU_W = GWRConstants.TAU_W;
    protected double TAU_N = GWRConstants.TAU_N;

    protected double INSERTION_THRESHOLD = GWRConstants.INSERTION_THRESHOLD;

    protected int MAX_EDGE_AGE = GWRConstants.MAX_EDGE_AGE;

    protected boolean VALIDATION = false;

    protected Mesh mesh;

    public Sampler sampler;

    protected long tick;

    protected double[] sampledPoint;

    protected Vertex bestMatching;
    protected Vertex secondBestMatching;

    protected double bestDistance;
    protected double secondBestDistance;

    protected Edge connectingEdge;

    protected Vertex newlyCreated;

    protected boolean vertexCreationLock = false;

    // Just statistics
    protected int verticesActive;
    protected int verticesHabituated;

    protected long tickOfLastVertexCreation;

    /**
     * Returns the edge connecting two given vertices or null, if this does not
     * exist
     * 
     * @param vertex
     * @param otherVertex
     * @return An edge or null
     */
    public static Edge findEdgeConnecting(Vertex vertex, Vertex otherVertex) {

	for (Edge edge : vertex.neighborhood) {
	    if (edge.hasVertex(otherVertex)) {
		return edge;
	    }
	}
	return null;
    }

    /**
     * Check if the vertex is HABITUATED
     * 
     * @param vertex
     * @return true if the condition is satisfied
     */
    public static boolean isHabituated(Vertex vertex) {
	return vertex.firingCounter < HABITUATION;
    }

    /**
     * This method the mesh
     * 
     */
    public Mesh mesh() {
	return mesh;
    }

    /**
     * This method the sampler
     * 
     */
    public Sampler sampler() {
	return sampler;
    }

    /**
     * This method returns the number of algorithm iterations so far
     * 
     */
    public long getTick() {
	return tick;
    }

    /**
     * This method returns the last sampled point
     * 
     */
    public double[] lastSampledPoint() {
	return sampledPoint;
    }

    /**
     * This method sets the GLOBAL insertion Threshold of the algorithm
     * 
     * @param threshold
     */
    public boolean initialize(Sampler sampler) {
	this.sampler = sampler;

	return true;
    }

    /**
     * Create two vertices at random positions
     * 
     */
    public void reset() {

	mesh = new Mesh();

	Vertex vertex = new Vertex(sampler.nextSample());
	vertex.firingCounter = INITIAL_STRENGTH;
	vertex.insertionThreshold = INSERTION_THRESHOLD;
	mesh.vertexlist.add(vertex);

	vertex = new Vertex(sampler.nextSample());
	vertex.firingCounter = INITIAL_STRENGTH;
	vertex.insertionThreshold = INSERTION_THRESHOLD;
	mesh.vertexlist.add(vertex);

	resetVariables();
    }

    public void disableVertexCreation() {
	vertexCreationLock = true;
    }

    public void enableVertexCreation() {
	vertexCreationLock = false;
    }

    /**
     * Just accept the new mesh as the default one
     * 
     */
    public void reset(Mesh mesh) {

	this.mesh = mesh;

	resetVariables();
    }

    /**
     * Reset all relevant variables
     * 
     */
    protected void resetVariables() {
	tick = 0;
	verticesActive = 0;
	verticesHabituated = 0;

	tickOfLastVertexCreation = 0;

	vertexCreationLock = false;
    }

    /**
     * One iteration of the algorithm
     * 
     */
    public void iteration() {

	tick++;

	// Step 1: Generate a data sample
	// Step 2: Compute distances
	// Step 3: Select the best matching vertex and the second best

	sampleAndFindWinners();

	// Step 4: Create a new edge or refresh the existing one
	updateOrCreateEdge();

	// Step 8: Age edges in the winner's neighborhood
	// MODIFICATION: THIS STEP HAS BEEN MOVED BEFORE STEP 5
	ageEdges();

	// Step 5: Calculate the activity of the best matching unit
	if (newVertexRequired()) {

	    // Step 6: If required, create a new vertex and update the mesh
	    createVertex();

	}

	// Step 9: update the firing counters of the winner and its
	// neighbors
	// MODIFICATION: THIS STEP HAS BEEN MOVED BEFORE STEP 7
	updateFiringCounters();

	// Step 7: Otherwise, adapt positions of the winner and its
	// neighbors
	// MODIFICATION: THIS STEP IS NO LONGER CONDITIONAL
	adaptPositions();

	// Step 10: Check if there are any vertices or edges to delete
	// MODIFICATION: THIS STEP HAS BEEN INCLUDED IN STEP 8

	// MODIFICATION: THE FOLLOWING STEP HAS BEEN ADDED
	updateVertex();

	// MODIFICATION: THE FOLLOWING STEP HAS BEEN ADDED
	updateMesh();

	// Debugging purposes only
	if (VALIDATION) {
	    validate();
	}
    }    

    /**
     * Select a sample point. In this class, it just returns a sample
     * 
     * @param sampler
     */
    protected double[] nextSample() {
	return sampler.nextSample();
    }

    /**
     * Select a sample point and find the bestMaching and secondBestMatching
     * vertices
     * 
     * @param sampler
     */
    protected void sampleAndFindWinners() {

	sampledPoint = nextSample();

	bestMatching = null;
	secondBestMatching = null;

	bestDistance = Double.MAX_VALUE;
	secondBestDistance = Double.MAX_VALUE;

	verticesHabituated = 0;
	verticesActive = 0;

	for (Vertex vertex : mesh.vertexlist) {

	    if (!isHabituated(vertex)) {
		verticesHabituated++;
	    } else {
		verticesActive++;
	    }

	    double distance =
		    AlgebraicOperatorsNd.normSquare(sampledPoint,
			    vertex.position);

	    if (distance < bestDistance) {

		secondBestMatching = bestMatching;
		secondBestDistance = bestDistance;

		bestMatching = vertex;
		bestDistance = distance;

	    } else if (distance < secondBestDistance) {

		secondBestMatching = vertex;
		secondBestDistance = distance;

	    }
	}

	bestDistance = Math.sqrt(bestDistance);
	secondBestDistance = Math.sqrt(secondBestDistance);

	newlyCreated = null;
    }

    /**
     * Updates the reference vectors of the winner and the neighborhood
     */
    protected void adaptPositions() {

	// Updates the reference vectors of the winner
	for (int i = 0; i < bestMatching.position.length; i++) {
	    bestMatching.position[i] +=
		    EPSILON_W * bestMatching.firingCounter
			    * (sampledPoint[i] - bestMatching.position[i]);
	}

	// Updates all vertices connected to the winner
	for (Edge edge : bestMatching.neighborhood) {
	    Vertex vertex = edge.getOtherVertex(bestMatching);
	    for (int i = 0; i < vertex.position.length; i++) {
		vertex.position[i] +=
			EPSILON_N * vertex.firingCounter
				* (sampledPoint[i] - vertex.position[i]);
	    }
	}
    }

    /**
     * This method either refreshes the age of the edge between the winner and
     * the second best unit or creates a new edge if it does not exist
     */
    protected void updateOrCreateEdge() {

	connectingEdge = findEdgeConnecting(bestMatching, secondBestMatching);

	if (connectingEdge == null) {
	    // Create the new edge
	    connectingEdge = new Edge(bestMatching, secondBestMatching);
	    mesh.edgelist.add(connectingEdge);
	    bestMatching.neighborhood.add(connectingEdge);
	    secondBestMatching.neighborhood.add(connectingEdge);

	    touchNeighborhood(bestMatching);
	    touch(bestMatching);
	    touchNeighborhood(secondBestMatching);
	    touch(secondBestMatching);
	} else {
	    connectingEdge.age = 0;
	}
    }

    /**
     * Adds a new vertex between the best matching unit and the second best
     * matching
     */
    protected void createVertex() {

	// Compute the new reference vector
	double[] newPosition = new double[bestMatching.position.length];
	for (int i = 0; i < bestMatching.position.length; i++) {
	    newPosition[i] = (bestMatching.position[i] + sampledPoint[i]) / 2;
	}

	// Create the new node
	Vertex newVertex = new Vertex(newPosition);
	newVertex.firingCounter = INITIAL_STRENGTH;
	newVertex.insertionThreshold = INSERTION_THRESHOLD;
	mesh.vertexlist.add(newVertex);

	// Remove the edge between best matching and the second best
	Edge edge = findEdgeConnecting(bestMatching, secondBestMatching);
	if (edge != null) {
	    edge.start.neighborhood.remove(edge);
	    edge.end.neighborhood.remove(edge);
	    mesh.edgelist.remove(edge);
	}

	// Connect to both best matching and second best
	Edge newEdge1 = new Edge(newVertex, bestMatching);
	mesh.edgelist.add(newEdge1);
	bestMatching.neighborhood.add(newEdge1);
	newVertex.neighborhood.add(newEdge1);

	touch(newVertex);
	touchNeighborhood(bestMatching);
	touch(bestMatching);

	newlyCreated = newVertex;
	tickOfLastVertexCreation = tick;

	Edge newEdge2 = new Edge(newVertex, secondBestMatching);
	mesh.edgelist.add(newEdge2);
	secondBestMatching.neighborhood.add(newEdge2);
	newVertex.neighborhood.add(newEdge2);

	touchNeighborhood(secondBestMatching);
	touch(secondBestMatching);

    }

    /**
     * In this class, it just propagates the call
     * 
     * @see ageEdges(Vertex)
     */
    protected void ageEdges() {

	ageEdges(bestMatching);
    }

    /**
     * Increase the age in the neighborhood of the best matching vertex. Then
     * verify edges ages and remove edges that are too old, then remove vertices
     * that became isolated, as well. Note that edge aging is a local process:
     * therefore, only edges and vertices that are neighbor to the best matching
     * vertex could have been affected.
     */
    protected void ageEdges(Vertex vertex) {

	Vertex otherVertex;
	Edge edge;
	// Iterator must be used here, as edges might be removed
	Iterator<Edge> iterator = vertex.neighborhood.listIterator();
	while (iterator.hasNext()) {
	    edge = iterator.next();
	    otherVertex = edge.getOtherVertex(vertex);

	    edge.age += 1;

	    if (edge.age > MAX_EDGE_AGE) {
		iterator.remove();
		otherVertex.neighborhood.remove(edge);
		mesh.edgelist.remove(edge);

		touchNeighborhood(vertex);
		touch(vertex);
		touchNeighborhood(otherVertex);
		touch(otherVertex);

		// Check if the other node is now isolated
		if (otherVertex.neighborhood.size() == 0) {
		    mesh.vertexlist.remove(otherVertex);
		}
	    }
	}

	// Check if this vertex is now isolated
	if (vertex.neighborhood.size() == 0) {
	    mesh.vertexlist.remove(vertex);
	}
    }

    /**
     * Perform any verification activity, if required (for debugging purposes
     * only)
     */
    protected void validate() {
	// Do nothing by default
    }

    /**
     * Checks if the activity level of the best matching node is sufficiently
     * high.
     * 
     * @return true if the condition is satisfied
     */
    protected boolean aboveInsertionThreshold() {

	return bestDistance > bestMatching.insertionThreshold;
    }

    /**
     * Checks if a new vertex can indeed be created
     * 
     * @return true if the condition is satisfied
     */
    protected boolean canCreateVertex() {

	return !vertexCreationLock && mesh.vertexlist.size() < MAX_VERTICES;
    }

    /**
     * Checks the GWR condition for splitting the vertex
     * 
     * @return true if the condition is satisfied
     */
    protected boolean newVertexRequired() {

	return canCreateVertex() && isHabituated(bestMatching)
		&& aboveInsertionThreshold();
    }

    /**
     * Updates the firing counters of the winning vertex and its neighbors. The
     * firing counter decays with time constant TAU, until it reaches the basal
     * level INITIAL_STRENGTH - (1 / ALFA).
     * 
     * @see "Marsland, S. et al., A self-organising network that grows when
     *      required", Eqn. 16, p. 1045, Neural Networks 15 (2002)"
     */
    protected void updateFiringCounters() {

	// Update the best matching vertex
	bestMatching.firingCounter +=
		((ALPHA_W * (INITIAL_STRENGTH - bestMatching.firingCounter)) - 1)
			/ TAU_W;

	// Update the neighbors
	Vertex vertex;
	for (Edge edge : bestMatching.neighborhood) {
	    vertex = edge.getOtherVertex(bestMatching);
	    vertex.firingCounter +=
		    ((ALPHA_N * (INITIAL_STRENGTH - vertex.firingCounter)) - 1)
			    / TAU_N;
	}
    }

    /**
     * This method will be called whenever the neighborhood of a vertex has been
     * altered
     * 
     * @param vertex
     */
    protected void touchNeighborhood(Vertex vertex) {
	for (Edge edge : vertex.neighborhood) {
	    touch(edge.getOtherVertex(vertex));
	}
    }

    /**
     * This method will be called whenever the neighborhood of a vertex has been
     * altered
     * 
     * @param vertex
     */
    protected void touch(Vertex vertex) {
	vertex.touched = true;
    }

    /**
     * Updates the best matching vertex, if required
     */
    protected void updateVertex() {

	if (bestMatching.touched) {
	    bestMatching.touched = false;
	}
    }

    /**
     * Updates the mesh, if required
     */
    protected void updateMesh() {
	// Just do nothing, by default
    }

    public String toString() {
	return this.getClass().getSimpleName() + "(\"" + sampler + "\")";
    }

    public boolean isConvergent() {
	return false;
    }
    
    public void orientMesh() {
	throw new RuntimeException("Method not implementd.");
    }

    public void setALPHA_W(double aw) {
	ALPHA_W = aw;
    }

    public double getALPHA_W() {
	return ALPHA_W;
    }

    public void setALPHA_N(double an) {
	ALPHA_N = an;
    }

    public double getALPHA_N() {
	return ALPHA_N;
    }

    public void setEPSILON_W(double ew) {
	EPSILON_W = ew;
    }

    public double getEPSILON_W() {
	return EPSILON_W;
    }

    public void setEPSILON_N(double en) {
	EPSILON_N = en;
    }

    public double getEPSILON_N() {
	return EPSILON_N;
    }

    public void setINITIAL_STRENGTH(double is) {
	INITIAL_STRENGTH = is;
    }

    public double getINITIAL_STRENGTH() {
	return INITIAL_STRENGTH;
    }

    public void setINSERTION_THRESHOLD(double threshold) {
	INSERTION_THRESHOLD = threshold;

	for (Vertex vertex : mesh.vertexlist) {
	    vertex.insertionThreshold =
		    Math.min(vertex.insertionThreshold, INSERTION_THRESHOLD);
	}
    }

    public double getINSERTION_THRESHOLD() {
	return INSERTION_THRESHOLD;
    }

    public void setHABITUATION(double h) {
	HABITUATION = h;
    }

    public double getHABITUATION() {
	return HABITUATION;
    }

    public void setMAX_VERTICES(int mv) {
	MAX_VERTICES = mv;
    }

    public int getMAX_VERTICES() {
	return MAX_VERTICES;
    }

    public int getMAX_EDGE_AGE() {
	return MAX_EDGE_AGE;
    }

    public void setMAX_EDGE_AGE(int max_edge_age) {
	MAX_EDGE_AGE = max_edge_age;
    }

    public double getTAU_W() {
	return TAU_W;
    }

    public void setTAU_W(double tau_w) {
	TAU_W = tau_w;
    }

    public double getTAU_N() {
	return TAU_N;
    }

    public void setTAU_N(double tau_n) {
	TAU_N = tau_n;
    }

    public boolean isVALIDATION() {
	return VALIDATION;
    }

    public void setVALIDATION(boolean validation) {
	VALIDATION = validation;
    }

    public int getEdges() {
	return mesh.edgelist.size();
    }

    public int getVerticesActive() {
	return verticesActive;
    }

    public int getVerticesHabituated() {
	return verticesHabituated;
    }

    public long getTickOfLastVertexCreation() {
	return tickOfLastVertexCreation;
    }

    public boolean getVertexCreationLock() {
	return vertexCreationLock;
    }

}
