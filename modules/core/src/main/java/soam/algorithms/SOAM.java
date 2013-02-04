

package soam.algorithms;

import java.util.ArrayList;
import java.util.Iterator;

import soam.Edge;
import soam.Vertex;

public abstract class SOAM extends GWR {

    protected static boolean BOUNDARY_IS_STABLE =
	    SOAMConstants.BOUNDARY_IS_STABLE;

    protected double ETA_S = SOAMConstants.ETA_S;

    protected boolean ADAPT_INSERTION_THRESHOLD =
	    SOAMConstants.ADAPT_INSERTION_THRESHOLD;

    // FIXME: the initial values for these params should be in SOAMConstants
    protected boolean ADAPT_HALF_DISK = false;

    // FIXME: the initial values for these params should be in SOAMConstants
    protected boolean ADAPT_CONNECTED = false;

    protected double ALPHA_IT = SOAMConstants.ALPHA_IT;

    protected double INSERTION_THRESHOLD_MIN =
	    SOAMConstants.INSERTION_THRESHOLD - (1 / SOAMConstants.ALPHA_IT);

    protected double TAU_IT_HABITUATION = SOAMConstants.TAU_IT_HABITUATION;
    protected double TAU_IT_DISHABITUATION =
	    SOAMConstants.TAU_IT_DISHABITUATION;

    protected boolean STABILIZE_REGULAR_NEIGHBORHOODS =
	    SOAMConstants.STABILIZE_REGULAR_NEIGHBORHOODS;

    // FIXME: the initial values for these params should be in SOAMConstants
    protected long MAX_VERTEX_INACTIVITY = 0l;

    protected boolean FOCALIZED_SAMPLING = SOAMConstants.FOCALIZED_SAMPLING;
    protected double FOCALIZATION_RADIUS = SOAMConstants.FOCALIZATION_RADIUS;

    // FIXME: the initial values for these params should be in SOAMConstants
    protected boolean CHECK_EDGE_VIABILITY = false;

    // FIXME: the initial values for these params should be in SOAMConstants
    protected int ACCUMULATION = 0;

    protected boolean SIMPLICIAL_ADAPTATION =
	    SOAMConstants.SIMPLICIAL_ADAPTATION;

    // FIXME: the initial values for these params should be in SOAMConstants
    protected boolean CENTROIDAL_ADAPTATION = false;

    protected boolean NEURAL_GAS_ADAPTATION =
	    SOAMConstants.NEURAL_GAS_ADAPTATION;

    protected boolean STOP_NEIGHBOR_ADAPTATION =
	    SOAMConstants.STOP_NEIGHBOR_ADAPTATION;

    protected boolean OFF_BOUNDARY_PROJECTIONS =
	    SOAMConstants.OFF_BOUNDARY_PROJECTIONS;

    // FIXME: the initial values for these params should be in SOAMConstants
    protected boolean COMPUTE_QUANTIZATION_ERROR = false;

    // FIXME: the initial values for these params should be in SOAMConstants
    protected boolean COMPUTE_DISTANCE_ERROR = false;
    protected double VERTEX_ERROR_THRESHOLD = 0d;

    protected long COMPUTE_STATISTICS_PERIOD = 0;

    protected double EPSILON_E = 0.002d;

    protected long timeBaseline;
    protected long timeIterationBaseline;

    protected Vertex thirdBestMatching;

    protected double thirdBestDistance;

    protected int verticesConnected;
    protected int verticesSingular;
    protected int verticesHalfdisk;
    protected int verticesDisk;
    protected int verticesPatch;
    protected int verticesBoundary;

    protected long tickOfFirstConvergence;
    protected long timeOfFirstConvergence;

    protected long timeElapsed;
    protected long timeElapsedOnSampleAndFindWinners;

    protected int verticesNotStable;
    protected double stableVerticesFraction;

    protected double averageLatencyNotStable;

    protected double vertexMaximumError;
    protected double vertexAverageError;
    protected double vertexMinimumError;

    protected double quantizationError;

    protected double edgeMaximumLength;
    protected double edgeAverageLength;
    protected double edgeMinimumLength;
    protected double edgeDelaunayFraction;

    protected boolean dueToVertexErrorThreshold = false;

    public static boolean isConnected(Vertex vertex) {
	return vertex.state != SOAMConstants.ACTIVE
		&& vertex.state != SOAMConstants.HABITUATED;
    }

    public static boolean isHalfDisk(Vertex vertex) {
	return vertex.state == SOAMConstants.HALF_DISK
		|| vertex.state == SOAMConstants.BOUNDARY;
    }

    public static boolean isDisk(Vertex vertex) {
	return vertex.state == SOAMConstants.DISK
		|| vertex.state == SOAMConstants.PATCH;
    }

    public static boolean isBoundary(Vertex vertex) {
	return vertex.state == SOAMConstants.BOUNDARY;
    }

    public static boolean isPatch(Vertex vertex) {
	return vertex.state == SOAMConstants.PATCH;
    }

    public static boolean isSingular(Vertex vertex) {
	return vertex.state == SOAMConstants.SINGULAR;
    }

    public static boolean isRegular(Vertex vertex) {
	return vertex.state == SOAMConstants.DISK
		|| vertex.state == SOAMConstants.HALF_DISK
		|| vertex.state == SOAMConstants.PATCH
		|| vertex.state == SOAMConstants.BOUNDARY;
    }

