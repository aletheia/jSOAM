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

package soam.views;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.WindowConstants;

import soam.Edge;
import soam.Vertex;
import soam.algorithms.SOAMConstants;
import soam.algorithms.SOAM2d;

public class VertexInspector extends JFrame implements SOAMConstants {
    private static final long serialVersionUID = 1L;

    private final static String PLUS_ICON_LOCATION = "icons/plus-8.png";
    private final static String MINUS_ICON_LOCATION = "icons/minus-8.png";

    protected Vertex vertex;
    protected Vertex secondVertex;
    protected double[] point;

    protected boolean zoomed = true;

    protected JButton repaintButton;
    protected JButton increaseButton;
    protected JButton decreaseButton;

    VertexInspectorPanel panel;

    protected static Vertex generate() {
        double h0 = 30d;
        double h1 = 50d;
        double h2 = 70d;

        double[] f0 = { 100d, 100d, h1 };
        Vertex vertex = new Vertex(f0);

        double[] f1 = { 100d, 80d, h0 };
        vertex.neighborhood.add(new Edge(new Vertex(f1), vertex));
        double[] f2 = { 82.679d, 90d, h2 };
        vertex.neighborhood.add(new Edge(new Vertex(f2), vertex));
        double[] f3 = { 117.324d, 90d, h0 };
        vertex.neighborhood.add(new Edge(new Vertex(f3), vertex));
        double[] f4 = { 82.679d, 110d, h2 };
        vertex.neighborhood.add(new Edge(new Vertex(f4), vertex));
        double[] f5 = { 117.32d, 110d, h0 };
        vertex.neighborhood.add(new Edge(new Vertex(f5), vertex));
        double[] f6 = { 90d, 120d, h0 };
        vertex.neighborhood.add(new Edge(new Vertex(f6), vertex));

        return vertex;
    }

    public VertexInspector(Vertex vertex) {
        this(vertex, null, null);
    }

    public VertexInspector(Vertex vertex, double[] point) {
        this(vertex, null, point);
    }

