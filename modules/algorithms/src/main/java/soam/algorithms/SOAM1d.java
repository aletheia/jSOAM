
package soam.algorithms;

import soam.Vertex;

/**
 * Implementation of the GWR for 1-manifolds
 * 
 */
public class SOAM1d extends SOAM {

    /**
     * Check if the vertex is DISK by default a vertex is 1 Disk if it has only
     * 2 neighbors and they are at least connected
     * 
     * @param vertex
     * @return true if the condition is satisfied
     */
    protected static boolean is1Disk(Vertex vertex) {
	if (!isConnected(vertex) || !isNeighborhoodHabituated(vertex)) {
	    return false;
	} else if (vertex.neighborhood.size() == 2) {
	    return true;
	}
	return false;
    }

    /**
     * Check if the vertex is DISK by default a vertex is 1 Half Disk if it has
     * only 1 neighbor that is at least connected
     * 
     * @param vertex
     * @return true if the condition is satisfied
     */
    protected static boolean is1HalfDisk(Vertex vertex) {
	if (!isConnected(vertex) || !isNeighborhoodHabituated(vertex)) {
	    return false;
	} else if (vertex.neighborhood.size() == 1) {
	    return true;
	}
	return false;
    }

    /**
     * Check if the vertex is PATCH, by default a vertex is a PATCH if the state
     * of its neighborhood is DISK
     * 
     * @param vertex
     * @return true if the condition is satisfied
     */
    protected static boolean is1Patch(Vertex vertex) {
	if (!is1Disk(vertex) || !isNeighborhoodRegular(vertex)) {
	    return false;
	}
	return true;
    }

    /**
     * Check if the vertex is BOUNDARY, by default a vertex is a BOUNDARY if its
     * a HalfDisk and his neighborhood is only a DISK or above
     * 
     * @param vertex
     * @return true if the condition is satisfied
     */
    protected static boolean is1Boundary(Vertex vertex) {
	if (!is1HalfDisk(vertex) || !isNeighborhoodRegular(vertex)) {
	    return false;
	}
	return true;
    }

    /**
     * A vertex is SINGULAR if it is CONNECTED but cannot became DISK or
     * HALF_DISK because with its neighborhood is not homeomorphic to a a 1Disk
     * 
     * @param vertex
     * @return
     */
    protected static boolean is1Singular(Vertex vertex) {
	if (!isConnected(vertex) || !isNeighborhoodHabituated(vertex)) {
	    return false;
	} else if (vertex.neighborhood.size() < 3) {
	    return false;
	}
	return true;
    }

    @Override
    protected void centroidalAdaptation() {

	try {
	    // Compute edge projection
	    double[] edgeBarycenter =
		    findBarycenter(bestMatching.position,
			    secondBestMatching.position, sampledPoint);

	    double rho_w = ETA_S * EPSILON_W;

	    // Adapt vertices
	    adaptPosition(secondBestMatching, rho_w
		    * secondBestMatching.firingCounter, sampledPoint,
		    edgeBarycenter, false);

	    adaptPosition(bestMatching, rho_w * bestMatching.firingCounter,
		    sampledPoint, edgeBarycenter, false);

	    adaptPosition(bestMatching, rho_w * bestMatching.firingCounter,
		    sampledPoint, bestMatching.position, true);

	} catch (NullPointerException e) {
	    System.err
		    .println("Null pointer exception in SOAM1d.centroidalAdaptation()");
	}
    }

    @Override
    protected void simplicialAdaptation() {

	// Compute edge projection
	double[] edgeProjection =
		findProjection(sampledPoint, bestMatching.position,
			secondBestMatching.position);

	double rho_w = ETA_S * EPSILON_W;

	// Adapt vertices
	adaptPosition(secondBestMatching, rho_w
		* secondBestMatching.firingCounter, sampledPoint,
		edgeProjection, false);

	adaptPosition(bestMatching, rho_w * bestMatching.firingCounter,
		sampledPoint, edgeProjection, false);

	adaptPosition(bestMatching, rho_w * bestMatching.firingCounter,
		sampledPoint, bestMatching.position, true);
    }