    public static boolean isStable(Vertex vertex) {
	return vertex.state == SOAMConstants.PATCH
		|| (BOUNDARY_IS_STABLE && vertex.state == SOAMConstants.BOUNDARY);
    }

    /**
     * Verify that all the neighbors are habituated
     * 
     * @param vertex
     * @return true if the condition is satisfied
     */
    public static boolean isNeighborhoodHabituated(Vertex vertex) {
	for (Edge edge : vertex.neighborhood) {
	    if (edge.getOtherVertex(vertex).state < SOAMConstants.HABITUATED) {
		return false;
	    }
	}
	return true;
    }

    /**
     * Verify that all the neighbors are connected
     * 
     * @param vertex
     * @return true if the condition is satisfied
     */
    public static boolean isNeighborhoodConnected(Vertex vertex) {
	for (Edge edge : vertex.neighborhood) {
	    if (edge.getOtherVertex(vertex).state < SOAMConstants.CONNECTED) {
		return false;
	    }
	}
	return true;
    }

    /**
     * Verify that all the neighbors are regular
     * 
     * @param vertex
     * @return true if the condition is satisfied
     */
    public static boolean isNeighborhoodRegular(Vertex vertex) {
	for (Edge edge : vertex.neighborhood) {
	    if (!isRegular(edge.getOtherVertex(vertex))) {
		return false;
	    }
	}
	return true;
    }

    /**
     * Verify that all the neighbors are regular
     * 
     * @param vertex
     * @return true if the condition is stable
     */
    public static boolean isNeighborhoodStable(Vertex vertex) {
	for (Edge edge : vertex.neighborhood) {
	    if (!isStable(edge.getOtherVertex(vertex))) {
		return false;
	    }
	}
	return true;
    }

    /**
     * The radius of a neighborhood is equal to the length of its longer edge
     * (i.e. a cautious definition)
     * 
     * @param vertex
     * 
     * @return the value of the radius
     */
    public static double getNeighborhoodRadius(Vertex vertex) {
	double[] o = vertex.position;
	double distance = 0f;

	for (Edge edge : vertex.neighborhood) {
	    double[] p = edge.getOtherVertex(vertex).position;
	    double tmp = AlgebraicOperators2d.normSquare(p, o);
	    if (tmp > distance) {
		distance = tmp;
	    }
	}

	return Math.sqrt(distance);
    }

    /**
     * Compute the projection of a point over an edge
     * 
     * @param p
     *            The point to be projected
     * @param v0
     *            First edge vertex
     * @param v1
     *            Second edge vertex
     * 
     * @return The point on the edge being closer to p
     * 
     * @see "Schneider, P. J., Eberly, D. H., <i>Geometric Tools For Computer
     *      Graphics</i>, Morgan Kaufmann Publishers, 2003, Section 10.2"
     */
    public static double[] findProjection(double[] p, double[] v0, double[] v1) {

	double t = AlgebraicOperatorsNd.scalarProduct(v1, p, v0);

	double den = AlgebraicOperatorsNd.scalarProduct(v1, v1, v0);

	t /= (den != 0 ? den : Double.MIN_VALUE);

	// Assumption: p cannot be closer to v1 than to v0, hence t < 1
	if (t <= 0) {
	    return v0;
	} else {
	    double[] q = new double[p.length];

	    for (int i = 0; i < q.length; ++i) {
		q[i] = v0[i] + (t * (v1[i] - v0[i]));
	    }

	    return q;
	}
    }

    /**
     * Compute the projection of a point over a triangle
     * 
     * 
     * @param p
     *            The point to be projected
     * @param v0
     *            First triangle vertex
     * @param v1
     *            Second triangle vertex
     * @param v2
     *            Third triangle vertex
     * 
     * @return The point on the triangle being closer to p
     * 
     * @see "Schneider, P. J., Eberly, D. H., <i>Geometric Tools For Computer
     *      Graphics</i>, Morgan Kaufmann Publishers, 2003, Section 10.3.2"
     */
    public static double[] findProjection(double[] p, double[] v0, double[] v1,
	    double[] v2) {

	// Compute projection coefficients s and t
	double a = AlgebraicOperatorsNd.scalarProduct(v1, v1, v0);
	double b = AlgebraicOperatorsNd.scalarProduct(v1, v2, v0);
	double c = AlgebraicOperatorsNd.scalarProduct(v2, v2, v0);
	double d = -AlgebraicOperatorsNd.scalarProduct(v1, p, v0);
	double e = -AlgebraicOperatorsNd.scalarProduct(v2, p, v0);

	double den = a * c - b * b;
	den = (den != 0 ? den : Double.MIN_VALUE);

	double s = (b * e - c * d) / den;

	double t = (b * d - a * e) / den;

	// Assumption: p cannot be closer to either v1 or v2 than to v0,
	// hence s < 1 and t < 1
	if (s <= 0 && t <= 0) {
	    return v0;

	} else if (s <= 0) {
	    // The closer point is on the edge v0, v2
	    den = (c != 0 ? c : Double.MIN_VALUE);
	    t = -e / den;

	    double[] q = new double[p.length];

	    for (int i = 0; i < q.length; ++i) {
		q[i] = v0[i] + (t * (v2[i] - v0[i]));
	    }

	    return q;

	} else if (t <= 0) {
	    // The closer point is on the edge v0, v1
	    den = (a != 0 ? a : Double.MIN_VALUE);
	    s = -d / den;

	    double[] q = new double[p.length];

	    for (int i = 0; i < q.length; ++i) {
		q[i] = v0[i] + (s * (v1[i] - v0[i]));
	    }

	    return q;

	} else {
	    double[] q = new double[p.length];

	    for (int i = 0; i < q.length; ++i) {
		q[i] = v0[i] + (s * (v1[i] - v0[i])) + (t * (v2[i] - v0[i]));
	    }

	    return q;
	}
    }

