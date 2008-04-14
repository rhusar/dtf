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
/*
 * Copyright (C) 2001, 2002,
 *
 * Hewlett-Packard Arjuna Labs,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: TestBrowser.java 170 2008-03-25 18:59:26Z jhalliday $
 */

package org.jboss.dtf.tools.testcomposer.frames;

import org.jboss.dtf.testframework.coordinator.TestDefinitionRepository;
import org.jboss.dtf.testframework.coordinator.TestDefinition;
import org.jboss.dtf.tools.testcomposer.TestComposer;
import org.jboss.dtf.tools.testcomposer.forms.NewTestForm;

import javax.swing.*;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeNode;
import java.util.HashMap;
import java.util.Iterator;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class TestBrowser extends JInternalFrame implements ActionListener, TreeSelectionListener
{
    private final static String NEW_GROUP_PREFIX = "New_group_";

    private final static String NEW_TEST_MENU_ITEM = "New Test";
    private final static String OPEN_TEST_MENU_ITEM= "Open";
    private final static String REMOVE_TEST_MENU_ITEM = "Remove";

    private final static String NEW_GROUP_MENU_ITEM = "New Group";

    protected TestDefinitionRepository _testRepository = null;
    protected JTree _tree = null;
    protected DefaultMutableTreeNode _treeRoot = null;
    protected TestComposer _parent = null;
    protected TreePath _currentlySelectedPath = null;

    public TestBrowser(TestComposer parent)
    {
        super("Test Browser",
              true, //resizable
              true, //closable
              true, //maximizable
              true);//iconifiable

        this.setBackground(Color.white);
        this.setSize(250,parent.getDesktop().getHeight());
        JScrollPane scroller = new JScrollPane(_tree = new JTree(_treeRoot = new DefaultMutableTreeNode("Test Definitions")));

        _tree.addTreeSelectionListener(this);
        _tree.setCellRenderer(new TestBrowserCellRenderer());
        _tree.putClientProperty("JTree.lineStyle", "Angled");
        _parent = parent;
        populateTree(_treeRoot,TestComposer.TestSetData.getTestDefinitionsMap());
        this.getContentPane().add(scroller);
        this.setJMenuBar(createMenuBar());
        this.show();
    }

    protected JMenuBar createMenuBar()
    {
        JMenuBar menuBar = new JMenuBar();
        JMenu menu;
        JMenuItem item;

        menuBar.add( menu = new JMenu("Tests") );
        menu.add(item = new JMenuItem(NEW_TEST_MENU_ITEM));
        item.addActionListener(this);
        item.setEnabled(false);
        item.setMnemonic(KeyEvent.VK_N);

        menu.add(item = new JMenuItem(OPEN_TEST_MENU_ITEM));
        item.addActionListener(this);
        item.setEnabled(false);
        item.setMnemonic(KeyEvent.VK_O);

        menu.add(item = new JMenuItem(REMOVE_TEST_MENU_ITEM));
        item.addActionListener(this);
        item.setEnabled(false);
        item.setMnemonic(KeyEvent.VK_R);

        menuBar.add( menu = new JMenu("Groups") );
        menu.add(item = new JMenuItem(NEW_GROUP_MENU_ITEM));
        item.addActionListener(this);
        item.setMnemonic(KeyEvent.VK_N);
        item.setEnabled(false);

        return(menuBar);
    }

    protected void populateTree(DefaultMutableTreeNode tree, HashMap tests)
    {
        Iterator i = tests.keySet().iterator();

        while (i.hasNext())
        {
            String key = (String)i.next();
            TestDefinition testDef = (TestDefinition)tests.get(key);

            createTreeNodeFromTest(testDef);
        }
    }

    private TreeNode treeHasChild(String name, TreeNode node)
    {
        for (int count=0;count<node.getChildCount();count++)
        {
            if (node.getChildAt(count).toString().equals(name))
            {
                return(node.getChildAt(count));
            }
        }

        return(null);
    }

    protected void createTreeNodeFromTest(TestDefinition test)
    {
        String testName = test.getFullId();
        DefaultMutableTreeNode currentNode = _treeRoot;
        DefaultMutableTreeNode tempNode;

        while ( testName.indexOf('/') != -1 )
        {
            String subGroup = testName.substring(0,testName.indexOf('/'));
            testName = testName.substring(testName.indexOf('/')+1);

            if ( (tempNode = (DefaultMutableTreeNode)treeHasChild(subGroup, currentNode)) != null )
            {
                currentNode = tempNode;
            }
            else
            {
                currentNode.add(currentNode = new DefaultMutableTreeNode(subGroup));
            }
        }

        DefaultMutableTreeNode testNode = new DefaultMutableTreeNode(test);

/*        testNode.add(new DefaultMutableTreeNode("Description:"+test.getDescription()));
        testNode.add(new DefaultMutableTreeNode("#Names Required:"+test.getNamesRequired()));
        testNode.add(new DefaultMutableTreeNode("#Actions:"+test.getActionList().size()));*/

        currentNode.add(testNode);
    }

    /**
     * Invoked when an action occurs.
     */
    public void actionPerformed(ActionEvent e)
    {
        String actionCommand = e.getActionCommand();

        if ( (actionCommand.equals(NEW_TEST_MENU_ITEM)) && (getCurrentlySelectedItem() instanceof String) )
        {
            NewTestForm newTestForm = new NewTestForm(null);
            newTestForm.show();
            TestDefinition test = newTestForm.getTestDefinition(getCurrentlySelectedGroup());
            _parent.createTestDesignFrame(test);

            DefaultMutableTreeNode currentNode = (DefaultMutableTreeNode)_currentlySelectedPath.getLastPathComponent();
            DefaultMutableTreeNode newTestNode = new DefaultMutableTreeNode(test);
            currentNode.add(newTestNode);
        }

        if ( (actionCommand.equals(OPEN_TEST_MENU_ITEM)) && (getCurrentlySelectedItem() instanceof TestDefinition) )
        {
            _parent.createTestDesignFrame((TestDefinition)getCurrentlySelectedItem());
        }

        if ( (actionCommand.equals(NEW_GROUP_MENU_ITEM)) && (getCurrentlySelectedItem() instanceof String) )
        {
            createNewGroup();
        }
    }

    protected void createNewGroup()
    {
        int groupNumber = 0;
        DefaultMutableTreeNode currentNode = (DefaultMutableTreeNode)_currentlySelectedPath.getLastPathComponent();
        do
        {
            groupNumber++;
        } while (treeHasChild(NEW_GROUP_PREFIX+groupNumber,currentNode) != null);

        currentNode.add(new DefaultMutableTreeNode(NEW_GROUP_PREFIX+groupNumber));
        _tree.updateUI();
        _tree.setSelectionPath(_currentlySelectedPath);
    }

    protected Object getCurrentlySelectedItem()
    {
        return((DefaultMutableTreeNode)_currentlySelectedPath.getLastPathComponent()).getUserObject();
    }

    protected boolean isTestGroupSelected()
    {
        return(getCurrentlySelectedItem() instanceof String);
    }

    protected boolean isRootSelected()
    {
        return(_currentlySelectedPath.getLastPathComponent() == _treeRoot);
    }

    protected String getCurrentlySelectedGroup()
    {
        String groupName = "";

        for (int count=1;count<_currentlySelectedPath.getPathCount();count++)
        {
            Object obj = ((DefaultMutableTreeNode)_currentlySelectedPath.getPathComponent(count)).getUserObject();

            if (groupName.length() > 0)
            {
                groupName += '/';
            }

            if (obj instanceof String)
            {
                groupName = groupName + obj;
            }
            else
            {
                break;
            }
        }

        return(groupName);
    }

    /**
     * Called whenever the value of the selection changes.
     * @param e the event that characterizes the change.
     */
    public void valueChanged(TreeSelectionEvent e)
    {
        boolean newItem = false;
        boolean openRemoveItem = false;

        _currentlySelectedPath = e.getPath();

        if ( ((DefaultMutableTreeNode)_currentlySelectedPath.getLastPathComponent()).isRoot() )
        {
            _tree.setEditable(false);
        }
        else
        {
            _tree.setEditable(isTestGroupSelected());
        }

        if ( (isTestGroupSelected()) && (!isRootSelected()) )
        {
            newItem = true;
        }

        if (!isTestGroupSelected())
        {
            openRemoveItem = true;
        }

        getJMenuBar().getMenu(0).getMenuComponent(0).setEnabled(newItem);
        getJMenuBar().getMenu(0).getMenuComponent(1).setEnabled(openRemoveItem);
        getJMenuBar().getMenu(0).getMenuComponent(2).setEnabled(openRemoveItem);
        getJMenuBar().getMenu(1).getMenuComponent(0).setEnabled(isTestGroupSelected());
    }
}
