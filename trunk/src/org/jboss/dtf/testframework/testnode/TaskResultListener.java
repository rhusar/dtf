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
// $Id: TaskResultListener.java 170 2008-03-25 18:59:26Z jhalliday $

package org.jboss.dtf.testframework.testnode;

import java.rmi.RemoteException;

/**
 * The interface to be implemented by a task who is interested in
 * listening to the output from a task run by the TestNode.
 */
public interface TaskResultListener extends java.rmi.Remote
{
	/**
	 * This method is invoked when a line of test is outputted from the task.
	 * @param taskId The task id. of the task that returned data.
	 * @param data The information outputted from the task.
	 * @exception java.rmi.Exception Thrown by the underlying RMI code.
	 */
	public void taskReturnedData( TaskIdInterface 	taskId,
								  String  			data ) throws java.rmi.RemoteException;

	/**
	 * This method is invoked when the task finishes.
	 * @param taskId The task Id. of the task that finished.
	 */
	public void taskFinished( TaskIdInterface 	taskId,
							  TestNodeInterface	testNode,
							  String            testPermutation,
							  boolean			taskStartedSuccessfully ) throws java.rmi.RemoteException, NoSuchTaskId;

	/**
	 * This method is invoked when the task signals Ready.
	 * @param taskId The task Id. of the task that signals Ready.
	 */
    public void taskSignalledReady(TaskIdInterface      taskId,
                                   TestNodeInterface    testNode,
                                   String               testPermutation ) throws java.rmi.RemoteException;

    public void taskHasTimedout(TaskIdInterface         taskId,
                                TestNodeInterface       testNode,
                                String                  testPermutation ) throws java.rmi.RemoteException;
}
