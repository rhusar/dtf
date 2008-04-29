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
package org.jboss.dtf.testframework.testnode;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.BufferedInputStream;
import java.net.URL;

import org.jboss.dtf.testframework.utils.logging.LoggingService;
import org.jboss.dtf.testframework.utils.logging.LoggingFactory;
import org.jboss.dtf.testframework.utils.logging.exception.LoggingServiceException;
import org.jboss.dtf.testframework.utils.logging.plugins.JDBCLoggingServicePlugin;

/*
 * Copyright (C) 1998, 1999, 2000, 2001, 2002, 2003
 *
 * Arjuna Technologies Ltd.
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: JUnitReportParser.java 170 2008-03-25 18:59:26Z jhalliday $
 */

public class JUnitReportParser extends TaskRunner
{
    private final static String TEST_REPORT_PREFIX = "TEST-";
    private final static String TEST_REPORT_SUFFIX = ".xml";

    private final static String TESTSUITE_TAG = "testsuite";
    private final static String TESTCASE_TAG = "testcase";
    private final static String TESTSUITE_NAME_ATTRIBUTE = "name";
    private final static String TESTCASE_NAME_ATTRIBUTE = "name";
    private final static String TESTSUITE_PACKAGE_ATTRIBUTE = "package";
    private final static String TESTSUITE_ERRORS_ATTRIBUTE = "errors";
    private final static String TESTSUITE_FAILURES_ATTRIBUTE = "failures";
    private final static String ERROR_TAG = "error";
    private final static String ERROR_MESSAGE_ATTRIBUTE = "message";
    private final static String FAILURE_TAG = "failure";
    private final static String FAILURE_MESSAGE_ATTRIBUTE = "message";

    private boolean complete = false;

    /**
     * This is the main body of the task runner.  Within this method
     * the task runner can start the task and monitor its state.
     *
     * @throws Exception
     */
    public void runTask() throws Exception
    {
        this.indicateTaskIsRunning();

        System.out.println("Parsing test output in directory: "+_parameters[0]);

        File outputDir = new File(_parameters[0]);
        File files[] = outputDir.listFiles();

		if ( files != null )
		{
			for (int count=0;count<files.length;count++)
			{
				if ( files[count].getName().startsWith(TEST_REPORT_PREFIX) && files[count].getName().endsWith(TEST_REPORT_SUFFIX) )
				{
					System.out.println("Parsing file URL: "+files[count].toURL());
					parseTestSuites(files[count].toURL());
				}
			}
		}

        _finished = true;
        this.readySignalled();

        /*
         * If there is a listener registered then
         * inform the listener that the task has finished.
         */
        if (_listener!=null)
        {
            _listener.taskFinished( _taskId, _associatedTestNode, _testPermutationCode, true );
        }
    }

	/**
	 * Retrieves the logging service to be used by the task runner to log
	 * results.
	 *
	 * @return A reference to the logging service to use to log results.
	 */
	public LoggingService getLoggingService()
	{
		try
		{
			return LoggingFactory.getLogger(JDBCLoggingServicePlugin.class.getName(),"jdbc/ResultsDB");
		}
		catch (LoggingServiceException e)
		{
			e.printStackTrace(System.err);
		}
		return null;
	}

    protected synchronized boolean parseTestSuites(URL testSuiteFile)
    {
        boolean success = true;

        try
        {
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new BufferedInputStream(testSuiteFile.openStream()));
            Element testSuiteElement = doc.getDocumentElement();

            String testSuiteName = testSuiteElement.getAttributes().getNamedItem(TESTSUITE_NAME_ATTRIBUTE).getNodeValue();
            int numberOfTestSuiteErrors = Integer.parseInt(testSuiteElement.getAttributes().getNamedItem(TESTSUITE_ERRORS_ATTRIBUTE).getNodeValue());
            int numberOfTestSuiteFailures= Integer.parseInt(testSuiteElement.getAttributes().getNamedItem(TESTSUITE_FAILURES_ATTRIBUTE).getNodeValue());

            NodeList testCaseElements = testSuiteElement.getElementsByTagName(TESTCASE_TAG);

            /**
             * Initiate the test using the test suite name as the test name and the test case elements as the tasks
             */
            getLoggingService().initiateTest(testSuiteName, _runId, _testPermutationCode, testCaseElements.getLength());
            getLoggingService().logTestInformation(testSuiteName, _runId, _testPermutationCode,"Number of errors: "+numberOfTestSuiteErrors+", Number of failures: "+numberOfTestSuiteFailures);

            for (int caseCount=0;caseCount<testCaseElements.getLength();caseCount++)
            {
                Element testCaseElement = (Element)testCaseElements.item(caseCount);
                String testCaseName = testCaseElement.getAttributes().getNamedItem(TESTCASE_NAME_ATTRIBUTE).getNodeValue();

                getLoggingService().initiateTask(testSuiteName, _runId, testCaseName, _taskPermutationCode, _testPermutationCode);

                NodeList childNodes = testCaseElement.getChildNodes();

                getLoggingService().logResult("Passed", testCaseName, testSuiteName, _runId, _taskPermutationCode, _testPermutationCode);

                for (int childCount=0;childCount<childNodes.getLength();childCount++)
                {
                    Node childElement = childNodes.item(childCount);

                    if ( childElement.getNodeName().equals(ERROR_TAG) )
                    {
                        getLoggingService().logResult("Failed", testCaseName, testSuiteName, _runId, _taskPermutationCode, _testPermutationCode);

                        Node messageNode = childElement.getFirstChild();

                        if ( messageNode != null )
                        {
                            String message = messageNode.getNodeValue();

                            if ( message != null )
                            {
                                getLoggingService().logInformation(message, testCaseName, testSuiteName, _runId, _taskPermutationCode, _testPermutationCode);
                            }
                        }
                    }
                    else
                    if ( childElement.getNodeName().equals(FAILURE_TAG) )
                    {
                        getLoggingService().logResult("Failed", testCaseName, testSuiteName, _runId, _taskPermutationCode, _testPermutationCode);

                        Node messageNode = childElement.getAttributes().getNamedItem(FAILURE_MESSAGE_ATTRIBUTE);

                        if ( messageNode != null )
                        {
                            String message = messageNode.getNodeValue();

                            if ( message != null )
                            {
                                getLoggingService().logInformation(message, testCaseName, testSuiteName, _runId, _taskPermutationCode, _testPermutationCode);
                            }
                        }
                    }
                }
            }

            getLoggingService().testComplete(testSuiteName, _runId, _testPermutationCode);
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);

            success = false;
        }

        complete = true;

        return success;
    }

    /**
     * Called by the framework when it wishes to terminate
     * the task this runner is running.
     *
     * @return True if the task was successfully terminated.
     */
    public boolean terminate()
    {
        System.out.println("Cannot terminate - doesn't spawn a task");
        return true;
    }

    /**
     * Called by the framework when it wishes to wait for
     * the task to finish.  This method <u>MUST</u> block
     * until the task has finished. If a method is called
     * which could throw an InterruptedException then let
     * this be thrown by the method and it will be handled
     * correctly by the test node.
     *
     * @throws InterruptedException
     */
    public synchronized void waitFor() throws InterruptedException
    {
        if ( !complete)
        {
            wait();
        }
    }
}