    public static double[] findBarycenter(double[] p1, double[] p2) {
	double[] barycenter = new double[p1.length];
	double lambda = 0.5f;

	for (int i = 0; i < barycenter.length; i++) {
	    barycenter[i] = lambda * p1[i] + lambda * p2[i];
	}

	return barycenter;
    }

    public static double[] findBarycenter(double[] p1, double[] p2, double p3[]) {
	double[] barycenter = new double[p1.length];
	double lambda = (double) 1 / 3;

	for (int i = 0; i < barycenter.length; i++) {
	    barycenter[i] = lambda * p1[i] + lambda * p2[i] + lambda * p3[i];
	}

	return barycenter;
    }

    protected boolean delaunayEdge(Edge edge, boolean tryToFix) {
	Vertex[] sharedVertices = new Vertex[2];

	for (int i = 0; i < edge.start.neighborhood.size(); i++) {
	    Vertex vertex =
		    edge.start.neighborhood.get(i).getOtherVertex(edge.start);
	    for (int j = 0; j < edge.end.neighborhood.size(); j++) {
		if (edge.end.neighborhood.get(j).getOtherVertex(edge.end) == vertex) {
		    if (sharedVertices[0] == null) {
			sharedVertices[0] = vertex;
		    } else if (sharedVertices[1] == null) {
			sharedVertices[1] = vertex;
		    } else {
			// Overconnected, cannot fix
			return false;
		    }
		}
	    }
	}

	if (sharedVertices[0] == null) {
	    // Underconnected, cannot fix
	    return false;
	} else if (sharedVertices[1] == null) {
	    if (isHalfDisk(edge.start) && isHalfDisk(edge.end)) {
		return true;
	    } else {
		// Underconnected, cannot fix
		return false;
	    }
	} else {
	    if (AlgebraicOperatorsNd.normSquare(edge.start.position,
		    edge.end.position) >= AlgebraicOperatorsNd.normSquare(
		    sharedVertices[0].position, sharedVertices[1].position)) {

		if (tryToFix) {
		    if (findEdgeConnecting(sharedVertices[0], sharedVertices[1]) != null) {
			return false;
		    } else {
			// OK, flip the edge

			edge.start.neighborhood.remove(edge);
			edge.end.neighborhood.remove(edge);

			sharedVertices[0].neighborhood.add(edge);
			sharedVertices[1].neighborhood.add(edge);

			edge.start = sharedVertices[0];
			edge.end = sharedVertices[1];

			// Refresh it (it's a new one)
			edge.age = 0;

			return true;
		    }
		} else {
		    return false;
		}
	    }
	}

	return true;

    }

    protected boolean isDelaunay(Edge edge) {
	return delaunayEdge(edge, false);
    }

    /**
     * Reset all relevant variables
     * 
     */
    @Override
    protected void resetVariables() {
	super.resetVariables();

	timeElapsed = 0;
	timeElapsedOnSampleAndFindWinners = 0;

	verticesConnected = 0;
	verticesSingular = 0;
	verticesHalfdisk = 0;
	verticesDisk = 0;
	verticesPatch = 0;
	verticesBoundary = 0;

	timeBaseline = 0;

	tickOfFirstConvergence = 0;
	timeOfFirstConvergence = 0;
	stableVerticesFraction = 0d;

	vertexMaximumError = 0d;
	vertexAverageError = 0d;
	vertexMinimumError = 0d;

	quantizationError = 0d;

	edgeMaximumLength = 0d;
	edgeAverageLength = 0d;
	edgeMinimumLength = 0d;
	edgeDelaunayFraction = 0d;

    }

    /**
     * Select a sample point. Use localized sampling, if required.</br> CAUTION:
     * focalization is implemented via rejection sampling, so it might slow down
     * the execution significantly.
     * 
     * @param sampler
     */
    @Override
    protected double[] nextSample() {
	if (FOCALIZED_SAMPLING) {
	    if (stableVerticesFraction == 1d) {
		return super.nextSample();
	    }

	    Vertex vertex = null;
	    boolean hit = false;
	    while (!hit) {
		vertex =
			mesh.vertexlist
				.get((int) (Math.random() * mesh.vertexlist
					.size()));
		if (!isStable(vertex) || Math.random() > stableVerticesFraction) {
		    hit = true;
		}
	    }

	    if (isStable(vertex)) {
		return sampler.nextSample();
	    } else {
		double radius =
			getNeighborhoodRadius(vertex) * FOCALIZATION_RADIUS;
		return sampler.nextSample(vertex.position, radius);
	    }

	} else {
	    return super.nextSample();
	}
    }

