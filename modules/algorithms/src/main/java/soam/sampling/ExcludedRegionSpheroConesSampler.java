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

package soam.sampling;

import java.util.ArrayList;
import java.util.Random;
import java.awt.image.BufferedImage;

import soam.algorithms.Algorithm;
import soam.algorithms.AlgebraicOperatorsNd;
import soam.utils.Transformation3D;

public class ExcludedRegionSpheroConesSampler implements Sampler {
    protected final static double CUBE_SIZE = 256d;
    protected final static int MAX_RETRIES = 1024;

    protected Algorithm algorithm;

    protected double C1_HEIGHT = 50d;
    protected double C1_RADIUS = 25d;
    protected double C1_RHO = 5d;

    protected double C2_HEIGHT = 50d;
    protected double C2_RADIUS = 25d;
    protected double C2_RHO = 5d;

    protected double C2_ROTATION_AROUND_Y = 0d;

    protected double EPSILON = 0.6d;

    protected boolean SAMPLE_C1_SPHERICAL_CAP = true;
    protected boolean SAMPLE_C1_BASE = true;
    protected boolean SAMPLE_C1_SIDE = true;
    protected boolean SAMPLE_C1_TOROIDAL = true;
    protected boolean SAMPLE_C2_SPHERICAL_CAP = true;
    protected boolean SAMPLE_C2_BASE = true;
    protected boolean SAMPLE_C2_SIDE = true;
    protected boolean SAMPLE_C2_TOROIDAL = true;
    protected boolean SAMPLE_C1_TOROIDAL_HS = true;
    protected boolean SAMPLE_C2_TOROIDAL_HS = true;
    protected boolean SAMPLE_HOT_SPOT_1 = true;
    protected boolean SAMPLE_HOT_SPOT_2 = true;

    protected double gamma1;
    protected double gamma2;

    protected double[] e1 = { 0d, 0d, 1d };
    protected double[] e2 = { 0d, 0d, 1d };

    protected double[] eStar2;

    protected Transformation3D e2Rotation;

    protected double totalArea;

    protected double cone1SphericalCapArea;
    protected double cone1BaseArea;
    protected double cone1SideArea;
    protected double cone1ToroidalPatchArea;

    protected double cone2SphericalCapArea;
    protected double cone2SideArea;
    protected double cone2BaseArea;
    protected double cone2ToroidalPatchArea;

    protected double cone1hs1Theta;
    protected double cone2hs1Theta;

    protected double cone1hs2Theta;
    protected double cone2hs2Theta;

    protected double[] hs1Normal;
    protected double[] hs2Normal;

    protected double coneSideHsArea;
    protected double cone1ToroidalHsArea;
    protected double cone2ToroidalHsArea;

    protected Random random;

    public boolean initialize(Algorithm algorithm) {

	this.algorithm = algorithm;

	reset();
	random = new Random();

	return true;
    }

    public void reset() {
	computeBasicParameters();
	computeSurfaceAreas();

	return;
    }

    public int getDimensions() {
	return 3;
    }

    public String getDescription() {
	return "";
    }

    public BufferedImage getBufferedImage() {
	return null;
    }

    public ArrayList<double[]> getPointCloud() {
	return null;
    }

    /**
     * This method must ensure a non-null sample. Retries are performed only
     * when sampling of some surface patches is disabled. It will not happen
     * under normal circumstances.
     */
    public double[] nextSample() {
	for (int r = 0; r < MAX_RETRIES; r++) {
	    double[] p = basicNextSample();

	    if (p == null) {
		continue;
	    }

	    for (int i = 0; i < p.length; i++) {
		p[i] += CUBE_SIZE / 2;
	    }

	    return p;
	}

	throw new RuntimeException("Unable to generate a sample");
    }

