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
// $Id: TaskRunner.java 170 2008-03-25 18:59:26Z jhalliday $

package org.jboss.dtf.testframework.testnode;

import org.jboss.dtf.testframework.testnode.*;
import org.jboss.dtf.testframework.utils.*;
import org.jboss.dtf.testframework.utils.logging.LoggingFactory;
import org.jboss.dtf.testframework.utils.logging.LoggingService;
import org.jboss.dtf.testframework.utils.logging.exception.LoggingServiceException;
import org.jboss.dtf.testframework.productrepository.NodeConfiguration;
import org.jboss.dtf.testframework.productrepository.ProductConfiguration;

import java.util.Hashtable;
import java.util.ArrayList;
import java.rmi.RemoteException;
import java.io.File;

public abstract class TaskRunner implements Runnable
{
	public final static String JAVA_HOME_DIRECTORY_PARAMETER = "java_home";
    private final static String JAVA_TASK_RUNNER_DEBUG_PROPERTY = "com.arjuna.mw.testframework.javataskrunner.debug";
    protected static boolean        DebugEnabled = Boolean.valueOf( System.getProperty(JAVA_TASK_RUNNER_DEBUG_PROPERTY, "false") ).booleanValue();

    protected String  				_className;
	protected String[] 				_parameters;
	protected String[]				_jvmParameters;
	protected int    				_testType = TestNodeInterface.WAIT_NONE,
				    				_timeoutValue;
	protected NodeConfiguration 	_nodeConfig;
	protected ProductConfiguration  _productConfig;
	protected TaskResultListener	_listener = null;
	protected TaskIdInterface		_taskId = null;
	protected TaskResultListener  	_resultListener = null;
	protected TestNodeInterface		_associatedTestNode = null;
	protected String				_associatedTestNodeName;
	protected Hashtable				_runnerParameters = null;
	private Thread					_thrd = null;

	protected boolean				_finished = false;
    protected Object                _finishedSyncObject = new Object();
	protected boolean				_ready = false;
	protected RunUID				_runId = null;
	protected String				_loggingResource = null;
	protected String				_taskPermutationCode = null;
	protected String				_testPermutationCode = null;
	protected String				_testId = null;
    protected String                _taskName = null;

    protected boolean               _timedOut = false;
    protected boolean               _running = false;
    protected Object                _taskIsRunning = new Object();

	protected ServiceUtils			_serviceUtils = null;
	protected LoggingService		_loggingService = null;
	protected String 				_classpathRef = null;

	/**
	 * Called by the framework to initialise this task runner.
	 *
	 * @param runnerParameters
	 * @param testId
	 * @param className
	 * @param taskName
	 * @param parameters
	 * @param jvmParameters
	 * @param testType
	 * @param timeoutValue
	 * @param productConfig
	 * @param nodeConfig
	 * @param taskId
	 * @param listener
	 * @param associatedTestNode
	 * @param runId
	 * @param taskPermutationCode
	 * @param testPermutationCode
	 * @param loggingResource
	 * @param serviceUtils
	 * @throws java.rmi.RemoteException
	 */
	public final void initialise(	Hashtable			runnerParameters,
									String				testId,
									String 				className,
									String				classpathRef,
                                    String              taskName,
					  				String[]			parameters,
					  				String[]			jvmParameters,
					  				int	 				testType,
									int	 				timeoutValue,
									ProductConfiguration productConfig,
									NodeConfiguration nodeConfig,
									TaskIdInterface		taskId,
									TaskResultListener	listener,
									TestNodeInterface	associatedTestNode,
									RunUID				runId,
									String				taskPermutationCode,
									String				testPermutationCode,
									String				loggingResource,
									ServiceUtils		serviceUtils) throws java.rmi.RemoteException
	{
		_testId = testId;
        _taskName = taskName;
		_taskPermutationCode = taskPermutationCode;
		_testPermutationCode = testPermutationCode;
		_className = className;
		_serviceUtils = serviceUtils;
		_parameters = parameters;
		_jvmParameters = jvmParameters;
		_testType = testType;
		_timeoutValue = timeoutValue;
		_productConfig = productConfig;
		_nodeConfig = nodeConfig;
		_listener = listener;
		_taskId = taskId;
		_associatedTestNode = associatedTestNode;
		_associatedTestNodeName = _associatedTestNode.getName();
		_runId = runId;
		_loggingResource = loggingResource;
		_runnerParameters = runnerParameters;
		_classpathRef = classpathRef;

		try
		{
			_loggingService = LoggingFactory.getDefaultLogger(_loggingResource);
		}
		catch (LoggingServiceException e)
		{
			throw new RemoteException("Failed to retrieve logger: "+e);
		}

	}