    /**
     * Checks the condition for splitting the vertex
     * 
     * @return true if the condition is satisfied
     */
    @Override
    protected boolean newVertexRequired() {
	dueToVertexErrorThreshold = false;

	if (vertexCreationLock) {
	    return false;

	} else if (!super.newVertexRequired()) {

	    if (VERTEX_ERROR_THRESHOLD > 0 && isStable(bestMatching)) {
		if ((bestMatching.error + secondBestMatching.error) / 2 > VERTEX_ERROR_THRESHOLD) {
		    dueToVertexErrorThreshold = true;

		    return true;
		}
	    }
	    return false;
	} else {
	    return true;
	}
    }

    /**
     * Select a sample point and find the bestMaching and secondBestMatching
     * vertices
     * 
     */
    @Override
    @SuppressWarnings("unused")
    protected void sampleAndFindWinners() {

	if (timeBaseline == 0l) {
	    timeBaseline = System.nanoTime();
	}

	timeIterationBaseline = System.nanoTime();
	timeElapsed = timeIterationBaseline - timeBaseline;

	sampledPoint = nextSample();

	bestMatching = null;
	secondBestMatching = null;

	bestDistance = Double.MAX_VALUE;
	secondBestDistance = Double.MAX_VALUE;

	verticesActive = 0;
	verticesHabituated = 0;
	verticesConnected = 0;
	verticesSingular = 0;
	verticesDisk = 0;
	verticesHalfdisk = 0;
	verticesPatch = 0;
	verticesBoundary = 0;

	averageLatencyNotStable = 0;

	if (COMPUTE_DISTANCE_ERROR) {
	    vertexMinimumError = Double.MAX_VALUE;
	    vertexMaximumError = 0d;
	    vertexAverageError = 0d;
	}

	for (Vertex vertex : mesh.vertexlist) {

	    // Compute statistics
	    switch (vertex.state) {

	    case SOAMConstants.ACTIVE:
		verticesActive++;
		break;

	    case SOAMConstants.HABITUATED:
		verticesHabituated++;
		break;

	    case SOAMConstants.CONNECTED:
		verticesConnected++;
		break;

	    case SOAMConstants.SINGULAR:
		verticesSingular++;
		break;

	    case SOAMConstants.DISK:
		verticesDisk++;
		break;

	    case SOAMConstants.HALF_DISK:
		verticesHalfdisk++;
		break;

	    case SOAMConstants.PATCH:
		verticesPatch++;
		break;

	    case SOAMConstants.BOUNDARY:
		verticesBoundary++;
		break;
	    }

	    double distance =
		    AlgebraicOperatorsNd.normSquare(vertex.position,
			    sampledPoint);

	    if (distance < bestDistance) {

		thirdBestMatching = secondBestMatching;
		thirdBestDistance = secondBestDistance;

		secondBestMatching = bestMatching;
		secondBestDistance = bestDistance;

		bestMatching = vertex;
		bestDistance = distance;

	    } else if (distance < secondBestDistance) {

		thirdBestMatching = secondBestMatching;
		thirdBestDistance = secondBestDistance;

		secondBestMatching = vertex;
		secondBestDistance = distance;

	    } else if (distance < thirdBestDistance) {

		thirdBestMatching = vertex;
		thirdBestDistance = distance;
	    }

	    if (!isStable(vertex)) {
		averageLatencyNotStable += tick - vertex.lastWin;
	    }

	    if (COMPUTE_DISTANCE_ERROR) {
		if (vertex.error < vertexMinimumError) {
		    vertexMinimumError = vertex.error;
		}
		if (vertex.error > vertexMaximumError) {
		    vertexMaximumError = vertex.error;
		}
		vertexAverageError += vertex.error;
	    }
	}

	bestDistance = Math.sqrt(bestDistance);
	secondBestDistance = Math.sqrt(secondBestDistance);

	bestMatching.lastWin = tick;

	verticesNotStable =
		verticesActive + verticesHabituated + verticesConnected
			+ verticesSingular + verticesDisk + verticesHalfdisk;
	if (!BOUNDARY_IS_STABLE) {
	    verticesNotStable += verticesBoundary;
	}

	if (verticesNotStable == 0 && tickOfFirstConvergence == 0) {
	    tickOfFirstConvergence = tick;
	    timeOfFirstConvergence = System.nanoTime() - timeBaseline;
	}

	stableVerticesFraction =
		1 - (double) verticesNotStable / mesh.vertexlist.size();

	if (verticesNotStable > 0) {
	    averageLatencyNotStable /= verticesNotStable;
	}

	vertexAverageError /= mesh.vertexlist.size();

	if (COMPUTE_STATISTICS_PERIOD > 0
		&& tick % COMPUTE_STATISTICS_PERIOD == 0) {

	    if (COMPUTE_QUANTIZATION_ERROR) {
		quantizationError = computeQuantizationError();
	    }

	    // References to edges are kept just for debugging
	    Edge minimumErrorEdge = null;
	    Edge maximumErrorEdge = null;
	    Edge minimumLengthEdge = null;
	    Edge maximumLengthEdge = null;

	    int delaunayEdges = 0;

	    edgeMinimumLength = Double.MAX_VALUE;
	    edgeMaximumLength = 0d;
	    edgeAverageLength = 0d;

	    for (Edge edge : mesh.edgelist) {
		double length =
			AlgebraicOperatorsNd.norm(edge.end.position,
				edge.start.position);

		if (length < edgeMinimumLength) {
		    edgeMinimumLength = length;
		    minimumLengthEdge = edge;
		}
		if (length > edgeMaximumLength) {
		    edgeMaximumLength = length;
		    maximumLengthEdge = edge;
		}
		edgeAverageLength += length;

		if (isDelaunay(edge)) {
		    delaunayEdges++;
		}
	    }

	    edgeAverageLength /= mesh.edgelist.size();
	    edgeDelaunayFraction =
		    delaunayEdges / (double) mesh.edgelist.size();
	}

	timeElapsedOnSampleAndFindWinners +=
		(System.nanoTime() - timeIterationBaseline);
    }

