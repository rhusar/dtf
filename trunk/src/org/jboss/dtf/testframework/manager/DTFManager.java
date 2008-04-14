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
// $Id: DTFManager.java 170 2008-03-25 18:59:26Z jhalliday $

package org.jboss.dtf.testframework.manager;

import org.jboss.dtf.testframework.nameservice.*;
import org.jboss.dtf.testframework.serviceregister.*;

import java.rmi.*;
import java.util.*;
import java.io.*;
import java.net.*;

/**
 *
 * @author  administrator
 */
public class DTFManager extends javax.swing.JFrame implements ConsoleLogger {

    private NameServiceInterface        _nameService = null;
    private ServiceRegisterInterface    _serviceRegister = null;

    /** Creates new form DTFManager */
    public DTFManager(NameServiceInterface nameService, ServiceRegisterInterface serviceRegister) {

        _nameService = nameService;
        _serviceRegister = serviceRegister;
        initComponents();
        initCustomComponents();

        log("Distributed Test Framework Manager");
        log("Started...");
    }

    public void log(String text)
    {
        _consoleText.append(new java.util.Date().toString() + " : " + text + "\n\r");
        _consoleText.setSelectionStart(_consoleText.getText().length());
    }

    private void initialisePlugins()
    {
        String listData;
        Properties props = new Properties();

        try
        {
        	FileInputStream fin = new FileInputStream("plugins.conf");
        	props.load(fin);
        	fin.close();
        }
        catch (IOException e)
        {
			javax.swing.JOptionPane.showMessageDialog(this,"Failed to open plugins.conf");
        }


        int numProperties = Integer.parseInt(props.getProperty("NumberOfPlugins","0"));

        for (int count=0;count<numProperties;count++)
        {
            listData = props.getProperty("PlugIn_"+count);

            if (listData == null)
            {
                System.out.println("Error while parsing plugins.conf");
                javax.swing.JOptionPane.showMessageDialog(this,"Error while loading plugins.conf");
                System.exit(0);
            }

            loadPlugin(listData);
        }
    }

    private void loadPlugin(String pluginFileName)
    {
    	try
    	{
	        File file = new File(pluginFileName);

	        URLClassLoader urlClassLoader = new URLClassLoader(new URL[] { file.toURL() });

	        Class pluginClass = urlClassLoader.loadClass(PluginManager.stripExtension(pluginFileName));

	        DTFManagerPlugin plugin = (DTFManagerPlugin)pluginClass.newInstance();
	        System.out.println("Successfully loaded '"+plugin.getName()+"'");
	    }
	    catch (Exception e)
	    {
	    	System.out.println("Failed to load plugin in '"+pluginFileName+"'");
	   	}
    }

    private void initCustomComponents()
    {
        _coordinatorBrowser = new CoordinatorBrowser(this,this,_nameService);
        _coordinatorBrowser.setIconifiable(true);
        _coordinatorBrowser.setResizable(true);
        jDesktopPane1.add(_coordinatorBrowser, javax.swing.JLayeredPane.DEFAULT_LAYER);
        _coordinatorBrowser.setBounds(0, 0, 260, 400);

        _testNodeBrowser = new TestNodeBrowser(this,this,_nameService,_serviceRegister);
        _testNodeBrowser.setIconifiable(true);
        _testNodeBrowser.setResizable(true);
        jDesktopPane1.add(_testNodeBrowser, javax.swing.JLayeredPane.DEFAULT_LAYER);
        _testNodeBrowser.setBounds(260, 0, 260, 400);

	_testSelector = new CoordinatorGUI("http://bob/testdefs.xml");
        jDesktopPane1.add(_testSelector, javax.swing.JLayeredPane.DEFAULT_LAYER);
        _testSelector.setBounds(520, 0, 260, 400);
    }
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        _menuBar = new javax.swing.JMenuBar();
        _coordinatorMenu = new javax.swing.JMenu();
        jMenuItem1 = new javax.swing.JMenuItem();
        jDesktopPane1 = new javax.swing.JDesktopPane();
        _consoleFrame = new javax.swing.JInternalFrame();
        _innerScrollPane = new javax.swing.JScrollPane();
        _consoleText = new javax.swing.JTextArea();

        _coordinatorMenu.setText("Menu");
        jMenuItem1.setText("Item");
        _coordinatorMenu.add(jMenuItem1);
        _menuBar.add(_coordinatorMenu);

        setTitle("Distributed Test Framework Manager v1.0a");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                exitForm(evt);
            }
        });

        jDesktopPane1.setPreferredSize(new java.awt.Dimension(800, 600));
        jDesktopPane1.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                jDesktopPane1ComponentResized(evt);
            }
        });

        _consoleFrame.setTitle("Console");
        _consoleFrame.setIconifiable(true);
        _consoleFrame.setVisible(true);
        _consoleFrame.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                _consoleFrameComponentResized(evt);
            }
        });

        _innerScrollPane.setVerticalScrollBarPolicy(javax.swing.JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        _innerScrollPane.setHorizontalScrollBarPolicy(javax.swing.JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        _innerScrollPane.setViewportView(_consoleText);

        _consoleFrame.getContentPane().add(_innerScrollPane, java.awt.BorderLayout.CENTER);

        jDesktopPane1.add(_consoleFrame, javax.swing.JLayeredPane.DEFAULT_LAYER);
        _consoleFrame.setBounds(0, 340, 800, 130);

        setJMenuBar(_menuBar);

        getContentPane().add(jDesktopPane1, java.awt.BorderLayout.CENTER);

        pack();
    }//GEN-END:initComponents

    private void jDesktopPane1ComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_jDesktopPane1ComponentResized
        System.out.println("Component Resized");
        java.awt.Component c = evt.getComponent();
        _consoleFrame.setBounds(0,c.getHeight()-160,c.getWidth(),160);
    }//GEN-LAST:event_jDesktopPane1ComponentResized

    private void _consoleFrameComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event__consoleFrameComponentResized
    }//GEN-LAST:event__consoleFrameComponentResized

    /** Exit the Application */
    private void exitForm(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_exitForm
        System.exit(0);
    }//GEN-LAST:event_exitForm

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        if (args.length == 0)
        {
            System.out.println("Usage: DTFManager [ uri://NameService ]");
            System.exit(1);
        }

        try
        {
			System.out.println("Looking up NameService...");
            NameServiceInterface nameService = (NameServiceInterface)Naming.lookup(args[0]);

			System.out.println("Looking up ServiceRegister...");
			ServiceRegisterInterface register = (ServiceRegisterInterface)nameService.lookup("/ServiceRegister");

            new DTFManager(nameService,register).show();
        }
        catch (Exception e)
        {
            System.out.println("Exception: "+e.toString());
        }

    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuBar _menuBar;
    private javax.swing.JMenu _coordinatorMenu;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JDesktopPane jDesktopPane1;
    private javax.swing.JInternalFrame _consoleFrame;
    private javax.swing.JScrollPane _innerScrollPane;
    private javax.swing.JTextArea _consoleText;
    // End of variables declaration//GEN-END:variables
    private CoordinatorBrowser      _coordinatorBrowser;
    private TestNodeBrowser         _testNodeBrowser;
    private CoordinatorGUI          _testSelector;
}
