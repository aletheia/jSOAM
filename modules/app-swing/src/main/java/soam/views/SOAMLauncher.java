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

import java.io.IOException;
import java.util.ArrayList;
import java.lang.reflect.*;

import java.awt.Dimension;
import java.awt.Cursor;
import java.awt.Toolkit;
import java.awt.event.*;

import javax.swing.*;
import static javax.swing.GroupLayout.Alignment.*;

public class SOAMLauncher extends JFrame implements ActionListener {
    private static final long serialVersionUID = 1L;

    protected JLabel algorithmLabel;
    protected JLabel samplerLabel;
    protected JComboBox algorithmChooser;
    protected JComboBox samplerChooser;
    protected JButton launchButton;

    protected boolean busy = false;
    
    protected static ArrayList<ClassDescription> options;

    public static void main(String[] args) throws IOException {
        ClassDescription description;
        ClassDescription[] related;

        options = new ArrayList<ClassDescription>();

        // If needed, just edit this section - BEGIN
        
        description = new ClassDescription();
        description.name = "GWR";
        description.controller = soam.algorithms.GWR.class;
        description.viewer = soam.views.AlgorithmConsole.class;
        related = new ClassDescription[7];
        related[0] = new ClassDescription();
        related[0].name = "GrayscaleImageSampler";
        related[0].controller = soam.sampling.GrayscaleImageSampler.class;
        related[1] = new ClassDescription();
        related[1].name = "ImageSampler";
        related[1].controller = soam.sampling.ImageSampler.class;
        related[2] = new ClassDescription();
        related[2].name = "PlySampler";
        related[2].controller = soam.sampling.PLYSampler.class;
        related[3] = new ClassDescription();
        related[3].name = "CurveIn3DSampler";
        related[3].controller = soam.sampling.CurveIn3DSampler.class;
        related[4] = new ClassDescription();
        related[4].name = "SurfaceIn3DSampler";
        related[4].controller = soam.sampling.SurfaceIn3DSampler.class;
        related[5] = new ClassDescription();
        related[5].name = "SurfaceInNDSampler";
        related[5].controller = soam.sampling.SurfaceInNDSampler.class;
        related[6] = new ClassDescription();
        related[6].name = "ThresholdImageSampler";
        related[6].controller = soam.sampling.ThresholdImageSampler.class;
        description.related = related;
        options.add(description);

        description = new ClassDescription();
        description.name = "SOAM : 1-manifold";
        description.controller = soam.algorithms.SOAM1d.class;
        description.viewer = soam.views.AlgorithmConsole.class;
        related = new ClassDescription[3];
        related[0] = new ClassDescription();
        related[0].name = "ThresholdImageSampler";
        related[0].controller = soam.sampling.ThresholdImageSampler.class;
        related[1] = new ClassDescription();
        related[1].name = "CurveIn3DSampler";
        related[1].controller = soam.sampling.CurveIn3DSampler.class;
        related[2] = new ClassDescription();
        related[2].name = "PlySampler";
        related[2].controller = soam.sampling.PLYSampler.class;
        description.related = related;
        options.add(description);

        description = new ClassDescription();
        description.name = "SOAM : 2-manifold";
        description.controller = soam.algorithms.SOAM2d.class;
        description.viewer = soam.views.AlgorithmConsole.class;
        related = new ClassDescription[10];
        related[0] = new ClassDescription();
        related[0].name = "GrayscaleImageSampler";
        related[0].controller = soam.sampling.GrayscaleImageSampler.class;
        related[1] = new ClassDescription();
        related[1].name = "ImageSampler";
        related[1].controller = soam.sampling.ImageSampler.class;
        related[2] = new ClassDescription();
        related[2].name = "PLYSampler";
        related[2].controller = soam.sampling.PLYSampler.class;
        related[3] = new ClassDescription();
        related[3].name = "CurveIn3DSampler";
        related[3].controller = soam.sampling.CurveIn3DSampler.class;
        related[4] = new ClassDescription();
        related[4].name = "SurfaceIn3DSampler";
        related[4].controller = soam.sampling.SurfaceIn3DSampler.class;
        related[5] = new ClassDescription();
        related[5].name = "SurfaceInNDSampler";
        related[5].controller = soam.sampling.SurfaceInNDSampler.class;
        related[6] = new ClassDescription();
        related[6].name = "MolecularSurfaceSampler";
        related[6].controller = soam.sampling.MolecularSurfaceSampler.class;
        related[7] = new ClassDescription();
        related[7].name = "TOFSequenceSampler";
        related[7].controller = soam.sampling.TOFSequenceSampler.class;
        related[8] = new ClassDescription();
        related[8].name = "PLYSequenceSampler";
        related[8].controller = soam.sampling.PLYSequenceSampler.class;
        related[9] = new ClassDescription();
        related[9].name = "ExcludedRegionSpheroConesSampler";
        related[9].controller = soam.sampling.ExcludedRegionSpheroConesSampler.class;
        description.related = related;
        options.add(description);

        // If needed, just change this section - END

        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {

                SOAMLauncher launcher = new SOAMLauncher(options);

                // Make it appear in the middle of the screen
                Dimension screenSize = Toolkit.getDefaultToolkit()
                        .getScreenSize();
                Dimension frameSize = launcher.getSize();
                if (frameSize.height > screenSize.height)
                    frameSize.height = screenSize.height;
                if (frameSize.width > screenSize.width)
                    frameSize.width = screenSize.width;
                launcher.setLocation((screenSize.width - frameSize.width) / 2,
                        (screenSize.height - frameSize.height) / 2);

                launcher.setVisible(true);
            }
        });

    }

    public SOAMLauncher(ArrayList<ClassDescription> options) {

        int prototypeLength = 0;

        String[] algorithms = new String[options.size()];
        for (int i = 0; i < options.size(); i++) {
            algorithms[i] = options.get(i).name;
        }

        algorithmLabel = new JLabel("Algorithm:");
        samplerLabel = new JLabel("Sampler:");

        algorithmChooser = new JComboBox();
        for (int i = 0; i < options.size(); i++) {
            algorithmChooser.addItem(options.get(i).name);
            if (options.get(i).name.length() > prototypeLength) {
                prototypeLength = options.get(i).name.length();
            }
            for (int j = 0; j < options.get(i).related.length; j++) {
                if (options.get(i).related[j].name.length() > prototypeLength) {
                    prototypeLength = options.get(i).related[j].name.length();
                }
            }
        }
        
        algorithmChooser.setPrototypeDisplayValue(repeat('M', prototypeLength));

        algorithmChooser.addActionListener(this);

        samplerChooser = new JComboBox();
        for (int i = 0; i < options.get(0).related.length; i++) {
            samplerChooser.addItem(options.get(0).related[i].name);
        }

        launchButton = new JButton("Launch");
        launchButton.addActionListener(this);

        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);

        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);

        layout.setHorizontalGroup(layout.createSequentialGroup().addGroup(
                layout.createParallelGroup(LEADING)
                        .addComponent(algorithmLabel)
                        .addComponent(samplerLabel)).addGroup(
                layout.createParallelGroup(TRAILING).addComponent(
                        algorithmChooser).addComponent(samplerChooser)
                        .addComponent(launchButton)));

        layout.setVerticalGroup(layout.createSequentialGroup().addGroup(
                layout.createParallelGroup(BASELINE).addComponent(
                        algorithmLabel).addComponent(algorithmChooser))
                .addGroup(
                        layout.createParallelGroup(BASELINE).addComponent(
                                samplerLabel).addComponent(samplerChooser))
                .addGroup(
                        layout.createParallelGroup(BASELINE).addComponent(
                                launchButton)));

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setResizable(false);
        setTitle("soam Launcher");
        pack();
    }

    @SuppressWarnings("unchecked")
    public void actionPerformed(ActionEvent e) {

        if (e.getSource() == algorithmChooser) {
            int algorithm = algorithmChooser.getSelectedIndex();
            samplerChooser.removeAllItems();
            for (int i = 0; i < options.get(algorithm).related.length; i++) {
                samplerChooser.addItem(options.get(algorithm).related[i].name);
            }
        } else if (e.getSource() == launchButton) {

            launchButton.setEnabled(false);
            Cursor hourglassCursor = new Cursor(Cursor.WAIT_CURSOR);
            setCursor(hourglassCursor);

            int algorithm = algorithmChooser.getSelectedIndex();
            int sampler = samplerChooser.getSelectedIndex();

            Constructor[] consts = options.get(algorithm).viewer
                    .getDeclaredConstructors();

            if (consts.length != 1) {
                throw new RuntimeException("Viewer class: "
                        + options.get(algorithm).viewer.getName()
                        + " does not declare an appropriate constructor");
            }

            try {
                
                consts[0].setAccessible(true);
                consts[0].newInstance(options.get(algorithm).controller,
                        options.get(algorithm).related[sampler].controller);
                
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "It did NOT work!",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }

            launchButton.setEnabled(true);
            Cursor normalCursor = new Cursor(Cursor.DEFAULT_CURSOR);
            setCursor(normalCursor);
        }
    }

    private static String repeat(char ch, int times) {

        // Ugly, but will be used only once - at startup
        String tmp = new String();
        for (int j = 0; j < times; j++) {
            tmp = tmp + ch;
        }
        return tmp;

    }
}

class ClassDescription {

    String name;
    Class<?> controller;
    Class<?> viewer;
    ClassDescription[] related;

}
