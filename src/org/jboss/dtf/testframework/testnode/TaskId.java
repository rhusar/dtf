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
// $Id: TaskId.java 170 2008-03-25 18:59:26Z jhalliday $

package org.jboss.dtf.testframework.testnode;

public class TaskId extends java.rmi.server.UnicastRemoteObject implements TaskIdInterface
{
	private short		_testId = UNDEFINED_TEST_ID;
	private short		_taskId = 0;
	private short     	_serviceId = UNKNOWN_SERVICE_ID;
	private String      _taskName = "";

	public TaskId() throws java.rmi.RemoteException
	{
		super();
	}

	public final TaskIdInterface incrementTaskId() throws java.rmi.RemoteException
	{
		return(incTaskId());
	}

	private final synchronized TaskIdInterface incTaskId() throws java.rmi.RemoteException
	{
		TaskIdInterface newId = copyTask();
		short taskId = newId.getTaskId();

		newId.setTaskId(++taskId);
		System.out.println("Just run id "+this.dumpInfo()+" returned "+ newId.dumpInfo());
		return(newId);
	}

	public final short getTaskId() throws java.rmi.RemoteException
	{
		return(_taskId);
	}

	public final short getTestId() throws java.rmi.RemoteException
	{
		return(_testId);
	}

	public final void setTaskId(short id) throws java.rmi.RemoteException
	{
		_taskId = id;
	}

	public final void setTestId(short id) throws java.rmi.RemoteException
	{
		_testId = id;
	}

	public final short getServiceId() throws java.rmi.RemoteException
	{
		return(_serviceId);
	}

	public final void setServiceId(short id) throws java.rmi.RemoteException
	{
		_serviceId = id;
	}

	public String dumpInfo() throws java.rmi.RemoteException
	{
		return("TaskId["+_taskId+","+_testId+","+_serviceId+"]");
	}

	public String getHashCode() throws java.rmi.RemoteException
	{
		return(Long.toHexString(((long)_serviceId<<32)|((long)_testId<<16)|((long)_taskId)));
	}

	public boolean inSameTest(TaskIdInterface id) throws java.rmi.RemoteException
	{
		return( ( _testId != UNDEFINED_TEST_ID && _serviceId != UNKNOWN_SERVICE_ID ) && (getTestId() == id.getTestId()) );
	}

	public TaskIdInterface copyTask() throws java.rmi.RemoteException
	{
		TaskId cloneTask = new TaskId();

		cloneTask.setTaskId(getTaskId());
		cloneTask.setTestId(getTestId());
		cloneTask.setServiceId(getServiceId());
		return(cloneTask);
	}

}
