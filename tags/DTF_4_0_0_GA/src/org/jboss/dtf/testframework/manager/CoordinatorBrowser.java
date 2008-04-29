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
// $Id: CoordinatorBrowser.java 170 2008-03-25 18:59:26Z jhalliday $

package org.jboss.dtf.testframework.manager;

import org.jboss.dtf.testframework.nameservice.*;
import org.jboss.dtf.testframework.testnode.RunUID;
import org.jboss.dtf.testframework.coordinator.*;
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
public class CoordinatorBrowser extends javax.swing.JInternalFrame implements ActionListener, CoordinatorManager {

    private final static String         RUN = "RUN",
                                        REFRESH = "REFRESH",
                                        VIEW_RESULTS = "VIEW_RESULTS";

    protected NameServiceInterface      _nameService = null;

    protected JButton                   _runButton, _refreshButton, _viewResultsButton;
    protected DefaultMutableTreeNode    _rootNode = null;
    protected ConsoleLogger             _logger = null;
    protected JFrame                    _parent = null;

    protected String                    _selectedCoordinator = null;

    /** Creates new form CoordinatorBrowser */
    public CoordinatorBrowser(JFrame parent, ConsoleLogger logger, NameServiceInterface nameService) {
        _nameService = nameService;
        _logger = logger;
        _parent = parent;
        setTitle("Coordinator Browser");
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
        else
        if (actionCommand.equals(RUN))
        {
            new TestRunProperties(this,_parent, true).show();
        }
        else
        if (actionCommand.equals(VIEW_RESULTS))
        {
            try
            {
                CoordinatorInterface coordinator = (CoordinatorInterface)_nameService.lookup("/Coordinators/"+_selectedCoordinator);
                CoordinatorDescriptor descriptor = coordinator.getDescriptor();

                if (descriptor._currentRunUID != null)
                {
                    HTTPBrowser.showURL(descriptor._loggingURL+"default.asp?page=view_results&runid="+descriptor._currentRunUID.getUID());
                }
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
            }
        }
    }

    protected void refreshTree()
    {
        try
        {
            _logger.log("Refreshing coordinator list");

            _rootNode.removeAllChildren();
            String[] coordinatorNames = _nameService.lookupNames("/Coordinators/");

            _logger.log(coordinatorNames.length+" coordinator(s) found.");

            for (int count=0;count<coordinatorNames.length;count++)
            {
                try
                {
                    DefaultMutableTreeNode  coordinatorNode = new DefaultMutableTreeNode(coordinatorNames[count]),
                                            testNode;

                    CoordinatorInterface coordinator = (CoordinatorInterface)_nameService.lookup("/Coordinators/"+coordinatorNames[count]);

                    CoordinatorDescriptor cd = coordinator.getDescriptor();
                    coordinatorNode.add( new DefaultMutableTreeNode("Name = [ "+cd._name+" ]") );
                    coordinatorNode.add( new DefaultMutableTreeNode("Number Of Test Managers = [ "+cd._numberOfTestManagers+" ]") );
                    coordinatorNode.add( testNode = new DefaultMutableTreeNode("Test In Progress = [ "+cd._testInProgress+" ]") );

                    if (cd._testInProgress)
                    {
                        testNode.add( new DefaultMutableTreeNode("Software Version = [ "+cd._softwareVersion+" ]") );
                        testNode.add( new DefaultMutableTreeNode("Test Run UID = [ "+cd._currentRunUID.getUID()+" ]") );
                        testNode.add( new DefaultMutableTreeNode("Test Queue Size = [ "+cd._numQueuedTests+" ]") );
                    }

                    coordinatorNode.add (new DefaultMutableTreeNode("Max Number of Retries = [ "+cd._maxNumRetries+" ]") );
                    coordinatorNode.add (new DefaultMutableTreeNode("Logging URL = [ "+cd._loggingURL+" ]") );

                    _rootNode.add( coordinatorNode );
                }
                catch (java.rmi.RemoteException e)
                {
                    e.printStackTrace(System.err);
                    // If this exception is thrown its likely the coordinator no longer exists
                    // therefore ignore it
                }
            }

            if (_coordinatorTree != null)
            {
                boolean[] data = storeTreeLayout();
                _coordinatorTree.updateUI();
                restoreTreeLayout(data);
            }
        }
        catch (NameNotBound e)
        {
            e.printStackTrace(System.err);
        }
        catch (java.rmi.RemoteException e)
        {
            e.printStackTrace(System.err);
        }
    }

