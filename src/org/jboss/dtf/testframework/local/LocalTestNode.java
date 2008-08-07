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
 *
 * $Id$
 */
package org.jboss.dtf.testframework.local;

import org.jboss.dtf.testframework.testnode.*;
import org.jboss.dtf.testframework.coordinator.UnsupportedProduct;
import org.jboss.dtf.testframework.coordinator.Action;
import org.jboss.dtf.testframework.coordinator.NoAssociatedData;
import org.jboss.dtf.testframework.utils.*;
import org.jboss.dtf.testframework.productrepository.TaskRunnerConfiguration;
import org.jboss.dtf.testframework.productrepository.NodeConfiguration;
import org.jboss.dtf.testframework.productrepository.ProductConfiguration;
import org.jboss.dtf.testframework.coordinator2.TestDefinition;
import org.jboss.dtf.testframework.coordinator2.TaskDefinition;
import org.jdom.input.SAXBuilder;
import org.jdom.Document;
import org.jdom.Element;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Hashtable;
import java.util.List;
import java.util.HashMap;
import java.io.IOException;
import java.io.File;
import java.net.URL;

/**
 * A modified version of the DTF TestNode, redesigned to run in-process rather than as a remote slave.
 *
 * Many methods do nothing, since they are never called. The implementation of the rest is stolen from
 * the real TestNode code and stripped down by throwing out irrelevant things like the permutation codes.
 *
 * @author Jonathan Halliday (jonathan.halliday@redhat.com)
 */