    protected double[] basicNextSample() {
	double v = random.nextDouble() * totalArea;

	if (v < cone1SphericalCapArea) {
	    if (SAMPLE_C1_SPHERICAL_CAP) {
		return sampleCone1SphericalCap();
	    } else {
		return null;
	    }
	} else if (v < cone1SphericalCapArea + cone1BaseArea) {
	    if (SAMPLE_C1_BASE) {
		return sampleCone1Base();
	    } else {
		return null;
	    }
	} else if (v < cone1SphericalCapArea + cone1BaseArea + cone1SideArea) {
	    if (SAMPLE_C1_SIDE) {
		return sampleCone1Side();
	    } else {
		return null;
	    }
	} else if (v < cone1SphericalCapArea + cone1BaseArea + cone1SideArea
		+ cone1ToroidalPatchArea) {
	    if (SAMPLE_C1_TOROIDAL) {
		return sampleCone1ToroidalPatch(false);
	    } else {
		return null;
	    }
	} else if (v < cone1SphericalCapArea + cone1BaseArea + cone1SideArea
		+ cone1ToroidalPatchArea + cone1SphericalCapArea) {
	    if (SAMPLE_C2_SPHERICAL_CAP) {
		return sampleCone2SphericalCap();
	    } else {
		return null;
	    }
	} else if (v < cone1SphericalCapArea + cone1BaseArea + cone1SideArea
		+ cone1ToroidalPatchArea + cone1SphericalCapArea
		+ cone2BaseArea) {
	    if (SAMPLE_C2_BASE) {
		return sampleCone2Base();
	    } else {
		return null;
	    }
	} else if (v < cone1SphericalCapArea + cone1BaseArea + cone1SideArea
		+ cone1ToroidalPatchArea + cone1SphericalCapArea
		+ cone2BaseArea + cone2SideArea) {
	    if (SAMPLE_C2_SIDE) {
		return sampleCone2Side();
	    } else {
		return null;
	    }
	} else if (v < cone1SphericalCapArea + cone1BaseArea + cone1SideArea
		+ cone1ToroidalPatchArea + cone1SphericalCapArea
		+ cone2BaseArea + cone2SideArea + cone2ToroidalPatchArea) {
	    if (SAMPLE_C2_TOROIDAL) {
		return sampleCone2ToroidalPatch(false);
	    } else {
		return null;
	    }
	} else if (v < cone1SphericalCapArea + cone1BaseArea + cone1SideArea
		+ cone1ToroidalPatchArea + cone1SphericalCapArea
		+ cone2BaseArea + cone2SideArea + cone2ToroidalPatchArea
		+ cone1ToroidalHsArea) {
	    if (SAMPLE_C1_TOROIDAL_HS) {
		return sampleCone1ToroidalPatch(true);
	    } else {
		return null;
	    }
	} else if (v < cone1SphericalCapArea + cone1BaseArea + cone1SideArea
		+ cone1ToroidalPatchArea + cone1SphericalCapArea
		+ cone2BaseArea + cone2SideArea + cone2ToroidalPatchArea
		+ cone1ToroidalHsArea + cone2ToroidalHsArea) {
	    if (SAMPLE_C2_TOROIDAL_HS) {
		return sampleCone2ToroidalPatch(true);
	    } else {
		return null;
	    }
	} else {
	    v = random.nextDouble();
	    if (v < 0.5d) {
		if (SAMPLE_HOT_SPOT_1) {
		    return sampleHotSpot(cone1hs1Theta, cone2hs1Theta,
			    hs1Normal);
		} else {
		    return null;
		}
	    } else {
		if (SAMPLE_HOT_SPOT_2) {
		    return sampleHotSpot(cone1hs2Theta, cone2hs2Theta,
			    hs2Normal);
		} else {
		    return null;
		}
	    }
	}
    }

    public double[] nextSample(final double[] point, double radius) {
	throw new RuntimeException("Method not implemented");
    }

