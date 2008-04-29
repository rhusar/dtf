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
// $Id: MergeDTFTestDefinitionFiles.java 170 2008-03-25 18:59:26Z jhalliday $

package org.jboss.dtf.testframework.utils;

import org.jdom.input.*;
import org.jdom.output.*;
import org.jdom.*;

import java.util.*;
import java.io.*;
import java.net.*;

import org.jboss.dtf.testframework.coordinator.InvalidDefinitionFile;

public class MergeDTFTestDefinitionFiles
{
	private Element 	_root = null;
	private HashMap 	_groups = new HashMap();
	private boolean		_answeredAll = false;

	public MergeDTFTestDefinitionFiles(ArrayList local, ArrayList url, String outputFilename)
	{
		_root = new Element("test_set");

		try
		{
			for (int count=0;count<local.size();count++)
			{
				String filename = (String)local.get(count);

				addTestGroups(new File(filename).toURL());
			}

			for (int count=0;count<url.size();count++)
			{
				String urlText = (String)url.get(count);

				addTestGroups(new URL(urlText));
			}

			XMLOutputter outputter = new XMLOutputter();
            outputter.setFormat(Format.getPrettyFormat());

			Document doc = new Document(_root);
			FileOutputStream fos = new FileOutputStream(outputFilename);
            outputter.output(doc, fos);
            fos.close();

            System.out.println("Generated '"+outputFilename+"'");
		}
		catch (Exception e)
		{
			System.out.println("Error - "+e);
			e.printStackTrace(System.err);
		}
	}

	private String queryUser(String query, String defaultText)
	{
		String answer = null;

		System.out.print(query + " ["+defaultText+"]:");
		try
		{
			BufferedReader rdr = new BufferedReader(new InputStreamReader(System.in));
			answer = rdr.readLine();

			if (answer.length() == 0)
				answer = defaultText;
		}
		catch (java.io.IOException e)
		{
		}
		return(answer);
	}

	private Element searchElements(List elements, String attributeName, String value)
	{
		for (int count=0;count<elements.size();count++)
		{
			Element ele = (Element)elements.get(count);

			if (ele.getAttributeValue(attributeName).equalsIgnoreCase(value))
				return(ele);
		}

		return(null);
	}

	private void addTaskDeclarations(Element newGroup, Element taskDeclaration)
	{
		boolean answeredAll = false;

		System.out.println("Merging task declarations");

		Element newTaskDeclaration = newGroup.getChild("task_declaration");

		List tasks = taskDeclaration.getChildren("task");

		if (newTaskDeclaration != null)
		{
			List newTasks = newTaskDeclaration.getChildren("task");


			for (int count=0;count<tasks.size();count++)
			{
				Element task = (Element)tasks.get(count);

				if (task.getName().equalsIgnoreCase("task"))
				{
					String taskId = task.getAttributeValue("id");
					String className = task.getAttributeValue("classname");
					Element newTask = searchElements(newTasks, "id", taskId);

					if (newTask != null)
					{
						String newClassName = newTask.getAttributeValue("classname");
						String answer = "";

						System.out.println("Task '"+taskId+"' already exists current:'"+newClassName+"' new:'"+className+"'");

						if (!answeredAll)
							answer = queryUser("Overwrite with new declaration (y/n/a)","y");

						if (answer.startsWith("a"))
							answeredAll = true;

						if ( (answer.startsWith("y")) || (answeredAll) )
						{
							newGroup.removeContent(newTask);
							newGroup.addContent((Element)task.clone());
						}
					}
					else
					{
						newGroup.addContent(task);
					}
				}
				else
				{
					System.out.println("Found element '"+task.getName()+"' - trimming");
				}
			}
		}
		else
		{
			newGroup.addContent((Element)taskDeclaration.clone());
		}
	}

	private void addTestDeclaration(Element newGroup, Element testDeclaration)
	{
		System.out.println("Merging Test Declaration");
		boolean answeredAll = false;

		String testId = testDeclaration.getAttributeValue("id");
		String descriptiveName = testDeclaration.getAttributeValue("descriptive_name");
		String author = testDeclaration.getAttributeValue("author");

		List newTestDeclarations = newGroup.getChildren("test_declaration");

		Element newTestDeclaration = searchElements(newTestDeclarations, "id", testId);

		if (newTestDeclaration != null)
		{
			System.out.println("Test '"+testId+"' already exists");
			System.out.println("Current:-");
			System.out.println("  Descriptive Name: "+descriptiveName);
			System.out.println("            Author: "+author);
			System.out.println("New:-");
			System.out.println("  Descriptive Name: "+newTestDeclaration.getAttributeValue("descriptive_name"));
			System.out.println("            Author: "+newTestDeclaration.getAttributeValue("author"));

			String answer = queryUser("Overwrite current test declaration (y/n)","y");

			if (answer.equalsIgnoreCase("y"))
			{
				newGroup.removeContent(newTestDeclaration);
				newGroup.addContent((Element)testDeclaration.clone());
			}
		}
		else
		{
			System.out.println("Test '"+testId+"' does not exist merging");

			newTestDeclaration = new Element("test_declaration");

			newTestDeclaration.setAttribute("id",testDeclaration.getAttributeValue("id"));
			newTestDeclaration.setAttribute("descriptive_name",testDeclaration.getAttributeValue("descriptive_name"));
			newTestDeclaration.setAttribute("author",testDeclaration.getAttributeValue("author"));
			newGroup.addContent(newTestDeclaration);

			newTestDeclaration.addContent(testDeclaration);
		}
	}

