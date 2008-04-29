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
 * $Id: TestComposer.java 170 2008-03-25 18:59:26Z jhalliday $
 */

package org.jboss.dtf.tools.testcomposer;

import org.jboss.dtf.tools.testcomposer.controls.TestDesignFrame;
import org.jboss.dtf.tools.testcomposer.frames.TestBrowser;
import org.jboss.dtf.testframework.coordinator.TestDefinition;

import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;

/**
 * @todo Add ability to specify known task runners and change edit box to combo box
 */
public class TestComposer extends JFrame implements ActionListener
{
	public final static String VERSION_TEXT = "v1.0";

    private final static int PREVIOUS_FILE_COUNT = 5;
    private final static String PREVIOUS_FILE_MENU_ITEM = "PREVIOUS_FILE_";

    private final static String NEW_MENU_ITEM = "New";
    private final static String OPEN_MENU_ITEM = "Open";
    private final static String EXIT_MENU_ITEM = "Exit";
    private final static String HELP_MENU_ITEM = "Help";
    private final static String ABOUT_MENU_ITEM = "About";
    private final static String CASCADE_MENU_ITEM = "Cascade";
    private final static String TILE_MENU_ITEM = "Tile";
    private final static String FRAME_TITLE = "Distributed Test Framework - Test Composer "+VERSION_TEXT;

    public static UtilityFrame UtilityControlsFrame = new UtilityFrame();

    public static TestSetData     TestSetData = new TestSetData();

    private ArrayList       _previousFiles = new ArrayList(PREVIOUS_FILE_COUNT);
    private JDesktopPane    _desktop = null;
    private ArrayList       _frames = new ArrayList();

    public TestComposer()
    {
        super();

        this.setTitle(FRAME_TITLE);

        createDesktop();
        readPreviousFilesList();

        _desktop.add(UtilityControlsFrame);

        this.pack();
        this.setJMenuBar(createMenuBar());
        UtilityControlsFrame.setVisible(true);
        UtilityControlsFrame.setLocation(getWidth() - UtilityControlsFrame.getWidth(),0);
        this.show();
    }

    private void readPreviousFilesList()
    {
        try
        {
            ObjectInputStream in = new ObjectInputStream( new FileInputStream("previousfiles.bin") );
            _previousFiles = (ArrayList)in.readObject();
            in.close();
        }
        catch (Exception e)
        {
            System.err.println("Failed to read previous files file");
        }
    }

    private void createPreviousFilesMenu(JMenu menu)
    {
        JMenuItem item;
        for (int count=0;count<_previousFiles.size();count++)
        {
            File f = (File)_previousFiles.get(count);

            menu.add( item = new JMenuItem(count+" "+f.getPath()) );
            item.setMnemonic((char)('0'+count));
            item.setActionCommand(PREVIOUS_FILE_MENU_ITEM+count);
            item.addActionListener(this);
        }

        if (_previousFiles.size() > 0)
        {
            menu.addSeparator();
        }
    }