    @Override
    protected void computeDistanceError() {
	// Compute edge projection
	double[] edgeP =
		findProjection(sampledPoint, bestMatching.position,
			secondBestMatching.position);

	// Exponential decaying average
	bestMatching.error +=
		EPSILON_E
			* (AlgebraicOperatorsNd.norm(sampledPoint, edgeP) - bestMatching.error);
    }

    /**
     * Change the state of a vertex
     * 
     * @return f true if the state is changed
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
	    } else if (is1Disk(vertex)) {
		setDisk(vertex);
		return true;
	    } else if (is1HalfDisk(vertex)) {
		setHalfDisk(vertex);
		return true;
	    } else if (is1Singular(vertex)) {
		setSingular(vertex);
		return true;
	    }
	    break;

	case SOAMConstants.SINGULAR:
	    if (!isNeighborhoodHabituated(vertex)) {
		setHabituated(vertex);
		return true;
	    } else if (vertex.touched && !is1Singular(vertex)) {
		setConnected(vertex);
		return true;
	    }
	    break;

	case SOAMConstants.DISK:
	    if (!isNeighborhoodHabituated(vertex)) {
		setHabituated(vertex);
		return true;
	    } else if (vertex.touched && !is1Disk(vertex)) {
		setConnected(vertex);
		return true;
	    } else if (isNeighborhoodRegular(vertex)) {
		setPatch(vertex);
		return true;
	    }
	    break;

	case SOAMConstants.HALF_DISK:
	    if (!isNeighborhoodHabituated(vertex)) {
		setHabituated(vertex);
		return true;
	    } else if (vertex.touched && !is1HalfDisk(vertex)) {
		setConnected(vertex);
		return true;
	    } else if (isNeighborhoodRegular(vertex)) {
		setBoundary(vertex);
		return true;
	    }
	    break;

	case SOAMConstants.PATCH:
	    if (!isNeighborhoodHabituated(vertex)) {
		setHabituated(vertex);
		return true;
	    } else if (vertex.touched && !is1Disk(vertex)) {
		setConnected(vertex);
		return true;
	    } else if (!isNeighborhoodRegular(vertex)) {
		setDisk(vertex);
		return true;
	    }
	    break;

	case SOAMConstants.BOUNDARY:
	    if (!isNeighborhoodHabituated(vertex)) {
		setHabituated(vertex);
		return true;
	    } else if (vertex.touched && !is1HalfDisk(vertex)) {
		setConnected(vertex);
		return true;
	    } else if (!isNeighborhoodRegular(vertex)) {
		setHalfDisk(vertex);
		return true;
	    }
	}

	// No state changes
	return false;
    }

    /**
     * Perform any verification activity, if required (for debugging purposes
     * only)
     */
    @Override
    protected void validate() {
	if (!validate(bestMatching)) {
	    // Just for debugging
	    validate(bestMatching);
	    System.err.println("Vertex " + bestMatching + " is not valid.");
	}
    }

    /**
     * Make sure that the vertex is actually in the state it claims to be
     */
    protected boolean validate(Vertex vertex) {

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
	    if (isSingular(vertex)) {
		return true;
	    }
	    break;

	case SOAMConstants.DISK:
	    if (is1Disk(vertex)) {
		return true;
	    }
	    break;

	case SOAMConstants.HALF_DISK:
	    if (is1HalfDisk(vertex)) {
		return true;
	    }
	    break;

	case SOAMConstants.PATCH:
	    if (is1Patch(vertex)) {
		return true;
	    }
	    break;

	case SOAMConstants.BOUNDARY:
	    if (is1Boundary(vertex)) {
		return true;
	    }
	    break;

	}

	return false;
    }

}