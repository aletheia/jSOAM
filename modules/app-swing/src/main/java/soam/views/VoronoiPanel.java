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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.filechooser.FileNameExtensionFilter;

import soam.Vertex;
import soam.algorithms.GWRConstants;
import soam.algorithms.Algorithm;
import soam.utils.GraphicsFileExport;

/**
 * The JPanel for the Voronoi Tessellation of the manifold.
 * 
 * @author Niccolo' Piarulli, Marco Piastra
 * 
 */
public class VoronoiPanel extends JPanel implements GWRConstants {
    private static final long serialVersionUID = 1L;

    protected static String defaultPath = System.getProperty("user.dir");

    protected int MAX_ORDER = 2;
    protected boolean SHOW_INSERTION_THRESHOLD = false;
    protected boolean SHOW_D_BALL = false;

    Color col1, col2, col3;

    int N, taka, haba;

    int k, i, j, l, n, nm;

    double di2, di, cp2, cpx, ys, t;

    double x0, y0, xx, yy, xa1 = 0, ya1 = 0, yy2;

    double di4, di3, cp3, cpx3, ys3, t2, ds, us;

    double y20, y21, sa0, sa1;

    int br, br2, u, k2;

    int xz, xz2, yz, yz2;

    String NS, habaS, takaS;

    double Nd, takad, habad;

    Color cl[] = new Color[13];

    /**
     * The gwrd model
     */
    protected Algorithm algorithm;

    public VoronoiPanel(Algorithm algorithm, int width, int height) {
	this.algorithm = algorithm;

	col1 = Color.lightGray;
	col2 = Color.black;
	col3 = Color.yellow;
	cl[0] = Color.gray;
	cl[1] = Color.red;
	cl[2] = Color.blue;
	habad = 250;
	takad = 250;

	haba = (int) habad;
	taka = (int) takad;

	setPreferredSize(new Dimension(width, height));
    }

    public double dou(String dous) {
	double dou1;
	dou1 = (Double.valueOf(dous)).doubleValue();
	return dou1;
    }

    public double rand() {
	double rand1;
	rand1 = Math.random();
	return rand1;
    }

    public double jou(double a, double b) {
	double jou1;
	jou1 = Math.pow(a, b);
	return jou1;
    }

    void heapv(double te1[], double te2[], double te3[], int NN) {
	int kk, kks, ii, jj, mm;
	double b1, b2, b3, c1, c2, c3;
	kks = (int) (NN / 2);

	for (kk = kks; kk >= 1; kk--) {
	    ii = kk;
	    b1 = te1[ii - 1];
	    b2 = te2[ii - 1];
	    b3 = te3[ii - 1];
	    while (2 * ii <= NN) {
		jj = 2 * ii;
		if (jj + 1 <= NN) {
		    if (te1[jj - 1] < te1[jj]) {
			jj++;
		    }
		}
		if (te1[jj - 1] <= b1) {
		    break;
		}
		te1[ii - 1] = te1[jj - 1];
		te2[ii - 1] = te2[jj - 1];
		te3[ii - 1] = te3[jj - 1];
		ii = jj;
	    }// wend
	    te1[ii - 1] = b1;
	    te2[ii - 1] = b2;
	    te3[ii - 1] = b3;
	}// next kk

	for (mm = NN - 1; mm >= 1; mm--) {
	    c1 = te1[mm];
	    c2 = te2[mm];
	    c3 = te3[mm];
	    te1[mm] = te1[0];
	    te2[mm] = te2[0];
	    te3[mm] = te3[0];
	    ii = 1;
	    while (2 * ii <= mm) {
		kk = 2 * ii;
		if (kk + 1 <= mm) {
		    if (te1[kk - 1] <= te1[kk]) {
			kk++;
		    }
		}
		if (te1[kk - 1] <= c1) {
		    break;
		}
		te1[ii - 1] = te1[kk - 1];
		te2[ii - 1] = te2[kk - 1];
		te3[ii - 1] = te3[kk - 1];
		ii = kk;
	    }// wend
	    te1[ii - 1] = c1;
	    te2[ii - 1] = c2;
	    te3[ii - 1] = c3;
	}// next mm
    }

