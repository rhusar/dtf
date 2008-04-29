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
// $Id: VerifyDTFXML.java 170 2008-03-25 18:59:26Z jhalliday $

package org.jboss.dtf.testframework.utils;

import org.jboss.dtf.testframework.coordinator.*;

import java.io.File;
import java.net.URL;

public class VerifyDTFXML
{

	public static boolean verify(URL url)
	{
        boolean result = false;
        try
        {
            System.out.println("Parsing test definitions file...");
            TestDefinitionRepository testRepository = new TestDefinitionRepository(url);
            TaskDefinitionRepository taskRepository = new TaskDefinitionRepository(url);

            System.out.println("Verifying test definitions...");
            result = testRepository.verifyRepository(taskRepository);

            System.out.println("Number of tests defined: "+testRepository.getTestDefinitionsMap().size());

            System.out.println("Complete");
        }
        catch (Exception e)
        {
            System.err.println("ERROR - "+e);
            result = false;
        }

		return(result);
	}

	public static void main(String[] args)
	{
		String testDefs = null;
		boolean local = false;

		for (int count=0;count<args.length;count++)
		{
			if (args[count].equalsIgnoreCase("-local"))
				local = true;
			else
			if (args[count].equalsIgnoreCase("-testdefs"))
				testDefs = args[count+1];
		}

		if (testDefs == null)
		{
			System.out.println("Usage: VerifyDTFXML [-testdefsurl <xml file>] [-local]");
			System.out.println("  -testdefs <file> | specify the XML file to verify");
			System.out.println("  -local           | indicates that the XML filename given above is a local file");
		}
		else
		{
			URL xmlFile = null;

			try
			{
				if (local)
					xmlFile = new File(testDefs).toURL();
				else
					xmlFile = new URL(testDefs);

				if (VerifyDTFXML.verify(xmlFile))
				{
					System.out.println("File '"+testDefs+"' is a valid test definition file");
				}
			}
			catch (java.net.MalformedURLException e)
			{
				System.err.println("ERROR - Malformed URL generated from '"+testDefs+"'");
				System.exit(1);
			}
		}
	}
}