    protected void computeBasicParameters() {

	// Opening Angles
	gamma1 = Math.atan(C1_RADIUS / C1_HEIGHT);
	gamma2 = Math.atan(C2_RADIUS / C2_HEIGHT);

	// Versors
	e2 = new double[] { 0d, 0d, 1d };

	e2Rotation = new Transformation3D();

	e2Rotation.yRotate(-C2_ROTATION_AROUND_Y);

	e2Rotation.transform(e2, e2);

	eStar2 = new double[3];
	for (int i = 0; i < e2.length; i++) {
	    eStar2[i] = -e2[i];
	}

	// HotSpots
	double delta =
		-2 * Math.cos(C2_ROTATION_AROUND_Y) * Math.sin(gamma2)
			* Math.sin(gamma1) + Math.cos(gamma1)
			* Math.cos(gamma1) - Math.cos(C2_ROTATION_AROUND_Y)
			* Math.cos(C2_ROTATION_AROUND_Y) - Math.sin(gamma2)
			* Math.sin(gamma2);

	if (delta > 0 && Math.sin(C2_ROTATION_AROUND_Y) > 0) {
	    double xNum = Math.sqrt(delta);
	    double den1 = (Math.cos(gamma1) * Math.sin(C2_ROTATION_AROUND_Y));
	    double den2 = (Math.cos(gamma2) * Math.sin(C2_ROTATION_AROUND_Y));

	    cone1hs1Theta =
		    Math.atan2(xNum / den1, (Math.cos(C2_ROTATION_AROUND_Y)
			    * Math.sin(gamma1) + Math.sin(gamma2))
			    / den1);
	    cone2hs1Theta =
		    Math.atan2(-xNum / den2, -(Math.cos(C2_ROTATION_AROUND_Y)
			    * Math.sin(gamma2) + Math.sin(gamma1))
			    / den2);

	    cone1hs2Theta =
		    Math.atan2(-xNum / den1, (Math.cos(C2_ROTATION_AROUND_Y)
			    * Math.sin(gamma1) + Math.sin(gamma2))
			    / den1);
	    cone2hs2Theta =
		    Math.atan2(xNum / den2, -(Math.cos(C2_ROTATION_AROUND_Y)
			    * Math.sin(gamma2) + Math.sin(gamma1))
			    / den2);

	} else {
	    cone1hs1Theta = 0d;
	    cone2hs1Theta = 0d;
	    cone1hs2Theta = 0d;
	    cone2hs2Theta = 0d;
	}

	hs1Normal = new double[] { 1d, 0d, 0d };
	Transformation3D rot = new Transformation3D();
	rot.zRotate(cone1hs1Theta);
	rot.yRotate(-gamma1);

	rot.transform(hs1Normal, hs1Normal);

	hs2Normal = new double[] { 1d, 0d, 0d };
	rot.unit();
	rot.zRotate(cone1hs2Theta);
	rot.yRotate(-gamma1);

	rot.transform(hs2Normal, hs2Normal);
    }

