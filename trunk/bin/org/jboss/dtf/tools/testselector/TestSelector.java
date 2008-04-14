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
package org.jboss.dtf.tools.testselector;

import org.jboss.dtf.testframework.coordinator.TestDefinitionRepository;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.util.ArrayList;
import java.util.Hashtable;
import java.awt.event.KeyEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public class TestSelector extends JFrame implements ActionListener
{
    private final static String OPEN_TEST_DEFS_ACTION = "OPEN_TEST_DEFS_ACTION";
    private final static String OPEN_NODE_CONFIG_ACTION = "OPEN_NODE_CONFIG_ACTION";
    private final static String EXIT_ACTION = "EXIT_ACTION";
    private final static String ADD_OS_ACTION = "ADD_OS_ACTION";

    private ArrayList               _testNames = null;
    private ArrayList               _productNames = null;
    private ArrayList               _osNames = null;
    private Hashtable               _selections = null;

    private JTree                   _selectionTree = null;
    private DefaultMutableTreeNode  _rootNode = null;

    public TestSelector()
    {
        super("Distributed Test Framework - Test Selector");

        setSize( 400, 300 );

        this.setJMenuBar( createMenuBar() );

        show();
    }

    private JMenuBar createMenuBar()
    {
        JMenuBar menuBar = new JMenuBar();

        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic(KeyEvent.VK_F);

        JMenuItem menuItem;

        menuItem = new JMenuItem("Open Test Definitions", KeyEvent.VK_O);
        menuItem.setAccelerator( KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.ALT_MASK) );
        menuItem.setActionCommand( OPEN_TEST_DEFS_ACTION );
        menuItem.addActionListener(this);
        fileMenu.add(menuItem);

        menuItem = new JMenuItem("Open Node Configuration", KeyEvent.VK_N);
        menuItem.setAccelerator( KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.ALT_MASK) );
        menuItem.setActionCommand( OPEN_NODE_CONFIG_ACTION );
        menuItem.addActionListener(this);
        fileMenu.add(menuItem);

        menuItem = new JMenuItem("Open Test Selections", KeyEvent.VK_T);
        menuItem.setEnabled(false);
        menuItem.setAccelerator( KeyStroke.getKeyStroke(KeyEvent.VK_E, ActionEvent.ALT_MASK) );
        menuItem.addActionListener(this);
        fileMenu.add(menuItem);

        menuItem = new JMenuItem("Save Test Selections", KeyEvent.VK_S);
        menuItem.setEnabled(false);
        menuItem.setAccelerator( KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.ALT_MASK) );
        menuItem.addActionListener(this);
        fileMenu.add(menuItem);

        fileMenu.addSeparator();

        menuItem = new JMenuItem("Exit", KeyEvent.VK_X);
        menuItem.setAccelerator( KeyStroke.getKeyStroke(KeyEvent.VK_X, ActionEvent.CTRL_MASK) );
        menuItem.setActionCommand( EXIT_ACTION );
        fileMenu.add(menuItem);
        menuItem.addActionListener(this);
        menuBar.add(fileMenu);

        JMenu osMenu = new JMenu("Operating Systems");
        fileMenu.setMnemonic(KeyEvent.VK_O);

        menuItem = new JMenuItem("Add OS", KeyEvent.VK_A);
        menuItem.setAccelerator( KeyStroke.getKeyStroke(KeyEvent.VK_PLUS, ActionEvent.CTRL_MASK) );
        menuItem.setEnabled(false);
        menuItem.setActionCommand( ADD_OS_ACTION );
        menuItem.addActionListener(this);
        osMenu.add(menuItem);

        menuItem = new JMenuItem("Remove OS", KeyEvent.VK_R);
        menuItem.setAccelerator( KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, ActionEvent.CTRL_MASK) );
        menuItem.setEnabled(false);
        menuItem.addActionListener(this);
        osMenu.add(menuItem);
        menuBar.add(osMenu);

        return menuBar;
    }

    private void retrieveTests(File testDefsFile)
    {
        _testNames = new ArrayList();

        try
        {
            TestDefinitionRepository testRepository = new TestDefinitionRepository( testDefsFile.toURL() );
        }
        catch (Exception e)
        {
            JOptionPane.showMessageDialog(this,"Failed to retrieve test definitions");
            e.printStackTrace(System.err);
        }
    }

    private void retrieveProducts(File nodeConfigFile)
    {
        _productNames = new ArrayList();
    }

    private void createEmptySelections()
    {
        getJMenuBar().getMenu(0).getMenuComponent(2).setEnabled(true);
        getJMenuBar().getMenu(0).getMenuComponent(3).setEnabled(true);
        getJMenuBar().getMenu(1).getMenuComponent(0).setEnabled(true);
        getJMenuBar().getMenu(1).getMenuComponent(1).setEnabled(true);

        _selections = new Hashtable();
        _osNames = new ArrayList();

        if ( _selectionTree == null )
        {
            _rootNode = new DefaultMutableTreeNode("Test Selections");
            _selectionTree = new JTree(_rootNode);

            this.getContentPane().add( _selectionTree );
        }
    }

    private String getOSName()
    {
        return new InputPromptDialog("Operating System:", "add", this).getData();
    }

    private void addOS(String osName)
    {
        Hashtable products = null;
        Hashtable tests = null;

        _selections.put( osName, products = new Hashtable() );

        DefaultMutableTreeNode osNode = new DefaultMutableTreeNode( osName );
        _rootNode.add( osNode );

        for (int count=0;count<_productNames.size();count++)
        {
            products.put( (String)_productNames.get(count), tests = new Hashtable() );

            DefaultMutableTreeNode productNode;
            osNode.add( productNode = new DefaultMutableTreeNode( (String)_productNames.get(count) ) );

            for (int testCount=0;testCount<_testNames.size();testCount++)
            {
                tests.put( (String)_testNames.get(testCount), new Boolean(false) );
                productNode.add( new DefaultMutableTreeNode( (String)_testNames.get(testCount)) );
            }
        }

        _selectionTree.updateUI();
    }

    /**
     * Invoked when an action occurs.
     */
    public void actionPerformed(ActionEvent e)
    {
        String actionCommand = e.getActionCommand();

        if ( actionCommand.equals( OPEN_TEST_DEFS_ACTION ) )
        {
            JFileChooser jfc = new JFileChooser();

            jfc.setDialogTitle("Open Test Definitions File...");
            int returnVal = jfc.showOpenDialog(this);

            if ( returnVal == JFileChooser.APPROVE_OPTION )
            {
                retrieveTests( jfc.getSelectedFile() );

                if ( _productNames != null )
                {
                    createEmptySelections();
                }
            }
        }

        if ( actionCommand.equals( OPEN_NODE_CONFIG_ACTION ) )
        {
            JFileChooser jfc = new JFileChooser();

            jfc.setDialogTitle("Open Test Node Configuration File...");

            int returnVal = jfc.showOpenDialog(this);

            if ( returnVal == JFileChooser.APPROVE_OPTION )
            {
                retrieveProducts( jfc.getSelectedFile() );

                if ( _testNames != null )
                {
                    createEmptySelections();
                }
            }
        }

        if ( actionCommand.equals( ADD_OS_ACTION ) )
        {
            addOS(getOSName());
        }

        if ( actionCommand.equals( EXIT_ACTION ) )
        {
            dispose();
            System.exit(0);
        }
    }

    public static void main(String[] args)
    {
        new TestSelector();
    }
}
