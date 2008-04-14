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
 * $Id: PositionMarker.java 170 2008-03-25 18:59:26Z jhalliday $
 */

package org.jboss.dtf.tools.testcomposer.controls;

import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGImageDecoder;

import java.awt.image.BufferedImage;
import java.awt.*;
import java.io.FileInputStream;

public class PositionMarker extends Component
{
    private final static String POINTER_IMAGE_FILENAME = "images/marker.jpg";

    protected static BufferedImage _pointer = null;

    public PositionMarker()
    {
        super();

        this.setSize( _pointer.getWidth(), _pointer.getHeight() );
    }

    public void paint(Graphics g)
    {
        g.drawImage(_pointer,0,0,this);
    }

    public void update(Graphics g)
    {
        paint(g);
    }

    static
    {
        try
        {
            JPEGImageDecoder decoder = JPEGCodec.createJPEGDecoder(new FileInputStream(POINTER_IMAGE_FILENAME));
            _pointer = decoder.decodeAsBufferedImage();
        }
        catch (Exception e)
        {
            System.err.println("An unexpected exception occurred while trying to load '"+POINTER_IMAGE_FILENAME+"' - "+e);
            System.exit(0);
        }
    }

}