    @Override
    public void paint(Graphics g) {

	Vertex vertex;
	double[] position;
	int ics, ips;

	N = algorithm.mesh().vertexlist.size();

	Point[] p = new Point[N];
	for (int i = 0; i < N; i++) {
	    vertex = algorithm.mesh().vertexlist.get(i);
	    position = vertex.position;
	    ics = (int) position[0];
	    ips = (int) position[1];
	    p[i] = new Point(ics, ips);
	}

	double kx[] = new double[N];
	double ky[] = new double[N];
	double kz[] = new double[N];

	double x1[] = new double[N];
	double y1[] = new double[N];

	int x[] = new int[N];
	int y[] = new int[N];

	double s[] = new double[N];

	g.setColor(col1);
	g.fillRect(0, 0, getWidth(), getHeight());

	for (int k = 0; k < N; k++) {
	    x1[k] = p[k].x + Math.cos(k * 1.1) + k * 0.0001;
	    y1[k] = p[k].y + Math.sin(k * 1.1) + k * 0.001;
	    x[k] = (int) (x1[k]);
	    y[k] = (int) (y1[k]);
	    s[k] = jou(x1[k] * x1[k] + y1[k] * y1[k], 0.5);

	    g.setColor(col2);
	    g.fillOval(x[k] - 2, y[k] - 2, 4, 4);

	    if (SHOW_INSERTION_THRESHOLD || SHOW_D_BALL) {

		int radius =
			(int) algorithm.mesh().vertexlist.get(k).insertionThreshold;

		if (SHOW_INSERTION_THRESHOLD) {
		    g.setColor(Color.magenta);
		    g.drawOval(x[k] - radius, y[k] - radius, 2 * radius,
			    2 * radius);
		}
		if (SHOW_D_BALL) {
		    g.setColor(Color.cyan);
		    g.drawOval(x[k] - 2 * radius, y[k] - 2 * radius,
			    4 * radius, 4 * radius);
		}
	    }
	}

	heapv(s, x1, y1, N);
	if (N > 1) {
	    for (i = 1; i <= N - 1; i++) {
		for (j = i + 1; j <= N; j++) {
		    di2 = (y1[i - 1] - y1[j - 1]) / (x1[i - 1] - x1[j - 1]);
		    di = -1 / di2;
		    cp2 = (y1[i - 1] + y1[j - 1]) / 2;
		    cpx = (x1[i - 1] + x1[j - 1]) / 2;
		    ys = cp2 - cpx * di;
		    t =
			    jou(x1[i - 1] - x1[j - 1], 2)
				    + jou(y1[i - 1] - y1[j - 1], 2);
		    if (ys > 0 && ys < taka) {
			x0 = 0;
			y0 = ys;
		    } else {
			if (di > 0) {
			    x0 = -ys / di;
			    y0 = 0;
			} else {
			    x0 = (taka - ys) / di;
			    y0 = taka;
			}
		    }
		    yy = di * haba + ys;
		    if (yy > 0 && yy < taka) {
			xa1 = haba;
			ya1 = yy;
		    } else {
			if (di > 0) {
			    xa1 = (taka - ys) / di;
			    ya1 = taka;
			} else {
			    xa1 = -ys / di;
			    ya1 = 0;
			}
		    }
		    l = 1;
		    kx[l - 1] = x0;
		    ky[l - 1] = y0;
		    for (k = 1; k <= N; k++) {
			if (k != i && k != j) {
			    di4 =
				    (y1[i - 1] - y1[k - 1])
					    / (x1[i - 1] - x1[k - 1]);
			    di3 = -1 / di4;
			    cp3 = (y1[i - 1] + y1[k - 1]) / 2;
			    cpx3 = (x1[i - 1] + x1[k - 1]) / 2;
			    ys3 = cp3 - cpx3 * di3;
			    t2 =
				    jou(x1[i - 1] - x1[k - 1], 2)
					    + jou(y1[i - 1] - y1[k - 1], 2);
			    y20 = di3 * x0 + ys3;
			    y21 = di3 * xa1 + ys3;
			    sa0 = y0 - y20;
			    sa1 = ya1 - y21;
			    if (sa0 * sa1 < 0) {
				l++;
				kx[l - 1] = (ys3 - ys) / (di - di3);
				ky[l - 1] = di * kx[l - 1] + ys;
			    }// if sa0*sa1<0
			}// if(k!=i && k!=j)
		    }// next k
		    l++;
		    kx[l - 1] = xa1;
		    ky[l - 1] = ya1;
		    for (u = 1; u <= l; u++) {
			kz[u - 1] = 0;
		    }
		    heapv(kx, ky, kz, l);
		    for (k = 1; k <= l - 1; k++) {
			k2 = k + 1;
			xx = (kx[k - 1] + kx[k2 - 1]) / 2;
			yy2 = di * xx + ys;
			ds = jou(xx - x1[i - 1], 2) + jou(yy2 - y1[i - 1], 2);
			br2 = 0;
			for (u = 1; u <= N; u++) {
			    if (u != i && u != j) {
				us =
					jou(xx - x1[u - 1], 2)
						+ jou(yy2 - y1[u - 1], 2);
				if (us < ds) {
				    br2 = br2 + 1;
				}
			    }
			}// next u
			if (br2 < MAX_ORDER) {
			    xz = (int) (kx[k - 1] + 0.5);
			    xz2 = (int) (kx[k2 - 1] + 0.5);
			    yz = (int) (ky[k - 1] + 0.5);
			    yz2 = (int) (ky[k2 - 1] + 0.5);
			    g.setColor(cl[br2]);
			    g.drawLine(xz, yz, xz2, yz2);
			}// if br2<3
		    }// next k
		}// next j
	    }// next i
	}

	g.setColor(col2);
	g.drawString("N=" + N, 15, 15);
	if (MAX_ORDER == 1) {
	    g.setColor(cl[0]);
	    g.drawString("First order", 50, 15);
	} else if (MAX_ORDER == 2) {
	    g.setColor(cl[1]);
	    g.drawString("Second order", 50, 15);
	} else if (MAX_ORDER == 3) {
	    g.setColor(cl[2]);
	    g.drawString("Third order", 50, 15);
	} else if (MAX_ORDER == 4) {
	    g.setColor(cl[3]);
	    g.drawString("Fourth order", 50, 15);
	}
    }

