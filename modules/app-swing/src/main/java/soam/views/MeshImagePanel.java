/*  
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
import java.awt.Component;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import java.io.File;

import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import javax.swing.filechooser.FileNameExtensionFilter;

import soam.Edge;
import soam.Vertex;
import soam.algorithms.Algorithm;
import soam.utils.GraphicsFileExport;

public class MeshImagePanel extends JPanel {
    private static final long serialVersionUID = 1L;

    protected static String defaultPath = System.getProperty("user.dir");

    protected boolean SHOW_IMAGE = true;

    protected boolean SHOW_VERTICES = true;
    protected boolean SHOW_VERTEX_ERROR = false;

    protected boolean SHOW_EDGES = true;

    protected boolean SHOW_INSERTION_THRESHOLD = false;
    protected boolean ZOOM_REPAINT = true;

    protected int VERTEX_RADIUS = 3;

    protected Algorithm algorithm;
    protected BufferedImage image;
    protected String description = "";

    protected ZoomFrame zoomFrame;
    protected ZoomPanel zoomPanel;

    protected double maxVertexError = Double.MIN_VALUE;

    public MeshImagePanel(Algorithm algorithm, BufferedImage image,
	    String description) {
	super();

	this.algorithm = algorithm;
	this.image = image;
	this.description = description;

	setPreferredSize(new Dimension(image.getWidth(), image.getHeight()));
    }

    public void setImage(BufferedImage image) {
	this.image = image;
    }

    public void setDescription(String text) {
	description = text;
    }

    @Override
    public void paint(Graphics go) {

	go.setColor(Color.lightGray);
	go.fillRect(0, 0, getWidth(), getHeight());

	Graphics g = go.create();
	g.translate(getTranslationX(), getTranslationY());

	if (SHOW_IMAGE) {
	    g.drawImage(image, 0, 0, null);
	}

	Vertex vertex;
	Edge edge;
	double[] position1, position2;

	if (SHOW_EDGES) {
	    try {
		// Do not use iterators here: the main thread is running in
		// parallel
		for (int i = 0; i < algorithm.mesh().edgelist.size(); i++) {
		    g.setColor(Color.orange);
		    edge = algorithm.mesh().edgelist.get(i);
		    position1 = edge.start.position;
		    position2 = edge.end.position;
		    g.setColor(Color.DARK_GRAY);
		    g.drawLine((int) Math.round(position1[0]), (int) Math
			    .round(position1[1]), (int) Math
			    .round(position2[0]), (int) Math
			    .round(position2[1]));
		}
	    } catch (IndexOutOfBoundsException e) {
		// Do nothing, it's OK: see above
	    } catch (NullPointerException e) {
		// Do nothing, it's OK: see above
	    }
	}

	if (SHOW_VERTICES) {
	    double maxVertexErrorThisTime = Double.MIN_VALUE;
	    int diameter = 2 * VERTEX_RADIUS;

	    // Do not use iterators here: the main thread is running in parallel
	    try {
		for (int i = 0; i < algorithm.mesh().vertexlist.size(); i++) {
		    vertex = algorithm.mesh().vertexlist.get(i);

		    position1 = vertex.position;
		    g.setColor(Color.DARK_GRAY);
		    g.drawOval((int) Math.round(position1[0]) - (diameter / 2),
			    (int) Math.round(position1[1]) - (diameter / 2),
			    diameter, diameter);

		    if (SHOW_VERTEX_ERROR) {
			if (vertex.error > maxVertexErrorThisTime) {
			    maxVertexErrorThisTime = vertex.error;
			}
			int error =
				(int) (Math.min(vertex.error / maxVertexError,
					1) * 255);
			g.setColor(new Color(error, 0, 255 - error));
		    } else {
			g.setColor(SOAMStateColorCodes.getColorFor(vertex));
		    }

		    g.fillOval((int) Math.round(position1[0]) - (diameter / 2),
			    (int) Math.round(position1[1]) - (diameter / 2),
			    diameter, diameter);
		    
		    if (SHOW_INSERTION_THRESHOLD) {
			int diameter2 = (int) (2 * vertex.insertionThreshold);
			g.drawOval((int) Math.round(position1[0])
				- (diameter2 / 2), (int) Math
				.round(position1[1])
				- (diameter2 / 2), diameter2, diameter2);
		    }
		}
	    } catch (IndexOutOfBoundsException e) {
		// Do nothing, it's OK: see above
	    } catch (NullPointerException e) {
		// Do nothing, it's OK: see above
	    }

	    maxVertexError = maxVertexErrorThisTime;
	}
    }

    @Override
    public void repaint() {
	super.repaint();

	if (ZOOM_REPAINT && zoomFrame != null && zoomFrame.isVisible()) {
	    zoomPanel.repaint();
	}
    }

    public JFrame newFrame(String title) {
	return new MeshImageFrame(this, title);
    }

    protected int getTranslationX() {
	if (getWidth() > image.getWidth()) {
	    return (int) ((getWidth() - image.getWidth()) / 2);
	} else {
	    return 0;
	}
    }

    protected int getTranslationY() {
	if (getHeight() > image.getHeight()) {
	    return (int) ((getHeight() - image.getHeight()) / 2);
	} else {
	    return 0;
	}
    }

    protected class MeshImageFrame extends JFrame {
	private static final long serialVersionUID = 1L;

	protected MeshImagePanel imagePanel;

	protected JMenuItem zoomMenuItem;
	protected JMenuItem optionsMenuItem;
	protected JMenuItem exportPNGMenuItem;
	protected JMenuItem exportEPSMenuItem;

	protected JLabel mousePositionLabel;

	protected BeanEditor optionsEditor;

	protected int mouseX = -1;
	protected int mouseY = -1;

	protected MeshImageFrame(MeshImagePanel panel, String title) {
	    super(title);
	    this.imagePanel = panel;

	    MouseListener mouseListener = new MouseListener() {

		public void mouseClicked(MouseEvent e) {
		    int x = e.getX() - imagePanel.getTranslationX();
		    int y = e.getY() - imagePanel.getTranslationY();

		    Vertex closestVertex = null;
		    double shortestDistance = Double.MAX_VALUE;
		    for (int i = 0; i < algorithm.mesh().vertexlist.size(); i++) {
			Vertex vertex = algorithm.mesh().vertexlist.get(i);
			double d =
				Math.pow(vertex.position[0] - x, 2)
					+ Math.pow(vertex.position[1] - y, 2);
			if (d < shortestDistance) {
			    closestVertex = vertex;
			    shortestDistance = d;
			}
		    }

		    if (shortestDistance <= 36d) {
			VertexInspector.inspect(closestVertex);
		    }
		}

		public void mousePressed(MouseEvent e) {
		}

		public void mouseReleased(MouseEvent e) {
		}

		public void mouseEntered(MouseEvent e) {
		}

		public void mouseExited(MouseEvent e) {
		    mouseX = -1;
		    mouseY = -1;
		    mousePositionLabel.setText(description + " : ");
		}

	    };

	    imagePanel.addMouseListener(mouseListener);

	    MouseMotionListener mouseMotionListener =
		    new MouseMotionListener() {

			public void mouseMoved(MouseEvent e) {
			    mouseX = e.getX() - imagePanel.getTranslationX();
			    mouseY = e.getY() - imagePanel.getTranslationY();

			    mousePositionLabel.setText(description + " : "
				    + mouseX + "," + mouseY);
			}

			public void mouseDragged(MouseEvent e) {
			}
		    };

	    imagePanel.addMouseMotionListener(mouseMotionListener);

	    add(imagePanel, BorderLayout.CENTER);

	    JMenuBar menuBar = new JMenuBar();
	    JMenu menu = new JMenu("View");
	    zoomMenuItem = new JMenuItem("Open Zoom Window");
	    exportPNGMenuItem = new JMenuItem("Export as PNG");
	    exportEPSMenuItem = new JMenuItem("Export as EPS");
	    optionsMenuItem = new JMenuItem("View Options");

	    ActionListener menuListener = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    if (e.getSource() == optionsMenuItem) {
			if (optionsEditor == null) {
			    optionsEditor = new BeanEditor(imagePanel);
			    optionsEditor.setLocation(getLocation().x,
				    getLocation().y + imagePanel.getHeight());

			}
			optionsEditor.setVisible(true);
		    } else if (e.getSource() == zoomMenuItem) {
			ensureZoomFrame();
			zoomFrame.setVisible(true);
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

				if (!GraphicsFileExport.exportPNG(imagePanel,
					file, imagePanel.getWidth(), imagePanel
						.getWidth())) {

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

				if (!GraphicsFileExport.exportEPS(imagePanel,
					file, imagePanel.getWidth(), imagePanel
						.getWidth())) {

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

	    zoomMenuItem.addActionListener(menuListener);
	    exportPNGMenuItem.addActionListener(menuListener);
	    exportEPSMenuItem.addActionListener(menuListener);
	    optionsMenuItem.addActionListener(menuListener);

	    menu.add(zoomMenuItem);
	    menu.addSeparator();
	    menu.add(exportPNGMenuItem);
	    menu.add(exportEPSMenuItem);
	    menu.addSeparator();
	    menu.add(optionsMenuItem);
	    menuBar.add(menu);
	    add(menuBar, BorderLayout.PAGE_START);

	    // Vertical toolbar
	    JToolBar toolBar = new JToolBar();

	    mousePositionLabel = new JLabel(description + " : ");

	    toolBar.add(mousePositionLabel);
	    add(toolBar, BorderLayout.PAGE_END);

	    pack();
	}

	protected void ensureZoomFrame() {
	    if (zoomFrame != null) {
		return;
	    }

	    zoomPanel = new ZoomPanel(imagePanel);
	    zoomPanel.setZoomLocation(new Point(
		    (int) (imagePanel.getWidth() / 2), (int) (imagePanel
			    .getHeight() / 2)));

	    MouseInputAdapter zoomUpdater = new MouseInputAdapter() {
		public void mouseMoved(MouseEvent e) {
		    update(e);
		}

		private void update(MouseEvent e) {
		    zoomPanel.setZoomLocation(e.getPoint());
		}
	    };

	    imagePanel.addMouseListener(zoomUpdater);
	    imagePanel.addMouseMotionListener(zoomUpdater);
	    imagePanel.zoomPanel = zoomPanel;
	    imagePanel.zoomFrame = zoomFrame;

	    zoomFrame =
		    new ZoomFrame(zoomPanel, algorithm.getClass()
			    .getSimpleName()
			    + " Zoom on Image View");
	    zoomFrame.pack();

	    zoomFrame.setLocation(getLocation().x + imagePanel.getWidth(),
		    getLocation().y);
	    zoomFrame.setResizable(true);

	    zoomFrame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
	}

	@Override
	public void repaint() {
	    super.repaint();

	    if (mouseX > 0 && mouseY > 0) {
		mousePositionLabel.setText(description + " : " + mouseX + ","
			+ mouseY);
	    } else {
		mousePositionLabel.setText(description + " : ");
	    }

	}

	@Override
	public void setVisible(boolean visible) {
	    super.setVisible(visible);

	    if (!visible && optionsEditor != null) {
		optionsEditor.setVisible(visible);
	    }
	    if (!visible && zoomFrame != null) {
		zoomFrame.setVisible(visible);
	    }
	}

	@Override
	public void dispose() {
	    if (optionsEditor != null) {
		optionsEditor.dispose();
	    }
	    if (zoomFrame != null) {
		zoomFrame.dispose();
	    }

	    super.dispose();
	}
    }

    protected class ZoomFrame extends JFrame {
	private static final long serialVersionUID = 1L;

	private final static String PLUS_ICON_LOCATION = "icons/plus-8.png";
	private final static String MINUS_ICON_LOCATION = "icons/minus-8.png";

	protected final static int MAX_ZOOM = 8;
	protected final static int MIN_ZOOM = 1;

	protected JButton increaseButton;
	protected JButton decreaseButton;

	protected ZoomFrame(ZoomPanel panel, String title) {
	    super(title);

	    if (panel == null) {
		throw new RuntimeException("zoomPanel has not been created yet");
	    }

	    add(zoomPanel, BorderLayout.CENTER);

	    JToolBar zoomToolBar = new JToolBar(JToolBar.VERTICAL);
	    increaseButton = new JButton();
	    increaseButton.setIcon(new ImageIcon(this.getClass().getResource(
		    PLUS_ICON_LOCATION)));

	    decreaseButton = new JButton();
	    decreaseButton.setIcon(new ImageIcon(this.getClass().getResource(
		    MINUS_ICON_LOCATION)));

	    ActionListener toolBarListener = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    if (e.getSource() == increaseButton) {
			zoomPanel.magnification =
				Math.min(MAX_ZOOM, zoomPanel.magnification + 1);
			zoomPanel.repaint();
		    } else if (e.getSource() == decreaseButton) {
			zoomPanel.magnification =
				Math.max(MIN_ZOOM, zoomPanel.magnification - 1);
			zoomPanel.repaint();
		    }
		}
	    };

	    increaseButton.addActionListener(toolBarListener);
	    decreaseButton.addActionListener(toolBarListener);

	    zoomToolBar.add(increaseButton);
	    zoomToolBar.add(decreaseButton);
	    add(zoomToolBar, BorderLayout.WEST);
	}
    }

    protected class ZoomPanel extends JPanel {
	private static final long serialVersionUID = 1L;

	protected float magnification = 4.0f;

	private Component onComponent;
	private Point position;

	public ZoomPanel(Component onComponent) {
	    if (onComponent == null)
		throw new NullPointerException("zoomComponent");
	    this.onComponent = onComponent;

	    setPreferredSize(new Dimension(400, 400));
	}

	@Override
	protected void paintComponent(Graphics g) {
	    if (position != null) {
		g.setColor(Color.lightGray);
		g.fillRect(0, 0, getWidth(), getHeight());

		Graphics2D g2 = (Graphics2D) g.create();
		g2.translate(getWidth() / 2, getHeight() / 2);
		g2.scale(magnification, magnification);
		g2.translate(-position.x, -position.y);
		onComponent.paint(g2);

		g2.dispose();

		// Draw crosshair
		g.setColor(Color.BLACK);
		g.drawLine((getWidth() / 2) - 10, getHeight() / 2,
			(getWidth() / 2) + 10, getHeight() / 2);
		g.drawLine(getWidth() / 2, (getHeight() / 2) - 10,
			getWidth() / 2, (getHeight() / 2) + 10);

	    }
	}

	public void setZoomLocation(Point p) {
	    this.position = p;
	    repaint();
	}
    }

    public boolean isSHOW_IMAGE() {
	return SHOW_IMAGE;
    }

    public void setSHOW_IMAGE(boolean show_image) {
	SHOW_IMAGE = show_image;
    }

    public boolean isSHOW_VERTICES() {
	return SHOW_VERTICES;
    }

    public void setSHOW_VERTICES(boolean show_vertices) {
	SHOW_VERTICES = show_vertices;
    }

    public boolean isSHOW_EDGES() {
	return SHOW_EDGES;
    }

    public void setSHOW_EDGES(boolean show_edges) {
	SHOW_EDGES = show_edges;
    }

    public boolean isSHOW_INSERTION_THRESHOLD() {
	return SHOW_INSERTION_THRESHOLD;
    }

    public void setSHOW_INSERTION_THRESHOLD(boolean show_insertion_threshold) {
	SHOW_INSERTION_THRESHOLD = show_insertion_threshold;
    }

    public int getVERTEX_RADIUS() {
	return VERTEX_RADIUS;
    }

    public void setVERTEX_RADIUS(int vertex_radius) {
	VERTEX_RADIUS = vertex_radius;
    }

    public boolean getZOOM_REPAINT() {
	return ZOOM_REPAINT;
    }

    public void setZOOM_REPAINT(boolean zoom_repaint) {
	ZOOM_REPAINT = zoom_repaint;
    }

    public boolean isSHOW_VERTEX_ERROR() {
	return SHOW_VERTEX_ERROR;
    }

    public void setSHOW_VERTEX_ERROR(boolean show_vertex_error) {
	SHOW_VERTEX_ERROR = show_vertex_error;
    }
}
