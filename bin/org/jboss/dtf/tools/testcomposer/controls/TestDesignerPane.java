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
 * $Id: TestDesignerPane.java 170 2008-03-25 18:59:26Z jhalliday $
 */

package org.jboss.dtf.tools.testcomposer.controls;

import org.jboss.dtf.testframework.coordinator.TaskDefinition;
import org.jboss.dtf.testframework.coordinator.TestDefinition;
import org.jboss.dtf.testframework.coordinator.NoAssociatedData;
import org.jboss.dtf.testframework.coordinator.TaskNotFound;
import org.jboss.dtf.testframework.coordinator.actions.WaitForAction;
import org.jboss.dtf.testframework.coordinator.actions.TerminateAction;
import org.jboss.dtf.testframework.coordinator.actions.StartAction;
import org.jboss.dtf.testframework.coordinator.actions.PerformAction;
import org.jboss.dtf.tools.testcomposer.*;
import org.jboss.dtf.tools.testcomposer.forms.PerformTaskForm;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.HashMap;

public class TestDesignerPane extends JPanel implements MouseListener, ComponentListener, ActionListener, DropTargetListener, ComponentSizeChangeListener
{
    private final static int EXTRA_VERTICAL_GAP = 15;
    private final static String START_MARKER = "START";

    private final static String TERMINATE_MENU_ITEM = "Terminate Task";
    private final static String WAIT_FOR_MENU_ITEM = "Wait for Task";
    private final static String REMOVE_MENU_ITEM = "Remove";
    private final static String ADD_TO_PALETTE_MENU_ITEM = "Add to Palette";
    private final static String PROPERTIES_MENU_ITEM = "Properties";

    private ComposerComponent _previousSelected = null;

    private ArrayList   _connectors = new ArrayList();
    private ArrayList   _componentLadder = new ArrayList();
    private Component   _menuContext = null;
    private HashMap     _runtimeIds = new HashMap();
    private TestDefinition _testDefinition = null;
    private int         _position = 0;
    private DropTarget  _dropTarget = null;
    protected TestDesignFrame _testDesigner = null;

    public TestDesignerPane(TestDesignFrame testDesigner, TestDefinition testDef)
    {
        super();

        _testDesigner = testDesigner;

        createStartMarker();
        setupComponentPositions();

        this.setBackground(Color.white);
        this.setLayout(null);
        this.addMouseListener(this);
        this.addComponentListener(this);
        this.setMinimumSize(new Dimension(600,800));
        this.setPreferredSize(new Dimension(600,800));
        this.setSize(600,800);

        try
        {
            populate(testDef);
        }
        catch (TaskNotFound e)
        {
            System.err.println("Error: "+e);
            System.exit(0);
        }

        _testDefinition = testDef;

        _dropTarget = new DropTarget(this,this);
    }

    public DropTarget getDropTarget()
    {
        return(_dropTarget);
    }

    private void increasePosition()
    {
        _position++;
    }

    private int getCurrentPosition()
    {
        return(_position);
    }

