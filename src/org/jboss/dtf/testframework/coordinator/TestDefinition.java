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
// $Id: TestDefinition.java 170 2008-03-25 18:59:26Z jhalliday $

package org.jboss.dtf.testframework.coordinator;

import java.util.*;

import org.jboss.dtf.testframework.utils.UniqueNameGenerator;
import org.jboss.dtf.testframework.coordinator.actions.PerformAction;
import org.jboss.dtf.testframework.coordinator.actions.StartAction;
import org.jboss.dtf.testframework.coordinator.actions.WaitForAction;
import org.jboss.dtf.testframework.coordinator.actions.TerminateAction;
import org.jdom.Element;

/**
 * This class represents a tests definition gained from the XML file.
 */
public class TestDefinition
{
	public static final String 	PERFORM_TASK_NAME = "perform_task",
								START_TASK_NAME = "start_task",
								WAIT_FOR_TASK_NAME = "wait_for_task",
								TERMINATE_TASK_NAME = "terminate_task";


	private String 		_id = null;
	private String		_group = null;
	private String 		_descriptiveName = null;
	private ArrayList 	_actionList = new ArrayList();
	private HashMap		_osProductList = new HashMap();
	private int			_retryCount = 0;
	private int			_numberOfTasksStarted = 0;
	private String[]	_names;
	private int			_namesRequired;
	private HashSet		_completedCombinations = new HashSet();
	private String		_description = null;
	private Hashtable	_runnerParameters = null;

	public TestDefinition(String group, String id, String descriptiveName, String description, int namesRequired, Hashtable runnerParameters)
	{
		_id = id;
		_group = group;
		_descriptiveName = descriptiveName;
		_description = description;
		_namesRequired = namesRequired;
		_runnerParameters = runnerParameters;

		populateNameList(namesRequired);
	}

	public final Hashtable getParametersForRunner(String runnerName)
	{
		return( (Hashtable) _runnerParameters.get(runnerName) );
	}

	public final void addCompletedCombination(PermutationCode completedPermutation)
	{
		_completedCombinations.add(completedPermutation.toString());
	}

	public final boolean isPermutationCompleted(PermutationCode permCode)
	{
		return(_completedCombinations.contains(permCode.toString()));
	}

	public final void increaseRetryCount()
	{
		_retryCount++;
	}

	public final int getRetryCount()
	{
		return(_retryCount);
	}

	public final String getGroupId()
	{
		return(_group);
	}

	public final String getDescription()
	{
		return(_description);
	}

	public final String getId()
	{
		return(_id);
	}

	public final String getFullId()
	{
		return(generateFullId(_group,_id));
	}

	public final static String generateFullId(String groupId, String testId)
	{
		return(groupId + "/" + testId);
	}

	public final ArrayList getActionList()
	{
		return(_actionList);
	}

	public final HashMap getOSProductMap()
	{
		return(_osProductList);
	}

	/**
	 * Retrieve a list of OSProductCombinations
	 * which are still to be executed.
	 *
	 * @return
	 */
	public final ArrayList getToBePerformed()
	{
		ArrayList toBePerformed = new ArrayList();

		Iterator i = _osProductList.keySet().iterator();

		while (i.hasNext())
		{
			String osId = (String)i.next();

			HashMap productMap = (HashMap)_osProductList.get(osId);

			Iterator productIter = productMap.keySet().iterator();

			while ( productIter.hasNext() )
			{
				String productId = (String)productIter.next();

				Boolean selected = (Boolean) productMap.get( productId );

				if ( !selected.booleanValue() )
				{
                    toBePerformed.add( new OSProductCombination(osId, productId) );
				}
			}
		}

		return toBePerformed;
	}

	public final void addOSProduct(String osId, String productId)
	{
		Object product = _osProductList.get(productId);
		HashMap productMap = null;

		if (product == null)
		{
			_osProductList.put(osId, productMap = new HashMap());
		}
		else
		{
			productMap = (HashMap)product;
		}

		if (!productMap.containsKey(productId))
		{
			productMap.put(productId,new Boolean(false));
		}
	}

    public final void addPerformTaskAction(PerformAction a)
    {
        _actionList.add(a);
    }

	public final void addPerformTaskAction(String id, String runtimeId, String location, String nameList, String[] parameters, String[] jvmParameters)
	{
		Action performAction = new PerformAction(id,runtimeId,location,parameters,jvmParameters,nameList);
		_actionList.add(performAction);
	}

    public final void addStartTaskAction(StartAction a)
    {
        _actionList.add(a);
    }