    /**
     * This method specialization verifies whether the connecting edge is viable
     * and deletes it if it is not
     */
    @Override
    protected void updateOrCreateEdge() {

	super.updateOrCreateEdge();

	if (!isViableEdge(connectingEdge)) {
	    // Delete the edge
	    mesh.edgelist.remove(connectingEdge);
	    bestMatching.neighborhood.remove(connectingEdge);
	    secondBestMatching.neighborhood.remove(connectingEdge);

	    touchNeighborhood(bestMatching);
	    touch(bestMatching);
	    touchNeighborhood(secondBestMatching);
	    touch(secondBestMatching);

	    connectingEdge = null;
	    // There is no check that either vertex remains unconnected:
	    // This is intentional
	}
    }

    protected boolean isViableEdge(Edge edge) {
	if (!CHECK_EDGE_VIABILITY
		|| (!isHabituated(edge.start) && !isHabituated(edge.end))) {

	    return true;
	} else {
	    double distance =
		    AlgebraicOperatorsNd.norm(edge.start.position,
			    edge.end.position);

	    return distance < edge.start.insertionThreshold
		    + edge.end.insertionThreshold;
	}
    }

    /**
     * Add state to vertex or, if required, edge error computation
     */
    @Override
    protected void createVertex() {
	super.createVertex();

	// Make it 'winner' on this tick, to avoid premature removal
	newlyCreated.lastWin = tick;

	if (dueToVertexErrorThreshold) {
	    // Complete connection and preserve overall topology
	    Vertex vertex;
	    for (Edge edge : bestMatching.neighborhood) {
		vertex = edge.getOtherVertex(bestMatching);
		if (vertex != newlyCreated
			&& findEdgeConnecting(vertex, secondBestMatching) != null) {
		    // Connect to newly created
		    Edge newEdge = new Edge(vertex, newlyCreated);
		    mesh.edgelist.add(newEdge);
		    vertex.neighborhood.add(newEdge);
		    newlyCreated.neighborhood.add(newEdge);
		    touch(vertex);
		}
	    }

	    // Adjust firing counter
	    newlyCreated.firingCounter =
		    (bestMatching.firingCounter + secondBestMatching.firingCounter) / 2;

	    // Distribute error
	    double error = (bestMatching.error + secondBestMatching.error) / 3;
	    bestMatching.error = error;
	    secondBestMatching.error = error;
	    newlyCreated.error = error;
	}
    }

    /**
     * Updates the reference vectors of the winner and the neighborhood
     */
    @Override
    protected void adaptPositions() {

	if (SIMPLICIAL_ADAPTATION && isStable(bestMatching)) {

	    simplicialAdaptation();

	} else if (CENTROIDAL_ADAPTATION && isStable(bestMatching)) {

	    centroidalAdaptation();

	} else {

	    double rho_w = EPSILON_W;
	    double rho_n = EPSILON_N;
	    if (isStable(bestMatching)) {
		rho_w *= ETA_S;
		rho_n *= ETA_S;
	    }

	    // Updates the reference vectors of the winner
	    adaptPosition(bestMatching, rho_w * bestMatching.firingCounter,
		    sampledPoint, bestMatching.position, true);

	    if (STOP_NEIGHBOR_ADAPTATION && isStable(bestMatching)) {
		// Just do nothing: avoid adapting neighbors

	    } else if (NEURAL_GAS_ADAPTATION && isStable(bestMatching)) {
		// Updates just the closest vertex to the winner
		adaptPosition(secondBestMatching, rho_n
			* secondBestMatching.firingCounter, sampledPoint,
			secondBestMatching.position, false);
	    } else {
		// Updates all vertices connected to the winner
		for (Edge edge : bestMatching.neighborhood) {
		    Vertex vertex = edge.getOtherVertex(bestMatching);
		    adaptPosition(vertex, rho_n * vertex.firingCounter,
			    sampledPoint, vertex.position, false);
		}
	    }
	}

	if (COMPUTE_DISTANCE_ERROR && isStable(bestMatching)) {
	    computeDistanceError();
	}

    }

