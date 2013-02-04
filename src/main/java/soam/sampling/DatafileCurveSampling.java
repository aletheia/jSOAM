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

package soam.sampling;

import java.io.*;
import java.util.StringTokenizer;
import java.util.ArrayList;

public class DatafileCurveSampling {

	public double[] X;
	public double[] Z;
	
	
	public static DatafileCurveSampling createFrom(String xinv, String zinv)
			throws IOException {

		DatafileCurveSampling sample = new DatafileCurveSampling();

		File inputFile;
		FileReader fr;
		BufferedReader in;
		ArrayList<Double> list;

		// Load Xinv file
		inputFile = new File(xinv);

		fr = new FileReader(inputFile);
		in = new BufferedReader(fr);
		list = createFrom(in.readLine());

		sample.X = new double[list.size()];
		for (int i = 0; i < sample.X.length; i++) {
			sample.X[i] = list.get(i).doubleValue();
		}

		in.close();
		fr.close();

		// Load Zinv file
		inputFile = new File(zinv);

		fr = new FileReader(inputFile);
		in = new BufferedReader(fr);
		list = createFrom(in.readLine());

		sample.Z = new double[list.size()];
		for (int i = 0; i < sample.Z.length; i++) {
			sample.Z[i] = list.get(i).doubleValue();
		}

		in.close();
		fr.close();

		return sample;
	}

	public static ArrayList<Double> createFrom(String row) {
        String token;
        ArrayList<Double> list = new ArrayList<Double>(); 
        
        try {
            StringTokenizer strtok = new StringTokenizer(row, ",");

            while(strtok.hasMoreTokens()) {
                token = strtok.nextToken();
                list.add(Double.parseDouble(token));
            }

            return list;
        } 
        catch (Exception e) {
            throw new RuntimeException("Unrecognizable string");
        }
    }

	// For unit testing only
	public static void main(String[] args) throws IOException {
		if (args.length != 2) {
			System.out
					.println("Usage: CurveSampling <xinv-filename> <zinv-filename>");
			System.exit(1);
		}

		DatafileCurveSampling sample = DatafileCurveSampling.createFrom(args[0], args[1]);
		System.out.println(sample);

		System.out.println("Ok");
	}

}