public class LocalTestNode extends UnicastRemoteObject implements TestNodeInterface
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

    private final static String JVM_DEFINITIONS_ELEMENT = "jvm-definitions";
    private final static String JVM_ELEMENT = "jvm";
    private final static String VERSION_ATTRIBUTE = "version";
    private final static String JAVA_HOME_ATTRIBUTE = "java-home";
    private final static String DEFAULT_ATTRIBUTE = "default";

    private String _osId = null;
    private String _nodeName = "";
    private Hashtable _jvms = new Hashtable();
    private String _defaultJvmId = null;
    private Hashtable _taskRunners = new Hashtable();
    private ProductConfiguration _productConfiguration = null;

    private static short _testIdCounter = 0;

    private final HashMap<String, TaskRunner> _activeTaskList = new HashMap<String, TaskRunner>();

    public LocalTestNode(URL configFile, String productConfigurationFileName) throws Exception {
        super();
        parseTestNodeConfig(configFile);
        _productConfiguration = ProductConfiguration.deserializeXML(new File(productConfigurationFileName));
    }

    public String getName() throws RemoteException
    {
        return _nodeName;
    }

    public String getHostAddress() throws RemoteException
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean ping() throws RemoteException
    {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public int performTask(String s, Hashtable hashtable, String s1, String s2, String s3, String[] strings, String[] strings1, String s4, int i, TaskIdInterface taskIdInterface, RunUID runUID, String s5, String s6) throws RemoteException, TestNodeBusy, UnsupportedProduct, TaskRunnerNotSupported
    {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void runTask(String s, Hashtable hashtable, String s1, String s2, String s3, String[] strings, String[] strings1, String s4, int i, int i1, TaskResultListener taskResultListener, TaskIdInterface taskIdInterface, RunUID runUID, String s5, String s6) throws RemoteException, TestNodeBusy, UnsupportedProduct, TaskRunnerNotSupported
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void terminateTask(TaskIdInterface taskIdInterface, String s) throws RemoteException, NoSuchTaskId
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean terminateAllTasks() throws RemoteException
    {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean updateSoftware() throws RemoteException, TestNodeBusy
    {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean updateSoftware(String productName, boolean deploySoftware) throws RemoteException
    {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void shutdown(boolean restart, boolean onComplete) throws RemoteException
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public TaskIdInterface generateTaskId(String taskName) throws RemoteException, TestNodeBusy
    {
        TaskIdInterface taskId =  new TaskId();
        synchronized(LocalTestNode.class) {
            _testIdCounter += 1;
            taskId.setTestId( _testIdCounter );
        }
        return taskId;
    }

    public void initiateTest(String s, TaskIdInterface taskIdInterface) throws RemoteException, TestNodeBusy
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void testFinished(String s, TaskIdInterface taskIdInterface, String s1) throws RemoteException, TasksStillRunning
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void waitForTask(TaskIdInterface taskIdInterface) throws RemoteException, NoSuchTaskId, InterruptedException
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public String[] getActiveTaskList() throws RemoteException
    {
        return new String[0];  //To change body of implemented methods use File | Settings | File Templates.
    }

    public TestNodeDescription getNodeDescription() throws RemoteException
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public RemoteFileReaderInterface getDeployLogOutput(String productName, boolean outOrErr) throws IOException, RemoteException
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    ////////////////

    public int performTask(TestDefinition testDefinition, TaskDefinition taskDefinition, Action action) throws Exception
    {
        String[] parameters = getParameters(action, taskDefinition, testDefinition.getNamesRequired());
        String[] jvmParameters = getJVMParameters(action, taskDefinition, testDefinition.getNamesRequired());
        String runtimeTaskId = action.getAssociatedRuntimeTaskId();

        // Get the test level runner parameters
        Hashtable runnerParameters = testDefinition.getParametersForRunner(taskDefinition.getRunner());
        // If there are task level runner parameters override the test level parameters
        if (taskDefinition.getRunnerParameters() != null)
        {
            runnerParameters = taskDefinition.getRunnerParameters();
        }

        TaskRunner taskRunner = getTaskRunner(runnerParameters,
                taskDefinition.getRunner(),
                testDefinition.getId(),
                taskDefinition.getClassName(),
                taskDefinition.getClasspath(),
                taskDefinition.getId(),
                parameters,
                jvmParameters,
                TestNodeInterface.WAIT_NONE,
                taskDefinition.getTimeout(),
                "productName",
                this.generateTaskId( taskDefinition.getId() ), // TaskIDInterface
                this, // TestNode
                new RunUID(0), // RunUID
                "taskPermutationCode",
                "testPermutationCode");


        synchronized(_activeTaskList)
        {
            _activeTaskList.put(runtimeTaskId, taskRunner);
        }

        taskRunner.start();

        // the TestNode starts the TaskRunner, and then uses a method of the TaskRunner
        // to wait and check for timeout. (i.e. performTask() is synchronous)

        if (!taskRunner.hasFinished() && taskRunner.waitForReadyOrFinished())
        {
            terminateTask(action);
            return RESULT_TIMEOUT;
        } else
        {
            waitForTask(action);
            return RESULT_READY;
        }
    }

    public void startTask(TestDefinition testDefinition, TaskDefinition taskDefinition, Action action) throws Exception
    {
        String[] parameters = getParameters(action, taskDefinition, testDefinition.getNamesRequired());
        String[] jvmParameters = getJVMParameters(action, taskDefinition, testDefinition.getNamesRequired());
        String runtimeTaskId = action.getAssociatedRuntimeTaskId();

        // Get the test level runner parameters
        Hashtable runnerParameters = testDefinition.getParametersForRunner(taskDefinition.getRunner());
        // If there are task level runner parameters override the test level parameters
        if (taskDefinition.getRunnerParameters() != null)
        {
            runnerParameters = taskDefinition.getRunnerParameters();
        }

        TaskRunner taskRunner = getTaskRunner(runnerParameters,
                taskDefinition.getRunner(),
                testDefinition.getId(),
                taskDefinition.getClassName(),
                taskDefinition.getClasspath(),
                taskDefinition.getId(),
                parameters,
                jvmParameters,
                taskDefinition.getType(),
                taskDefinition.getTimeout(),
                "productName",
                this.generateTaskId( taskDefinition.getId() ), // TaskIDInterface
                this, // TestNode
                new RunUID(0), // RunUID
                "taskPermutationCode",
                "testPermutationCode");

        synchronized(_activeTaskList)
        {
            _activeTaskList.put(runtimeTaskId, taskRunner);
        }

        taskRunner.start();

        if ( taskDefinition.getType() == TaskDefinition.EXPECT_READY )
        {
            taskRunner.waitForReadyOrFinished();
        }
    }

    public void waitForTask(Action action) throws NoAssociatedData, NoSuchTaskId, InterruptedException
    {
	    String runtimeTaskId = action.getAssociatedRuntimeTaskId();
        TaskRunner taskRunner;

   		synchronized(_activeTaskList)
		{
			taskRunner = _activeTaskList.get(runtimeTaskId);
		}

		if (taskRunner == null)
		{
			throw new NoSuchTaskId();
		}
		else
		{
			taskRunner.waitFor();
            synchronized (_activeTaskList) {
                _activeTaskList.remove(runtimeTaskId);
            }
        }
    }

    public void terminateTask(Action action) throws NoAssociatedData
    {
        String runtimeTaskId = action.getAssociatedRuntimeTaskId();
		TaskRunner runner = null;

		synchronized(_activeTaskList)
		{
			runner = _activeTaskList.get(runtimeTaskId);
		}

		if (runner != null)
		{
			runner.terminate();
            synchronized (_activeTaskList) {
                _activeTaskList.remove(runtimeTaskId);
            }
        }
    }

    public boolean isActive() {
        synchronized (_activeTaskList) {
            return !_activeTaskList.isEmpty();
        }
    }

    private String[] getJVMParameters(Action action, TaskDefinition taskDefinition, int uniqNameCount)
            throws NoAssociatedData
    {
        String[] jvmParameters = taskDefinition.getJVMParameters();
        long parameterSettings = taskDefinition.getParameterSettings();

        if ( ( ( parameterSettings & TaskDefinition.OVERRIDE_JVM_PARAMETERS ) != 0 ) && ( action.getJVMParameterList().length > 0 ) )
        {
            jvmParameters = action.getJVMParameterList();
        }
        if ( ( parameterSettings & TaskDefinition.PREPEND_JVM_PARAMETERS ) != 0 )
        {
            jvmParameters = ArrayUtils.prependArray(jvmParameters, action.getJVMParameterList());
        }
        if ( ( parameterSettings & TaskDefinition.APPEND_JVM_PARAMETERS ) != 0 )
        {
            jvmParameters = ArrayUtils.appendArray(jvmParameters, action.getJVMParameterList());
        }
        for (int paramCount=0;paramCount<jvmParameters.length;paramCount++)
        {
            jvmParameters[paramCount] = ParameterPreprocessor.preprocessParameters(jvmParameters[paramCount], false);
        }

        StringPreprocessor pre = new StringPreprocessor();
        for (int nameCount=0;nameCount<uniqNameCount;nameCount++)
        {
            pre.addReplacement( ""+(nameCount+1), "value_"+(nameCount+1) );
        }

        if (jvmParameters!=null)
        {
            jvmParameters = pre.preprocessParameters(jvmParameters, false);

            jvmParameters = ParameterPreprocessor.preprocessParameters(jvmParameters, false);
        }

        return jvmParameters;
    }

    private String[] getParameters(Action action, TaskDefinition taskDefinition, int uniqNameCount)
            throws NoAssociatedData
    {
        String[] parameters = taskDefinition.getParameters();
        long parameterSettings = taskDefinition.getParameterSettings();

        if ( ( ( parameterSettings & TaskDefinition.OVERRIDE_PARAMETERS ) != 0 ) && ( action.getParameterList().length > 0 ) )
        {
            parameters = action.getParameterList();
        }
        if ( ( parameterSettings & TaskDefinition.PREPEND_PARAMETERS ) != 0 )
        {
            parameters = ArrayUtils.prependArray(parameters, action.getParameterList());
        }
        if ( ( parameterSettings & TaskDefinition.APPEND_PARAMETERS ) != 0 )
        {
            parameters = ArrayUtils.appendArray(parameters, action.getParameterList());
        }
        for (int paramCount=0;paramCount<parameters.length;paramCount++)
        {
            parameters[paramCount] = ParameterPreprocessor.preprocessParameters(parameters[paramCount], false);
        }

        StringPreprocessor pre = new StringPreprocessor();
        for (int nameCount=0;nameCount<uniqNameCount;nameCount++)
        {
            pre.addReplacement( ""+(nameCount+1), "value_"+(nameCount+1) );
        }

        // If there are parameters to be passed from the test definition do so
        if (parameters!=null)
        {
            parameters = pre.preprocessParameters(parameters, false);
            parameters = ParameterPreprocessor.preprocessParameters(parameters, false);
        }

        return parameters;
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
				info = _productConfiguration.getTaskRunnerConfiguration(type);

				/** If it doesn't exist in the product configuration then we have problems **/
				if ( info == null )
				{
					System.err.println("Task runner "+type+" is not supported by this TestNode");
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

            NodeConfiguration nodeConfig = _productConfiguration.getNodeConfiguration(_osId);

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

				//System.out.println("Using jvm id '"+jvmId+"'");

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
			String classpath = _productConfiguration.getClasspath( classpathName );

			if ( classpath != null )
			{
				pre.addReplacement("CLASSPATH", classpath);
			}

            parameters = pre.preprocessParameters(parameters);
            jvmParameters = pre.preprocessParameters(jvmParameters);

			runner.initialise(combinedRunnerParameters, testId, className, classpathRef, taskName, parameters,
                    jvmParameters, testType, timeoutValue, _productConfiguration, nodeConfig, taskId, null, associatedTestNode,
                    runId, taskPermutationCode, testPermutationCode, info.getLogTo(), new ServiceUtils(""));
		}
		catch (TaskRunnerNotSupported e)
		{
			throw e;
		}
		catch (java.lang.Exception e)
		{
			System.err.println("ERROR - Cannot create an instance of the task runner '"+info.getClassname()+"'");
			throw new TaskRunnerNotSupported("Problem in test runner setup: "+e);
		}
		return(runner);
	}

    private void parseTestNodeConfig(URL configFile)
    {
        try
        {
            SAXBuilder xmlBuilder = new SAXBuilder();
            Document doc = xmlBuilder.build(configFile);

            // Retrieve root element, then retrieve the test node configuration element
            Element root = doc.getRootElement();
            Element testNodeRootElement = root;

            _nodeName = testNodeRootElement.getAttributeValue(TEST_NODE_NAME);
            _osId = testNodeRootElement.getAttributeValue(TEST_NODE_OS_ID);

            if (_osId == null)
            {
                System.err.println("No operating system id. specified - this is required");
                System.exit(1);
            }

            Element taskRunnerDefinitions = root.getChild(TASK_RUNNER_DEFINITIONS_ELEMENT);
            List taskRunnerElements = taskRunnerDefinitions.getChildren(TASK_RUNNER_ELEMENT);

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

            if (_defaultJvmId == null)
            {
                System.err.println("default jvm-definitions node configuration not specified!");
                System.exit(1);
            }

            /** Get the jvm elements **/
            List jvmDefinitionElements = jvmDefinitionsElement.getChildren(JVM_ELEMENT);

            for (int count = 0; count < jvmDefinitionElements.size(); count++)
            {
                Element jvmDefinitionElement = (Element) jvmDefinitionElements.get(count);

                _jvms.put(jvmDefinitionElement.getAttributeValue(VERSION_ATTRIBUTE), jvmDefinitionElement.getAttributeValue(JAVA_HOME_ATTRIBUTE));
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.exit(1);
        }
    }
}