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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.BeanDescriptor;
import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyDescriptor;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.AbstractTableModel;

class BeanViewer extends JFrame implements PropertyChangeListener {
    private static final long serialVersionUID = 1L;

    protected static String defaultPath = System.getProperty("user.dir");

    protected Object bean;
    protected JTable propertyJTable;
    protected PropertyTableData tableData;

    protected boolean COLLECT_VALUES = false;
    protected ArrayList<String[]> collectedValues = new ArrayList<String[]>();

    protected JMenuItem collectValuesMenuItem;
    protected JMenuItem exportCSVMenuItem;
    protected JMenuItem exportMenuItem;

    public BeanViewer(Object bean) {

	this.bean = bean;

	getContentPane().setLayout(new BorderLayout());

	// Horizontal menu
	JMenuBar menuBar = new JMenuBar();
	JMenu menu = new JMenu("Options");
	collectValuesMenuItem = new JCheckBoxMenuItem("Collect values");
	collectValuesMenuItem.setSelected(false);
	exportCSVMenuItem = new JMenuItem("Export collected values as CSV");
	exportMenuItem = new JMenuItem("Export values as Properties");

	ActionListener menuListener = new ActionListener() {
	    public void actionPerformed(ActionEvent e) {

		if (e.getSource() == collectValuesMenuItem) {
		    setCOLLECT_VALUES(collectValuesMenuItem.isSelected());

		} else if (e.getSource() == exportCSVMenuItem) {
		    while (true) {
			JFileChooser fc = new JFileChooser(defaultPath);
			fc.addChoosableFileFilter(new FileNameExtensionFilter(
				"CSV Files", "csv"));
			fc.setDialogTitle(this.getClass().getName()
				+ " : Export values as CSV");

			if (fc.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
			    File file = fc.getSelectedFile();
			    defaultPath = file.getAbsolutePath();

			    if (!exportValuesAsCSV(file)) {
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

		} else if (e.getSource() == exportMenuItem) {
		    while (true) {
			JFileChooser fc = new JFileChooser(defaultPath);
			fc.addChoosableFileFilter(new FileNameExtensionFilter(
				"Properties Files", "properties"));
			fc.setDialogTitle(this.getClass().getName()
				+ " : Export to Properties");

			if (fc.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
			    File file = fc.getSelectedFile();
			    defaultPath = file.getAbsolutePath();

			    if (!exportAsProperties(file)) {

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

	collectValuesMenuItem.addActionListener(menuListener);
	exportCSVMenuItem.addActionListener(menuListener);
	exportMenuItem.addActionListener(menuListener);

	menu.add(collectValuesMenuItem);
	menu.addSeparator();
	menu.add(exportCSVMenuItem);
	menu.add(exportMenuItem);
	menuBar.add(menu);
	add(menuBar, BorderLayout.PAGE_START);

	tableData = new PropertyTableData(bean);
	propertyJTable = new JTable(tableData);
	tableData.addTableModelListener(propertyJTable);

	JScrollPane ps = new JScrollPane();
	ps.getViewport().add(propertyJTable);
	getContentPane().add(ps, BorderLayout.CENTER);

	setDefaultCloseOperation(HIDE_ON_CLOSE);

	pack();
    }

    public void propertyChange(PropertyChangeEvent evt) {

	tableData.setProperty(evt.getPropertyName(), evt.getNewValue());

    }

    public void refresh() {

	tableData.refresh();
	if (COLLECT_VALUES) {
	    collectedValues.add(tableData.propertyValues());
	}
	tableData.fireTableAllRowsUpdated();
    }

    public boolean isCOLLECT_VALUES() {
	return COLLECT_VALUES;
    }

    public void setCOLLECT_VALUES(boolean collect_values) {
	COLLECT_VALUES = collect_values;

	if (COLLECT_VALUES) {
	    collectedValues.clear();
	    collectedValues.add(tableData.propertyNames());
	}
    }

    public boolean exportValuesAsCSV(File file) {

	try {
	    FileWriter writer = new FileWriter(file);

	    for (int i = 0; i < collectedValues.size(); i++) {
		String[] row = collectedValues.get(i);
		for (int j = 0; j < row.length - 1; j++) {
		    writer.write(row[j]);
		    writer.write(',');
		}
		writer.write(row[row.length - 1]);
		writer.write('\n');
	    }

	    writer.flush();
	    writer.close();

	} catch (IOException e) {
	    e.printStackTrace();
	    return false;
	}

	return true;
    }

    public boolean exportAsProperties(File file) {
	refresh();
	Properties tmp = tableData.asProperties();

	try {
	    FileWriter writer = new FileWriter(file);
	    tmp.store(writer, bean.getClass().toString());
	    writer.flush();
	    writer.close();

	} catch (IOException e) {
	    return false;
	}

	return true;
    }

    @SuppressWarnings("unchecked")
    class PropertyTableData extends AbstractTableModel {
	private static final long serialVersionUID = 1L;

	protected String[][] properties;
	protected int numProps = 0;
	protected Vector descriptors;

	public PropertyTableData(Object bean) {

	    try {

		BeanInfo info;
		if (bean instanceof Component) {
		    // Get declared properties only
		    info =
			    Introspector.getBeanInfo(bean.getClass(), bean
				    .getClass().getSuperclass());
		} else {
		    info =
			    Introspector.getBeanInfo(bean.getClass(),
				    Object.class);
		}
		BeanDescriptor descr = info.getBeanDescriptor();

		setTitle(descr.getName() + " Statistics");

		PropertyDescriptor[] props = info.getPropertyDescriptors();

		numProps = props.length;
		descriptors = new Vector(numProps);

		for (int k = 0; k < numProps; k++) {

		    // Skip read-write properties
		    if (props[k].getReadMethod() == null
			    || props[k].getWriteMethod() != null) {
			continue;
		    }

		    String name = props[k].getDisplayName();
		    boolean added = false;

		    for (int i = 0; i < descriptors.size(); i++) {
			String str =
				((PropertyDescriptor) descriptors.elementAt(i))
					.getDisplayName();
			if (name.compareToIgnoreCase(str) < 0) {
			    descriptors.insertElementAt(props[k], i);
			    added = true;
			    break;
			}
		    }

		    if (!added)
			descriptors.addElement(props[k]);
		}

		numProps = descriptors.size();

		properties = new String[numProps][2];

		for (int k = 0; k < numProps; k++) {
		    PropertyDescriptor prop =
			    (PropertyDescriptor) descriptors.elementAt(k);

		    properties[k][0] = prop.getDisplayName();
		    Method readMethod = prop.getReadMethod();

		    if (readMethod != null
			    && readMethod.getParameterTypes().length == 0) {
			Object value = readMethod.invoke(bean, (Object[]) null);
			properties[k][1] = objToString(value);
		    } else {
			properties[k][1] = "error";
		    }
		}

	    } catch (Exception ex) {
		ex.printStackTrace();
		JOptionPane
			.showMessageDialog(BeanViewer.this, "Error: "
				+ ex.toString(), "Warning",
				JOptionPane.WARNING_MESSAGE);
	    }
	}

	public void refresh() {
	    try {
		for (int k = 0; k < numProps; k++) {
		    PropertyDescriptor prop =
			    (PropertyDescriptor) descriptors.elementAt(k);

		    properties[k][0] = prop.getDisplayName();
		    Method readMethod = prop.getReadMethod();

		    if (readMethod != null
			    && readMethod.getParameterTypes().length == 0) {
			Object value = readMethod.invoke(bean, (Object[]) null);
			properties[k][1] = objToString(value);
		    } else {
			properties[k][1] = "error";
		    }
		}

	    } catch (Exception ex) {
		ex.printStackTrace();
	    }
	}

	public Properties asProperties() {
	    Properties tmp = new Properties();
	    for (int k = 0; k < numProps; k++) {
		tmp.put(properties[k][0], properties[k][1]);
	    }

	    return tmp;
	}

	public void fireTableAllRowsUpdated() {
	    fireTableRowsUpdated(0, numProps - 1);
	}

	public void setProperty(String name, Object value) {

	    for (int k = 0; k < numProps; k++)
		if (name.equals(properties[k][0])) {
		    properties[k][1] = objToString(value);
		    propertyJTable.tableChanged(new TableModelEvent(this, k));
		    propertyJTable.repaint();

		    break;
		}
	}

	public int getRowCount() {
	    return numProps;
	}

	public int getColumnCount() {
	    return 2;
	}

	@Override
	public String getColumnName(int nCol) {
	    return nCol == 0 ? "Property" : "Value";
	}

	@Override
	public boolean isCellEditable(int nRow, int nCol) {
	    // In the viewer all cells are not editable
	    return false;
	}

	public Object getValueAt(int nRow, int nCol) {
	    if (nRow < 0 || nRow >= getRowCount())
		return "";

	    switch (nCol) {

	    case 0:
		return properties[nRow][0];

	    case 1:
		return properties[nRow][1];

	    }

	    return "";
	}

	public String objToString(Object value) {
	    if (value == null)
		return "null";

	    if (value instanceof Dimension) {
		Dimension dim = (Dimension) value;
		return "" + dim.width + "," + dim.height;
	    } else if (value instanceof Insets) {
		Insets ins = (Insets) value;
		return "" + ins.left + "," + ins.top + "," + ins.right + ","
			+ ins.bottom;
	    } else if (value instanceof Rectangle) {
		Rectangle rc = (Rectangle) value;
		return "" + rc.x + "," + rc.y + "," + rc.width + ","
			+ rc.height;
	    } else if (value instanceof Color) {
		Color col = (Color) value;
		return "" + col.getRed() + "," + col.getGreen() + ","
			+ col.getBlue();
	    }
	    return value.toString();
	}

	public Object stringToObj(String str, Class<?> cls) {
	    try {
		if (str == null)
		    return null;

		String name = cls.getName();
		if (name.equals("java.lang.String"))
		    return str;
		else if (name.equals("int"))
		    return new Integer(str);
		else if (name.equals("long"))
		    return new Long(str);
		else if (name.equals("float"))
		    return new Float(str);
		else if (name.equals("double"))
		    return new Double(str);
		else if (name.equals("boolean"))
		    return new Boolean(str);
		else if (name.equals("java.awt.Dimension")) {
		    int[] i = strToInts(str);
		    return new Dimension(i[0], i[1]);
		} else if (name.equals("java.awt.Point")) {
		    int[] i = strToInts(str);
		    return new Point(i[0], i[1]);
		} else if (name.equals("java.awt.Insets")) {
		    int[] i = strToInts(str);
		    return new Insets(i[0], i[1], i[2], i[3]);
		} else if (name.equals("java.awt.Rectangle")) {
		    int[] i = strToInts(str);
		    return new Rectangle(i[0], i[1], i[2], i[3]);
		} else if (name.equals("java.awt.Color")) {
		    int[] i = strToInts(str);
		    return new Color(i[0], i[1], i[2]);
		}

		return null; // not supported

	    } catch (Exception ex) {
		return null;
	    }
	}

	public int[] strToInts(String str) throws Exception {

	    int[] i = new int[4];
	    StringTokenizer tokenizer = new StringTokenizer(str, ",");

	    for (int k = 0; k < i.length && tokenizer.hasMoreTokens(); k++)
		i[k] = Integer.parseInt(tokenizer.nextToken());

	    return i;
	}

	public String[] propertyNames() {
	    String[] tmp = new String[numProps];
	    for (int i = 0; i < numProps; i++) {
		tmp[i] = properties[i][0];
	    }

	    return tmp;
	}

	public String[] propertyValues() {
	    String[] tmp = new String[numProps];
	    for (int i = 0; i < numProps; i++) {
		tmp[i] = properties[i][1];
	    }

	    return tmp;
	}
    }
}
