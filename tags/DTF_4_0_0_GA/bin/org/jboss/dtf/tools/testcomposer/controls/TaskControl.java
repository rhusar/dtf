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
 * $Id: TaskControl.java 170 2008-03-25 18:59:26Z jhalliday $
 */

package org.jboss.dtf.tools.testcomposer.controls;

import org.jboss.dtf.tools.testcomposer.ComponentSizeChangeListener;
import com.sun.image.codec.jpeg.JPEGCodec;

import java.util.ArrayList;
import java.io.Serializable;
import java.io.FileInputStream;
import java.awt.dnd.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.event.MouseEvent;

import org.jdom.Element;
import org.jdom.Document;

public abstract class TaskControl extends ComposerComponent implements DragSourceListener, DragGestureListener, Serializable
{
    protected final static int PERFORM = 0, START = 1, TERMINATE = 2, WAIT_FOR = 3;
    protected final static String[] TYPE_TEXT = { "PERFORM", "START", "TERMINATE", "WAIT_FOR" };
    protected final static String[] NICE_TYPE_TEXT = { "Perform", "Start", "Terminate", "Wait For" };
    protected final static String[] IMAGE_FILENAME = { "perform_task_icon.jpg", "start_task_icon.jpg", "terminate_task_icon.jpg", "wait_for_task_icon.jpg" };
    protected final static Color[] COLORS = { Color.gray, Color.green, Color.black,  Color.magenta };
    protected final static int PIXELS_PER_ROW = 15;
    protected static BufferedImage      Expand = null;
	protected static BufferedImage      ExpandOver = null;
    protected static BufferedImage      Contract = null;
	protected static BufferedImage      ContractOver = null;
    protected static BufferedImage[]    ControlImage = null;

    protected String    _taskName = "Unknown";
    protected String    _location = null;
    protected String    _runtimeId = null;
    protected ArrayList _parameters = new ArrayList();
    protected ArrayList _jvmParameters = new ArrayList();
    protected int       _type = PERFORM;
    protected String    _nameList = null;
    protected TaskControl _associatedTaskControl = null;
    protected boolean   _highlight = false;

    protected boolean   _mouseOver = false;
    protected boolean   _expanded = false;
    protected boolean   _expandable = true;
	protected boolean	_overExpand = false;

    protected DragSource _taskCtrlDragSource = null;
    protected DragGestureRecognizer _gestureRecognizer = null;

    protected ComponentSizeChangeListener _testDesignPane = null;

    public TaskControl(ComponentSizeChangeListener testDesignPane)
    {
        super();

        setBackground(Color.white);

        _testDesignPane = testDesignPane;

        _taskCtrlDragSource = new DragSource();
        _gestureRecognizer = _taskCtrlDragSource.createDefaultDragGestureRecognizer(this,DnDConstants.ACTION_MOVE,this);

        this.setSize(150,100);
    }

    public boolean getExpandable()
    {
        return(_expandable);
    }

    public void setExpandable(boolean expandable)
    {
        _expandable = expandable;

        _expanded = expandable?_expandable:false;
    }

    public void setTaskName(String name)
    {
        _taskName = name;
		calculateControlSize();
    }

    public String getTaskName()
    {
        return(_taskName);
    }

    public void setLocationParameter(String location)
    {
        _location = location;
        calculateControlSize();
    }

    public String getLocationParameter()
    {
        return(_location);
    }

    public void setRuntimeId(String runtimeId)
    {
        _runtimeId = runtimeId;
        calculateControlSize();
    }

    public String getRuntimeId()
    {
        return(_runtimeId);
    }

    public void addParam(String param)
    {
        _parameters.add(param);
        calculateControlSize();
    }

    public void addJVMParam(String param)
    {
        _jvmParameters.add(param);
        calculateControlSize();
    }

    public String[] getParameters()
    {
        String[] parameterArray = new String[_parameters.size()];
        System.arraycopy(_parameters.toArray(),0,parameterArray,0,parameterArray.length);
        return(parameterArray);
    }

