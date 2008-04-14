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
// $Id: TestNode.java 170 2008-03-25 18:59:26Z jhalliday $

package org.jboss.dtf.testframework.testnode;

import org.jboss.dtf.testframework.coordinator.resultscollator.*;
import org.jboss.dtf.testframework.coordinator.UnsupportedProduct;
import org.jboss.dtf.testframework.testnode.*;
import org.jboss.dtf.testframework.serviceregister.*;
import org.jboss.dtf.testframework.utils.*;
import org.jboss.dtf.testframework.productrepository.ProductConfiguration;
import org.jboss.dtf.testframework.productrepository.ProductRepositoryInterface;
import org.jboss.dtf.testframework.productrepository.NodeConfiguration;
import org.jboss.dtf.testframework.productrepository.TaskRunnerConfiguration;

import org.jdom.input.*;
import org.jdom.*;

import java.io.*;
import java.util.*;
import java.rmi.server.*;
import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.net.URL;

public class TestNode extends UnicastRemoteObject implements TestNodeInterface, TaskResultListener, TimeoutListener
{
	private final static String TEST_NODE_NAME = "name";
	private final static String TEST_NODE_OS_ID = "os";

	private final static String TASK_RUNNER_DEFINITIONS_ELEMENT = "task-runner-definitions";
    private final static String TASK_RUNNER_ELEMENT = "task-runner";
    private final static String TASK_RUNNER_NAME_ATTRIBUTE = "name";
    private final static String TASK_RUNNER_CLASS_ATTRIBUTE = "class";
    private final static String TASK_RUNNER_LOG_TO_ATTRIBUTE = "log-to";
    private final static String TASK_RUNNER_PARAM_ELEMENT = "param";
    private final static String TASK_RUNNER_PARAM_NAME_ATTRIBUTE = "name";
    private final static String TASK_RUNNER_PARAM_VALUE_ATTRIBUTE = "value";

    private final static String DEFAULT_CONFIG_FILENAME = "nodeconfig.xml";
    private final static String VERSION_TABLE_FILENAME = "product-versions.cfg";

	private final static String JVM_DEFINITIONS_ELEMENT = "jvm-definitions";
	private final static String JVM_ELEMENT = "jvm";
	private final static String VERSION_ATTRIBUTE = "version";
	private final static String JAVA_HOME_ATTRIBUTE = "java-home";
	private final static String DEFAULT_ATTRIBUTE = "default";

	private static final int DEFAULT_COLLATOR_PORT = 8001;

	private static String _osId = null;

	private String 				_nodeName = "";
	private TaskIdInterface 	_taskPerforming = null;
	private short				_currentTestId = 0;
	private Hashtable			_activeTaskList = new Hashtable();
    private Hashtable           _timeoutThreads = new Hashtable();
	private Hashtable			_resultListeners = new Hashtable();
	private BufferedWriter		_logFile = null;
    private String              _productCurrentlyInUse = null;
	private ResultsReporter		_reporter = null;
	private String				_currentTestName = "None";
    private RunUID              _currentRunId = null;
	private RegistrationDaemon 	_registrationDaemon = null;
    private Hashtable           _taskRunners = new Hashtable();
    private Hashtable           _productVersions = new Hashtable();
	private Hashtable			_jvms = new Hashtable();
	private String				_defaultJvmId = null;
    private boolean             _deployOnRelease = false;
    private Object              _deployInProgress = new Object();
	private Boolean				_shutdownOnComplete = null;
	private ServiceUtils		_serviceUtils = null;
    private ProductRepositoryInterface _productRepository;
    private TestNodeDescription _description = null;

	/**
	 * @param configFile
	 * @param nameServiceURI
	 * @throws java.rmi.RemoteException
	 */
	public TestNode(URL configFile, String nameServiceURI, String nodeName) throws java.rmi.RemoteException
	{
		super();

		_serviceUtils = new ServiceUtils(nameServiceURI);
		try
		{
			_productRepository = _serviceUtils.getProductRepository();
		}
		catch (ServiceNotFound e)
		{
			System.out.println("Could not find the product repository: "+e);
			e.printStackTrace();
			System.exit(1);
		}

		try
		{
			_logFile = new BufferedWriter(new FileWriter("testnode.log"));
			writeLog("# TestNode Log File");
		}
		catch (IOException e)
		{
			System.out.println("ERROR - Failed to create log file");
			System.exit(1);
		}

		parseConfig(configFile);

		_registrationDaemon = new RegistrationDaemon(_serviceUtils, this, _osId, createSupportedProductList());


		if ( nodeName != null )
		{
			_nodeName = nodeName;
		}

        deserializeVersionTable();
	}

