/** 
Licensed Materials - Property of IBM

IBM Cognos Products: DOCS

(C) Copyright IBM Corp. 2005, 2008

US Government Users Restricted Rights - Use, duplication or disclosure restricted by GSA ADP Schedule Contract with
IBM Corp.
*/
/**
 * CSExplorer.java
 *
 * Copyright (C) 2008 Cognos ULC, an IBM Company. All rights reserved.
 * Cognos (R) is a trademark of Cognos ULC, (formerly Cognos Incorporated).
 *
 */

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import org.apache.log4j.BasicConfigurator;

import com.cognos.developer.schemas.bibus._3.ContentManagerService_PortType;

// This Java class extends the JPanel class so that you can 
// display a window.
public class CSExt extends JFrame
{
	private CRNConnect connect;

	// The following variables represent the dialog components.
	private JPanel outputPane;
	private JPanel outputPanel;
	private JTextField cmURL;
	private JTextField selectedSearchPath;
	
	private static String br = System.getProperty("line.separator");

	private static Logon sessionLogon;
	
	// This is the constructor.
	public CSExt(String title, CRNConnect connection)
	{
		// Set the title of the frame, even before the variables are declared.
		super(title);
		connect = connection;
		addComponents();
	}

	// Add all components to the frame's panel.
	private void addComponents()
	{
		JMenuBar mBar = new JMenuBar();
		this.setJMenuBar(mBar);

		//declare menuItems
		JMenuItem exit;
		JMenuItem refresh;
		JMenuItem about;
		JMenuItem overview;

		//Add and populate the File menu. 
		JMenu fileMenu = new JMenu("File");
		mBar.add(fileMenu);

		refresh = new JMenuItem("Refresh Tree");
		fileMenu.add(refresh);
		refresh.addActionListener(new MenuHandler());

		exit = new JMenuItem("Exit");
		fileMenu.add(exit);
		exit.addActionListener(new MenuHandler());

		//Add and populate the Help menu.
		JMenu helpMenu = new JMenu("Help");
		mBar.add(helpMenu);

		about = new JMenuItem("About");
		helpMenu.add(about);
		about.addActionListener(new MenuHandler());

		overview = new JMenuItem("Overview");
		helpMenu.add(overview);
		overview.addActionListener(new MenuHandler());

		JPanel mainPanel = new JPanel(new GridLayout(2, 0));
		// create a cmURL panel
		JPanel cmURLPanel = new JPanel();

		// Add the URL text field and label
		cmURL = new JTextField(CRNConnect.CM_URL.length() + 5);
		cmURL.setText(CRNConnect.CM_URL);
		cmURL.setEditable(false);
		cmURLPanel.add(new JLabel("Server URL:"), BorderLayout.WEST);
		cmURLPanel.add(cmURL, BorderLayout.EAST);

		// Create the Button and Button Panel
		selectedSearchPath = new JTextField(CRNConnect.CM_URL.length() + 5);
		JPanel searchPathPanel = new JPanel();
		searchPathPanel.add(new JLabel("Search Path:"), BorderLayout.WEST);
		searchPathPanel.add(selectedSearchPath, BorderLayout.EAST);

		// Add the status text pane.
		outputPane = new CSExplorerTree(connect);

		// Add the panels to the mainPanel
		mainPanel.add(cmURLPanel);
		mainPanel.add(searchPathPanel);

		outputPanel = new JPanel(new GridLayout(0, 1));
		outputPanel.add(outputPane);

		JPanel panel = new JPanel(new BorderLayout());
		panel.add(mainPanel, BorderLayout.NORTH);
		panel.add(outputPanel);

		setContentPane(panel);
	}

