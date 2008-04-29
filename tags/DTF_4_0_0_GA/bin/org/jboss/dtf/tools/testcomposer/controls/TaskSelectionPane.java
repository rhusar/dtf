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
 * $Id: TaskSelectionPane.java 170 2008-03-25 18:59:26Z jhalliday $
 */

package org.jboss.dtf.tools.testcomposer.controls;

import org.jboss.dtf.tools.testcomposer.TestComposer;
import org.jboss.dtf.tools.testcomposer.TaskDefinitionTransferWrapper;
import org.jboss.dtf.tools.testcomposer.propertymodels.PerformPropertyModel;
import org.jboss.dtf.tools.testcomposer.frames.TestBrowserCellRenderer;
import org.jboss.dtf.testframework.coordinator.TaskDefinition;

import javax.swing.*;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import java.awt.*;
import java.awt.dnd.*;
import java.util.Iterator;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.ArrayList;

public class TaskSelectionPane extends JPanel implements TreeSelectionListener, DragGestureListener, DragSourceListener
{
    private JTree                   _taskTree = null;
    private DefaultMutableTreeNode  _treeRoot = null;
    private String                  _fullTaskName = null;
    private TaskDefinition          _lastSelectedTask = null;
    private JTable                  _propertiesTable = null;
    private DragSource              _dragSource = null;
    private DragGestureRecognizer   _gestureRecognizer = null;

    public TaskSelectionPane()
    {
        super();

        this.setLayout(new BorderLayout());
        this.add(new JLabel("Tasks:"),BorderLayout.NORTH);
        this.add(new JScrollPane(_taskTree = new JTree(_treeRoot = new DefaultMutableTreeNode("Task Definitions"))),BorderLayout.CENTER);

        this.add(_propertiesTable = new JTable(),BorderLayout.SOUTH);
        _taskTree.setCellRenderer(new TestBrowserCellRenderer());
        _propertiesTable.setDefaultRenderer(ArrayList.class, new StringArrayCellRenderer());

        _taskTree.addTreeSelectionListener(this);

        _dragSource = new DragSource();
        _gestureRecognizer = _dragSource.createDefaultDragGestureRecognizer(_taskTree,DnDConstants.ACTION_MOVE,this);
    }

    public void populate()
    {
        HashMap taskMap = TestComposer.TestSetData.getTaskDefinitionsMap();

        Iterator i = taskMap.keySet().iterator();
        traverse(i,taskMap,_treeRoot);

        _taskTree.invalidate();
        _taskTree.repaint();
    }

    public void controlSelected(TaskControl t)
    {
        if (t instanceof PerformTaskControl)
        {
            _propertiesTable.setModel(new PerformPropertyModel(t));
        }
    }
    private void traverse(Iterator i, HashMap taskMap, DefaultMutableTreeNode current)
    {
        DefaultMutableTreeNode temp = null;

        while (i.hasNext())
        {
            String taskName = (String)i.next();
            Object subTaskMap = taskMap.get(taskName);

            if (subTaskMap instanceof HashMap)
            {
                current.add(temp = new DefaultMutableTreeNode(taskName));
            }
            else
            {
                current.add(temp = new DefaultMutableTreeNode(subTaskMap));
            }

            /**
             * If this node has a subtree then traverse that tree
             */
            if (subTaskMap instanceof HashMap)
            {
                traverse( ((HashMap)subTaskMap).keySet().iterator(), ((HashMap)subTaskMap), temp);
            }
            else
            {
                addProperties( (TaskDefinition)subTaskMap, temp );
            }
        }
    }

    private void addProperties( TaskDefinition subTaskMap, DefaultMutableTreeNode temp )
    {
        DefaultMutableTreeNode runner = null;

        temp.add(new DefaultMutableTreeNode("Classname:"+subTaskMap.getClassName()));
        temp.add(runner = new DefaultMutableTreeNode("Runner:"+subTaskMap.getRunner()));

        /**
         * If the runner has parameters add them as child nodes
         */
        Hashtable runnerParams = subTaskMap.getRunnerParameters();
        if ( (runnerParams != null) && (!runnerParams.isEmpty()) )
        {
            Iterator i = runnerParams.keySet().iterator();

            while (i.hasNext())
            {
                String name = (String)i.next();
                String value = (String)runnerParams.get(name);

                runner.add(new DefaultMutableTreeNode(name+"="+value));
            }
        }

        temp.add(new DefaultMutableTreeNode("Type:"+subTaskMap.getTypeText()));
        temp.add(new DefaultMutableTreeNode("Timeout:"+subTaskMap.getTimeout()));
    }

    private TreeNode treeHasChild(String name, TreeNode node)
    {
        for (int count=0;count<node.getChildCount();count++)
        {
            if (node.getChildAt(count).equals(name))
            {
                return(node.getChildAt(count));
            }
        }

        return(null);
    }

