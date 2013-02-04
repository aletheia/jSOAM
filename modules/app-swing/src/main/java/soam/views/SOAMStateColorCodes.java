/**
 * 
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

package soam.views;

import java.awt.Color;

import soam.Vertex;
import soam.algorithms.SOAMConstants;
import soam.algorithms.GWR;

public class SOAMStateColorCodes implements SOAMConstants {
    private static Color patchColor = new Color(191, 0, 0);
    private static Color boundaryColor = new Color(159, 0, 159);

    public static Color getColorFor(Vertex vertex) {

	switch (vertex.state) {
	case ACTIVE:
	    if (GWR.isHabituated(vertex)) {
		return Color.green;
	    } else {
		return Color.cyan;
	    }
	case HABITUATED:
	    return Color.green;
	case CONNECTED:
	    return Color.yellow;
	case SINGULAR:
	    return Color.blue;
	case DISK:
	    return Color.red;
	case HALF_DISK:
	    return Color.magenta;
	case PATCH:
	    return patchColor;
	case BOUNDARY:
	    return boundaryColor;
	}

	throw new RuntimeException("Unknown state: " + vertex.state);
    }

}
