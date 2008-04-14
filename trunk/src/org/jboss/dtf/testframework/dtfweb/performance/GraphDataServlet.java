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
 * $Id: GraphDataServlet.java 170 2008-03-25 18:59:26Z jhalliday $
 */
package org.jboss.dtf.testframework.dtfweb.performance;

import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGImageEncoder;
import com.sun.image.codec.jpeg.JPEGEncodeParam;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.ServletException;
import java.io.IOException;
import java.awt.*;
import java.awt.image.BufferedImage;

public class GraphDataServlet extends HttpServlet
{
    private final static Color SeriesColours[] = { Color.blue, Color.red, Color.orange, Color.black,
                                                   Color.cyan, Color.magenta, Color.pink, Color.gray };
    private final static int DEFAULT_WIDTH = 400;
    private final static int DEFAULT_HEIGHT = 300;

    public static final String getSeriesColor(int seriesNumber)
    {
        String redHex = Integer.toHexString(SeriesColours[seriesNumber].getRed());
        String greenHex = Integer.toHexString(SeriesColours[seriesNumber].getGreen());
        String blueHex = Integer.toHexString(SeriesColours[seriesNumber].getBlue());

        redHex = redHex.length() < 2 ? "0"+redHex : redHex.substring(0,2);
        greenHex = greenHex.length() < 2 ? "0"+greenHex : greenHex.substring(0,2);
        blueHex = blueHex.length() < 2 ? "0"+blueHex : blueHex.substring(0,2);

        return("#"+redHex+greenHex+blueHex);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        String perfRunIdStr = request.getParameter("perfrunid");
        String configWidth = request.getParameter("width");
        String configHeight = request.getParameter("height");

        int width = configWidth != null ? Integer.parseInt(configWidth) : DEFAULT_WIDTH;
        int height = configHeight != null ? Integer.parseInt(configHeight) : DEFAULT_HEIGHT;

        XYGraphingBean graph = new XYGraphingBean();

        if ( perfRunIdStr.equalsIgnoreCase("usergraph") )
        {
            HttpSession session = request.getSession();
            UserDefinedGraph userDefGraph = UserDefinedGraph.getGraph(session);

            if ( userDefGraph != null )
            {
                long[] perfRunList = userDefGraph.getPerformanceDataList();

                for (int count=0;count<perfRunList.length;count++)
                {
                    XYDataSeries series = DTFPerformanceResultManager.getPerformanceDataSeries(SeriesColours[count],perfRunList[count]);
                    series.setBeginAtOrigin(false);
                    graph.addDataSeries(series);
                }
            }

            String x = userDefGraph.getXAxisLabel();
            String y = userDefGraph.getYAxisLabel();

            if ( x != null )
            {
                graph.setXAxisLabel(x);
            }

            if ( y != null )
            {
                graph.setYAxisLabel(y);
            }
        }
        else
        {
            long runId = Long.parseLong( perfRunIdStr );
            graph.addDataSeries(DTFPerformanceResultManager.getPerformanceDataSeries(Color.blue,runId));
        }

        BufferedImage img = graph.createGraphImage(width,height);
        response.setContentType("image/jpeg");
        JPEGImageEncoder enc = JPEGCodec.createJPEGEncoder(response.getOutputStream());
        enc.encode(img);
        System.out.println("Complete");
    }
}