    /**
     * Called whenever the value of the selection changes.
     * @param e the event that characterizes the change.
     */
    public void valueChanged(TreeSelectionEvent e)
    {
        Object[] path = e.getPath().getPath();
        Object lastSelected = ((DefaultMutableTreeNode)path[path.length - 1]).getUserObject();

        if (lastSelected instanceof TaskDefinition)
        {
            String taskName = "";

            for (int count=1;count<path.length;count++)
            {
                if (count != 1)
                    taskName += "/";

                taskName += path[count];
            }

            _fullTaskName = taskName;
            _lastSelectedTask = (TaskDefinition)lastSelected;
        }
        else
        {
            _lastSelectedTask = null;
        }
    }

    public String getLastSelectedTasksFullId()
    {
        return(_fullTaskName);
    }

    public TaskDefinition getLastSelectedTask()
    {
        return(_lastSelectedTask);
    }

    /**
     * Called as the hotspot enters a platform dependent drop site.
     * This method is invoked when the following conditions are true:
     * <UL>
     * <LI>The logical cursor's hotspot initially intersects
     * a GUI <code>Component</code>'s  visible geometry.
     * <LI>That <code>Component</code> has an active
     * <code>DropTarget</code> associated with it.
     * <LI>The <code>DropTarget</code>'s registered
     * <code>DropTargetListener</code> dragEnter() method is invoked and
     * returns successfully.
     * <LI>The registered <code>DropTargetListener</code> invokes
     * the <code>DropTargetDragEvent</code>'s acceptDrag() method to
     * accept the drag based upon interrogation of the source's
     * potential drop action(s) and available data types
     * (<code>DataFlavor</code>s).
     * </UL>
     *<P>
     *@param dsde the <code>DragSourceDragEvent</code>
     */

    public void dragEnter(DragSourceDragEvent dsde)
    {
    }

    /**
     * Called as the hotspot moves over a platform dependent drop site.
     * This method is invoked when the following conditions
     * are true:
     *<UL>
     *<LI>The cursor's logical hotspot has moved but still
     * intersects the visible geometry of the <code>Component</code>
     * associated with the previous dragEnter() invocation.
     * <LI>That <code>Component</code> still has a
     * <code>DropTarget</code> associated with it.
     * <LI>That <code>DropTarget</code> is still active.
     * <LI>The <code>DropTarget</code>'s registered
     * <code>DropTargetListener</code> dragOver() method
     * is invoked and returns successfully.
     * <LI>The <code>DropTarget</code> does not reject
     * the drag via rejectDrag()
     * </UL>
     * <P>
     * @param dsde the <code>DragSourceDragEvent</code>
     */

    public void dragOver(DragSourceDragEvent dsde)
    {
    }

    /**
     * Called when the user has modified the drop gesture.
     * This method is invoked when the state of the input
     * device(s) that the user is interacting with changes.
     * Such devices are typically the mouse buttons or keyboard
     * modifiers that the user is interacting with.
     * <P>
     * @param dsde the <code>DragSourceDragEvent</code>
     */

    public void dropActionChanged(DragSourceDragEvent dsde)
    {
    }

    /**
     * Called as the hotspot exits a platform dependent drop site.
     * This method is invoked when the following conditions
     * are true:
     * <UL>
     * <LI>The cursor's logical hotspot no longer
     * intersects the visible geometry of the <code>Component</code>
     * associated with the previous dragEnter() invocation.
     * </UL>
     * OR
     * <UL>
     * <LI>The <code>Component</code> that the logical cursor's hotspot
     * intersected that resulted in the previous dragEnter() invocation
     * no longer has an active <code>DropTarget</code> or
     * <code>DropTargetListener</code> associated with it.
     * </UL>
     * OR
     * <UL>
     * <LI> The current <code>DropTarget</code>'s
     * <code>DropTargetListener</code> has invoked rejectDrag()
     * since the last dragEnter() or dragOver() invocation.
     * </UL>
     * <P>
     * @param dse the <code>DragSourceEvent</code>
     */

    public void dragExit(DragSourceEvent dse)
    {
    }

    /**
     * This method is invoked to signify that the Drag and Drop
     * operation is complete. The getDropSuccess() method of
     * the <code>DragSourceDropEvent</code> can be used to
     * determine the termination state. The getDropAction() method
     * returns the operation that the <code>DropTarget</code>
     * selected (via the DropTargetDropEvent acceptDrop() parameter)
     * to apply to the Drop operation. Once this method is complete, the
     * current <code>DragSourceContext</code> and
     * associated resources become invalid.
     * <P>
     * @param dsde the <code>DragSourceDropEvent</code>
     */

    public void dragDropEnd(DragSourceDropEvent dsde)
    {
    }

    /**
     * A <code>DragGestureRecognizer</code> has detected
     * a platform-dependent drag initiating gesture and
     * is notifying this listener
     * in order for it to initiate the action for the user.
     * <P>
     * @param dge the <code>DragGestureEvent</code> describing
     * the gesture that has just occurred
     */

    public void dragGestureRecognized(DragGestureEvent dge)
    {
        TaskDefinition selected = this.getLastSelectedTask();

        if (selected != null)
        {
            _dragSource.startDrag (dge, DragSource.DefaultMoveDrop, TestBrowserCellRenderer.getTaskDefImage(), new Point(0,0), new TaskDefinitionTransferWrapper(getLastSelectedTasksFullId()), this);
        }
    }
}