    protected void computeSurfaceAreas() {

	// Spherical Caps
	double hCap1 = C1_RHO * (1 - Math.sin(gamma1));
	cone1SphericalCapArea = 2 * Math.PI * C1_RHO * hCap1;
	double hCap2 = C2_RHO * (1 - Math.sin(gamma2));
	cone2SphericalCapArea = 2 * Math.PI * C2_RHO * hCap2;

	// Cone Base
	cone1BaseArea = Math.PI * C1_RADIUS * C1_RADIUS;
	cone2BaseArea = Math.PI * C2_RADIUS * C2_RADIUS;

	// Cone Side
	double lc1 = Math.sqrt(C1_HEIGHT * C1_HEIGHT + C1_RADIUS * C1_RADIUS);
	double rotRadiusC1 =
		0.5d * lc1 * Math.sin(gamma1) + C1_RHO * Math.cos(gamma1);
	cone1SideArea = 2 * Math.PI * rotRadiusC1 * lc1;

	double lc2 = Math.sqrt(C2_HEIGHT * C2_HEIGHT + C2_RADIUS * C2_RADIUS);
	double rotRadiusC2 =
		0.5d * lc2 * Math.sin(gamma2) + C2_RHO * Math.cos(gamma2);
	cone2SideArea = 2 * Math.PI * rotRadiusC2 * lc2;

	// Toroidal Patches
	double l11 = (C1_RHO * Math.PI) / 2;
	double rotRadius11 = C1_RADIUS + (2 * C1_RHO) / Math.PI;

	double l21 = gamma1 * C1_RHO;
	double rotRadius21 =
		C1_RADIUS
			+ ((C1_RHO * Math.sin(gamma1 * 0.5d)) / (gamma1 * 0.5d))
			* Math.cos(gamma1 * 0.5d);

	cone1ToroidalPatchArea =
		2 * Math.PI * l11 * rotRadius11 + 2 * Math.PI * l21
			* rotRadius21;

	double l12 = (C2_RHO * Math.PI) / 2;
	double rotRadius12 = C2_RADIUS + (2 * C2_RHO) / Math.PI;

	double l22 = gamma2 * C1_RHO;
	double rotRadius22 =
		C2_RADIUS
			+ ((C2_RHO * Math.sin(gamma2 * 0.5d)) / (gamma2 * 0.5d))
			* Math.cos(gamma2 * 0.5d);

	cone2ToroidalPatchArea =
		2 * Math.PI * l12 * rotRadius12 + 2 * Math.PI * l22
			* rotRadius22;

	// Hot Spots
	if (cone1hs1Theta != 0d && cone2hs1Theta != 0d) {

	    double[] cone1Apex = new double[] { 0d, 0d, 0.75d * C1_HEIGHT };
	    double[] cone2StarApex = new double[] { 0d, 0d, 0.75d * C2_HEIGHT };

	    invertAndRotate(e2Rotation, cone2StarApex, cone2StarApex);

	    double[] cone1hs1Base =
		    new double[] { C1_RADIUS, 0d, -0.25 * C1_HEIGHT };
	    double[] cone2StarHs1Base =
		    new double[] { C1_RADIUS, 0d, -0.25 * C1_HEIGHT };

	    Transformation3D rot = new Transformation3D();
	    rot.zRotate(cone1hs1Theta);
	    rot.transform(cone1hs1Base, cone1hs1Base);

	    rot.unit();
	    rot.zRotate(cone2hs1Theta);
	    rot.transform(cone2StarHs1Base, cone2StarHs1Base);

	    invertAndRotate(e2Rotation, cone2StarHs1Base, cone2StarHs1Base);

	    // The area of each hotSpot is equal to the norm of the cross
	    // product
	    // (cone1Apex - cone1hs1Base) X (cone2StarApex - cone2StarHs1Base)
	    double x1 = cone1Apex[0] - cone1hs1Base[0];
	    double y1 = cone1Apex[1] - cone1hs1Base[1];
	    double z1 = cone1Apex[2] - cone1hs1Base[2];

	    double x2 = cone2StarApex[0] - cone2StarHs1Base[0];
	    double y2 = cone2StarApex[1] - cone2StarHs1Base[1];
	    double z2 = cone2StarApex[2] - cone2StarHs1Base[2];

	    double norm =
		    (y1 * z2 - y2 * z1) * (y1 * z2 - y2 * z1)
			    + (x1 * z2 - x2 * z1) * (x1 * z2 - x2 * z1)
			    + (x1 * y2 - x2 * y1) * (x1 * y2 - x2 * y1);

	    norm = Math.sqrt(norm);

	    coneSideHsArea = 2 * norm;
	} else {
	    coneSideHsArea = 0d;
	}

	cone1ToroidalHsArea =
		((Math.PI * 0.5d + gamma1) - (Math.PI - C2_ROTATION_AROUND_Y))
			* cone1ToroidalPatchArea;
	cone1ToroidalHsArea = Math.max(cone1ToroidalHsArea, 0);
	cone2ToroidalHsArea =
		((Math.PI * 0.5d + gamma2) - (Math.PI - C2_ROTATION_AROUND_Y))
			* cone2ToroidalPatchArea;
	cone2ToroidalHsArea = Math.max(cone2ToroidalHsArea, 0);

	totalArea =
		cone1SphericalCapArea + cone1BaseArea + cone1SideArea
			+ cone1ToroidalPatchArea + cone2SphericalCapArea
			+ cone2BaseArea + cone2SideArea
			+ cone2ToroidalPatchArea + cone1ToroidalHsArea
			+ cone2ToroidalHsArea + coneSideHsArea;
    }

