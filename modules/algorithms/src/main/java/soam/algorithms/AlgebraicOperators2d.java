
package soam.algorithms;

public class AlgebraicOperators2d {

    /**
     * Check if the 2D segment (a,b) intersects (c, d)
     * 
     * @see "Erickson, J., <a
     *      href="http://compgeom.cs.uiuc.edu/~jeffe/teaching/algorithms/notes/x06-sweepline.pdf">
     *      Line Segment Intersection</a>, <i>Algorithms Course Materials</i>,
     *      University of Illinois at Urbana-Champaign, 2006."
     * 
     * @param a
     *            The first point of the first segment
     * @param b
     *            The second point of the first segment
     * @param c
     *            The first point of the second segment
     * @param d
     *            The second point of the second segment
     * @return True if the two segments intersect
     */
    public static boolean intersects(final double[] a, final double[] b,
            final double[] c, final double[] d) {
        return (areCCWSorted(a, c, d) != areCCWSorted(b, c, d))
                && (areCCWSorted(a, b, c) != areCCWSorted(a, b, d));
    }

    /**
     * Check if three 2D points a, b and c are in counter-clockwise order
     * 
     * @param a
     *            The first point
     * @param b
     *            The second point
     * @param c
     *            The third point
     * @return True if the three points are CCW sorted
     * @see "Erickson, J., <a
     *      href="http://compgeom.cs.uiuc.edu/~jeffe/teaching/algorithms/notes/x05-convexhull.pdf">
     *      Convex Hulls</a>, <i>Algorithms Course Materials</i>, University
     *      of Illinois at Urbana-Champaign, 2006."
     */
    public static boolean areCCWSorted(final double[] a, final double[] b,
            final double[] c) {
        return ((c[1] - a[1]) * (b[0] - a[0])) > ((b[1] - a[1]) * (c[0] - a[0]));
    }

    /**
     * Check if the 2D triangle (a,b,c) is obtuse
     * 
     * @param a
     *            The first point
     * @param b
     *            The second point
     * @param c
     *            The third point
     * @return True if the triangle is obtuse
     */
    public static boolean isTriangleObtuse(final double[] a, final double[] b,
            final double[] c) {

        return isAngleObtuse(b, c, a) || isAngleObtuse(a, b, c)
                || isAngleObtuse(a, c, b);
    }

    /**
     * Returns true if the sector between two 2D vectors a and b is convex. The
     * cross product between u=(a[0],a[1],0) and v=(b[0],b[1],0), which yields a
     * normal vector with a positive 'z' component: this means that the angle
     * between u and v is between 0 and PI. Recall that cross product is
     * anti-symmetric, i.e. u x v = - v x u.
     * 
     * @param a
     *            The first vector
     * @param b
     *            The second vector
     */
    public static boolean isSectorConvex(final double[] a, final double[] b) {

        // Returns the sign of the z component in the cross product u x v
        // | x y z |
        // | a[0] b[0] 0 |
        // | a[1] b[1] 0 |
        return (a[0] * b[1] - a[1] * b[0]) >= 0;
    }

    /**
     * Returns true if the sector between the two vectors (a - o) and (b - o) is
     * convex.
     * 
     * @param a
     *            The first vector
     * @param b
     *            The second vector
     * @param o
     *            The origin vector
     * 
     * @see #isSectorConvex(double[] a, double[] b)
     */
    public static boolean isSectorConvex(final double[] a, final double[] b,
            final double[] o) {

        return ((a[0] - o[0]) * (b[1] - o[1]) - (a[1] - o[1]) * (b[0] - o[0])) >= 0;
    }

    /**
     * Returns true if vector a lies inside the counter-clockwise sector between
     * vector b and vector c
     * 
     * @param a
     *            The first vector
     * @param b
     *            The second vector
     * @param c
     *            The third vector
     * @see <a
     *      href="http://www.geometrictools.com/Documentation/MinimalCycleBasis.pdf">Eberly,
     *      D., <i>The Minimal Cycle Basis for a Planar Graph</i>, Geometric
     *      Tools Inc., November 2, 2005</a>
     * 
     */
    public static boolean isCCWBetween(final double[] a, final double[] b,
            final double[] c) {

        if (isSectorConvex(b, c)) {
            // Convex
            return isSectorConvex(b, a) && isSectorConvex(a, c);
        } else {
            // Reflex
            return !(!isSectorConvex(b, a) && !isSectorConvex(a, c));
        }
    }

    /**
     * Returns true if vector (a - o) lies inside the counter-clockwise sector
     * between (b - o) and (c - o)
     * 
     * @param a
     *            The first vector
     * @param b
     *            The second vector
     * @param c
     *            The third vector
     * @param o
     *            The origin vector
     * 
     * @see #isCCWBetween(double[] a, double[] b, double[] c)
     */
    public static boolean isCCWBetween(final double[] a, final double[] b,
            final double[] c, final double o[]) {

        if (isSectorConvex(b, c, o)) {
            // Convex
            return isSectorConvex(b, a, o) && isSectorConvex(a, c, o);
        } else {
            // Reflex
            return !(!isSectorConvex(b, a, o) && !isSectorConvex(a, c, o));
        }
    }

    /**
     * Calculate the scalar product between two vectors "u" and "v"
     * 
     * @param u
     *            The first vector
     * @param v
     *            The second vector
     * @return The scalar product
     */
    public static double scalarProduct(final double[] u, final double[] v) {
        return (u[0] * v[0] + u[1] * v[1]);
    }

