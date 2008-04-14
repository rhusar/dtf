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
 * $Id: WebLoggingServicePlugin.java 170 2008-03-25 18:59:26Z jhalliday $
 */
package org.jboss.dtf.testframework.utils.logging.plugins;

import org.jboss.dtf.testframework.testnode.RunUID;
import org.jboss.dtf.testframework.utils.XMLUtils;
import org.jboss.dtf.testframework.utils.HTTPRequestGenerator;
import org.jboss.dtf.testframework.utils.logging.LoggingService;
import org.jboss.dtf.testframework.utils.logging.exception.LoggingServiceException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.util.HashMap;
import java.util.GregorianCalendar;
import java.util.Calendar;
import java.net.URL;
import java.io.ByteArrayInputStream;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class WebLoggingServicePlugin implements LoggingService
{
	private static final String WEB_LOGGING_SERVICE_URL = "org.jboss.dtf.testframework.utils.logging.plugins.WebLoggingServicePlugin.URL";

	private static final String INITIATE_TEST_RUN_SCRIPT_FILENAME = "initiate_testrun.jsp";
	private static final String TEST_RUN_COMPLETE_SCRIPT_FILENAME = "testrun_complete.jsp";
	private static final String LOG_RESULT_SCRIPT_FILENAME = "log_result.jsp";
	private static final String LOG_INFORMATION_SCRIPT_FILENAME = "log_information.jsp";
	private static final String INITIATE_TEST_SCRIPT_FILENAME = "initiate_test.jsp";
	private static final String INITIATE_TASK_SCRIPT_FILENAME = "initiate_task.jsp";
	private static final String TEST_COMPLETE_SCRIPT_FILENAME = "test_complete.jsp";
	private static final String LOG_TEST_INFORMATION_SCRIPT_FILENAME = "log_test_information.jsp";
	private static final String LOG_TEST_RUN_INFORMATION_SCRIPT_FILENAME = "log_test_run_information.jsp";
	private static final String TEST_TIMEDOUT_SCRIPT_FILENAME = "log_timeout.jsp";

	private String _baseLoggingURL = null;

	public void initialise(String loggerURL) throws LoggingServiceException
	{
		_baseLoggingURL = System.getProperty(WEB_LOGGING_SERVICE_URL, loggerURL);
	}

	public void startBatch()
	{
		// Not supported
	}

	public void endBatch()
	{
		// Not supported
	}

	public RunUID initiateTestRun(String testDefinitionURL,
								  String testSelectionURL,
								  String softwareVersion,
								  String distributionList) throws LoggingServiceException
	{
		String loggingURL = new String(_baseLoggingURL);

		HashMap dataMap = new HashMap();
		String definitionDescription = "", selectionDescription = "";
		RunUID runUID = null;
		if (!loggingURL.endsWith("/"))
		{
			loggingURL += "/";
		}
		/*
																																																																																																																																																																																																																																																											 * Append the initiate testrun script filename
																																																																																																																																																																																																																																																											 */
		loggingURL += INITIATE_TEST_RUN_SCRIPT_FILENAME;
		if (testDefinitionURL != null)
		{
			try
			{
				DocumentBuilder xmlBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
				Document doc = xmlBuilder.parse(new URL(testDefinitionURL).openStream());
				Element root = doc.getDocumentElement();
				Node descriptionNode = XMLUtils.getFirstNamedChild(root, "description");
				if ( (descriptionNode != null) && (descriptionNode.getFirstChild() != null) )
				{
					definitionDescription = descriptionNode.getFirstChild().getNodeValue();
				}
			}
			catch (Exception e)
			{
				System.out.println("Error trying to parse the test definition xml file");
				System.exit(1);
			}
		}
		else
		{
			testDefinitionURL = "";
		}

		if (testSelectionURL != null)
		{
			try
			{
				DocumentBuilder xmlBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
				Document doc = xmlBuilder.parse(new URL(testSelectionURL).openStream());
				Element root = doc.getDocumentElement();
				Node descriptionNode = XMLUtils.getFirstNamedChild(root, "description");
				if ( (descriptionNode != null) && (descriptionNode.getFirstChild() != null) )
					selectionDescription = descriptionNode.getFirstChild().getNodeValue();
			}
			catch (Exception e)
			{
				System.out.println("Error trying to parse the test definition xml file");
				e.printStackTrace(System.err);
				System.exit(1);
			}
		}
		else
		{
			testDefinitionURL = "";
		}

		dataMap.put("TestDefinitionURL", testDefinitionURL);
		dataMap.put("TestDefinitionDescription", definitionDescription != null ? definitionDescription : "");
		dataMap.put("TestSelectionURL", testSelectionURL);
		dataMap.put("TestSelectionDescription", selectionDescription != null ? selectionDescription : "");
		dataMap.put("SoftwareVersion", softwareVersion);
		dataMap.put("DistributionList", distributionList != null ? distributionList : "");

		String returnData = HTTPRequestGenerator.postRequest(loggingURL, dataMap);

		if (returnData == null)
			throw new LoggingServiceException("Error posting request to '" + loggingURL + "'");
		try
		{
			DocumentBuilder xmlBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document doc = xmlBuilder.parse(new ByteArrayInputStream(returnData.getBytes()));
			Element root = doc.getDocumentElement();
			runUID = new RunUID(Long.parseLong(root.getAttributeNode("runuid").getNodeValue()));
			Node creationNode = XMLUtils.getFirstNamedChild(root, "run_creation");

			if ((creationNode != null) && (!new Boolean(creationNode.getAttributes().getNamedItem("success").getNodeValue()).booleanValue()))
			{
				runUID = null;
			}
		}
		catch (Exception e)
		{
			System.out.println("Error trying to parse the resultant xml file from the web server");
			e.printStackTrace(System.err);
			System.exit(1);
		}
		return (runUID);
	}

	public RunUID initiateTestRun(String softwareVersion,
								  String distributionList) throws LoggingServiceException
	{
		String loggingURL = new String(_baseLoggingURL);

		HashMap dataMap = new HashMap();
		RunUID runUID = null;
		if (!loggingURL.endsWith("/"))
		{
			loggingURL += "/";
		}
		/*
																																																																																																																																																																																																																																																											 * Append the initiate testrun script filename
																																																																																																																																																																																																																																																											 */
		loggingURL += INITIATE_TEST_RUN_SCRIPT_FILENAME;
		dataMap.put("TestDefinitionURL", "");
		dataMap.put("TestDefinitionDescription", "");
		dataMap.put("TestSelectionURL", "");
		dataMap.put("TestSelectionDescription", "");
		dataMap.put("SoftwareVersion", softwareVersion);
		dataMap.put("DistributionList", distributionList);
		String returnData = HTTPRequestGenerator.postRequest(loggingURL, dataMap);
		if (returnData == null)
			throw new LoggingServiceException("Error posting request to '" + loggingURL + "'");
		try
		{
			DocumentBuilder xmlBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document doc = xmlBuilder.parse(new ByteArrayInputStream(returnData.getBytes()));
			Element root = doc.getDocumentElement();
			runUID = new RunUID(Long.parseLong(root.getAttributeNode("runuid").getNodeValue()));
			Node creationNode = XMLUtils.getFirstNamedChild(root, "run_creation");
			if ((creationNode != null) && (!new Boolean(creationNode.getAttributes().getNamedItem("success").getNodeValue()).booleanValue()))
			{
				runUID = null;
			}
		}
		catch (Exception e)
		{
			System.out.println("Error trying to parse the resultant xml file from the web server");
			e.printStackTrace(System.err);
			System.exit(1);
		}
		return (runUID);
	}

	public boolean testRunComplete(RunUID runUID) throws LoggingServiceException
	{
		String loggingURL = new String(_baseLoggingURL);

		HashMap dataMap = new HashMap();
		if (!loggingURL.endsWith("/"))
		{
			loggingURL += "/";
		}
		/*
		 * Append the test run complete script filename
		 */
		loggingURL += TEST_RUN_COMPLETE_SCRIPT_FILENAME;
		dataMap.put("RunID", Long.toString(runUID.getUID()));
		String returnData = HTTPRequestGenerator.postRequest(loggingURL, dataMap);

		if (returnData == null)
			throw new LoggingServiceException("Error posting request to '" + loggingURL + "'");

		try
		{
			DocumentBuilder xmlBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document doc = xmlBuilder.parse(new ByteArrayInputStream(returnData.getBytes()));
			Element root = doc.getDocumentElement();
			Node creationNode = XMLUtils.getFirstNamedChild(root, "run_complete");
			if ((creationNode != null) && (!new Boolean(creationNode.getAttributes().getNamedItem("success").getNodeValue()).booleanValue()))
			{
				runUID = null;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace(System.err);
			throw new LoggingServiceException("Error trying to parse the resultant xml file from the web server");
		}
		return (true);
	}

	public boolean logResult(String result,
							 String taskName,
							 String testName,
							 RunUID runUID,
							 String taskPermutationCode,
							 String testPermutationCode) throws LoggingServiceException
	{
		String loggingURL = new String(_baseLoggingURL);

		HashMap dataMap = new HashMap();
		boolean returnValue = true;
		if (!loggingURL.endsWith("/"))
		{
			loggingURL += "/";
		}

		loggingURL += LOG_RESULT_SCRIPT_FILENAME;
		dataMap.put("RunID", Long.toString(runUID.getUID()));
		dataMap.put("Result", result);
		dataMap.put("TaskName", taskName);
		dataMap.put("TestName", testName);
		dataMap.put("TaskPermutationCode", taskPermutationCode);
		dataMap.put("TestPermutationCode", testPermutationCode);
		String returnData = HTTPRequestGenerator.postRequest(loggingURL, dataMap);
		if (returnData == null)
			throw new LoggingServiceException("Error posting request to '" + loggingURL + "'");
		try
		{
			DocumentBuilder xmlBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document doc = xmlBuilder.parse(new ByteArrayInputStream(returnData.getBytes()));
			Element root = doc.getDocumentElement();
			Node loggedNode = XMLUtils.getFirstNamedChild(root, "log_update_state");
			if ((loggedNode != null) && (!new Boolean(loggedNode.getAttributes().getNamedItem("success").getNodeValue()).booleanValue()))
			{
				returnValue = false;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace(System.err);
			throw new LoggingServiceException("Error trying to parse the resultant xml file from the web server");
		}
		return (returnValue);
	}

	public boolean logTestRunInformation(String information,
										 String taskName,
										 String testName,
										 RunUID runUID,
										 String taskPermutationCode,
										 String testPermutationCode) throws LoggingServiceException
	{
		String loggingURL = new String(_baseLoggingURL);

		HashMap dataMap = new HashMap();
		boolean result = false;
		if (!loggingURL.endsWith("/"))
		{
			loggingURL += "/";
		}
		/*
																																																																																																																																																																																																																																																											 * Append the test run complete script filename
																																																																																																																																																																																																																																																											 */
		loggingURL += LOG_TEST_RUN_INFORMATION_SCRIPT_FILENAME;
		dataMap.put("RunID", Long.toString(runUID.getUID()));
		dataMap.put("Information", information);
		dataMap.put("TaskName", taskName);
		dataMap.put("TestName", testName);
		dataMap.put("TaskPermutationCode", taskPermutationCode);
		dataMap.put("TestPermutationCode", testPermutationCode);
		String returnData = HTTPRequestGenerator.postRequest(loggingURL, dataMap);
		if (returnData == null)
			throw new LoggingServiceException("Error posting request to '" + loggingURL + "'");
		try
		{
			DocumentBuilder xmlBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document doc = xmlBuilder.parse(new ByteArrayInputStream(returnData.getBytes()));
			Element root = doc.getDocumentElement();
			Node loggedNode = XMLUtils.getFirstNamedChild(root, "logged");
			if ((loggedNode != null) && (new Boolean(loggedNode.getAttributes().getNamedItem("success").getNodeValue()).booleanValue()))
			{
				result = true;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace(System.err);
			throw new LoggingServiceException("Error trying to parse the resultant xml file from the web server");
		}
		return (result);
	}

	public boolean logInformation(String information,
								  String taskName,
								  String testName,
								  RunUID runUID,
								  String taskPermutationCode,
								  String testPermutationCode) throws LoggingServiceException
	{
		String loggingURL = new String(_baseLoggingURL);

		HashMap dataMap = new HashMap();
		boolean result = false;
		if (!loggingURL.endsWith("/"))
		{
			loggingURL += "/";
		}
		/*
																																																																																																																																																																																																																																																											 * Append the test run complete script filename
																																																																																																																																																																																																																																																											 */
		loggingURL += LOG_INFORMATION_SCRIPT_FILENAME;
		dataMap.put("RunID", Long.toString(runUID.getUID()));
		dataMap.put("Information", information);
		dataMap.put("TaskName", taskName);
		dataMap.put("TestName", testName);
		dataMap.put("TaskPermutationCode", taskPermutationCode);
		dataMap.put("TestPermutationCode", testPermutationCode);
		String returnData = HTTPRequestGenerator.postRequest(loggingURL, dataMap);

		if (returnData == null)
			throw new LoggingServiceException("Error posting request to '" + loggingURL + "'");

		try
		{
			DocumentBuilder xmlBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document doc = xmlBuilder.parse(new ByteArrayInputStream(returnData.getBytes()));
			Element root = doc.getDocumentElement();
			Node loggedNode = XMLUtils.getFirstNamedChild(root, "log_update_state");
			if ((loggedNode != null) && (new Boolean(loggedNode.getAttributes().getNamedItem("success").getNodeValue()).booleanValue()))
			{
				result = true;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace(System.err);
			throw new LoggingServiceException("Error trying to parse the resultant xml file from the web server");
		}
		return (result);
	}

	public boolean initiateTest(String testName,
								RunUID runUID,
								String permutationCode,
								int numberOfTasks) throws LoggingServiceException
	{
		String loggingURL = new String(_baseLoggingURL);

		HashMap dataMap = new HashMap();
		boolean returnValue = true;
		if (!loggingURL.endsWith("/"))
		{
			loggingURL += "/";
		}
		/*		 * Append the test run complete script filename		 */
		loggingURL += INITIATE_TEST_SCRIPT_FILENAME;
		dataMap.put("RunID", Long.toString(runUID.getUID()));
		dataMap.put("TestName", testName);
		dataMap.put("PermutationCode", permutationCode);
		dataMap.put("NumberOfTasks", Integer.toString(numberOfTasks));
		String returnData = HTTPRequestGenerator.postRequest(loggingURL, dataMap);
		if (returnData == null)
			throw new LoggingServiceException("Error posting request to '" + loggingURL + "'");
		try
		{
			DocumentBuilder xmlBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document doc = xmlBuilder.parse(new ByteArrayInputStream(returnData.getBytes()));
			Element root = doc.getDocumentElement();
			Node loggedNode = XMLUtils.getFirstNamedChild(root, "initiated");
			if ((loggedNode != null) && (!new Boolean(loggedNode.getAttributes().getNamedItem("success").getNodeValue()).booleanValue()))
			{
				returnValue = false;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace(System.err);
			throw new LoggingServiceException("Error trying to parse the resultant xml file from the web server");
		}
		return (returnValue);
	}

	public String getDateTime()
	{
		GregorianCalendar cal = new GregorianCalendar();
		return (cal.get(Calendar.DAY_OF_MONTH) + "/" + cal.get(Calendar.MONTH) + "/" + cal.get(Calendar.YEAR) + " " + cal.get(Calendar.HOUR) + ":" + cal.get(Calendar.MINUTE) + "." + cal.get(Calendar.SECOND));
	}

	public boolean logTestInformation(String testName,
									  RunUID runUID,
									  String permutationCode,
									  String information) throws LoggingServiceException
	{
		String loggingURL = new String(_baseLoggingURL);

		HashMap dataMap = new HashMap();
		boolean returnValue = true;
		if (!loggingURL.endsWith("/"))
		{
			loggingURL += "/";
		}
		/*
																																																																																																																																																																																																																																																											 * Append the test run complete script filename
																																																																																																																																																																																																																																																											 */
		loggingURL += LOG_TEST_INFORMATION_SCRIPT_FILENAME;
		information = "[" + getDateTime() + "]:" + information;
		dataMap.put("RunID", Long.toString(runUID.getUID()));
		dataMap.put("TestName", testName);
		dataMap.put("PermutationCode", permutationCode);
		dataMap.put("Information", information);
		String returnData = HTTPRequestGenerator.postRequest(loggingURL, dataMap);
		if (returnData == null)
			throw new LoggingServiceException("Error posting request to '" + loggingURL + "'");
		try
		{
			DocumentBuilder xmlBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document doc = xmlBuilder.parse(new ByteArrayInputStream(returnData.getBytes()));
			Element root = doc.getDocumentElement();
			Node loggedNode = XMLUtils.getFirstNamedChild(root, "logged");
			if ((loggedNode != null) && (!new Boolean(loggedNode.getAttributes().getNamedItem("success").getNodeValue()).booleanValue()))
			{
				returnValue = false;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace(System.err);
			throw new LoggingServiceException("Error trying to parse the resultant xml file from the web server");
		}
		return (returnValue);
	}

	public boolean initiateTask(String testName,
								RunUID runUID,
								String taskName,
								String taskPermutationCode,
								String testPermutationCode) throws LoggingServiceException
	{
		String loggingURL = new String(_baseLoggingURL);

		HashMap dataMap = new HashMap();
		boolean returnValue = true;
		if (!loggingURL.endsWith("/"))
		{
			loggingURL += "/";
		}
		/*
																																																																																																																																																																																																																																																											 * Append the initiate test run script filename
																																																																																																																																																																																																																																																											 */
		loggingURL += INITIATE_TASK_SCRIPT_FILENAME;
		dataMap.put("RunID", Long.toString(runUID.getUID()));
		dataMap.put("TestName", testName);
		dataMap.put("TaskName", taskName);
		dataMap.put("TestPermutationCode", testPermutationCode);
		dataMap.put("TaskPermutationCode", taskPermutationCode);
		String returnData = HTTPRequestGenerator.postRequest(loggingURL, dataMap);
		if (returnData == null)
			throw new LoggingServiceException("Error posting request to '" + loggingURL + "'");
		try
		{
			DocumentBuilder xmlBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document doc = xmlBuilder.parse(new ByteArrayInputStream(returnData.getBytes()));
			Element root = doc.getDocumentElement();
			Node loggedNode = XMLUtils.getFirstNamedChild(root, "initiated");
			if ((loggedNode != null) && (!new Boolean(loggedNode.getAttributes().getNamedItem("success").getNodeValue()).booleanValue()))
			{
				returnValue = false;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace(System.err);
			throw new LoggingServiceException("Error trying to parse the resultant xml file from the web server");
		}
		return (returnValue);
	}

	public boolean testComplete(String testName,
								RunUID runUID,
								String permutationCode) throws LoggingServiceException
	{
		String loggingURL = new String(_baseLoggingURL);

		HashMap dataMap = new HashMap();
		boolean returnValue = true;
		if (!loggingURL.endsWith("/"))
		{
			loggingURL += "/";
		}
		/*
																																																																																																																																																																																																																																																											 * Append the test run complete script filename
																																																																																																																																																																																																																																																											 */
		loggingURL += TEST_COMPLETE_SCRIPT_FILENAME;
		dataMap.put("RunID", Long.toString(runUID.getUID()));
		dataMap.put("TestName", testName);
		dataMap.put("PermutationCode", permutationCode);
		String returnData = HTTPRequestGenerator.postRequest(loggingURL, dataMap);
		if (returnData == null)
			throw new LoggingServiceException("Error posting request to '" + loggingURL + "'");
		try
		{
			DocumentBuilder xmlBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document doc = xmlBuilder.parse(new ByteArrayInputStream(returnData.getBytes()));
			Element root = doc.getDocumentElement();
			Node loggedNode = XMLUtils.getFirstNamedChild(root, "test_complete");
			if ((loggedNode != null) && (!new Boolean(loggedNode.getAttributes().getNamedItem("success").getNodeValue()).booleanValue()))
			{
				returnValue = false;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace(System.err);
			throw new LoggingServiceException("Error trying to parse the resultant xml file from the web server");
		}
		return (returnValue);
	}

	public boolean logTimeout(String testName,
							  RunUID runUID,
							  String permutationCode) throws LoggingServiceException
	{
		String loggingURL = new String(_baseLoggingURL);

		HashMap dataMap = new HashMap();
		boolean returnValue = true;
		if (!loggingURL.endsWith("/"))
		{
			loggingURL += "/";
		}
		/*
																																																																																																																																																																																																																																																											 * Append the test timedout script filename
																																																																																																																																																																																																																																																											 */
		loggingURL += TEST_TIMEDOUT_SCRIPT_FILENAME;
		dataMap.put("RunID", Long.toString(runUID.getUID()));
		dataMap.put("TestName", testName);
		dataMap.put("PermutationCode", permutationCode);
		String returnData = HTTPRequestGenerator.postRequest(loggingURL, dataMap);
		if (returnData == null)
			throw new LoggingServiceException("Error posting request to '" + loggingURL + "'");
		try
		{
			DocumentBuilder xmlBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document doc = xmlBuilder.parse(new ByteArrayInputStream(returnData.getBytes()));
			Element root = doc.getDocumentElement();
			Node loggedNode = XMLUtils.getFirstNamedChild(root, "test_complete");
			if ((loggedNode != null) && (!new Boolean(loggedNode.getAttributes().getNamedItem("success").getNodeValue()).booleanValue()))
			{
				returnValue = false;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace(System.err);
			throw new LoggingServiceException("Error trying to parse the resultant xml file from the web server");
		}
		return (returnValue);
	}
}
