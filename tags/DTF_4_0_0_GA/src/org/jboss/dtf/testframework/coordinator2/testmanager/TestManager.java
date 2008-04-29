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
 * Arjuna Technology Ltd.
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: TestManager.java 170 2008-03-25 18:59:26Z jhalliday $
 */
package org.jboss.dtf.testframework.coordinator2.testmanager;

import org.jboss.dtf.testframework.coordinator2.runmanager.RunListElement;
import org.jboss.dtf.testframework.coordinator2.runmanager.TestNodeTicket;
import org.jboss.dtf.testframework.coordinator2.runmanager.ResultListener;
import org.jboss.dtf.testframework.coordinator2.runmanager.NodeManager;
import org.jboss.dtf.testframework.coordinator2.Coordinator;
import org.jboss.dtf.testframework.coordinator2.TaskDefinitionRepository;
import org.jboss.dtf.testframework.coordinator2.OSProductCombination;
import org.jboss.dtf.testframework.coordinator2.TaskDefinition;
import org.jboss.dtf.testframework.coordinator2.TestDefinition;
import org.jboss.dtf.testframework.coordinator2.TaskDataHandler;
import org.jboss.dtf.testframework.coordinator2.auditlog.AuditLogEntry;

import org.jboss.dtf.testframework.testnode.*;

import org.jboss.dtf.testframework.utils.ArrayUtils;
import org.jboss.dtf.testframework.utils.ParameterPreprocessor;
import org.jboss.dtf.testframework.utils.UniqueNameGenerator;
import org.jboss.dtf.testframework.utils.StringPreprocessor;

import org.jboss.dtf.testframework.coordinator.UnsupportedProduct;
import org.jboss.dtf.testframework.coordinator.Action;
import org.jboss.dtf.testframework.coordinator.NoAssociatedData;
import org.jboss.dtf.testframework.coordinator.TaskNotFound;
import org.jboss.dtf.testframework.coordinator.RunTaskFailure;
import org.jboss.dtf.testframework.serviceregister.ServiceNotFound;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.HashSet;
import java.util.Enumeration;

import org.apache.log4j.Logger;

public class TestManager extends Thread
{
	private final static int			INITIAL_BACKOFF_PERIOD = 500;
	private final static int			MAX_NUM_RETRIES = 6;
	private final static int			BACK_OFF_INCREMENT = 2;

	private static int					TestManagerId = 0;

	private ArrayList					_nodeTicket;
	private NodeTicketReleaseListener   _releaseListener;
	private RunListElement				_element;
	private RunUID						_runId;
	private TaskDefinitionRepository	_taskRepository;
	private boolean						_failed = false;
	private ResultListener				_resultListener = null;

	private Hashtable					_taskToRuntimeMap = new Hashtable();
	private Hashtable					_runtimeTasks = new Hashtable();
	private Hashtable					_taskPermutationCodes = new Hashtable();
	private Hashtable					_usedNodes = new Hashtable();
    private Logger						_logger = Logger.getLogger(this.getClass());
	private ArrayList					_uniqueNames = null;
	private boolean						_testStopped = false;
	private NodeManager					_nodeManager = null;
	private AuditLogEntry				_auditLogEntry = null;

	public TestManager(	RunUID 						runId,
						RunListElement 				element,
						ArrayList 					nodeTicket,
						NodeTicketReleaseListener 	releaseListener,
						TaskDefinitionRepository	taskRepository,
						ResultListener				resultListener,
						AuditLogEntry				auditLogEntry,
						NodeManager					nodeManager )
	{
        this.setName("TestManager["+runId.getUID()+"]("+TestManagerId+++")");
		_runId = runId;
		_releaseListener = releaseListener;
		_nodeTicket = nodeTicket;
        _element = element;
		_taskRepository = taskRepository;
		_resultListener = resultListener;
		_auditLogEntry = auditLogEntry;
		_nodeManager = nodeManager;

		generateUniqueNameList();

		start();
	}

	private void generateUniqueNameList()
	{
		TestDefinition test = _element.getTest();

		_uniqueNames = new ArrayList();

        if ( _logger.isInfoEnabled() )
        {
		    _logger.info("Generating unique name list");
        }

		for (int count=0;count<test.getNamesRequired();count++)
		{
            _uniqueNames.add( _runId.getUID()+"/"+test.getFullId()+"/"+UniqueNameGenerator.getName());
		}
	}

	public RunUID getRunId()
	{
		return _runId;
	}

	/**
	 * This method stops the current test immediately.
	 */
	public boolean stopTest()
	{
		boolean success = true;

        _testStopped = true;

		Object[] nodes = _usedNodes.keySet().toArray();

		for (int count=0;count<nodes.length;count++)
		{
			String nodeName = null;

			try
			{
				TestNodeTicket testNodeTicket = (TestNodeTicket)nodes[count];
				TestNodeInterface testNode = testNodeTicket.getNode();

				nodeName = testNode.getName();

                if ( _logger.isInfoEnabled() )
                {
                    _logger.info("Stopping all tasks running on '"+nodeName+"'");
                }

				testNode.terminateAllTasks();
			}
			catch (java.rmi.RemoteException e)
			{
				_logger.error("Failed to stop all tasks running on '"+nodeName+"': "+e);

				success = false;
			}
		}


		return success;
	}

