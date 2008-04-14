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
 * $Id: RMILoggingServer.java 170 2008-03-25 18:59:26Z jhalliday $
 */
package org.jboss.dtf.tools.rmilogger;

import org.jboss.dtf.testframework.testnode.RunUID;
import org.jboss.dtf.testframework.utils.logging.exception.LoggingServiceException;

import java.rmi.RemoteException;
import java.rmi.Naming;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;

public class RMILoggingServer extends java.rmi.server.UnicastRemoteObject implements RMILoggingServerInterface
{
	public final static String RMI_LOGGING_SERVICE_NAME = "/RMILoggingServer";

	private long	_nextRunId = 0;

	public RMILoggingServer() throws RemoteException
	{
		super();
	}

	public RunUID initiateTestRun(String testDefinitionURL,
								  String testSelectionURL,
								  String softwareVersion,
								  String distributionList) throws LoggingServiceException, RemoteException
	{
		System.out.println("Test run initiated ("+_nextRunId+"):");
		System.out.println("\tTest Definitions:"+testDefinitionURL);
		System.out.println("\t Test Selections:"+testSelectionURL);
		System.out.println("\tSoftware Version:"+softwareVersion);
		System.out.println("\t    Distribution:"+distributionList);

		return new RunUID(_nextRunId++);
	}

	public RunUID initiateTestRun(String softwareVersion,
								  String distributionList) throws LoggingServiceException, RemoteException
	{
		System.out.println("Test run initiated:");
		System.out.println("\tSoftware Version:"+softwareVersion);
		System.out.println("\t    Distribution:"+distributionList);
		return new RunUID(_nextRunId++);
	}

	public boolean testRunComplete(RunUID runUID) throws LoggingServiceException, RemoteException
	{
		System.out.println("Test run complete ("+runUID.getUID()+")");

		return true;
	}

	public boolean logResult(String result,
							 String taskName,
							 String testName,
							 RunUID runUID,
							 String taskPermutationCode,
							 String testPermutationCode) throws LoggingServiceException, RemoteException
	{
		System.out.println("Test Result ("+runUID.getUID()+"):");
		System.out.println("\t     Result:"+result);
		System.out.println("\t  Task Name:"+taskName);
		System.out.println("\t  Test Name:"+testName);
		System.out.println("\t  Task Perm:"+taskPermutationCode);
		System.out.println("\t  Test Perm:"+testPermutationCode);

		return true;
	}

	public boolean logTestRunInformation(String information,
										 String taskName,
										 String testName,
										 RunUID runUID,
										 String taskPermutationCode,
										 String testPermutationCode) throws LoggingServiceException, RemoteException
	{
		return true;
	}

	public boolean logInformation(String information,
								  String taskName,
								  String testName,
								  RunUID runUID,
								  String taskPermutationCode,
								  String testPermutationCode) throws LoggingServiceException, RemoteException
	{
		return true;
	}

	public boolean initiateTest(String testName,
								RunUID runUID,
								String permutationCode,
								int numberOfTasks) throws LoggingServiceException, RemoteException
	{
		System.out.println("Test initiated ("+runUID.getUID()+"):");
		System.out.println("\t      Test Name:"+testName);
		System.out.println("\t    Permutation:"+permutationCode);
		System.out.println("\tNumber of tasks:"+numberOfTasks);

		return true;
	}

	public boolean logTestInformation(String testName,
									  RunUID runUID,
									  String permutationCode,
									  String information) throws LoggingServiceException, RemoteException
	{
		return true;
	}

	public boolean initiateTask(String testName,
								RunUID runUID,
								String taskName,
								String taskPermutationCode,
								String testPermutationCode) throws LoggingServiceException, RemoteException
	{
		System.out.println("Task initiated ("+runUID.getUID()+"):");
		System.out.println("\tTest Name:"+testName);
		System.out.println("\tTask Name:"+taskName);
		System.out.println("\tTask Perm:"+taskPermutationCode);
		System.out.println("\tTest Perm:"+testPermutationCode);

		return true;
	}

	public boolean testComplete(String testName,
								RunUID runUID,
								String permutationCode) throws LoggingServiceException, RemoteException
	{
		System.out.println("Test complete ("+runUID.getUID()+"):");
		System.out.println("\t  Test Name:"+testName);
		System.out.println("\tPermutation:"+permutationCode);

		return true;
	}

	public boolean logTimeout(String testName,
							  RunUID runUID,
							  String permutationCode) throws LoggingServiceException, RemoteException
	{
		System.out.println("Test timedout ("+runUID.getUID()+"):");
		System.out.println("\t  Test Name:"+testName);
		System.out.println("\tPermutation:"+permutationCode);

		return true;
	}


	public static void initialiseRegistry()
	{
		try
		{
			System.out.println("Creating RMI registry on port "+Registry.REGISTRY_PORT);
			LocateRegistry.createRegistry(Registry.REGISTRY_PORT);
		}
		catch (RemoteException ex)
		{
		}
	}

	public static void main(String[] args)
	{
		System.out.println("RMI Logging Service");

		try
		{
			initialiseRegistry();

			RMILoggingServer loggingService = new RMILoggingServer();

			Naming.rebind( RMILoggingServer.RMI_LOGGING_SERVICE_NAME, loggingService );

			System.out.println("Ready");
		}
		catch (Exception e)
		{
			System.err.println("An unexpected exception was thrown: "+e);
			e.printStackTrace(System.err);
		}
	}
}
