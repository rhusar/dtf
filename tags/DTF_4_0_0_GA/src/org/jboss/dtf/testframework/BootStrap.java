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
//
// Copyright (C) 2001,
//
// HP Arjuna Labs
// Newcastle upon Tyne,
// Tyne and Wear,
// UK.
//
// $Id: BootStrap.java 170 2008-03-25 18:59:26Z jhalliday $

package org.jboss.dtf.testframework;

import java.util.*;
import java.net.*;
import java.io.IOException;

import org.jdom.input.*;
import org.jdom.*;
import org.jboss.dtf.testframework.*;

/**
 * Loads classes from a remote server using the URLClassLoader
 * using an XML configuration file located on the remote server
 * a given class is instantiated.
 */

public class BootStrap
{
	private static class BootStrapConfiguration
	{
		static URL[]   _urls;
		static String  _bootClass;
	}

	/**
	 * Parses the XML file pointed to by the URL.  Populates the necessary
	 * configuration information gained for the XML file.
	 */
	static private void parseXML(String xmlURL) throws InvalidConfiguration, org.jdom.JDOMException, java.net.MalformedURLException
	{
		/*
		 * Use the SAXBuilder to parse a remote configuration file
		 * containing the URL classpath and the initial class to load
		 * and run
		 */
		SAXBuilder xmlBuilder = new SAXBuilder();
		Document doc = null;
        try {
            doc = xmlBuilder.build(new URL(xmlURL));
        } catch(IOException e) {
            throw new InvalidConfiguration();
        }

		/*
		 * Retrieve root element, then retrieve the classpath element
		 */
		Element root = doc.getRootElement();
		Element classpathRootElement = root.getChild("classpath");

		/*
		 * If no classpath element report invalid configuration
		 */
		if (classpathRootElement == null)
			throw new InvalidConfiguration();

		/*
		 * Retrieve the list of child elements in the <CLASSPATH> element
		 */
		java.util.List classpathList = classpathRootElement.getChildren();
		Iterator classpath_itr = classpathList.iterator();
		Vector urls = new Vector();

		System.out.print("Classpath: ");
		while (classpath_itr.hasNext())
		{
			Element classpathElement = (Element)classpath_itr.next();
			urls.addElement( classpathElement.getText() );
			System.out.print(classpathElement.getText() + ";");
		}

		/*
		 * Create an array from the vector of URLS
		 */
		BootStrapConfiguration._urls = new URL[urls.size()];
		for (int count=0;count<urls.size();count++)
		{
			String urlString = (String)urls.elementAt(count);

			BootStrapConfiguration._urls[count] = new URL(urlString);
		}

		Element initialClassElement = root.getChild("initial_class");

		if (initialClassElement == null)
			throw new InvalidConfiguration();

		BootStrapConfiguration._bootClass = initialClassElement.getTextTrim();
		System.out.println("\nInitial Class:" + BootStrapConfiguration._bootClass);
	}

	public static void main(String args[])
	{
		if (args.length == 0)
		{
			System.out.println("Usage: TestSuiteBootStrap [URL:\\CONFIGURATION.XML]");
			System.out.println("   e.g. TestSuiteBoot http://www.test.com/files/xml/config.xml");
			System.exit(0);
		}

		try
		{
			System.out.println("Retrieving XML configuration file: "+args[0]);

			/*
			 * Parse the XML configuration file
			 */
			parseXML(args[0]);

			/*
			 * Create a URL ClassLoader passing in the URL's of the classes
			 * in the XML configuration file.
			 */
			URLClassLoader urlClassLoader = new URLClassLoader(BootStrapConfiguration._urls);

			/*
			 * Load the initial class configured in the XML configuration file.
			 */
			Class bootClass = urlClassLoader.loadClass(BootStrapConfiguration._bootClass);

			/*
			 * Create a new instance of this class
			 */
			Object boot = bootClass.newInstance();
		}
		catch (org.jdom.JDOMException jdome)
		{
			System.err.println("XML parser error - is the configuration URL correct?");
		}
		catch (InvalidConfiguration ic)
		{
			System.err.println("The XML configuration file is not valid");
		}
		catch (ClassNotFoundException cnfe)
		{
			System.err.println("Boot class does not exist in classpath");
		}
		catch (InstantiationException ie)
		{
			System.err.println("Class does not implement Bootable");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