    protected double[] sampleCone1SphericalCap() {
	double xi = gamma1 + (random.nextDouble() * (Math.PI * 0.5d - gamma1));
	double theta = random.nextDouble() * 2 * Math.PI;

	double[] n = { 1d, 0d, 0d };
	double[] p = { 0d, 0d, 0.75d * C1_HEIGHT };

	Transformation3D rot = new Transformation3D();

	rot.zRotate(theta);
	rot.yRotate(-xi);
	rot.transform(n, n);

	for (int i = 0; i < p.length; i++) {
	    p[i] += C1_RHO * n[i];
	}

	// return p;
	return excludedRegionSurfaceFromCone1(p, n);
    }

    protected double[] sampleCone1Base() {
	double u = random.nextDouble() * C1_RADIUS;
	double theta = random.nextDouble() * 2 * Math.PI;

	double[] n = { 0d, 0d, -1d };
	double[] p = { u, 0d, -0.25d * C1_HEIGHT };

	Transformation3D rot = new Transformation3D();

	rot.zRotate(theta);
	rot.transform(p, p);

	for (int i = 0; i < p.length; i++) {
	    p[i] += C1_RHO * n[i];
	}

	// return p;
	return excludedRegionSurfaceFromCone1(p, n);
    }

    protected double[] sampleCone1Side() {
	double u = random.nextDouble() * C1_HEIGHT;
	double theta = random.nextDouble() * 2 * Math.PI;

	double[] n = { 1d, 0d, 0d };
	double[] p =
		{ ((C1_HEIGHT - u) / C1_HEIGHT) * C1_RADIUS, 0d,
			u - (0.25d * C1_HEIGHT) };

	Transformation3D rot = new Transformation3D();

	rot.zRotate(theta);
	rot.transform(p, p);

	rot.yRotate(-gamma1);
	rot.transform(n, n);

	for (int i = 0; i < p.length; i++) {
	    p[i] += C1_RHO * n[i];
	}

	// return p;
	return excludedRegionSurfaceFromCone1(p, n);
    }

    protected double[] sampleCone1ToroidalPatch(boolean hotSpot) {
	double maxAngle = (gamma1 + Math.PI * 0.5d);
	if (hotSpot) {
	    maxAngle =
		    Math.min(maxAngle, (Math.PI - C2_ROTATION_AROUND_Y)
			    * (1 + EPSILON));
	}

	double xi = -Math.PI * 0.5d + (random.nextDouble() * maxAngle);
	double theta = random.nextDouble() * 2 * Math.PI;

	double[] n = { 1d, 0d, 0d };
	double[] p = { C1_RADIUS, 0d, -0.25d * C1_HEIGHT };

	Transformation3D rot = new Transformation3D();

	rot.zRotate(theta);
	rot.transform(p, p);

	rot.yRotate(-xi);
	rot.transform(n, n);

	for (int i = 0; i < p.length; i++) {
	    p[i] += C1_RHO * n[i];
	}

	// return p;
	return excludedRegionSurfaceFromCone1(p, n);
    }

    protected double[] excludedRegionSurfaceFromCone1(double[] p1, double[] n1) {
	double[] pStar2 = new double[3];
	double sinGamma2 = Math.sin(gamma2);

	double product = AlgebraicOperatorsNd.scalarProduct(n1, eStar2);

	if (product > sinGamma2) {
	    // pStar2 lies on the spherical cap of C2
	    for (int i = 0; i < pStar2.length; i++) {
		pStar2[i] =
			p1[i] + 0.75d * C2_HEIGHT * eStar2[i] + C2_RHO * n1[i];
	    }
	} else {
	    // pStar2 lies on the toroidal patch

	    // Gram-Schmidt orthogonalization of n1 w.r.t. eStar2
	    double[] ePerp2 = new double[3];
	    for (int i = 0; i < ePerp2.length; i++) {
		ePerp2[i] = n1[i] - product * eStar2[i];
	    }
	    double norm = AlgebraicOperatorsNd.norm(ePerp2);
	    for (int i = 0; i < ePerp2.length; i++) {
		ePerp2[i] /= norm;
	    }

	    for (int i = 0; i < pStar2.length; i++) {
		pStar2[i] =
			p1[i] - 0.25d * C2_HEIGHT * eStar2[i] + C2_RADIUS
				* ePerp2[i] + C2_RHO * n1[i];
	    }
	}

	return pStar2;
    }

