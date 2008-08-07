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

import org.jdom.Element;

import java.util.*;

import org.jboss.dtf.testframework.coordinator.actions.PerformAction;
import org.jboss.dtf.testframework.coordinator.actions.StartAction;
import org.jboss.dtf.testframework.coordinator.actions.WaitForAction;
import org.jboss.dtf.testframework.coordinator.actions.TerminateAction;
import org.jboss.dtf.testframework.coordinator.Action;
import org.jboss.dtf.testframework.coordinator.NoAssociatedData;
import org.jboss.dtf.testframework.coordinator.TaskNotFound;
import org.jboss.dtf.testframework.coordinator.PermutationCode;

import org.jboss.dtf.testframework.utils.UniqueNameGenerator;
import org.jboss.dtf.testframework.coordinator2.exceptions.InvalidPermutationException;

public class TestDefinition
{
	public static final String TERMINATE_TASK_NAME = "terminate_task";

	private String 		_id = null;
	private String		_group = null;
	private String 		_descriptiveName = null;
	private ArrayList 	_actionList = new ArrayList();
	private int			_retryCount = 0;
	private int			_numberOfTasksStarted = 0;
	private String[]	_names;
	private int			_namesRequired;
	private String		_description = null;
	private Hashtable	_runnerParameters = new Hashtable();
	private Hashtable	_productCombinations = new Hashtable();
	private ArrayList	_permutations = new ArrayList();

	/**
	 * Create emtpy TestDefinition
	 */
	public TestDefinition( String group )
	{
		_group = group;
	}

	public void setId(String id)
	{
		_id = id;
	}