	/**
	 * Retrieves a table of name value parameters passed to this task runner
	 * by the currently executing test.
	 *
	 * @return A hashtable containing the name value pairs.
	 */
    public final Hashtable getRunnerParameters()
    {
        return(_runnerParameters);
    }

	/**
	 * Registers a task result listener for this task runner.  Only one can
	 * registered per task runner and only the last one is stored.  This listener
	 * will be invoked when data is generated by the task being executed.
	 *
	 * @param listener A reference to the task result listener.
	 */
	public final void registerResultListener(TaskResultListener listener)
	{
		_resultListener = listener;
	}

	/**
	 * Retrieves the logging service to be used by the task runner to log
	 * results.
	 *
	 * @return A reference to the logging service to use to log results.
	 */
	public LoggingService getLoggingService()
	{
		return _loggingService;
	}

	/**
	 * Get the name of the task being run by this task runner.
	 *
	 * @return The name of the task being executed.
	 */
    public final String getTaskName()
    {
        return(_taskName);
    }

	/**
	 * Retrieve the task id. given to this task by the parent test node.
	 *
	 * @return The task id. of the task being run.
	 */
    public final TaskIdInterface getRunningTaskId()
    {
        return(_taskId);
    }

	/**
	 * Retrieve the permutation code of the test currently being run.
	 *
	 * @return The permutation code of the test currently being run.
	 */
    public final String getRunningTestPermutationCode()
    {
        return(_testPermutationCode);
    }

	/**
	 * Retrieve the permutation code of the task being run by this
	 * task runner.
	 *
	 * @return The permutation of the task this task runner is to run.
	 */
    public final String getRunningTaskPermutationCode()
    {
        return(_taskPermutationCode);
    }

	/**
	 * Retrieve a reference to the task result listener for this task runner.
	 *
	 * @return The task result listener for this task runner.
	 */
    public final TaskResultListener getResultListener()
    {
        return(_resultListener);
    }

	/**
	 * Returns true if the task this task runner is running has timedout.
	 *
	 * @return True - if the task has timedout.
	 */
    public boolean hasTimedOut()
    {
        return(_timedOut);
    }

	public String toString()
	{
		try
		{
			return(_className+" "+_taskId.dumpInfo());
		}
		catch (Exception e)
		{
			return(null);
		}
	}

	/**
	 * This method is called by a task runner when the task it is running has declared that
	 * it is ready to be used.  This signal is only acted upon if the task is run as a server.
	 *
	 * @throws java.rmi.RemoteException
	 */
	protected final synchronized void readySignalled() throws java.rmi.RemoteException
	{
        System.out.println("#### Result listener: "+_resultListener);
		if (_resultListener!=null)
		{
			_resultListener.taskSignalledReady(_taskId,_associatedTestNode,_testPermutationCode);
		}
		_ready = true;
		notifyAll();
	}

	public final void start()
	{
		if (_thrd==null)
		{
            try
            {
                synchronized(_taskIsRunning)
                {
                    _running = false;

                    /**
                     * Start the thread
                     */
                    _thrd = new Thread(this);
                    _thrd.start();

                    if ( !_running )
                    {
                        System.out.println("#### WAITING FOR TASK TO START ####");
                        _taskIsRunning.wait();
                        System.out.println("#### TASK HAS STARTED ####");
                    }
                }
            }
            catch (InterruptedException e)
            {
            }
		}
	}

	/**
	 * Called by the framework to wait for the task to signal Ready or finish.
	 *
	 * @return true if neither happened within the timeout period
	 */
	public final synchronized boolean waitForReadyOrFinished() throws InterruptedException
	{
		System.out.println("Waiting "+_timeoutValue+" seconds");
		wait(_timeoutValue*1000); // Convert timeout value in to milliseconds
        _timedOut = ((!_ready) && (!_finished));
		System.out.println("Finished waiting _ready="+_ready+" and _finished="+_finished);
		return( (!_ready) && (!_finished) );
	}

	/**
	 * Called by the framework to wait for the task ro signal that it is finished.
	 *
	 * @return True if the task did not finish.
	 * @throws InterruptedException
	 */
    public final boolean waitForFinished() throws InterruptedException
    {
        synchronized(_finishedSyncObject)
        {
            _finishedSyncObject.wait(_timeoutValue*1000);
        }

        return(!_finished);
    }

