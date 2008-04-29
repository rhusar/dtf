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
 * $Id: TaskDataHandler.java 170 2008-03-25 18:59:26Z jhalliday $
 */
package org.jboss.dtf.testframework.coordinator2;

import org.jboss.dtf.testframework.testnode.TaskResultListener;
import org.jboss.dtf.testframework.testnode.TaskIdInterface;
import org.jboss.dtf.testframework.testnode.TestNodeInterface;
import org.jboss.dtf.testframework.testnode.NoSuchTaskId;

import org.jboss.dtf.testframework.coordinator2.*;
import org.jboss.dtf.testframework.coordinator2.testmanager.*;

import java.rmi.RemoteException;

public class TaskDataHandler extends java.rmi.server.UnicastRemoteObject implements TaskResultListener
{
    protected TestManager       _testManager = null;
    protected TaskDefinition    _task = null;
	protected TestDefinition	_test = null;
    protected String            _taskPermutationCode = null;

    public TaskDataHandler(TestManager testManager, TestDefinition test, TaskDefinition task, String taskPermutationCode) throws RemoteException
    {
        super();

        _task = task;
        _testManager = testManager;
        _taskPermutationCode = taskPermutationCode;
		_test = test;
    }

    /**
     * This method is invoked when a line of test is outputted from the task.
     * @param taskId The task id. of the task that returned data.
     * @param data The information outputted from the task.
     * @exception RemoteException Thrown by the underlying RMI code.
     */
    public void taskReturnedData(TaskIdInterface taskId,
                                 String data) throws RemoteException
    {
    }

    /**
     * This method is invoked when the task finishes.
     * @param taskId The task Id. of the task that finished.
     */
    public void taskFinished(TaskIdInterface taskId,
                             TestNodeInterface testNode,
                             String testPermutation,
                             boolean taskStartedSuccessfully) throws RemoteException, NoSuchTaskId
    {
    }

    /**
     * This method is invoked when the task signals Ready.
     * @param taskId The task Id. of the task that signals Ready.
     */
    public void taskSignalledReady(TaskIdInterface      taskId,
                                   TestNodeInterface    testNode,
                                   String               testPermutationCode) throws RemoteException
    {
    }

    public void taskHasTimedout(TaskIdInterface     taskId,
                                TestNodeInterface   testNode,
                                String              testPermutation) throws RemoteException
    {
        try
        {
            Coordinator.getLoggingService().logTestInformation(_test.getFullId(), _testManager.getRunId(), testPermutation,"The task '"+_testManager.getRuntimeId(taskId)+"' has timed out - execution halted\n");
            Coordinator.getLoggingService().logTimeout(_test.getFullId(), _testManager.getRunId(), testPermutation);
        }
        catch (Exception ex)
        {
            System.err.println("ERROR - Failed to log test information (logging timeout): "+ex);
			ex.printStackTrace();
        }

        try
        {
            System.out.println("Task has timedout '"+taskId.dumpInfo()+"' on testnode '"+testNode.getName()+"' running test permutation '"+testPermutation+"'");
            testNode.terminateTask(taskId, testPermutation);
        }
        catch (NoSuchTaskId e)
        {
            System.err.println("ERROR - Task id '"+taskId.dumpInfo()+"' is not running on that node");
        }
    }
}