    public String[] getJVMParameters()
    {
        String[] parameterArray = new String[_jvmParameters.size()];
        System.arraycopy(_jvmParameters.toArray(),0,parameterArray,0,parameterArray.length);
        return(parameterArray);
    }

    public String getNameList()
    {
        return(_nameList);
    }

    public void setNameList(String nameList)
    {
        _nameList = nameList;
    }

    protected void calculateControlSize()
    {
        int numberOfLines = 0;
        int maxWidth = _taskName!=null ? (_taskName.length() * 8) : 200;

		if ( maxWidth < 200 )
		{
			maxWidth = 200;
		}

        if (_expanded)
            numberOfLines += (_runtimeId==null?0:1) + (_location==null?0:1);

        Dimension newSize = new Dimension( ((maxWidth > 600)?600:maxWidth) + 5, (numberOfLines * PIXELS_PER_ROW) + 7 + ControlImage[_type].getHeight());
        System.out.println("Setting size = "+newSize);

        if ( newSize.getHeight() < (ControlImage[_type].getHeight() + 4) )
        {
            newSize = new Dimension( (int)newSize.getWidth(), (int)(ControlImage[_type].getHeight() + 4) );
            System.out.println("Resetting size = "+newSize);
        }

        this.setSize(newSize);

        if (_testDesignPane != null)
        {
            _testDesignPane.componentSizeHasChanged();
        }
    }


    public void highlight(boolean on)
    {
        _highlight = on;
    }

    /**
     * If the UI delegate is non-null, calls its paint
     * method.  We pass the delegate a copy of the Graphics
     * object to protect the rest of the paint code from
     * irrevocable changes (for example, Graphics.translate()).
     *
     * @see #paint
     */
    protected void paintComponent(Graphics g)
    {
        /**
         * Draw main rectangle
         */
        Dimension size = this.getSize();
        g.setColor(getColor(_mouseOver?Color.orange:Color.black));
        g.drawImage(ControlImage[_type], 2, 2, this);
        g.drawRect(0,0,(int)size.getWidth() - 1, (int)size.getHeight() - 1);

//        g.setColor(getColor(_mouseOver?Color.orange:COLORS[_type]));
//        g.fillRect(2,2, (int)size.getWidth() - 4, 1 + PIXELS_PER_ROW);

        g.drawImage(_expanded ? (_overExpand ? ContractOver : Contract):(_overExpand ? ExpandOver : Expand),(int)size.getWidth() - 12, 5, this);

        g.setColor(getColor(Color.blue));
        g.drawString(getTaskName(), 2 + ControlImage[_type].getWidth() + 2, PIXELS_PER_ROW );

        if (_expanded)
        {
            g.setColor(getColor(Color.black));
            Point currentPosition = new Point(5, 4 + ControlImage[_type].getHeight() + PIXELS_PER_ROW );

            if (_runtimeId != null)
            {
                g.drawString( "Runtime Id:" + _runtimeId, currentPosition.x, currentPosition.y);
                currentPosition.y += (currentPosition.y + PIXELS_PER_ROW);
            }

            if (_location != null)
            {
                g.drawString( "Location:" + _location, currentPosition.x, currentPosition.y);
                currentPosition.y += (currentPosition.y + PIXELS_PER_ROW);
            }
        }
    }

    protected Color getColor(Color c)
    {
        return(_highlight?c.darker():c);
    }

    public void associatedTaskControl(TaskControl tc)
    {
        _associatedTaskControl = tc;
    }

