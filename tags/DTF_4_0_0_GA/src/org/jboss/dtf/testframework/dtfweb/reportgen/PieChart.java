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
// Decompiled by Jad v1.5.8e2. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://kpdus.tripod.com/jad.html
// Decompiler options: packimports(3)
// Source File Name:   PieChart.java

package org.jboss.dtf.testframework.dtfweb.reportgen;

import java.awt.*;
import java.awt.image.BufferedImage;

public class PieChart
{

    public PieChart()
    {
    }

    public static BufferedImage getImage(int width, int height, int values[], Color colors[])
    {
        BufferedImage img = new BufferedImage(width, height, 2);
        Graphics2D g = img.createGraphics();
        int percs[] = new int[values.length];
        int maxValue = 0;
        int prevValue = 0;
        g.setStroke(new BasicStroke(2.0F));
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        for(int count = 0; count < values.length; count++)
            maxValue += values[count];

        for(int count = 0; count < values.length; count++)
            percs[count] = (int)(((float)values[count] / (float)maxValue) * 360F);

        for(int count = 0; count < percs.length; count++)
        {
            g.setColor(colors[count]);
            g.fillArc(0, 0, width, height, prevValue, percs[count]);
            prevValue = percs[count];
        }

        return img;
    }
}
