

package soam.algorithms;

import java.util.ArrayList;

import soam.Edge;
import soam.Vertex;

public class SOAM2d extends SOAM {

    /**
     * This enum describes the types of the link
     * 
     * @see SOAM2d#linkType(Vertex)
     */
    public enum LinkType {
	NONE, HALF_SPHERE, SPHERE, SINGULAR;
    }

    protected boolean CUSP_IS_SINGULAR = true;

    protected boolean COMPLETE_FACES_ON_WITNESS = true;
    protected boolean COMPLETE_FACES_EAGERLY = false;
    protected long EDGE_FLIP_PERIOD = 0;

    /**
     * By default a 2-disk is a vertex that have a neighborhood homeomorphic to
     * a 2-Disk, that is: the boundary of the star [bd(vertex) =
     * cl(star(vertex))] have to be a 2-Sphere
     * 
     * @param vertex
     * @return true if vertex is homeomorphic to a 2-Disk
     */
    public static boolean is2Disk(Vertex vertex) {
	return linkType(vertex) == LinkType.SPHERE;
    }

    /**
     * A vertex is a 2 half disk if the closure of its star is homeomorphic to a
     * half disk
     * 
     * @param vertex
     * @return
     */
    public static boolean is2HalfDisk(Vertex vertex) {
	return linkType(vertex) == LinkType.HALF_SPHERE;
    }

    /**
     * A vertex is part of the boundary if it is a Half disk and its
     * neighborhood is disk or half disk
     * 
     * @param vertex
     * @return
     */
    public static boolean is2Boundary(Vertex vertex) {
	return isNeighborhoodRegular(vertex) && is2HalfDisk(vertex);
    }

    /**
     * Check if the vertex is PATCH, by default a vertex is a PATCH if the state
     * of its neighborhood is DISK
     * 
     * @param vertex
     * @return true if the vertex is a disk and have a neighborhood of disk or
     *         half disk
     */
    public static boolean is2Patch(Vertex vertex) {
	return isNeighborhoodRegular(vertex) && is2Disk(vertex);
    }

    /**
     * A cusp is dangerously similar to a tetrahedron
     */
    public static boolean isCusp(Vertex vertex) {
	return vertex.neighborhood.size() == 3
		&& linkType(vertex) == LinkType.SPHERE;
    }

    /**
     * Check if the vertex is a topological singularity, ie if the vertex and
     * its neighborhood forms a 3-simplex or a simplex with dimension greater
     * than 2
     * 
     * @param vertex
     * @return true if there is a vertex in the link that is homeomorphic to a
     *         1-Disk AND there is vertex in the link that is spurious
     */
    public static boolean isNotPlanar(Vertex vertex) {
	return linkType(vertex) == LinkType.SINGULAR;
    }