	private class MenuHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent e)
		{
			if (e.getActionCommand().startsWith("http://"))
			{
				connect.connectionChange(e.getActionCommand());
			}
			try
			{
				JMenuItem menuClicked = (JMenuItem)e.getSource();
				if (menuClicked.getText() == "Exit")
				{
					System.exit(0);
				}

				if (menuClicked.getText() == "Refresh Tree")
				{
					recreateTree();
				}

				if (menuClicked.getText() == "About")
				{
					JOptionPane.showMessageDialog(
						((JMenuItem)e.getSource()).getParent(),
						"IBM Cognos Sample Application\n\n"
							+ "Version 1.0.0\n"
							+ "This application uses the IBM Cognos Software Development Kit",
						"About IBM Cognos Samples",
						JOptionPane.INFORMATION_MESSAGE,
						new ImageIcon("../Common/about.gif"));
				}
				if (menuClicked.getText().compareTo("Overview") == 0)
				{
					JFrame explainWindow =
						new JFrame("Overview for CSExplorer");
					File explainFile = new File("Java_CSExplorer_Explain.html");
					if (! explainFile.exists())
					{
						JOptionPane.showMessageDialog(null, "Explain file not found");
						return;
					}
					URL explainURL =
						new URL("file:///" + explainFile.getAbsolutePath());
					JEditorPane explainPane = new JEditorPane();
					explainPane.setPage(explainURL);
					explainPane.setEditable(false);

					JScrollPane explainScroll =
						new JScrollPane(
							explainPane,
							JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
							JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
					explainWindow.getContentPane().add(explainScroll);
					explainWindow.setSize(640, 480);
					explainWindow.setVisible(true);
				}

			}
			catch (Exception ex)
			{
				JOptionPane.showMessageDialog(
					null,
					"Exception caught:" + ex.toString());
			}
		}
	}

	public void recreateTree()
	{
		outputPanel.setVisible(false);
		outputPanel.remove(outputPane);
		outputPane = new CSExplorerTree(connect);
		outputPanel.add(outputPane);
		outputPanel.setVisible(true);
	}

	public void updateSelectedSearchPath(String newSearchPath)
	{
		selectedSearchPath.setText(newSearchPath);
	}

	// Create the main method to execute the application.
	public static void main(String args[])
	{
		BasicConfigurator.configure();

		CRNConnect connection = new CRNConnect();
		connection.connectToCognosServer();
		sessionLogon = new Logon();

		while (!Logon.loggedIn(connection))
		{
			sessionLogon.logon(connection);

			if (!Logon.loggedIn(connection))
			{
				int retry =
					JOptionPane.showConfirmDialog(
						null,
						"Login Failed. Please try again.",
						"Login Failed",
						JOptionPane.OK_CANCEL_OPTION);
				if (retry != JOptionPane.OK_OPTION)
				{
					System.exit(0);
				}
			}

		}
		
		CognosControl ctrl = new CognosControl();
		
		try {
			cogAllList(connection.getCMService(), ctrl, "/content","0", "");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Cognos 보고서 재귀호출 
	 * @param ctrl
	 * @param list
	 * @throws ClassNotFoundException 
	 */
	private static void cogAllList(ContentManagerService_PortType cmService, CognosControl ctrl, String searchPath, String upReportId, String tab) throws ClassNotFoundException {		
		
		ArrayList<CognosObject> list = ctrl.getSubContentFolderListPath(searchPath, cmService);
		ArrayList<ReportInfo> reportList = new ArrayList<ReportInfo>(); 
		
		/**
		 * http://10.253.43.64:9300/p2pd/servlet/dispatch
		 * admin/admin!@
		 */
		int size = list.size();
		for (int i = 0; i < size; i++) {
			
			CognosObject obj = list.get(i);
			ReportInfo report = new ReportInfo();
			report.setUpReportId(upReportId);
			report.setReportNm(obj.getDefaultName());
			report.setExecUrl(obj.getSearchPath());			
			if("folder".equals(obj.getObjectType())){
				report.setMenuDiv("FOLDER");
				report.setReportDiv("folder");
				cogAllList(cmService,ctrl,obj.getSearchPath(),report.getReportId(),"");
			}else{				
				if("shortcut".equals(obj.getObjectType())){
					report.setReportDiv(obj.getTargetObjectType());								
				}else{					
					report.setReportDiv(obj.getObjectType());
				}							
				report.setMenuDiv("REPORT");
				report.setReportId(obj.getReportId());
				
				StringBuffer sb = new StringBuffer();
				
				sb.append("INSERT INTO IF_BI_TOBE_ID_MAPP VALUES('"+obj.getStoreId()+"'")
				.append(",")
				.append("'"+obj.getSearchPath().replaceAll("'", "''")+"'")
				.append(",")
				.append("'"+obj.getDefaultName()+"');")
				.append(String.valueOf(br));
				
				String filePath = "/if_bi_tobe_id_mapp.sql";
		        try {
		            FileWriter fw = new FileWriter(new File(filePath), true);
		            fw.write(sb.toString());
		            fw.close();
		        } catch(IOException ioe) {
		            ioe.printStackTrace();
		        }
			}	
			reportList.add(report); 
		}		
	}

}