    protected void adaptPosition(Vertex vertex, double eta, double[] p,
	    double[] o, boolean increaseCounter) {
	if (ACCUMULATION > 0 && isStable(vertex)) {

	    // Ensure accumulator
	    if (vertex.accumulator == null) {
		vertex.accumulator = new double[p.length];
	    }

	    if (vertex.accumulatorCounter < ACCUMULATION) {

		// Put adaptation into the accumulator
		for (int i = 0; i < vertex.position.length; i++) {
		    vertex.accumulator[i] += eta * (p[i] - o[i]);
		}
		if (increaseCounter) {
		    vertex.accumulatorCounter++;
		}
	    } else {
		vertex.accumulatorCounter++;

		// Transfer average adaptation to the position and reset the
		// accumulator
		for (int i = 0; i < vertex.position.length; i++) {
		    vertex.position[i] +=
			    (vertex.accumulator[i] + eta * (p[i] - o[i]))
				    / vertex.accumulatorCounter;
		    vertex.accumulator[i] = 0;
		}
		vertex.accumulatorCounter = 0;
	    }
	} else {
	    if (ACCUMULATION > 0 && vertex.accumulatorCounter > 0) {
		// Some residue has been left here
		for (int i = 0; i < vertex.accumulator.length; i++) {
		    vertex.accumulator[i] = 0;
		}
		vertex.accumulatorCounter = 0;
	    }

	    // Adapt position directly
	    for (int i = 0; i < vertex.position.length; i++) {
		vertex.position[i] += eta * (p[i] - o[i]);
	    }
	}
    }

    /**
     * Do not age edges from a regular vertex
     * 
     */
    @Override
    protected void ageEdges() {
	if (!STABILIZE_REGULAR_NEIGHBORHOODS || !isRegular(bestMatching)) {
	    super.ageEdges();
	}
    }

    /**
     * Updates the firing counters of the winning vertex and its neighbors. The
     * firing counter decays with time constant TAU, until it reaches the basal
     * level INITIAL_STRENGTH - (1 / ALFA).
     * 
     * @see "Marsland, S. et al., A self-organising network that grows when
     *      required", Eqn. 16, p. 1045, Neural Networks 15 (2002)"
     */
    @Override
    protected void updateFiringCounters() {

	super.updateFiringCounters();

	// Change the state of the bestMatching unit, if required
	if (changeState(bestMatching)) {
	    touch(bestMatching);
	}

	// Adapt insertion threshold
	if (ADAPT_INSERTION_THRESHOLD) {
	    if (isSingular(bestMatching)
		    || (ADAPT_HALF_DISK && isHalfDisk(bestMatching))
		    || (ADAPT_CONNECTED && isConnected(bestMatching) && !isRegular(bestMatching))) {
		bestMatching.insertionThreshold +=
			((ALPHA_IT * (INSERTION_THRESHOLD - bestMatching.insertionThreshold)) - 1)
				/ TAU_IT_HABITUATION;
	    } else if (isDisk(bestMatching)) {
		bestMatching.insertionThreshold +=
			((ALPHA_IT / TAU_IT_DISHABITUATION) * (INSERTION_THRESHOLD - bestMatching.insertionThreshold));
	    }
	}
    }

    /**
     * Reap out all aging vertices, if required
     */
    @Override
    protected void updateMesh() {
	super.updateMesh();

	// Make sure that vertices can exceed maximum aging by at most 10%
	if (MAX_VERTEX_INACTIVITY > 0
		&& tick % Math.round(MAX_VERTEX_INACTIVITY / 10d) == 0) {

	    Iterator<Vertex> vertices = mesh.vertexlist.listIterator();
	    while (vertices.hasNext()) {
		Vertex vertex = vertices.next();
		if (vertex.lastWin < tick - MAX_VERTEX_INACTIVITY) {

		    for (Edge edge : vertex.neighborhood) {
			Vertex otherVertex = edge.getOtherVertex(vertex);
			otherVertex.neighborhood.remove(edge);
			touch(otherVertex);
			mesh.edgelist.remove(edge);
		    }

		    vertices.remove();
		}
	    }
	}
    }

    /**
     * Perform simplicial adaptation
     * 
     */
    protected abstract void simplicialAdaptation();

    /**
     * Perform centroidal adaptation
     * 
     */
    protected abstract void centroidalAdaptation();

    /**
     * Compute distance error
     * 
     */
    protected abstract void computeDistanceError();

    /**
     * Change the state of a vertex
     * 
     * @return true if the state is changed
     */
    protected abstract boolean changeState(Vertex vertex);

    /**
     * Set the state of the vertex as HABITUATED
     * 
     * @param vertex
     */
    protected void setHabituated(Vertex vertex) {
	vertex.state = SOAMConstants.HABITUATED;
    }

    /**
     * Set the state of the vertex as CONNECTED
     * 
     * @param vertex
     */
    protected void setConnected(Vertex vertex) {
	vertex.state = SOAMConstants.CONNECTED;
    }

    /**
     * Set the state of the vertex as DISK
     * 
     * @param vertex
     */
    protected void setDisk(Vertex vertex) {
	vertex.state = SOAMConstants.DISK;
    }

    /**
     * Set the state of the vertex as HALF_DISK
     * 
     * @param vertex
     */
    protected void setHalfDisk(Vertex vertex) {
	vertex.state = SOAMConstants.HALF_DISK;
    }

    /**
     * Set the state of the vertex as PATCH
     * 
     * @param vertex
     */
    protected void setPatch(Vertex vertex) {
	vertex.state = SOAMConstants.PATCH;
    }

    /**
     * Set the state of the vertex as BOUNDARY
     * 
     * @param vertex
     */
    protected void setBoundary(Vertex vertex) {
	vertex.state = SOAMConstants.BOUNDARY;
    }

    /**
     * Set the state of the vertex as SINGULAR
     * 
     * @param vertex
     */
    protected void setSingular(Vertex vertex) {
	vertex.state = SOAMConstants.SINGULAR;
    }