    public void initialise(String collatorIp, int collatorPort)
    {
        try
        {
            updateSoftware();

			writeLog("Starting registration daemon..");
			_registrationDaemon.startDaemon();

			System.out.println("Test Node Id. "+_registrationDaemon.getServiceId()+" running on "+_osId);

			// Connect to the results collator
			_reporter = new CollatorReporter(collatorIp, collatorPort, _registrationDaemon.getServiceId());
        }
        catch (TestNodeBusy e)
        {
            // Ignore this, not possible at this stage
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.err.println("Failed to perform initial software update: "+e);
            System.exit(1);
        }
    }
	/**
	 * Parse the node configuration file.
	 *
	 * @param configFile A url to the node configuration file.
	 */
    private void parseConfig(URL configFile)
    {
        try
        {
            SAXBuilder xmlBuilder = new SAXBuilder();
            Document doc = xmlBuilder.build(configFile);

            /*
             * Retrieve root element, then retrieve the test node configuration element
             */
            Element root = doc.getRootElement();
            Element testNodeRootElement = root;

			_nodeName = testNodeRootElement.getAttributeValue(TEST_NODE_NAME);
			_osId = testNodeRootElement.getAttributeValue(TEST_NODE_OS_ID);

			if ( _osId == null )
			{
				System.err.println("No operating system id. specified - this is required");
				System.exit(1);
			}

			Element taskRunnerDefinitions = root.getChild(TASK_RUNNER_DEFINITIONS_ELEMENT);
            List taskRunnerElements = taskRunnerDefinitions .getChildren(TASK_RUNNER_ELEMENT);

            for (int taskRunnerCount = 0; taskRunnerCount < taskRunnerElements.size(); taskRunnerCount++)
            {
                Element taskRunnerElement = (Element) taskRunnerElements.get(taskRunnerCount);

                if (taskRunnerElement != null)
                {
					TaskRunnerConfiguration newTaskRunner = new TaskRunnerConfiguration();
                    String name = taskRunnerElement.getAttributeValue(TASK_RUNNER_NAME_ATTRIBUTE);
                    String taskRunnerClass = taskRunnerElement.getAttributeValue(TASK_RUNNER_CLASS_ATTRIBUTE);
                    String loggingResource = taskRunnerElement.getAttributeValue(TASK_RUNNER_LOG_TO_ATTRIBUTE);

					newTaskRunner.setClassname(taskRunnerClass);
					newTaskRunner.setLogTo(loggingResource);

                    List taskRunnerParameterList = taskRunnerElement.getChildren(TASK_RUNNER_PARAM_ELEMENT);
                    for (int count = 0; count < taskRunnerParameterList.size(); count++)
                    {
                        Element parameter = (Element) taskRunnerParameterList.get(count);

                        newTaskRunner.setParameter(parameter.getAttributeValue(TASK_RUNNER_PARAM_NAME_ATTRIBUTE),
                                parameter.getAttributeValue(TASK_RUNNER_PARAM_VALUE_ATTRIBUTE));
                    }

                    _taskRunners.put(name, newTaskRunner);
                }
            }

			/** Get jvm-definitions element **/
			Element jvmDefinitionsElement = root.getChild(JVM_DEFINITIONS_ELEMENT);

			_defaultJvmId = jvmDefinitionsElement.getAttributeValue(DEFAULT_ATTRIBUTE);

			if ( _defaultJvmId == null )
			{
				System.err.println("default jvm-definitions node configuration not specified!");
				System.exit(1);
			}

			/** Get the jvm elements **/
			List jvmDefinitionElements = jvmDefinitionsElement.getChildren(JVM_ELEMENT);

			for (int count=0;count<jvmDefinitionElements.size();count++)
			{
				Element jvmDefinitionElement = (Element)jvmDefinitionElements.get(count);

				_jvms.put( jvmDefinitionElement.getAttributeValue(VERSION_ATTRIBUTE), jvmDefinitionElement.getAttributeValue(JAVA_HOME_ATTRIBUTE) );
			}
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.exit(1);
        }
    }

	public boolean ping() throws java.rmi.RemoteException
	{
        System.out.println("ping");
		// Do nothing
		return(true);
	}

	private void writeLog(String logText)
	{
		try
		{
			_logFile.write(new java.util.Date().toString()+" - "+logText+"\n");
			_logFile.flush();
		}
		catch (IOException e)
		{
			System.out.println("ERROR - Failed to write log to log file");
			System.exit(1);
		}
	}

	/**
	 * This method retrieves an InputStream for the log output for a deployment.
	 * @param productName
	 * @param outOrErr True indicates the output stream and False indicates the error stream
	 * @return An input stream to the data
	 * @throws RemoteException
	 */
	public RemoteFileReaderInterface getDeployLogOutput(String productName, boolean outOrErr) throws java.io.IOException, RemoteException
	{
		return outOrErr ? ANTInstaller.getDeployLogOutputStream(productName) : ANTInstaller.getDeployLogErrorStream(productName);
	}