    private boolean[] storeTreeLayout()
    {
        boolean[] data = new boolean[_coordinatorTree.getRowCount()];

        for (int count=0;count<data.length;count++)
        {
            data[count] = _coordinatorTree.isExpanded(count);
        }

        return(data);
    }

    private void restoreTreeLayout(boolean[] data)
    {
        for (int count=0;count<data.length;count++)
        {
            if (data[count])
                _coordinatorTree.expandRow(count);
        }
    }



    private void initTreeContents()
    {
        try
        {
            _rootNode = new DefaultMutableTreeNode("Coordinators");

            refreshTree();

            _scrollPane = new javax.swing.JScrollPane();

            _coordinatorTree = new javax.swing.JTree(_rootNode);
            _coordinatorTree.putClientProperty("JTree.lineStyle", "Horizontal");
            _coordinatorTree.putClientProperty("JTree.lineStyle", "Angled");


            _scrollPane.setVerticalScrollBarPolicy(javax.swing.JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
            _coordinatorTree.setDoubleBuffered(true);
            _scrollPane.setViewportView(_coordinatorTree);

            _scrollPane.setVerticalScrollBarPolicy(javax.swing.JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
            getContentPane().add(_scrollPane, java.awt.BorderLayout.CENTER);

            _coordinatorTree.addTreeSelectionListener(new TreeSelectionListener() {
                public void valueChanged(TreeSelectionEvent e) {
                    DefaultMutableTreeNode node = (DefaultMutableTreeNode)
                                       _coordinatorTree.getLastSelectedPathComponent();

                    if (node == null) return;

                    String nodeInfo = (String)node.getUserObject();

                    if (nodeInfo.endsWith("_Coordinator"))
                    {
                        _runButton.setEnabled(true);
                        _viewResultsButton.setEnabled(true);
                        _selectedCoordinator = nodeInfo;
                    }
                    else
                    {
                        _viewResultsButton.setEnabled(false);
                        _runButton.setEnabled(false);
                    }

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

        _runButton = new JButton(new ImageIcon("run.gif"));
        _runButton.setToolTipText("Start test run");
        _runButton.setActionCommand(RUN);
        _runButton.setEnabled(false);
        _runButton.addActionListener(this);
        toolbar.add(_runButton);

        _viewResultsButton = new JButton(new ImageIcon("results.gif"));
        _viewResultsButton.setToolTipText("View results for current run");
        _viewResultsButton.setActionCommand(VIEW_RESULTS);
        _viewResultsButton.setEnabled(false);
        _viewResultsButton.addActionListener(this);
        toolbar.add(_viewResultsButton);
    }

    public void performTestRun(String testDefURL, String testSelURL, String softwareVersion)
    {
        _logger.log("Attempting to perform test run using: ");
        _logger.log("Coordinator: '"+_selectedCoordinator+"'");
        _logger.log("Definition File: '"+testDefURL+"'");
        _logger.log("Selection File: '"+testSelURL+"'");
        _logger.log("Software Version: '"+softwareVersion+"'");

        try
        {
            CoordinatorInterface coordinator = (CoordinatorInterface)_nameService.lookup("/Coordinators/"+_selectedCoordinator);

            coordinator.initialiseTestRun(testDefURL, testSelURL, softwareVersion, "", false);
            _logger.log("Test run started.");
        }
        catch (Exception e)
        {
            _logger.log("ERROR: "+e.toString());
        }
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
    private javax.swing.JTree _coordinatorTree;
    private javax.swing.JScrollPane _scrollPane;
}