	public void run()
	{
		String uniquePrefix = UniqueNameGenerator.getUID();

		try
		{
			/*
			 * Initiate this test with the web logger
			 */
			Coordinator.getLoggingService().initiateTest(_element.getTest().getFullId(), _runId, _element.getPermutation().toString(), _element.getTest().getNumberOfTasksStarted());
			Coordinator.getLoggingService().logTestInformation(_element.getTest().getFullId(), _runId, _element.getPermutation().toString(), _element.getTest().getDescription());
		}
		catch (Exception e)
		{
			_logger.error("ERROR - Failed to initialise test with web logging service:"+e);
			_failed = true;
		}

		if ( !_failed )
		{
			/*
			 * For each action in this tests action list
			 * perform the given action
			 */
			ArrayList actionList = _element.getTest().getActionList();
			int count = 0;
			OSProductCombination[] osProductCombination = _element.getPermutation().getPermutationElements();
			int osProductIndex = 0;
			int testNodeIndex = 0;

			while ( (count<actionList.size()) && (!_failed) && (!_testStopped))
			{
				String productName;
				String taskIdToPerform;
				String nameList;
				String location;
				String parameters[];
				String jvmParameters[];
				String runtimeTaskId;
				OSProductCombination taskPermCode;
				long parameterSettings;
				TaskDefinition task = null;

				try
				{
                    if ( _logger.isInfoEnabled() )
                    {
					    _logger.info("Performing action "+ ( count + 1 ) +" of "+actionList.size());
                    }

					Action action = (Action)actionList.get(count);

					switch (action.getType())
					{
						/*
						 * Retrieve the parameters for this action
						 * then retrieve the action's associated task
						 * and run it to completion
						 */
						case Action.PERFORM_TASK :
							taskIdToPerform	= action.getAssociatedTaskId();
							nameList = action.getAssociatedNameList();

							try
							{
                                if ( _logger.isDebugEnabled() )
                                {
								    _logger.debug("Finding task in group '"+_element.getTest().getGroupId()+"' with id '"+taskIdToPerform+"'");
                                }

								task = _taskRepository.getTaskDefinition(_element.getTest().getGroupId(), taskIdToPerform);
							}
							catch (TaskNotFound e)
							{
                                _logger.error("Cannot find task in test group: ", e);
							}

							/**
							 * If the task is not part of this group and has been explicitly specified
							 * then retrieve it from the task respository
							 */
							if (task == null)
							{
								task = _taskRepository.getTaskDefinition(taskIdToPerform);
							}

							parameters = task.getParameters();
							jvmParameters = task.getJVMParameters();
							runtimeTaskId = action.getAssociatedRuntimeTaskId();

							parameterSettings = task.getParameterSettings();

							if ( ( ( parameterSettings & TaskDefinition.OVERRIDE_PARAMETERS ) != 0 ) && ( action.getParameterList().length > 0 ) )
							{
								parameters = action.getParameterList();
							}

							if ( ( ( parameterSettings & TaskDefinition.OVERRIDE_JVM_PARAMETERS ) != 0 ) && ( action.getJVMParameterList().length > 0 ) )
							{
								jvmParameters = action.getJVMParameterList();
							}

							if ( ( parameterSettings & TaskDefinition.PREPEND_PARAMETERS ) != 0 )
							{
								parameters = ArrayUtils.prependArray(parameters, action.getParameterList());
							}

							if ( ( parameterSettings & TaskDefinition.PREPEND_JVM_PARAMETERS ) != 0 )
							{
								jvmParameters = ArrayUtils.prependArray(jvmParameters, action.getJVMParameterList());
							}

							if ( ( parameterSettings & TaskDefinition.APPEND_PARAMETERS ) != 0 )
							{
								parameters = ArrayUtils.appendArray(parameters, action.getParameterList());
							}

							if ( ( parameterSettings & TaskDefinition.APPEND_JVM_PARAMETERS ) != 0 )
							{
								jvmParameters = ArrayUtils.appendArray(jvmParameters, action.getJVMParameterList());
							}

							for (int paramCount=0;paramCount<parameters.length;paramCount++)
							{
								parameters[paramCount] = ParameterPreprocessor.preprocessParameters(parameters[paramCount], false);
							}

							for (int paramCount=0;paramCount<jvmParameters.length;paramCount++)
							{
								jvmParameters[paramCount] = ParameterPreprocessor.preprocessParameters(jvmParameters[paramCount], false);
							}

							location = action.getLocation();

							/** List of nodes to run the task on **/
							ArrayList testNodes = new ArrayList();

							switch (action.getLocationType())
							{
								case Action.LOCATION_INDEPENDENT :
								{
									taskPermCode = _element.getPermutation().getPermutationCodeForLITask();

									if ( runtimeTaskId != null )
									{
										_taskPermutationCodes.put(runtimeTaskId, osProductCombination[osProductIndex]);
									}

									testNodes.add( ((TestNodeTicket)_nodeTicket.get(0)) );
									productName = _element.getPermutation().getPermutationCodeForLITask().getProductId();
									break;
								}

								case Action.LOCATION_ALL :
								{
									taskPermCode = _element.getPermutation().getPermutationCodeForLITask();

									for (int nodeCount=0;nodeCount<_nodeTicket.size();nodeCount++)
									{
										TestNodeTicket tnt = (TestNodeTicket)_nodeTicket.get(nodeCount);
										testNodes.add( tnt );
									}

									productName = _element.getPermutation().getPermutationCodeForLITask().getProductId();

									break;
								}

								default:
								case Action.LOCATION_DEPENDENT :
								{
									if ( location != null )
									{
										TaskIdInterface[] taskId = (TaskIdInterface[])_runtimeTasks.get(location);

										for (int taskCount=0;taskCount<taskId.length;taskCount++)
										{
                                            TestNodeInterface node = Coordinator.getServiceRegistry().lookupService( taskId[taskCount].getServiceId() );

											testNodes.add( _nodeManager.getNodeTicket(node) );
										}

										taskPermCode = (OSProductCombination)_taskPermutationCodes.get(location);
										productName = taskPermCode.getProductId();
									}
                                    else
									{
										TestNodeTicket tnt = (TestNodeTicket)_nodeTicket.get(testNodeIndex);
										testNodes.add( tnt );

										if ( runtimeTaskId != null )
										{
											_taskPermutationCodes.put(runtimeTaskId, osProductCombination[osProductIndex]);
										}

										taskPermCode = osProductCombination[osProductIndex];
										productName = osProductCombination[osProductIndex++].getProductId();

										testNodeIndex = (testNodeIndex + 1) % _nodeTicket.size();
									}
									break;
								}
							}

							_failed = runTaskWaitToComplete(task.getId(), runtimeTaskId, productName, task, _element.getTest(), taskPermCode, _element.getPermutation().toString(), parameters, jvmParameters, uniquePrefix, testNodes );
							break;



							/*
							 * Retrieve the parameters for this action
							 * the retrieve the action's associated task
							 * run it and store the returned TaskId in
							 * the runtime tasks map against its runtime id
							 */
							case Action.START_TASK :

								try
								{
									nameList = action.getAssociatedNameList();
									taskIdToPerform	= action.getAssociatedTaskId();

									try
									{
										task = _taskRepository.getTaskDefinition(_element.getTest().getGroupId(), taskIdToPerform);
									}
									catch (TaskNotFound e)
									{
                                        if ( _logger.isDebugEnabled() )
                                        {
										    _logger.debug("Information: "+e);
                                        }
									}

									/**
									 * If the task is not part of this group and has been explicitly specified
									 * then retrieve it from the task respository
									 */
									if (task == null)
									{
                                        if ( _logger.isDebugEnabled() )
                                        {
										    _logger.debug("Not found: group='"+_element.getTest().getGroupId()+"', taskIdToPerform='"+taskIdToPerform+"', looking elsewhere...");
                                        }

										task = _taskRepository.getTaskDefinition(taskIdToPerform);
									}

									location = action.getLocation();
									parameters = task.getParameters();
									jvmParameters = task.getJVMParameters();
									runtimeTaskId = action.getAssociatedRuntimeTaskId();

									parameterSettings = task.getParameterSettings();

									if ( ( ( parameterSettings & TaskDefinition.OVERRIDE_PARAMETERS ) != 0 ) && ( action.getParameterList().length > 0 ) )
									{
										parameters = action.getParameterList();
									}

									if ( ( ( parameterSettings & TaskDefinition.OVERRIDE_JVM_PARAMETERS ) != 0 ) && ( action.getJVMParameterList().length > 0 ) )
									{
										jvmParameters = action.getJVMParameterList();
									}

									if ( ( parameterSettings & TaskDefinition.PREPEND_PARAMETERS ) != 0 )
									{
										parameters = ArrayUtils.prependArray(parameters, action.getParameterList());
									}

									if ( ( parameterSettings & TaskDefinition.PREPEND_JVM_PARAMETERS ) != 0 )
									{
										jvmParameters = ArrayUtils.prependArray(jvmParameters, action.getJVMParameterList());
									}

									if ( ( parameterSettings & TaskDefinition.APPEND_PARAMETERS ) != 0 )
									{
										parameters = ArrayUtils.appendArray(parameters, action.getParameterList());
									}

									if ( ( parameterSettings & TaskDefinition.APPEND_JVM_PARAMETERS ) != 0 )
									{
										jvmParameters = ArrayUtils.appendArray(jvmParameters, action.getJVMParameterList());
									}

									for (int paramCount=0;paramCount<parameters.length;paramCount++)
									{
										parameters[paramCount] = ParameterPreprocessor.preprocessParameters(parameters[paramCount], false);
									}

									for (int paramCount=0;paramCount<jvmParameters.length;paramCount++)
									{
										jvmParameters[paramCount] = ParameterPreprocessor.preprocessParameters(jvmParameters[paramCount], false);
									}

									/** List of nodes to run the task on **/
									testNodes = new ArrayList();

									switch (action.getLocationType())
									{
										case Action.LOCATION_INDEPENDENT :
										{
											taskPermCode = _element.getPermutation().getPermutationCodeForLITask();
											testNodes.add( ((TestNodeTicket)_nodeTicket.get(0)) );
											productName = _element.getPermutation().getPermutationCodeForLITask().getProductId();

											if ( runtimeTaskId != null )
											{
												_taskPermutationCodes.put(runtimeTaskId, taskPermCode);
											}

											break;
										}

										case Action.LOCATION_ALL :
										{
											taskPermCode = osProductCombination[osProductIndex];

											for (int nodeCount=0;nodeCount<_nodeTicket.size();nodeCount++)
											{
												TestNodeTicket tnt = (TestNodeTicket)_nodeTicket.get(nodeCount);
												testNodes.add( tnt );
											}

											productName = osProductCombination[osProductIndex].getProductId();

											if ( runtimeTaskId != null )
											{
												_taskPermutationCodes.put(runtimeTaskId, taskPermCode);
											}

											break;
										}

										default:
										case Action.LOCATION_DEPENDENT :
										{
											if ( location != null )
											{
												TaskIdInterface[] taskId = (TaskIdInterface[])_runtimeTasks.get(location);

												for (int taskCount=0;taskCount<taskId.length;taskCount++)
												{
													TestNodeInterface node = Coordinator.getServiceRegistry().lookupService( taskId[taskCount].getServiceId() );

													testNodes.add( _nodeManager.getNodeTicket(node) );
												}

												taskPermCode = (OSProductCombination)_taskPermutationCodes.get(location);
												productName = taskPermCode.getProductId();

												if ( runtimeTaskId != null )
												{
													_taskPermutationCodes.put(runtimeTaskId, taskPermCode);
												}
											}
											else
											{
												TestNodeTicket tnt = (TestNodeTicket)_nodeTicket.get(testNodeIndex);
												testNodes.add( tnt );

												taskPermCode = osProductCombination[osProductIndex];
												productName = osProductCombination[osProductIndex++].getProductId();

												testNodeIndex = (testNodeIndex + 1) % _nodeTicket.size();

												if ( runtimeTaskId != null )
												{
													_taskPermutationCodes.put(runtimeTaskId, taskPermCode);
												}
											}
											break;
										}
									}

                                    if ( _logger.isInfoEnabled() )
                                    {
									    _logger.info("Starting task '"+task.getId()+"' with runtime id of '"+runtimeTaskId+"' ('"+productName+"') on "+testNodes.size()+" node(s)");
                                    }

									TaskIdInterface[] newTaskIds;
									newTaskIds = runTask(runtimeTaskId, productName, task, _element.getTest(), taskPermCode, _element.getPermutation().toString(), nameList, parameters, jvmParameters, uniquePrefix, location, testNodes);

									if ( runtimeTaskId != null )
									{
										for (int taskCount=0;taskCount<newTaskIds.length;taskCount++)
										{
											_taskToRuntimeMap.put(newTaskIds[taskCount], runtimeTaskId);
										}
									}

									if ( runtimeTaskId == null )
									{
										_logger.warn("Warning - No runtime id. associated with this start task");
									}
									else
									{
										_runtimeTasks.put(runtimeTaskId, newTaskIds);
									}
								}
								catch (RunTaskFailure e)
								{
									_logger.error("ERROR - While trying to run task '"+e+"'");
									_failed = true;
								}

								break;

						case Action.WAIT_FOR_TASK :
							runtimeTaskId = action.getAssociatedRuntimeTaskId();

                            if ( _logger.isInfoEnabled() )
                            {
							    _logger.info("Waiting for task '"+runtimeTaskId+"'");
                            }

							TaskIdInterface[] taskId = (TaskIdInterface[])_runtimeTasks.get(runtimeTaskId);

							if (taskId==null)
							{
								_logger.error("No such task id '"+runtimeTaskId+"' error in test definition file");
								Coordinator.getLoggingService().logTestInformation(_element.getTest().getFullId(), _runId, _element.getPermutation().toString(),"No such task id '"+runtimeTaskId+"' error in test definition file - test execution halted\n");
								_failed = true;
							}
							else
							{
								for (int taskIdCount=0;taskIdCount<taskId.length;taskIdCount++)
								{
									try
									{
										TestNodeInterface testNode = Coordinator.getServiceRegistry().lookupService(taskId[taskIdCount].getServiceId());

										testNode.waitForTask(taskId[taskIdCount]);
									}
									catch (InterruptedException e)
									{
									}
									catch (ServiceNotFound e)
									{
										_logger.error(" ERROR - Service used is not found", e);
										_failed = true;
									}
									catch (NoSuchTaskId e)
									{
									}
								}
							}

                            if ( _logger.isDebugEnabled() )
                            {
							    _logger.debug("Finished waiting for task '"+runtimeTaskId+"'");
                            }
							break;

						case Action.TERMINATE_TASK :
							runtimeTaskId = action.getAssociatedRuntimeTaskId();

                            if ( _logger.isInfoEnabled() )
                            {
							    _logger.info("Terminating task '"+runtimeTaskId+"'");
                            }

							TaskIdInterface[] taskIds = (TaskIdInterface[])_runtimeTasks.get(runtimeTaskId);

							if (taskIds==null)
							{
								_logger.error("No such task id '"+runtimeTaskId+"' error in the test definition file");
								Coordinator.getLoggingService().logTestInformation(_element.getTest().getFullId(), _runId, _element.getPermutation().toString(),"The test definition XML for this test is incorrect - test execution halted\n");
								_failed = true;
							}
							else
							{
                                if ( _logger.isDebugEnabled() )
                                {
								    _logger.debug("There are "+taskIds.length+" runtime task(s) associated with this task");
                                }

								for (int taskIdCount=0;taskIdCount<taskIds.length;taskIdCount++)
								{
									try
									{
										if (taskIds[taskIdCount] != null)
										{
											TestNodeInterface testNode = Coordinator.getServiceRegistry().lookupService(taskIds[taskIdCount].getServiceId());
											testNode.terminateTask(taskIds[taskIdCount], _element.getPermutation().toString());
										}
										else
										{
											_logger.error("Task id number "+taskIdCount+" associated with this task is NULL");
										}
									}
									catch (ServiceNotFound e)
									{
										_logger.error("Service not found", e);
										_failed = true;
									}
									catch (NoSuchTaskId e)
									{
										_logger.warn("Test node reported task id. number "+taskIdCount+" does not exist");
									}
								}
							}
							break;

					}
				}
				catch (NoAssociatedData e)
				{
					_logger.error("Internal action parsing error", e);

					try
					{
						Coordinator.getLoggingService().logTestInformation(_element.getTest().getFullId(), _runId, _element.getPermutation().toString(),"Error in action associated with this test - test execution halted\n");
					}
					catch (Exception ex)
					{
						_logger.error("Failed to log test information", e);
					}

					_failed = true;
				}
				catch (TaskNotFound e)
				{
					_logger.error("Task Id mismatch in test definition for test '"+_element.getTest().getId()+"'", e);
					try
					{
						Coordinator.getLoggingService().logTestInformation(_element.getTest().getFullId(), _runId, _element.getPermutation().toString(),"Task Id mismatch in test definition for this test ("+e+") - test execution halted\n");
					}
					catch (Exception ex)
					{
						_logger.error("Failed to log test information (task id mismatch)", ex);
					}

					_failed = true;
				}
				catch (Exception e)
				{
					_logger.error("Unexpected exception", e);
					_failed = true;
				}

				count++;
			}
		}

		/** Free up the used test nodes **/
		if ( ( _failed ) || ( _testStopped ) || ( !freeUsedNodes() ) )
		{
			killAllOutstandingTasks();
		}

		try
		{
			Coordinator.getLoggingService().testComplete(_element.getTest().getFullId(), _runId, _element.getPermutation().toString());
		}
		catch (Exception e)
		{
			_logger.error("Failed to log test completion", e);
			_failed = true;
		}

		/** Release node ticket **/
		_releaseListener.releaseNodeTicket(_nodeTicket, _element.getPermutation().getPermutationElements());

		/** Report whether the test framework ran the test successfully **/
		_resultListener.testResult(_element, _failed);

        if ( _logger.isInfoEnabled() )
        {
		    _logger.info("Test manager complete");
        }
	}