	/**
	 * Generate a list of the products supported by this TestNode
	 */
	private String[] createSupportedProductList() throws RemoteException
	{
        _description = new TestNodeDescription(_osId, _registrationDaemon);

		ArrayList supportedProductList = new ArrayList();
		String[] productNames = _productRepository.getProductNames();

		for (int count=0;count<productNames.length;count++)
		{
			ProductConfiguration productConfig = _productRepository.getProductConfiguration(productNames[count]);
            NodeConfiguration nodeConfig = productConfig.getNodeConfiguration(_osId);

			/** If the node is supported by this product and it is not excluded **/
			if ( ( nodeConfig != null ) && ( !nodeConfig.isNodeExcluded( getName() ) ) )
			{
				/** Add product to supported product list **/
				supportedProductList.add(productNames[count]);
    		}
		}

		String[] returnArray = new String[supportedProductList.size()];
		supportedProductList.toArray(returnArray);

		System.out.println("Supported product list "+supportedProductList);
		return returnArray;
	}

	public void initiateTest(String				currentTestId,
							 TaskIdInterface	taskId) throws java.rmi.RemoteException, TestNodeBusy
	{
		if ( (_taskPerforming!=null) && (!_taskPerforming.inSameTest(taskId)) )
		{
			if (_taskPerforming.getTestId() != TaskId.UNDEFINED_TEST_ID)
				throw new TestNodeBusy();
		}

        synchronized( _deployInProgress )
        {
		    _currentTestName = currentTestId;
		    _taskPerforming = taskId;
        }
	}

	public int performTask( String			taskType,
							Hashtable		runnerParameters,
							String 			className,
							String			classpathRef,
                            String          taskName,
							String[]		parameters,
							String[]		jvmParameters,
							String  		product,
							int				timeoutValue,
							TaskIdInterface	taskId,
							RunUID			runId,
							String			taskPermutationCode,
							String			testPermutationCode) throws java.rmi.RemoteException, UnsupportedProduct, TestNodeBusy, TaskRunnerNotSupported
	{
    	int result = RESULT_READY;
	    try
	    {
    		writeLog("Request to perform task received '"+className+"' using product: "+product);

    		System.out.println("**** "+runnerParameters);
    		if ( (_taskPerforming!=null) && (!_taskPerforming.inSameTest(taskId)) )
    		{
    			if (_taskPerforming.getTestId() != TaskId.UNDEFINED_TEST_ID)
    				throw new TestNodeBusy();
    		}

            _currentRunId = runId;
            _productCurrentlyInUse = product;
    		_taskPerforming = taskId;
    		System.out.println("Perform task '"+className+"' using product: "+product+".");

    		if (_productRepository.getProductConfiguration(product).supportsOs(_osId))
    		{
    			TaskRunner taskRunner = getTaskRunner(runnerParameters,
    													taskType,
    													_currentTestName,
    													className,
    													classpathRef,
    													taskName,
    													parameters,
    													jvmParameters,
    													TestNodeInterface.WAIT_NONE,
    													timeoutValue,
    													product,
    													taskId, // TaskIDInterface
    													this, // TestNode
    													runId, // RunUID
    													taskPermutationCode,
    													testPermutationCode);

    			System.out.println("Task "+className+" started give task id. "+taskId.dumpInfo()+" ("+taskId.getHashCode()+")");

				synchronized(_activeTaskList)
				{
    				_activeTaskList.put(taskId.getHashCode(), taskRunner);
				}

    			taskRunner.start();

    			// the TestNode starts the TaskRunner, and then uses a method of the TaskRunner
    			// to wait and check for timeout. (i.e. performTask() is synchronous)
    			try
    			{
    				if (!taskRunner.hasFinished() && taskRunner.waitForReadyOrFinished())
    				{
						System.out.println("**** TASK TIMEDOUT ****");
    					result = RESULT_TIMEOUT;
    				}
    			}
    			catch (InterruptedException e)
    			{
    			}
    		}
    		else
    			throw new UnsupportedProduct(product);

    		dumpActiveTaskList();
    	}
		catch (TaskRunnerNotSupported e)
		{
			throw e;
		}
    	catch (Exception e)
    	{
    	    System.out.println("ERROR - "+e.toString());
    	    e.printStackTrace();
    	}
    	return(result);
	}

	public void taskSignalledReady( TaskIdInterface	taskId ) throws java.rmi.RemoteException
	{
		TaskRunner taskRunner;

		synchronized(_activeTaskList)
		{
        	taskRunner = (TaskRunner)_activeTaskList.get(taskId.getHashCode());
		}

        synchronized(taskRunner)
        {
            System.out.println("#### Task '"+taskId.dumpInfo()+"' has signalled ready");
            taskRunner.notifyAll();
        }
        /*
         * Check to see if test had result listener registered
         */
        TaskResultListener listener = (TaskResultListener)_resultListeners.get(taskId.getHashCode());

        if (listener!=null)
        {
            listener.taskSignalledReady( taskId, this, taskRunner.getRunningTestPermutationCode() );
        }
	}

