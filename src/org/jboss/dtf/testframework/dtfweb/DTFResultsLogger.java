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
package org.jboss.dtf.testframework.dtfweb;

import org.jboss.dtf.testframework.dtfweb.utils.*;
import org.jboss.dtf.testframework.coordinator.OSProductCombination;

import java.sql.*;
import javax.servlet.*;
import javax.servlet.http.*;

import javax.naming.*;
import javax.sql.DataSource;

import java.io.File;
import java.net.InetAddress;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.StringTokenizer;

public class DTFResultsLogger
{
	private static DataSource  _pool = DBUtils.getDataSource();

	private static boolean _tablesCreated = false;

	public boolean signalTestRunComplete(HttpServletRequest request)
	{
		boolean returnValue = true;
        Connection conn = null;

		// This script is called whenever a test completes
		try
		{
			conn = _pool.getConnection();
			Statement s = conn.createStatement();
			// This script is called whenever a test run is completed

			String dateTimeComplete = new Timestamp(new java.util.Date().getTime()).toString();
			String runId = request.getParameter("RunID");

			// Update row in TestRuns table
			String updateTestRunSQL = "UPDATE TestRuns SET DateTimeFinished='"+ dateTimeComplete +"' WHERE RunId="+ runId;
			s.execute(updateTestRunSQL);

			ResultSet rs = s.executeQuery("SELECT DistributionList FROM TestRuns WHERE RunId="+ runId);
			String distributionList = null;

			if (rs.next())
			{
				distributionList = rs.getString("DistributionList");
			}
			rs.close();
			s.close();

			File f = new File(request.getRequestURI());

			emailResults(distributionList, Long.parseLong(runId), null);
		}
		catch (SQLException e)
		{
			System.out.println("Unexpected Exception - "+e.toString());
			returnValue = false;
		}
        finally
        {
            if ( conn != null )
            {
                try
                {
                    conn.close();
                }
                catch (SQLException e)
                {
                    e.printStackTrace(System.err);
                }
            }
        }

		return(returnValue);
	}

    public boolean signalTestTimedout(HttpServletRequest request)
    {
        boolean returnValue = true;
        Connection conn = null;

		// This script is called whenever a test timesout
		try
		{
			conn = _pool.getConnection();
			Statement s = conn.createStatement();
			// This script is called whenever a test timesout completed

			String runId = request.getParameter("RunID");
            String testName = request.getParameter("TestName");
            String permutationCode = request.getParameter("PermutationCode");

			// Update row in TestResults table
			String updateTestRunSQL = "UPDATE TestResults SET TimedOut=1 WHERE RunId="+ runId +" AND TestName='"+ testName +"' AND PermutationCode='"+permutationCode+"'";
			s.execute(updateTestRunSQL);

            s.close();
		}
		catch (SQLException e)
		{
			System.out.println("Unexpected Exception - "+e.toString());
			returnValue = false;
		}
        finally
        {
            try
            {
                if ( conn != null )
                {
                    conn.close();
                }
            }
            catch (SQLException e)
            {
                e.printStackTrace(System.err);
            }
        }

		return(returnValue);
    }

	public void emailResults(String distributionList, long runId, String comments)
	{
		try
		{
			/*
			 * Get SMTP details
			 */
        	InitialContext initialcontext = new InitialContext();
            Context context = (Context)initialcontext.lookup("java:comp/env");
            String smtpServer = (String)context.lookup("dtf/smtp_server");

			ResultsMessenger htmlMsg = new ResultsMessenger(smtpServer,EmailDetails.HTML_EMAIL);

			htmlMsg.setRunId(runId);

			/*
			 * If the distribution list is not specified or its length is 0.  Then
			 * send the results to the distribution list specified in the setup.
			 * Otherwise send it to the distribution list.
			 */
			if ( (distributionList == null) || (distributionList.length() == 0) )
			{
			}
			else
			{
				System.out.println("Parsing distribution list '"+distributionList+"'");
				/*
				 * String each recipient from the ; delimited distribution list
				 * e.g. a@b.com;c@d.com;e@f.com
				 */
				 StringTokenizer st = new StringTokenizer(distributionList,";");

			     while (st.hasMoreTokens()) {
			         String recipient = st.nextToken();
			         System.out.println("Recipient = "+recipient);

			         htmlMsg.addRecipient(recipient);
			     }
			}

			String htmlEmailURL = getRootURL()+"/view_results.jsp?runid="+runId+"&emailversion=true"+ (comments != null ? "&comments="+URLEncoder.encode(comments) : "");

			htmlMsg.send(new java.net.URL(htmlEmailURL));

			System.out.println("Sent email");
		}
		catch (Exception e)
		{
			e.printStackTrace(System.err);
			System.err.println("ERROR - While trying to email results");
		}
	}