	private boolean killAllOutstandingTasks()
	{
		boolean failed = false;

        if ( _logger.isInfoEnabled() )
        {
		    _logger.info("Killing all tasks on used test nodes due to failure");
        }

		Enumeration nodeEnum = _usedNodes.keys();

		while (nodeEnum.hasMoreElements())
		{
			TestNodeTicket ticket = (TestNodeTicket)nodeEnum.nextElement();
			TestNodeInterface tni = ticket.getNode();

            failed = false;
			boolean finished = false;
			int 	backOffPeriod = INITIAL_BACKOFF_PERIOD;
			int		failureCount = 0;

			while ( (!finished) && (!failed) )
			{
				try
				{
					tni.terminateAllTasks();
                    tni.testFinished(_element.getPermutation().toString(),(TaskIdInterface)_usedNodes.get(ticket), _element.getTest().getId());

					finished = true;
				}
                catch (TasksStillRunning e)
                {
                    if ( _logger.isDebugEnabled() )
                    {
                        _logger.debug("Tasks still running - trying again...");
                    }
                    finished = false;
                }
				catch (java.rmi.RemoteException e)
				{
					_logger.error("An unexpected RemoteException occurred", e);

					/** Remove this nodelist so that it isn't used in the future **/
					if ( _nodeManager.removeNode(ticket) )
					{
                        if ( _logger.isDebugEnabled() )
                        {
						    _logger.debug("Removed node from node list");
                        }
					}
					else
					{
						_logger.error("Cannot find node in node list, this shouldn't happen");
					}

					failed = true;
				}

				if ( !finished )
				{
					try
					{
						if (++failureCount>MAX_NUM_RETRIES)
						{
							_logger.error("Max retries reached");
							failed = true;
							finished = true;
						}
						else
						{
							Thread.sleep(backOffPeriod);
							backOffPeriod *= BACK_OFF_INCREMENT;
						}
					}
					catch (InterruptedException e)
					{
					}
				}
			}

			if ( failed )
			{
				/** Remove this nodelist so that it isn't used in the future **/
				if ( _nodeManager.removeNode(ticket) )
				{
					if ( _logger.isDebugEnabled() )
					{
						_logger.debug("Removed node from node list");
					}
				}
			}
		}

        if ( _logger.isInfoEnabled() )
        {
		    _logger.info("Killing all tasks on used test nodes due to failure completed ("+(failed ? "failed" : "successful")+")");
        }

		return failed;
	}

