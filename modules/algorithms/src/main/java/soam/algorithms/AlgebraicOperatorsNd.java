
package soam.algorithms;


public class AlgebraicOperatorsNd {

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
     * Calculate the scalar product between two vectors "u" and "v"
     * 
     * @param u
     *            The first vector
     * @param v
     *            The second vector
     * @return The scalar product
     */
    public static double scalarProduct(final double[] u, final double[] v) {
	
        double result = 0;
        int l = u.length;
        for (int i = 0; i < l; i++) {
            result += u[i] * v[i];
        }
        return result;
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
	
        double result = 0;
        int l = u.length;
        for (int i = 0; i < l; i++) {
            result += (u[i] - o[i]) * (v[i] - o[i]);
        }
        return result;
    }

    /**
     * Calculate "u - v"
     * 
     * @param u
     *            The first vector
     * @param v
     *            The second vector
     * 
     * @return The difference vector
     */
    public static double[] minus(final double[] u, final double[] v) {
        double[] tmp = new double[u.length];
        for (int i = 0; i < tmp.length; i++) {
            tmp[i] = u[i] - v[i];
        }
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
        return (double) Math.sqrt(normSquare(u));
    }

    /**
     * Calculate the square of the norm of a vector "u"
     * 
     * @param u
     *            The vector
     * @return The norm
     */
    public static double normSquare(final double[] u) {
	
        double result = 0;
        double tmp = 0;
        int l = u.length;
        for (int i = 0; i < l; i++) {
            tmp = u[i];
            result += tmp * tmp;
        }
        return result;
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
	
        return (double) Math.sqrt(normSquare(u, o));
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
	
        double result = 0;
        double tmp = 0;
        int l = u.length;
        for (int i = 0; i < l; i++) {
            tmp = (u[i] - o[i]);
            result += tmp * tmp;
        }
        return result;
    }

    /**
     * Check if the angle between "u" and "v"
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
     * Calculate the angle between two vectors having vertex in "o"
     * 
     * @param u
     *            The first vector
     * @param v
     *            The second vector
     * @param o
     *            The origin
     * @return The angle between
     */
    public static double angle(final double[] u, final double[] v,
            final double[] o) {

        double scalarProduct_uv = scalarProduct(u, v, o);
        double denominator = (double) Math.sqrt(scalarProduct(u, u, o)
                * scalarProduct(v, v, o) - scalarProduct_uv * scalarProduct_uv);

        return (double) Math.atan2(denominator, scalarProduct_uv);
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