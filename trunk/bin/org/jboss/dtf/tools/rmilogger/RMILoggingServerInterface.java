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
 * $Id: RMILoggingServerInterface.java 170 2008-03-25 18:59:26Z jhalliday $
 */
package org.jboss.dtf.tools.rmilogger;

import org.jboss.dtf.testframework.utils.logging.exception.LoggingServiceException;
import org.jboss.dtf.testframework.testnode.RunUID;

import java.rmi.RemoteException;

public interface RMILoggingServerInterface extends java.rmi.Remote
{
	public RunUID initiateTestRun(String testDefinitionURL,
								  String testSelectionURL,
								  String softwareVersion,
								  String distributionList) throws LoggingServiceException, RemoteException;

	public RunUID initiateTestRun(String softwareVersion,
								  String distributionList) throws LoggingServiceException, RemoteException;

	public boolean testRunComplete(RunUID runUID) throws LoggingServiceException, RemoteException;

	public boolean logResult(String result,
							 String taskName,
							 String testName,
							 RunUID runUID,
							 String taskPermutationCode,
							 String testPermutationCode) throws LoggingServiceException, RemoteException;

	public boolean logTestRunInformation(String information,
										 String taskName,
										 String testName,
										 RunUID runUID,
										 String taskPermutationCode,
										 String testPermutationCode) throws LoggingServiceException, RemoteException;

	public boolean logInformation(String information,
								  String taskName,
								  String testName,
								  RunUID runUID,
								  String taskPermutationCode,
								  String testPermutationCode) throws LoggingServiceException, RemoteException;

	public boolean initiateTest(String testName,
								RunUID runUID,
								String permutationCode,
								int numberOfTasks) throws LoggingServiceException, RemoteException;

	public boolean logTestInformation(String testName,
									  RunUID runUID,
									  String permutationCode,
									  String information) throws LoggingServiceException, RemoteException;

	public boolean initiateTask(String testName,
								RunUID runUID,
								String taskName,
								String taskPermutationCode,
								String testPermutationCode) throws LoggingServiceException, RemoteException;

	public boolean testComplete(String testName,
								RunUID runUID,
								String permutationCode) throws LoggingServiceException, RemoteException;

	public boolean logTimeout(String testName,
							  RunUID runUID,
							  String permutationCode) throws LoggingServiceException, RemoteException;
}
