

package soam.sampling;

import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.Random;
import java.util.Vector;
import java.util.Hashtable;
import java.text.ParseException;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import soam.algorithms.AlgebraicOperatorsNd;
import soam.algorithms.Algorithm;
import soam.utils.FortranFormat;

public class MolecularSurfaceSampler implements Sampler {

    static FortranFormat formatter;
    static Hashtable<String, Double> atomicRadiusTable;

    static {

	// Set up formatter and set to automatically add carriage returns to the
	// end of lines when formatting
	try {
	    formatter =
		    new FortranFormat(
			    "(A6,I5,1X,A4,A1,A3,1X,A1,I4,A1,3X,3F8.3,2F6.2,10X,2A2)");
	} catch (ParseException e) {
	    e.printStackTrace();
	}

	formatter.setAddReturn(true);

	// Setup the Hashtable for atomic radii
	initalizeAtomicRadiusTable();
    }

    protected static String defaultPath = System.getProperty("user.dir");

    protected File file;

    protected Random random;

    protected Vector<Atom> atoms;

    // "Water radius" as default
    protected double probeRadius = 1.4d;

    protected double minX;
    protected double minY;
    protected double minZ;

    protected double maxX;
    protected double maxY;
    protected double maxZ;

    public boolean initialize(Algorithm algorithm) {

	random = new Random();

	while (true) {
	    JFileChooser fc = new JFileChooser(defaultPath);
	    fc.addChoosableFileFilter(new FileNameExtensionFilter("PDB Files",
		    "pdb"));
	    fc.setDialogTitle(this.getClass().getName() + " : PDB file");

	    if (fc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
		file = fc.getSelectedFile();
		defaultPath = file.getAbsolutePath();

		try {
		    atoms = parsePDB(new BufferedReader(new FileReader(file)));

		} catch (Exception e) {
		    JOptionPane.showMessageDialog(null,
			    "Could not load PDB file \"" + file.getPath()
				    + "\"", "Error", JOptionPane.ERROR_MESSAGE);
		    continue;
		}

		break;

	    } else {
		return false;
	    }
	}

	findNeighbors();
	constructSimplicialComplex();
	computeTrilaterations();

	return true;
    }