	private boolean freeUsedNodes()
	{
		boolean failed = false;

        if ( _logger.isDebugEnabled() )
        {
		    _logger.debug("Freeing up used test nodes");
        }

		Enumeration nodeEnum = _usedNodes.keys();

		while (nodeEnum.hasMoreElements())
		{
			TestNodeTicket ticket = (TestNodeTicket)nodeEnum.nextElement();
			TestNodeInterface tni = ticket.getNode();

			boolean finished = false;
			int 	backOffPeriod = INITIAL_BACKOFF_PERIOD;
			int		failureCount = 0;

			while (!finished)
			{
				try
				{
					tni.testFinished(_element.getPermutation().toString(),(TaskIdInterface)_usedNodes.get(ticket), _element.getTest().getId());

					finished = true;
				}
				catch (java.rmi.RemoteException e)
				{
					_logger.error("Unexpected RemoteException", e);

					/** Remove this nodelist so that it isn't used in the future **/
					if ( _nodeManager.removeNode(ticket) )
					{
                        if ( _logger.isDebugEnabled() )
                        {
						    _logger.debug("Removed node from node list");
                        }
					}
					else
					{
						_logger.error("Cannot find node in node list, this shouldn't happen");
					}
				}
				catch (TasksStillRunning tasksStillRunning)
				{
					_logger.warn("Tasks still running, trying again...");
				}

				if ( !finished )
				{
					try
					{
						if (++failureCount>MAX_NUM_RETRIES)
						{
							_logger.error("Max retries reached");
							failed = true;
							finished = true;
						}
						else
						{
							Thread.sleep(backOffPeriod);
							backOffPeriod *= BACK_OFF_INCREMENT;
						}
					}
					catch (InterruptedException e)
					{
					}
				}
			}
		}

        if ( _logger.isInfoEnabled() )
        {
		    _logger.info("Freeing up used test nodes attemped completed ("+ (failed ? "failed" : "successfully")+ ")");
        }

		return !failed;
	}

