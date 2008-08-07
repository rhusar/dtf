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
 *
 * $Id$
 */
package org.jboss.dtf.testframework.local;

import org.jboss.dtf.testframework.utils.logging.LoggingService;
import org.jboss.dtf.testframework.utils.logging.exception.LoggingServiceException;
import org.jboss.dtf.testframework.testnode.RunUID;

/**
 * A lightweight logger for the in-process version of the DTF. Most methods do nothing as they are never called.
 * For the most part this class exists only to pass on test outcome notifications to the LocalTestManager.
 * Note that whilst this logging approach is fine for JavaTaskRunner it will fail for UnitTaskRunner since it's
 * not got a listener for remote connections.
 *
 * @author Jonathan Halliday (jonathan.halliday@redhat.com)
 */
public class LocalLoggingService implements LoggingService
{
    private static LocalTestManager localTestManager;

    private static final Object taskCountLock = new Object();
    private static int taskCount;

    public static LocalTestManager getLocalTestManager() {
        return localTestManager;
    }

    public static void setLocalTestManager(LocalTestManager testManager) {
        localTestManager = testManager;
    }

    public static int getTaskCount() {
        synchronized (taskCountLock) {
            return taskCount;
        }
    }

    public static void resetTraskCount() {
        synchronized (taskCountLock) {
            taskCount = 0;
        }
    }

    /////////////

    public LocalLoggingService() {
    }

    public void initialise(String loggerURL) throws LoggingServiceException
    {
        // System.out.println("LocalLoggingService.initialise");
    }

    public RunUID initiateTestRun(String testDefinitionURL, String testSelectionURL, String softwareVersion, String distributionList) throws LoggingServiceException
    {
        System.out.println("LocalLoggingService.initiateTestRun");
        return null;
    }

    public RunUID initiateTestRun(String softwareVersion, String distributionList) throws LoggingServiceException
    {
        System.out.println("LocalLoggingService.initiateTestRun");
        return null;
    }

    public boolean testRunComplete(RunUID runUID) throws LoggingServiceException
    {
        System.out.println("LocalLoggingService.testRunComplete");
        return false;
    }

    public boolean logResult(String result, String taskName, String testName, RunUID runUID, String taskPermutationCode, String testPermutationCode) throws LoggingServiceException
    {
        System.out.println("LocalLoggingService.logResult: "+result+" from "+testName+" "+taskName);

        synchronized(taskCountLock) {
            taskCount--;
        }

        if(localTestManager != null) {
            localTestManager.logTaskResult(testName, taskName, result);
        }

        return false;
    }

    public boolean logTestRunInformation(String information, String taskName, String testName, RunUID runUID, String taskPermutationCode, String testPermutationCode) throws LoggingServiceException
    {
        System.out.println("LocalLoggingService.logTestRunInformation");
        return false;
    }

    public boolean logInformation(String information, String taskName, String testName, RunUID runUID, String taskPermutationCode, String testPermutationCode) throws LoggingServiceException
    {
        System.out.println("LocalLoggingService.logInformation");
        return false;
    }

    public boolean initiateTest(String testName, RunUID runUID, String permutationCode, int numberOfTasks) throws LoggingServiceException
    {
        System.out.println("LocalLoggingService.initiateTest");
        return false;
    }

    public boolean logTestInformation(String testName, RunUID runUID, String permutationCode, String information) throws LoggingServiceException
    {
        System.out.println("LocalLoggingService.logTestInformation");
        return false;
    }

    public boolean initiateTask(String testName, RunUID runUID, String taskName, String taskPermutationCode, String testPermutationCode) throws LoggingServiceException
    {
        System.out.println("LocalLoggingService.initiateTask: "+testName+" "+runUID+" "+taskName+" "+taskPermutationCode+" "+testPermutationCode);

        synchronized(taskCountLock) {
            taskCount++;
        }

        return false;
    }

    public boolean testComplete(String testName, RunUID runUID, String permutationCode) throws LoggingServiceException
    {
        System.out.println("LocalLoggingService.testComplete");
        return false;
    }

    public boolean logTimeout(String testName, RunUID runUID, String permutationCode) throws LoggingServiceException
    {
        System.out.println("LocalLoggingService.logTimeout");
        return false;
    }
}