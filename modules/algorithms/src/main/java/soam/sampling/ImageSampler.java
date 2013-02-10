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

import java.io.File;

import java.util.ArrayList;
import java.util.Random;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.imageio.ImageIO;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

import soam.algorithms.Algorithm;

public class ImageSampler implements Sampler {

    protected static String defaultPath = System.getProperty("user.dir");

    protected double SCALE_FACTOR = 1d;
    // For debugging
    protected Point NEXT_SAMPLE = null;

    protected double NOISE_STDEV = 0d;
    protected double NOISE_BOUND = 0d;

    protected BufferedImage bufferedImage;
    protected Raster raster;

    protected File file;

    protected int numBands;
    protected int dimension;

    protected ArrayList<double[]> pointCloud;

    protected Random random;

    public boolean initialize(Algorithm algorithm) {

	random = new Random();

	while (true) {
	    JFileChooser fc = new JFileChooser(defaultPath);
	    fc.addChoosableFileFilter(new FileNameExtensionFilter(
		    "Image Files", "bmp", "gif", "jpg", "jpeg", "png", "tif",
		    "tiff"));
	    fc.setAccessory(new ImagePreview(fc));
	    fc.setDialogTitle(this.getClass().getName() + " : Image file");

	    if (fc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
		file = fc.getSelectedFile();
		defaultPath = file.getAbsolutePath();

		try {
		    bufferedImage = ImageIO.read(file);

		    raster = bufferedImage.getData();
		    numBands = raster.getNumBands();
		    dimension = numBands + 2;

		} catch (Exception e) {
		    JOptionPane.showMessageDialog(null,
			    "Could not load image \"" + file.getPath() + "\"",
			    "Error", JOptionPane.ERROR_MESSAGE);
		    continue;
		}

		return true;

	    } else {
		return false;
	    }
	}
    }

    public void reset() {
	return;
    }

    /**
     * Gets the description string
     * 
     * @return dimension
     */
    public String getDescription() {
	return "";
    }

    /**
     * Gets the complete dimension of the sample space
     * 
     * @return dimension
     */
    public int getDimensions() {
	return dimension;
    }

    /**
     * Gets the image as BufferedImage
     * 
     * @return BufferedImage
     */
    public BufferedImage getBufferedImage() {
	return bufferedImage;
    }

    /**
     * Gets the point cloud, as an array of vectors
     * 
     * @return dimension
     */
    public ArrayList<double[]> getPointCloud() {

	if (pointCloud == null) {
	    pointCloud = new ArrayList<double[]>();

	    for (int i = 0; i < raster.getWidth(); i++) {
		for (int j = 0; j < raster.getHeight(); j++) {
		    double[] point = new double[2 + numBands];

		    point[0] = i;
		    point[1] = j;

		    for (int b = 0; b < numBands; b++) {
			point[2 + b] =
				raster.getSampleDouble((int) point[0],
					(int) point[1], b);

			point[2 + b] *= SCALE_FACTOR;
		    }

		    pointCloud.add(point);
		}
	    }
	}

	return pointCloud;
    }

    /**
     * Gets the image as Raster
     * 
     * @return Raster
     */
    public Raster getRaster() {
	return raster;
    }

    /**
     * Gets a sample from the image
     * 
     * @return A sampled point
     */
    public double[] nextSample() {
	double[] sample = new double[2 + numBands];

	// Uniform sampling over image space
	if (NEXT_SAMPLE == null) {
	    sample[0] = random.nextDouble() * (raster.getWidth() - 1);
	    sample[1] = random.nextDouble() * (raster.getHeight() - 1);
	} else {
	    // Enforce image boundaries
	    sample[0] =
		    Math.min(Math.max(NEXT_SAMPLE.x, 0d), (double) raster
			    .getWidth() - 1);
	    sample[1] =
		    Math.min(Math.max(NEXT_SAMPLE.y, 0d), (double) raster
			    .getHeight() - 1);
	    NEXT_SAMPLE = null;
	}

	for (int i = 0; i < numBands; i++) {
	    sample[2 + i] =
		    raster.getSampleDouble((int) sample[0], (int) sample[1], i);

	    sample[2 + i] *= SCALE_FACTOR;
	}

	return sample;
    }

