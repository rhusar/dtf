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
package org.jboss.dtf.testframework.coordinator2;

import org.jdom.input.SAXBuilder;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.net.URL;

import org.jboss.dtf.testframework.coordinator.TaskNotFound;

public class TaskDefinitionRepository
{
	private final static String IMPORT_ELEMENT = "import";
	private final static String IMPORT_ELEMENTS_TEST_SET_ATTRIBUTE = "test-set";

	private HashMap _taskDefinitions = new HashMap();

	public TaskDefinitionRepository()
	{
	}

	public TaskDefinitionRepository(URL testDefinitionFile)
	{
		parse(testDefinitionFile);
	}

	private void parse(URL testDefinitionFile)
	{
		try
		{
			SAXBuilder xmlBuilder = new SAXBuilder();
			Document doc = xmlBuilder.build(testDefinitionFile);
			int defaultTimeoutValue = 480;

			/*
			 * Retrieve root element
			 */
			Element root = doc.getRootElement();

			Element defaultTimeout = root.getChild("default_timeout");

			if (defaultTimeout != null)
			{
				defaultTimeoutValue = Integer.parseInt(defaultTimeout.getAttributeValue("value"));
			}

			/**
			 * Find all import references
			 */
			List imports = root.getChildren(IMPORT_ELEMENT);

			for (int count = 0; count < imports.size(); count++)
			{
				Element importElement = (Element) imports.get(count);
				String testSetAttr = importElement.getAttributeValue(IMPORT_ELEMENTS_TEST_SET_ATTRIBUTE);

				parse(new URL(testDefinitionFile, testSetAttr));
			}

			getTaskDeclarations(_taskDefinitions, defaultTimeoutValue, root);
		}
		catch (JDOMException e)
		{
			e.printStackTrace();
			System.out.println("\nERROR: Incorrect test definition file");
			System.exit(0);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			System.out.println(e.toString());
			System.exit(0);
		}
	}

	public HashMap getTaskDefinitionsMap()
	{
		return (_taskDefinitions);
	}

	public final ArrayList getTaskDefinitions(String groupId, ArrayList taskIds) throws TaskNotFound
	{
		ArrayList results = new ArrayList();

		for (int taskIdCount = 0; taskIdCount < taskIds.size(); taskIdCount++)
		{
			String findTaskId = (String) taskIds.get(taskIdCount);
			TaskDefinition task = getTaskDefinition(groupId, findTaskId);
			results.add(task);
		}

		return (results);
	}

	protected final String stripGroupId(String name)
	{
		if (name.lastIndexOf('/') != -1)
			name = name.substring(0, name.lastIndexOf('/'));

		return (name);
	}

	protected final String stripTaskId(String name)
	{
		if (name.lastIndexOf('/') != -1)
			name = name.substring(name.lastIndexOf('/') + 1);

		return (name);
	}

	public final TaskDefinition getTaskDefinition(String fullTaskId) throws TaskNotFound
	{
		String groupId = stripGroupId(fullTaskId);
		String taskId = stripTaskId(fullTaskId);

		return (getTaskDefinition(groupId, taskId));
	}

	public final TaskDefinition getTaskDefinition(String groupId, String taskId) throws TaskNotFound
	{
		String groupSearch = groupId;
		HashMap group = _taskDefinitions;

		if (taskId.indexOf('/') != -1)
		{
			/**
			 * If the task id is a fully qualified task name then
			 * find the task using the groups specified
			 */
			groupSearch = taskId.substring(0, taskId.lastIndexOf('/'));
			/**
			 * Remove the group names from the task id
			 */
			taskId = taskId.substring(taskId.lastIndexOf('/') + 1);
		}

		while ((groupSearch.indexOf('/') != -1) && (group != null))
		{
			group = (HashMap) group.get(groupSearch.substring(0, groupSearch.indexOf('/')));

			if (group == null)
				throw new TaskNotFound("Cannot find group '" + groupSearch + "'");

			groupSearch = groupSearch.substring(groupSearch.indexOf('/') + 1);
		}

		if (groupSearch.length() > 0)
		{
			group = (HashMap) group.get(groupSearch);
		}

		if (group == null)
			throw new TaskNotFound("Cannot find group '" + groupSearch + "'(2)");

		TaskDefinition task = (TaskDefinition) group.get(taskId);

		if (task == null)
			throw new TaskNotFound("Cannot find task '" + taskId + "' in group '" + groupId + "'");

		return (task);
	}

	private void getTaskDeclarations(HashMap group, int defaultTimeout, Element root) throws Exception
	{
		/*
		 * Retrieve a list of test_groups
		 */
		List testGroups = root.getChildren("test_group");

		for (int groupCount = 0; groupCount < testGroups.size(); groupCount++)
		{
			Element testGroup = (Element) testGroups.get(groupCount);
			HashMap taskDefinitions = null;

			String groupName = testGroup.getAttributeValue("name");
			taskDefinitions = (HashMap) group.get(groupName);

			if (taskDefinitions == null)
			{
				taskDefinitions = new HashMap();
				group.put(groupName, taskDefinitions);
                System.out.println("Creating group named: "+groupName);
				getTaskDeclarations(taskDefinitions, defaultTimeout, testGroup);
			}
		}

		if (root.getName().equalsIgnoreCase("test_group"))
		{
			/*
			 * Retrieve task declaration element
			 */
			Element taskDeclarations = root.getChild("task_declaration");

			if (taskDeclarations != null)
			{
				List taskList = taskDeclarations.getChildren("task");

				for (int count = 0; count < taskList.size(); count++)
				{
					TaskDefinition task = new TaskDefinition((Element) taskList.get(count), defaultTimeout);
					if (group.containsKey(task.getId()))
					{
						throw new Exception("Task already exists - name not unique");
					}

					group.put(task.getId(), task);
				}
			}
		}
	}

	public final TaskDefinition[] getAllTasksForTest(TestDefinition test) throws TaskNotFound
	{
		ArrayList tasks = test.getTasksInvolved();
		TaskDefinition[] result = new TaskDefinition[tasks.size()];

		for (int count = 0; count < tasks.size(); count++)
		{
			try
			{
				result[count] = getTaskDefinition((String) tasks.get(count));
			}
			catch (TaskNotFound e)
			{
				// This can be thrown if the task's group has not been explicitly given
			}

			if (result[count] == null)
			{
				result[count] = getTaskDefinition(test.getGroupId(), (String) tasks.get(count));
			}
		}
		return (result);
	}
}