    /**
     * Compute the topological type of the link of a vertex. Possible results
     * are:</br> - NONE: the link is planar but not regular</br> - HALF_SPHERE:
     * the link is regular and homeomorphic to a 1-half sphere</br> - SPHERE:
     * the link is regular and homeomorphic to a 1-sphere</br> - SINGULAR: the
     * link is incompatible with a planar Cl(St(Vertex))</br>
     * 
     * @param vertex
     * @return LinkType
     */
    public static LinkType linkType(Vertex vertex) {
	Vertex[] link = getLink(vertex);
	Vertex seed = null;
	int seedIndex = 0;

	int connectedTo0 = 0;
	int connectedTo1 = 0;
	int connectedTo2 = 0;
	int connectedToMore = 0;

	for (int i = 0; i < link.length; i++) {

	    // Find the number of vertices in the link that are connected to
	    // link[i]
	    int tmp = 0;
	    for (Edge edge : link[i].neighborhood) {
		Vertex otherVertex = edge.getOtherVertex(link[i]);

		for (int j = 0; j < link.length; j++) {
		    if (link[j] == otherVertex) {
			tmp++;
			break;
		    }
		}
	    }

	    switch (tmp) {
	    case 0:
		connectedTo0++;
		break;
	    case 1:
		connectedTo1++;
		break;
	    case 2:
		seed = link[i];
		seedIndex = i;
		connectedTo2++;
		break;
	    default:
		connectedToMore++;
	    }
	}

	if (connectedToMore != 0
		|| (connectedTo2 > 0 && connectedTo1 == 0 && connectedTo0 > 0)) {
	    return LinkType.SINGULAR;
	} else if (connectedTo2 >= link.length - 2 && connectedTo0 == 0
		&& connectedToMore == 0) {
	    // The link MIGHT be regular, but the 2-connected nodes must belong
	    // to the same 1-sphere or 1-half-sphere - or else the link is not
	    // planar
	    if (connectedTo2 > 0) {
		removeConnected(link, seed, seedIndex);
		for (int i = 0; i < link.length; i++) {
		    if (link[i] != null) {
			return LinkType.SINGULAR;
		    }
		}
	    }

	    if (connectedTo2 == link.length) {
		return LinkType.SPHERE;
	    } else {
		return LinkType.HALF_SPHERE;
	    }
	} else {
	    return LinkType.NONE;
	}
    }

    private static void removeConnected(Vertex[] link, Vertex vertex, int index) {

	if (link[index] == null) {
	    return;
	} else {
	    link[index] = null;
	}

	for (Edge edge : vertex.neighborhood) {
	    Vertex otherVertex = edge.getOtherVertex(vertex);

	    for (int j = 0; j < link.length; j++) {
		if (link[j] == otherVertex) {
		    removeConnected(link, otherVertex, j);
		    break;
		}
	    }
	}
    }

    protected static void sortNeighborhoodRecursively(
	    ArrayList<Vertex> oriented, Vertex vertex, Vertex from, Vertex to) {

	sortNeighborhood(vertex, from, to);
	oriented.add(vertex);

	Vertex[] link = getLink(vertex);
	for (int i = 0; i < link.length; i++) {
	    if (!oriented.contains(link[i])) {
		sortNeighborhoodRecursively(oriented, link[i], vertex,
			link[i > 0 ? i - 1 : link.length - 1]);
	    }
	}
    }

    // TODO Sometimes it will be extended to half-disks as well
    protected static void sortNeighborhood(Vertex vertex, Vertex from, Vertex to) {
	if (!isDisk(vertex)) {
	    throw new RuntimeException(
		    "Cannot sort the neighborhood of a non-disk vertex");
	}

	Vertex[] link = getLink(vertex);

	int index;
	if ((from != null) && (to != null)) {
	    if (findEdgeConnecting(from, to) == null) {
		throw new RuntimeException(
			"The two input vertices are not connected");
	    }

	    swap(from, link, 0);
	    swap(to, link, 1);

	    index = 1;
	} else {
	    // Just keep the first vertex and find a suitable successor
	    for (Edge edge : link[0].neighborhood) {
		Vertex otherVertex = edge.getOtherVertex(link[0]);

		if (indexOf(otherVertex, link) > 0) {
		    swap(otherVertex, link, 1);
		}
	    }

	    index = 0;
	}

	for (; index < link.length - 1; index++) {
	    boolean found = false;
	    for (Edge edge : link[index].neighborhood) {
		Vertex otherVertex = edge.getOtherVertex(link[index]);

		if (indexOf(otherVertex, link) > index) {
		    swap(otherVertex, link, index + 1);
		    found = true;
		    break;
		}
	    }
	    if (!found) {
		throw new RuntimeException("No suitable successor found");
	    }
	}

	// Sort the actual neighborhood
	ArrayList<Edge> neighborhoodCopy = new ArrayList<Edge>(vertex.neighborhood);
	
	vertex.neighborhood.clear();
	for (int i = 0; i < link.length; i++) {
	    boolean found = false;
	    for (Edge edge : neighborhoodCopy) {
		if (edge.getOtherVertex(vertex) == link[i]) {
		    vertex.neighborhood.add(edge);
		    found = true;
		    break;
		}
	    }
	    if (!found) {
		throw new RuntimeException("No edge found");
	    }
	}
    }

