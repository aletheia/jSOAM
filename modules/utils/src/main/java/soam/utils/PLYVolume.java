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

package soam.utils;

import java.io.File;
import java.util.Vector;
import java.util.Iterator;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

@SuppressWarnings("unchecked")
public class PLYVolume {

    /**
     * This class computes the total volume of a 3D triangular mesh, as read
     * from a ply file.<BR>
     * The mesh is supposed to obey certain conditions:
     * <ol>
     * <li>it represents one or more closed manifolds</li>
     * <li>it contains triangular faces only</li>
     * <li>all the faces are consistently oriented</li>
     * </ol>
     * <b>WARNING:</b> no consistency checks about the above conditions are
     * performed on the input mesh.
     * 
     * @see "Schneider, P. J., Eberly, D. H., <i>Geometric Tools For Computer
     *      Graphics</i>, Morgan Kaufmann Publishers, 2003, Section 13.12.3"
     * @param [-f filename]
     */
    public static void main(String[] args) {
	String defaultPath = System.getProperty("user.dir");

	File file;

	// Vertices
	Vector x;
	Vector y;
	Vector z;

	// Faces
	Vector faces = null;

	if (args.length >= 2 && args[0].equals("-f")) {
	    file = new File(args[1]);
	} else {
	    while (true) {
		JFileChooser fc = new JFileChooser(defaultPath);
		fc.addChoosableFileFilter(new FileNameExtensionFilter(
			"PLY Files", "ply"));
		fc.setDialogTitle("PLYVolume : PLY file");

		if (fc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
		    file = fc.getSelectedFile();
		    defaultPath = file.getAbsolutePath();

		    break;

		} else {
		    return;
		}
	    }
	}

	try {
	    PLY plyObj = new PLY();
	    System.out.print("Loading file: " + file.getAbsolutePath()
		    + " ... ");
	    plyObj.loadfile(file.getAbsolutePath());

	    try {
		faces = (Vector) plyObj.getProperty("vertex_indices", "face");
	    } catch (Exception e) {
		// Let's try again, with the other name
	    }
	    
	    if (faces == null) {
		faces = (Vector) plyObj.getProperty("vertex_index", "face");		
	    }

	    x = (Vector) plyObj.getProperty("x", "vertex");
	    y = (Vector) plyObj.getProperty("y", "vertex");
	    z = (Vector) plyObj.getProperty("z", "vertex");

	    System.out.println("loaded.");

	} catch (Exception e) {
	    System.err.println("ERROR: Could not load PLY file.");
	    return;
	}

	System.out.println("The mesh contains " + x.size() + " vertices and "
		+ faces.size() + " faces.");

	// volume_{tetrahedron} = \frac{1}{6} \sum_{i} (v_{0,i} \cdot (v_{1,i}
	// \times v_{2,i}))
	double volume = 0d;
	Iterator iterator = faces.iterator();
	while (iterator.hasNext()) {
	    Vector face = (Vector) iterator.next();

	    if (face.size() != 3) {
		System.err
			.println("ERROR: The mesh contains non-triangular faces.");
		return;
	    }

	    double x0 =
		    ((Float) x.get(((Integer) face.get(0)).intValue()))
			    .doubleValue();
	    double y0 =
		    ((Float) y.get(((Integer) face.get(0)).intValue()))
			    .doubleValue();
	    double z0 =
		    ((Float) z.get(((Integer) face.get(0)).intValue()))
			    .doubleValue();

	    double x1 =
		    ((Float) x.get(((Integer) face.get(1)).intValue()))
			    .doubleValue();
	    double y1 =
		    ((Float) y.get(((Integer) face.get(1)).intValue()))
			    .doubleValue();
	    double z1 =
		    ((Float) z.get(((Integer) face.get(1)).intValue()))
			    .doubleValue();

	    double x2 =
		    ((Float) x.get(((Integer) face.get(2)).intValue()))
			    .doubleValue();
	    double y2 =
		    ((Float) y.get(((Integer) face.get(2)).intValue()))
			    .doubleValue();
	    double z2 =
		    ((Float) z.get(((Integer) face.get(2)).intValue()))
			    .doubleValue();

	    double tmpX = x0 * (y1 * z2 - y2 * z1);
	    double tmpY = y0 * (x2 * z1 - x1 * z2);
	    double tmpZ = z0 * (x1 * y2 - x2 * y1);

	    volume += tmpX + tmpY + tmpZ;
	}

	volume /= 6d;

	System.out.println("The total volume is: " + volume);

    }

}
