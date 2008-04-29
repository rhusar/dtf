/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 *
 * (C) 2008,
 * @author JBoss Inc.
 */
//
// Copyright (C) 2001,
//
// HP Arjuna Labs
// Newcastle upon Tyne,
// Tyne and Wear,
// UK.
//
// $Id: CoordinatorGUI.java 170 2008-03-25 18:59:26Z jhalliday $

package org.jboss.dtf.testframework.manager;

import org.jboss.dtf.testframework.coordinator.*;

import org.jdom.input.*;
import org.jdom.output.XMLOutputter;
import org.jdom.output.Format;
import org.jdom.*;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;

public class CoordinatorGUI extends JInternalFrame implements ActionListener
{
	private HashMap _supportedOS = new HashMap();
	private HashMap _supportedProduct = new HashMap();
	private HashMap _testGroups = new HashMap();
	private DefaultMutableTreeNode _root = null;
	private JTree 	tree;
	private JTable	_table = null;

	public CoordinatorGUI(String testDefinitionsURL)
	{
		super("The Coordinator");

		// Create root node
		_root = new DefaultMutableTreeNode("Test Selection");

		retrieveConfiguration(testDefinitionsURL);

		// Create tree
		createTree(_root);

		// Create menubar
		setJMenuBar(createMenu());

		// Create JTree from root node
		tree = new JTree(_root);

		// Set properties on JTree
		tree.putClientProperty("JTree.lineStyle", "Angled");
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		tree.setCellRenderer(new CheckBoxTreeRenderer());

		// Create scrollable view to put tree into
		JScrollPane treeView = new JScrollPane(tree);

		// Place view into content pane
		getContentPane().add(treeView, BorderLayout.CENTER);

		//Listen for when the selection changes.
		tree.addTreeSelectionListener(new TreeSelectionListener() {
		    public void valueChanged(TreeSelectionEvent e) {
		        DefaultMutableTreeNode node = (DefaultMutableTreeNode)
		                           tree.getLastSelectedPathComponent();

		        if (node == null) return;

				// Ensure the node selected was a checkable object
				if (node.getUserObject() instanceof CheckableTreeNode)
				{
					// Get selected node object
	        		CheckableTreeNode nodeInfo = (CheckableTreeNode)(node.getUserObject());

	        		// Toggle selection flag
	        		nodeInfo.setSelected(!nodeInfo.getSelected());

					// Ensure all children objects are selected
					selectAllChildren(node,nodeInfo.getSelected());
		        }

		        // Redraw tree
		        tree.updateUI();
		    }

			/**
			 * Recursively set all children under the given node to a given state
			 */
			private void selectAllChildren(DefaultMutableTreeNode node, boolean state)
			{
        		for (int iCount=0;iCount<node.getChildCount();iCount++)
        		{
        			DefaultMutableTreeNode child = (DefaultMutableTreeNode)(node.getChildAt(iCount));

        			CheckableTreeNode childNodeInfo = (CheckableTreeNode)(child.getUserObject());
    				childNodeInfo.setSelected(state);
    				if (child.getChildCount()>0)
    					selectAllChildren(child,state);
        		}
			}
		});

		pack();
		setVisible(true);
	}

	/**
	 * Create menu bar - the menu bar consists of two menus File and Coordinator
	 * the File menu contains menu items to allow the user to open and save their selections,
	 * the coordinator menu allows the user to run the coordinator from the GUI if required
	 */
	private JMenuBar createMenu()
	{
		// Create top-level menu bar
		JMenuBar menuBar = new JMenuBar();

		// Create 'file' menu
		JMenu fileMenu = new JMenu("File");
		// Create 'coordinator' menu
		JMenu coordinatorMenu = new JMenu("Coordinator");

		// Create menuitems
		JMenuItem openMenuItem = new JMenuItem("Open", java.awt.event.KeyEvent.VK_O),
				  saveMenuItem = new JMenuItem("Save", java.awt.event.KeyEvent.VK_S),
				  saveToWebMenuItem = new JMenuItem("Save to Web", java.awt.event.KeyEvent.VK_W),
				  exitMenuItem = new JMenuItem("Exit", java.awt.event.KeyEvent.VK_X);
		JMenuItem launchMenuItem = new JMenuItem("Launch", java.awt.event.KeyEvent.VK_L);

		// Add menus and items to the menu bar
		coordinatorMenu.add(launchMenuItem);
		fileMenu.add(openMenuItem);
		openMenuItem.addActionListener(this);
		fileMenu.add(saveMenuItem);
		fileMenu.add(saveToWebMenuItem);
		saveMenuItem.addActionListener(this);
		saveToWebMenuItem.addActionListener(this);
		fileMenu.addSeparator();
		fileMenu.add(exitMenuItem);
		exitMenuItem.addActionListener(this);
		menuBar.add(fileMenu);
		menuBar.add(coordinatorMenu);

		return(menuBar);
	}

