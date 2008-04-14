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
 * $Id: Marker.java 170 2008-03-25 18:59:26Z jhalliday $
 */

package org.jboss.dtf.tools.testcomposer.controls;

import java.awt.*;

public class Marker extends ComposerComponent
{
    private final static int PIXELS_PER_ROW = 15;

    protected String _markerName = null;

    public Marker()
    {
        super();

        this.setSize(new Dimension(150,PIXELS_PER_ROW + 5));
    }

    public void setMarkerName(String name)
    {
        _markerName = name;
    }

    public String getMarkerName()
    {
        return(_markerName);
    }

    /**
     * Paints this component.  This method is called when the contents
     * of the component should be painted in response to the component
     * first being shown or damage needing repair.  The clip rectangle
     * in the Graphics parameter will be set to the area which needs
     * to be painted.
     * For performance reasons, Components with zero width or height
     * aren't considered to need painting when they are first shown,
     * and also aren't considered to need repair.
     * @param g The graphics context to use for painting.
     * @see       java.awt.Component#update
     * @since     JDK1.0
     */
    public void paint(Graphics g)
    {
        super.paint(g);

        /**
         * Draw main rectangle
         */
        Dimension size = this.getSize();
        g.drawRect(0,0,(int)size.getWidth() - 1, (int)size.getHeight() - 1);

        g.setColor(Color.blue);
        g.fillRect(2,2, (int)size.getWidth() - 4, 1 + PIXELS_PER_ROW);

        g.setColor(Color.white);
        g.drawString(getMarkerName(), ((int)size.getWidth()/2) - (g.getFontMetrics().stringWidth(getMarkerName())/2), PIXELS_PER_ROW );
    }

}