    /**
     * Calculate the scalar product between two vectors "u" and "v" having
     * origin o
     * 
     * @param u
     *            The first vector
     * @param v
     *            The second vector
     * @param o
     *            The second vector
     * @return The scalar product
     */
    public static double scalarProduct(final double[] u, final double[] v,
            final double[] o) {
        return ((u[0] - o[0]) * (v[0] - o[0]))
                + ((u[1] - o[1]) * (v[1] - o[1]));
    }

    /**
     * Calculate "u - v"
     * 
     * @param u
     *            The first vector
     * @param v
     *            The second vector
     * 
     * @return The norm
     */
    public static double[] minus(final double[] u, final double[] v) {
        double[] tmp = { u[0] - v[0], u[1] - v[1] };
        return tmp;
    }

    /**
     * Calculate the norm of a vector "u"
     * 
     * @param u
     *            The vector
     * @return The norm
     */
    public static double norm(final double[] u) {
        return (double) Math.sqrt(scalarProduct(u, u));
    }

    /**
     * Calculate the square of the norm of a vector "u"
     * 
     * @param u
     *            The vector
     * @return The norm
     */
    public static double normSquare(final double[] u) {
        return scalarProduct(u, u);
    }

    /**
     * Calculate the norm of a vector "u" having vertex in "o"
     * 
     * @param u
     *            The first vector
     * @param o
     *            The origin
     * @return The norm
     */
    public static double norm(final double[] u, final double o[]) {
        return (double) Math.sqrt(scalarProduct(u, u, o));
    }

    /**
     * Calculate the norm of a vector "u" having vertex in "o"
     * 
     * @param u
     *            The first vector
     * @param o
     *            The origin
     * @return The norm
     */
    public static double normSquare(final double[] u, final double o[]) {
        return scalarProduct(u, u, o);
    }

    /**
     * Check if the angle between "u" and "v" is obtuse
     * 
     * @param u
     *            The first vector
     * @param v
     *            The second vector
     * @return true if the angle is obtuse
     */
    public static boolean isAngleObtuse(final double[] u, final double[] v) {
        return scalarProduct(u, v) < 0;
    }

    /**
     * Check if the angle between "u" and "v" with origin "o" is obtuse
     * 
     * @param u
     *            The first vector
     * @param v
     *            The second vector
     * @param o
     *            The origin
     * @return true if the angle is obtuse
     */
    public static boolean isAngleObtuse(final double[] u, final double[] v,
            final double[] o) {
        return scalarProduct(u, v, o) < 0;
    }

    /**
     * Calculate the cosine of the angle between two vectors "u" and "v"
     * 
     * @param u
     *            The first vector
     * @param v
     *            The second vector
     * @return The cosine of the angle
     */
    public static double angleCosine(final double[] u, final double[] v) {
        return scalarProduct(u, v) / (norm(u) * norm(v));
    }

    /**
     * Calculate the cosine of the angle between two vectors "u" and "v" with
     * origin "o"
     * 
     * @param u
     *            The first vector
     * @param v
     *            The second vector
     * @param o
     *            The origin
     * @return The cosine of the angle
     */
    public static double angleCosine(final double[] u, final double[] v,
            final double[] o) {
        return scalarProduct(u, v, o) / (norm(u, o) * norm(v, o));
    }

    /**
     * Calculate the cotan of the angle between the vector "u" and "v" having
     * vertex in "o"
     * 
     * @param u
     *            The first vector
     * @param v
     *            The second vector
     * @param o
     *            The origin
     * 
     * @return The cotan of the angle between v and u
     * 
     * @see "Meyer, M., Desbrun, M., Schroeder, P., Barr, A. H., <a
     *      href="http://www.cs.caltech.edu/~mmeyer/Publications/diffGeomOps.pdf">Discrete
     *      Differential-Geometry Operators for Triangulated 2-Manifolds</a>,
     *      <i>Proc. of Visualization and Mathematics</i>, Berlin, 2002,
     *      Appendix B , p. 25"
     * 
     */
    public static double angleCotan(final double[] u, final double[] v,
            final double[] o) {

        double normSquare_u = normSquare(u, o);
        double normSquare_v = normSquare(v, o);
        double scalarProduct_uv = scalarProduct(u, v, o);
        if (u.equals(v)) {
            return 0;
        } else {
            return scalarProduct_uv
                    / (double) Math.sqrt(normSquare_u * normSquare_v
                            - scalarProduct_uv * scalarProduct_uv);
        }
    }

    /**
     * Calculate the area of the triangular face described by two vectors "u"
     * and "v" having vertex in "o". Use the cross product rule: area = 1/2 * ||
     * u x v ||
     * 
     * @param u
     *            The first vector
     * @param v
     *            The second vector
     * @return The area
     * 
     * @see "Meyer, M., Desbrun, M., Schroeder, P., Barr, A. H., <a
     *      href="http://www.cs.caltech.edu/~mmeyer/Publications/diffGeomOps.pdf">Discrete
     *      Differential-Geometry Operators for Triangulated 2-Manifolds</a>,
     *      <i>Proc. of Visualization and Mathematics</i>, Berlin, 2002,
     *      Section 7.1"
     */
    public static double triangleArea(final double[] u, final double[] v,
            final double[] o) {

        double normSquare_u = normSquare(u, o);
        double normSquare_v = normSquare(v, o);
        double scalarProduct_uv = scalarProduct(u, v, o);
        return Math.sqrt(normSquare_u * normSquare_v - scalarProduct_uv
                * scalarProduct_uv) / 2d;
    }

}