    protected double[] sampleCone2SphericalCap() {
	double xi = gamma2 + (random.nextDouble() * (Math.PI * 0.5d - gamma2));
	double theta = random.nextDouble() * 2 * Math.PI;

	double[] n = { 1d, 0d, 0d };
	double[] p = { 0d, 0d, 0.75d * C2_HEIGHT };

	Transformation3D rot = new Transformation3D();

	rot.zRotate(theta);
	rot.yRotate(-xi);
	rot.transform(n, n);

	for (int i = 0; i < p.length; i++) {
	    p[i] += C2_RHO * n[i];
	}

	invertAndRotate(e2Rotation, n, n);
	invertAndRotate(e2Rotation, p, p);

	// return p;
	return excludedRegionSurfaceFromCone2(p, n);
    }

    protected double[] sampleCone2Base() {
	double u = random.nextDouble() * C2_RADIUS;
	double theta = random.nextDouble() * 2 * Math.PI;

	double[] n = { 0d, 0d, -1 };
	double[] p = { u, 0d, -0.25d * C2_HEIGHT };

	Transformation3D rot = new Transformation3D();

	rot.zRotate(theta);
	rot.transform(p, p);

	for (int i = 0; i < p.length; i++) {
	    p[i] += C2_RHO * n[i];
	}

	invertAndRotate(e2Rotation, n, n);
	invertAndRotate(e2Rotation, p, p);

	// return p;
	return excludedRegionSurfaceFromCone2(p, n);
    }

    protected double[] sampleCone2Side() {
	double u = random.nextDouble() * C2_HEIGHT;
	double theta = random.nextDouble() * 2 * Math.PI;

	double[] n = { 1d, 0d, 0d };
	double[] p =
		{ ((C2_HEIGHT - u) / C2_HEIGHT) * C2_RADIUS, 0d,
			u - (0.25d * C2_HEIGHT) };

	Transformation3D rot = new Transformation3D();

	rot.zRotate(theta);
	rot.transform(p, p);

	rot.yRotate(-gamma2);
	rot.transform(n, n);

	for (int i = 0; i < p.length; i++) {
	    p[i] += C2_RHO * n[i];
	}

	invertAndRotate(e2Rotation, n, n);
	invertAndRotate(e2Rotation, p, p);

	// return p;
	return excludedRegionSurfaceFromCone2(p, n);
    }

    protected double[] sampleCone2ToroidalPatch(boolean hotSpot) {
	double maxAngle = (gamma2 + Math.PI * 0.5d);
	if (hotSpot) {
	    maxAngle =
		    Math.min(maxAngle, (Math.PI - C2_ROTATION_AROUND_Y)
			    * (1 + EPSILON));
	}

	double xi = -Math.PI * 0.5d + (random.nextDouble() * maxAngle);
	double theta = random.nextDouble() * 2 * Math.PI;

	double[] n = { 1d, 0d, 0d };
	double[] p = { C2_RADIUS, 0d, -0.25d * C2_HEIGHT };

	Transformation3D rot = new Transformation3D();

	rot.zRotate(theta);
	rot.transform(p, p);

	rot.yRotate(-xi);
	rot.transform(n, n);

	for (int i = 0; i < p.length; i++) {
	    p[i] += C2_RHO * n[i];
	}

	invertAndRotate(e2Rotation, n, n);
	invertAndRotate(e2Rotation, p, p);

	// return p;
	return excludedRegionSurfaceFromCone2(p, n);
    }

    protected double[] excludedRegionSurfaceFromCone2(double[] p2, double[] n2) {
	double[] pStar2 = new double[3];
	double sinGamma1 = Math.sin(gamma1);

	double product = AlgebraicOperatorsNd.scalarProduct(n2, e1);

	if (product >= sinGamma1) {
	    // p2 lies on the spherical cap of C2
	    for (int i = 0; i < pStar2.length; i++) {
		pStar2[i] = p2[i] + 0.75d * C1_HEIGHT * e1[i] + C1_RHO * n2[i];
	    }
	} else {
	    // p2 lies on the toroidal patch of C2

	    // Gram-Schmidt orthogonalization of n2 w.r.t. e1
	    double[] ePerp1 = new double[3];
	    for (int i = 0; i < ePerp1.length; i++) {
		ePerp1[i] = n2[i] - product * e1[i];
	    }
	    double norm = AlgebraicOperatorsNd.norm(ePerp1);
	    for (int i = 0; i < ePerp1.length; i++) {
		ePerp1[i] /= norm;
	    }

	    for (int i = 0; i < pStar2.length; i++) {
		pStar2[i] =
			p2[i] - 0.25d * C1_HEIGHT * e1[i] + C1_RADIUS
				* ePerp1[i] + C1_RHO * n2[i];
	    }
	}

	return pStar2;
    }

