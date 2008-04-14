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
package org.jboss.dtf.testframework.dtfweb;

import org.jboss.dtf.testframework.utils.ServiceUtils;
import org.jboss.dtf.testframework.utils.RemoteFileReaderInterface;
import org.jboss.dtf.testframework.testnode.TestNodeInterface;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import java.io.*;

/*
 * Copyright (C) 1998, 1999, 2000, 2001, 2002, 2003
 *
 * Arjuna Technologies Ltd.
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: ShowLogOutput.java 170 2008-03-25 18:59:26Z jhalliday $
 */

public class ShowLogOutput extends HttpServlet
{
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
	{
		resp.setContentType("text/plain");

		PrintWriter out = resp.getWriter();

		short nodeId = Short.parseShort(req.getParameter("node"));
		boolean isOutput = req.getParameter("error") == null;
		String productName = req.getParameter("product");
		System.err.println("Node Id = "+nodeId+" Product Name = "+productName);
		ServiceUtils su = new ServiceUtils(new DTFRunManager().getDefaultNameServiceURI());

		try
		{
			TestNodeInterface tni = su.getServiceRegister().lookupService(nodeId);

			RemoteFileReaderInterface in = tni.getDeployLogOutput(productName, isOutput);
			String inLine;

			while ( ( inLine = in.readLine() ) != null )
			{
				out.println(inLine);
			}

			in.close();
		}
		catch (IOException e)
		{
			out.write("There is no current deploy log for this product");
			e.printStackTrace(System.err);
		}
		catch (Exception e)
		{
			throw new ServletException("Failed to retrieve service", e);
		}
	}
}