	public boolean signalTestComplete(HttpServletRequest request)
	{
		boolean returnValue = true;
        Connection conn = null;

		// This script is called whenever a test completes
		try
		{
			conn = _pool.getConnection();
			Statement s = conn.createStatement();

			String dateTimeComplete = new Timestamp(new java.util.Date().getTime()).toString();
			String runId = request.getParameter("RunID");
			String testName = request.getParameter("TestName");
			String permutationCode = request.getParameter("PermutationCode");
			int numberOfTasks = 0;

			/**
			 * Get number of tasks involved in this test
			 */
			ResultSet infoRs = s.executeQuery("SELECT NumberOfTasks FROM TestResults WHERE RunId="+ runId +" AND TestName='"+ testName +"' AND PermutationCode='"+ permutationCode +"'");

			if (infoRs.next())
			{
				numberOfTasks = infoRs.getInt("NumberOfTasks");
			}
			else
			{
				System.err.println("Cannot find number of tasks for this test");
				returnValue = false;
			}

			String resultsSQL = "SELECT * FROM TestTaskResults WHERE RunId="+ runId +" AND TestName='"+ testName +"' AND PermutationCode='"+ permutationCode +"'";

			ResultSet results = s.executeQuery(resultsSQL);

   			String overAllResult = "Passed";
   			int resultCount = 0;

		    while (results.next())
		    {
		   		if (!results.getString("Result").equals("Passed"))
		   		{
		   			overAllResult = "Failed";
				}
		   		resultCount++;
		   	}

		   	results.close();

   			if ( resultCount == 0 )
           {
               System.out.println("FAILED DUE TO: Number of results:"+resultCount+", Number of tasks: "+numberOfTasks);
               overAllResult = "Failed";
           }

			System.out.println("TestResults entry does exist - updating DateTimeFinished");

   			// Update row in TestResults table
   			String updateTestResultsSQL = "UPDATE TestResults SET DateTimeFinished='"+ dateTimeComplete +"', OverallResult='"+ overAllResult +"' WHERE RunId="+ runId +" AND TestName='"+ testName +"' AND PermutationCode='"+ permutationCode +"'";

   			s.execute(updateTestResultsSQL);

			s.close();
		}
		catch (SQLException e)
		{
			System.out.println("Unexpected Exception - "+e.toString());
			returnValue = false;
		}
        finally
        {
            try
            {
                if ( conn != null )
                {
                    conn.close();
                }
            }
            catch (SQLException e)
            {
                e.printStackTrace(System.err);
            }
        }

		return(returnValue);
	}

	public boolean logResult(HttpServletRequest request)
	{
		boolean returnValue = true;
        Connection conn = null;

		// This script is called to a log test information
		try
		{
			conn = _pool.getConnection();
			Statement s = conn.createStatement();

			String dateTimeStarted = new Timestamp(new java.util.Date().getTime()).toString();
			String runId = request.getParameter("RunID");
			String testName = request.getParameter("TestName");
			String taskName = request.getParameter("TaskName");
			String taskPermutationCode = request.getParameter("TaskPermutationCode");
			String testPermutationCode = request.getParameter("TestPermutationCode");
		    String result = request.getParameter("Result");
	   		String createTestRunSQL = "";

			System.out.println("Logging result in run id. '"+runId+"'");
			System.out.println(" [TestName           ] = '"+testName+"'");
			System.out.println(" [TaskPermutationCode] = '"+taskPermutationCode+"'");
			System.out.println(" [TestPermutationCode] = '"+testPermutationCode+"'");
			System.out.println(" [Result             ] = '"+result+"'");
			System.out.println(" [DateTimeStarted    ] = '"+dateTimeStarted+"'");

			// Retrieve the test information
		    String testSQL = "SELECT * FROM TestTaskResults WHERE RunID="+ runId +" AND taskName='"+ taskName +"' AND testName='"+ testName +"' AND PermutationCode='"+ testPermutationCode +"' AND TaskPermutationCode='"+ taskPermutationCode +"'";

			ResultSet results = s.executeQuery(testSQL);

			// If there is no test result for this test runid permutation code then creat one
			if (!results.next())
			{
				System.out.println("TestTaskResult entry does not exist - creating");

				// Create row in Tests table
				createTestRunSQL = "INSERT INTO TestTaskResults (RunId, TestName, TaskName, PermutationCode, TaskPermutationCode, TimeLogged, Result) VALUES";
				createTestRunSQL = createTestRunSQL +  "("+ runId +","+
												       "'"+ testName +"',"+
												       "'"+ taskName +"',"+
												       "'"+ testPermutationCode +"',"+
												       "'"+ taskPermutationCode +"',"+
												       "'"+ dateTimeStarted +"',"+
												       "'"+ result +"')";
			}
			else
			{
				System.out.println("TestTaskResult entry does exist - updating");

	    		// Update row in TestTaskResults table
		        createTestRunSQL = "UPDATE TestTaskResults SET Result = '"+ result +"' WHERE RunId="+ runId +" AND TestName='"+ testName +"' AND TaskName='"+ taskName +"' AND PermutationCode='"+ testPermutationCode +"' AND TaskPermutationCode='"+ taskPermutationCode +"'";
			}

			s.execute(createTestRunSQL);

			results.close();
			s.close();
		}
		catch (SQLException e)
		{
			System.out.println("Unexpected Exception - "+e.toString());
			returnValue = false;
		}
        finally
        {
            if ( conn != null )
            {
                try
                {
                    conn.close();
                }
                catch (SQLException e)
                {
                    e.printStackTrace(System.err);
                }
            }
        }
		return(returnValue);
	}