	public void taskFinished( TaskIdInterface taskId, TestNodeInterface testNode, String testPermutation, boolean taskStartedSuccessfully ) throws java.rmi.RemoteException, NoSuchTaskId
	{
		writeLog("Task has finished "+taskId.getHashCode());
		System.out.println("Task has finished "+taskId.getHashCode()+" success flag :"+taskStartedSuccessfully);
		TaskRunner runner = null;

		synchronized(_activeTaskList)
		{
			if (!_activeTaskList.containsKey(taskId.getHashCode()))
				throw new NoSuchTaskId(taskId.dumpInfo());

        	runner = (TaskRunner)_activeTaskList.get(taskId.getHashCode());
			_activeTaskList.remove(taskId.getHashCode());

            StartTaskRunnerWrapper timeoutThread;
            /** If there is a timeout thread for this task interrupt it **/
            if ( ( timeoutThread = (StartTaskRunnerWrapper)_timeoutThreads.get(runner) ) != null )
            {
                timeoutThread.interrupt();
                _timeoutThreads.remove(runner);
            }
		}

		System.out.println("Adding test '"+_currentTestName+"' permutation code '"+testPermutation+"' to results list");
		_reporter.addResults(_currentRunId, testPermutation, taskId, "results/"+taskId.getTestId()+"/", _currentTestName, runner.getTaskName());


		/*
		 * Check to see if test had result listener registered
		 */
		TaskResultListener listener = (TaskResultListener)_resultListeners.get(taskId.getHashCode());

		if (listener!=null)
		{
			listener.taskFinished( taskId, this, testPermutation, taskStartedSuccessfully );
			/*
			 * Remove the listener from the list as the task has finished
			 */
			_resultListeners.remove(taskId.getHashCode());
		}

		dumpActiveTaskList();
	}

	public void terminateTask( TaskIdInterface	taskId, String testPermutationCode ) throws java.rmi.RemoteException, NoSuchTaskId
	{
		writeLog("Request to terminate task received "+taskId.dumpInfo());
		System.out.println("Request to terminate task received "+taskId.dumpInfo());

		TaskRunner runner = null;

		synchronized(_activeTaskList)
		{
			runner = (TaskRunner)_activeTaskList.get(taskId.getHashCode());
		}

		if (runner != null)
		{
			if (!runner.terminate())
			{
				System.out.println("Task has already terminated");
				writeLog("Task has already terminated");
			}
			else
			{
				System.out.println("Task successfully terminated");
				writeLog("Task successfully terminated");
			}
		}
		else
		{
			System.out.println("Task runner not found - task has completed");
			writeLog("Task runner not found - task has completed");
		}
		//taskFinished( taskId, this, testPermutationCode, true );

		dumpActiveTaskList();
	}

	/**
	 * Terminate all tasks currently being run by this testnode.
	 *
	 * @throws RemoteException
	 */
	public boolean terminateAllTasks() throws RemoteException
	{
		boolean success = true;
        Object[] runners = null;

		synchronized(_activeTaskList)
		{
			runners = _activeTaskList.values().toArray();
		}

		for (int count=0;count<runners.length;count++)
		{
			TaskRunner taskRunner = (TaskRunner)runners[count];

			success &= taskRunner.terminate();
		}

		return success;
	}

	public void taskReturnedData( TaskIdInterface taskId, String data ) throws java.rmi.RemoteException
	{
	}

	public String getName() throws java.rmi.RemoteException
	{
		return(_nodeName);
	}

	public String getHostAddress() throws java.rmi.RemoteException
	{
		String ip = null;

		try
		{
			ip = java.net.InetAddress.getLocalHost().getHostAddress();
		}
		catch (java.net.UnknownHostException e)
		{
		}

		return(ip);
	}

	public void testFinished( String permutationCode, TaskIdInterface taskId, String testId ) throws java.rmi.RemoteException, TasksStillRunning
	{
		synchronized(_activeTaskList)
		{
			if (_activeTaskList.size()>0)
			{
				System.out.println("Tasks still running, size="+_activeTaskList.size());
				dumpActiveTaskList();
				throw new TasksStillRunning();
			}
		}

		_taskPerforming = null;
		dumpActiveTaskList();

		/**
		 * If the shutdown on complete variable is not null we have been requested to shutdown
		 * the value of the Boolean says whether the node should shutdown completely or restart.
		 */
		if ( _shutdownOnComplete != null )
		{
			exit(_shutdownOnComplete.booleanValue());
		}

        if ( _deployOnRelease )
        {
            updateSoftware(_productCurrentlyInUse, true);
            _deployOnRelease = false;
        }
	}

