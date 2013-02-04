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

package soam.utils;

import java.io.*;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;

public class TOFSequenceFilter {
    public static final String PREFIX = "filtered_";

    public static void exponentialSmoothing(File directory,
	    final String prefix, double alpha) throws IOException {

	FilenameFilter filter = new FilenameFilter() {
	    public boolean accept(File tmp, String name) {
		return name.matches(prefix + ".*\\.pgm");
	    }
	};

	File[] imageFiles = directory.listFiles(filter);

	BufferedImage referenceImage = PGMImageIO.read(imageFiles[0]);
	PGMImageIO.write(referenceImage, new File(directory, PREFIX
		+ imageFiles[0].getName()));

	WritableRaster referenceRaster = referenceImage.getRaster();

	for (int i = 1; i < imageFiles.length; i++) {
	    Raster currentRaster = PGMImageIO.read(imageFiles[i]).getRaster();

	    for (int x = 0; x < referenceRaster.getWidth(); x++) {
		for (int y = 0; y < referenceRaster.getHeight(); y++) {
		    double s = referenceRaster.getSampleDouble(x, y, 0);
		    double d = currentRaster.getSampleDouble(x, y, 0);

		    s = alpha * d + (1 - alpha) * s;

		    referenceRaster.setSample(x, y, 0, s);
		}
	    }

	    PGMImageIO.write(referenceImage, new File(directory, PREFIX
		    + imageFiles[i].getName()));
	}
    }

    public static void exponentialSmoothingWithReflectanceThreshold(
	    File directory, final String disPrefix, final String refPrefix,
	    double alpha, double threshold) throws IOException {

	FilenameFilter distanceFilter = new FilenameFilter() {
	    public boolean accept(File tmp, String name) {
		return name.matches(disPrefix + ".*\\.pgm");
	    }
	};

	File[] distanceImageFiles = directory.listFiles(distanceFilter);

	FilenameFilter reflectanceFilter = new FilenameFilter() {
	    public boolean accept(File tmp, String name) {
		return name.matches(refPrefix + ".*\\.pgm");
	    }
	};

	File[] reflectanceImageFiles = directory.listFiles(reflectanceFilter);

	BufferedImage referenceImage = PGMImageIO.read(distanceImageFiles[0]);
	PGMImageIO.write(referenceImage, new File(directory, PREFIX
		+ distanceImageFiles[0].getName()));

	WritableRaster referenceRaster = referenceImage.getRaster();
	// Filter out pixels with reflectance below threshold
	{
	    Raster reflectanceRaster =
		    PGMImageIO.read(reflectanceImageFiles[0]).getRaster();

	    for (int x = 0; x < referenceRaster.getWidth(); x++) {
		for (int y = 0; y < referenceRaster.getHeight(); y++) {
		    double r = reflectanceRaster.getSampleDouble(x, y, 0);

		    if (r < threshold) {
			referenceRaster.setSample(x, y, 0, 0d);
		    }
		}
	    }

	    PGMImageIO.write(referenceImage, new File(directory, PREFIX
		    + distanceImageFiles[0].getName()));
	}

	for (int i = 1; i < distanceImageFiles.length; i++) {
	    Raster distanceRaster =
		    PGMImageIO.read(distanceImageFiles[i]).getRaster();

	    Raster reflectanceRaster =
		    PGMImageIO.read(reflectanceImageFiles[i]).getRaster();

	    for (int x = 0; x < referenceRaster.getWidth(); x++) {
		for (int y = 0; y < referenceRaster.getHeight(); y++) {
		    double s = referenceRaster.getSampleDouble(x, y, 0);
		    double d = distanceRaster.getSampleDouble(x, y, 0);
		    double r = reflectanceRaster.getSampleDouble(x, y, 0);

		    if (r < threshold) {
			s = 0d;
		    } else if (s == 0d) {
			s = (2 - alpha) * d;
		    } else {
			s = alpha * d + (1 - alpha) * s;
		    }

		    referenceRaster.setSample(x, y, 0, s);
		}
	    }

	    PGMImageIO.write(referenceImage, new File(directory, PREFIX
		    + distanceImageFiles[i].getName()));
	}
    }

    public static void main(String[] args) throws IOException {
	System.out.print("Input parameters:");
	for (int i = 0; i < args.length; i++) {
	    System.out.print(" " + args[i]);
	}
	System.out.println();

	// exponentialSmoothing(new File(args[0]), args[1], Double
	// .parseDouble(args[2]));

	exponentialSmoothingWithReflectanceThreshold(new File(args[0]),
		args[1], args[2], Double.parseDouble(args[3]), Double
			.parseDouble(args[4]));

	System.out.println("Sequence successfully filtered.");
    }
}
