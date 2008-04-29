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
 * $Id: ComposerComponent.java 170 2008-03-25 18:59:26Z jhalliday $
 */

package org.jboss.dtf.tools.testcomposer.controls;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseEvent;

public class ComposerComponent extends JComponent implements java.awt.event.MouseListener, MouseMotionListener
{
    protected ComponentConnector    _inputConnector = null;
    protected ComponentConnector    _outputConnector = null;
    protected PositionMarker        _marker = null;

    public ComposerComponent()
    {
        super();

        this.addMouseListener(this);
        this.addMouseMotionListener(this);
    }

    public Point getOnFinishPoint()
    {
        Point result = this.getLocation();
        Dimension size = this.getSize();

        result.x = result.x + (int)size.getWidth();
        result.y = result.y + (int)(size.getHeight() / 2);

        return(result);
    }

    public Point getFollowOnPoint()
    {
        Point result = this.getLocation();
        Dimension size = this.getSize();

        result.x = result.x + (int)(size.getWidth() / 2);
        result.y = result.y + (int)size.getHeight();

        return(result);
    }

    public Point getInputPoint()
    {
        Point result = this.getLocation();
        Dimension size = this.getSize();

        result.x = result.x + (int)(size.getWidth() / 2);

        return(result);
    }

    public void setInputConnector(ComponentConnector c)
    {
        _inputConnector = c;
    }

    public void setOutputConnector(ComponentConnector c)
    {
        _outputConnector = c;
    }

    public ComponentConnector getInputConnector()
    {
        return(_inputConnector);
    }

    public ComponentConnector getOutputConnector()
    {
        return(_outputConnector);
    }

    public boolean hasAssociatedPositionMarker()
    {
        return(_marker != null);
    }

    public PositionMarker createPositionMarker()
    {
        _marker = new PositionMarker();
        _marker.setVisible(false);
        return(_marker);
    }

    public PositionMarker getAssociatedPositionMarker()
    {
        return(_marker);
    }

    public void setAssociatedPositionMarker(PositionMarker marker)
    {
        _marker = marker;
    }

    /**
     * Invoked when the mouse has been clicked on a component.
     */
    public void mouseClicked(MouseEvent e)
    {
    }

    /**
     * Invoked when a mouse button is pressed on a component and then
     * dragged.  Mouse drag events will continue to be delivered to
     * the component where the first originated until the mouse button is
     * released (regardless of whether the mouse position is within the
     * bounds of the component).
     */
    public void mouseDragged(MouseEvent e)
    {
    }

    /**
     * Invoked when a mouse button has been pressed on a component.
     */
    public void mousePressed(MouseEvent e)
    {
    }

    /**
     * Invoked when the mouse button has been moved on a component
     * (with no buttons no down).
     */
    public void mouseMoved(MouseEvent e)
    {
    }

    /**
     * Invoked when a mouse button has been released on a component.
     */
    public void mouseReleased(MouseEvent e)
    {
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
}
