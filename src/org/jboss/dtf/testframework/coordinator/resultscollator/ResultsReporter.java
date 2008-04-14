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
// $Id: ResultsReporter.java 170 2008-03-25 18:59:26Z jhalliday $

package org.jboss.dtf.testframework.coordinator.resultscollator;

import org.jboss.dtf.testframework.testnode.TaskIdInterface;
import org.jboss.dtf.testframework.testnode.RunUID;

import java.util.ArrayList;

/**
 * This class is used by the TestNodes to communicate test results back to the
 * coordinator once a test has finished.
 */
public abstract class ResultsReporter extends Thread
{
	private ArrayList				_queue = new ArrayList();
	protected short					_serviceId = -1;
	protected int                   _port;
	protected String                _ipAddress;

	public ResultsReporter(	String 	ipAddress,
							int 	port,
							short	serviceId) throws java.net.UnknownHostException,
													  java.io.IOException
	{
	    /*
	     * Store initialising variables for later use
	     */
	    _ipAddress = ipAddress;
	    _port = port;
		_serviceId = serviceId;

		this.setPriority(Thread.MIN_PRIORITY);
		start();
	}

	public synchronized void addResults(RunUID runUID, String permutationCode, TaskIdInterface taskId, String directory, String testId, String taskName)
	{
        /*
         * Queue results for processing later
         */
	    try
	    {
	        System.out.println("Queuing permutation code '"+permutationCode+"' task id. "+taskId.dumpInfo()+" from directory "+directory+" for test '"+testId+"'");
	    }
	    catch (Exception e)
	    {
	    }

		_queue.add(new ResultDefinition(runUID, permutationCode, taskId, directory, testId, taskName));
		notifyAll();
	}

	public synchronized void run()
	{
		while (true)
		{
			try
			{
				wait();
			}
			catch (InterruptedException e)
			{
			}

			/*
			 * While there are elements in the queue process them
			 */
			while (_queue.size()>0)
			{
				ResultDefinition resultDef = (ResultDefinition)_queue.get(0);
				_queue.remove(0);

				sendResultDefinition(resultDef);
			}
		}
	}

	abstract void sendResultDefinition(ResultDefinition resDef);


	protected class ResultDefinition
	{
	    public long             _runId;
		public TaskIdInterface 	_taskId;
		public String 	        _directory;
		public String           _permutationCode;
		public String           _testId;
        public String           _taskName;

		public ResultDefinition(RunUID runId, String permutationCode, TaskIdInterface taskId, String directory, String testId, String taskName)
		{
		    _runId = runId.getUID();
			_taskId = taskId;
			_directory = directory;
			_permutationCode = permutationCode;
			_testId = testId;
            _taskName = taskName;
		}
	}
}