	public boolean logTestRunInformation(HttpServletRequest request)
	{
		boolean returnValue = true;
		String createTestInformationSQL;
        Connection conn = null;

		try
		{
			conn = _pool.getConnection();
			Statement s = conn.createStatement();
			String testSQL, createTestSQL;

			String dateTimeStarted = new Timestamp(new java.util.Date().getTime()).toString();
			String runId = request.getParameter("RunID");
			String testName = request.getParameter("TestName");
			String permutationCode = request.getParameter("PermutationCode");
			String information = request.getParameter("Information");

	   		information = StringUtils.replaceString(information,"\n","<br>");
	   		information = information.replace('\'', '\"');

			System.out.println("Log test information in run id. '"+runId+"'");
			System.out.println(" [TestName       ] = '"+testName+"'");
			System.out.println(" [PermutationCode] = '"+permutationCode+"'");
			System.out.println(" [Information    ] = '"+information+"'");
			System.out.println(" [DateTimeStarted] = '"+dateTimeStarted+"'");

		    testSQL = "SELECT * FROM TestRuns WHERE RunID="+ runId;

			ResultSet results = s.executeQuery(testSQL);

			// If there is no test result for this test runid permutation code then creat one
			if (results.next())
			{
				System.out.println("TestRuns entry does exist - updating");

				String previousInformation = results.getString("Information");

				if (previousInformation != null)
			    	createTestInformationSQL  = "UPDATE TestRuns SET Information = '"+ previousInformation + information +"' WHERE RunId="+ runId;
			    else
			        createTestInformationSQL = "UPDATE TestRuns SET Information = '"+ information +"' WHERE RunId="+ runId;

				s.execute(createTestInformationSQL);
			}

			results.close();
			s.close();
		}
		catch (SQLException e)
		{
			System.out.println("Unexpected Exception - "+e.toString());
			returnValue = false;
		}
        finally
        {
            if ( conn != null )
            {
                try
                {
                    conn.close();
                }
                catch (SQLException e)
                {
                    e.printStackTrace(System.err);
                }
            }
        }
		return(returnValue);
	}

