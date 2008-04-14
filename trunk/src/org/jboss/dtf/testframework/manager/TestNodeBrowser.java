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
// $Id: TestNodeBrowser.java 170 2008-03-25 18:59:26Z jhalliday $

package org.jboss.dtf.testframework.manager;

import org.jboss.dtf.testframework.nameservice.*;
import org.jboss.dtf.testframework.serviceregister.*;
import org.jboss.dtf.testframework.testnode.*;
import org.jboss.dtf.testframework.utils.HTTPBrowser;

import javax.swing.JTree;
import javax.swing.JFrame;
import javax.swing.event.*;
import javax.swing.tree.*;
import javax.swing.JToolBar;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.ImageIcon;

import java.awt.event.*;

/**
 *
 * @author  administrator
 */
public class TestNodeBrowser extends javax.swing.JInternalFrame implements ActionListener {

    private final static String         REFRESH = "REFRESH";

    protected NameServiceInterface      _nameService = null;
    protected ServiceRegisterInterface  _serviceRegister = null;

    protected JButton                   _refreshButton;
    protected DefaultMutableTreeNode    _rootNode = null;
    protected ConsoleLogger             _logger = null;
    protected JFrame                    _parent = null;
    protected String                    _selectedTestNode = null;

    /** Creates new form TestNodeBrowser */
    public TestNodeBrowser(JFrame parent, ConsoleLogger logger, NameServiceInterface nameService, ServiceRegisterInterface serviceRegister) {
        _nameService = nameService;
        _serviceRegister = serviceRegister;
        _logger = logger;
        _parent = parent;
        setTitle("TestNode Browser");
        initComponents();
        initTreeContents();
        setVisible(true);
    }

    public void actionPerformed(ActionEvent e)
    {
        String actionCommand = e.getActionCommand();

        if (actionCommand.equals(REFRESH))
        {
            refreshTree();
        }
    }

    protected void refreshTree()
    {
        try
        {
            _logger.log("Refreshing TestNode list");

            _rootNode.removeAllChildren();
            TestNodeInterface[] nodes = _serviceRegister.getRegister();

            _logger.log(nodes.length+" TestNode(s) found.");

            for (int count=0;count<nodes.length;count++)
            {
                try
                {
                    DefaultMutableTreeNode  testNodeNode = new DefaultMutableTreeNode(nodes[count].getName());

                    testNodeNode.add( new DefaultMutableTreeNode("Host Name = [ "+nodes[count].getHostAddress()+" ]") );
                    _rootNode.add( testNodeNode );
                }
                catch (java.rmi.RemoteException e)
                {
                    // If this exception is thrown its likely the test node no longer exists
                    // therefore ignore it
                }
            }

            if (_testNodeTree != null)
            {
                boolean[] data = storeTreeLayout();
                _testNodeTree.updateUI();
                restoreTreeLayout(data);
            }
        }
        catch (java.rmi.RemoteException e)
        {
            e.printStackTrace(System.err);
        }
    }

    private boolean[] storeTreeLayout()
    {
        boolean[] data = new boolean[_testNodeTree.getRowCount()];

        for (int count=0;count<data.length;count++)
        {
            data[count] = _testNodeTree.isExpanded(count);
        }

        return(data);
    }

    private void restoreTreeLayout(boolean[] data)
    {
        for (int count=0;count<data.length;count++)
        {
            if (data[count])
                _testNodeTree.expandRow(count);
        }
    }



    private void initTreeContents()
    {
        try
        {
            _rootNode = new DefaultMutableTreeNode("TestNodes");

            refreshTree();

            _scrollPane = new javax.swing.JScrollPane();

            _testNodeTree = new javax.swing.JTree(_rootNode);
            _testNodeTree.putClientProperty("JTree.lineStyle", "Horizontal");
            _testNodeTree.putClientProperty("JTree.lineStyle", "Angled");


            _scrollPane.setVerticalScrollBarPolicy(javax.swing.JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
            _testNodeTree.setDoubleBuffered(true);
            _scrollPane.setViewportView(_testNodeTree);

            _scrollPane.setVerticalScrollBarPolicy(javax.swing.JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
            getContentPane().add(_scrollPane, java.awt.BorderLayout.CENTER);

            _testNodeTree.addTreeSelectionListener(new TreeSelectionListener() {
                public void valueChanged(TreeSelectionEvent e) {
                    DefaultMutableTreeNode node = (DefaultMutableTreeNode)
                                       _testNodeTree.getLastSelectedPathComponent();

                    if (node == null) return;

                    String nodeInfo = (String)node.getUserObject();
                }
            });

            JToolBar toolbar = new JToolBar();
            addButtons(toolbar);
            getContentPane().add(toolbar, java.awt.BorderLayout.NORTH);
            pack();
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
        }
    }

    private void addButtons(JToolBar toolbar)
    {
        _refreshButton = new JButton(new ImageIcon("refresh.gif"));
        _refreshButton.setToolTipText("Refresh list");
        _refreshButton.setActionCommand(REFRESH);
        _refreshButton.addActionListener(this);
        toolbar.add(_refreshButton);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents

        pack();
    }//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
    private javax.swing.JTree _testNodeTree;
    private javax.swing.JScrollPane _scrollPane;
}