	public final String getRuntimeId(TaskIdInterface taskId)
	{
		return((String)_taskToRuntimeMap.get(taskId));
	}

	private boolean runTaskWaitToComplete(  String taskName,
											String runtimeId,
	                                        String productName,
	                                        TaskDefinition task,
	                                        TestDefinition test,
	                                        OSProductCombination taskPermutationCode,
	                                        String testPermutationCode,
	                                        String[] parameters,
	                                        String[] jvmParameters,
	                                        String uniquePrefix,
	                                        ArrayList nodes)
	{
		TaskIdInterface newTaskId = null;
		int 			backOffPeriod = INITIAL_BACKOFF_PERIOD;
		int				failureCount = 0;
		TaskIdInterface taskId = null;
		boolean 		failed = false;
		boolean			complete = false;
		int				result = -1;
		TaskIdInterface taskIds[] = new TaskIdInterface[nodes.size()];
        TestNodeTicket 	ticket = null;

        /** Ensure we don't alter the original object **/
        taskPermutationCode = new OSProductCombination(taskPermutationCode);

        StringPreprocessor pre = new StringPreprocessor();
        for (int nameCount=0;nameCount<_uniqueNames.size();nameCount++)
        {
            if ( _logger.isDebugEnabled() )
            {
                _logger.debug( "Unique name #"+(nameCount+1)+" = "+(String)_uniqueNames.get(nameCount) );
            }

            pre.addReplacement( ""+(nameCount+1), (String)_uniqueNames.get(nameCount) );
        }

        /*
         * If there are parameters to be passed from the test definition do so
         */
        if (parameters!=null)
        {
            parameters = pre.preprocessParameters(parameters, false);

            parameters = ParameterPreprocessor.preprocessParameters(parameters, false);
        }

        /*
         * If there are parameters to be passed from the test definition do so
         */
        if (jvmParameters!=null)
        {
            jvmParameters = pre.preprocessParameters(jvmParameters, false);

            jvmParameters = ParameterPreprocessor.preprocessParameters(jvmParameters, false);
        }

        /*
         * Get the test level runner parameters
         */
        Hashtable runnerParameters = test.getParametersForRunner(task.getRunner());

        /*
         * If there are task level runner parameters override the test level parameters
         */
        if (task.getRunnerParameters() != null)
        {
            runnerParameters = task.getRunnerParameters();
        }

		while ( (!failed) && (!complete) )
		{
			try
			{
				try
				{
					for (int count=0;count<nodes.size();count++)
					{
                        try
                        {
                            String taskIdSuffix = nodes.size() > 1 ? "("+count+")" : "";

                            ticket = (TestNodeTicket)nodes.get(count);
                            TestNodeInterface node = ticket.getNode();

                            /** If this task is being started on multiple nodes **/
                            if ( nodes.size() > 1 )
                            {
                                /** Ensure OS Id in task permutation code matches that of the node **/
                                taskPermutationCode.setOSId(ticket.getNodeDescription().getOSID());
                            }

                            if (_logger.isInfoEnabled() )
                            {
                                _logger.info("Attempting to run test on '"+node.getName()+"' on '"+node.getHostAddress()+"'");
                            }

                            if (!_usedNodes.containsKey(ticket))
                            {
                                taskId = node.generateTaskId( taskName );
                                _usedNodes.put(ticket, taskId);
                            }
                            else
                            {
                                taskId = (TaskIdInterface)_usedNodes.get(ticket);
                            }

                            node.initiateTest(test.getFullId(), taskId);

                            if ( _logger.isInfoEnabled() )
                            {
                                _logger.info("Performing task '"+task.getId()+"' on node '"+node.getName()+"' running on '"+node.getHostAddress()+"'");
                            }

                            _auditLogEntry.addNodeLogEntry( AuditLogEntry.createNodeEntry(node.getName(), task.getId(), taskPermutationCode.toString()) );

                            if (task.getType() == TaskDefinition.EXPECT_READY)
                                result = node.performTask( task.getRunner(), runnerParameters, task.getClassName(), task.getClasspath(), task.generateUniqueId(runtimeId) + taskIdSuffix, parameters, jvmParameters, productName, task.getTimeout(), taskId, _runId, taskPermutationCode.toString(), testPermutationCode);
                            else
                                result = node.performTask( task.getRunner(), runnerParameters, task.getClassName(), task.getClasspath(), task.generateUniqueId(runtimeId) + taskIdSuffix, parameters, jvmParameters, productName, task.getTimeout(), taskId, _runId, taskPermutationCode.toString(), testPermutationCode);

                            if ( _logger.isDebugEnabled() )
                            {
                                _logger.debug("Returned from node invocation");
                            }

                            newTaskId = taskId.incrementTaskId();

                            _usedNodes.put(ticket, newTaskId);

                            taskIds[count] = taskId;

                            // If the task timedout
                            if (result == node.RESULT_TIMEOUT)
                            {
                                _logger.warn("The task timed out");

                                try
                                {
                                    Coordinator.getLoggingService().logTestInformation(test.getFullId(), _runId, testPermutationCode.toString(),"The task '"+task.getId()+"' has timed out - execution halted\n");
                                    Coordinator.getLoggingService().logTimeout(test.getFullId(), _runId, testPermutationCode.toString());
                                }
                                catch (Exception ex)
                                {
                                    _logger.error("ERROR - Failed to log test information (timeout)");
                                }

                                node.terminateTask( taskId, testPermutationCode );
                            }
                            else
                            {
                                if ( _logger.isDebugEnabled() )
                                {
                                    _logger.debug("Waiting for task to complete");
                                }

                                // Wait for this task to complete
                                node.waitForTask(taskId);

                                if ( _logger.isDebugEnabled() )
                                {
                                    _logger.debug("Task completed");
                                }
                            }
                        }
                        catch (InterruptedException e)
                        {
                            _logger.warn("InterruptedException thrown - ignoring");
                        }
                        catch (NoSuchTaskId e)
                        {
                            _logger.warn("NoSuchTaskId thrown - task may have completed quickly");
                        }
					}

					complete = true;
				}
				catch (UnsupportedProduct e)
				{
                    _logger.error("Unsupported product reported by TestNode", e);
					throw e;
				}
			}
			catch (UnsupportedProduct e)
			{
				_logger.error("Unsupported product reported by TestNode", e);
				failed = true;
			}
			catch (TaskRunnerNotSupported e)
			{
				_logger.error("The TestNode doesn't support this task runner", e);
				failed = true;
			}
			catch (TestNodeBusy e)
			{
				_logger.error("Testnode reporting busy: "+e);
			}
			catch (java.rmi.RemoteException e)
			{
				_logger.error("Unexpected RemoteException", e);

				/** Remove this nodelist so that it isn't used in the future **/
				if ( _nodeManager.removeNode(ticket) )
				{
                    if ( _logger.isInfoEnabled() )
                    {
					    _logger.info("Removed node from node list");
                    }
				}
				else
				{
					_logger.error("Cannot find node in node list, this shouldn't happen");
				}

				/** Remove it from local list **/
				nodes.remove(ticket);
			}

			if (!complete)
			{
				try
				{
					if (++failureCount>MAX_NUM_RETRIES)
					{
						_logger.error("ERROR - Max retries reached");
						failed = true;
					}
					else
					{
						Thread.sleep(backOffPeriod);
						backOffPeriod *= BACK_OFF_INCREMENT;
					}
				}
				catch (InterruptedException e)
				{
				}
			}
		}

		if ( runtimeId != null)
		{
			/**
			 * Add taskids to runtimeTasks map
			 */
			_runtimeTasks.put(runtimeId, taskIds);
		}

		return(failed);
	}