	public boolean logTestInformation(HttpServletRequest request)
	{
		boolean returnValue = true;
		String createTestInformationSQL;
        Connection conn = null;

		try
		{
			System.out.println("LogTestInformation Called: "+request);
			conn = _pool.getConnection();
			Statement s = conn.createStatement();
			String testSQL, createTestSQL;

			String dateTimeStarted = new Timestamp(new java.util.Date().getTime()).toString();
			String runId = request.getParameter("RunID");
			String testName = request.getParameter("TestName");
			String permutationCode = request.getParameter("PermutationCode");
			String information = request.getParameter("Information");

	   		information = StringUtils.replaceString(information,"\n","<br>");
	   		information = information.replace('\'', '\"');

			System.out.println("Log test information in run id. '"+runId+"'");
			System.out.println(" [TestName       ] = '"+testName+"'");
			System.out.println(" [PermutationCode] = '"+permutationCode+"'");
			System.out.println(" [Information    ] = '"+information+"'");
			System.out.println(" [DateTimeStarted] = '"+dateTimeStarted+"'");

		    testSQL = "SELECT * FROM TestResults WHERE RunID="+ runId +" AND testName='"+ testName +"' AND PermutationCode='"+ permutationCode +"'";

			ResultSet results = s.executeQuery(testSQL);

			// If there is no test result for this test runid permutation code then creat one
			if (results.next())
			{
				System.out.println("TestResults entry does exist - updating");

				ResultSet infoRs = s.executeQuery("SELECT Information FROM TestResults WHERE RunId="+ runId +" AND TestName='"+ testName +"' AND PermutationCode='"+ permutationCode +"'");

				if (infoRs.next())
				{
					String previousInformation = infoRs.getString("Information");

			        createTestInformationSQL  = "UPDATE TestResults SET Information = '"+ previousInformation + information +"' WHERE RunId="+ runId +" AND TestName='"+ testName +"' AND PermutationCode='"+ permutationCode +"'";
			    }
			    else
			    {
			        createTestInformationSQL = "UPDATE TestResults SET Information = '"+ information +"' WHERE RunId="+ runId +" AND TestName='"+ testName +"' AND PermutationCode='"+ permutationCode +"'";
				}

				s.execute(createTestInformationSQL);

				infoRs.close();
			}

			results.close();
			s.close();
		}
		catch (SQLException e)
		{
			System.out.println("Unexpected Exception - "+e.toString());
			returnValue = false;
		}
        finally
        {
            if ( conn != null )
            {
                try
                {
                    conn.close();
                }
                catch (SQLException e)
                {
                    e.printStackTrace(System.err);
                }
            }
        }
		return(returnValue);
	}

	public boolean logInformation(HttpServletRequest request)
	{
		boolean returnValue = true;
        Connection conn = null;

		// This script is called to a log test information
		try
		{
			conn = _pool.getConnection();
			Statement s = conn.createStatement();

			String dateTimeStarted = new Timestamp(new java.util.Date().getTime()).toString();
			String runId = request.getParameter("RunID");
			String testName = request.getParameter("TestName");
			String taskName = request.getParameter("TaskName");
			String taskPermutationCode = request.getParameter("TaskPermutationCode");
			String testPermutationCode = request.getParameter("TestPermutationCode");
			String information = request.getParameter("Information");
	   		String createTestRunSQL = "";

			System.out.println("Logging information in run id. '"+runId+"'");
			System.out.println(" [TestName           ] = '"+testName+"'");
			System.out.println(" [TaskName           ] = '"+taskName+"'");
			System.out.println(" [TestPermutationCode] = '"+testPermutationCode+"'");
			System.out.println(" [TaskPermutationCode] = '"+taskPermutationCode+"'");
			System.out.println(" [Information        ] = '"+information+"'");
			System.out.println(" [DateTimeStarted    ] = '"+dateTimeStarted+"'");

			// Retrieve the test information
		    String testSQL = "SELECT * FROM TestTaskResults WHERE RunID="+ runId +" AND taskName='"+ taskName +"' AND testName='"+ testName +"' AND PermutationCode='"+ testPermutationCode +"' AND TaskPermutationCode='"+ taskPermutationCode +"'";

	   		information = StringUtils.replaceString(information,"\n","<br>");
	   		information = information.replace('\'', '\"');

			ResultSet results = s.executeQuery(testSQL);

			// If there is no test result for this test runid permutation code then creat one
			if (!results.next())
			{
				System.out.println("TestTaskResults entry does not exist - creating");

				// Create row in Tests table
				createTestRunSQL = "INSERT INTO TestTaskResults (RunId, TestName, TaskName, PermutationCode, TaskPermutationCode, TimeLogged, Information) VALUES";
				createTestRunSQL = createTestRunSQL +  "("+ runId +","+
												       "'"+ testName +"',"+
												       "'"+ taskName +"',"+
												       "'"+ testPermutationCode +"',"+
												       "'"+ taskPermutationCode +"',"+
												       "'"+ dateTimeStarted +"',"+
												       "'"+ information +"')";
			}
			else
			{
				System.out.println("TestTaskResults entry does exist - updating");

				ResultSet infoRs = s.executeQuery("SELECT Information FROM TestTaskResults WHERE RunId="+ runId +" AND TestName='"+ testName +"' AND TaskName='"+ taskName +"' AND PermutationCode='"+ testPermutationCode +"' AND TaskPermutationCode='"+ taskPermutationCode +"'");

				if (infoRs.next())
				{
					String previousInformation = infoRs.getString("Information");

		    		// Update row in TestTaskResults table
			        createTestRunSQL = "UPDATE TestTaskResults SET Information = '"+ previousInformation + information +"' WHERE RunId="+ runId +" AND TestName='"+ testName +"' AND TaskName='"+ taskName +"' AND PermutationCode='"+ testPermutationCode +"' AND TaskPermutationCode='"+ taskPermutationCode +"'";
			    }
			    else
			    {
					createTestRunSQL = "UPDATE TestTaskResults SET Information = '"+ information +"' WHERE RunId="+ runId +" AND TestName='"+ testName +"' AND TaskName='"+ taskName +"' AND PermutationCode='"+ testPermutationCode +"' AND TaskPermutationCode='"+ taskPermutationCode +"'";
				}

				infoRs.close();
			}

			s.execute(createTestRunSQL);

			results.close();
			s.close();
		}
		catch (SQLException e)
		{
			returnValue = false;
			System.err.println("Unexpected Exception - "+e.toString());
			e.printStackTrace(System.err);
		}
        finally
        {
            if ( conn != null )
            {
                try
                {
                    conn.close();
                }
                catch (SQLException e)
                {
                    e.printStackTrace(System.err);
                }
            }
        }

		System.out.println("Complete");
		return(returnValue);
	}

