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
 * $Id: CSVDataServlet.java 170 2008-03-25 18:59:26Z jhalliday $
 */
package org.jboss.dtf.testframework.dtfweb.performance;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.ServletException;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.BufferedWriter;
import java.util.ArrayList;
import java.awt.*;

public class CSVDataServlet extends HttpServlet
{
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        long perfRunId = -1;
        String perfRunIdStr = request.getParameter("perfrunid");

        response.setContentType("text/plain");
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(response.getOutputStream()));


        if ( perfRunIdStr.equals("usergraph") )
        {
            HttpSession session = request.getSession();

            if ( session != null )
            {
                UserDefinedGraph graph = UserDefinedGraph.getGraph(session);

                if (graph != null)
                {
                    long[] runIds = graph.getPerformanceDataList();

                    out.write("User Graph contains "+runIds.length+" series\n");

                    for (int count=0;count<runIds.length;count++)
                    {
                        ArrayList data = DTFPerformanceResultManager.getPerformanceData(runIds[count]);

                        out.write("\nSeries '"+(count+1)+"'\n\n");
                        for (int dataCount=0;dataCount<data.size();dataCount++)
                        {
                            XYDataPoint p = (XYDataPoint)data.get(dataCount);
                            out.write( p.getX() + "," + p.getY() + "\n" );
                        }
                    }
                }
            }
        }
        else
        {
            perfRunId = Long.parseLong(perfRunIdStr);
            DTFPerformanceRunInformation info = DTFPerformanceRunInformation.getRunInformation(perfRunId);

            if ( info != null )
            {
                out.write("Performance Run Id: " + perfRunId + "\n");
                out.write("Test Run Id: " + info.getTestRunId() + "\n");
                out.write("Test Name: " + info.getTestName() + "\n");
                out.write("Task Name: " + info.getTaskName() + "\n");
                out.write("Permutation Code: " + info.getPermutationCode() + "\n");
            }
            else
            {
                response.sendError(400, "Cannot find performance run '"+perfRunId+"'");
            }

            ArrayList data = DTFPerformanceResultManager.getPerformanceData(perfRunId);

            response.setContentType("text/plain");

            for (int count=0;count<data.size();count++)
            {
                XYDataPoint p = (XYDataPoint)data.get(count);
                out.write( p.getX() + "," + p.getY() + "\n" );
            }
        }

        out.close();
    }
}