    public JMenuBar createMenuBar()
    {
        JMenuBar menuBar = new JMenuBar();
        JMenuItem item = null;
        JMenu menu = new JMenu("Test Set");
        menu.setMnemonic(KeyEvent.VK_T);
        menuBar.add(menu);
        menu.add(item = new JMenuItem(NEW_MENU_ITEM));
        item.setMnemonic(KeyEvent.VK_N);
        item.addActionListener(this);
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.ALT_MASK));
        menu.add(item = new JMenuItem(OPEN_MENU_ITEM));
        item.setMnemonic(KeyEvent.VK_O);
        item.addActionListener(this);
        menu.add(item = new JMenuItem("Save"));
        item.setEnabled(false);
        item.addActionListener(this);
        item.setMnemonic(KeyEvent.VK_S);
        menu.addSeparator();
        createPreviousFilesMenu(menu);
        menu.add(item = new JMenuItem(EXIT_MENU_ITEM));
        item.addActionListener(this);
        item.setMnemonic(KeyEvent.VK_X);
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, ActionEvent.ALT_MASK));

        menu = new JMenu("Window");
        menu.setMnemonic(KeyEvent.VK_W);
        menuBar.add(menu);
        menu.add(item = new JMenuItem(CASCADE_MENU_ITEM));
        item.setMnemonic(KeyEvent.VK_C);
        item.addActionListener(this);
        menu.add(item = new JMenuItem(TILE_MENU_ITEM));
        item.setMnemonic(KeyEvent.VK_T);
        item.addActionListener(this);

        menuBar.add(Box.createHorizontalGlue());

        menu = new JMenu(HELP_MENU_ITEM);
        menu.setMnemonic(KeyEvent.VK_H);
        menuBar.add(menu);
        menu.add(item = new JMenuItem(ABOUT_MENU_ITEM));
        item.addActionListener(this);
        item.setMnemonic(KeyEvent.VK_A);

        return(menuBar);
    }

    /**
     * Invoked when an action occurs.
     */
    public void actionPerformed(ActionEvent e)
    {
        String actionCommand = e.getActionCommand();

        /**
         * If the New menu item is selected then
         * enable the Tests menu
         */
        if (actionCommand.equals(NEW_MENU_ITEM))
        {
            closeFrames();
            TestSetData.clear();
            displayTestBrowser();
        }
        else
        {
            if (actionCommand.equals(OPEN_MENU_ITEM))
            {
                JFileChooser fileChooser = new JFileChooser();
                int returnValue = fileChooser.showOpenDialog(this);

                if (returnValue == JFileChooser.APPROVE_OPTION)
                {
                    openFile(fileChooser.getSelectedFile());
                }
            }
            else
            {
                if ( actionCommand.startsWith(PREVIOUS_FILE_MENU_ITEM) )
                {
                    int index = Integer.parseInt(actionCommand.substring(PREVIOUS_FILE_MENU_ITEM.length()));

                    openFile( (File)_previousFiles.get(index) );
                }
                else
                {
                    if ( actionCommand.equals(ABOUT_MENU_ITEM) )
                    {
						//AboutDialog abt = new AboutDialog(this, true);
                    }
                    else
                    {
                        if ( (actionCommand.equals(EXIT_MENU_ITEM)) &&
                             ( JOptionPane.showConfirmDialog(this,"Are you sure you wish to quit?","Warning",JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION ) )
                        {
                            System.exit(0);
                        }
                    }
                }
            }
        }
    }

    private void openFile(File f)
    {
        closeFrames();

        try
        {
            TestSetData.initialise(f.toURL());

            addToPreviousFilesOpened(f);
            displayTestBrowser();
        }
        catch (Exception ex)
        {
            JOptionPane.showMessageDialog(this,"An error occurred while attempting to import the test definitions file");
            ex.printStackTrace(System.err);
        }
    }

    protected void addToPreviousFilesOpened(File f)
    {
        if ( !_previousFiles.contains(f) )
        {
            _previousFiles.add(f);
            if (_previousFiles.size() > PREVIOUS_FILE_COUNT)
            {
                _previousFiles.remove(0);
            }

            try
            {
                ObjectOutputStream out = new ObjectOutputStream( new FileOutputStream("previousfiles.bin") );
                out.writeObject(_previousFiles);
                out.close();
            }
            catch (Exception e)
            {
                System.err.println("Failed to write previous files file");
            }

            setJMenuBar(createMenuBar());
        }
    }

    public void displayTestBrowser()
    {
        TestBrowser testBrowser = new TestBrowser(this);
        _desktop.add(testBrowser);
        testBrowser.setLocation(0,0);
        testBrowser.setVisible(true);
    }

    public JDesktopPane getDesktop()
    {
        return _desktop;
    }

    private void createDesktop()
    {
        _desktop = new JDesktopPane();
        _desktop.setPreferredSize(new Dimension(800,400));
        _desktop.setMinimumSize(new Dimension(200,100));
        this.setContentPane(_desktop);
    }

    private void closeFrames()
    {
        for (int count=0;count<_frames.size();count++)
        {
            TestDesignFrame frame = (TestDesignFrame)_frames.get(count);

            if (frame != null)
                frame.dispose();

            _frames.remove(count);
        }
    }

    public void createTestDesignFrame(TestDefinition test)
    {
        TestDesignFrame frame = new TestDesignFrame(test);
        frame.setVisible(true); //necessary as of kestrel
        frame.setLocation(50,50);
        _desktop.add(frame);
        _frames.add(frame);

        try
        {
          frame.setSelected(true);
        }
        catch (java.beans.PropertyVetoException e)
        {
        }
    }

    public static void main(String [] args)
    {
        TestComposer t = new TestComposer();

        t.addWindowListener(new WindowAdapter() {
               public void windowClosing(WindowEvent e) {System.exit(0);}
           });
    }
}
