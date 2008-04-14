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
 * $Id: RMILoggingServicePlugin.java 170 2008-03-25 18:59:26Z jhalliday $
 */
package org.jboss.dtf.tools.rmilogger;

import org.jboss.dtf.testframework.utils.logging.LoggingService;
import org.jboss.dtf.testframework.utils.logging.exception.LoggingServiceException;
import org.jboss.dtf.testframework.testnode.RunUID;
import org.jboss.dtf.tools.rmilogger.RMILoggingServer;
import org.jboss.dtf.tools.rmilogger.RMILoggingServerInterface;

import java.rmi.Naming;
import java.rmi.RemoteException;

public class RMILoggingServicePlugin implements LoggingService
{
	private final static String RMI_LOGGING_SERVICE_RMI_NAME_PROPERTY = "org.jboss.dtf.testframework.utils.logging.plugins.RMILoggingServer.name";
	private final static String RMI_LOGGING_SERVICE_RMI_NAME_PROPERTY_DEFAULT = "//localhost"+RMILoggingServer.RMI_LOGGING_SERVICE_NAME;

	private RMILoggingServerInterface _logger = null;

	public void initialise(String loggerURL) throws LoggingServiceException
	{
		String remoteReference = System.getProperty(RMI_LOGGING_SERVICE_RMI_NAME_PROPERTY, loggerURL);

		if ( remoteReference == null )
		{
			remoteReference = RMI_LOGGING_SERVICE_RMI_NAME_PROPERTY_DEFAULT;
		}

		try
		{
			_logger = (RMILoggingServerInterface)Naming.lookup(loggerURL);
		}
		catch (Exception e)
		{
			throw new LoggingServiceException("Failed to find RMI logging server ("+remoteReference+"):"+e);
		}
	}

	public RunUID initiateTestRun(String testDefinitionURL,
								  String testSelectionURL,
								  String softwareVersion,
								  String distributionList) throws LoggingServiceException
	{
		RunUID returnValue = null;

		try
		{
			returnValue = _logger.initiateTestRun(testDefinitionURL, testSelectionURL, softwareVersion, distributionList);
		}
		catch (RemoteException e)
		{
			throw new LoggingServiceException("Unexpected remote exception: "+e);
		}

		return returnValue;
	}

	public RunUID initiateTestRun(String softwareVersion,
								  String distributionList) throws LoggingServiceException
	{
		RunUID returnValue = null;

		try
		{
			returnValue = _logger.initiateTestRun(softwareVersion, distributionList);
		}
		catch (RemoteException e)
		{
			throw new LoggingServiceException("Unexpected remote exception: "+e);
		}

		return returnValue;
	}

	public boolean testRunComplete(RunUID runUID) throws LoggingServiceException
	{
		boolean returnValue = false;

		try
		{
			returnValue = _logger.testRunComplete(runUID);
		}
		catch (RemoteException e)
		{
			throw new LoggingServiceException("Unexpected remote exception: "+e);
		}

		return returnValue;
	}

	public boolean logResult(String result,
							 String taskName,
							 String testName,
							 RunUID runUID,
							 String taskPermutationCode,
							 String testPermutationCode) throws LoggingServiceException
	{
		boolean returnValue = false;

		try
		{
			returnValue = _logger.logResult(result, taskName, testName, runUID, taskPermutationCode, testPermutationCode);
		}
		catch (RemoteException e)
		{
			throw new LoggingServiceException("Unexpected remote exception: "+e);
		}

		return returnValue;
	}

	public boolean logTestRunInformation(String information,
										 String taskName,
										 String testName,
										 RunUID runUID,
										 String taskPermutationCode,
										 String testPermutationCode) throws LoggingServiceException
	{
		boolean returnValue = false;

		try
		{
			returnValue = _logger.logTestRunInformation(information, taskName, testName, runUID, taskPermutationCode, testPermutationCode);
		}
		catch (RemoteException e)
		{
			throw new LoggingServiceException("Unexpected remote exception: "+e);
		}

		return returnValue;
	}

	public boolean logInformation(String information,
								  String taskName,
								  String testName,
								  RunUID runUID,
								  String taskPermutationCode,
								  String testPermutationCode) throws LoggingServiceException
	{
		boolean returnValue = false;

		try
		{
			returnValue = _logger.logInformation(information, taskName, testName, runUID, taskPermutationCode, testPermutationCode);
		}
		catch (RemoteException e)
		{
			throw new LoggingServiceException("Unexpected remote exception: "+e);
		}

		return returnValue;
	}

	public boolean initiateTest(String testName,
								RunUID runUID,
								String permutationCode,
								int numberOfTasks) throws LoggingServiceException
	{
		boolean returnValue = false;

		try
		{
			returnValue = _logger.initiateTest(testName, runUID, permutationCode, numberOfTasks);
		}
		catch (RemoteException e)
		{
			throw new LoggingServiceException("Unexpected remote exception: "+e);
		}

		return returnValue;
	}

	public boolean logTestInformation(String testName,
									  RunUID runUID,
									  String permutationCode,
									  String information) throws LoggingServiceException
	{
		boolean returnValue = false;

		try
		{
			returnValue = _logger.logTestInformation(testName, runUID, permutationCode, information);
		}
		catch (RemoteException e)
		{
			throw new LoggingServiceException("Unexpected remote exception: "+e);
		}

		return returnValue;
	}

	public boolean initiateTask(String testName,
								RunUID runUID,
								String taskName,
								String taskPermutationCode,
								String testPermutationCode) throws LoggingServiceException
	{
		boolean returnValue = false;

		try
		{
			returnValue = _logger.initiateTask(testName, runUID, taskName, taskPermutationCode, testPermutationCode);
		}
		catch (RemoteException e)
		{
			throw new LoggingServiceException("Unexpected remote exception: "+e);
		}

		return returnValue;
	}

	public boolean testComplete(String testName,
								RunUID runUID,
								String permutationCode) throws LoggingServiceException
	{
		boolean returnValue = false;

		try
		{
			returnValue = _logger.testComplete(testName, runUID, permutationCode);
		}
		catch (RemoteException e)
		{
			throw new LoggingServiceException("Unexpected remote exception: "+e);
		}

		return returnValue;
	}

	public boolean logTimeout(String testName,
							  RunUID runUID,
							  String permutationCode) throws LoggingServiceException
	{
		boolean returnValue = false;

		try
		{
			returnValue = _logger.logTimeout(testName, runUID, permutationCode);
		}
		catch (RemoteException e)
		{
			throw new LoggingServiceException("Unexpected remote exception: "+e);
		}

		return returnValue;
	}
}