    private static void swap(Vertex vertex, Vertex[] link, int index) {
	Vertex otherVertex = link[index];
	int index2 = indexOf(vertex, link);

	if (index2 < 0) {
	    throw new RuntimeException(
		    "The input vertex is not contained in the link");
	}

	link[index] = vertex;
	link[index2] = otherVertex;
    }

    private static int indexOf(Vertex vertex, Vertex[] link) {
	for (int i = 0; i < link.length; i++) {
	    if (vertex == link[i]) {
		return i;
	    }
	}

	return -1;
    }

    protected static Vertex[] getLink(Vertex vertex) {
	Vertex[] link = new Vertex[vertex.neighborhood.size()];

	for (int i = 0; i < link.length; i++) {
	    link[i] = vertex.neighborhood.get(i).getOtherVertex(vertex);
	}

	return link;
    }

    protected static boolean canBeConnected(Vertex vertex) {
	if (vertex == null) {
	    return false;
	}

	return vertex.state == SOAMConstants.CONNECTED
		|| vertex.state == SOAMConstants.HALF_DISK
		|| vertex.state == SOAMConstants.BOUNDARY;
    }

    /**
     * This version actually implements mesh orientation, provided that all
     * vertices are at least regular. It sorts the neighborhoods of all vertices
     * in a consistent way.
     */
    @Override
    public void orientMesh() {
	ArrayList<Vertex> oriented = new ArrayList<Vertex>();

	while (oriented.size() < mesh.vertexlist.size()) {
	    Vertex initial = null;
	    for (Vertex vertex : mesh.vertexlist) {
		if (!oriented.contains(vertex)) {
		    initial = vertex;
		    break;
		}
	    }
	    if (initial == null) {
		throw new RuntimeException("No initial element found");
	    }

	    sortNeighborhoodRecursively(oriented, initial, null, null);
	}

    }

    /**
     * This version add face completion. A triangular face is completed whenever
     * these two conditions hold:</br> a) the sampledPoint is a weak witness for
     * the face</br> b) the three closest vertices can be connected, i.e. they
     * are neither disks nor singular
     */
    @Override
    protected void updateOrCreateEdge() {
	super.updateOrCreateEdge();

	if (COMPLETE_FACES_ON_WITNESS && canBeConnected(bestMatching)
		&& canBeConnected(secondBestMatching)
		&& canBeConnected(thirdBestMatching)) {

	    Edge edge13 = findEdgeConnecting(bestMatching, thirdBestMatching);
	    Edge edge23 =
		    findEdgeConnecting(secondBestMatching, thirdBestMatching);

	    if (edge13 == null && (COMPLETE_FACES_EAGERLY || edge23 != null)) {

		// Complete the face
		edge13 = new Edge(bestMatching, thirdBestMatching);
		mesh.edgelist.add(edge13);
		bestMatching.neighborhood.add(edge13);
		thirdBestMatching.neighborhood.add(edge13);

		touchNeighborhood(bestMatching);
		touch(bestMatching);
		touchNeighborhood(thirdBestMatching);
		touch(thirdBestMatching);
	    }

	    if (edge23 == null && (COMPLETE_FACES_EAGERLY || edge13 != null)) {

		// Complete the face
		edge23 = new Edge(secondBestMatching, thirdBestMatching);
		mesh.edgelist.add(edge23);
		secondBestMatching.neighborhood.add(edge23);
		thirdBestMatching.neighborhood.add(edge23);

		touchNeighborhood(secondBestMatching);
		touch(secondBestMatching);
		touchNeighborhood(thirdBestMatching);
		touch(thirdBestMatching);
	    }
	}
    }