    protected double[] sampleHotSpot(double c1Theta, double c2Theta,
	    double[] hsNormal) {
	double u1 = random.nextDouble() * C1_HEIGHT;

	double[] p1 =
		{ ((C1_HEIGHT - u1) / C1_HEIGHT) * C1_RADIUS, 0d,
			u1 - (0.25d * C1_HEIGHT) };

	Transformation3D rot = new Transformation3D();

	rot.zRotate(c1Theta);
	rot.transform(p1, p1);

	for (int i = 0; i < p1.length; i++) {
	    p1[i] += C1_RHO * hsNormal[i];
	}

	double u2 = random.nextDouble() * C2_HEIGHT;
	double[] n2 = { 1d, 0d, 0d };

	double[] pStar2 =
		{ ((C2_HEIGHT - u2) / C2_HEIGHT) * C2_RADIUS, 0d,
			u2 - (0.25d * C2_HEIGHT) };

	rot.unit();
	rot.zRotate(c2Theta);
	rot.transform(pStar2, pStar2);

	rot.yRotate(-gamma2);
	rot.transform(n2, n2);

	for (int i = 0; i < pStar2.length; i++) {
	    pStar2[i] += C2_RHO * n2[i];
	}

	invertAndRotate(e2Rotation, pStar2, pStar2);
	// Just for validation
	invertAndRotate(e2Rotation, n2, n2);

	for (int i = 0; i < pStar2.length; i++) {
	    pStar2[i] += p1[i];
	}

	return pStar2;
    }

    protected void invertAndRotate(Transformation3D rot, double[] vFrom,
	    double[] vTo) {

	for (int i = 0; i < e2.length; i++) {
	    vTo[i] = -vFrom[i];
	}

	rot.transform(vTo, vTo);
    }

    public double getC1_HEIGHT() {
	return C1_HEIGHT;
    }

    public void setC1_HEIGHT(double c1_height) {
	if (c1_height <= 0d) {
	    System.err.println("C1_HEIGHT cannot be null or negative");
	    return;
	}

	C1_HEIGHT = c1_height;
	reset();
    }

    public double getC1_RADIUS() {
	return C1_RADIUS;
    }

    public void setC1_RADIUS(double c1_radius) {
	if (c1_radius <= 0d) {
	    System.err.println("C1_RADIUS cannot be null or negative");
	    return;
	}

	C1_RADIUS = c1_radius;
	reset();
    }

    public double getC1_RHO() {
	return C1_RHO;
    }

    public void setC1_RHO(double c1_rho) {
	if (c1_rho <= 0d) {
	    System.err.println("C1_RHO cannot be null or negative");
	    return;
	}

	C1_RHO = c1_rho;
	reset();
    }

    public double getC2_HEIGHT() {
	return C2_HEIGHT;
    }

    public void setC2_HEIGHT(double c2_height) {
	if (c2_height <= 0d) {
	    System.err.println("C2_HEIGHT cannot be null or negative");
	    return;
	}

	C2_HEIGHT = c2_height;
	reset();
    }

    public double getC2_RADIUS() {
	return C2_RADIUS;
    }

    public void setC2_RADIUS(double c2_radius) {
	if (c2_radius <= 0d) {
	    System.err.println("C2_RADIUS cannot be null or negative");
	    return;
	}

	C2_RADIUS = c2_radius;
	reset();
    }

    public double getC2_RHO() {
	return C2_RHO;
    }

