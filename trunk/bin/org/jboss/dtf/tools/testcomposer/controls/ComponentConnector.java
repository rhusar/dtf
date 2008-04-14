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
 * $Id: ComponentConnector.java 170 2008-03-25 18:59:26Z jhalliday $
 */

package org.jboss.dtf.tools.testcomposer.controls;

import java.awt.*;

public class ComponentConnector
{
    public final static int TOP = 0, BOTTOM = 1, RIGHT = 2;

    protected final static int NEAR_DISTANCE = 25;

    protected ComposerComponent _start = null;
    protected ComposerComponent _finish = null;
    protected int               _startPoint;
    protected int               _finishPoint;
    protected boolean           _pointSelected = false;

    public ComponentConnector(ComposerComponent start, int startPoint, ComposerComponent finish, int finishPoint)
    {
        _start = start;
        _finish = finish;
        _startPoint = startPoint;
        _finishPoint = finishPoint;
    }

    public ComposerComponent getStartComponent()
    {
        return(_start);
    }

    public ComposerComponent getFinishComponent()
    {
        return(_finish);
    }

    public void setStartComponent(ComposerComponent c)
    {
        _start = c;
    }

    public void setFinishComponent(ComposerComponent c)
    {
        _finish = c;
    }

    public double getDistanceFrom(Point p)
    {
        java.awt.Point thisPoint = new java.awt.Point(p.x, p.y);
        java.awt.Point finishPoint = new java.awt.Point(getFinishPoint().x, getFinishPoint().y);
        return(finishPoint.distance(thisPoint));
    }

    public boolean isNear(Point p)
    {
        double distance = getDistanceFrom(p);

        return (distance < NEAR_DISTANCE);
    }

    public Point getStartPoint()
    {
        switch(_startPoint)
        {
            case TOP :
                return _start.getInputPoint();
            case BOTTOM :
                return _start.getFollowOnPoint();
            case RIGHT :
                return _start.getOnFinishPoint();
        }

        return _finish.getFollowOnPoint();
    }

    public Point getFinishPoint()
    {
        switch(_finishPoint)
        {
            case TOP :
                return _finish.getInputPoint();
            case BOTTOM :
                return _finish.getFollowOnPoint();
            case RIGHT :
                return _finish.getOnFinishPoint();
        }

        return _finish.getInputPoint();
    }

    public void paint(Graphics g)
    {
        Point startPoint = getStartPoint();
        Point finishPoint = getFinishPoint();

        g.setColor(Color.black);
        g.drawLine( (int)startPoint.x,  (int)startPoint.y,
                    (int)finishPoint.x, (int)startPoint.y );
        g.drawLine( (int)finishPoint.x, (int)startPoint.y,
                    (int)finishPoint.x, (int)finishPoint.y );
    }
}