    /**
     * Perform any verification activity, if required (for debugging purposes
     * only)
     */
    @Override
    protected void validate() {
	super.validate();

	// Firing counter
	if (bestMatching.firingCounter > INITIAL_STRENGTH) {
	    System.err.println("Firing counter of vertex " + bestMatching
		    + " exceeds maximum value.");
	} else if (bestMatching.firingCounter < INITIAL_STRENGTH
		- (1 / ALPHA_W) - 0.0001d) {
	    System.err.println("Firing counter of vertex " + bestMatching
		    + " is below minimum value.");
	}

	// Insertion threshold
	if (bestMatching.insertionThreshold > INSERTION_THRESHOLD + 0.0001d) {
	    System.err.println("Insertion threshold of vertex " + bestMatching
		    + " exceeds maximum value.");
	} else if (bestMatching.insertionThreshold < INSERTION_THRESHOLD
		- (1 / ALPHA_IT) - 0.0001d) {
	    System.err.println("Insertion threshold of vertex " + bestMatching
		    + " is below minimum value.");
	}

    }

    protected double computeQuantizationError() {
	ArrayList<double[]> pointCloud = sampler.getPointCloud();

	if (pointCloud == null) {
	    return 0d;
	}

	double error = 0d;
	for (double[] point : pointCloud) {
	    double squaredDistance = Double.MAX_VALUE;

	    for (Vertex vertex : mesh.vertexlist) {
		double tmp =
			AlgebraicOperatorsNd.normSquare(vertex.position, point);

		if (tmp < squaredDistance) {
		    squaredDistance = tmp;
		}
	    }

	    error += squaredDistance;
	}

	return error;
    }

    protected void updateALPHA_IT() {
	ALPHA_IT = 1 / (INSERTION_THRESHOLD - INSERTION_THRESHOLD_MIN);
    }

    @Override
    public boolean isConvergent() {
	return stableVerticesFraction == 1.0d;
    }

    @Override
    public void setINSERTION_THRESHOLD(double insertion_threshold) {
	super.setINSERTION_THRESHOLD(insertion_threshold);

	updateALPHA_IT();
    }

    public boolean isADAPT_INSERTION_THRESHOLD() {
	return ADAPT_INSERTION_THRESHOLD;
    }

    public void setADAPT_INSERTION_THRESHOLD(boolean adapt_insertion_threshold) {

	if (adapt_insertion_threshold && !ADAPT_INSERTION_THRESHOLD) {
	    for (Vertex vertex : mesh.vertexlist) {
		vertex.insertionThreshold = INSERTION_THRESHOLD;
	    }
	}

	ADAPT_INSERTION_THRESHOLD = adapt_insertion_threshold;
    }

    public boolean isADAPT_HALF_DISK() {
	return ADAPT_HALF_DISK;
    }

    public void setADAPT_HALF_DISK(boolean adapt_half_disk) {
	ADAPT_HALF_DISK = adapt_half_disk;
    }

    public boolean isADAPT_CONNECTED() {
	return ADAPT_CONNECTED;
    }

    public void setADAPT_CONNECTED(boolean adapt_connected) {
	ADAPT_CONNECTED = adapt_connected;
    }

    public double getETA_S() {
	return ETA_S;
    }

    public void setETA_S(double eta_s) {
	ETA_S = eta_s;
    }

    public double getTAU_IT_HABITUATION() {
	return TAU_IT_HABITUATION;
    }

    public void setTAU_IT_HABITUATION(double tau_it_habituation) {
	TAU_IT_HABITUATION = tau_it_habituation;
    }

    public double getTAU_IT_DISHABITUATION() {
	return TAU_IT_DISHABITUATION;
    }

    public void setTAU_IT_DISHABITUATION(double tau_it_dishabituation) {
	TAU_IT_DISHABITUATION = tau_it_dishabituation;
    }

    public boolean isSTABILIZE_REGULAR_NEIGHBORHOODS() {
	return STABILIZE_REGULAR_NEIGHBORHOODS;
    }

    public void setSTABILIZE_REGULAR_NEIGHBORHOODS(
	    boolean stabilize_regular_neighborhoods) {
	STABILIZE_REGULAR_NEIGHBORHOODS = stabilize_regular_neighborhoods;
    }

    public boolean isFOCALIZED_SAMPLING() {
	return FOCALIZED_SAMPLING;
    }

    public void setFOCALIZED_SAMPLING(boolean focalized_sampling) {
	FOCALIZED_SAMPLING = focalized_sampling;
    }

    public boolean isSIMPLICIAL_ADAPTATION() {
	return SIMPLICIAL_ADAPTATION;
    }

    public void setSIMPLICIAL_ADAPTATION(boolean simplicial_adaptation) {
	SIMPLICIAL_ADAPTATION = simplicial_adaptation;
    }

    public boolean isSTOP_NEIGHBOR_ADAPTATION() {
	return STOP_NEIGHBOR_ADAPTATION;
    }

    public void setSTOP_NEIGHBOR_ADAPTATION(boolean stop_adaptation) {
	STOP_NEIGHBOR_ADAPTATION = stop_adaptation;
    }

    public boolean isNEURAL_GAS_ADAPTATION() {
	return NEURAL_GAS_ADAPTATION;
    }