	public boolean initiateTest(HttpServletRequest request)
	{
		boolean returnValue = true;
        Connection conn = null;

		try
		{
			conn = _pool.getConnection();
			Statement s = conn.createStatement();
			String testSQL, createTestSQL;

			String dateTimeStarted = new Timestamp(new java.util.Date().getTime()).toString();
			String runId = request.getParameter("RunID");
			String testName = request.getParameter("TestName");
			String permutationCode = request.getParameter("PermutationCode");
			String numberOfTasks = request.getParameter("NumberOfTasks");

			System.out.println("Initiating test in run id. '"+runId+"'");
			System.out.println(" [TestName       ] = '"+testName+"'");
			System.out.println(" [PermutationCode] = '"+permutationCode+"'");
			System.out.println(" [NumberOfTasks  ] = '"+numberOfTasks+"'");
			System.out.println(" [DateTimeStarted] = '"+dateTimeStarted+"'");

		    testSQL = "SELECT * FROM TestResults WHERE RunID="+ runId +" AND testName='"+ testName +"' AND PermutationCode='"+ permutationCode +"'";

			ResultSet results = s.executeQuery(testSQL);

			// If there is no test result for this test runid permutation code then creat one
			if (!results.next())
			{
				System.out.println("TestResults entry does not exist - creating");

     			// Create row in TestResults table
			    createTestSQL = "INSERT INTO TestResults (RunID, TestName, PermutationCode, DateTimeStarted, NumberOfTasks, OverallResult, Information) VALUES";
			    createTestSQL = createTestSQL + "("+ runId +","+
			   									"'"+ testName +"',"+
			   									"'"+ permutationCode +"',"+
			   									"'"+ dateTimeStarted +"',"+
			   									numberOfTasks +","+
			   									"'Uncertain', ' ')";
				s.execute(createTestSQL);
			}

			results.close();
			s.close();
		}
		catch (SQLException e)
		{
			System.out.println("Unexpected Exception - "+e.toString());
			returnValue = false;
		}
        finally
        {
            if ( conn != null )
            {
                try
                {
                    conn.close();
                }
                catch (SQLException e)
                {
                    e.printStackTrace(System.err);
                }
            }
        }

		return(returnValue);
	}