	public TaskIdInterface generateTaskId(String taskName) throws java.rmi.RemoteException, TestNodeBusy
	{
	    System.out.println("Generating task id. "+taskName+" (number of threads "+Thread.activeCount()+")");

		if ( _taskPerforming!=null && _shutdownOnComplete == null)
		{
			if (_taskPerforming.getTestId() != TaskId.UNDEFINED_TEST_ID)
				throw new TestNodeBusy();
		}

		synchronized(_activeTaskList)
		{
			if (_activeTaskList.size()>0)
				throw new TestNodeBusy();
		}

		return(generateNewTaskId(taskName));
	}

	public synchronized TaskId generateNewTaskId(String taskName) throws java.rmi.RemoteException
	{
		TaskId taskId = new TaskId();
		taskId.setTestId(_currentTestId++);
		taskId.setServiceId(_registrationDaemon.getServiceId());

		return(taskId);
	}

	public void waitForTask( TaskIdInterface 	taskId ) throws java.rmi.RemoteException, NoSuchTaskId, InterruptedException
	{
		TaskRunner taskRunner = null;
		dumpActiveTaskList();
		writeLog("Waiting for task "+taskId.getHashCode());

		synchronized(_activeTaskList)
		{
			taskRunner = (TaskRunner)_activeTaskList.get(taskId.getHashCode());
		}

		if (taskRunner == null)
		{
			throw new NoSuchTaskId(taskId.dumpInfo());
		}
		else
		{
			taskRunner.waitFor();
		}

		writeLog("Finished waiting for task "+taskId.getHashCode());
	}

	public void runTask( 	 String				taskType,
							 Hashtable			runnerParameters,
							 String				className,
							 String				classpathRef,
                             String             taskName,
							 String[]			parameters,
							 String[]			jvmParameters,
							 String  			product,
                             int 			    testType,
                             int				timeoutValue,
							 TaskResultListener	listener, // someone wants to be informed of this task
							 TaskIdInterface	taskId,
							 RunUID				runId,
							 String				taskPermutationCode,
							 String				testPermutationCode) throws java.rmi.RemoteException, TestNodeBusy, UnsupportedProduct, TaskRunnerNotSupported
	{
		System.out.println("**** "+runnerParameters);

		writeLog("Request to run task received "+taskId.getHashCode());
		if ( (_taskPerforming!=null) && (!_taskPerforming.inSameTest(taskId)) )
		{
			if (_taskPerforming.getTestId() != TaskId.UNDEFINED_TEST_ID)
				throw new TestNodeBusy();
		}

        _currentRunId = runId;
		_taskPerforming = taskId;
        _productCurrentlyInUse = product;

		if (_productRepository.getProductConfiguration(product).supportsOs(_osId))
		{
			TaskRunner taskRunner = getTaskRunner(runnerParameters, taskType, _currentTestName, className, classpathRef, taskName, parameters, jvmParameters, testType, timeoutValue, product, taskId, this, runId, taskPermutationCode, testPermutationCode);

   			System.out.println("Task "+className+" started give task id. "+taskId.dumpInfo()+" ("+taskId.getHashCode()+")");

			synchronized(_activeTaskList)
			{
				_activeTaskList.put(taskId.getHashCode(), taskRunner);
			}

			/*
			 * Register result listener
			 */
			_resultListeners.put(taskId.getHashCode(), listener);
			// set us up to be notified when the task finishes. We will then forward the note
			// to the listener
			taskRunner.registerResultListener(this);

            /**
             * Use Start task runner wrapper to ensure timeout is checked
             */
			StartTaskRunnerWrapper startTaskWrapper = new StartTaskRunnerWrapper(taskRunner, this);
            startTaskWrapper.startTask();

            _timeoutThreads.put(taskRunner, startTaskWrapper);

            if ( testType == TestNodeInterface.WAIT_READY )
            {
                System.out.println("Task is a WAIT READY task");
                try
                {
                    synchronized(taskRunner)
                    {
                        System.out.println("#### Waiting for task '"+taskId.dumpInfo()+"' to signal ready");
                        taskRunner.wait(timeoutValue * 1000);
                        System.out.println("#### Finished waiting for task '"+taskId.dumpInfo()+"' to signal ready");
                    }
                }
                catch (InterruptedException e)
                {
                }
            }
		}
		else
			throw new UnsupportedProduct(product);

		dumpActiveTaskList();
	}

