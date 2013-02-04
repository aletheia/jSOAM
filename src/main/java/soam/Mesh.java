

package soam;


import java.io.Serializable;
import java.util.ArrayList;


public class Mesh implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * This ArrayList contains all the vertices
     */
    public ArrayList<Vertex> vertexlist;

    /**
     * This ArrayList contains all connections (edges) between vertices
     */
    public ArrayList<Edge> edgelist;

    /**
     * The constructor
     */
    public Mesh() {
        vertexlist = new ArrayList<Vertex>();
        edgelist = new ArrayList<Edge>();
    }
}