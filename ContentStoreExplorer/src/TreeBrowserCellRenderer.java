/** 
Licensed Materials - Property of IBM

IBM Cognos Products: DOCS

(C) Copyright IBM Corp. 2005

US Government Users Restricted Rights - Use, duplication or disclosure restricted by GSA ADP Schedule Contract with
IBM Corp.
*/
/**
 * TreeBrowserCellRenderer.java
 *
 * Copyright (C) 2005 Cognos ULC, an IBM Company. All rights reserved.
 * Cognos (R) is a trademark of Cognos ULC, (formerly Cognos Incorporated).
 */

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.ImageIcon;
import java.awt.Component;

	class TreeBrowserCellRenderer extends DefaultTreeCellRenderer
	{

		public Component getTreeCellRendererComponent(
			JTree tree,
			Object value,
			boolean sel,
			boolean expanded,
			boolean leaf,
			int row,
			boolean hasFocus)
		{

			super.getTreeCellRendererComponent(
				tree,
				value,
				sel,
				expanded,
				leaf,
				row,
				hasFocus);

			String iconPath =
				getIconPath(
					((TreeBrowserNode) ((DefaultMutableTreeNode)value)
						.getUserObject())
						.getCMObject()
						.getObjectClass()
						.getValue()
						.toString());

			ImageIcon currentImageIcon = new ImageIcon(iconPath);
			java.io.File iconFile = new java.io.File(iconPath);
			
			if (iconFile.exists())
			{
				setIcon(currentImageIcon);
			}
			else
			{
				setIcon(getDefaultLeafIcon());
			}

			return this;
		}


		public String getIconPath(String className)
		{
			String iconPathPrefix =
				"../../../webcontent/ps/portal/images/icon_";
			String iconPathVarfix = "";
			String iconPathSuffix = ".gif";
			if (className.compareTo("configuration") == 0)
			{
				iconPathVarfix = "_folder";
			}
			if (className.compareTo("root") == 0)
			{
				className = "folder";
			}
			if (className.compareTo("directory") == 0)
			{
				className = "folder";
			}
			if (className.compareTo("importDeploymentFolder") == 0)
			{
				className = "deployment";
			}
			if (className.compareTo("exportDeploymentFolder") == 0)
			{
				className = "deployment";
			}
			if (className.compareTo("capability") == 0)
			{
				className = "folder";
			}
			if (className.compareTo("content") == 0)
			{
				className = "folder";
			}
			if (className.compareTo("securedFunction") == 0)
			{
				className = "secured_function";
			}
			if (className.compareTo("securedFeature") == 0)
			{
				className = "secured_feature";
			}
			if (className.compareTo("dataSource") == 0)
			{
				className = "data_source";
			}
			if (className.compareTo("group") == 0)
			{
				className = "user_group";
			}
			if (className.compareTo("role") == 0)
			{
				className = "user_role";
			}
			if (className.compareTo("account") == 0)
			{
				className = "user";
			}
			if (className.compareTo("dataSourceConnection") == 0)
			{
				className = "data_source_connection";
			}
			if (className.compareTo("dataSourceSignon") == 0)
			{
				className = "signon";
			}
			if ( (className.compareTo("logService") == 0)
				|| (className.compareTo("reportService") == 0)
				|| (className.compareTo("presentationService") == 0)
				|| (className.compareTo("jobAndScheduleMonitoringService") == 0)
				|| (className.compareTo("contentManagerService") == 0)
				|| (className.compareTo("batchReportService") == 0)
			   )
			{
				className = "service";
			}
			if (className.compareTo("jobDefinition") == 0)
			{
				className = "job";
			}

			
			return (iconPathPrefix + className + iconPathVarfix + iconPathSuffix);
		}
	}