	/**
	 * THis method returns the list of tasks currently being run by this node.
	 *
	 * @return An array of task names currently being run.
	 * @throws RemoteException
	 */
	public String[] getActiveTaskList() throws RemoteException
	{
		String[] tasks = null;

		synchronized(_activeTaskList)
		{
			Object[] keys = _activeTaskList.keySet().toArray();

			tasks = new String[keys.length];

			for (int count=0;count<keys.length;count++)
			{
				TaskRunner tr = (TaskRunner)_activeTaskList.get(keys[count]);
				if (tr!=null)
					tasks[count] = tr.getTaskName() + " ["+tr._className+"]";
			}
		}

		return tasks;
	}

	public void dumpActiveTaskList()
	{
		synchronized(_activeTaskList)
		{
			Object[] keys = _activeTaskList.keySet().toArray();

			System.out.println("---Active Task List---");
			for (int count=0;count<keys.length;count++)
			{
				TaskRunner tr = (TaskRunner)_activeTaskList.get(keys[count]);
				if (tr!=null)
					System.out.println((String)keys[count]+" "+tr.toString());
			}
		}

		System.out.println();
	}

    private TaskRunner getTaskRunner(	Hashtable			runnerParameters,
										String				type,
										String				testId,
										String 				className,
										String				classpathRef,
                                        String              taskName,
					  					String[]			parameters,
					  					String[]			jvmParameters,
					  					int	 				testType,
										int	 				timeoutValue,
										String				productName,
										TaskIdInterface		taskId,				// generated by Coordinator side
										TestNodeInterface	associatedTestNode, // this TestNode
										RunUID				runId,
										String				taskPermutationCode,
										String				testPermutationCode) throws TaskRunnerNotSupported
	{
		TaskRunner runner = null;
        TaskRunnerConfiguration info = null;

		try
		{
			/** First check the local task runner definitions, use that if one exists **/
            info = (TaskRunnerConfiguration)_taskRunners.get(type);

			/** If it doesn't exist then check the product configuration **/
			if (info == null)
			{
				ProductConfiguration productConfig = _serviceUtils.getProductRepository().getProductConfiguration(productName);

				info = productConfig.getTaskRunnerConfiguration(type);

				/** If it doesn't exist in the product configuration then we have problems **/
				if ( info == null )
				{
					System.out.println("Task runner "+type+" is not supported by this TestNode");
					throw new TaskRunnerNotSupported("Task runner "+type+" is not supported by this TestNode");
				}
			}

			runner = (TaskRunner)Class.forName(info.getClassname()).newInstance();

            /**
             * Combine the runner parameters passed from the invocation
             * with those configured in the test node configuration file
             */
            Hashtable combinedRunnerParameters = new Hashtable(info.getParameters());

            if (runnerParameters != null)
            {
                combinedRunnerParameters.putAll(runnerParameters);
            }

            StringPreprocessor pre = new StringPreprocessor();

			ProductConfiguration productConfig = _productRepository.getProductConfiguration(productName);
            NodeConfiguration nodeConfig = productConfig.getNodeConfiguration(_osId);

			/**
			 * If the java home parameter isn't set then set it to the one specified from the coordinator
			 */
			if ( !combinedRunnerParameters.containsKey(TaskRunner.JAVA_HOME_DIRECTORY_PARAMETER) )
			{
				String jvmId = nodeConfig.getJvmId();

				/** If the jvm id has not been configured use the default for the testnode **/
				if ( ( jvmId == null ) || ( jvmId.length() == 0 ) )
				{
					jvmId = _defaultJvmId;
				}

				System.out.println("Using jvm id '"+jvmId+"'");

				if ( _jvms.containsKey(jvmId) )
				{
					combinedRunnerParameters.put(TaskRunner.JAVA_HOME_DIRECTORY_PARAMETER, _jvms.get(jvmId));
				}
				else
				{
					throw new TaskRunnerNotSupported("The jvm '"+nodeConfig.getJvmId()+"' is not supported by this testnode");
				}
			}

			/** Use the default classpath if none is specified **/
			String classpathName = (classpathRef == null) ? nodeConfig.getDefaultClasspath() : classpathRef;

			pre.addReplacements(nodeConfig.getPreprocessedSets());
			String classpath = productConfig.getClasspath( classpathName );

			if ( classpath != null )
			{
				pre.addReplacement("CLASSPATH", classpath);
			}

            parameters = pre.preprocessParameters(parameters);
            jvmParameters = pre.preprocessParameters(jvmParameters);

			runner.initialise(combinedRunnerParameters, testId, className, classpathRef, taskName, parameters, jvmParameters, testType, timeoutValue, productConfig, nodeConfig, taskId, this, associatedTestNode, runId, taskPermutationCode, testPermutationCode, info.getLogTo(), _serviceUtils);
		}
		catch (TaskRunnerNotSupported e)
		{
			throw e;
		}
		catch (java.lang.Exception e)
		{
			System.out.println("ERROR - Cannot create an instance of the task runner '"+info.getClassname()+"'");
			e.printStackTrace(System.err);

			_registrationDaemon.disableProduct( productName );
		}
		return(runner);
	}

