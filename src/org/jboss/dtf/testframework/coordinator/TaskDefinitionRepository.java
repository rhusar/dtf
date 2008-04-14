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
// $Id: TaskDefinitionRepository.java 170 2008-03-25 18:59:26Z jhalliday $

package org.jboss.dtf.testframework.coordinator;

import java.util.*;
import java.net.URL;

import org.jdom.input.*;
import org.jdom.*;

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
            List imports = root.getChildren( IMPORT_ELEMENT );

            for (int count=0;count<imports.size();count++)
            {
                Element importElement = (Element)imports.get(count);
                String testSetAttr = importElement.getAttributeValue( IMPORT_ELEMENTS_TEST_SET_ATTRIBUTE );

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
        return(_taskDefinitions);
    }

	public final ArrayList getTaskDefinitions(String groupId, ArrayList taskIds) throws TaskNotFound
	{
		ArrayList results = new ArrayList();

		for (int taskIdCount=0;taskIdCount<taskIds.size();taskIdCount++)
		{
			String findTaskId = (String)taskIds.get(taskIdCount);
			TaskDefinition task = getTaskDefinition(groupId, findTaskId);
			results.add(task);
		}

		return(results);
	}

	protected final String stripGroupId( String name )
	{
		if (name.lastIndexOf('/') != -1)
			name = name.substring(0,name.lastIndexOf('/'));

		return(name);
	}

	protected final String stripTaskId( String name )
	{
		if (name.lastIndexOf('/') != -1)
			name = name.substring(name.lastIndexOf('/') +1);

		return(name);
	}

	public final TaskDefinition getTaskDefinition(String fullTaskId) throws TaskNotFound
	{
		String groupId = stripGroupId( fullTaskId );
		String taskId = stripTaskId( fullTaskId );

		return( getTaskDefinition( groupId, taskId ) );
	}

	public final TaskDefinition getTaskDefinition(String groupId, String taskId) throws TaskNotFound
	{
		String groupSearch = groupId;
		HashMap group = _taskDefinitions;

                if ( taskId.indexOf('/') != -1 )
                {
                    /**
                     * If the task id is a fully qualified task name then
                     * find the task using the groups specified
                     */
                    groupSearch = taskId.substring(0,taskId.lastIndexOf('/'));
                    /**
                     * Remove the group names from the task id
                     */
                    taskId = taskId.substring(taskId.lastIndexOf('/') + 1);
                }

                while ( (groupSearch.indexOf('/') != -1) && (group != null) )
                {
                        group = (HashMap)group.get(groupSearch.substring(0,groupSearch.indexOf('/')));

                        if (group == null)
                            throw new TaskNotFound("Cannot find group '"+groupSearch+"'");

                        groupSearch = groupSearch.substring(groupSearch.indexOf('/')+1);
                }

                if (groupSearch.length()>0)
                {
                        group = (HashMap)group.get(groupSearch);
                }

                if (group == null)
                        throw new TaskNotFound("Cannot find group '"+groupSearch+"'");

                TaskDefinition task = (TaskDefinition)group.get(taskId);

                if (task == null)
                        throw new TaskNotFound("Cannot find task '"+taskId+"' in group '"+groupId+"'");

		return(task);
	}

	private void getTaskDeclarations(HashMap group, int defaultTimeout, Element root) throws Exception
	{
		/*
		 * Retrieve a list of test_groups
		 */
		List testGroups = root.getChildren("test_group");

		for (int groupCount=0;groupCount<testGroups.size();groupCount++)
		{
			Element testGroup = (Element)testGroups.get(groupCount);
			HashMap taskDefinitions = null;

			String groupName = testGroup.getAttributeValue("name");
			taskDefinitions = (HashMap)group.get(groupName);

			if (taskDefinitions==null)
			{
				taskDefinitions = new HashMap();
				group.put(groupName, taskDefinitions);
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

				for (int count=0;count<taskList.size();count++)
				{
					Element taskElement = (Element)taskList.get(count);

					String id = taskElement.getAttributeValue("id");
					String classname = taskElement.getAttributeValue("classname");
					String timeoutStr = taskElement.getAttributeValue("timeout");
					String taskRunner = taskElement.getAttributeValue("runner");
                    String paramSetting = taskElement.getAttributeValue("parameter_setting");
                    String[] taskParameters = null;
                    String[] taskJVMParameters = null;
					Hashtable taskRunnerParameters = null;
					int timeout = defaultTimeout;

                    List parameters = taskElement.getChildren("param");
                    taskParameters = new String[parameters.size()];
                    for (int paramCount=0;paramCount<parameters.size();paramCount++)
                    {
                        Element paramElement = (Element)parameters.get(paramCount);
                        taskParameters[paramCount] = paramElement.getTextTrim();
                    }

                    parameters = taskElement.getChildren("jvm_param");

                    taskJVMParameters = new String[parameters.size()];
                    for (int paramCount=0;paramCount<parameters.size();paramCount++)
                    {
                        Element paramElement = (Element)parameters.get(paramCount);
                        taskJVMParameters[paramCount] = paramElement.getTextTrim();
                    }

					/*
					 * Check for task runner attribute, if this doesn't exist
					 * then check for task  runner  element  within the  task
					 * element.  If  that  doesn't  exist throw an exception.
					 */
					if (taskRunner == null)
					{
						System.out.println("Looking for 'runner' element");
						Element taskRunnerElement = taskElement.getChild("runner");

						if (taskRunnerElement != null)
						{
							taskRunner = taskRunnerElement.getAttributeValue("name");

							List taskRunnerParameterElements = taskRunnerElement.getChildren("param");

							taskRunnerParameters = new Hashtable();

							for (int paramCount=0;paramCount<taskRunnerParameterElements.size();paramCount++)
							{
								Element parameterElement = (Element)taskRunnerParameterElements.get(paramCount);
								taskRunnerParameters.put( parameterElement.getAttributeValue("name"), parameterElement.getAttributeValue("value") );
							}
						}

						if (taskRunner == null)
						{
							throw new Exception("Task runner not defined for task "+id);
						}
					}

					if (timeoutStr != null)
					{
						timeout = Integer.parseInt(timeoutStr);
					}

					String type = taskElement.getAttributeValue("type");

                    TaskDefinition task = new TaskDefinition(id,classname,timeout,type,taskRunner,taskRunnerParameters,taskParameters,taskJVMParameters,paramSetting);

                    if (group.containsKey(id))
                    {
                        throw new Exception("Task already exists - name not unique");
                    }

                    group.put(id,task);
				}
			}
		}
	}

	public final TaskDefinition[] getAllTasksForTest(TestDefinition test) throws TaskNotFound
	{
		ArrayList tasks = test.getTasksInvolved();
		TaskDefinition[] result = new TaskDefinition[tasks.size()];

		for (int count=0;count<tasks.size();count++)
		{
			try
			{
				result[count] = getTaskDefinition((String)tasks.get(count));
			}
			catch (TaskNotFound e)
			{
				// This can be thrown if the task's group has not been explicitly given
			}

			if (result[count] == null)
			{
				result[count] = getTaskDefinition(test.getGroupId(), (String)tasks.get(count));
			}
		}
		return(result);
	}
}