    /**
     * Gets a localized sample from the image
     * 
     * @return A sampled point
     */
    public double[] nextSample(final double[] point, double radius) {

	if (NEXT_SAMPLE != null) {
	    return nextSample();
	}

	double[] sample = new double[2 + numBands];

	// Select a point in the neighborhood of the point
	double angle = random.nextDouble() * 2 * Math.PI;
	double distance = random.nextDouble() * radius;
	sample[0] = point[0] + (distance * Math.cos(angle));
	sample[1] = point[1] + (distance * Math.sin(angle));

	// Enforce image boundaries
	sample[0] =
		Math.min(Math.max(sample[0], 0d),
			(double) raster.getWidth() - 1);
	sample[1] =
		Math.min(Math.max(sample[1], 0d),
			(double) raster.getHeight() - 1);

	for (int i = 0; i < numBands; i++) {
	    sample[2 + i] =
		    raster.getSampleDouble((int) sample[0], (int) sample[1], i);

	    sample[2 + i] *= SCALE_FACTOR;
	}

	return sample;
    }

    public String toString() {
	return this.getClass().getSimpleName() + "(\"" + file.getPath() + "\")";
    }

    protected class ImagePreview extends JComponent implements
	    PropertyChangeListener {
	private static final long serialVersionUID = 1L;

	ImageIcon thumbnail = null;
	File file = null;

	public ImagePreview(JFileChooser fc) {
	    setPreferredSize(new Dimension(100, 50));
	    fc.addPropertyChangeListener(this);
	}

	public void loadImage() {
	    if (file == null) {
		thumbnail = null;
		return;
	    }

	    ImageIcon tmpIcon = new ImageIcon(file.getPath());
	    if (tmpIcon != null) {
		if (tmpIcon.getIconWidth() > 90) {
		    thumbnail =
			    new ImageIcon(tmpIcon.getImage().getScaledInstance(
				    90, -1, Image.SCALE_DEFAULT));
		} else {
		    thumbnail = tmpIcon;
		}
	    }
	}

	public void propertyChange(PropertyChangeEvent e) {
	    boolean update = false;
	    String prop = e.getPropertyName();

	    if (JFileChooser.DIRECTORY_CHANGED_PROPERTY.equals(prop)) {
		file = null;
		update = true;

	    } else if (JFileChooser.SELECTED_FILE_CHANGED_PROPERTY.equals(prop)) {
		file = (File) e.getNewValue();
		update = true;
	    }

	    if (update) {
		thumbnail = null;
		if (isShowing()) {
		    loadImage();
		    repaint();
		}
	    }
	}

	protected void paintComponent(Graphics g) {
	    if (thumbnail == null) {
		loadImage();
	    }
	    if (thumbnail != null) {
		int x = getWidth() / 2 - thumbnail.getIconWidth() / 2;
		int y = getHeight() / 2 - thumbnail.getIconHeight() / 2;

		if (y < 0) {
		    y = 0;
		}

		if (x < 5) {
		    x = 5;
		}
		thumbnail.paintIcon(this, g, x, y);
	    }
	}
    }

    public Point getNEXT_SAMPLE() {
	return NEXT_SAMPLE;
    }

    public void setNEXT_SAMPLE(Point next_sample) {
	NEXT_SAMPLE = next_sample;
    }

    public double getSCALE_FACTOR() {
	return SCALE_FACTOR;
    }

    public void setSCALE_FACTOR(double scale_factor) {
	SCALE_FACTOR = scale_factor;
	
	pointCloud = null;
    }

    public double getNOISE_STDEV() {
	return NOISE_STDEV;
    }

    public void setNOISE_STDEV(double noise_stdev) {
	NOISE_STDEV = noise_stdev;
    }

    public double getNOISE_BOUND() {
	return NOISE_BOUND;
    }

    public void setNOISE_BOUND(double noise_bound) {
	NOISE_BOUND = noise_bound;
    }

}