	public final void addStartTaskAction(String id, String location, String nameList, String runtimeId, String[] parameters, String[] jvmParameters)
	{
		Action startTaskAction = new StartAction(id, runtimeId, location, parameters, jvmParameters, nameList);
		_actionList.add(startTaskAction);
	}

    public final void addWaitForTaskAction(WaitForAction a)
    {
        _actionList.add(a);
    }

	public final void addWaitForTaskAction(String runtimeId)
	{
		Action waitForTaskAction = new WaitForAction(runtimeId);
		_actionList.add(waitForTaskAction);
	}

    public final void addTerminateTaskAction(TerminateAction a)
    {
        _actionList.add(a);
    }

	public final void addTerminateTaskAction(String runtimeId)
	{
		Action terminateTaskAction = new TerminateAction(runtimeId);
		_actionList.add(terminateTaskAction);
	}

    public final Action getActionWithRuntimeId(String runtimeId)
    {
		ArrayList results = new ArrayList();

		for (int actionCount=0;actionCount<_actionList.size();actionCount++)
		{
			Action action = (Action)_actionList.get(actionCount);
			try
			{
				String associatedTask = action.getAssociatedTaskId();

				if ( (action.getType() == Action.PERFORM_TASK) || (action.getType() == Action.START_TASK) )
				{
                    String currentRuntimeId = action.getAssociatedRuntimeTaskId();
					if ( (currentRuntimeId != null) && (currentRuntimeId.equalsIgnoreCase(runtimeId)))
                    {
					    return(action);
                    }
				}
			}
			catch (NoAssociatedData e)
			{
				// Ignore it
			}
		}

		return(null);
	}

	public final ArrayList getTasksInvolved()
	{
		ArrayList results = new ArrayList();

		for (int actionCount=0;actionCount<_actionList.size();actionCount++)
		{
			Action action = (Action)_actionList.get(actionCount);
			try
			{
				String associatedTask = action.getAssociatedTaskId();

				if ( (action.getType() == Action.PERFORM_TASK) || (action.getType() == Action.START_TASK) )
				{
					results.add(associatedTask);
				}
			}
			catch (NoAssociatedData e)
			{
				// Ignore it
			}
		}

		return(results);
	}

	public void setNumberOfTasksStarted(int numTasks)
	{
		_numberOfTasksStarted = numTasks;
	}

	public int getNumberOfTasksStarted()
	{
		return(_numberOfTasksStarted);
	}

	public final String getName(int num)
	{
		return(_names[num]);
	}

	public final int getNamesRequired()
	{
		return(_namesRequired);
	}

	private final void populateNameList(int numberOfNames)
	{
		_names = new String[numberOfNames];

		for (int count=0;count<numberOfNames;count++)
		{
			_names[count] = getFullId()+ "/" +UniqueNameGenerator.getName();
		}
	}

	public void verifyTest(TaskDefinitionRepository taskDefRep) throws TaskNotFound
	{
		try
		{
			TaskDefinition[] tasks = taskDefRep.getAllTasksForTest(this);
			ArrayList startedTasks = new ArrayList();

			for (int actionCount=0;actionCount<_actionList.size();actionCount++)
			{
				Action action = (Action)_actionList.get(actionCount);
				try
				{
					String associatedRuntimeTaskId = action.getAssociatedRuntimeTaskId();

					if (action.getType() == Action.START_TASK)
					{
						startedTasks.add(associatedRuntimeTaskId);
					}
					else
					{
						if ( (action.getType() == Action.TERMINATE_TASK) || (action.getType() == Action.WAIT_FOR_TASK) )
						{
							if (startedTasks.contains(associatedRuntimeTaskId))
							{
								startedTasks.remove(associatedRuntimeTaskId);
							}
							else
							{
								throw new TaskNotFound("ERROR - Action performed on '"+associatedRuntimeTaskId+"' and task not running in test '"+getFullId()+"'");
							}
						}
					}
				}
				catch (NoAssociatedData e)
				{
					// Ignore it
				}
			}
		}
		catch (TaskNotFound e)
		{
			throw e;
		}
	}

    public String toString()
    {
        return(this.getId());
    }

    public final Element serializeToXML() throws NoAssociatedData
    {
        Element testDefElement = new Element("test_declaration");

        testDefElement.setAttribute("id", getId());

        Element actionListElement = new Element("action_list");
        for (int count=0;count<_actionList.size();count++)
        {
            Action a = (Action)_actionList.get(count);

            actionListElement.addContent(a.serializeToXML());
        }

        return(testDefElement);
    }

}
