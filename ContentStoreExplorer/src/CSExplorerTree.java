/** 
Licensed Materials - Property of IBM

IBM Cognos Products: DOCS

(C) Copyright IBM Corp. 2005

US Government Users Restricted Rights - Use, duplication or disclosure restricted by GSA ADP Schedule Contract with
IBM Corp.
*/
/**
 * CSExplorerTree.java
 *
 * Copyright (C) 2005 Cognos ULC, an IBM Company. All rights reserved.
 * Cognos (R) is a trademark of Cognos ULC, (formerly Cognos Incorporated).
 */

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.lang.reflect.Method;
import java.util.HashMap;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import com.cognos.developer.schemas.bibus._3.BaseClass;
import com.cognos.developer.schemas.bibus._3.PropEnum;
import com.cognos.developer.schemas.bibus._3.QueryOptions;
import com.cognos.developer.schemas.bibus._3.SearchPathMultipleObject;
import com.cognos.developer.schemas.bibus._3.Sort;

public class CSExplorerTree
	extends JPanel
	implements
		TreeSelectionListener,
		TreeExpansionListener,
		java.awt.event.MouseListener,
		java.awt.event.ActionListener
{
	private JScrollPane detailsPane;
	private JSplitPane splitPane;
	private JScrollPane treeView;
	private JTable detailsTable;
	private JTable tableOfDetails;
	private JTree tree;
	private JTextField currentNode;
	private JButton forwardButton;
	private JButton backButton;
	private static String defaultRootSearchPath = "/";
	private static String DETAILS_MENU_STRING = "Details...";
	private CRNConnect connection;
	private TreeBrowserNodeDetailTableModel tableModelForDetails;

	public CSExplorerTree(CRNConnect connect)
	{
		super(new GridLayout(1, 0));

		connection = connect;

		//Create the nodes.
		DefaultMutableTreeNode top = new DefaultMutableTreeNode();
		TreeBrowserNode cmRootNode =
			new TreeBrowserNode(defaultRootSearchPath, connection);
		cmRootNode.setContainer(top);
		createNodes(top);

		for (int i = 0; i < top.getChildCount(); i++)
		{
			createNodes((DefaultMutableTreeNode)top.getChildAt(i));
			System.out.println("tree:" + top.getChildAt(i));
		}

		((TreeBrowserNode)top.getUserObject()).setChildrenPopulated(true);

		//Create a tree that allows one selection at a time.
		tree = new JTree(top);
		tree.getSelectionModel().setSelectionMode(
			TreeSelectionModel.SINGLE_TREE_SELECTION);

		//Listen for when the selection changes.
		tree.addTreeExpansionListener(this);
		tree.addTreeSelectionListener(this);
		tree.addMouseListener(this);
		tree.setCellRenderer(new TreeBrowserCellRenderer());

		//Create the scroll pane and add the tree to it. 
		treeView = new JScrollPane(tree);

		//Create the HTML viewing pane.
		detailsTable = new JTable(new TreeBrowserTableModel());
		detailsTable.addMouseListener(this);
		detailsTable.setShowGrid(false);

		//Create the output pane.
		detailsPane = new JScrollPane(detailsTable);

		//Add the scroll panes to a split pane.
		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		splitPane.setLeftComponent(treeView);
		splitPane.setRightComponent(detailsPane);

		Dimension minimumSize = new Dimension(200, 50);
		detailsPane.setMinimumSize(minimumSize);
		treeView.setMinimumSize(minimumSize);
		splitPane.setDividerLocation(.5);

		splitPane.setPreferredSize(new Dimension(700, 500));

		//Add the split pane to this panel.
		add(splitPane);

		//Set the root node as the currently selected node in the tree
		tree.setSelectionRow(0);
	}
	
	/** Required by MouseListener interface. */
	public void mousePressed(MouseEvent mEvent)
	{}
	public void mouseEntered(MouseEvent mEvent)
	{}
	public void mouseExited(MouseEvent mEvent)
	{}
	public void mouseReleased(MouseEvent mEvent)
	{}
	public void mouseClicked(MouseEvent mEvent)
	{
		if (javax.swing.SwingUtilities.isRightMouseButton(mEvent))
		{
			javax.swing.JMenuItem detailsItem =
				new javax.swing.JMenuItem(DETAILS_MENU_STRING);
			detailsItem.addActionListener(this);

			javax.swing.JPopupMenu dropDownMenu = new javax.swing.JPopupMenu();
			dropDownMenu.add(detailsItem);
			//dropDownMenu.add(exploreItem);
			dropDownMenu.show(
				mEvent.getComponent(),
				mEvent.getX(),
				mEvent.getY());
		}

		if (javax.swing.SwingUtilities.isLeftMouseButton(mEvent))
		{
			if (mEvent.getClickCount() > 1)
			{
				if (mEvent.getSource().getClass() == JTable.class)
				{
					JTable tableForDBLClick = (JTable)mEvent.getSource();
					TreeBrowserNode nodeForDBLClick =
						(
							(TreeBrowserTableModel)tableForDBLClick
								.getModel())
								.getTbnForRow(
							tableForDBLClick.getSelectedRow());
					exploreNode(nodeForDBLClick);
				}
			}
		}
	}

	public void exploreNode(TreeBrowserNode node)
	{
		tree.setSelectionPath(new TreePath((node.getContainer().getPath())));
	}

	public void treeCollapsed(TreeExpansionEvent teEvent)
	{}

	public void treeExpanded(TreeExpansionEvent teEvent)
	{
		this.getParent().setCursor(
			Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		DefaultMutableTreeNode currentExpandedNode =
			(DefaultMutableTreeNode)teEvent.getPath().getLastPathComponent();
		for (int i = 0; i < currentExpandedNode.getChildCount(); i++)
		{
			createNodes(
				(DefaultMutableTreeNode)currentExpandedNode.getChildAt(i));
		}
		this.getParent().setCursor(Cursor.getDefaultCursor());
	}

	/** Required by TreeSelectionListener interface. */
	public void valueChanged(TreeSelectionEvent e)
	{
		DefaultMutableTreeNode node =
			(DefaultMutableTreeNode)tree.getLastSelectedPathComponent();

		if (node != null)
		{
			createNodes(node);
			displayInfo((TreeBrowserNode)node.getUserObject());

			if ((this.getParent() != null)
				&& (this
					.getParent()
					.getParent()
					.getParent()
					.getParent()
					.getParent()
					!= null))
			{
				(
					(CSExplorer)this
						.getParent()
						.getParent()
						.getParent()
						.getParent()
						.getParent())
						.updateSelectedSearchPath(
					((TreeBrowserNode)node.getUserObject())
						.getCMObject()
						.getSearchPath()
						.getValue());

			}
		}
	}

	public void actionPerformed(java.awt.event.ActionEvent action)
	{
		JMenuItem actionSource = (JMenuItem)action.getSource();
		if (DETAILS_MENU_STRING.compareTo(actionSource.getText()) == 0)
		{
			TreeBrowserNode nodeForPopup;
			if (((javax.swing.JPopupMenu)actionSource.getParent())
				.getInvoker()
				.getClass()
				== JTree.class)
			{
				nodeForPopup =
					(TreeBrowserNode) ((DefaultMutableTreeNode)tree
						.getLastSelectedPathComponent())
						.getUserObject();
				showDetailPane(nodeForPopup, connection);
			}
			if (((javax.swing.JPopupMenu)actionSource.getParent())
				.getInvoker()
				.getClass()
				== JTable.class)
			{
				JTable tableForPopup =
					(JTable) ((javax.swing.JPopupMenu)actionSource.getParent())
						.getInvoker();
				if (tableForPopup.getSelectedRow() != -1)
				{
					nodeForPopup =
						(
							(TreeBrowserTableModel)tableForPopup
								.getModel())
								.getTbnForRow(
							tableForPopup.getSelectedRow());
					showDetailPane(nodeForPopup, connection);
				}
			}

		}
	}

	public void showDetailPane(TreeBrowserNode node, CRNConnect connection)
	{

		BaseClass cmObject = node.getCMObject();
		Class cmObjectClass = cmObject.getClass();
		Method[] cmObjectMethods = cmObjectClass.getMethods();

		HashMap objectProperties = new HashMap();
		for (int i = 0; i < cmObjectMethods.length; i++)
		{
			String methodName = cmObjectMethods[i].getName();

			try
			{
				PropEnum theProperty =
					PropEnum.fromString(
						TreeBrowserNodeDetailTableModel.propertyFromGetMethod(
							methodName));
				objectProperties.put(cmObjectMethods[i], theProperty);
			}
			catch (IllegalArgumentException isEx)
			{
				cmObjectMethods[i] = null;
			}
		}
		PropEnum[] allTheProps = new PropEnum[objectProperties.size()];

		int j = 0;
		for (int i = 0; i < cmObjectMethods.length; i++)
		{
			if (cmObjectMethods[i] != null)
			{
				try
				{
					allTheProps[j++] =
						PropEnum.fromString(
							((PropEnum)objectProperties
								.get(cmObjectMethods[i]))
								.getValue());
				}
				catch (IllegalArgumentException isEx)
				{
					j--;
				}
			}
		}

		try
		{
			if (!node.getDetailsPopulated())
			{
				SearchPathMultipleObject cmObjPath = new SearchPathMultipleObject();
				cmObjPath.set_value(cmObject.getSearchPath().getValue());
				 
				node.setCMObject(
					connection.getCMService().query(
						cmObjPath,
						allTheProps,
						new Sort[] {},
						new QueryOptions())[0]);
				node.setDetailsPopulated(true);
			}
		}
		catch (java.rmi.RemoteException remoteEx)
		{
			System.out.println("remoteException");
		}

		tableModelForDetails =
			new TreeBrowserNodeDetailTableModel(node.getCMObject());

		tableOfDetails = new JTable(tableModelForDetails);

		JScrollPane detailsPane = new JScrollPane(tableOfDetails);
		JPanel navPanel = createNavPanel(node);

		JPanel detailsAndNav = new JPanel();
		detailsAndNav.setLayout(new BoxLayout(detailsAndNav, BoxLayout.Y_AXIS));
		detailsAndNav.add(navPanel, BorderLayout.NORTH);
		detailsAndNav.add(detailsPane);
		
		JFrame detailsFrame =
			new JFrame(
				"Details for "
					+ node.getCMObject().getDefaultName().getValue());
		detailsFrame.getContentPane().add(detailsAndNav);

		detailsFrame.pack();
		detailsFrame.setVisible(true);
	}


	private JPanel createNavPanel(TreeBrowserNode node)
	{
		// Add the current node text field and label
		currentNode = new JTextField(40);
		currentNode.setText(node.getCMObject().getDefaultName().getValue());
		currentNode.setEditable(false);

		//Put together a panel for the current node
		JPanel nodePanel = new JPanel();
		nodePanel.add(new JLabel("Current Node:"));
		nodePanel.add(currentNode);
		
		//get the button panel
		JPanel buttonPanel = createMainButtonPanel();

		
		// create the main panel and add the components
		JPanel mainPanel = new JPanel(new GridLayout(3,0));
		
		// Add everything to the main panel
		mainPanel.add(nodePanel);
		mainPanel.add(buttonPanel);
		
		return mainPanel;
	}
	
	private JPanel createMainButtonPanel()
	{
		// Create the button Panel
		JPanel buttonPanel = new JPanel();

		// Create and add the Buttons
		backButton = new JButton("<< Back");
		backButton.addActionListener(new navBackButtonHandler());
		buttonPanel.add(backButton);

		forwardButton = new JButton("Forward >>");
		forwardButton.addActionListener(new navFwdButtonHandler());
		buttonPanel.add(forwardButton);

		return buttonPanel;
	}
	
	private class navFwdButtonHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent e)
		{
			if (tableOfDetails.getSelectedRow() >= 0)
			{
				currentNode.setText((tableOfDetails.getValueAt(tableOfDetails.getSelectedRow(),1)).toString());
				tableModelForDetails.incrementNavigation(tableOfDetails);
				tableOfDetails.changeSelection(0, 0, false, false);
			}
		}
	}

	private class navBackButtonHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent e)
		{
			tableModelForDetails.decrementNavigation(tableOfDetails);
			currentNode.setText(tableModelForDetails.getCurrentNode().toString());
			tableOfDetails.changeSelection(0, 0, false, false);
		}
	}
	
	private void displayInfo(TreeBrowserNode node)
	{
		if (node != null)
		{
			((TreeBrowserTableModel)detailsTable.getModel()).clear();

			for (int i = 0; i < node.getNumChildren(); i++)
			{
				detailsTable.getModel().setValueAt(
					node.getChild(i, connection),
					i,
					0);
			}
			detailsTable.revalidate();
			detailsTable.repaint();
		}
		return;
	}

	private void createNodes(DefaultMutableTreeNode top)
	{
		if (((TreeBrowserNode)top.getUserObject()).getChildrenPopulated())
		{
			return;
		}

		DefaultMutableTreeNode subNode = null;

		TreeBrowserNode tmpNode = (TreeBrowserNode)top.getUserObject();
		for (int i = 0; i < tmpNode.getNumChildren(); i++)
		{
			TreeBrowserNode child = tmpNode.getChild(i, connection);
			subNode = new DefaultMutableTreeNode();
			child.setContainer(subNode);
			top.add(subNode);
		}
		((TreeBrowserNode)top.getUserObject()).setChildrenPopulated(true);
	}

}
