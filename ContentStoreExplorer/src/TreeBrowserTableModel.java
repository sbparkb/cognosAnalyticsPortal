/** 
Licensed Materials - Property of IBM

IBM Cognos Products: DOCS

(C) Copyright IBM Corp. 2005

US Government Users Restricted Rights - Use, duplication or disclosure restricted by GSA ADP Schedule Contract with
IBM Corp.
*/
/**
 * TreeBrowserTableModel.java
 *
 * Copyright (C) 2005 Cognos ULC, an IBM Company. All rights reserved.
 * Cognos (R) is a trademark of Cognos ULC, (formerly Cognos Incorporated).
 */

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;

class TreeBrowserTableModel extends AbstractTableModel
{
	private String[] columnNames = { "Name", "Type", "SearchPath" };
	private ArrayList data = new ArrayList();

	public int getColumnCount()
	{
		return columnNames.length;
	}

	public int getRowCount()
	{
		return data.size();
	}

	public String getColumnName(int col)
	{
		return columnNames[col];
	}

	public Object getValueAt(int row, int col)
	{
		if (col == 0)
		{
			return ((TreeBrowserNode)data.get(row)).getCMObject().getDefaultName().getValue();
		}
		if (col == 1)
		{
			return ((TreeBrowserNode)data.get(row)).getCMObject().getObjectClass().getValue();
		}
		if (col == 2)
		{
			return ((TreeBrowserNode)data.get(row)).getCMObject().getSearchPath().getValue();
		}
		return null;
	}

	public void setValueAt(Object tbnObject, int row, int column)
	{
		data.add(row, tbnObject);
	}

	public Class getColumnClass(int column)
	{
		return getValueAt(0, column).getClass();
	}

	public void add(Object tbnObject)
	{
		data.add(tbnObject);
	}

	public void clear()
	{
		data.clear();
	}
	
	public TreeBrowserNode getTbnForRow(int row)
	{
		return (TreeBrowserNode) data.get(row);
	}

}
