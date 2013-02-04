

package soam;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * 
 * @author Marco Piastra, Matteo Stori
 * 
 */
public class Edge implements Externalizable {
    private static final long serialVersionUID = 1L;

    /**
     * Starting point of the edge
     */
    public Vertex start;

    /**
     * Ending point of the edge
     */
    public Vertex end;

    /**
     * Age of the edge
     */
    public int age;

    /**
     * The constructor
     */
    public Edge(Vertex start, Vertex end) {
	this.start = start;
	this.end = end;
	age = 0;
	if (start == end) {
	    System.out.println("How come? - KB");
	}
    }

    /**
     * The void constructor, for externalization purposes only
     */
    public Edge() {
	age = 0;
    }

    /**
     * This method checks if the specified vertex belongs to the edge
     * 
     * @param vertex
     *            The vertex to check
     * @return true if the vertex belongs to the edge
     */
    public boolean hasVertex(Vertex vertex) {
	if (vertex == start || vertex == end) {
	    return true;
	} else {
	    return false;
	}
    }

    /**
     * Return the other vertex, given one vertex
     * 
     * @param vertex
     *            The given vertex
     * @return The other vertex of the edge
     */
    public Vertex getOtherVertex(Vertex vertex) {
	if (start == vertex) {
	    return end;
	} else if (end == vertex) {
	    return start;
	} else {
	    throw new RuntimeException("No such vertex");
	}
    }

    /**
     * The method writes on the output stream.
     * 
     * @see Edge#readExternal(ObjectOutput)
     * @see Vertex
     */
    public void writeExternal(ObjectOutput stream) throws IOException {
	stream.writeObject(start);
	stream.writeObject(end);
	stream.writeInt(age);
    }

    /**
     * Given that the neighborhood of vertices is NOT written to the stream, the
     * latter has to be reconstructed after loading.
     * 
     * @see Vertex
     */
    public void readExternal(ObjectInput stream) throws IOException {
	try {
	    start = (Vertex) stream.readObject();
	    end = (Vertex) stream.readObject();
	} catch (ClassNotFoundException e) {
	    throw new IOException(e.toString());
	}
	age = stream.readInt();
	
	// Incremental neighborhood reconstruction
	start.neighborhood.add(this);
	end.neighborhood.add(this);	
    }

}