	public final TaskIdInterface[] runTask(   String runtimeId,
	                                          String productName,
	                                          TaskDefinition task,
	                                          TestDefinition test,
	                                          OSProductCombination taskPermutationCode,
	                                          String testPermutationCode,
	                                          String nameList,
	                                          String[] parameters,
	                                          String[] jvmParameters,
	                                          String uniquePrefix,
	                                          String location,
	                                          ArrayList nodes) throws RunTaskFailure
	{
		boolean 			busy;
		boolean				failed = false;
		int					failureCount = 0;
		int 				nodeCount = 0;
		TaskIdInterface[]	taskId = new TaskIdInterface[nodes.size()];
		int					backOffPeriod = INITIAL_BACKOFF_PERIOD;
		HashSet				usedNodes = new HashSet();

        /** Ensure task permutation code isn't changed **/
        taskPermutationCode = new OSProductCombination(taskPermutationCode);

		while ( ( !failed ) && ( nodeCount < nodes.size() ) )
		{
			busy = false;

            String taskIdSuffix = nodes.size() > 1 ? "("+nodeCount+")" : "";
			TestNodeTicket ticket = (TestNodeTicket)nodes.get(nodeCount);
			TestNodeInterface node = ticket.getNode();

			try
			{
				if (!usedNodes.contains(ticket))
				{
                    /** If this task is being started on multiple nodes **/
                    if ( nodes.size() > 1 )
                    {
                        /** Ensure OS Id in task permutation code matches that of the node **/
                        taskPermutationCode.setOSId(ticket.getNodeDescription().getOSID());
                    }

	                taskId[nodeCount] = runTaskOnGivenTestNode(ticket,
	                                                           productName,
	                                                           test,
	                                                           task,
	                                                           taskPermutationCode.toString(),
	                                                           testPermutationCode,
	                                                           parameters,
	                                                           jvmParameters,
	                                                           uniquePrefix,
                                                               taskIdSuffix,
	                                                           runtimeId);

					usedNodes.add(ticket);

                    if ( _logger.isInfoEnabled() )
                    {
					    _logger.info("Task started on node "+node.getName());
                    }
				}
				else
				{
                    taskId[nodeCount] = (TaskIdInterface)_usedNodes.get(ticket);

                    if ( _logger.isInfoEnabled() )
                    {
					    _logger.info("Task already started on this TestNode skipping");
                    }
				}
		        nodeCount++;
			}
			catch (UnsupportedProduct e)
			{
				_logger.error("Unsupported Product reported by testnode", e);
				failed = true;
			}
			catch (TaskRunnerNotSupported e)
			{
				_logger.error("The TestNode doesn't support this task runner", e);
				failed = true;
			}
			catch (TestNodeBusy e)
			{
				_logger.error("Test Node is reporting busy", e);
        		busy = true;
			}
			catch (java.rmi.RemoteException e)
			{
				_logger.error("Unexpected RemoteException", e);

				/** Remove this nodelist so that it isn't used in the future **/
				if ( _nodeManager.removeNode(ticket) )
				{
                    if ( _logger.isInfoEnabled() )
                    {
					    _logger.info("Removed node from node list");
                    }
				}
				else
				{
					_logger.error("Cannot find node in node list, this shouldn't happen");
				}

				/** Remove it from local list **/
				nodes.remove(nodeCount);

				failed = true;
			}

			/*
			 * If we have been through the list of all TestNodes without the
			 * task being run then back off and try again later
			 */
			if ( busy )
			{
				try
				{
					if (++failureCount>MAX_NUM_RETRIES)
					{
						_logger.error("Max retries reached");
						failed = true;
					}
					else
					{
						Thread.sleep(backOffPeriod);
						backOffPeriod *= BACK_OFF_INCREMENT;
					}
				}
				catch (InterruptedException e)
				{
				}
			}
		}

		if (failed)
		{
			throw new RunTaskFailure(test.getFullId());
		}

		return(taskId);
	}