    @Override
    protected void centroidalAdaptation() {

	Vertex thirdBestMatching = findThirdBestMatching();

	try {
	    // Compute edge projection
	    double[] edgeBarycenter =
		    findBarycenter(bestMatching.position,
			    secondBestMatching.position);

	    // Compute triangle projection
	    double[] triangleBarycenter =
		    findBarycenter(bestMatching.position,
			    secondBestMatching.position,
			    thirdBestMatching.position);

	    double rho_w = ETA_S * EPSILON_W;

	    // Adapt vertices
	    adaptPosition(thirdBestMatching, rho_w
		    * thirdBestMatching.firingCounter, sampledPoint,
		    triangleBarycenter, false);

	    adaptPosition(secondBestMatching, rho_w
		    * secondBestMatching.firingCounter, sampledPoint,
		    edgeBarycenter, false);
	    adaptPosition(secondBestMatching, rho_w
		    * secondBestMatching.firingCounter, sampledPoint,
		    triangleBarycenter, false);

	    adaptPosition(bestMatching, rho_w * bestMatching.firingCounter,
		    sampledPoint, edgeBarycenter, false);
	    adaptPosition(bestMatching, rho_w * bestMatching.firingCounter,
		    sampledPoint, triangleBarycenter, false);

	    adaptPosition(bestMatching, rho_w * bestMatching.firingCounter,
		    sampledPoint, bestMatching.position, true);

	} catch (NullPointerException e) {
	    System.err
		    .println("Null pointer exception in SOAM2d.centroidalAdaptation()");
	}
    }

    @Override
    protected void simplicialAdaptation() {

	try {
	    // Compute edge projection
	    double[] edgeProjection =
		    findProjection(sampledPoint, bestMatching.position,
			    secondBestMatching.position);

	    // Compute triangle projection
	    double[] triangleProjection =
		    findProjection(sampledPoint, bestMatching.position,
			    secondBestMatching.position,
			    thirdBestMatching.position);

	    double rho_w = ETA_S * EPSILON_W;

	    // Adapt vertices
	    adaptPosition(thirdBestMatching, rho_w
		    * thirdBestMatching.firingCounter, sampledPoint,
		    triangleProjection, false);

	    adaptPosition(secondBestMatching, rho_w
		    * secondBestMatching.firingCounter, sampledPoint,
		    edgeProjection, false);
	    adaptPosition(secondBestMatching, rho_w
		    * secondBestMatching.firingCounter, sampledPoint,
		    triangleProjection, false);

	    adaptPosition(bestMatching, rho_w * bestMatching.firingCounter,
		    sampledPoint, edgeProjection, false);
	    adaptPosition(bestMatching, rho_w * bestMatching.firingCounter,
		    sampledPoint, triangleProjection, false);

	    adaptPosition(bestMatching, rho_w * bestMatching.firingCounter,
		    sampledPoint, bestMatching.position, true);

	} catch (NullPointerException e) {
	    System.err
		    .println("Null pointer exception in SOAM.simplicialAdaptation()");
	}
    }

    @Override
    protected void computeDistanceError() {
	// Find thirdBestMatching
	Vertex thirdBestMatching = findThirdBestMatching();

	// Compute triangle projection
	double[] triangleP =
		findProjection(sampledPoint, bestMatching.position,
			secondBestMatching.position, thirdBestMatching.position);

	// Exponential decaying average
	bestMatching.error +=
		EPSILON_E
			* (AlgebraicOperatorsNd.norm(sampledPoint, triangleP) - bestMatching.error);
    }