	/**
	 * Returns true if the task has finished.
	 *
	 * @return True - if the task has finished.
	 */
    public final boolean hasFinished()
    {
        return(_finished);
    }

	/**
	 * Returns true if this task has signalled that it is ready.
	 *
	 * @return True if the task has signalled ready.
	 *
	 * @throws Exception If this task is not expected to have a ready state.
	 */
    public final boolean isReady() throws Exception
    {
        if (_testType != TestNodeInterface.WAIT_READY)
            throw new Exception("This task does not expect ready");

        return(_ready);
    }

	public final void run()
	{
		try
		{
			runTask();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Retrieve the service utils used by the framework.  This
	 * contains methods to retrieve references to all of the
	 * services offered by the testing framework.
	 *
	 * @return A reference to the service utils.
	 */
	public ServiceUtils getServiceUtils()
	{
		return _serviceUtils;
	}

	/**
	 * Called by the task runner when it has detected that the
	 * task is running.
	 */
    public final void indicateTaskIsRunning()
    {
        System.out.println("#### Indicating that the task is now running...");
        synchronized(_taskIsRunning)
        {
            System.out.println("#### TASK IS RUNNING ####");
            _running = true;
            _taskIsRunning.notifyAll();
        }
    }

	public final NodeConfiguration getNodeConfiguration()
	{
		return _productConfig.getNodeConfiguration(TestNode.getOSId());
	}

	/**
	 * Get the classpath required to run tasks for the given product
	 *
	 * @return
	 */
	public String getClasspathString()
	{
		String classpath = "";

		/** Create preprocessor and add SETs as replacements **/
		StringPreprocessor preproc = new StringPreprocessor();
		preproc.addReplacements(_nodeConfig.getPreprocessedSets());
        preproc.addReplacement("JAVA_HOME", getJavaHome());
		/** Use the default classpath if none is specified **/
		String classpathName = (_classpathRef == null) ? _nodeConfig.getDefaultClasspath() : _classpathRef;

        ArrayList classpathElements = _productConfig.getClasspathList(classpathName);

		if ( classpathElements != null )
		{
			/**
			 * Search the classpath for wildcards
			 */
			for (int count=0;count<classpathElements.size();count++)
			{
				String classpathElement = (String)classpathElements.get(count);

				/** Preprocess element to remove $(VARS) **/
				classpathElement = preproc.preprocessParameters(classpathElement);

				/** If the element contains a wildcard **/
				if ( classpathElement.indexOf('*') != -1 )
				{
					classpath = addWildcardElements(classpath, classpathElement);
				}
				else
				{
					classpath += classpathElement + File.pathSeparatorChar;
				}
			}
		}
        else
		{
			System.out.println("Warning - classpath '"+classpathName+"' is not defined in product repository");
		}
		return classpath;
	}

	private String addWildcardElements(String originalClasspath, String wildcardElement)
	{
		File[] files = WildCardProcessor.processWildcard(wildcardElement);

		if ( files != null )
		{
			for (int count=0;count<files.length;count++)
			{
				originalClasspath += files[count].getAbsolutePath() + File.pathSeparatorChar;
			}
		}

        return originalClasspath;
	}

	/**
	 * This is the main body of the task runner.  Within this method
	 * the task runner can start the task and monitor its state.
	 *
	 * @throws Exception
	 */
	public abstract void runTask() throws Exception;

	/**
	 * Called by the framework when it wishes to terminate
	 * the task this runner is running.
	 *
	 * @return True if the task was successfully terminated.
	 */
	public abstract boolean terminate();

	/**
	 * Called by the framework when it wishes to wait for
	 * the task to finish.  This method <u>MUST</u> block
	 * until the task has finished. If a method is called
	 * which could throw an InterruptedException then let
	 * this be thrown by the method and it will be handled
	 * correctly by the test node.
	 *
	 * @throws InterruptedException
	 */
	public abstract void waitFor() throws InterruptedException;

    public final String getJavaHome()
    {
        File javaBinDir = new File((String)_runnerParameters.get(JAVA_HOME_DIRECTORY_PARAMETER));
        return javaBinDir.getAbsolutePath();
    }

	/**
	 * Retrieve the configured JAVA executable directory (JAVA_HOME/bin)
	 *
	 * @return
	 */
	public final String getJavaExe()
	{
		File javaBinDir = new File((String)_runnerParameters.get(JAVA_HOME_DIRECTORY_PARAMETER), "bin");
		return new File(javaBinDir, "java").getAbsolutePath();
	}
}
