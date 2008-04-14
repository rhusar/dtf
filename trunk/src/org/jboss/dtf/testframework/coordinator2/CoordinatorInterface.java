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
 * $Id: CoordinatorInterface.java 170 2008-03-25 18:59:26Z jhalliday $
 */
package org.jboss.dtf.testframework.coordinator2;

import org.jboss.dtf.testframework.testnode.RunUID;
import org.jboss.dtf.testframework.coordinator2.scheduler.ScheduleInformation;
import org.jboss.dtf.testframework.coordinator2.scheduler.SchedulerInterface;
import org.jboss.dtf.testframework.coordinator2.runmanager.CannotStartRunException;

import java.net.URL;

public interface CoordinatorInterface extends java.rmi.Remote
{
    public SchedulerInterface getScheduler() throws java.rmi.RemoteException;

	/**
	 * Start a test run.  The coordinator will run the tests defined in
	 * testDefsURL and selected within the testSelections file.
	 *
	 * @param testDefsURL A URL to the test definitions file.
	 * @param testSelectionsURL A URL to the test selections file.
	 * @param distributionList The email distribution list.
	 * @param softwareVersion The software version to log against.
	 * @throws java.rmi.RemoteException
	 */
	public void run(  URL 	    testDefsURL,
				      URL 	    testSelectionsURL,
					  String	distributionList,
					  String	softwareVersion,
					  boolean	waitToComplete) throws java.rmi.RemoteException, CoordinatorBusyException, CannotStartRunException;

	/**
	 * Stops the currently active run.
	 *
	 * @param waitForTestToComplete If this parameter is true then the run will stop once
	 * the current test has finished.  If false then the run will stop immediately.
	 *
	 * @throws java.rmi.RemoteException
	 * @throws CoordinatorIdleException If the coordinator isn't currently running any
	 * tests then this exception will be thrown.
	 */
	public boolean stopRun(boolean waitForTestToComplete, RunUID runId) throws CoordinatorIdleException, java.rmi.RemoteException;

	/**
	 * Retrieve information about the current run in progress.
	 *
	 * @return
	 * @throws java.rmi.RemoteException
	 * @throws CoordinatorIdleException If the coordinator is not currently running any tests.
	 */
	public RunInformation[] getCurrentRunInformation() throws CoordinatorIdleException, java.rmi.RemoteException;

	/**
	 * Is this coordinator busy?
	 * @return True if the coordinator is busy
	 * @throws java.rmi.RemoteException
	 */
    public boolean isBusy() throws java.rmi.RemoteException;

	/**
	 * Is this run in progress?
	 * @param runId
	 * @return
	 * @throws java.rmi.RemoteException
	 */
    public boolean isRunInProgress(RunUID runId) throws java.rmi.RemoteException;

	/**
	 * Restart the coordinator
	 *
	 * @throws java.rmi.RemoteException
	 */
	public void restart() throws java.rmi.RemoteException;

	/**
	 * Shutdown the coordinator
	 *
	 * @throws java.rmi.RemoteException
	 */
	public void shutdown() throws java.rmi.RemoteException;
}
