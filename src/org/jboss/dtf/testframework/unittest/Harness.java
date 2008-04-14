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
// $Id: Harness.java 170 2008-03-25 18:59:26Z jhalliday $

package org.jboss.dtf.testframework.unittest;

import org.jboss.dtf.testframework.utils.logging.LoggingService;
import org.jboss.dtf.testframework.utils.logging.LoggingFactory;
import org.jboss.dtf.testframework.testnode.RunUID;

import org.jboss.dtf.testframework.utils.IORStore;
import org.jboss.dtf.testframework.utils.DistributedIORStore;

public class Harness implements HarnessInterface
{
	public static final int RETURN_VALUE_CLASS_NOT_FOUND = 10;
	public static final int RETURN_VALUE_GENERAL_FAILURE = 1;
	public static final int RETURN_VALUE_SUCCESS = 0;

	private IORStore				_iorStore = null;
	private RunUID 					_runId;
	private Test					_testClass;
	private String					_taskPermutationCode;
	private String					_testPermutationCode;
	private String					_testId;
	private String					_taskName;
    private String					_loggerURL;
	private LoggingService			_loggingService;

	public Harness(RunUID runId, String taskPermutationCode, String testPermutationCode, String loggerURL, String nameServiceURI, String testClass, String testId, String taskId, String[] parameters) throws Exception, java.rmi.RemoteException
	{
		_taskName = taskId; //testClass + "<" + Long.toHexString(System.currentTimeMillis()) + ">";
		_runId = runId;
		_taskPermutationCode = taskPermutationCode;
		_testPermutationCode = testPermutationCode;
		_testId = testId;

		_loggerURL = loggerURL;

		try
		{
			_loggingService = LoggingFactory.getDefaultLogger(_loggerURL);

			/*
			 * Initiate this test with the web logger
			 */
			_loggingService.initiateTask(_testId, runId, _taskName, taskPermutationCode, testPermutationCode);
		}
		catch (Exception e)
		{
			System.out.println("Failed to retrieve logger '"+_loggerURL+"'");
			e.printStackTrace(System.err);
			System.exit(RETURN_VALUE_GENERAL_FAILURE);
		}

		/*
		 * Create instance of test class and initialise the test
		 */
		try
		{
			_testClass = (Test)Class.forName(testClass).newInstance();

			_iorStore = new DistributedIORStore();
      		_iorStore.initialiseStore(nameServiceURI);
		}
		catch (ClassNotFoundException e)
		{
			_loggingService.logResult(Test.getResultText(Test.FAILURE), _taskName, _testId, _runId, _taskPermutationCode, _testPermutationCode );
			_loggingService.logTestInformation(_testId,_runId,_testPermutationCode,"Task class not found '"+_testClass+"'");
			System.out.println("Failed to load class '"+testClass+"'");
			e.printStackTrace(System.err);
			System.exit(RETURN_VALUE_CLASS_NOT_FOUND);
		}

		_testClass.initialise(_testId, _taskName, parameters, this);

		_testClass.runTest();
	}

	public void logTestRunInformation(String information)
	{
		try
		{
			_loggingService.logTestRunInformation(information, _taskName, _testClass.getTestName(), _runId, _taskPermutationCode, _testPermutationCode);
		}
		catch (Exception e)
		{
			e.printStackTrace(System.err);
			System.out.println("FATAL ERROR - Cannot log infomation for '"+_testClass.getTestName()+"' for run id. "+_runId.getUID()+" permutation '"+_testPermutationCode+"' of '"+information+"'");
		}
	}

	/**
	 * This method logs textual information to the logger.
	 * @param information The textual information to be logged.
	 */
	public void logInformation(String information)
	{
		try
		{
			System.out.println(information);
			_loggingService.logInformation(information, _taskName, _testClass.getTestName(), _runId, _taskPermutationCode, _testPermutationCode);
		}
		catch (Exception e)
		{
			e.printStackTrace(System.err);
			System.out.println("FATAL ERROR - Cannot log infomation for '"+_testClass.getTestName()+"' for run id. "+_runId.getUID()+" permutation '"+_testPermutationCode+"' of '"+information+"'");
		}
	}

	public void logResult(String result)
	{
		try
		{
			System.out.println(result);
			_loggingService.logResult(result, _taskName, _testClass.getTestName(), _runId, _taskPermutationCode, _testPermutationCode);
		}
		catch (Exception e)
		{
			System.out.println("FATAL ERROR - Cannot log result for '"+_testClass.getTestName()+"' for run id. "+_runId.getUID()+" permutation '"+_testPermutationCode+"' of "+result);
			e.printStackTrace(System.err);
		}
	}

	public boolean registerService(String name, String ior)
	{
		boolean returnValue;

		try
		{
			_iorStore.storeIOR(name, ior);
			returnValue = true;
		}
		catch (Exception e)
		{
			System.out.println("ERROR: "+e);
			e.printStackTrace(System.err);
			returnValue = false;
		}

		return(returnValue);
	}

	public String getService(String name) throws ServiceLookupException
	{
		String returnValue = null;

		try
		{
			returnValue = _iorStore.loadIOR(name);
		}
		catch (Exception e)
		{
			throw new ServiceLookupException("Failed to lookup '"+name+"'");
		}

		return(returnValue);
	}

	public static void main(String[] args)
	{
		/*
		 * Ensure arguents have been passed to this task
		 */
		if (args.length<4)
		{
			System.out.println("This task is ran automatically by the TestNodes");
			System.exit(1);
		}
		else
		{
			System.out.println("Distributed Unit Test Harness v0.1");

			/*
			 * Parameter 0   = RunUID
			 * Parameter 1   = TaskPermutationCode
			 * Parameter 2   = TestPermutationCode
			 * Parameter 3   = Logger URL
			 * Parameter 4   = Name Service URI
			 * Parameter 5   = Test Class
			 * Parameter 6   = Test Id.
             * Parameter 7   = Task Id.
			 * Parameter 8-n = Parameters to test
			 */

			 /*
			  * If parameters have been passed to pass to the test
			  * then package them into an array with a fake first
			  * entry.  This allows the parameters to be used in
			  * place of args like in main.
			  */
			String parameters[] = null;

			if (args.length>=9)
			{
				parameters = new String[args.length-8];

				for (int paramCount=0,argCount=8;argCount<args.length;argCount++,paramCount++)
				{
					parameters[paramCount]= args[argCount];
				}
			}
           	else
           	{
           		parameters = new String[0];
           	}

           	try
           	{
				new Harness(new RunUID(Long.parseLong(args[0])), args[1], args[2], args[3], args[4], args[5], args[6], args[7], parameters);
			}
			catch (Exception e)
			{
				e.printStackTrace();
				System.exit(RETURN_VALUE_GENERAL_FAILURE);
			}

			System.exit(RETURN_VALUE_SUCCESS);
		}
	}
}