    public JFrame newFrame(String title) {
	return new VoronoiFrame(this, title);
    }

    protected class VoronoiFrame extends JFrame {
	private static final long serialVersionUID = 1L;

	protected VoronoiPanel voronoiPanel;

	protected JMenuItem optionsMenuItem;
	protected JMenuItem exportPNGMenuItem;
	protected JMenuItem exportEPSMenuItem;

	protected JButton repaintButton;

	protected BeanEditor optionsEditor;

	protected VoronoiFrame(VoronoiPanel panel, String title) {
	    super(title);
	    this.voronoiPanel = panel;

	    add(voronoiPanel, BorderLayout.CENTER);

	    // Horizontal menu
	    JMenuBar menuBar = new JMenuBar();
	    JMenu menu = new JMenu("View");
	    exportPNGMenuItem = new JMenuItem("Export as PNG");
	    exportEPSMenuItem = new JMenuItem("Export as EPS");
	    optionsMenuItem = new JMenuItem("View Options");

	    ActionListener menuListener = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    if (e.getSource() == optionsMenuItem) {
			if (optionsEditor == null) {
			    optionsEditor = new BeanEditor(voronoiPanel);
			    optionsEditor.setLocation(getLocation().x,
				    getLocation().y + voronoiPanel.getHeight());

			}
			optionsEditor.setVisible(true);
		    } else if (e.getSource() == exportPNGMenuItem) {
			while (true) {
			    JFileChooser fc = new JFileChooser(defaultPath);
			    fc
				    .addChoosableFileFilter(new FileNameExtensionFilter(
					    "PNG Files", "png"));
			    fc.setDialogTitle(this.getClass().getName()
				    + " : Export to PNG");

			    if (fc.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
				File file = fc.getSelectedFile();
				defaultPath = file.getAbsolutePath();

				if (!GraphicsFileExport.exportPNG(voronoiPanel,
					file, voronoiPanel.getWidth(),
					voronoiPanel.getWidth())) {

				    JOptionPane.showMessageDialog(null,
					    "Could not export to \""
						    + file.getPath() + "\"",
					    "Error", JOptionPane.ERROR_MESSAGE);
				    continue;
				}
				break;
			    } else {
				break;
			    }
			}
		    } else if (e.getSource() == exportEPSMenuItem) {
			while (true) {
			    JFileChooser fc = new JFileChooser(defaultPath);
			    fc
				    .addChoosableFileFilter(new FileNameExtensionFilter(
					    "EPS Files", "eps"));
			    fc.setDialogTitle(this.getClass().getName()
				    + " : Export to EPS");

			    if (fc.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
				File file = fc.getSelectedFile();
				defaultPath = file.getAbsolutePath();

				if (!GraphicsFileExport.exportEPS(voronoiPanel,
					file, voronoiPanel.getWidth(),
					voronoiPanel.getWidth())) {

				    JOptionPane.showMessageDialog(null,
					    "Could not export to \""
						    + file.getPath() + "\"",
					    "Error", JOptionPane.ERROR_MESSAGE);
				    continue;
				}
				break;
			    } else {
				break;
			    }
			}
		    }
		}
	    };

	    exportPNGMenuItem.addActionListener(menuListener);
	    exportEPSMenuItem.addActionListener(menuListener);
	    optionsMenuItem.addActionListener(menuListener);

	    menu.add(exportPNGMenuItem);
	    menu.add(exportEPSMenuItem);
	    menu.addSeparator();
	    menu.add(optionsMenuItem);
	    menuBar.add(menu);
	    add(menuBar, BorderLayout.PAGE_START);

	    JToolBar zoomToolBar = new JToolBar();
	    repaintButton = new JButton("Repaint");

	    ActionListener toolBarListener = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    if (e.getSource() == repaintButton) {
			voronoiPanel.repaint();
		    }
		}
	    };

	    repaintButton.addActionListener(toolBarListener);

	    zoomToolBar.add(repaintButton);

	    add(zoomToolBar, BorderLayout.PAGE_END);

	    pack();
	}

	@Override
	public void setVisible(boolean b) {
	    super.setVisible(b);

	    if (b) {
		voronoiPanel.repaint();
	    }
	}
    }

    public int getMAX_ORDER() {
	return MAX_ORDER;
    }

    public void setMAX_ORDER(int max_order) {
	MAX_ORDER = max_order;
    }

    public boolean isSHOW_INSERTION_THRESHOLD() {
	return SHOW_INSERTION_THRESHOLD;
    }

    public void setSHOW_INSERTION_THRESHOLD(boolean show_insertion_threshold) {
	SHOW_INSERTION_THRESHOLD = show_insertion_threshold;
    }

    public boolean isSHOW_D_BALL() {
	return SHOW_D_BALL;
    }

    public void setSHOW_D_BALL(boolean show_d_ball) {
	SHOW_D_BALL = show_d_ball;
    }
}