	public boolean initiateTask(HttpServletRequest request)
	{
		boolean returnValue = true;
		Connection conn = null ;

		try
		{
			conn = _pool.getConnection();
			Statement s = conn.createStatement();
			String testSQL, createTestSQL;

			String dateTimeStarted = new Timestamp(new java.util.Date().getTime()).toString();
			String runId = request.getParameter("RunID");
			String testName = request.getParameter("TestName");
			String taskName = request.getParameter("TaskName");
			String taskPermutationCode = request.getParameter("TaskPermutationCode");
			String testPermutationCode = request.getParameter("TestPermutationCode");

			System.out.println("Initiating task in run id. '"+runId+"'");
			System.out.println(" [TestName           ] = '"+testName+"'");
			System.out.println(" [TaskName           ] = '"+taskName+"'");
			System.out.println(" [TaskPermutationCode] = '"+taskPermutationCode+"'");
			System.out.println(" [TestPermutationCode] = '"+testPermutationCode+"'");

   			testSQL = "SELECT * FROM TestTaskResults WHERE RunID="+ runId +" AND TestName='"+ testName +"' AND TaskName='"+ taskName +"' AND PermutationCode='"+ testPermutationCode +"' AND TaskPermutationCode='"+ taskPermutationCode +"'";

			ResultSet results = s.executeQuery(testSQL);

			// If there is no test result for this test runid permutation code then creat one
			if (!results.next())
			{
				System.out.println("TestTaskResults entry does not exist - creating");

     			// Create row in TestTaskResults table
			    createTestSQL = "INSERT INTO TestTaskResults (RunID, TestName, TaskName, PermutationCode, TaskPermutationCode, TimeLogged,  Information, Result) VALUES";
			    createTestSQL = createTestSQL +   "("+ runId +","+
			   									  "'"+ testName +"',"+
			   									  "'"+ taskName +"',"+
			   									  "'"+ testPermutationCode +"',"+
			   									  "'"+ taskPermutationCode +"',"+
			   									  "'"+ dateTimeStarted +"',"+
			   									  "'',"+
			   									  "'Uncertain')";
			}
			else
			{
				System.out.println("TestTaskResults entry does exist - updating");

	    		// Update row in TestResults table
     			createTestSQL = "UPDATE TestTaskResults SET TimeLogged='"+ dateTimeStarted +"' WHERE RunId="+ runId +" AND TestName='"+ testName +"' AND TaskName='"+ taskName +"' AND PermutationCode='"+ testPermutationCode +"' AND TaskPermutationCode='"+ taskPermutationCode +"'";
			}

			s.execute(createTestSQL);

			results.close();
			s.close();
		}
		catch (SQLException e)
		{
			returnValue = false;
			System.err.println("Unexpected Exception - "+e.toString());
			e.printStackTrace(System.err);
		}
        finally
        {
            if ( conn != null )
            {
                try
                {
                    conn.close();
                }
                catch (SQLException e)
                {
                    e.printStackTrace(System.err);
                }
            }
        }

		return(returnValue);
	}

	public long initiateTestRun(HttpServletRequest request)
	{
		long runId = -1;
        Connection conn = null;

		try
		{
			conn = _pool.getConnection();
			Statement s = conn.createStatement();
			String createTestRunSQL;

			String testDefinitionURL = request.getParameter("TestDefinitionURL");
			String testDefinitionDescription = request.getParameter("TestDefinitionDescription");
			String testSelectionURL = request.getParameter("TestSelectionURL");
			String testSelectionDescription = request.getParameter("TestSelectionDescription");
			String softwareVersion = request.getParameter("SoftwareVersion");
			String distributionList = request.getParameter("DistributionList");
			String dateTimeStarted = new Timestamp(new java.util.Date().getTime()).toString();

			runId = getNextRunIdAndIncrement();

			System.out.println("Initiating test run run id. '"+runId+"'");
			System.out.println(" [TestDefinitionURL        ] = '"+testDefinitionURL+"'");
			System.out.println(" [TestDefinitionDescription] = '"+testDefinitionDescription+"'");
			System.out.println(" [TestSelectionURL         ] = '"+testSelectionURL+"'");
			System.out.println(" [TestSelectionDescription ] = '"+testSelectionDescription+"'");
			System.out.println(" [SoftwareVersion          ] = '"+softwareVersion+"'");
			System.out.println(" [DistributionList         ] = '"+distributionList+"'");
			System.out.println(" [DateTimeStarted          ] = '"+dateTimeStarted+"'");

			createTestRunSQL = "INSERT INTO TestRuns (RunId, DateTimeStarted, TestDefinitions, TestDefinitionsDescription, TestSelection, TestSelectionDescription, SoftwareVersion, DistributionList, Information) VALUES";
			createTestRunSQL = createTestRunSQL + "("+runId+","+
												   "'"+ dateTimeStarted +"',"+
												   "'"+ testDefinitionURL +"',"+
												   "'"+ testDefinitionDescription +"',"+
												   "'"+ testSelectionURL +"',"+
												   "'"+ testSelectionDescription +"',"+
												   "'"+ softwareVersion +"',"+
												   "'"+ distributionList +"', '')";

			s.execute(createTestRunSQL);

			s.close();
		}
		catch (SQLException e)
		{
			System.out.println("Unexpected Exception - While creating test run");
			e.printStackTrace(System.err);
			runId = -1;
		}
        finally
        {
            if ( conn != null )
            {
                try
                {
                    conn.close();
                }
                catch (SQLException e)
                {
                    e.printStackTrace(System.err);
                }
            }
        }

		return(runId);
	}

