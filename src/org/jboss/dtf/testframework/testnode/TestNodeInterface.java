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
// $Id: TestNodeInterface.java 170 2008-03-25 18:59:26Z jhalliday $

package org.jboss.dtf.testframework.testnode;

import org.jboss.dtf.testframework.coordinator.UnsupportedProduct;
import org.jboss.dtf.testframework.utils.RemoteFileReaderInterface;

import java.util.Hashtable;

/**
 * The RMI interface for the TestNode.  A TestNode will implement this
 * interface.
 */
public interface TestNodeInterface extends java.rmi.Remote
{
	/**
	 * Wait for task to return the text 'Ready' before returning
	 * @see #performTask
	 */
	public final static int 	WAIT_READY = 1;
	/**
	 * Do not wait for the task to return anything
	 * @see #performTask
	 */
	public final static int 	WAIT_NONE = 2;

	/**
	 * Task returned the string 'Ready'.
	 * @see #performTask
	 */
	public final static int 	RESULT_READY = 1;

	/**
	 * The task returned nothing within the expected timeout period.
	 * @see #performTask
	 */
	public final static int 	RESULT_TIMEOUT = 2;

	/**
	 * Get the Name of the TestNode (specified in the XML configuration for that
	 * test node).
	 * @return Returns the name of the TestNode as a string.
	 * @exception java.rmi.RemoteException Thrown by the RMI implementation.
	 */
	public String getName() throws java.rmi.RemoteException;

	public String getHostAddress() throws java.rmi.RemoteException;

	/**
	 * This method is called by the service register to ensure this test node
	 * is responding.  Always return true
	 */
	public boolean ping() throws java.rmi.RemoteException;

	/**
	 * Instructs the TestNode to run a given Java class.
	 * @return Returns the information that was returned from the server.
	 * @param className The fully qualified name of the class to be run.
	 * @param parameters The parameters to be passed to this class.
	 * @param productId The string identifier for the product that this test should be
	 * run under.
	 * @param taskType The type of test to be run.
	 * @param timeoutValue The amount of time the class is given to
	 * produce the expected output.  If it doesn't then the value RESULT_TIMEOUT
	 * is returned.
	 * @exception java.rmi.RemoteException Thrown by the RMI implementation.
	 */
	public int performTask( String			taskType,
							Hashtable		runnerParameters,
							String 			className,
							String			classpathRef,
                            String          taskName,
							String[]		parameters,
							String[]		jvmParameters,
							String			productId,
							int				timeoutValue,
							TaskIdInterface taskId,
							RunUID			runId,
							String			taskPermutationCode,
							String			testPermutationCode) throws java.rmi.RemoteException, TestNodeBusy, UnsupportedProduct, TaskRunnerNotSupported;

	/**
	 * Instructs the TestNode to run a given Java class and report back using the
	 * listener any information returned by the task.
	 * @param className The fully qualified name of the class to be run.
	 * @param parameters The parameters to be passed to this class.
	 * @param timeoutValue The amount of time the class is given to
	 * produce the expected output.  If it doesn't then the value RESULT_TIMEOUT
	 * is returned.
	 * @param listener A class which should be informed when the task returns information.
	 * @exception java.rmi.RemoteException Thrown by the RMI implementation.
	 */
	public void runTask( 	 String				taskType,
							 Hashtable			runnerParameters,
							 String				className,
							 String				classpathRef,
                             String             taskName,
							 String[]			parameters,
							 String[]			jvmParameters,
							 String				product,
                             int 			    testType,
							 int				timeoutValue,
							 TaskResultListener	listener,
							 TaskIdInterface	taskId,
							 RunUID				runId,
							 String				taskPermutationCode,
							 String				testPermutationCode) throws java.rmi.RemoteException, TestNodeBusy, UnsupportedProduct, TaskRunnerNotSupported;

	public void terminateTask( TaskIdInterface	taskId,
	                           String           testPermutationCode ) throws java.rmi.RemoteException, NoSuchTaskId;

	public boolean terminateAllTasks() throws java.rmi.RemoteException;

	/**
     * Instructs the testnode to update its software for all the products it supports.
     * Using the ProductRepository it can check to ensure it has the latest versions
     * of any product it supports.
     *
     * @return True if the software update was successfull, otherwise false.
	 */
	public boolean updateSoftware( ) throws java.rmi.RemoteException, TestNodeBusy;
    public boolean updateSoftware(String productName, boolean deploySoftware) throws java.rmi.RemoteException;

	/**
	 * Instructs the TestNode to deregister with the ServiceRegister and shutdown
	 * @param restart If true the testnode will request to be restarted once it has shutdown
	 * this will be used after a software update.
	 * @param onComplete If true the testnode will wait until it has completed it's current
	 * test before shutting down.
	 */
	public void shutdown(boolean restart, boolean onComplete) throws java.rmi.RemoteException;

	/**
	 * Generates a default task id.
	 * @param taskName A name given to this task.
	 * @return The TaskId
	 */
	public TaskIdInterface generateTaskId(String taskName) throws java.rmi.RemoteException, TestNodeBusy;

	public void initiateTest(String				currentTestId,
							 TaskIdInterface	taskId) throws java.rmi.RemoteException, TestNodeBusy;

	/**
	 * Inform the test node that the test has finished and that it is now
	 * free to perform other tests.  This also initiates the upload of results
	 * generated by the tasks to the coordinator.
	 * @param taskId The taskId indentifying the test which has finished.
	 * @param testId A string identifier for this test used to report back
	 * the results to the Coordinator
	 */
	public void testFinished( String permutationCode,
	                          TaskIdInterface taskId,
	                          String testId ) throws java.rmi.RemoteException, TasksStillRunning;

	/**
	 * This method blocks until the task identified completes or until the timeoutValue elapses
	 * @param taskId The taskId identifying the task which must complete.
	 * @exception NoSuchTaskId This is thrown if a task isn't running with this task id.
	 */
	public void waitForTask( TaskIdInterface 	taskId ) throws java.rmi.RemoteException, NoSuchTaskId, InterruptedException;

	/**
	 * THis method returns the list of tasks currently being run by this node.
	 *
	 * @return An array of task names currently being run.
	 * @throws java.rmi.RemoteException
	 */
	public String[] getActiveTaskList() throws java.rmi.RemoteException;

    /**
     * This methods returns the description of this test node.
     * @return
     * @throws java.rmi.RemoteException
     */
    public TestNodeDescription getNodeDescription() throws java.rmi.RemoteException;

	/**
	 * This method retrieves an InputStream for the log output for a deployment.
	 * @param productName
	 * @param outOrErr True indicates the output stream and False indicates the error stream
	 * @return An input stream to the data
	 * @throws java.rmi.RemoteException
	 */
	public RemoteFileReaderInterface getDeployLogOutput(String productName, boolean outOrErr) throws java.io.IOException, java.rmi.RemoteException;
}