	private TaskIdInterface runTaskOnGivenTestNode( TestNodeTicket ticket,
													String productName,
													TestDefinition test,
													TaskDefinition task,
													String taskPermutationCode,
													String testPermutationCode,
													String[] parameters,
													String[] jvmParameters,
													String uniquePrefix,
                                                    String taskIdSuffix,
													String runtimeId)
						throws UnsupportedProduct, TaskRunnerNotSupported, TestNodeBusy, java.rmi.RemoteException
	{
		TaskIdInterface taskId;

		if (!_usedNodes.containsKey(ticket))
		{
            if ( _logger.isInfoEnabled() )
            {
			    _logger.info("Generating task id");
            }

			taskId = ticket.getNode().generateTaskId( runtimeId );
			_usedNodes.put(ticket, taskId);
		}
		else
		{
			taskId = (TaskIdInterface)_usedNodes.get(ticket);
		}

        if (_logger.isInfoEnabled() )
        {
		    _logger.info("Initiating test "+test.getFullId());
        }

		ticket.getNode().initiateTest(test.getFullId(), taskId);

		StringPreprocessor pre = new StringPreprocessor();
		for (int count=0;count<_uniqueNames.size();count++)
		{
            if ( _logger.isDebugEnabled() )
            {
			    _logger.debug( "Unique name #"+(count+1)+" = "+(String)_uniqueNames.get(count) );
            }

			pre.addReplacement( ""+(count+1), (String)_uniqueNames.get(count) );
		}

		if ( parameters != null )
		{
			parameters = pre.preprocessParameters(parameters, false);

			parameters = ParameterPreprocessor.preprocessParameters(parameters, false);
		}

        if ( jvmParameters != null )
        {
            jvmParameters = pre.preprocessParameters(jvmParameters, false);

            jvmParameters = ParameterPreprocessor.preprocessParameters(jvmParameters, false);
        }

		/*
		 * Generate the parameters to pass to the task
		 */
		ArrayList params = new ArrayList();

		/*
		 * If there are parameters to be passed from the test definition do so
		 */
		if (parameters!=null)
		{
			for (int paramCount=0;paramCount<parameters.length;paramCount++)
				params.add(parameters[paramCount]);
		}

		String[] stringParams = new String[params.size()];
		System.arraycopy(params.toArray(),0,stringParams,0,params.size());

		/*
		 * Get the test level runner parameters
		 */
		Hashtable runnerParameters = test.getParametersForRunner(task.getRunner());

		/*
		 * If there are task level runner parameters override the test level parameters
		 */
		if (task.getRunnerParameters() != null)
		{
			runnerParameters = task.getRunnerParameters();
		}

		TestNodeInterface node = ticket.getNode();

		_auditLogEntry.addNodeLogEntry( AuditLogEntry.createNodeEntry(node.getName(), task.getId(), taskPermutationCode) );

		node.runTask(	task.getRunner(),
						runnerParameters,
						task.getClassName(),
						task.getClasspath(),
						task.generateUniqueId(runtimeId) + taskIdSuffix,
						stringParams,
						jvmParameters,
						productName,
						(task.getType() == TaskDefinition.EXPECT_READY) ? TestNodeInterface.WAIT_READY : TestNodeInterface.WAIT_NONE,
						task.getTimeout(),
						new TaskDataHandler(this,test,task,taskPermutationCode),
						taskId,
						_runId,
						taskPermutationCode,
						testPermutationCode);

		TaskIdInterface newTaskId = taskId.incrementTaskId();

		_usedNodes.put(ticket,newTaskId);

		return(taskId);
	}
}