    /**
     * This method is called when a task which has been run times out
     *
     * @param runner The runner which is running this task.
     */
    public void taskTimedOut(TaskRunner runner)
    {
    	// This is wierd. TestNode registers itself as a resultListener.
    	// So this should be calling taskHasTimedOut() on itself.
    	// RB defines taskHasTimedOut() to avoid a recursive scenario.
    	// TaskHasTimedOut(0 looks up the original listener, and forwarsd the call.
        try
        {
            TaskResultListener taskResultListener = runner.getResultListener();

            System.out.println("Task has timed out '"+runner.toString()+"'");
            taskResultListener.taskHasTimedout(runner.getRunningTaskId(),this,runner.getRunningTestPermutationCode());
        }
        catch (RemoteException e)
        {
            e.printStackTrace(System.err);
            writeLog("ERROR - Unexpected exception "+e);
        }
    }

    /**
     * This method is invoked when the task signals Ready.
     * @param taskId The task Id. of the task that signals Ready.
     */
    public void taskSignalledReady(TaskIdInterface taskId,
                                   TestNodeInterface testNode,
                                   String testPermutation) throws RemoteException
    {
        System.out.println("Task signalled ready");
    }

    private void serializeVersionTable()
    {
        try
        {
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream( VERSION_TABLE_FILENAME ));
            out.writeObject(_productVersions);
            out.close();
        }
        catch (Exception e)
        {
            System.err.println("ERROR - While serializing the version table: "+e);
        }
    }

    /**
     * This method required a fix to handle the case when the VERSION_TABLE_FILENAME
     * does nto exist.
     */
    private void deserializeVersionTable()
    {
        try
        {
        	FileInputStream fis ;

    	    try {
    			// try to open the installers file
    			fis = new FileInputStream(VERSION_TABLE_FILENAME) ;
    		    }
    		    catch (FileNotFoundException e) {
    			// the file was not found - assume that we start with a new, empty version table
    			_productVersions = new Hashtable() ;

    			return ;
    		}

            ObjectInputStream in = new ObjectInputStream(fis);
            _productVersions = (Hashtable)in.readObject();
            in.close();
        }
        catch (Exception e)
        {
            System.err.println("ERROR - While deserializing the version table: "+e);
        }
    }

    /**
     * Called to inform the testnode that the given product should be updated.  This
     * should not be called directly - the product repository calls this method and
     * is responsible for ensuring all testnodes are up-to-date.  If the test node
     * is busy (i.e. in the midst of running tasks) it will throw a testnodebusy exception.
     *
     * @param productName The name of the product to update.
     * @param deploySoftware Should the node deploy the software
     * @return True - if the update was successful.
     * @throws java.rmi.RemoteException
     */
    public boolean updateSoftware(String productName, boolean deploySoftware) throws java.rmi.RemoteException
    {
        boolean success = true;

        synchronized ( _deployInProgress )
        {
            /**
             * If a task is currently in progress and it is running against this product then store the information for
             * installation once this node has been released
             */
            if ( deploySoftware && ( _taskPerforming!=null ) && ( productName.equals(_productCurrentlyInUse)) )
            {
                _deployOnRelease = true;
				return true;
            }

            Long currentVersion = (Long)_productVersions.get(productName);

            ProductConfiguration productConfig = _productRepository.getProductConfiguration(productName);
            NodeConfiguration nodeConfig = productConfig.getNodeConfiguration(_osId);

            /**
             * If there are currently no versions of this software installed
             * or the current version in the software repository is newer
             * then install this product.
             */
            if ( ( deploySoftware ) && ( nodeConfig != null) &&
                 ( ( currentVersion == null ) ||
                   ( !_productRepository.isCurrentVersion(productName, currentVersion.longValue() ) ) ) )
            {
                URL productInstaller = _productRepository.getProductInstaller(productName);

                if ( productInstaller != null )
                {
                    System.out.println("Installing product '"+productName+"'");

                    Properties props = new Properties();
                    props.putAll(nodeConfig.getPreprocessedProperties());
                    props.putAll(nodeConfig.getPreprocessedSets());

                    success &= ANTInstaller.install( productName, productInstaller, props );

                    /** If the install was successfull, update the local version number **/
                    if ( success )
                    {
                        _productVersions.put( productName, new Long( _productRepository.getCurrentVersion(productName) ) );

						_registrationDaemon.enableProduct( productName );

                        System.out.println("ANT deploy complete");
                    }
					else
					{
						System.out.println("ANT deploy failed - disabling product support");
						_registrationDaemon.disableProduct( productName );
					}
                }
            }

            _registrationDaemon.reregisterService(createSupportedProductList());

            serializeVersionTable();
        }

        return success;
    }

    public boolean updateSoftware() throws java.rmi.RemoteException, TestNodeBusy
	{
        /** If a task is being performed then throw test node busy exception **/
        if ( _taskPerforming!=null )
            throw new TestNodeBusy();

        boolean success = true;
        int productCount=0;
        String[] products;
		ProductRepositoryInterface productRepository;

		try
		{
			productRepository = _serviceUtils.getProductRepository();
			products = productRepository.getProductNames();
		}
		catch (ServiceNotFound e)
		{
			System.err.println("Cannot find product repository");
			throw new RemoteException("Cannot find product repository: "+e);
		}

        System.out.println("Updating node's products...");
        while (productCount<products.length)
        {
            ProductConfiguration product = productRepository.getProductConfiguration(products[productCount]);

            success &= updateSoftware( product.getName(), true );

            productCount++;
        }

		System.out.println("Update complete!");
        return success;
	}

    public void taskHasTimedout(TaskIdInterface taskId,
                                TestNodeInterface testNode,
                                String testPermutation) throws RemoteException
    {
        /*
         * Check to see if test had result listener registered
         */
        TaskResultListener listener = (TaskResultListener)_resultListeners.get(taskId.getHashCode());

        if (listener!=null)
        {
            listener.taskHasTimedout( taskId, this, testPermutation );
        }
    }

    /**
     * This methods returns the description of this test node.
     * @return
     * @throws RemoteException
     */
    public TestNodeDescription getNodeDescription() throws RemoteException
    {
        return _description;
    }

    public void shutdown(boolean restart, boolean onComplete) throws java.rmi.RemoteException
	{
		writeLog("Received request to shutdown "+(onComplete ? "on completion of current test" : ""));
		System.out.println("TestNode shutting down"+(onComplete ? " on completion of current test" : "")+"...");

		writeLog("De-registering with the service register");
		System.out.println("De-registering with the service register");

        if ( _registrationDaemon != null )
        {
            System.out.println("Shutting down registration daemon");

            _registrationDaemon.stopDaemon();
		    _registrationDaemon.deregisterService();
        }

		_shutdownOnComplete = new Boolean(restart);

		/** If we are not doing anything shutdown now **/
		if ( _taskPerforming == null )
		{
			onComplete = false;
		}

		if ( !onComplete )
		{
			System.out.println("Killing all tasks");
			terminateAllTasks();

			exit(restart);
		}
	}

	private void exit(boolean restart)
	{
		System.out.println("Exiting "+(restart ? "and restarting" : ""));

		if (restart)
			System.exit(1);
		else
			System.exit(2);
	}

	private final static int getRMIPort()
	{
		String rmiPort = System.getProperty("rmi.port", ""+Registry.REGISTRY_PORT);

		return Integer.parseInt(rmiPort);
	}

    public static void initialiseRegistry()
    {
        try
        {
			int rmiPort = getRMIPort();

            System.out.println("Creating RMI registry on port "+rmiPort);
            LocateRegistry.createRegistry(rmiPort);
        }
        catch (RemoteException ex)
        {
        }
    }

	public static void main(String args[])
	{
		if ( args.length < 2 )
		{
			System.out.println("TestNode Usage: org.jboss.dtf.testframework.testnode [URI of NameService] [Collator IP] {-PORT <collator port number>} {-URL [url://config.xml]} {-FILE [config.xml]} {-NAME [node name]}");
			System.exit(1);
		}

		try
		{
            initialiseRegistry();

			int collatorPort = DEFAULT_COLLATOR_PORT;
            String configFilename = DEFAULT_CONFIG_FILENAME;
			String nodeName = null;
			boolean urlSpecified = false;

			for (int count=2;count<args.length;count++)
			{
				if (args[count].equalsIgnoreCase("-PORT"))
				{
					collatorPort = Integer.parseInt( args[count + 1] );
				}
                else
                if (args[count].equalsIgnoreCase("-FILE"))
                {
                    configFilename = args[count + 1];
					urlSpecified = false;
                }
				else
				if (args[count].equalsIgnoreCase("-URL"))
				{
					configFilename = args[count + 1];
					urlSpecified = true;
				}
				else
				if (args[count].equalsIgnoreCase("-NAME"))
				{
					nodeName = args[count + 1];
				}
			}

			URL configFile;

			if ( urlSpecified )
			{
				configFile = new URL(configFilename);
			}
			else
			{
				configFile = new File(configFilename).toURL();
			}

			TestNode testNode = new TestNode(configFile, args[0], nodeName);
            System.out.println("Ready");

            /**
             * Perform test node initialisation
             */
            testNode.initialise(args[1], collatorPort);

		    Naming.rebind("//"+java.net.InetAddress.getLocalHost().getHostName()+":"+getRMIPort()+"/TestNode", testNode);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			System.exit(11);
		}
	}

	public static String getOSId()
	{
		return _osId;
	}
}