    public VertexInspector(Vertex vertex, Vertex secondVertex, double[] point) {
        super("VertexGWRInspector");

        this.vertex = vertex;
        this.secondVertex = secondVertex;
        this.point = point;

        panel = new VertexInspectorPanel();
        add(panel, BorderLayout.CENTER);

        JToolBar zoomToolBar = new JToolBar();
        repaintButton = new JButton("Repaint");
        increaseButton = new JButton();
        increaseButton.setIcon(new ImageIcon(this.getClass().getResource(
                PLUS_ICON_LOCATION)));

        decreaseButton = new JButton();
        decreaseButton.setIcon(new ImageIcon(this.getClass().getResource(
                MINUS_ICON_LOCATION)));

        ActionListener toolBarListener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (e.getSource() == repaintButton) {
                    panel.repaint();
                } else if (e.getSource() == increaseButton) {
                    panel.magnification = Math.min(panel.MAX_ZOOM,
                            panel.magnification + 1);
                    panel.repaint();
                } else if (e.getSource() == decreaseButton) {
                    panel.magnification = Math.max(panel.MIN_ZOOM,
                            panel.magnification - 1);
                    panel.repaint();
                }
            }
        };

        repaintButton.addActionListener(toolBarListener);
        increaseButton.addActionListener(toolBarListener);
        decreaseButton.addActionListener(toolBarListener);

        zoomToolBar.add(repaintButton);
        zoomToolBar.add(increaseButton);
        zoomToolBar.add(decreaseButton);

        add(zoomToolBar, BorderLayout.PAGE_END);

        pack();
    }

    public static void main(String[] args) {
        double[] p = { 110d, 110d, 50d };
        Vertex vertex = generate();
        VertexInspector t = inspect(vertex, vertex.neighborhood.get(0)
                .getOtherVertex(vertex), p);
        t.setVisible(true);
    }

    public static VertexInspector inspect(Vertex vertex) {
        return inspect(vertex, null, null);
    }

    public static VertexInspector inspect(Vertex vertex, double[] point) {
        return inspect(vertex, null, point);
    }

    public static VertexInspector inspect(Vertex vertex, Vertex secondVertex,
            double[] point) {
        VertexInspector t = new VertexInspector(vertex, secondVertex, point);

        t.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        t.setVisible(true);

        return t;
    }

    protected class VertexInspectorPanel extends JPanel {
        private static final long serialVersionUID = 1L;

        protected final int WIDTH = 800;
        protected final int HEIGHT = 500;
        protected final int KEY_X = 16;
        protected final int KEY_Y = 280;

        protected int[] x = new int[2];
        protected int[] y = new int[2];
        protected int[] z = new int[2];

        // Y projection center coordinates
        protected int xX = 650;
        protected int yX = 150;

        // Y projection center coordinates
        protected int xY = 400;
        protected int yY = 150;

        // Z projection center coordinates
        protected int xZ = 150;
        protected int yZ = 150;

        protected final double MIN_ZOOM = 2d;
        protected final double MAX_ZOOM = 64d;
        protected double magnification = 2d;

        protected VertexInspectorPanel() {
            super();
            setPreferredSize(new Dimension(WIDTH, HEIGHT));
        }

        public void paint(Graphics g) {
            String text;
            Vertex link[] = getLink(vertex);
            Vertex linkVertex;

            boolean secondVertexInLink = false;
            if (secondVertex != null) {
                for (int i = 0; i < link.length; i++) {
                    if (secondVertex == link[i]) {
                        secondVertexInLink = true;
                    }
                }
            }

            g.clearRect(0, 0, getWidth(), getHeight());

            // Z projection
            g.setColor(Color.darkGray);
            g.drawLine(xZ - 60, yZ - 60, xZ + 70, yZ - 60);
            g.drawLine(xZ - 60, yZ - 60, xZ - 60, yZ + 70);
            g.drawString("X", xZ + 70 - 20, yZ - 60 - 2);
            g.drawString("Y", xZ - 60 - 10, yZ + 70 - 20);

            z = moveZ(vertex, xZ, yZ, magnification);

            for (int i = 0; i < vertex.neighborhood.size(); i++) {
                Edge edge = vertex.neighborhood.get(i);
                g.setColor(Color.gray);
                g
                        .drawLine(
                                (int) (magnification
                                        * Math.round(edge.start.position[0]) + z[0]),
                                (int) (magnification
                                        * Math.round(edge.start.position[1]) + z[1]),
                                (int) (magnification
                                        * Math.round(edge.end.position[0]) + z[0]),
                                (int) (magnification
                                        * Math.round(edge.end.position[1]) + z[1]));
            }

            g.setColor(SOAMStateColorCodes.getColorFor(vertex));
            g.fillOval(xZ - 4, yZ - 4, 8, 8);

            for (int i = 0; i < vertex.neighborhood.size(); i++) {
                Vertex otherVertex = vertex.neighborhood.get(i).getOtherVertex(
                        vertex);
                g.setColor(SOAMStateColorCodes.getColorFor(otherVertex));
                g.fillOval((int) (magnification
                        * Math.round(otherVertex.position[0]) + z[0]) - 4,
                        (int) (magnification * Math
                                .round(otherVertex.position[1]))
                                + z[1] - 4, 8, 8);

                if (secondVertex == otherVertex) {
                    g.setColor(Color.black);
                    g.drawOval((int) (magnification
                            * Math.round(otherVertex.position[0]) + z[0]) - 4,
                            (int) (magnification * Math
                                    .round(otherVertex.position[1]))
                                    + z[1] - 4, 8, 8);
                }

                g.setColor(Color.black);
                text = "" + i;
                g.drawString(text, (int) (magnification * Math
                        .round(otherVertex.position[0]))
                        + z[0] - 4, (int) (magnification * Math
                        .round(otherVertex.position[1]))
                        + z[1] - 4);
            }

            // Draw link

            for (int i = 0; i < link.length; i++) {
                for (Edge edge : link[i].neighborhood) {
                    linkVertex = edge.getOtherVertex(link[i]);
                    for (int j = 0; j < link.length; j++) {
                        if (link[j] == linkVertex) {
                            g.setColor(Color.ORANGE);
                            g.drawLine((int) (magnification * Math
                                    .round(edge.start.position[0]))
                                    + z[0], (int) (magnification * Math
                                    .round(edge.start.position[1]))
                                    + z[1], (int) (magnification * Math
                                    .round(edge.end.position[0]))
                                    + z[0], (int) (magnification * Math
                                    .round(edge.end.position[1]))
                                    + z[1]);
                        }
                    }
                }
            }

            if (secondVertex != null && !secondVertexInLink) {
                g.setColor(SOAMStateColorCodes.getColorFor(secondVertex));
                g.fillOval((int) (magnification
                        * Math.round(secondVertex.position[0]) + z[0]) - 4,
                        (int) (magnification * Math
                                .round(secondVertex.position[1]))
                                + z[1] - 4, 8, 8);

                g.setColor(Color.black);
                g.drawOval((int) (magnification
                        * Math.round(secondVertex.position[0]) + z[0]) - 4,
                        (int) (magnification * Math
                                .round(secondVertex.position[1]))
                                + z[1] - 4, 8, 8);
            }

            if (point != null) {
                g.setColor(Color.BLACK);
                g.fillOval((int) (magnification * point[0]) + z[0] - 4,
                        (int) (magnification * point[1]) + z[1] - 4, 8, 8);
            }

            if (SOAM2d.isConnected(vertex)) {
                // Draw activity radius
                int radius = (int) (magnification * vertex.insertionThreshold);
                g.setColor(Color.LIGHT_GRAY);
                g.drawOval(xZ - radius, yZ - radius, 2 * radius, 2 * radius);
            }

            if (vertex.position.length > 2) {

                // Y projection

                g.setColor(Color.darkGray);
                g.drawLine(xY - 60, yY - 60, xY + 70, yZ - 60);
                g.drawLine(xY - 60, yY - 60, xY - 60, yY + 70);
                g.drawString("X", xY + 70 - 20, yY - 60 - 2);
                g.drawString("Z", xY - 60 - 10, yY + 70 - 20);

                y = moveY(vertex, xY, yY, magnification);

                for (int i = 0; i < vertex.neighborhood.size(); i++) {
                    Edge edge = vertex.neighborhood.get(i);

                    g.setColor(Color.gray);
                    g.drawLine((int) (magnification * Math
                            .round(edge.start.position[0]))
                            + y[0], (int) (magnification * Math
                            .round(edge.start.position[2]))
                            + y[1], (int) (magnification * Math
                            .round(edge.end.position[0]))
                            + y[0], (int) (magnification * Math
                            .round(edge.end.position[2]))
                            + y[1]);
                }

                g.setColor(SOAMStateColorCodes.getColorFor(vertex));
                g.fillOval(xY - 4, yY - 4, 8, 8);

                for (int i = 0; i < vertex.neighborhood.size(); i++) {
                    Vertex otherVertex = vertex.neighborhood.get(i)
                            .getOtherVertex(vertex);
                    g.setColor(SOAMStateColorCodes.getColorFor(otherVertex));
                    g.fillOval((int) (magnification * Math
                            .round(otherVertex.position[0]))
                            - 4 + y[0], (int) (magnification * Math
                            .round(otherVertex.position[2]))
                            - 4 + y[1], 8, 8);

                    if (secondVertex == otherVertex) {
                        g.setColor(Color.black);
                        g.drawOval((int) (magnification * Math
                                .round(otherVertex.position[0]))
                                - 4 + y[0], (int) (magnification * Math
                                .round(otherVertex.position[2]))
                                - 4 + y[1], 8, 8);
                    }

                    g.setColor(Color.black);
                    text = "" + i;
                    g.drawString(text, (int) (magnification * Math
                            .round(otherVertex.position[0]))
                            - 4 + y[0], (int) (magnification * Math
                            .round(otherVertex.position[2]))
                            - 4 + y[1]);
                }

                for (int i = 0; i < link.length; i++) {
                    for (Edge edge : link[i].neighborhood) {
                        linkVertex = edge.getOtherVertex(link[i]);
                        for (int j = 0; j < link.length; j++) {
                            if (link[j] == linkVertex) {
                                g.setColor(Color.ORANGE);
                                g.drawLine((int) (magnification * Math
                                        .round(edge.start.position[0]))
                                        + y[0], (int) (magnification * Math
                                        .round(edge.start.position[2]))
                                        + y[1], (int) (magnification * Math
                                        .round(edge.end.position[0]))
                                        + y[0], (int) (magnification * Math
                                        .round(edge.end.position[2]))
                                        + y[1]);
                            }
                        }
                    }
                }

                if (secondVertex != null && !secondVertexInLink) {
                    g.setColor(SOAMStateColorCodes.getColorFor(secondVertex));
                    g.fillOval((int) (magnification
                            * Math.round(secondVertex.position[0]) + y[0]) - 4,
                            (int) (magnification * Math
                                    .round(secondVertex.position[2]))
                                    + y[1] - 4, 8, 8);

                    g.setColor(Color.black);
                    g.drawOval((int) (magnification
                            * Math.round(secondVertex.position[0]) + y[0]) - 4,
                            (int) (magnification * Math
                                    .round(secondVertex.position[2]))
                                    + y[1] - 4, 8, 8);
                }

                if (point != null) {
                    g.setColor(Color.BLACK);
                    g.fillOval((int) (magnification * point[0]) + y[0] - 4,
                            (int) (magnification * point[2]) + y[1] - 4, 8, 8);
                }

                if (SOAM2d.isConnected(vertex)) {
                    // Draw activity radius
                    int radius = (int) (magnification * vertex.insertionThreshold);
                    g.setColor(Color.LIGHT_GRAY);
                    g
                            .drawOval(xY - radius, yY - radius, 2 * radius,
                                    2 * radius);
                }

                // X projection

                g.setColor(Color.darkGray);
                g.drawLine(xX - 60, yX - 60, xX + 70, yX - 60);
                g.drawLine(xX - 60, yX - 60, xX - 60, yX + 70);
                g.drawString("Y", xX + 70 - 20, yX - 60 - 2);
                g.drawString("Z", xX - 60 - 10, yX + 70 - 20);

                x = moveX(vertex, xX, yX, magnification);

                for (int i = 0; i < vertex.neighborhood.size(); i++) {
                    Edge edge = vertex.neighborhood.get(i);

                    g.setColor(Color.gray);

                    g.drawLine((int) (magnification * Math
                            .round(edge.start.position[1]))
                            + x[0], (int) (magnification * Math
                            .round(edge.start.position[2]))
                            + x[1], (int) (magnification * Math
                            .round(edge.end.position[1]))
                            + x[0], (int) (magnification * Math
                            .round(edge.end.position[2]))
                            + x[1]);
                }

                g.setColor(SOAMStateColorCodes.getColorFor(vertex));
                g.fillOval(xX - 4, yX - 4, 8, 8);

                for (int i = 0; i < vertex.neighborhood.size(); i++) {
                    Vertex otherVertex = vertex.neighborhood.get(i)
                            .getOtherVertex(vertex);
                    g.setColor(SOAMStateColorCodes.getColorFor(otherVertex));
                    g.fillOval((int) (magnification * Math
                            .round(otherVertex.position[1]))
                            - 4 + x[0], (int) (magnification * Math
                            .round(otherVertex.position[2]))
                            - 4 + x[1], 8, 8);

                    if (secondVertex == otherVertex) {
                        g.setColor(Color.black);
                        g.drawOval((int) (magnification * Math
                                .round(otherVertex.position[1]))
                                - 4 + x[0], (int) (magnification * Math
                                .round(otherVertex.position[2]))
                                - 4 + x[1], 8, 8);
                    }

                    g.setColor(Color.black);
                    text = "" + i;
                    g.drawString(text, (int) (magnification * Math
                            .round(otherVertex.position[1]))
                            - 4 + x[0], (int) (magnification * Math
                            .round(otherVertex.position[2]))
                            - 4 + x[1]);
                }

                for (int i = 0; i < link.length; i++) {
                    for (Edge edge : link[i].neighborhood) {
                        linkVertex = edge.getOtherVertex(link[i]);
                        for (int j = 0; j < link.length; j++) {
                            if (link[j] == linkVertex) {
                                g.setColor(Color.ORANGE);
                                g.drawLine((int) (magnification * Math
                                        .round(edge.start.position[1]))
                                        + x[0], (int) (magnification * Math
                                        .round(edge.start.position[2]))
                                        + x[1], (int) (magnification * Math
                                        .round(edge.end.position[1]))
                                        + x[0], (int) (magnification * Math
                                        .round(edge.end.position[2]))
                                        + x[1]);
                            }
                        }
                    }
                }

                if (secondVertex != null && !secondVertexInLink) {
                    g.setColor(SOAMStateColorCodes.getColorFor(secondVertex));
                    g.fillOval((int) (magnification
                            * Math.round(secondVertex.position[1]) + x[0]) - 4,
                            (int) (magnification * Math
                                    .round(secondVertex.position[2]))
                                    + x[1] - 4, 8, 8);

                    g.setColor(Color.black);
                    g.drawOval((int) (magnification
                            * Math.round(secondVertex.position[1]) + x[0]) - 4,
                            (int) (magnification * Math
                                    .round(secondVertex.position[2]))
                                    + x[1] - 4, 8, 8);
                }

                if (point != null) {
                    g.setColor(Color.BLACK);
                    g.fillOval((int) (magnification * point[1]) + x[0] - 4,
                            (int) (magnification * point[2]) + x[1] - 4, 8, 8);
                }

                if (SOAM2d.isConnected(vertex)) {
                    // Draw activity radius
                    int radius = (int) (magnification * vertex.insertionThreshold);
                    g.setColor(Color.LIGHT_GRAY);
                    g
                            .drawOval(xX - radius, yX - radius, 2 * radius,
                                    2 * radius);
                }
            }

            g.setColor(Color.BLACK);
            text = vertex.toString() + " : ";
            if (vertex.position.length > 2) {
                text += "{" + (int) vertex.position[0] + ", "
                        + (int) vertex.position[1] + ", "
                        + (int) vertex.position[2] + "}; Firing counter: "
                        + vertex.firingCounter + "; Insertion Threshold: "
                        + vertex.insertionThreshold + "; last winner: "
                        + vertex.lastWin;
            } else {
                text += "{" + (int) vertex.position[0] + ", "
                        + (int) vertex.position[1] + "}; Firing counter: "
                        + vertex.firingCounter + "; Insertion Threshold: "
                        + vertex.insertionThreshold + "; last winner: "
                        + vertex.lastWin;
            }
            g.drawString(text, KEY_X, KEY_Y);

            text = "State: " + stateOf(vertex);
            g.drawString(text, KEY_X, KEY_Y + KEY_X);

            if (secondVertex != null && !secondVertexInLink) {
                text = "Second vertex: " + secondVertex.toString() + " : ";
                if (vertex.position.length > 2) {
                    text += "{" + (int) vertex.position[0] + ", "
                            + (int) vertex.position[1] + ", "
                            + (int) vertex.position[2] + "}";
                } else {
                    text += "{" + (int) vertex.position[0] + ", "
                            + (int) vertex.position[1] + "}";
                }
                g.drawString(text, KEY_X, KEY_Y + 2 * KEY_X);
            }

            if (point != null) {
                if (vertex.position.length > 2) {
                    text = "Sampled point: {" + +(int) point[0] + ", "
                            + (int) point[1] + ", " + (int) point[2] + "}";
                } else {
                    text = "Sampled point: {" + +(int) point[0] + ", "
                            + (int) point[1] + "}";
                }
                g.drawString(text, KEY_X, KEY_Y + 3 * KEY_X);
            }

            g.drawString("Neighborhood:", KEY_X, KEY_Y + 4 * KEY_X);
            int i = 0;
            for (Edge edge : vertex.neighborhood) {
                Vertex otherVertex = edge.getOtherVertex(vertex);
                if (vertex.position.length > 2) {
                    text = "[" + i + ", age: " + edge.age + "] {"
                            + (int) otherVertex.position[0] + ", "
                            + (int) otherVertex.position[1] + ", "
                            + (int) otherVertex.position[2]
                            + "};  Firing counter: "
                            + otherVertex.firingCounter
                            + "; Insertion threshold: "
                            + otherVertex.insertionThreshold
                            + "; last winner: " + otherVertex.lastWin;
                } else {
                    text = "[" + i + ", age: " + edge.age + "] {"
                            + (int) otherVertex.position[0] + ", "
                            + (int) otherVertex.position[1]
                            + "};  Firing counter: "
                            + otherVertex.firingCounter
                            + "; Insertion threshold: "
                            + otherVertex.insertionThreshold
                            + "; last winner: " + otherVertex.lastWin;
                }

                g.drawString(text, KEY_X, KEY_Y + 5 * KEY_X + i * KEY_X);
                i++;
            }
        }
    }

    /**
     * This method ask the vertex for his state.
     * 
     * @param vertex
     * @return The string of the state
     */
    private String stateOf(Vertex vertex) {
        String text = "";
        int s = vertex.state;
        switch (s) {
        case SOAMConstants.ACTIVE:
            text = "ACTIVE";
            break;

        case SOAMConstants.HABITUATED:
            text = "HABITUATED";
            break;

        case SOAMConstants.CONNECTED:
            text = "CONNECTED";
            break;

        case SOAMConstants.SINGULAR:
            if (SOAM2d.isNotPlanar(vertex)) {
                text = "SINGULAR";
            } else if (SOAM2d.isCusp(vertex)) {
                text = "CUSP";
            }
            break;

        case SOAMConstants.DISK:
            text = "DISK";
            break;

        case SOAMConstants.HALF_DISK:
            text = "HALF DISK";
            break;
        case SOAMConstants.PATCH:
            text = "PATCH";
            break;

        case SOAMConstants.BOUNDARY:
            text = "BOUNDARY";
            break;
        }
        return text;
    }

    /**
     * This method constructs the link of the vertex neighborhood
     * 
     * @param vertex
     * @return
     */
    public static Vertex[] getLink(Vertex vertex) {
        Vertex[] link = new Vertex[vertex.neighborhood.size()];
        int i = 0;
        for (Edge edge : vertex.neighborhood) {
            link[i] = edge.getOtherVertex(vertex);
            i++;
        }
        return link;
    }

    /**
     * return the Z displacement for the other vertex
     * 
     * @param vertex
     * @param x
     * @param y
     * @param zoom
     * @return
     */
    private int[] moveZ(Vertex vertex, int x, int y, double zoom) {
        int[] xy = new int[2];
        if (!zoomed) {
            xy[0] = (int) (x - vertex.position[0]);
            xy[1] = (int) (y - vertex.position[1]);
        } else {
            xy[0] = (int) (x - zoom * vertex.position[0]);
            xy[1] = (int) (y - zoom * vertex.position[1]);
        }
        return xy;
    }

    /**
     * return the Y displacement for the other vertex
     * 
     * @param vertex
     * @param x
     * @param y
     * @param zoom
     * @return
     */
    private int[] moveY(Vertex vertex, int x, int z, double zoom) {
        int[] xz = new int[2];
        if (!zoomed) {
            xz[0] = (int) (x - vertex.position[0]);
            xz[1] = (int) (z - vertex.position[2]);
        } else {
            xz[0] = (int) (x - zoom * vertex.position[0]);
            xz[1] = (int) (z - zoom * vertex.position[2]);
        }
        return xz;
    }

    /**
     * return the X displacement for the other vertex
     * 
     * @param vertex
     * @param x
     * @param y
     * @param zoom
     * @return
     */
    private int[] moveX(Vertex vertex, int y, int z, double zoom) {
        int[] yz = new int[2];
        if (!zoomed) {
            yz[0] = (int) (y - vertex.position[1]);
            yz[1] = (int) (z - vertex.position[2]);
        } else {
            yz[0] = (int) (y - zoom * vertex.position[1]);
            yz[1] = (int) (z - zoom * vertex.position[2]);
        }
        return yz;
    }

}