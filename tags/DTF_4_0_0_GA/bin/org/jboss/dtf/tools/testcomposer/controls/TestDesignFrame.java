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
 * $Id: TestDesignFrame.java 170 2008-03-25 18:59:26Z jhalliday $
 */

package org.jboss.dtf.tools.testcomposer.controls;

import org.jboss.dtf.testframework.coordinator.TestDefinition;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class TestDesignFrame extends JInternalFrame implements ActionListener
{
    private final static String PERFORM_MENU_ITEM = "Perform Task";
    private final static String START_MENU_ITEM = "Start Task";

    private final static String NEW_TASK_MENU_ITEM = "New Task";

    private TestDesignerPane    _designerPane = null;
    private TaskSelectionPane   _taskList = null;
    private TestDefinition      _testDefinition = null;

    public TestDesignFrame(TestDefinition testDef)
    {
        super("Test Designer - "+testDef.getId(),
              true, //resizable
              true, //closable
              true, //maximizable
              true);//iconifiable

        this.setBackground(Color.white);
        this.setSize(500,300);

        JScrollPane scroller = new JScrollPane(_designerPane = new TestDesignerPane(this,testDef), JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

        _designerPane.setupComponentPositions();
        _taskList = new TaskSelectionPane();
        _testDefinition = testDef;
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, _taskList, scroller);
        this.getContentPane().add(splitPane);
        this.setJMenuBar(createMenuBar());
        this.populate();

        _taskList.setDropTarget(_designerPane.getDropTarget());

        this.show();
    }

    public void populate()
    {
        _taskList.populate();
    }

    protected JMenuBar createMenuBar()
    {
        JMenuBar menuBar = new JMenuBar();
        JMenu menu = new JMenu("Task");
        JMenuItem item = null;

        menuBar.add(menu);
        ButtonGroup group = new ButtonGroup();
        menu.add(item = new JRadioButtonMenuItem(PERFORM_MENU_ITEM));
        item.addActionListener(this);
        item.setMnemonic(KeyEvent.VK_P);
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, ActionEvent.CTRL_MASK));
        item.setSelected(true);
        group.add(item);
        menu.add(item = new JRadioButtonMenuItem(START_MENU_ITEM));
        item.addActionListener(this);
        item.setMnemonic(KeyEvent.VK_S);
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
        group.add(item);

        menu.addSeparator();

        menu.add(item = new JMenuItem(NEW_TASK_MENU_ITEM));
        item.addActionListener(this);
        item.setMnemonic(KeyEvent.VK_N);

        return(menuBar);
    }

    public TaskSelectionPane getTaskSelectionPane()
    {
        return(_taskList);
    }

    public boolean isPerformSelected()
    {
        return(this.getJMenuBar().getMenu(0).getItem(0).isSelected());
    }

    /**
     * Invoked when an action occurs.
     */
    public void actionPerformed(ActionEvent e)
    {
        String actionCommand = e.getActionCommand();
    }
}
