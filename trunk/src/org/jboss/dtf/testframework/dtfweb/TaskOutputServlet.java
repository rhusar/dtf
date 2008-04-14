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
 * $Id: TaskOutputServlet.java 170 2008-03-25 18:59:26Z jhalliday $
 */
package org.jboss.dtf.testframework.dtfweb;

import org.jboss.dtf.testframework.dtfweb.utils.DBUtils;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class TaskOutputServlet extends HttpServlet
{
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        DTFResultsManager dtfResultsManager = new DTFResultsManager();

        long runId = Long.parseLong(request.getParameter("runid"));
        String testName = request.getParameter("testname");
        String taskName = request.getParameter("taskname");
        String permutationCode = request.getParameter("permutationCode");
        String type = request.getParameter("type");

        response.setContentType("text/plain");
        InputStream in = dtfResultsManager.getTestTaskOutput(runId, testName, taskName, permutationCode, type);

        if ( in != null )
        {
            byte[] buffer = new byte[32768];
            int bytesRead;
            OutputStream outStr = response.getOutputStream();

            while ( ( bytesRead = in.read(buffer) ) > 0 )
            {
                String str = new String(buffer,0,bytesRead);
                outStr.write(buffer,0,bytesRead);
            }
        }

        in.close();
    }
}