    public void setC2_RHO(double c2_rho) {
	if (c2_rho <= 0d) {
	    System.err.println("C2_RHO cannot be null or negative");
	    return;
	}

	C2_RHO = c2_rho;
	reset();
    }

    public double getC2_ROTATION_AROUND_Y() {
	return C2_ROTATION_AROUND_Y;
    }

    public void setC2_ROTATION_AROUND_Y(double c2_rotation_around_y) {
	C2_ROTATION_AROUND_Y = c2_rotation_around_y;
	reset();
    }
    
    public boolean isSAMPLE_C1_SPHERICAL_CAP() {
        return SAMPLE_C1_SPHERICAL_CAP;
    }

    public void setSAMPLE_C1_SPHERICAL_CAP(boolean sample_c1_spherical_cap) {
        SAMPLE_C1_SPHERICAL_CAP = sample_c1_spherical_cap;
    }

    public boolean isSAMPLE_C1_BASE() {
        return SAMPLE_C1_BASE;
    }

    public void setSAMPLE_C1_BASE(boolean sample_c1_base) {
        SAMPLE_C1_BASE = sample_c1_base;
    }

    public boolean isSAMPLE_C1_SIDE() {
        return SAMPLE_C1_SIDE;
    }

    public void setSAMPLE_C1_SIDE(boolean sample_c1_side) {
        SAMPLE_C1_SIDE = sample_c1_side;
    }

    public boolean isSAMPLE_C1_TOROIDAL() {
        return SAMPLE_C1_TOROIDAL;
    }

    public void setSAMPLE_C1_TOROIDAL(boolean sample_c1_toroidal) {
        SAMPLE_C1_TOROIDAL = sample_c1_toroidal;
    }

    public boolean isSAMPLE_C2_SPHERICAL_CAP() {
        return SAMPLE_C2_SPHERICAL_CAP;
    }

    public void setSAMPLE_C2_SPHERICAL_CAP(boolean sample_c2_spherical_cap) {
        SAMPLE_C2_SPHERICAL_CAP = sample_c2_spherical_cap;
    }

    public boolean isSAMPLE_C2_BASE() {
        return SAMPLE_C2_BASE;
    }

    public void setSAMPLE_C2_BASE(boolean sample_c2_base) {
        SAMPLE_C2_BASE = sample_c2_base;
    }

    public boolean isSAMPLE_C2_SIDE() {
        return SAMPLE_C2_SIDE;
    }

    public void setSAMPLE_C2_SIDE(boolean sample_c2_side) {
        SAMPLE_C2_SIDE = sample_c2_side;
    }

    public boolean isSAMPLE_C2_TOROIDAL() {
        return SAMPLE_C2_TOROIDAL;
    }

    public void setSAMPLE_C2_TOROIDAL(boolean sample_c2_toroidal) {
        SAMPLE_C2_TOROIDAL = sample_c2_toroidal;
    }

    public boolean isSAMPLE_C1_TOROIDAL_HS() {
        return SAMPLE_C1_TOROIDAL_HS;
    }

    public void setSAMPLE_C1_TOROIDAL_HS(boolean sample_c1_toroidal_hs) {
        SAMPLE_C1_TOROIDAL_HS = sample_c1_toroidal_hs;
    }

    public boolean isSAMPLE_C2_TOROIDAL_HS() {
        return SAMPLE_C2_TOROIDAL_HS;
    }

    public void setSAMPLE_C2_TOROIDAL_HS(boolean sample_c2_toroidal_hs) {
        SAMPLE_C2_TOROIDAL_HS = sample_c2_toroidal_hs;
    }

    public boolean isSAMPLE_HOT_SPOT_1() {
        return SAMPLE_HOT_SPOT_1;
    }

    public void setSAMPLE_HOT_SPOT_1(boolean sample_hot_spot_1) {
        SAMPLE_HOT_SPOT_1 = sample_hot_spot_1;
    }

    public boolean isSAMPLE_HOT_SPOT_2() {
        return SAMPLE_HOT_SPOT_2;
    }

    public void setSAMPLE_HOT_SPOT_2(boolean sample_hot_spot_2) {
        SAMPLE_HOT_SPOT_2 = sample_hot_spot_2;
    }
}