    private void populate(TestDefinition testDef) throws TaskNotFound
    {
        try
        {
            ArrayList actions = testDef.getActionList();

            for (int count=0;count<actions.size();count++)
            {
                TaskDefinition taskDef = null;
                org.jboss.dtf.testframework.coordinator.Action currentAction = (org.jboss.dtf.testframework.coordinator.Action)actions.get(count);


                switch (currentAction.getType())
                {
                    case org.jboss.dtf.testframework.coordinator.Action.PERFORM_TASK :
                        try
                        {
                            taskDef = TestComposer.TestSetData.getTaskDefinition(currentAction.getAssociatedTaskId());
                        }
                        catch (TaskNotFound e)
                        {
                            /**
                             * If the task was not found then see if the task can be found in the tests test group
                             */
                            taskDef = TestComposer.TestSetData.getTaskDefinition(testDef.getGroupId() + '/' + currentAction.getAssociatedTaskId());
                        }

                        addPerformComponent(taskDef,currentAction,false);
                        break;

                    case org.jboss.dtf.testframework.coordinator.Action.START_TASK :
                        try
                        {
                            taskDef = TestComposer.TestSetData.getTaskDefinition(currentAction.getAssociatedTaskId());
                        }
                        catch (TaskNotFound e)
                        {
                            /**
                             * If the task was not found then see if the task can be found in the tests test group
                             */
                            taskDef = TestComposer.TestSetData.getTaskDefinition(testDef.getGroupId() + '/' + currentAction.getAssociatedTaskId());
                        }

                        addStartComponent(taskDef,currentAction,false);
                        break;

                    case org.jboss.dtf.testframework.coordinator.Action.WAIT_FOR_TASK :
                        addWaitForComponent(currentAction,false);
                        break;

                    case org.jboss.dtf.testframework.coordinator.Action.TERMINATE_TASK :
                        addTerminateComponent(currentAction,false);
                        break;
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
            JOptionPane.showMessageDialog(this,"An error occurred while parsing the test definition");
        }
    }

    private void createStartMarker()
    {
        Marker start = new Marker();
        start.setMarkerName(START_MARKER);

        insertIntoMatrix(0, start);
    }

    protected JPopupMenu createPopup()
    {
        JMenuItem item = null;
        JPopupMenu popupMenu = new JPopupMenu();

        popupMenu.add(item = new JMenuItem(REMOVE_MENU_ITEM));
        item.addActionListener(this);
        popupMenu.add(item = new JMenuItem(ADD_TO_PALETTE_MENU_ITEM));
        item.addActionListener(this);
        popupMenu.add(item = new JMenuItem(PROPERTIES_MENU_ITEM));
        item.addActionListener(this);

        return(popupMenu);
    }

    protected void appendStartComponentPopupMenu(JPopupMenu menu)
    {
        JMenuItem item = null;

        menu.addSeparator();
        menu.add(item = new JMenuItem(WAIT_FOR_MENU_ITEM));
        item.addActionListener(this);
        menu.add(item = new JMenuItem(TERMINATE_MENU_ITEM));
        item.addActionListener(this);
    }

    /**
     * Invoked when a mouse button has been pressed on a component.
     */
    public void mousePressed(MouseEvent e)
    {
        maybeShowPopup(e);

        Object obj = e.getComponent();

        if (obj instanceof TaskControl)
        {
            System.out.println("Setting up for task");
            TestComposer.UtilityControlsFrame.setupForTask((TaskControl)obj);
            _testDesigner.getTaskSelectionPane().controlSelected((TaskControl)obj);
        }
    }

    /**
     * Invoked when a mouse button has been released on a component.
     */
    public void mouseReleased(MouseEvent e)
    {
        maybeShowPopup(e);
    }

    /**
     * Invoked when the mouse enters a component.
     */
    public void mouseEntered(MouseEvent e)
    {
    }

    /**
     * Invoked when the mouse exits a component.
     */
    public void mouseExited(MouseEvent e)
    {
    }

    /**
     * Invoked when the mouse has been clicked on a component.
     */
    public void mouseClicked(MouseEvent e)
    {
    }

    protected void maybeShowPopup(MouseEvent e)
    {
        System.out.println("maybeShowPopup: "+e.isPopupTrigger());
        if ( e.isPopupTrigger() )
        {
            if (e.getComponent() instanceof ComposerComponent)
            {
                ComposerComponent component = (ComposerComponent)e.getComponent();
                JPopupMenu menu = createPopup();

                if (component instanceof StartTaskControl)
                {
                    appendStartComponentPopupMenu(menu);
                }

                _menuContext = e.getComponent();
                menu.show(e.getComponent(), e.getX(), e.getY());
            }
        }
    }

    protected ComposerComponent getComponentAt(int position)
    {
        return((ComposerComponent)_componentLadder.get(position));
    }

    public void createConnector(int startRung, int startPosition,
                                int finishRung, int finishPosition)
    {
        ComposerComponent startComponent = (ComposerComponent)_componentLadder.get(startRung);
        ComposerComponent finishComponent = (ComposerComponent)_componentLadder.get(finishRung);
        ComponentConnector connector = new ComponentConnector(startComponent, startPosition, finishComponent, finishPosition);
        _connectors.add(connector);
        startComponent.setOutputConnector(connector);
        finishComponent.setInputConnector(connector);
    }

    protected PerformTaskControl createPerformTaskControl(TaskDefinition taskDef, org.jboss.dtf.testframework.coordinator.Action a) throws NoAssociatedData
    {
        PerformTaskControl task = new PerformTaskControl(this);
        task.setTaskName(taskDef.getId());

        task.setLocationParameter(a.getLocation());
        String[] paramList = a.getParameterList();
        for (int count=0;count<paramList.length;count++)
        {
            task.addParam(paramList[count]);
        }
        task.setNameList(a.getAssociatedNameList());
        String[] jvmParamList = a.getJVMParameterList();
        for (int count=0;count<jvmParamList.length;count++)
        {
            task.addParam(jvmParamList[count]);
        }
        return(task);
    }

    public void insertPerformComponentAt(int position, TaskDefinition taskDef, org.jboss.dtf.testframework.coordinator.Action a, boolean addToTestDef)
    {
        try
        {
            PerformTaskControl task = createPerformTaskControl(taskDef,a);

            if (addToTestDef)
            {
                _testDefinition.addPerformTaskAction((PerformAction)a);
            }

            ComposerComponent current = getComponentAt(position - 1);
            ComponentConnector connector = getConnectorForComponent(current);

            if (connector != null)
            {
                connector.setFinishComponent(task);
            }

            insertIntoMatrix(position,task);
            createConnector(position, ComponentConnector.BOTTOM, position+1, ComponentConnector.TOP);
            setupComponentPositions();
        }
        catch (NoAssociatedData e)
        {
            JOptionPane.showMessageDialog(this,"An internal error has occured");
            e.printStackTrace(System.err);
        }
    }

    public void insertStartComponentAt(int position, TaskDefinition taskDef, org.jboss.dtf.testframework.coordinator.Action a, boolean addToTestDef)
    {
        try
        {
            StartTaskControl task = createStartTaskControl(taskDef,a,addToTestDef);

            if (addToTestDef)
            {
                _testDefinition.addStartTaskAction((StartAction)a);
            }

            ComposerComponent current = getComponentAt(position - 1);
            ComponentConnector connector = getConnectorForComponent(current);
            connector.setFinishComponent(task);
            /**
             *   +-----------+      +-----------+  A == Current, B == next
             *   |     A     |      |     A     |
             *   +-----+-----+      +-----+-----+
             *         | Z1               | Z1
             *   +-----+-----+      +-----+-----+
             *   |     B     |      |     C     |  <---- Position
             *   +-----------+      +-----+-----+
             *                            | Z2
             *                      +-----+-----+
             *                      |     B     |
             *                      +-----------+
             */
            insertIntoMatrix(position,task);
            createConnector(position, ComponentConnector.BOTTOM, position+1, ComponentConnector.TOP);
            setupComponentPositions();
        }
        catch (NoAssociatedData e)
        {
            JOptionPane.showMessageDialog(this,"An internal error has occured");
            e.printStackTrace(System.err);
        }
    }

    public void insertWaitForComponentAt(int position, org.jboss.dtf.testframework.coordinator.Action a, boolean addToTestDef)
    {
        try
        {
            WaitForTaskControl task = createWaitForTaskControl(a);

            if (addToTestDef)
            {
                _testDefinition.addWaitForTaskAction((WaitForAction)a);
            }

            ComposerComponent current = getComponentAt(position - 1);
            ComponentConnector connector = getConnectorForComponent(current);

            if (connector != null)
            {
                connector.setFinishComponent(task);
            }

            insertIntoMatrix(position,task);
            createConnector(position, ComponentConnector.BOTTOM, position+1, ComponentConnector.TOP);
            setupComponentPositions();
        }
        catch (NoAssociatedData e)
        {
            JOptionPane.showMessageDialog(this,"An internal error has occured");
            e.printStackTrace(System.err);
        }
    }

    public void insertTerminateComponentAt(int position, org.jboss.dtf.testframework.coordinator.Action a, boolean addToTestDef)
    {
        try
        {
            TerminateTaskControl task = createTerminateTaskControl(a);

            if (addToTestDef)
            {
                _testDefinition.addTerminateTaskAction((TerminateAction)a);
            }

            ComposerComponent current = getComponentAt(position - 1);
            ComponentConnector connector = getConnectorForComponent(current);
            insertIntoMatrix(position,task);

            if (connector != null)
            {
                System.out.println("No connector");
                connector.setFinishComponent(task);
                createConnector(position, ComponentConnector.BOTTOM, position+1, ComponentConnector.TOP);
            }
            else
            {
                createConnector(position-1, ComponentConnector.BOTTOM, position, ComponentConnector.TOP);
            }

            setupComponentPositions();
        }
        catch (NoAssociatedData e)
        {
            JOptionPane.showMessageDialog(this,"An internal error has occured");
            e.printStackTrace(System.err);
        }

    }

    protected StartTaskControl createStartTaskControl(TaskDefinition taskDef, org.jboss.dtf.testframework.coordinator.Action a, boolean addToTestDef) throws NoAssociatedData
    {
        StartTaskControl task = new StartTaskControl(this);
        task.setTaskName(taskDef.getId());

        task.setRuntimeId(a.getAssociatedRuntimeTaskId());
        _runtimeIds.put(a.getAssociatedRuntimeTaskId(), task);
        task.setNameList(a.getAssociatedNameList());
        task.setLocationParameter(a.getLocation());
        String[] paramList = a.getParameterList();
        for (int count=0;count<paramList.length;count++)
        {
            task.addParam(paramList[count]);
        }
        String[] jvmParamList = a.getJVMParameterList();
        for (int count=0;count<jvmParamList.length;count++)
        {
            task.addParam(jvmParamList[count]);
        }

        return(task);
    }

    public void addPerformComponent(TaskDefinition taskDef, org.jboss.dtf.testframework.coordinator.Action a, boolean addToTestDef)
    {
        try
        {
            PerformTaskControl task = createPerformTaskControl(taskDef,a);

            if (addToTestDef)
            {
                _testDefinition.addPerformTaskAction((PerformAction)a);
            }

            increasePosition();
            insertIntoMatrix(getCurrentPosition(),task);
            createConnector(getCurrentPosition() - 1,ComponentConnector.BOTTOM,getCurrentPosition(),ComponentConnector.TOP);
            setupComponentPositions();
        }
        catch (NoAssociatedData e)
        {
            JOptionPane.showMessageDialog(this,"An internal error has occured");
            e.printStackTrace(System.err);
        }
    }

    public void addStartComponent(TaskDefinition taskDef, org.jboss.dtf.testframework.coordinator.Action a, boolean addToTestDef)
    {
        try
        {
            StartTaskControl task = createStartTaskControl(taskDef, a, addToTestDef);
            if (addToTestDef)
            {
                _testDefinition.addStartTaskAction((StartAction)a);
            }

            increasePosition();
            insertIntoMatrix(getCurrentPosition(),task);
            createConnector(getCurrentPosition()-1,ComponentConnector.BOTTOM,getCurrentPosition(),ComponentConnector.TOP);
            setupComponentPositions();
        }
        catch (NoAssociatedData e)
        {
            JOptionPane.showMessageDialog(this,"An internal error has occured");
            e.printStackTrace(System.err);
        }
    }

    protected TerminateTaskControl createTerminateTaskControl(org.jboss.dtf.testframework.coordinator.Action a) throws NoAssociatedData
    {
        TerminateTaskControl task = new TerminateTaskControl(this);

        String runtimeId = a.getAssociatedRuntimeTaskId();
        task.setTaskName(runtimeId);
        task.setRuntimeId(runtimeId);
        task.associatedTaskControl((TaskControl)_runtimeIds.get(runtimeId));

        return(task);
    }

    public void addTerminateComponent(org.jboss.dtf.testframework.coordinator.Action a, boolean addToTestDef)
    {
        try
        {
            TerminateTaskControl task = createTerminateTaskControl(a);

            if (addToTestDef)
            {
                _testDefinition.addTerminateTaskAction((TerminateAction)a);
            }

            task.setLocationParameter(null);
            increasePosition();
            insertIntoMatrix(getCurrentPosition(),task);
            createConnector(getCurrentPosition()-1,ComponentConnector.BOTTOM,getCurrentPosition(),ComponentConnector.TOP);
            setupComponentPositions();
        }
        catch (NoAssociatedData e)
        {
            JOptionPane.showMessageDialog(this,"An internal error has occured");
            e.printStackTrace(System.err);
        }
    }

    protected WaitForTaskControl createWaitForTaskControl(org.jboss.dtf.testframework.coordinator.Action a) throws org.jboss.dtf.testframework.coordinator.NoAssociatedData
    {
        WaitForTaskControl task = new WaitForTaskControl(this);

        String runtimeId = a.getAssociatedRuntimeTaskId();
        task.setTaskName(runtimeId);
        task.setRuntimeId(runtimeId);
        task.associatedTaskControl((TaskControl)_runtimeIds.get(runtimeId));

        return(task);
    }

    public void addWaitForComponent(org.jboss.dtf.testframework.coordinator.Action a, boolean addToTestDef)
    {
        try
        {
            WaitForTaskControl task = createWaitForTaskControl(a);

            if (addToTestDef)
            {
                _testDefinition.addWaitForTaskAction((WaitForAction)a);
            }

            task.setLocationParameter(null);
            increasePosition();
            insertIntoMatrix(getCurrentPosition(),task);
            createConnector(getCurrentPosition()-1,ComponentConnector.BOTTOM,getCurrentPosition(),ComponentConnector.TOP);
            setupComponentPositions();
        }
        catch (NoAssociatedData e)
        {
            JOptionPane.showMessageDialog(this,"An internal error has occured");
            e.printStackTrace(System.err);
        }
    }

    public void setupComponentPositions()
    {
        int yPosition = 25;
        int maxHeight = 0;
        int maxWidth = 0;

        /**
         * Calculate the max height of all the components on this row
         */
        for (int count=0;count<_componentLadder.size();count++)
        {
            ComposerComponent component = (ComposerComponent)_componentLadder.get(count);

            maxWidth = (maxWidth < component.getWidth())?component.getWidth():maxWidth;
        }

        for (int count=0;count<_componentLadder.size();count++)
        {
            ComposerComponent component = (ComposerComponent)_componentLadder.get(count);
            int xPosition = (this.getWidth()/2) - (component.getWidth()/2);

            if (component != null)
            {
                component.setLocation( xPosition, yPosition);

                if (component.hasAssociatedPositionMarker())
                {
                    PositionMarker pm = component.getAssociatedPositionMarker();

                    pm.setLocation( xPosition - (pm.getWidth()*2), yPosition + component.getHeight() );
                }

                yPosition += component.getHeight() + EXTRA_VERTICAL_GAP;
            }
        }
    }

    protected void insertIntoMatrix(int rung, ComposerComponent component)
    {
        if ( _componentLadder.size() < rung )
        {
            for (int count=_componentLadder.size();count<rung+1;count++)
            {
                _componentLadder.add(null);
            }
        }

        _componentLadder.add(rung, component);

        PositionMarker pm = component.createPositionMarker();

        this.add(component);
        this.add(pm);
    }

    public void paint(Graphics g)
    {
        super.paint(g);

        for (int count=0;count<_connectors.size();count++)
        {
            ((ComponentConnector)_connectors.get(count)).paint(g);
        }
    }

    public ComponentConnector getConnectorForComponent(ComposerComponent c)
    {
        for (int count=0;count<_connectors.size();count++)
        {
            ComponentConnector connector = (ComponentConnector)_connectors.get(count);

            if (connector.getStartComponent() == c)
            {
                return(connector);
            }
        }

        return(null);
    }

    /**
     * Invoked when the component's size changes.
     */
    public void componentResized(ComponentEvent e)
    {
        setupComponentPositions();
    }

    /**
     * Invoked when the component's position changes.
     */
    public void componentMoved(ComponentEvent e)
    {
    }

    /**
     * Invoked when the component has been made visible.
     */
    public void componentShown(ComponentEvent e)
    {
    }

    /**
     * Invoked when the component has been made invisible.
     */
    public void componentHidden(ComponentEvent e)
    {
    }

    protected void removeObject(ComposerComponent c)
    {
        int position = -1;

        for (int count=0;count<_componentLadder.size();count++)
        {
            ComposerComponent component = (ComposerComponent)_componentLadder.get(count);

            if ( c == component )
            {
                position = count;
            }
        }

        if ( position != -1 )
        {
            ComponentConnector inputConnector = c.getInputConnector();
            ComponentConnector outputConnector = c.getOutputConnector();

            for (int count=position+1;count<_componentLadder.size();count++)
            {
                _componentLadder.set(count-1,_componentLadder.get(count));
            }

            _componentLadder.remove(_componentLadder.size()-1);
            this.remove(c);

            if (inputConnector != null)
            {
                if (position < _componentLadder.size())
                {
                    inputConnector.setFinishComponent((ComposerComponent)_componentLadder.get(position));
                }
                else
                {
                    _connectors.remove(inputConnector);
                }
            }

            _connectors.remove(outputConnector);
            this.invalidate();
            setupComponentPositions();
            this.repaint();
        }
    }

    protected void insertComponentAt(TaskControl t, int position)
    {
        if (t instanceof WaitForTaskControl)
        {
            WaitForTaskControl wftc = (WaitForTaskControl)t;

            insertWaitForComponentAt(position, wftc.getAction(), true);
        }

        if (t instanceof TerminateTaskControl)
        {
            TerminateTaskControl ttc = (TerminateTaskControl)t;

            insertTerminateComponentAt(position, ttc.getAction(), true);
        }
    }

    protected void insertSelectedComponenetAt(int position)
    {
        TaskSelectionPane taskList = _testDesigner.getTaskSelectionPane();

        if (_testDesigner.isPerformSelected())
        {
            System.out.println("Adding perform");
            if ( taskList.getLastSelectedTask() != null )
            {
                PerformTaskForm ptf = new PerformTaskForm(null,true);
                ptf.setData(true,taskList.getLastSelectedTask(),taskList.getLastSelectedTasksFullId());
                ptf.show();

                if ( ptf.getAction() != null )
                {
                    insertPerformComponentAt(position,taskList.getLastSelectedTask(),ptf.getAction(), true);
                }
            }
        }
        else
        {
            System.out.println("Adding start");
            if ( taskList.getLastSelectedTask() != null )
            {
                PerformTaskForm ptf = new PerformTaskForm(null,true);
                ptf.setData(false,taskList.getLastSelectedTask(),taskList.getLastSelectedTasksFullId());
                ptf.show();

                System.out.println("StartTaskForm.getAction() == "+ptf.getAction());

                if ( ptf.getAction() != null )
                {
                    System.out.println("Inserting start");
                    insertStartComponentAt(position, taskList.getLastSelectedTask(), ptf.getAction(), true);
                }
            }
        }
    }

    /**
     * Invoked when an action occurs.
     */
    public void actionPerformed(ActionEvent e)
    {
        String actionCommand = e.getActionCommand();

        if ( actionCommand.equals(REMOVE_MENU_ITEM) )
        {
            removeObject((ComposerComponent)_menuContext);
        }
        else
        {
            if ( actionCommand.equals(WAIT_FOR_MENU_ITEM) )
            {
                StartTaskControl selectedComponent = (StartTaskControl)_menuContext;
                addWaitForComponent(new WaitForAction(selectedComponent.getRuntimeId()),true);
            }
            else
            {
                if ( actionCommand.equals(TERMINATE_MENU_ITEM) )
                {
                    StartTaskControl selectedComponent = (StartTaskControl)_menuContext;
                    addTerminateComponent(new TerminateAction(selectedComponent.getRuntimeId()),true);
                }
            }
        }
    }

    /**
     * Called when a drag operation has
     * encountered the <code>DropTarget</code>.
     * <P>
     * @param dtde the <code>DropTargetDragEvent</code>
     */

    public void dragEnter(DropTargetDragEvent dtde)
    {
        dtde.acceptDrag (DnDConstants.ACTION_MOVE);
    }

    protected ComposerComponent getClosestComponent(Point mouse)
    {
        double closestDistance = Double.MAX_VALUE;
        double distance;
        ComposerComponent closestComponent = null;

        for (int count=0;count<_componentLadder.size();count++)
        {
            ComposerComponent c = (ComposerComponent)_componentLadder.get(count);

            Point p = c.getLocation();
            distance = p.distance(mouse);

            if ( distance < closestDistance )
            {
                closestDistance = distance;
                closestComponent = c;
            }
        }

        return(closestComponent);
    }
    /**
     * Called when a drag operation is ongoing
     * on the <code>DropTarget</code>.
     * <P>
     * @param e the <code>DropTargetDragEvent</code>
     */

    public void dragOver(DropTargetDragEvent e)
    {
        ComposerComponent closestComponent = getClosestComponent(e.getLocation());

        if (_previousSelected != null)
        {
            _previousSelected.getAssociatedPositionMarker().setVisible(false);
        }


        if (closestComponent != null)
        {
            _previousSelected = closestComponent;
            closestComponent.getAssociatedPositionMarker().setVisible(true);
        }
    }

    /**
     * Called if the user has modified
     * the current drop gesture.
     * <P>
     * @param dtde the <code>DropTargetDragEvent</code>
     */

    public void dropActionChanged(DropTargetDragEvent dtde)
    {
    }

    /**
     * The drag operation has departed
     * the <code>DropTarget</code> without dropping.
     * <P>
     * @param dte the <code>DropTargetEvent</code>
     */

    public void dragExit(DropTargetEvent dte)
    {
        if (_previousSelected != null)
        {
            _previousSelected.getAssociatedPositionMarker().setVisible(false);
        }
    }

    /**
     * The drag operation has terminated
     * with a drop on this <code>DropTarget</code>.
     * This method is responsible for undertaking
     * the transfer of the data associated with the
     * gesture. The <code>DropTargetDropEvent</code>
     * provides a means to obtain a <code>Transferable</code>
     * object that represents the data object(s) to
     * be transfered.<P>
     * From this method, the <code>DropTargetListener</code>
     * shall accept or reject the drop via the
     * acceptDrop(int dropAction) or rejectDrop() methods of the
     * <code>DropTargetDropEvent</code> parameter.
     * <P>
     * Subsequent to acceptDrop(), but not before,
     * <code>DropTargetDropEvent</code>'s getTransferable()
     * method may be invoked, and data transfer may be
     * performed via the returned <code>Transferable</code>'s
     * getTransferData() method.
     * <P>
     * At the completion of a drop, an implementation
     * of this method is required to signal the success/failure
     * of the drop by passing an appropriate
     * <code>boolean</code> to the <code>DropTargetDropEvent</code>'s
     * dropComplete(boolean success) method.
     * <P>
     * Note: The actual processing of the data transfer is not
     * required to finish before this method returns. It may be
     * deferred until later.
     * <P>
     * @param dtde the <code>DropTargetDropEvent</code>
     */

    public void drop(DropTargetDropEvent e)
    {
        double closestDistance = Double.MAX_VALUE;
        double distance;
        int closestPosition = -1;
        ComposerComponent closestComponent = null;

        for (int count=0;count<_componentLadder.size();count++)
        {
            ComposerComponent c = (ComposerComponent)_componentLadder.get(count);

            Point p = c.getLocation();
            Point mouse = e.getLocation();
            distance = p.distance(mouse);

            if ( distance < closestDistance )
            {
                closestPosition = count;
                closestDistance = distance;
                closestComponent = c;
            }
        }

        Transferable t = e.getTransferable();
        String transferData = null;

        try
        {
            transferData = (String)t.getTransferData(DataFlavor.stringFlavor);
        }
        catch (Exception ex)
        {
            System.err.println(ex);
        }

        if (_previousSelected != null)
        {
            _previousSelected.getAssociatedPositionMarker().setVisible(false);
        }

        _previousSelected = null;

        try
        {
            TaskControl taskControl = TaskControlFactory.getTaskControl(transferData);

            insertComponentAt(taskControl,closestPosition+1);
        }
        catch (Exception ex)
        {
            if (closestComponent != null)
            {
                insertSelectedComponenetAt(closestPosition+1);
            }
        }

        repaint();
    }

    public void componentSizeHasChanged()
    {
        setupComponentPositions();
    }
}