	/**
	 * Create TestDefinition class from test_declaration XML DOM element
	 *
	 * @param group
	 * @param testDecElement
	 */
	public TestDefinition( String group, Element testDecElement )
	{

		int numberOfTasksStarted = 0;
		/*
		 * Retrieve id, descriptive name
		 */
		Element descriptionElement = testDecElement.getChild("description");
		Element configElement = testDecElement.getChild("configuration");

		_id = testDecElement.getAttributeValue("id");
		_descriptiveName = testDecElement.getAttributeValue("descriptive_name");
		_description  = (descriptionElement!=null)?descriptionElement.getText().trim():"";
        _group = group;

		if (configElement != null)
		{
			String namesRequired = configElement.getAttributeValue("names_required");

			if (namesRequired!=null)
				_namesRequired = Integer.parseInt(namesRequired);

			List runnerConfigElements = configElement.getChildren("runner");

			for (int elementCount=0;elementCount<runnerConfigElements.size();elementCount++)
			{
				Element runnerConfigElement = (Element)runnerConfigElements.get(elementCount);

				String configForTaskRunnerName = runnerConfigElement.getAttributeValue("name");

				Hashtable parameters = new Hashtable();

				List parameterElements = runnerConfigElement.getChildren("param");

				for (int paramCount=0;paramCount<parameterElements.size();paramCount++)
				{
					Element paramElement = (Element)parameterElements.get(paramCount);

					parameters.put( paramElement.getAttributeValue("name"), paramElement.getAttributeValue("value") );
				}

				_runnerParameters.put( configForTaskRunnerName, parameters);
			}
		}

		/*
		 * Retrieve and interpret the action list for this test
		 */
		Element actionListElement = testDecElement.getChild("action_list");

		List actionList = actionListElement.getChildren();

		for (int count=0;count<actionList.size();count++)
		{
			Element actionElement = (Element)actionList.get(count);

			Action action = null;

			// If the action is a perform task action
			if ( ( action = PerformAction.getPerformAction(actionElement) ) != null )
			{
				addPerformTaskAction((PerformAction)action);
				numberOfTasksStarted++;
			}
			else
			// If the action is a start task action
			if ( ( action = StartAction.getStartAction(actionElement) ) != null )
			{
				addStartTaskAction((StartAction)action);
				numberOfTasksStarted++;
			}
			else
			// If the action is a wait for task action
			if ( ( action = WaitForAction.getWaitForAction(actionElement) ) != null )
			{
				addWaitForTaskAction((WaitForAction)action);
			}
			else
			// If the action is a terminate task action
			if ( ( action = TerminateAction.getTerminateAction(actionElement) ) != null )
			{
				addTerminateTaskAction((TerminateAction)action);
			}
		}

		setNumberOfTasksStarted(numberOfTasksStarted);
		populateNameList(_namesRequired);
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

	/**
	 * Retrieve the number of nodes required to run this test.
	 * This is calculated by finding out the number of tasks
	 * that are location dependent (i.e. in ideal conditions
	 * should be run on their own node).  Location independent
	 * and All node tasks are not counted as they are run on
	 * nodes already in use.
	 *
	 * @return The number of nodes required.
	 */
	public int getNumberOfNodesRequired()
	{
		int nodesRequired = 0;

		for (int count=0;count<_actionList.size();count++)
		{
			Action a = (Action)_actionList.get(count);

			try
			{
				/**
				 * Look at all tasks that start a task
				 */
				if ( ( a instanceof PerformAction ) || ( a instanceof StartAction ) )
				{
					switch (a.getLocationType())
					{
						case Action.LOCATION_DEPENDENT:
							nodesRequired++;
							break;
					}
				}
			}
			catch (NoAssociatedData nad)
			{
			}
		}

        if(nodesRequired == 0) {
            nodesRequired = 1; // all tasks are flexible so can share a node.
        }

        return nodesRequired;
	}

	public void addOSProduct(OSProductCombination p)
	{
		ArrayList osProductCombinations = (ArrayList)_productCombinations.get(p.getProductId());

		if ( osProductCombinations == null )
		{
			osProductCombinations = new ArrayList();
			_productCombinations.put(p.getProductId(), osProductCombinations);
		}

		osProductCombinations.add(p);
	}

	private void addPermutation(RuntimePermutation perm) throws InvalidPermutationException
	{
        if ( perm.getNumberOfNodes() != getNumberOfNodesRequired() )
		{
            throw new InvalidPermutationException("The number of nodes in the permutation does not match the required number of nodes");
		}

		_permutations.add(perm);
	}

	public ArrayList getPermutations()
	{
		return _permutations;
	}

	protected void setNumberOfTasksStarted(int numTasks)
	{
		_numberOfTasksStarted = numTasks;
	}

	public int getNumberOfTasksStarted()
	{
		return(_numberOfTasksStarted);
	}

	public final Hashtable getParametersForRunner(String runnerName)
	{
		return( (Hashtable) _runnerParameters.get(runnerName) );
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

	public String[] getPermutationProductIds()
	{
		String[] returnValue = new String[_productCombinations.size()];
		_productCombinations.keySet().toArray(returnValue);

		return returnValue;
	}

	public void generateAllPermutations(String productId) throws InvalidPermutationException
	{
		ArrayList osProductCombinations = (ArrayList)_productCombinations.get(productId);
		if ( (getNumberOfNodesRequired() == 0) || (osProductCombinations.size()==0) )
		{
			return;
		}

		int[] positions = new int[getNumberOfNodesRequired()];
		PermutationCode resultPermCode = new PermutationCode();
		Object[] objs = osProductCombinations.toArray();
		boolean finished = false;

		do
		{
			RuntimePermutation permutation = new RuntimePermutation();

			for (int count=0;count<positions.length;count++)
			{
				permutation.add( (OSProductCombination)objs[positions[count]] );
			}

			for (int count=positions.length-1;count>=0;count--)
			{
				if ( ++positions[count] == osProductCombinations.size() )
				{
					if (count!=0)
						positions[count] = 0;
					else
						finished = true;
				}
				else
					break;
			}

			addPermutation(permutation);
		} while (!finished);
	}

	public String getDescriptiveName()
	{
		return _descriptiveName;
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