    /**
     * Change the state of a vertex.
     * 
     * @return true if the state has changed
     */
    @Override
    protected boolean changeState(Vertex vertex) {

	switch (vertex.state) {

	case SOAMConstants.ACTIVE:
	    if (isHabituated(vertex)) {
		setHabituated(vertex);
		return true;
	    }
	    break;

	case SOAMConstants.HABITUATED:
	    if (isNeighborhoodHabituated(vertex)) {
		setConnected(vertex);
		return true;
	    }
	    break;

	case SOAMConstants.CONNECTED:
	    if (!isNeighborhoodHabituated(vertex)) {
		setHabituated(vertex);
		return true;
	    } else {
		switch (linkType(vertex)) {
		case SPHERE:
		    if (CUSP_IS_SINGULAR && vertex.neighborhood.size() == 3) {
			// It's a cusp
			setSingular(vertex);
		    } else {
			setDisk(vertex);
		    }
		    return true;
		case HALF_SPHERE:
		    setHalfDisk(vertex);
		    return true;
		case SINGULAR:
		    setSingular(vertex);
		    return true;
		}
	    }
	    break;

	case SOAMConstants.SINGULAR:
	    if (!isNeighborhoodHabituated(vertex)) {
		setHabituated(vertex);
		return true;
	    } else if (vertex.touched) {
		switch (linkType(vertex)) {
		case SPHERE:
		    if (!CUSP_IS_SINGULAR || vertex.neighborhood.size() != 3) {
			// It's not a cusp
			setConnected(vertex);
			return true;
		    }
		    break;
		case HALF_SPHERE:
		case NONE:
		    setConnected(vertex);
		    return true;
		}
	    }
	    break;

	case SOAMConstants.DISK:
	    if (!isNeighborhoodHabituated(vertex)) {
		setHabituated(vertex);
		return true;
	    } else if (vertex.touched) {
		switch (linkType(vertex)) {
		case SPHERE:
		    if (CUSP_IS_SINGULAR && vertex.neighborhood.size() == 3) {
			// It's a cusp
			setConnected(vertex);
			return true;
		    }
		    break;
		case HALF_SPHERE:
		case NONE:
		case SINGULAR:
		    setConnected(vertex);
		    return true;
		}
	    } else if (isNeighborhoodRegular(vertex)) {
		setPatch(vertex);
		return true;
	    }
	    break;

	case SOAMConstants.HALF_DISK:
	    if (!isNeighborhoodHabituated(vertex)) {
		setHabituated(vertex);
		return true;
	    } else if (vertex.touched) {
		switch (linkType(vertex)) {
		case SPHERE:
		case NONE:
		case SINGULAR:
		    setConnected(vertex);
		    return true;
		}
	    } else if (isNeighborhoodRegular(vertex)) {
		setBoundary(vertex);
		return true;
	    }
	    break;

	case SOAMConstants.PATCH:
	    if (!isNeighborhoodHabituated(vertex)) {
		setHabituated(vertex);
		return true;
	    } else if (vertex.touched) {
		switch (linkType(vertex)) {
		case SPHERE:
		    if (CUSP_IS_SINGULAR && vertex.neighborhood.size() == 3) {
			// It's a cusp
			setConnected(vertex);
			return true;
		    } else if (!isNeighborhoodRegular(vertex)) {
			setDisk(vertex);
			return true;
		    }
		    break;
		case SINGULAR:
		case HALF_SPHERE:
		case NONE:
		    setConnected(vertex);
		    return true;
		}
	    } else if (!isNeighborhoodRegular(vertex)) {
		setDisk(vertex);
		return true;
	    }
	    break;

	case SOAMConstants.BOUNDARY:
	    if (!isNeighborhoodHabituated(vertex)) {
		setHabituated(vertex);
		return true;
	    } else if (vertex.touched) {
		switch (linkType(vertex)) {
		case HALF_SPHERE:
		    if (!isNeighborhoodRegular(vertex)) {
			setHalfDisk(vertex);
			return true;
		    }
		    break;
		case SPHERE:
		case SINGULAR:
		case NONE:
		    setConnected(vertex);
		    return true;
		}
	    } else if (!isNeighborhoodRegular(vertex)) {
		setHalfDisk(vertex);
		return true;
	    }
	    break;
	}
	return false;
    }