    public String toString()
    {
        Element rootElement = new Element("task-control");
        rootElement.setAttribute("type",TYPE_TEXT[_type]);

        Element taskId = new Element("id");
        taskId.setText(_taskName);

        Element runtimeId = new Element("runtime-id");
        runtimeId.setText(_runtimeId);

        Element location = new Element("location");
        location.setText(_location);

        Element parameters = new Element("parameters");

        for (int count=0;count<_parameters.size();count++)
        {
            Element param = new Element("param");
            param.setText((String)_parameters.get(count));
            parameters.addContent(param);
        }

        Element jvmParameters = new Element("jvm-parameters");

        for (int count=0;count<_jvmParameters.size();count++)
        {
            Element param = new Element("param");
            param.setText((String)_jvmParameters.get(count));
            jvmParameters.addContent(param);
        }

        rootElement.addContent(taskId);
        rootElement.addContent(runtimeId);
        rootElement.addContent(location);
        rootElement.addContent(parameters);
        rootElement.addContent(jvmParameters);

        Document doc = new Document(rootElement);
        return(new org.jdom.output.XMLOutputter().outputString(rootElement));
    }

    public abstract org.jboss.dtf.testframework.coordinator.Action getAction();

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
     * Invoked when a mouse button has been pressed on a component.
     */
    public void mousePressed(MouseEvent e)
    {
        super.mousePressed(e);
        Dimension size = this.getSize();

        if ( ( e.getModifiers() & java.awt.event.MouseEvent.BUTTON1_MASK ) != 0 )
        {
            Rectangle rect = new Rectangle((int)size.getWidth() - 12, 5, 10, 10);

            if ( ( rect.contains(e.getX(),e.getY()) ) && ( _expandable ) )
            {
                _expanded = !_expanded;
                calculateControlSize();
                this.repaint();
            }
        }

        getParent().dispatchEvent(e);
    }

    /**
     * Invoked when a mouse button has been released on a component.
     */
    public void mouseReleased(MouseEvent e)
    {
        super.mouseReleased(e);
        getParent().dispatchEvent(e);
    }

    public void mouseEntered(MouseEvent event)
    {
        _mouseOver = true;
        this.repaint();

        if (_associatedTaskControl != null)
        {
            _associatedTaskControl.highlight(true);
            _associatedTaskControl.repaint();
        }

        super.mouseEntered(event);
    }

	/**
	 * Invoked when the mouse button has been moved on a component
	 * (with no buttons no down).
	 */
	public void mouseMoved(MouseEvent e)
	{
		super.mouseMoved(e);

		Dimension size = this.getSize();
		Rectangle rect = new Rectangle((int)size.getWidth() - 12, 5, 10, 10);

		if ( rect.contains(e.getX(),e.getY()) )
		{
			_overExpand = true;
			this.repaint();
		}
		else
		{
			if (_overExpand)
			{
				_overExpand = false;
				this.repaint();
			}
		}
	}

    public void mouseExited(MouseEvent event)
    {
        _mouseOver = false;
        this.repaint();

        if (_associatedTaskControl != null)
        {
            _associatedTaskControl.highlight(false);
            _associatedTaskControl.repaint();
        }

        super.mouseExited(event);
    }

    public void dragStart(DragSourceEvent event)
    {
    }

    public void dragSetData(DragSourceEvent event)
    {
    }

    public void dragFinished(DragSourceEvent event)
    {
    }

    static
    {
        try
        {
            Expand = JPEGCodec.createJPEGDecoder(new FileInputStream("images/expand.jpg")).decodeAsBufferedImage();
			ExpandOver = JPEGCodec.createJPEGDecoder(new FileInputStream("images/expand-over.jpg")).decodeAsBufferedImage();
            Contract = JPEGCodec.createJPEGDecoder(new FileInputStream("images/contract.jpg")).decodeAsBufferedImage();
			ContractOver = JPEGCodec.createJPEGDecoder(new FileInputStream("images/contract-over.jpg")).decodeAsBufferedImage();
            ControlImage = new BufferedImage[TYPE_TEXT.length];

            for (int count=0;count<ControlImage.length;count++)
            {
                ControlImage[count] = JPEGCodec.createJPEGDecoder(new FileInputStream("images/"+IMAGE_FILENAME[count])).decodeAsBufferedImage();
            }
        }
        catch (Exception e)
        {
            System.err.println("Failed to load images");
            e.printStackTrace(System.err);
        }
    }
}
