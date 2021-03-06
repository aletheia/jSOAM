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

import java.io.*;
import java.awt.image.*;

public class PGMImageIO {

    public static BufferedImage read(File file) throws IOException {
	InputStream in = new BufferedInputStream(new FileInputStream(file));
	BufferedImage image = read(in);
	in.close();

	return image;
    }

    public static BufferedImage read(InputStream in) throws IOException {
	if (in.read() != 'P' || in.read() != '2')
	    throw new IOException("Magic P2 is missing");
	if (!isWhiteSpace(in.read()))
	    throw new IOException("White-space missing");

	int width = readDecimal(in);
	int height = readDecimal(in);

	// Skip depth
	readDecimal(in);

	short[] pixels = new short[width * height];
	for (int i = 0; i < pixels.length; i++) {
	    pixels[i] = (short) readDecimal(in);
	}

	BufferedImage image =
		new BufferedImage(width, height, BufferedImage.TYPE_USHORT_GRAY);
	short[] imagePixels =
		((DataBufferUShort) image.getRaster().getDataBuffer())
			.getData();

	System.arraycopy(pixels, 0, imagePixels, 0, width * height);
	return image;
    }

    public static void write(BufferedImage image, File file) throws IOException {
	OutputStream out = new BufferedOutputStream(new FileOutputStream(file));
	write(image, out);
	out.close();
    }

    public static void write(BufferedImage image, OutputStream out)
	    throws IOException {

	if (image.getType() != BufferedImage.TYPE_USHORT_GRAY) {
	    throw new IOException(
		    "BufferedImage is not of type TYPE_USHORT_GRAY");
	}

	PrintStream ps = new PrintStream(out, true);

	ps.println("P2");
	ps.println(image.getWidth() + " " + image.getHeight());
	ps.println("65536");

	short[] imagePixels =
		((DataBufferUShort) image.getRaster().getDataBuffer())
			.getData();

	for (int i = 0; i < imagePixels.length; i++) {
	    ps.println(uShortToInt(imagePixels[i]));
	}
    }

    /**
     * Returns 'true' if 'c' is a space, tab, new-line or carriage-return.
     */
    private static boolean isWhiteSpace(int c) {
	return c == ' ' || c == '\t' || c == '\n' || c == '\r';
    }

    /**
     * Skips zero or more white-space characters, and comments, then reads one
     * or more digits, then one white-space character.
     */
    private static int readDecimal(InputStream in) throws IOException {
	int c = in.read();

	while (isWhiteSpace(c))
	    c = in.read();

	while (c == '#') {
	    while (c != '\n')
		c = in.read();
	    while (isWhiteSpace(c))
		c = in.read();
	}

	if (c < '0' || c > '9') {
	    throw new IOException("Number missing");
	}

	int decimal = 0;

	while (c >= '0' && c <= '9') {
	    decimal = 10 * decimal + (c - '0');
	    c = in.read();
	}

	if (!isWhiteSpace(c)) {
	    throw new IOException("White-space missing");
	}

	return decimal;
    }

    private static int uShortToInt(short x) {
	return (x & 0xffff);
    }

    public static void main(String[] args) throws IOException {
	BufferedImage image = read(new File(args[0]));

	System.out.println("Image " + image + " successfully read.");

	write(image, new File(args[1]));

	System.out.println("Image " + args[1] + " successfully written.");
    }
}