	public long getNextRunIdAndIncrement()
	{
		int returnValue = 0;
        Connection conn = null;

		try
		{
			conn = _pool.getConnection();
			conn.setAutoCommit(false);

			Statement s = conn.createStatement();

			ResultSet configTable = s.executeQuery("SELECT * FROM Configuration");

			if (!configTable.next())
			{
				System.out.println("No RunId found - initialising");
				returnValue = 0;
				s.execute("insert into Configuration values (1,'//localhost/NameService')");
			}
			else
			{
				returnValue = configTable.getInt("RunId");
				s.execute("update Configuration set RunId=RunId+1");
			}

			configTable.close();
			s.close();

			conn.commit();
			conn.setAutoCommit(true);
		}
		catch (Exception e)
		{
			System.out.println("Unexpected Exception - While trying to get RunId and increment");
			e.printStackTrace(System.err);
        }
        finally
        {
            if ( conn != null )
            {
                try
                {
                    conn.close();
                }
                catch (SQLException e)
                {
                    e.printStackTrace(System.err);
                }
            }
        }

		return((long)returnValue);
	}

	public static long getCurrentRunId()
	{
		int returnValue = -1;
        Connection conn = null;

		try
		{
			conn = _pool.getConnection();
			Statement s = conn.createStatement();

			ResultSet configTable = s.executeQuery("SELECT * FROM Configuration");

			if (configTable.next())
			{
				returnValue = configTable.getInt("RunId");
			}

			configTable.close();
			s.close();
		}
		catch (Exception e)
		{
			System.out.println("Unexpected Exception - While trying to retrieve RunId");
			e.printStackTrace(System.err);
		}
        finally
        {
            if ( conn != null )
            {
                try
                {
                    conn.close();
                }
                catch (SQLException e)
                {
                    e.printStackTrace(System.err);
                }
            }
        }
		return((long)returnValue);
	}

	public static String getRootURL()
	{
		String returnValue = null;
        Connection conn = null;

		try
		{
			conn = _pool.getConnection();
			Statement s = conn.createStatement();

			ResultSet rs = s.executeQuery("SELECT * FROM Configuration");

			if (rs.next())
			{
				returnValue = rs.getString("RootURL");
			}

			rs.close();
			s.close();
		}
		catch (Exception e)
		{
			e.printStackTrace(System.err);
			System.err.println("ERROR - Cannot retrieve the RootURL");
		}
        finally
        {
            if ( conn != null )
            {
                try
                {
                    conn.close();
                }
                catch (SQLException e)
                {
                    e.printStackTrace(System.err);
                }
            }
        }
		return(returnValue);
	}