	private static String stripFilename(String filename)
	{
		if (filename.indexOf(File.separatorChar)!=-1)
		{
			filename = filename.substring(filename.lastIndexOf(File.separatorChar)+1);
		}

		return(filename);
	}

	/**
	 * Action Listener method to catch user interaction with the menus
	 */
	public void actionPerformed(ActionEvent e)
	{
		if (e.getActionCommand().equalsIgnoreCase("Open"))
		{
			readInSelection();
		}
		else
		if (e.getActionCommand().equalsIgnoreCase("Save"))
		{
			writeSelection();
		}
		else
		if (e.getActionCommand().equalsIgnoreCase("Save to Web"))
		{
			writeSelectionToWeb("http://bob/webresults/create_xml.asp?filename=");
		}
		else
		if (e.getActionCommand().equalsIgnoreCase("Exit"))
		{
			dispose();
			System.exit(0);
		}
		tree.updateUI();
	}

	private void writeSelectionToWeb(String urlString)
	{
		String filename = writeSelection();

		String returnData = "";

		try
		{
			urlString += stripFilename(filename);

			System.out.println("Opening connection to "+urlString);
			URL url = new URL(urlString);
			byte[] buffer = new byte[8192];
			int bytesRead;
			FileInputStream fileIn = new FileInputStream(filename);
			URLConnection connection = url.openConnection();

			connection.setDoOutput(true);
			connection.setDoInput(true);

			BufferedOutputStream out = new BufferedOutputStream(connection.getOutputStream());

			System.out.println("Writing data");
			while ( (bytesRead = fileIn.read(buffer)) != -1)
			{
				out.write(buffer,0,bytesRead);
			}

			fileIn.close();
			out.close();

			BufferedReader in = new BufferedReader(
							new InputStreamReader( connection.getInputStream() ) );

			String inputLine;

			while ((inputLine = in.readLine())!=null)
			{
				returnData += inputLine;
			}
			System.out.println("Returned data = "+returnData);
			in.close();

			JOptionPane.showMessageDialog(this,"File Uploaded Successfully!");
		}
		catch (Exception e)
		{
			e.printStackTrace();
			returnData = null;
		}
	}

