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
// $Id: TaskIdInterface.java 170 2008-03-25 18:59:26Z jhalliday $

package org.jboss.dtf.testframework.testnode;

public interface TaskIdInterface extends java.rmi.Remote
{
	public final static short 	UNKNOWN_SERVICE_ID = -1,
								UNDEFINED_TEST_ID = -1;

	public TaskIdInterface copyTask() throws java.rmi.RemoteException;

	public TaskIdInterface incrementTaskId() throws java.rmi.RemoteException;

	public short getTaskId() throws java.rmi.RemoteException;

	public void setTaskId(short id) throws java.rmi.RemoteException;

	public short getTestId() throws java.rmi.RemoteException;

	public void setTestId(short id) throws java.rmi.RemoteException;

	public short getServiceId() throws java.rmi.RemoteException;

	public void setServiceId(short id) throws java.rmi.RemoteException;

	public String dumpInfo() throws java.rmi.RemoteException;

	public String getHashCode() throws java.rmi.RemoteException;

	public boolean inSameTest(TaskIdInterface id) throws java.rmi.RemoteException;
}
