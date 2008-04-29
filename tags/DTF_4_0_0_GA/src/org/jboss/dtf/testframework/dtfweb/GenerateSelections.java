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
 * Copyright (C) 1998, 1999, 2000, 2001, 2002
 *
 * Arjuna Technologies Ltd.
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: GenerateSelections.java 170 2008-03-25 18:59:26Z jhalliday $
 */
package org.jboss.dtf.testframework.dtfweb;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Enumeration;

/**
 * Generate the test selections from the given test definitions.
 * The OS's to be selected are passed in a parameter.
 *
 *    testdefs=http://xxxxxxx
 * 	  productname=ArjunaJTS
 *    oslist=CYGWIN_NT-5.0,Linux,SunOS
 */
public class GenerateSelections extends HttpServlet
{
	private final static String TEST_DEFINITIONS_PARAMETER = "testdefs";
	private final static String OS_LIST_PARAMETER = "oslist";
	private final static String PRODUCT_NAME_PARAMETER = "productname";

	private Hashtable parseTestDefs(Element testDefsElement)
	{
		Hashtable tests = new Hashtable();
		NodeList childNodes = testDefsElement.getChildNodes();

		for (int count=0;count<childNodes.getLength();count++)
		{
			Node node = childNodes.item(count);

			if ( node.getNodeName().equals("test_group") )
			{
				tests.put(node.getAttributes().getNamedItem("name").getNodeValue(), parseTestGroupNode(node));
			}
		}

		return tests;
	}

	private Hashtable parseTestGroupNode(Node testGroupNode)
	{
		Hashtable tests = new Hashtable();
        NodeList childNodes = testGroupNode.getChildNodes();

		for (int count=0;count<childNodes.getLength();count++)
		{
			Node node = childNodes.item(count);

			if ( node.getNodeName().equals("test_group") )
			{
				tests.put( node.getAttributes().getNamedItem("name").getNodeValue(), parseTestGroupNode(node) );
			}
			else
			if ( node.getNodeName().equals("test_declaration") )
			{
				tests.put( node.getAttributes().getNamedItem("id").getNodeValue(), "test" );
			}
		}

		return tests;
	}

	private void generateSelections(String osName, String productName, PrintStream out, Hashtable tests)
	{
		out.println("\t<os id=\""+osName+"\">");
		out.println("\t\t<product id=\""+productName+"\">");

		generateGroupTests(out, tests);

		out.println("\t\t</product>");
		out.println("\t</os>");
	}

	private void generateGroupTests(PrintStream out, Hashtable tests)
	{
		Enumeration e = tests.keys();

		while (e.hasMoreElements())
		{
			String name = (String)e.nextElement();
		    Object value = tests.get(name);

			if ( value instanceof Hashtable )
			{
				out.println("\t\t\t<test_group id=\""+name+"\">");
			 	generateGroupTests(out, (Hashtable)value);
				out.println("\t\t\t</test_group>");
			}
			else
			{
				out.println("\t\t\t\t<test id=\""+name+"\" selected=\"true\"/>");
			}
		}
	}

	protected void doGet(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException
	{
		String testDefsURL = httpServletRequest.getParameter(TEST_DEFINITIONS_PARAMETER);

		try
		{
			String osList = httpServletRequest.getParameter(OS_LIST_PARAMETER);
			String productName = httpServletRequest.getParameter("productname");
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse( new URL(testDefsURL).openStream() );
			Element rootElement = doc.getDocumentElement();

			Hashtable tests = parseTestDefs(rootElement);

            PrintStream out = new PrintStream( httpServletResponse.getOutputStream() );

			out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
			out.println("<test_selection>");
			out.println("\t<description>Automatic selection generated for "+osList+"</description>");

			StringTokenizer st = new StringTokenizer(osList, ",");

			while (st.hasMoreTokens())
			{
				String osName = st.nextToken();

				generateSelections(osName, productName, out, tests);
			}

			out.println("</test_selection>");
		}
		catch (Exception e)
		{
			throw new ServletException("Unexpected exception: "+e);
		}
	}
}