	public static void createTables() throws SQLException
	{
		if (!_tablesCreated)
		{
			System.out.println("Creating tables...");

			Connection conn = _pool.getConnection();
			Statement s = conn.createStatement();

			try
			{
				System.out.println("Creating 'Configuration' table");
				s.execute("create table Configuration(RunId INT, "+
				                                     "DefaultNameServiceURI VARCHAR(255),"+
				                                     "UploadDirectory VARCHAR(255),"+
				                                     "UploadWebDirectory VARCHAR(255),"+
				                                     "RootURL VARCHAR(255) )");

				s.execute("insert into Configuration values (0,'//localhost:1099/NameService','/opt/jakarta-tomcat-4.1.31/webapps/dtf/producttests','http://localhost:8080/dtf/producttests','http://localhost:8080/dtf/')");

			}
			catch (SQLException e)
			{
				System.out.println("Table already exists");
			}

			try
			{
				System.out.println("Creating 'EmailRecipients' table");
				s.execute("create table EmailRecipients( EmailAddress VARCHAR(255) primary key, EmailType int )");
            }
			catch (SQLException e)
			{
				System.out.println("Table already exists");
			}

			try
			{
				System.out.println("Creating 'KnownNameServices' table");
				s.execute("create table KnownNameServices( NameServiceURI VARCHAR(255) )");
            }
			catch (SQLException e)
			{
				System.out.println("Table already exists");
			}

			try
			{
				System.out.println("Creating 'TestRuns' table");
				s.execute("create table TestRuns(RunId INT PRIMARY KEY,"+
				                                "DateTimeStarted DATETIME,"+
				                                "DateTimeFinished DATETIME,"+
				                                "TestDefinitions VARCHAR(255),"+
				                                "TestDefinitionsDescription VARCHAR(255),"+
				                                "TestSelection VARCHAR(255),"+
				                                "TestSelectionDescription VARCHAR(255),"+
				                                "SoftwareVersion VARCHAR(255), INDEX(SoftwareVersion),"+
				                                "DistributionList VARCHAR(255),"+
				                                "Information BLOB )");
			}
			catch (SQLException e)
			{
				System.out.println("Table already exists");
			}

			try
			{
				System.out.println("Creating 'TestResults' table");
				s.execute("create table TestResults(RunId INT, INDEX(RunId),"+
				                                   "TestName VARCHAR(255),"+
				                                   "PermutationCode VARCHAR(255),"+
				                                   "DateTimeStarted DATETIME,"+
				                                   "DateTimeFinished DATETIME,"+
				                                   "NumberOfTasks INT,"+
				                                   "OverAllResult VARCHAR(255),"+
                                                   "TimedOut BOOL,"+
				                                   "Information BLOB )");
			}
			catch (SQLException e)
			{
				System.out.println("Table already exists");
			}

			try
			{
				System.out.println("Creating 'TestTaskResults' table");
				s.execute("create table TestTaskResults(RunId INT, INDEX(RunId),"+
				                                       "TestName VARCHAR(255), INDEX(TestName),"+
				                                       "TaskName VARCHAR(255),"+
				                                       "PermutationCode VARCHAR(255),"+
				                                       "TimeLogged DATETIME,"+
				                                       "Result VARCHAR(255),"+
				                                       "Information BLOB,"+
				                                       "TaskPermutationCode VARCHAR(255) )");
			}
			catch (SQLException e)
			{
				System.out.println("Table already exists");
			}

			try
			{
				System.out.println("Creating 'PredefinedRuns' table");

				s.execute("create table PredefinedRuns(Name VARCHAR(64),"+
				                                      "TestDefinitionsURL VARCHAR(255),"+
				                                      "TestSelectionsURL VARCHAR(255) )");
			}
			catch (SQLException e)
			{
				System.out.println("Table already exists");
			}

			try
			{
				s.execute("create table Products(Id VARCHAR(255),"+
				                            "Name VARCHAR(255) )");
			}
			catch (SQLException e)
			{
				System.out.println("Table already exists");
			}

			try
			{
				System.out.println("Creating 'OSs' table");

				s.execute("create table OSs(Id VARCHAR(255),"+
				                           "Name VARCHAR(255) )");
			}
			catch (SQLException e)
			{
				System.out.println("Table already exists");
			}

            try
            {
                System.out.print("Ensuring 'savedperfsets' table exists: ");
                s.executeUpdate("CREATE TABLE SavedPerfSets (Name VARCHAR(255) Primary Key, CreatedDateTime DATETIME, XAxisLabel VARCHAR(64), YAxisLabel VARCHAR(64))");

                System.out.println("Success");
            }
            catch (SQLException e)
            {
                System.out.println("Already exists");
            }

            try
            {
                System.out.print("Ensuring 'savedperfsetsdata' table exists: ");
                s.executeUpdate("CREATE TABLE SavedPerfSetsData (Name VARCHAR(255), PerfRunId INT, SeriesLabel VARCHAR(64))");

                System.out.println("Success");
            }
            catch (SQLException e)
            {
                System.out.println("Already exists");
            }

			try
			{
				System.out.print("Ensuring 'archiveid' table exists: ");
				s.executeUpdate("CREATE TABLE archiveid (ArchiveId INT)");

				System.out.println("Success");
			}
			catch (SQLException e)
			{
				System.out.println("Already exists");
			}

			try
			{
				System.out.print("Ensuring 'archivedresults' table exists: ");
				s.executeUpdate("CREATE TABLE archivedresults (ArchiveId INT PRIMARY KEY, Name VARCHAR(255), Comments VARCHAR(255), DateTime DATETIME)");

				System.out.println("Success");
			}
			catch (SQLException e)
			{
				System.out.println("Already exists :"+e);
			}

			try
			{
				System.out.print("Ensuring 'archivedruns' table exists: ");
				s.executeUpdate("CREATE TABLE archivedruns (ArchiveId INT, RunId INT)");

				System.out.println("Success");
			}
			catch (SQLException e)
			{
				System.out.println("Already exists");
			}

			s.close();
            conn.close();
			_tablesCreated = true;
		}
	}
}