	/**
	 * Write all selections to an XML file for loading at a later date
	 * by the GUI or the coordinator
	 */
	private String writeSelection()
	{
		JFileChooser fc = null;

		try
		{
			fc = new JFileChooser();
			int returnVal = fc.showDialog(this,"Save...");

    		if (returnVal == JFileChooser.APPROVE_OPTION)
    		{
				/*
				 * Create an XML Outputter which has an ident of 4 spaces
				 * and a CR as the line separator
				 */
				XMLOutputter outputter = new XMLOutputter();
                outputter.setFormat(Format.getPrettyFormat());

				Element rootElement = new Element("test_selection");
				Document doc = new Document(rootElement);
				traverseTreeAndWrite(rootElement, _root);
				FileOutputStream fos = new FileOutputStream(fc.getSelectedFile());
	            outputter.output(doc, fos);
	            fos.close();
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return(fc.getSelectedFile().getAbsolutePath());
	}

	/**
	 * Called by writeSelection to recursively traverse the tree
	 * and write the nodes it finds to the XML file
	 */
	private void traverseTreeAndWrite(Element xmlElement, DefaultMutableTreeNode treeNode)
	{
		Object obj = treeNode.getUserObject();
		Element newElement = xmlElement;

		if ( obj instanceof CheckableTreeNode )
		{
			CheckableTreeNode checkTreeNode = (CheckableTreeNode)obj;

			switch (checkTreeNode.getType())
			{
				case CheckableTreeNode.TYPE_OS :
					xmlElement.addContent(newElement = new Element("os"));
					newElement.setAttribute("id", checkTreeNode.getOSName());
					break;
				case CheckableTreeNode.TYPE_PRODUCT :
					xmlElement.addContent(newElement = new Element("product"));
					newElement.setAttribute("id", checkTreeNode.getProductName());
					break;
				case CheckableTreeNode.TYPE_GROUP :
					xmlElement.addContent(newElement = new Element("testGroup"));
					newElement.setAttribute("id", checkTreeNode.getGroupName());
					break;
				case CheckableTreeNode.TYPE_TEST :
					xmlElement.addContent(newElement = new Element("test"));
					newElement.setAttribute("id", checkTreeNode.getTestName());
					newElement.setAttribute("selected", new Boolean(checkTreeNode.getSelected()).toString());
					break;
			}
		}

		for (int count=0;count<treeNode.getChildCount();count++)
		{
			traverseTreeAndWrite(newElement, (DefaultMutableTreeNode)(treeNode.getChildAt(count)));
		}
	}

	private void readInSelection()
	{
		try
		{
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Create tree using the data retrieved from the coordinator configuration file
	 * for each supported OS it adds the supported Products along with the tests
	 * that these OS/Product combinations can run
	 */
	private void createTree(DefaultMutableTreeNode rootNode)
	{
		Object[] osName = _supportedOS.keySet().toArray();
		DefaultMutableTreeNode[] osNode = new DefaultMutableTreeNode[osName.length];
		DefaultMutableTreeNode productEntry, group;

		/*
		 * For each of the OS names in the supportedOS keyset
		 * create a tree node
		 */
		for (int count=0;count<osName.length;count++)
		{

			SupportedOS os = (SupportedOS)_supportedOS.get(osName[count]);

			/*
			 * Create CheckableTreeNode with the properties gained from the supported OS map
			 */
			CheckableTreeNode newOSCTNode = new CheckableTreeNode(os.getName());
			newOSCTNode.setId(os.getName());

			rootNode.add(osNode[count] = new DefaultMutableTreeNode(newOSCTNode));
			String[] productName = os.getSupportedProductList();

			/*
			 * For each of the Product's supported by this OS
			 * create a tree node
			 */
			for (int productCount=0;productCount<productName.length;productCount++)
			{
				CheckableTreeNode newProductCTNode = new CheckableTreeNode((String)osName[count],productName[productCount]);
					osNode[count].add(productEntry = new DefaultMutableTreeNode(newProductCTNode));
				Object[] testGroups = _testGroups.values().toArray();

				/*
				 * For each of the test groups that exist create a tree node
				 */
				for (int testGroupCount=0;testGroupCount<testGroups.length;testGroupCount++)
				{
					TestGroup testGroup = (TestGroup)testGroups[testGroupCount];
					productEntry.add(group = new DefaultMutableTreeNode(new CheckableTreeNode((String)osName[count],productName[productCount],testGroup.getName())));

					TestDefinition[] tests = testGroup.getTests();

					/*
					 * For each of the tests that exist within this testgroup
					 * create a tree node
					 */
					for (int testCount=0;testCount<tests.length;testCount++)
					{
						group.add(new DefaultMutableTreeNode(new CheckableTreeNode((String)osName[count],productName[productCount],testGroup.getName(),tests[testCount].getTestId())));
					}
				}
			}
		}
	}

	/**
	 * Retrieve the configuration from the coordinator configuration file
	 */
	private void retrieveConfiguration(String testDefinitionsURL)
	{
		try
		{
			SAXBuilder xmlBuilder = new SAXBuilder();
			Document doc = xmlBuilder.build(new File("coordinator.xml"));

			/*
			 * Retrieve root element
			 */
			Element root = doc.getRootElement();
			Element supportedProductElement = root.getChild("supported_product_list");
			Element supportedOSElement = root.getChild("os_product_combinations");
			/*
			 * Retrieve product elements
			 * This element contains the name/displaynames for all OS's supported
			 * by the test system
			 */
			List productConfigs = supportedProductElement.getChildren("product");

			for (int count=0;count<productConfigs.size();count++)
			{
				Element productConfig = (Element)productConfigs.get(count);

				String displayName = productConfig.getAttributeValue("displayName");
				String name = productConfig.getAttributeValue("name");

				_supportedProduct.put(displayName, name);
			}

			/*
			 * Retrieve os_product_combinations elements
			 * This element contains the name/displaynames for all OS's supported
			 * by the test system along with the product's they support
			 */
			List osConfigs = supportedOSElement.getChildren("os");

			for (int count=0;count<osConfigs.size();count++)
			{
				Element osConfig = (Element)osConfigs.get(count);

				String displayName = osConfig.getAttributeValue("displayName");
				String name = osConfig.getAttributeValue("name");
				SupportedOS supportedOS = new SupportedOS(name, displayName);
				Object[] supportedProductsList = osConfig.getChildren().toArray();

				for (int childCount=0;childCount<supportedProductsList.length;childCount++)
				{
					Element supportedProduct = (Element)supportedProductsList[childCount];
					String productName = supportedProduct.getAttributeValue("name");

					supportedOS.addProduct(productName);
				}

				_supportedOS.put(name, supportedOS);
			}

			/*
			 * Retrieve test definition configuration
			 */
			Document testDefDoc = xmlBuilder.build(new java.net.URL(testDefinitionsURL));
			Element testDefRoot = testDefDoc.getRootElement();

			/*
			 * Retrieve test_group's
			 */
			List testGroups = testDefRoot.getChildren("test_group");

			for (int count=0;count<testGroups.size();count++)
			{
				Element testGroup = (Element)testGroups.get(count);
				String groupName = testGroup.getAttributeValue("name");

				TestDefinition[] testsInGroup = retrieveTestsInGroup(testGroup);
				_testGroups.put(groupName, new TestGroup(groupName,testsInGroup));
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			System.err.println("Failed while reading configuration");
			System.exit(0);
		}
	}

	private TestDefinition[] retrieveTestsInGroup(Element testGroup)
	{
		List testDefs = testGroup.getChildren("test_declaration");
		TestDefinition[] results = new TestDefinition[testDefs.size()];

		for (int count=0;count<testDefs.size();count++)
		{
			Element testDef = (Element)testDefs.get(count);

			results[count] = new TestDefinition(testDef.getAttributeValue("id"),
											 	testDef.getAttributeValue("descriptive_name"));
		}

		return(results);
	}

	/**
	 * Class stored against each node in the JTree to hold the state of that node
	 */
	private class CheckableTreeNode
	{
		public final static int TYPE_OS = 1, TYPE_PRODUCT = 2, TYPE_GROUP = 3, TYPE_TEST = 4;

		private String _productName = null;
		private String _osName = null;
		private String _displayName = null;
		private String _groupName = null;
		private boolean _selected = false;
		private String _testName = null;
		private int	   _nodeType = 0;
		private String _id = null;

		/**
		 * Constructor for a Test node
		 */
		public CheckableTreeNode(String osName, String productName, String groupName, String test)
		{
			_productName = productName;
			_osName = osName;
			_groupName = groupName;
			_testName = test;
			_displayName = test;
			_nodeType = TYPE_TEST;
			_id = test;
		}

		/**
		 * Constructor for a TestGroup node
		 */
		public CheckableTreeNode(String osName, String productName, String groupName)
		{
			_productName = productName;
			_osName = osName;
			_groupName = groupName;
			_displayName = groupName;
			_nodeType = TYPE_GROUP;
			_id = groupName;
		}

		/**
		 * Constructor for an Product node
		 */
		public CheckableTreeNode(String osName, String productName)
		{
			_productName = productName;
			_osName = osName;
			_displayName = productName;
			_nodeType = TYPE_PRODUCT;
			_id = productName;
		}

		/**
		 * Constructor for an OS node
		 */
		public CheckableTreeNode(String osName)
		{
			_osName = osName;
			_displayName = osName;
			_nodeType = TYPE_OS;
			_id = osName;
		}

		public String getProductName()
		{
			return(_productName);
		}

		public String getOSName()
		{
			return(_osName);
		}

		public String getGroupName()
		{
			return(_groupName);
		}

		public String getTestName()
		{
			return(_testName);
		}

		public int getType()
		{
			return(_nodeType);
		}

		public void setSelected(boolean state)
		{
			_selected = state;
		}

		public boolean getSelected()
		{
			return(_selected);
		}

		public void setId(String id)
		{
			_id = id;
		}

		public String getId()
		{
			return(_id);
		}

		public String toString()
		{
			return(_displayName);
		}
	}


	/**
	 * The Cell Renderer for the JTree which draws checkbox images
	 */
	private class CheckBoxTreeRenderer extends DefaultTreeCellRenderer
	{
		ImageIcon _checkedBoxIcon,
				  _uncheckedBoxIcon;

		public CheckBoxTreeRenderer()
		{
			_checkedBoxIcon = new ImageIcon("checked.gif");
			_uncheckedBoxIcon = new ImageIcon("unchecked.gif");
		}

		public Component getTreeCellRendererComponent(	JTree 	tree,
														Object 	value,
														boolean	sel,
														boolean	expanded,
														boolean	leaf,
														int		row,
														boolean	hasFocus)
		{
			super.getTreeCellRendererComponent(tree,value,sel,expanded,leaf,row,hasFocus);

			DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;

			if (node.getUserObject() instanceof CheckableTreeNode)
			{
				CheckableTreeNode check = (CheckableTreeNode)(node.getUserObject());
				ImageIcon icon = check.getSelected()?_checkedBoxIcon:_uncheckedBoxIcon;

				setIcon(icon);
			}
			return(this);
		}
	}

	public static void main(String args[])
	{
		if (args.length==0)
		{
			System.out.println("Usage: CoordinatorGUI URL://TestDefinitions.xml");
		}
		else
			new CoordinatorGUI(args[0]);
	}
}