	public void addGroup(Element groupElement)
	{
		String answer = "";
		String groupName = groupElement.getAttributeValue("name");
		Element newGroup = null;

		// If the group is already part of the merged XML file then just add the tests
		if ( ( newGroup  = (Element)_groups.get(groupName) ) != null )
		{
			System.out.println("Group '"+groupName+"' exists");

			if (!_answeredAll)
				answer = queryUser("Do you want to merge this group (y/n/a)","a");

			if (answer.startsWith("a"))
				_answeredAll = true;

			if ( (answer.startsWith("y")) || (_answeredAll) )
			{
				System.out.println("Merging");

				addTaskDeclarations(newGroup, groupElement.getChild("task_declaration"));

				List testsInGroup = groupElement.getChildren("test_declaration");

				for (int count=0;count<testsInGroup.size();count++)
				{
					Element testElement = (Element)testsInGroup.get(count);

					addTestDeclaration(newGroup, testElement);
				}
			}
			else
			{
				System.out.println("Skipping");
			}
		}
		else
		{
			System.out.println("Group '"+groupName+"' does not already exist");

			newGroup = new Element("test_group");
			newGroup.setAttribute("name",groupName);
			_root.addContent(newGroup);
			_groups.put(groupName,newGroup);

			System.out.println("Merging");

			addTaskDeclarations(newGroup, groupElement.getChild("task_declaration"));

			List testsInGroup = groupElement.getChildren("test_declaration");

			for (int count=0;count<testsInGroup.size();count++)
			{
				Element testElement = (Element)testsInGroup.get(count);

				addTestDeclaration(newGroup, testElement);
			}
		}
	}

	public void addTestGroups(URL url)
	{
		try
		{
			SAXBuilder xmlBuilder = new SAXBuilder();
            Document doc = null;
            try {
                doc = xmlBuilder.build(url);
            } catch(IOException e) {
                throw new JDOMException(e.toString());
            }

			/*
			 * Retrieve the source's root element
			 */
			Element sourceRoot = doc.getRootElement();

			List children = sourceRoot.getChildren();

			for (int count=0;count<children.size();count++)
			{
				Element childElement = (Element)children.get(count);
				String elementName = childElement.getName();

				if (elementName.equalsIgnoreCase("test_group"))
				{
					addGroup(childElement);
				}
				else
				{
					System.out.println("Element '"+elementName+"' found - trimming");
				}
			}
		}
		catch (JDOMException e)
		{
			e.printStackTrace();
			System.out.println("\nERROR: Incorrect test definition file");
			System.exit(0);
		}

	}

	public static void main(String[] args)
	{
		ArrayList localSourceFiles = new ArrayList();
		ArrayList urlSourceFiles = new ArrayList();
		String outputFilename = "output.xml";

		for (int count=0;count<args.length;count++)
		{
			if (args[count].equalsIgnoreCase("-localsource"))
			{
				System.out.println("Adding Local source '"+args[count+1]+"'");
				localSourceFiles.add(args[count+1]);
			}
			if (args[count].equalsIgnoreCase("-urlsource"))
			{
				System.out.println("Adding URL source '"+args[count+1]+"'");
				urlSourceFiles.add(args[count+1]);
			}
			if (args[count].equalsIgnoreCase("-output"))
			{
				outputFilename = args[count+1];
				System.out.println("Output file set as '"+outputFilename+"'");
			}
		}

		if ( (localSourceFiles.size() == 0) && (urlSourceFiles.size() == 0) )
		{
			System.out.println("Usage: MergeDTFTestDefinitionFiles [-output <file>] [-localsource <file>] [-urlsource <url>]...");
			System.out.println(" -localsource <file> | Specifies a local XML source file");
			System.out.println(" -urlsource <url>    | Specifies the location of an XML source file");
			System.out.println(" -output <file>      | Specifies the name of the resultant output file");
		}
		else
		{
			System.out.println("Merging sources...");

			new MergeDTFTestDefinitionFiles(localSourceFiles, urlSourceFiles,outputFilename);

			System.out.println("Finished.");
		}
	}
}
