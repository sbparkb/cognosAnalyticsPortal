/** 
Licensed Materials - Property of IBM

IBM Cognos Products: DOCS

(C) Copyright IBM Corp. 2005, 2006

US Government Users Restricted Rights - Use, duplication or disclosure restricted by GSA ADP Schedule Contract with
IBM Corp.
*/
/**
 * TreeBrowserNodeDetailTableModel.java
 *
 * Copyright (C) 2006 Cognos ULC, an IBM Company. All rights reserved.
 * Cognos (R) is a trademark of Cognos ULC, (formerly Cognos Incorporated).
 *
 */

import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.tree.TreePath;
import java.util.ArrayList;
import java.lang.reflect.Method;
import java.lang.reflect.Array;

class TreeBrowserNodeDetailTableModel
	extends AbstractTableModel
	implements java.awt.event.ActionListener
{
	//	private Object myObject;
	private String[] columnNames = { "Property Name", "Property Value" };
	private ArrayList data = new ArrayList();
	private ArrayList navigationPoints = new ArrayList();
	private int navigationIndex = -1;

	public TreeBrowserNodeDetailTableModel(Object root)
	{
		//super(root);
		navigationPoints.add(root);
		navigationIndex++;
		renderNavigationChange();

	}

	public Object getChild(Object parentObject, int index)
	{
		return getRowObject(index);
	}

	public int getColumnCount()
	{
		return columnNames.length;
	}

	public boolean isLeaf(Object parentObject)
	{
		return true;
	}

	public int getIndexOfChild(Object parent, Object child)
	{
		for(int i = 0; i < getRowCount(); i++)
		{
			if (child == getRowObject(i))
			{
				return i;
			}
		}
		return -1;
	}

	public void valueForPathChanged(TreePath path, Object newValue)
	{
	}

	public int getChildCount(Object parentObject)
	{
		return getRowCount();
	}

	public int getRowCount()
	{
		return data.size();
	}

	public Object getValueAt(Object parent, int index)
	{
		return getChild(parent, index);
	}

	public String getColumnName(int col)
	{
		return columnNames[col];
	}

	public Object getRowObject(int row)
	{
		return data.get(row);
	}

	public Object getValueAt(int row, int col)
	{
		if (col == 0)
		{
			try
			{
				return propertyFromGetMethod(((Method)data.get(row)).getName());
			}
			catch (ClassCastException ccEx)
			{}
			return data.get(row);
		}
		if (col == 1)
		{
			try
			{
				Object propertyValue =
					((Method)data.get(row)).invoke(
						navigationPoints.get(navigationIndex),
						new Object[] {});
				if (propertyValue != null)
				{

					Class propertyValueClass = propertyValue.getClass();
					Method[] propertyValueMethods =
						propertyValueClass.getMethods();
					for (int i = 0; i < propertyValueMethods.length; i++)
					{
						if (propertyValueMethods[i]
							.getName()
							.compareTo("getValue")
							== 0)
						{
							return propertyValueMethods[i].invoke(
								propertyValue,
								new Object[] {});
						}
					}
				}
				return propertyValue;

			}
			catch (java.lang.reflect.InvocationTargetException itEx)
			{}
			catch (java.lang.IllegalAccessException iaEx)
			{}
			catch (ClassCastException ccEx)
			{
				return data.get(row);
			}
		}
		return null;
	}

	public void setValueAt(Object o, int row, int col)
	{
		data.add(row, o);
	}

	public Class getColumnClass(int c)
	{
		return getValueAt(0, c).getClass();
	}

	public void add(Object o)
	{
		data.add(o);
	}

	public void clear()
	{
		data.clear();
	}

	public static String propertyFromGetMethod(String methodName)
	{
		String propertyName = null;
		if (methodName.indexOf("get") == 0)
		{
			propertyName =
				methodName.substring(3, 4).toLowerCase()
					+ methodName.substring(4);
		}
		return propertyName;
	}

	public static String propertyFromSetMethod(String methodName)
	{
		String propertyName = null;
		if (methodName.indexOf("set") == 0)
		{
			propertyName =
				methodName.substring(3, 4).toLowerCase()
					+ methodName.substring(4);
		}
		return propertyName;
	}

	public void incrementNavigation(JTable navTable)
	{
		if (navTable.getSelectedRow() < 0)
		{
			return;
		}

		navigationPoints.add( getValueAt(navTable.getSelectedRow(), 1));
		navigationIndex++;
		renderNavigationChange();
		navTable.revalidate();
		navTable.repaint();
	}

	public void renderNavigationChange()
	{
		if(navigationPoints.get(navigationIndex).getClass().isArray())
		{
			if (Array.getLength(navigationPoints.get(navigationIndex)) <= 0)
			{
				return;
			}
			clear();
			for ( int i = 0; i < (Array.getLength(navigationPoints.get(navigationIndex))); i++ )
			{
				data.add( Array.get(navigationPoints.get(navigationIndex), i) );
			}
			return;
		}
		Method[] objectMethods = navigationPoints.get(navigationIndex).getClass().getMethods();
		if(objectMethods.length <= 0)
		{
			return;
		}
		clear();
		for(int i = 0; i < objectMethods.length; i++)
		{
			if (objectMethods[i].getName().indexOf("get") == 0)
			{
				if (objectMethods[i].getParameterTypes().length == 0)
				{
					data.add(objectMethods[i]);
				}
			}
		}
	}

	public void decrementNavigation(JTable navTable)
	{
		if (navigationIndex <= 0)
		{
			return;
		}

		navigationPoints.remove(navigationIndex);
		navigationIndex--;

		renderNavigationChange();
		navTable.revalidate();
		navTable.repaint();
	}

	/** Required by ActionListener */
	public void actionPerformed(java.awt.event.ActionEvent action)
	{
	}

	public Object getCurrentNode()
	{
		return navigationPoints.get(navigationIndex);
	}
}