    public void reset() {
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
     * Function to read PDB files, returns a Vector of Atoms, requires a
     * BufferedReader wrapping some input source (maybe a FileReader or an
     * InputStreamReader)
     * 
     * @see "Adapted from: ICLkevin, <a href="
     *      http://www.ichemlabs.com/content/reading
     *      -and-writing-pdb-and-smd-files-exercise-fortranformat" > Reading and
     *      Writing PDB and SMD Files: An Exercise with FortranFormat</a>,
     *      <i>iChemLabs</i>"
     * 
     * @param br
     *            A buffered reader on a PDB file
     * @return A vector of atoms in the PDB file
     * @throws IOException
     */
    protected Vector<Atom> parsePDB(BufferedReader br) throws IOException {
	// Vector of Atoms to be returned
	Vector<Atom> atoms = new Vector<Atom>();
	String line = null;

	// read lines from the BufferedReader
	while ((line = br.readLine()) != null) {

	    // check if the line is an atom record
	    if (line.startsWith("ATOM") || line.startsWith("HETATM")) {

		// use FortranFormat to parse the line, avoiding the tedious
		// substrings and casts!
		Vector<Object> objects = formatter.parse(line);

		// create the Atom object from the objects in the Vector, they
		// are in an order identical to the Fortran Format specification
		// string
		Atom a = new Atom();

		a.index = atoms.size();

		// set up the Atom object

		// notice that Java 1.5 Autounboxing allows us to quickly assign
		// the object values to the primitive values
		a.position = new double[3];

		a.position[0] = (Double) objects.get(8);
		a.position[1] = (Double) objects.get(9);
		a.position[2] = (Double) objects.get(10);

		a.symbol = (String) objects.get(13);

		// Added radius reading (pmarco@unipv.it)
		a.radius = atomicRadiusTable.get(a.symbol);

		atoms.add(a);
	    }
	}

	return atoms;
    }

    protected void findNeighbors() {
	minX = Float.MAX_VALUE;
	minY = Float.MAX_VALUE;
	minZ = Float.MAX_VALUE;

	maxX = -Float.MAX_VALUE;
	maxY = -Float.MAX_VALUE;
	maxZ = -Float.MAX_VALUE;

	for (int i = 0; i < atoms.size(); i++) {
	    Atom thisAtom = atoms.get(i);

	    // In passing, compute the bounding box
	    if (minX > thisAtom.position[0] - thisAtom.radius) {
		minX = thisAtom.position[0] - thisAtom.radius;
	    }
	    if (minY > thisAtom.position[1] - thisAtom.radius) {
		minY = thisAtom.position[1] - thisAtom.radius;
	    }
	    if (minZ > thisAtom.position[2] - thisAtom.radius) {
		minZ = thisAtom.position[2] - thisAtom.radius;
	    }

	    if (maxX < thisAtom.position[0] + thisAtom.radius) {
		maxX = thisAtom.position[0] + thisAtom.radius;
	    }
	    if (maxY < thisAtom.position[1] + thisAtom.radius) {
		maxY = thisAtom.position[1] + thisAtom.radius;
	    }
	    if (maxZ < thisAtom.position[2] + thisAtom.radius) {
		maxZ = thisAtom.position[2] + thisAtom.radius;
	    }

	    for (int j = i + 1; j < atoms.size(); j++) {
		Atom otherAtom = atoms.get(j);

		if (AlgebraicOperatorsNd.norm(thisAtom.position,
			otherAtom.position) <= thisAtom.radius + 2
			* probeRadius + otherAtom.radius) {
		    thisAtom.neighbors.add(otherAtom);
		    otherAtom.neighbors.add(thisAtom);
		}
	    }
	}
    }

    protected void constructSimplicialComplex() {
	for (int i = 0; i < atoms.size(); i++) {
	    Atom thisAtom = atoms.get(i);

	    for (int j = 0; j < thisAtom.neighbors.size(); j++) {
		Atom neighborAtom = thisAtom.neighbors.get(j);
		if (thisAtom.index <= neighborAtom.index) {

		    boolean noFaces = true;
		    for (int k = j + 1; k < thisAtom.neighbors.size(); k++) {
			Atom anotherNeighborAtom = thisAtom.neighbors.get(k);
			if (thisAtom.index <= anotherNeighborAtom.index
				&& neighborAtom.neighbors
					.contains(anotherNeighborAtom)) {
			    Face face =
				    new Face(thisAtom, neighborAtom,
					    anotherNeighborAtom);
			    thisAtom.faces.add(face);
			    neighborAtom.faces.add(face);
			    anotherNeighborAtom.faces.add(face);

			    noFaces = false;
			}
		    }

		    if (noFaces) {
			Edge edge = new Edge(thisAtom, neighborAtom);
			thisAtom.edges.add(edge);
			neighborAtom.edges.add(edge);
		    }
		}
	    }
	}
    }

    protected void computeTrilaterations() {
	for (int i = 0; i < atoms.size(); i++) {
	    Atom thisAtom = atoms.get(i);

	    boolean allBuried = true;
	    for (int j = 0; j < thisAtom.faces.size(); j++) {
		Face face = thisAtom.faces.get(j);

		if (face.one.index >= thisAtom.index
			&& face.two.index >= thisAtom.index
			&& face.three.index >= thisAtom.index) {

		    computeTrilaterations(face);
		    checkBuriedVertices(face);

		    if (!face.isBuried()) {
			allBuried = false;
		    }
		}
	    }

	    if (thisAtom.edges.size() > 0 || thisAtom.faces.size() == 0) {
		continue;
	    } else if (allBuried) {
		thisAtom.isBuried = true;
	    }
	}
    }

    /**
     * Compute the two 3D trilaterations of a face
     * 
     * @see "<a href="
     *      http://en.wikipedia.org/wiki/Trilateration">Trilateration</a>,
     *      <i>Wikipedia</i> for an explanation of the impossible naming of
     *      variables (sorry)."
     */
    protected void computeTrilaterations(Face face) {
	double r1sq = face.one.radius + probeRadius;
	double r2sq = face.two.radius + probeRadius;
	double r3sq = face.three.radius + probeRadius;

	r1sq *= r1sq;
	r2sq *= r2sq;
	r3sq *= r3sq;

	double d =
		AlgebraicOperatorsNd.norm(face.two.position, face.one.position);

	// i is the projection of (three - one) on (two - one)
	double i =
		AlgebraicOperatorsNd.scalarProduct(face.three.position,
			face.two.position, face.one.position);
	i /= d;

	// j = | (three - one) - i * ((two - one) / | (two - one) |) |
	double j = 0;
	for (int k = 0; k < 3; k++) {
	    double tmp =
		    (face.three.position[k] - face.one.position[k]) - (i / d)
			    * (face.two.position[k] - face.one.position[k]);
	    j += tmp * tmp;
	}
	j = Math.sqrt(j);

	double x = (r1sq - r2sq + d * d) / (2 * d);
	double y = (r1sq - r3sq + i * i + j * j) / (2 * j) - (i * x) / j;

	double z = r1sq - x * x - y * y;

	if (z < 0) {
	    throw new RuntimeException("Trilateration failed (z = " + z + ")");
	}

	z = Math.sqrt(z);

	face.firstVertex.position[0] = face.one.position[0] + x;
	face.firstVertex.position[1] = face.one.position[1] + y;
	face.firstVertex.position[2] = face.one.position[2] + z;

	face.secondVertex.position[0] = face.one.position[0] + x;
	face.secondVertex.position[1] = face.one.position[1] + y;
	face.secondVertex.position[2] = face.one.position[2] - z;
    }

    protected void checkBuriedVertices(Face face) {
	for (int i = 0; i < atoms.size(); i++) {
	    Atom thisAtom = atoms.get(i);

	    if (thisAtom == face.one || thisAtom == face.two
		    || thisAtom == face.three) {
		continue;
	    }

	    if (!face.firstVertex.isBuried) {
		double d1 =
			AlgebraicOperatorsNd.norm(face.firstVertex.position,
				thisAtom.position);
		if (d1 < thisAtom.radius + probeRadius) {
		    face.firstVertex.isBuried = true;
		}
	    }

	    if (!face.secondVertex.isBuried) {
		double d2 =
			AlgebraicOperatorsNd.norm(face.secondVertex.position,
				thisAtom.position);
		if (d2 < thisAtom.radius + probeRadius) {
		    face.secondVertex.isBuried = true;
		}
	    }

	    if (face.firstVertex.isBuried && face.secondVertex.isBuried) {
		break;
	    }
	}
    }

    public double[] nextSample() {
	double[] sample = new double[] { 0d, 0d, 0d };

	// Just for now
	return sample;
    }

    public double[] nextSample(double[] point, double radius) {

	// Just for now
	return nextSample();
    }

    /**
     * Default table of van der Waals Atomic Radii.</br> Values taken from
     * OpenBabel.
     * 
     * @see <a
     *      href="http://openbabel.sourceforge.net">openbabel.sourceforge.net</a>
     * @see <a
     *      href="http://jmol.svn.sourceforge.net/viewvc/jmol/trunk/Jmol/src/org/jmol/_documents/vdw_comparison.xls">vdw_comparison.xls</a>
     */
    private static void initalizeAtomicRadiusTable() {
	atomicRadiusTable = new Hashtable<String, Double>();

	atomicRadiusTable.put("H", 1.2d);
	atomicRadiusTable.put("He", 1.4d);
	atomicRadiusTable.put("Li", 1.82d);
	atomicRadiusTable.put("Be", 1.7d);
	atomicRadiusTable.put("B", 2.08d);
	atomicRadiusTable.put("C", 1.95d);
	atomicRadiusTable.put("N", 1.85d);
	atomicRadiusTable.put("O", 1.7d);
	atomicRadiusTable.put("F", 1.73d);
	atomicRadiusTable.put("Ne", 1.54d);
	atomicRadiusTable.put("Na", 2.27d);
	atomicRadiusTable.put("Mg", 1.73d);
	atomicRadiusTable.put("Al", 2.05d);
	atomicRadiusTable.put("Si", 2.1d);
	atomicRadiusTable.put("P", 2.08d);
	atomicRadiusTable.put("S", 2d);
	atomicRadiusTable.put("Cl", 1.97d);
	atomicRadiusTable.put("Ar", 1.88d);
	atomicRadiusTable.put("K", 2.75d);
	atomicRadiusTable.put("Ca", 1.973d);
	atomicRadiusTable.put("Sc", 1.7d);
	atomicRadiusTable.put("Ti", 1.7d);
	atomicRadiusTable.put("V", 1.7d);
	atomicRadiusTable.put("Cr", 1.7d);
	atomicRadiusTable.put("Mn", 1.7d);
	atomicRadiusTable.put("Fe", 1.7d);
	atomicRadiusTable.put("Co", 1.7d);
	atomicRadiusTable.put("Ni", 1.63d);
	atomicRadiusTable.put("Cu", 1.4d);
	atomicRadiusTable.put("Zn", 1.39d);
	atomicRadiusTable.put("Ga", 1.87d);
	atomicRadiusTable.put("Ge", 1.7d);
	atomicRadiusTable.put("As", 1.85d);
	atomicRadiusTable.put("Se", 1.9d);
	atomicRadiusTable.put("Br", 2.1d);
	atomicRadiusTable.put("Kr", 2.02d);
	atomicRadiusTable.put("Rb", 1.7d);
	atomicRadiusTable.put("Sr", 1.7d);
	atomicRadiusTable.put("Y", 1.7d);
	atomicRadiusTable.put("Zr", 1.7d);
	atomicRadiusTable.put("Nb", 1.7d);
	atomicRadiusTable.put("Mo", 1.7d);
	atomicRadiusTable.put("Tc", 1.7d);
	atomicRadiusTable.put("Ru", 1.7d);
	atomicRadiusTable.put("Rh", 1.7d);
	atomicRadiusTable.put("Pd", 1.63d);
	atomicRadiusTable.put("Ag", 1.72d);
	atomicRadiusTable.put("Cd", 1.58d);
	atomicRadiusTable.put("In", 1.93d);
	atomicRadiusTable.put("Sn", 2.17d);
	atomicRadiusTable.put("Sb", 2.2d);
	atomicRadiusTable.put("Te", 2.06d);
	atomicRadiusTable.put("I", 2.15d);
	atomicRadiusTable.put("Xe", 2.16d);
	atomicRadiusTable.put("Cs", 1.7d);
	atomicRadiusTable.put("Ba", 1.7d);
	atomicRadiusTable.put("La", 1.7d);
	atomicRadiusTable.put("Ce", 1.7d);
	atomicRadiusTable.put("Pr", 1.7d);
	atomicRadiusTable.put("Nd", 1.7d);
	atomicRadiusTable.put("Pm", 1.7d);
	atomicRadiusTable.put("Sm", 1.7d);
	atomicRadiusTable.put("Eu", 1.7d);
	atomicRadiusTable.put("Gd", 1.7d);
	atomicRadiusTable.put("Tb", 1.7d);
	atomicRadiusTable.put("Dy", 1.7d);
	atomicRadiusTable.put("Ho", 1.7d);
	atomicRadiusTable.put("Er", 1.7d);
	atomicRadiusTable.put("Tm", 1.7d);
	atomicRadiusTable.put("Yb", 1.7d);
	atomicRadiusTable.put("Lu", 1.7d);
	atomicRadiusTable.put("Hf", 1.7d);
	atomicRadiusTable.put("Ta", 1.7d);
	atomicRadiusTable.put("W", 1.7d);
	atomicRadiusTable.put("Re", 1.7d);
	atomicRadiusTable.put("Os", 1.7d);
	atomicRadiusTable.put("Ir", 1.7d);
	atomicRadiusTable.put("Pt", 1.72d);
	atomicRadiusTable.put("Au", 1.66d);
	atomicRadiusTable.put("Hg", 1.55d);
	atomicRadiusTable.put("Tl", 1.96d);
	atomicRadiusTable.put("Pb", 2.02d);
	atomicRadiusTable.put("Bi", 1.7d);
	atomicRadiusTable.put("Po", 1.7d);
	atomicRadiusTable.put("At", 1.7d);
	atomicRadiusTable.put("Rn", 1.7d);
	atomicRadiusTable.put("Fr", 1.7d);
	atomicRadiusTable.put("Ra", 1.7d);
	atomicRadiusTable.put("Ac", 1.7d);
	atomicRadiusTable.put("Th", 1.7d);
	atomicRadiusTable.put("Pa", 1.7d);
	atomicRadiusTable.put("U", 1.86d);
	atomicRadiusTable.put("Np", 1.7d);
	atomicRadiusTable.put("Pu", 1.7d);
	atomicRadiusTable.put("Am", 1.7d);
	atomicRadiusTable.put("Cm", 1.7d);
	atomicRadiusTable.put("Bk", 1.7d);
	atomicRadiusTable.put("Cf", 1.7d);
	atomicRadiusTable.put("Es", 1.7d);
	atomicRadiusTable.put("Fm", 1.7d);
	atomicRadiusTable.put("Md", 1.7d);
	atomicRadiusTable.put("No", 1.7d);
	atomicRadiusTable.put("Lr", 1.7d);
	atomicRadiusTable.put("Rf", 1.7d);
	atomicRadiusTable.put("Db", 1.7d);
	atomicRadiusTable.put("Sg", 1.7d);
	atomicRadiusTable.put("Bh", 1.7d);
	atomicRadiusTable.put("Hs", 1.7d);
	atomicRadiusTable.put("Mt", 1.7d);
    }

    /**
     * @see #parsePDB(BufferedReader br)
     */
    class Atom {
	int index;
	double[] position = new double[3];
	String symbol;
	double radius;
	boolean isBuried = false;

	ArrayList<Atom> neighbors = new ArrayList<Atom>();
	ArrayList<Edge> edges = new ArrayList<Edge>();
	ArrayList<Face> faces = new ArrayList<Face>();;
    }

    class Edge {
	Atom one;
	Atom two;

	boolean selfIntersecting = false;

	Edge(Atom one, Atom two) {
	    this.one = one;
	    this.two = two;
	}
    }

    class Face {
	Atom one;
	Atom two;
	Atom three;

	Vertex firstVertex = new Vertex();
	Vertex secondVertex  = new Vertex();

	boolean selfIntersecting = false;

	Face(Atom one, Atom two, Atom three) {
	    this.one = one;
	    this.two = two;
	    this.three = three;
	}

	boolean isBuried() {
	    return firstVertex.isBuried && secondVertex.isBuried;
	}
    }

    class Vertex {
	double[] position = new double[3];
	boolean isBuried = false;
    }

    public String toString() {
	return this.getClass().getSimpleName() + "(\"" + file.getPath() + "\")";
    }

}
