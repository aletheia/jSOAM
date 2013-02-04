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

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;

import java.io.File;
import java.text.DecimalFormat;
import java.util.Observer;
import java.util.Observable;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

import soam.Mesh;
import soam.algorithms.Algorithm;
import soam.sampling.Sampler;
import soam.utils.Executor;
import soam.utils.MeshIO;

public class AlgorithmConsole extends JFrame implements Observer,
	ActionListener, ItemListener {
    private static final long serialVersionUID = 1L;

    protected static String defaultPath = System.getProperty("user.dir");

    protected Class<?> algorithmClass;
    protected Class<?> samplerClass;

    protected Algorithm algorithm;
    protected Sampler sampler;
    protected Executor executor;

    protected boolean autoPaused = false;

    protected JMenuItem orientMeshMenuItem;
    protected JMenuItem exportSerMeshMenuItem;
    protected JMenuItem exportPlyMeshMenuItem;
    protected JMenuItem importSerMeshMenuItem;

    protected JButton startButton;
    protected JButton pauseButton;
    protected JButton resumeButton;
    protected JButton resetButton;

    protected JCheckBox imageCheckBox;
    protected JCheckBox voronoiCheckBox;
    protected JCheckBox view3DCheckBox;
    protected JCheckBox statisticsCheckBox;

    protected JLabel iterationsLabel;
    protected JFormattedTextField iterationsTextField;
    protected JLabel vertexLabel;
    protected JFormattedTextField vertexTextField;

    protected JButton algorithmParamsButton;
    protected JButton samplerParamsButton;
    protected JButton executorParamsButton;

    protected BufferedImage image;
    protected JFrame imageFrame;
    protected Component imageComponent;

    protected JFrame voronoiFrame;
    protected Component voronoiComponent;

    protected JFrame view3DFrame;
    protected Component view3DComponent;

    protected BeanEditor algorithmParametersEditor;
    protected BeanEditor samplerParametersEditor;
    protected BeanEditor executorParametersEditor;

    protected BeanViewer algorithmStatisticsViewer;

    public AlgorithmConsole(Class<?> algorithmClass, Class<?> samplerClass) {
	super(algorithmClass.getSimpleName() + " Console");

	this.algorithmClass = algorithmClass;
	this.samplerClass = samplerClass;

	try {
	    algorithm = (Algorithm) algorithmClass.newInstance();
	    sampler = (Sampler) samplerClass.newInstance();
	} catch (Exception e) {
	    e.printStackTrace();
	    return;
	}

	if (!algorithm.initialize(sampler)) {
	    return;
	}

	if (!sampler.initialize(algorithm)) {
	    return;
	}

	executor = new Executor(algorithm);
	executor.addObserver(this);

	reset();

	initializeGUI();

	setVisible(true);
    }

    protected void initializeGUI() {

	// Horizontal menu
	JMenuBar menuBar = new JMenuBar();
	JMenu menu = new JMenu("File");
	orientMeshMenuItem = new JMenuItem("Orient Mesh (2D Regular only)");
	exportSerMeshMenuItem = new JMenuItem("Export Serialized Mesh");
	exportPlyMeshMenuItem = new JMenuItem("Export Mesh as PLY");
	importSerMeshMenuItem = new JMenuItem("Import Serialized Mesh");

	ActionListener menuListener = new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		if (e.getSource() == orientMeshMenuItem) {
		    try {
			algorithm.orientMesh();
		    } catch (Exception ex) {
			JOptionPane.showMessageDialog(null,
				"Could not orient mesh: \"" + ex.getMessage()
					+ "\"", "Error",
				JOptionPane.ERROR_MESSAGE);
		    }
		} else if (e.getSource() == exportSerMeshMenuItem) {
		    while (true) {
			JFileChooser fc = new JFileChooser(defaultPath);
			fc.addChoosableFileFilter(new FileNameExtensionFilter(
				"Serialized Files", "ser"));
			fc.setDialogTitle(this.getClass().getName()
				+ " : Export Serialized Mesh");

			if (fc.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
			    File file = fc.getSelectedFile();
			    defaultPath = file.getAbsolutePath();

			    if (!MeshIO
				    .exportSerialized(algorithm.mesh(), file)) {

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
		} else if (e.getSource() == exportPlyMeshMenuItem) {
		    while (true) {
			JFileChooser fc = new JFileChooser(defaultPath);
			fc.addChoosableFileFilter(new FileNameExtensionFilter(
				"PLY Files", "ply"));
			fc.setDialogTitle(this.getClass().getName()
				+ " : Export Mesh as PLY");

			if (fc.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
			    File file = fc.getSelectedFile();
			    defaultPath = file.getAbsolutePath();

			    if (!MeshIO.exportPly(algorithm.mesh(), file)) {

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
		} else if (e.getSource() == importSerMeshMenuItem) {
		    while (true) {
			JFileChooser fc = new JFileChooser(defaultPath);
			fc.addChoosableFileFilter(new FileNameExtensionFilter(
				"Serialized Files", "ser"));
			fc.setDialogTitle(this.getClass().getName()
				+ " : Import Serialized Mesh");

			if (fc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
			    File file = fc.getSelectedFile();
			    defaultPath = file.getAbsolutePath();

			    Mesh mesh = MeshIO.importSerialized(file);
			    if (mesh == null) {

				JOptionPane.showMessageDialog(null,
					"Could not import from \""
						+ file.getPath() + "\"",
					"Error", JOptionPane.ERROR_MESSAGE);
				continue;
			    }

			    startButton.setEnabled(true);
			    pauseButton.setEnabled(false);
			    resumeButton.setEnabled(false);
			    resetButton.setEnabled(false);

			    executor.stop();
			    reset(mesh);
			    update(null, null);

			    break;
			} else {
			    break;
			}
		    }
		}
	    }
	};

	orientMeshMenuItem.addActionListener(menuListener);
	exportSerMeshMenuItem.addActionListener(menuListener);
	exportPlyMeshMenuItem.addActionListener(menuListener);
	importSerMeshMenuItem.addActionListener(menuListener);

	menu.add(orientMeshMenuItem);
	menu.addSeparator();
	menu.add(exportSerMeshMenuItem);
	menu.add(exportPlyMeshMenuItem);
	menu.addSeparator();
	menu.add(importSerMeshMenuItem);
	menuBar.add(menu);
	add(menuBar, BorderLayout.PAGE_START);

	JPanel mainPanel = new JPanel(new GridLayout(5, 1));

	// Description
	JLabel samplerlabel = new JLabel(sampler.toString());

	JPanel descriptionPanel = new JPanel(new FlowLayout());
	descriptionPanel.setBorder(BorderFactory
		.createTitledBorder("Sample Space"));

	descriptionPanel.add(samplerlabel);

	mainPanel.add("Center", descriptionPanel);

	// Control
	startButton = new JButton("Start");
	resumeButton = new JButton("Resume");
	pauseButton = new JButton("Pause");
	resetButton = new JButton("Reset");

	startButton.addActionListener(this);
	pauseButton.addActionListener(this);
	resumeButton.addActionListener(this);
	resetButton.addActionListener(this);

	startButton.setEnabled(true);
	pauseButton.setEnabled(false);
	resumeButton.setEnabled(false);
	resetButton.setEnabled(false);

	JPanel buttonPanel = new JPanel(new FlowLayout());
	buttonPanel.setBorder(BorderFactory.createTitledBorder("Control"));

	buttonPanel.add(startButton);
	buttonPanel.add(pauseButton);
	buttonPanel.add(resumeButton);
	buttonPanel.add(resetButton);

	mainPanel.add("Center", buttonPanel);

	// Views
	imageCheckBox = new JCheckBox("Image View", false);
	voronoiCheckBox = new JCheckBox("Voronoi View", false);
	view3DCheckBox = new JCheckBox("3D View", false);
	statisticsCheckBox = new JCheckBox("Statitstics", false);

	imageCheckBox.addItemListener(this);
	voronoiCheckBox.addItemListener(this);
	view3DCheckBox.addItemListener(this);
	statisticsCheckBox.addItemListener(this);

	JPanel viewsPanel = new JPanel(new FlowLayout());
	viewsPanel.setBorder(BorderFactory.createTitledBorder("Views"));

	if (sampler.getBufferedImage() != null) {
	    viewsPanel.add(imageCheckBox);
	}

	if (sampler.getDimensions() == 2) {
	    if (sampler.getBufferedImage() != null) {
		viewsPanel.add(voronoiCheckBox);
	    }
	} else {
	    viewsPanel.add(view3DCheckBox);
	}
	viewsPanel.add(statisticsCheckBox);

	mainPanel.add("Center", viewsPanel);

	// Execution
	iterationsLabel = new JLabel("Iterations:");
	iterationsTextField = new JFormattedTextField(new DecimalFormat());
	iterationsTextField.setValue(new Long(0));
	iterationsTextField.setColumns(12);
	iterationsTextField.setEditable(false);

	vertexLabel = new JLabel("Vertices:");
	vertexTextField = new JFormattedTextField(new DecimalFormat());
	vertexTextField.setValue(new Integer(0));
	vertexTextField.setColumns(6);
	vertexTextField.setEditable(false);

	JPanel executionPanel = new JPanel(new FlowLayout());
	executionPanel.setBorder(BorderFactory.createTitledBorder("Execution"));

	executionPanel.add(iterationsLabel);
	executionPanel.add(iterationsTextField);
	executionPanel.add(vertexLabel);
	executionPanel.add(vertexTextField);

	mainPanel.add("Center", executionPanel);

	// Parameters
	algorithmParamsButton = new JButton("Algorithm");
	samplerParamsButton = new JButton("Sampler");
	executorParamsButton = new JButton("Executor");

	algorithmParamsButton.addActionListener(this);
	samplerParamsButton.addActionListener(this);
	executorParamsButton.addActionListener(this);

	JPanel parametersPanel = new JPanel(new FlowLayout());
	parametersPanel.setBorder(BorderFactory
		.createTitledBorder("Parameters"));

	parametersPanel.add(algorithmParamsButton);
	parametersPanel.add(samplerParamsButton);
	parametersPanel.add(executorParamsButton);

	mainPanel.add("Center", parametersPanel);

	add(mainPanel, BorderLayout.CENTER);

	setResizable(false);
	setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
	pack();
    }

    protected void ensureImageFrame() {
	if (imageFrame != null) {
	    return;
	}

	image = sampler.getBufferedImage();

	imageComponent =
		new MeshImagePanel(algorithm, image, sampler.getDescription());

	imageFrame =
		((MeshImagePanel) imageComponent).newFrame(algorithm.getClass()
			.getSimpleName()
			+ " Image View");

	imageFrame
		.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
	imageFrame.setLocation(getLocation().x + getWidth(), getLocation().y);
    }

    protected void ensureView3DFrame() {
	if (view3DFrame != null) {
	    return;
	}

	view3DComponent = new Mesh3dPanel(algorithm, sampler.getDescription());

	view3DFrame =
		((Mesh3dPanel) view3DComponent).newFrame(algorithm.getClass()
			.getSimpleName()
			+ " 3D View");

	view3DFrame
		.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

	if (sampler.getBufferedImage() != null) {
	    view3DFrame.setLocation(getLocation().x, getLocation().y
		    + getHeight());
	} else {
	    view3DFrame.setLocation(getLocation().x + getWidth(),
		    getLocation().y);
	}
    }

    protected void ensureVoronoiFrame() {
	if (voronoiFrame != null) {
	    return;
	}

	voronoiComponent =
		new VoronoiPanel(algorithm, sampler.getBufferedImage()
			.getWidth(), sampler.getBufferedImage().getHeight());

	voronoiFrame =
		((VoronoiPanel) voronoiComponent).newFrame(algorithm.getClass()
			.getSimpleName()
			+ " Voronoi View");

	voronoiFrame
		.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
	voronoiFrame
		.setLocation(getLocation().x, getLocation().y + getHeight());
    }

    protected void ensureStatisticsViewerFrame() {
	if (algorithmStatisticsViewer != null) {
	    return;
	}

	algorithmStatisticsViewer = new BeanViewer(algorithm);

	algorithmStatisticsViewer
		.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
	algorithmStatisticsViewer.setLocation(getLocation().x + getWidth(),
		getLocation().y + getHeight());
    }

    public void reset() {
	algorithm.reset();
	sampler.reset();

	autoPaused = false;
    }

    public void reset(Mesh mesh) {
	algorithm.reset(mesh);
	sampler.reset();

	autoPaused = false;
    }

    @Override
    public void setVisible(boolean visible) {
	super.setVisible(visible);

	if (!visible && imageFrame != null) {
	    imageFrame.setVisible(visible);
	}
	if (!visible && view3DFrame != null) {
	    view3DFrame.setVisible(visible);
	}
	if (!visible && voronoiFrame != null) {
	    voronoiFrame.setVisible(visible);
	}
	if (!visible && algorithmParametersEditor != null) {
	    algorithmParametersEditor.setVisible(visible);
	}
	if (!visible && samplerParametersEditor != null) {
	    samplerParametersEditor.setVisible(visible);
	}
	if (!visible && executorParametersEditor != null) {
	    executorParametersEditor.setVisible(visible);
	}
	if (!visible && algorithmStatisticsViewer != null) {
	    algorithmStatisticsViewer.setVisible(visible);
	}

    }

    @Override
    public void dispose() {

	// Make sure that no zombies are left around ...
	executor.stop();

	if (imageFrame != null) {
	    imageFrame.dispose();
	}
	if (view3DFrame != null) {
	    view3DFrame.dispose();
	}
	if (voronoiFrame != null) {
	    voronoiFrame.dispose();
	}

	if (algorithmParametersEditor != null) {
	    algorithmParametersEditor.dispose();
	}
	if (samplerParametersEditor != null) {
	    samplerParametersEditor.dispose();
	}
	if (executorParametersEditor != null) {
	    executorParametersEditor.dispose();
	}

	if (algorithmStatisticsViewer != null) {
	    algorithmStatisticsViewer.dispose();
	}

	super.dispose();
    }

    public void update(Observable observable, Object obj) {
	iterationsTextField.setValue(new Long(algorithm.getTick()));
	vertexTextField
		.setValue(new Integer(algorithm.mesh().vertexlist.size()));

	if (imageFrame != null && imageFrame.isVisible()) {

	    ((MeshImagePanel) imageComponent).setImage(sampler
		    .getBufferedImage());
	    ((MeshImagePanel) imageComponent).setDescription(sampler
		    .getDescription());

	    imageFrame.repaint();
	    imageComponent.repaint();
	}

	if (view3DFrame != null && view3DFrame.isVisible()) {
	    ((Mesh3dPanel) view3DComponent).setDescription(sampler
		    .getDescription());

	    view3DFrame.repaint();
	    view3DComponent.repaint();
	}

	if (algorithmStatisticsViewer != null
		&& algorithmStatisticsViewer.isVisible()) {
	    algorithmStatisticsViewer.refresh();
	}

	// Avoid repainting the voronoi frame: it's just too much ..

	// Check if it is necessary to autoPause
	if (!autoPaused && executor.isAUTO_PAUSE() && algorithm.isConvergent()) {
	    autoPaused = true;

	    // Avoid duplicating code
	    this.actionPerformed(new ActionEvent(pauseButton, 0, null));
	}
    }

    public void actionPerformed(ActionEvent e) {

	if (e.getSource() == startButton) {
	    startButton.setEnabled(false);
	    pauseButton.setEnabled(true);
	    resumeButton.setEnabled(false);
	    resetButton.setEnabled(false);
	    orientMeshMenuItem.setEnabled(false);
	    exportSerMeshMenuItem.setEnabled(false);
	    exportPlyMeshMenuItem.setEnabled(false);
	    importSerMeshMenuItem.setEnabled(false);

	    executor.start();

	} else if (e.getSource() == pauseButton) {
	    startButton.setEnabled(false);
	    pauseButton.setEnabled(false);
	    resumeButton.setEnabled(true);
	    resetButton.setEnabled(true);
	    orientMeshMenuItem.setEnabled(true);
	    exportSerMeshMenuItem.setEnabled(true);
	    exportPlyMeshMenuItem.setEnabled(true);
	    importSerMeshMenuItem.setEnabled(true);

	    executor.pause();

	} else if (e.getSource() == resumeButton) {
	    startButton.setEnabled(false);
	    pauseButton.setEnabled(true);
	    resumeButton.setEnabled(false);
	    resetButton.setEnabled(false);
	    orientMeshMenuItem.setEnabled(false);
	    exportSerMeshMenuItem.setEnabled(false);
	    exportPlyMeshMenuItem.setEnabled(false);
	    importSerMeshMenuItem.setEnabled(false);

	    executor.resume();

	} else if (e.getSource() == resetButton) {
	    startButton.setEnabled(true);
	    pauseButton.setEnabled(false);
	    resumeButton.setEnabled(false);
	    resetButton.setEnabled(false);

	    executor.stop();
	    reset();
	    update(null, null);

	} else if (e.getSource() == algorithmParamsButton) {
	    if (algorithmParametersEditor == null) {
		algorithmParametersEditor = new BeanEditor(algorithm);
		algorithmParametersEditor.setLocation(getLocation().x,
			getLocation().y + getHeight());
	    }
	    algorithmParametersEditor.setVisible(true);

	} else if (e.getSource() == samplerParamsButton) {
	    if (samplerParametersEditor == null) {
		samplerParametersEditor = new BeanEditor(sampler);
		samplerParametersEditor.setLocation(getLocation().x
			+ getWidth(), getLocation().y + getHeight());
	    }
	    samplerParametersEditor.setVisible(true);
	} else if (e.getSource() == executorParamsButton) {
	    if (executorParametersEditor == null) {
		executorParametersEditor = new BeanEditor(executor);
		executorParametersEditor.setLocation(getLocation().x
			+ getWidth(), getLocation().y + getHeight());
	    }
	    executorParametersEditor.setVisible(true);
	}
    }

    public void itemStateChanged(ItemEvent e) {

	if (e.getSource() == imageCheckBox) {
	    if (e.getStateChange() == ItemEvent.SELECTED) {
		ensureImageFrame();
		imageFrame.setVisible(true);
	    } else if (e.getStateChange() == ItemEvent.DESELECTED
		    && imageFrame != null) {
		imageFrame.setVisible(false);
	    }
	} else if (e.getSource() == view3DCheckBox) {
	    if (e.getStateChange() == ItemEvent.SELECTED) {
		ensureView3DFrame();
		view3DFrame.setVisible(true);
	    } else if (e.getStateChange() == ItemEvent.DESELECTED
		    && view3DFrame != null) {
		view3DFrame.setVisible(false);
	    }
	} else if (e.getSource() == voronoiCheckBox) {
	    if (e.getStateChange() == ItemEvent.SELECTED) {
		ensureVoronoiFrame();
		voronoiFrame.setVisible(true);
	    } else if (e.getStateChange() == ItemEvent.DESELECTED
		    && voronoiFrame != null) {
		voronoiFrame.setVisible(false);
	    }
	} else if (e.getSource() == statisticsCheckBox) {
	    if (e.getStateChange() == ItemEvent.SELECTED) {
		ensureStatisticsViewerFrame();
		algorithmStatisticsViewer.setVisible(true);
	    } else if (e.getStateChange() == ItemEvent.DESELECTED
		    && algorithmStatisticsViewer != null) {
		algorithmStatisticsViewer.setVisible(false);
	    }
	}
    }
}
