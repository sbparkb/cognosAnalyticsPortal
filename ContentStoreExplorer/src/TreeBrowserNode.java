/** 
Licensed Materials - Property of IBM

IBM Cognos Products: DOCS

(C) Copyright IBM Corp. 2005

US Government Users Restricted Rights - Use, duplication or disclosure restricted by GSA ADP Schedule Contract with
IBM Corp.
*/
/**
 * TreeBrowserNode.java
 *
 * Copyright (C) 2005 Cognos ULC, an IBM Company. All rights reserved.
 * Cognos (R) is a trademark of Cognos ULC, (formerly Cognos Incorporated).
 */

//import com.cognos.developer.schemas.bibus._3.*;
import javax.swing.ImageIcon;
import javax.swing.tree.DefaultMutableTreeNode;

import com.cognos.developer.schemas.bibus._3.BaseClass;
import com.cognos.developer.schemas.bibus._3.OrderEnum;
import com.cognos.developer.schemas.bibus._3.PropEnum;
import com.cognos.developer.schemas.bibus._3.QueryOptions;
import com.cognos.developer.schemas.bibus._3.SearchPathMultipleObject;
import com.cognos.developer.schemas.bibus._3.Sort;


//import org.xml.sax.SAXException;

class TreeBrowserNode extends BaseClass
{

    BaseClass myCMObject;
	BaseClass[] children;
	DefaultMutableTreeNode myContainer;
	
	ImageIcon myIcon;
	
	private boolean childrenPopulated = false;
	private boolean detailsPopulated = false;

	public TreeBrowserNode(String searchPath, CRNConnect connection)
	{
		
		SearchPathMultipleObject cmSearchPath = new SearchPathMultipleObject(searchPath);
		
		try
		{
			PropEnum[] properties =	{ PropEnum.defaultName,
										PropEnum.searchPath,
					PropEnum.objectClass,
					PropEnum.hasChildren };
			myCMObject = (connection.getCMService().query(
						cmSearchPath,
						properties,
						new Sort[] {},
						new QueryOptions()))[0];

			String appendString = "/*";
			if (searchPath.lastIndexOf("/") == (searchPath.length() - 1))
			{
				appendString = "*";
			}
			if (searchPath.lastIndexOf("*") == (searchPath.length() - 1))
			{
				appendString = "";
			}
			Sort nodeSortType = new Sort();
			Sort nodeSortName = new Sort();
		
			nodeSortType.setOrder(OrderEnum.ascending);
			nodeSortType.setPropName(PropEnum.objectClass);
			nodeSortName.setOrder(OrderEnum.ascending);
			nodeSortName.setPropName(PropEnum.defaultName);
			Sort[] nodeSorts = new Sort[] {nodeSortType, nodeSortName};

			if(myCMObject.getHasChildren().isValue())
			{
				cmSearchPath.set_value(searchPath + appendString);
				children =
				connection.getCMService().query(
					cmSearchPath,
					properties,
					nodeSorts,
					new QueryOptions());
			}
		}
		//catch (SAXException saxEx)
		//{}
		catch (java.rmi.RemoteException remoteEx)
		{}

	}
	
	public TreeBrowserNode(BaseClass seedForNode, CRNConnect connection)
	{
		myCMObject = seedForNode;
		String searchPath = seedForNode.getSearchPath().getValue();
		SearchPathMultipleObject cmSearchPath = new SearchPathMultipleObject(searchPath);
		PropEnum[] properties =	
		{	PropEnum.defaultName,
			PropEnum.searchPath,
			PropEnum.objectClass,
			PropEnum.hasChildren 
		};
		Sort nodeSortType = new Sort();
		Sort nodeSortName = new Sort();
		
		nodeSortType.setOrder(OrderEnum.ascending);
		nodeSortType.setPropName(PropEnum.objectClass);
		nodeSortName.setOrder(OrderEnum.ascending);
		nodeSortName.setPropName(PropEnum.defaultName);
		Sort[] nodeSorts = new Sort[] {nodeSortType, nodeSortName};
		
		String appendString = "/*";
		if (searchPath.lastIndexOf("/") == (searchPath.length() - 1))
		{
			appendString = "*";
		}
		if (searchPath.lastIndexOf("*") == (searchPath.length() - 1))
		{
			appendString = "";
		}
		try
		{
			if (myCMObject.getHasChildren().isValue())
			{
				cmSearchPath.set_value(cmSearchPath.get_value() + appendString);
				children =
				connection.getCMService().query(
					cmSearchPath,
					properties,
					nodeSorts,
					new QueryOptions());
			}
		}
		catch (java.rmi.RemoteException remoteEx)
		{
		}
	}

	public void setChildrenPopulated(boolean bpop)
	{
		childrenPopulated = bpop;
	}

	public boolean getChildrenPopulated()
	{
		return childrenPopulated;
	}
	
	public void setDetailsPopulated(boolean bpop)
	{
		detailsPopulated = bpop;
	}
	
	public boolean getDetailsPopulated()
	{
		return detailsPopulated;
	}

	public TreeBrowserNode getChild(int index, CRNConnect connection)
	{
		TreeBrowserNode returnNode = null;
		if (children != null)
		{
			if (!getChildrenPopulated())
			{
				children[index] =
					new TreeBrowserNode(children[index], connection);
			}
			returnNode = (TreeBrowserNode)children[index];
		}

		return returnNode;
	}

	public int getNumChildren()
	{
		if (children == null)
		{
			return 0;
		}
		return children.length;
	}

	public String toString()
	{
		if (myCMObject.getDefaultName() == null)
		{
			return "NULL";
		}
		return myCMObject.getDefaultName().getValue();
	}
	
	public BaseClass getCMObject()
	{
		return myCMObject;
	}
	
	public void setCMObject(BaseClass newObject)
	{
		myCMObject = newObject;
	}
	
	public DefaultMutableTreeNode getContainer()
	{
		return myContainer;
	}

	public void setContainer(DefaultMutableTreeNode treeNode)
	{
		myContainer = treeNode;
		myContainer.setUserObject(this);
	}
	
	public void setIcon(ImageIcon icon)
	{
		myIcon = icon;
	}
	
	public ImageIcon getImageIcon()
	{
		return myIcon;
	}
}
