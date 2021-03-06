/**
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.

 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.

 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package soam;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectInput;

import java.text.DecimalFormat;
import java.util.ArrayList;


public class Vertex implements Externalizable {
    private static final long serialVersionUID = 1L;

    /**
     * This array list of edge represents the neighborhood
     */
    public ArrayList<Edge> neighborhood;

    /**
     * The coordinate system
     */
    public double[] position;

    /**
     * The firing counter
     */
    public double firingCounter;

    /**
     * The integer that owns the information of the state of the vertex 0 =
     * ACTIVE 1 = HABITUATED 2 = CONNECTED 3 = SINGULAR 4 = DISK 5 = HALF_DISK 6
     * = PATCH 7 = BOUNDARY default is ACTIVE (0)
     */
    public int state = 0;

    /**
     * Last winner indicates the last time (in tick) that the vertex was
     * bestMatching
     */
    public long lastWin = 0;

    /**
     * This variable represents the vertex insertionThresold, for the adaptive
     * methods
     */
    public double insertionThreshold = 25d;

    /**
     * It will be true whenever the vertex has been altered by the algorithm
     */
    public boolean touched = false;

    /**
     * Error of the vertex
     */
    public double error;    
        
    /**
     * Adaptation accumulator
     */
    public transient double[] accumulator;
    
    /**
     * Adaptation accumulator counter
     */
    public transient int accumulatorCounter;    
    
    /**
     * The constructor
     */
    public Vertex(double[] position) {
	this.position = position;
	neighborhood = new ArrayList<Edge>();
    }

    /**
     * The void constructor, for externalization purposes only
     */
    public Vertex() {
	this.position = null;
	neighborhood = new ArrayList<Edge>();
    }

    /**
     * The method writes on the output stream the entire content of the vertex
     * EXCEPT the neighborhood. This is intended to avoid the stack overflow
     * that may otherwise result when serializing large meshes.
     * 
     * @see Edge
     */
    public void writeExternal(ObjectOutput stream) throws IOException {
	stream.writeObject(position);
	stream.writeDouble(firingCounter);
	stream.writeInt(state);
	stream.writeLong(lastWin);
	stream.writeDouble(insertionThreshold);
	stream.writeBoolean(touched);
	stream.writeDouble(error);
    }

    /**
     * See the write method
     * 
     * @see Vertex#writeExternal(ObjectOutput)
     * @see Edge
     */
    public void readExternal(ObjectInput stream) throws IOException {
	try {
	    position = (double[]) stream.readObject();
	} catch (ClassNotFoundException e) {
	    throw new IOException(e.toString());
	}
	firingCounter = stream.readDouble();
	state = stream.readInt();
	lastWin = stream.readLong();
	insertionThreshold = stream.readDouble();
	touched = stream.readBoolean();
	error = stream.readDouble();
    }

    /**
     * Modified by Luca Bianchi
     */
    @Override
    public boolean equals(Object o){
		Vertex v = (Vertex) o;
    	
		return ((v.position == this.position));
    }
    

    
    /**
     * Calculates vertex neightbohrs
     * Added by Luca Bianchi
     */
    public ArrayList<Vertex> getNeighbors(){
    	ArrayList<Vertex> neighbors = new ArrayList<Vertex>();
    	for(Edge connection:neighborhood){
    		neighbors.add(connection.getOtherVertex(this));
    	}
    	return neighbors;
    }

    /**
     * Vertex toString
     * Added by Luca Bianchi
     */
    @Override
    public String toString(){
    	DecimalFormat df = new DecimalFormat("#.##");
    	StringBuffer sb = new StringBuffer("Vertex(");
    	for(int i=0;i<this.position.length;i++){
    		sb.append(df.format(this.position[i])+";");
    	}
    	sb.deleteCharAt(sb.length()-1);
    	sb.append(")");
    	return sb.toString();
    }
    

}