    /**
     * Perform any verification activity, if required (for debugging purposes
     * only)
     */
    @Override
    protected void validate() {
	super.validate();

	if (!validateState(bestMatching)) {
	    System.err.println("The state of vertex " + bestMatching
		    + " is not valid.");
	}
    }

    /**
     * Make sure that the vertex is in fact in the state it claims to be
     */
    protected boolean validateState(Vertex vertex) {

	switch (vertex.state) {
	case SOAMConstants.ACTIVE:
	    if (!isHabituated(vertex)) {
		return true;
	    }
	    break;

	case SOAMConstants.HABITUATED:
	    if (isHabituated(vertex)) {
		return true;
	    }
	    break;

	case SOAMConstants.CONNECTED:
	    if (isNeighborhoodHabituated(vertex)) {
		return true;
	    }
	    break;

	case SOAMConstants.SINGULAR:
	    if (isNotPlanar(vertex) || (CUSP_IS_SINGULAR && isCusp(vertex))) {
		return true;
	    }
	    break;

	case SOAMConstants.DISK:
	    if (is2Disk(vertex) && (!CUSP_IS_SINGULAR || !isCusp(vertex))) {
		return true;
	    }
	    break;

	case SOAMConstants.HALF_DISK:
	    if (is2HalfDisk(vertex)) {
		return true;
	    }
	    break;

	case SOAMConstants.PATCH:
	    if (is2Patch(vertex)) {
		return true;
	    }
	    break;

	case SOAMConstants.BOUNDARY:
	    if (is2Boundary(vertex)) {
		return true;
	    }
	    break;

	}

	return false;
    }

    /**
     * Perform edge flips on non-delaunay edges between stable vertices, if
     * required
     */
    @Override
    protected void updateMesh() {
	super.updateMesh();

	if (EDGE_FLIP_PERIOD > 0 && (tick % EDGE_FLIP_PERIOD) == 0) {

	    for (Edge edge : mesh.edgelist) {
		if (isStable(edge.start) && isStable(edge.end)) {
		    delaunayEdge(edge, true);
		}
	    }
	}
    }

    protected Vertex findThirdBestMatching() {

	Vertex thirdBestMatching = null;
	double distance = Double.MAX_VALUE;
	int count = 0;
	Vertex[] link = getLink(bestMatching);
	for (Edge edge : secondBestMatching.neighborhood) {
	    Vertex vertex = edge.getOtherVertex(secondBestMatching);
	    for (int i = 0; i < link.length; i++) {
		if (link[i] == vertex) {
		    count++;
		    double d =
			    AlgebraicOperatorsNd.normSquare(sampledPoint,
				    vertex.position);
		    if (d < distance) {
			thirdBestMatching = vertex;
			distance = d;
		    }
		}
	    }
	}

	return thirdBestMatching;
    }

    public boolean isCUSP_IS_SINGULAR() {
	return CUSP_IS_SINGULAR;
    }

    public void setCUSP_IS_SINGULAR(boolean cusp_is_singular) {
	CUSP_IS_SINGULAR = cusp_is_singular;
    }

    public boolean isCOMPLETE_FACES_ON_WITNESS() {
	return COMPLETE_FACES_ON_WITNESS;
    }

    public void setCOMPLETE_FACES_ON_WITNESS(boolean complete_faces_on_witness) {
	COMPLETE_FACES_ON_WITNESS = complete_faces_on_witness;
    }

    public boolean isCOMPLETE_FACES_EAGERLY() {
	return COMPLETE_FACES_EAGERLY;
    }

    public void setCOMPLETE_FACES_EAGERLY(boolean complete_faces_eagerly) {
	COMPLETE_FACES_EAGERLY = complete_faces_eagerly;
    }

    public long getEDGE_FLIP_PERIOD() {
	return EDGE_FLIP_PERIOD;
    }

    public void setEDGE_FLIP_PERIOD(long edge_flip_period) {
	EDGE_FLIP_PERIOD = edge_flip_period;
    }

}