    public void setNEURAL_GAS_ADAPTATION(boolean neural_gas_adaptation) {
	NEURAL_GAS_ADAPTATION = neural_gas_adaptation;
    }

    public boolean isOFF_BOUNDARY_PROJECTIONS() {
	return OFF_BOUNDARY_PROJECTIONS;
    }

    public void setOFF_BOUNDARY_PROJECTIONS(boolean off_boundary_projections) {
	OFF_BOUNDARY_PROJECTIONS = off_boundary_projections;
    }

    public double getEPSILON_E() {
	return EPSILON_E;
    }

    public void setEPSILON_E(double epsilon_e) {
	EPSILON_E = epsilon_e;
    }

    public double getFOCALIZATION_RADIUS() {
	return FOCALIZATION_RADIUS;
    }

    public void setFOCALIZATION_RADIUS(double focalization_radius) {
	FOCALIZATION_RADIUS = focalization_radius;
    }

    public boolean isBOUNDARY_IS_STABLE() {
	return BOUNDARY_IS_STABLE;
    }

    public void setBOUNDARY_IS_STABLE(boolean boundary_is_stable) {
	BOUNDARY_IS_STABLE = boundary_is_stable;
    }

    public double getVERTEX_ERROR_THRESHOLD() {
	return VERTEX_ERROR_THRESHOLD;
    }

    public void setVERTEX_ERROR_THRESHOLD(double vertex_error_threshold) {
	VERTEX_ERROR_THRESHOLD = vertex_error_threshold;
    }

    public long getMAX_VERTEX_INACTIVITY() {
	return MAX_VERTEX_INACTIVITY;
    }

    public void setMAX_VERTEX_INACTIVITY(long max_vertex_inactivity) {
	MAX_VERTEX_INACTIVITY = max_vertex_inactivity;
    }

    public int getVerticesConnected() {
	return verticesConnected;
    }

    public int getVerticesSingular() {
	return verticesSingular;
    }

    public int getVerticesHalfdisk() {
	return verticesHalfdisk;
    }

    public int getVerticesDisk() {
	return verticesDisk;
    }

    public int getVerticesPatch() {
	return verticesPatch;
    }

    public int getVerticesBoundary() {
	return verticesBoundary;
    }

    public long getTickOfFirstConvergence() {
	return tickOfFirstConvergence;
    }

    public long getTimeOfFirstConvergence() {
	return timeOfFirstConvergence;
    }

    public double getStableVerticesFraction() {
	return stableVerticesFraction;
    }

    public double getEdgeMaximumLength() {
	return edgeMaximumLength;
    }

    public double getEdgeAverageLength() {
	return edgeAverageLength;
    }

    public double getEdgeMinimumLength() {
	return edgeMinimumLength;
    }

    public double getEdgeDelaunayFraction() {
	return edgeDelaunayFraction;
    }

    public double getQuantizationError() {
	return quantizationError;
    }

    public double getVertexMaximumError() {
	return vertexMaximumError;
    }

    public double getVertexAverageError() {
	return vertexAverageError;
    }

    public double getVertexMinimumError() {
	return vertexMinimumError;
    }

    public long getTimeElapsed() {
	return timeElapsed;
    }

    public long getTimeElapsedOnSampleAndFindWinners() {
	return timeElapsedOnSampleAndFindWinners;
    }

    public boolean isCHECK_EDGE_VIABILITY() {
	return CHECK_EDGE_VIABILITY;
    }

    public void setCHECK_EDGE_VIABILITY(boolean check_edge_viability) {
	CHECK_EDGE_VIABILITY = check_edge_viability;
    }

    public long getCOMPUTE_STATISTICS_PERIOD() {
	return COMPUTE_STATISTICS_PERIOD;
    }

    public void setCOMPUTE_STATISTICS_PERIOD(long compute_statistics_period) {
	COMPUTE_STATISTICS_PERIOD = compute_statistics_period;
    }

    public boolean isCOMPUTE_QUANTIZATION_ERROR() {
	return COMPUTE_QUANTIZATION_ERROR;
    }

    public void setCOMPUTE_QUANTIZATION_ERROR(boolean compute_quantization_error) {
	COMPUTE_QUANTIZATION_ERROR = compute_quantization_error;
    }

    public double getINSERTION_THRESHOLD_MIN() {
	return INSERTION_THRESHOLD_MIN;
    }

    public void setINSERTION_THRESHOLD_MIN(double insertion_threshold_min) {
	INSERTION_THRESHOLD_MIN = insertion_threshold_min;

	updateALPHA_IT();
    }

    public boolean isCENTROIDAL_ADAPTATION() {
	return CENTROIDAL_ADAPTATION;
    }

    public void setCENTROIDAL_ADAPTATION(boolean centroidal_adaptation) {
	CENTROIDAL_ADAPTATION = centroidal_adaptation;
    }

    public boolean isCOMPUTE_DISTANCE_ERROR() {
	return COMPUTE_DISTANCE_ERROR;
    }

    public void setCOMPUTE_DISTANCE_ERROR(boolean compute_distance_error) {
	COMPUTE_DISTANCE_ERROR = compute_distance_error;
    }

    public int getACCUMULATION() {
	return ACCUMULATION;
    }

    public void setACCUMULATION(int accumulation) {
	ACCUMULATION = accumulation;
    }

}
