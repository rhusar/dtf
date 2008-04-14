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
package org.jboss.dtf.testframework.utils.logging.plugins;

/*
 * Copyright (C) 1998, 1999, 2000, 2001, 2002, 2003
 *
 * Arjuna Technologies Ltd.
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: JDBCLoggingServicePlugin.java 170 2008-03-25 18:59:26Z jhalliday $
 */

import org.jboss.dtf.testframework.utils.logging.LoggingService;
import org.jboss.dtf.testframework.utils.logging.exception.LoggingServiceException;
import org.jboss.dtf.testframework.utils.XMLUtils;
import org.jboss.dtf.testframework.testnode.RunUID;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.sql.*;
import java.net.URL;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class JDBCLoggingServicePlugin implements LoggingService
{
	private final static String LOGGING_SERVICE_URL_PROPERTY = "org.jboss.dtf.testframework.utils.logging.plugins.JDBCLoggingServicePlugin.URL";

	private DataSource	_dataSource = null;
	private Connection	_conn = null;

	public void initialise(String loggerURL) throws LoggingServiceException
	{
		try
		{
			String loggingURL = System.getProperty(LOGGING_SERVICE_URL_PROPERTY, loggerURL);

			InitialContext cxt = new InitialContext();

			_dataSource = (DataSource)cxt.lookup(loggingURL);

			if ( _dataSource == null )
			{
				throw new LoggingServiceException("Failed to find JDBC datasource");
			}

			_conn = _dataSource.getConnection();
		}
		catch (NamingException e)
		{
			throw new LoggingServiceException("Failed to lookup JDBC datasource: "+e);
		}
		catch (SQLException e)
		{
			throw new LoggingServiceException("Failed to get JDBC connection: "+e);
		}
	}

	public RunUID initiateTestRun(String testDefinitionURL,
								  String testSelectionURL,
								  String softwareVersion,
								  String distributionList) throws LoggingServiceException
	{
		RunUID runId = new RunUID(getNextRunIdAndIncrement());

		try
		{
			String definitionDescription = "";
			String selectionDescription = "";
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
					throw new LoggingServiceException("Error trying to parse the test definition xml file: "+e);
				}
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
					throw new LoggingServiceException("Error trying to parse the test definition xml file: "+e);
				}
			}
			PreparedStatement ps = _conn.prepareStatement("INSERT INTO TestRuns (RunId, DateTimeStarted, TestDefinitions, TestDefinitionsDescription, "+
																				"TestSelection, TestSelectionDescription, SoftwareVersion, DistributionList, " +
																				"Information) VALUES (?,now(),?,?,?,?,?,?,'')");
			ps.setLong(1, runId.getUID());
			ps.setString(2, testDefinitionURL);
			ps.setString(3, definitionDescription);
			ps.setString(4, testSelectionURL);
			ps.setString(5, selectionDescription);
			ps.setString(6, softwareVersion);
			ps.setString(7, distributionList);
			ps.executeUpdate();
			ps.close();
		}
		catch (SQLException e)
		{
			throw new LoggingServiceException("Failed to update results database: "+e);
		}

		return runId;
	}

	public long getNextRunIdAndIncrement()
	{
		int returnValue = 0;

		try
		{
			Statement s = _conn.createStatement();

			ResultSet configTable = s.executeQuery("SELECT * FROM Configuration");

			if (!configTable.next())
			{
				returnValue = 0;
				s.execute("INSERT INTO Configuration values (1,'//localhost/NameService')");
			}
			else
			{
				returnValue = configTable.getInt("RunId");
				s.execute("UPDATE Configuration SET RunId=RunId+1");
			}

			configTable.close();
			s.close();
		}
		catch (Exception e)
		{
			System.out.println("Unexpected Exception - While trying to get RunId and increment");
			e.printStackTrace(System.err);
        }

		return((long)returnValue);
	}

	public RunUID initiateTestRun(String softwareVersion,
								  String distributionList) throws LoggingServiceException
	{
		throw new LoggingServiceException("This logger does not support this method");
	}

	public boolean testRunComplete(RunUID runUID) throws LoggingServiceException
	{
		try
		{
			// Update row in TestRuns table
			PreparedStatement ps = _conn.prepareStatement("UPDATE TestRuns SET DateTimeFinished=now() WHERE RunId=?");

			ps.setLong(1, runUID.getUID());

			ps.executeUpdate();
			ps.close();

			/**
			 * todo Add code here to indicate to web server to email users with results
			 */
		}
		catch (SQLException e)
		{
			throw new LoggingServiceException("Failed to initiate test run: "+e);
		}

		return true;
	}

	public boolean logResult(String result,
							 String taskName,
							 String testName,
							 RunUID runUID,
							 String taskPermutationCode,
							 String testPermutationCode) throws LoggingServiceException
	{
		try
		{
			// Retrieve the test information
			PreparedStatement ps = _conn.prepareStatement("SELECT * FROM TestTaskResults WHERE RunID=? AND taskName=? AND testName=? AND PermutationCode=? AND TaskPermutationCode=?");

			ps.setLong(1, runUID.getUID());
			ps.setString(2, taskName);
			ps.setString(3, testName);
			ps.setString(4, testPermutationCode);
			ps.setString(5, taskPermutationCode);

			ResultSet results = ps.executeQuery();

			// If there is no test result for this test runid permutation code then create one
			if (!results.next())
			{
				if ( result == null )
				{
					System.out.println("GAK NULL FOUND");
					Thread.dumpStack();
				}
				PreparedStatement ps2 = _conn.prepareStatement("INSERT INTO TestTaskResults (RunId, TestName, TaskName, PermutationCode, TaskPermutationCode, TimeLogged, Result) "+
																"VALUES (?,?,?,?,?,now(),?)");

				ps2.setLong(1, runUID.getUID());
				ps2.setString(2, testName);
				ps2.setString(3, taskName);
				ps2.setString(4, testPermutationCode);
				ps2.setString(5, taskPermutationCode);
				ps2.setString(6, result);

				ps2.executeUpdate();
				ps2.close();
			}
			else
			{
				System.out.println("TestTaskResult entry does exist - updating");
				PreparedStatement ps2 = _conn.prepareStatement("UPDATE TestTaskResults SET Result = ? WHERE RunId=? AND TestName=? AND TaskName=? AND PermutationCode=? AND TaskPermutationCode=?");

				if ( result == null )
				{
					System.out.println("GAK NULL FOUND");
					Thread.dumpStack();
				}

				ps2.setString(1, result);
				ps2.setLong(2, runUID.getUID());
				ps2.setString(3, testName);
				ps2.setString(4, taskName);
				ps2.setString(5, testPermutationCode);
				ps2.setString(6, taskPermutationCode);

				ps2.executeUpdate();
				ps2.close();
			}

			ps.close();
		}
		catch (SQLException e)
		{
			throw new LoggingServiceException("Failed to log result: "+e);
		}

        return true;
	}

	public boolean logTestRunInformation(String information,
										 String taskName,
										 String testName,
										 RunUID runUID,
										 String taskPermutationCode,
										 String testPermutationCode) throws LoggingServiceException
	{
        String previousInformation = "";

		try
		{
			// Retrieve the test information
			PreparedStatement ps = _conn.prepareStatement("SELECT * FROM TestRuns WHERE RunID=?");

			ps.setLong(1, runUID.getUID());

			ResultSet results = ps.executeQuery();

			if ( results.next() )
			{
				previousInformation = results.getString("Information");
			}

			results.close();
			ps.close();

			ps = _conn.prepareStatement("UPDATE TestRuns SET Information = ? WHERE RunId=?");
			previousInformation += information;
			ps.setString(1, previousInformation);
			ps.setLong(2, runUID.getUID());
            ps.executeUpdate();

			ps.close();
		}
		catch (SQLException e)
		{
			throw new LoggingServiceException("Failed to log result: "+e);
		}

        return true;
	}

	public boolean logInformation(String information,
								  String taskName,
								  String testName,
								  RunUID runUID,
								  String taskPermutationCode,
								  String testPermutationCode) throws LoggingServiceException
	{
		try
		{
			PreparedStatement ps = _conn.prepareStatement("SELECT * FROM TestTaskResults WHERE RunID=? AND taskName=? AND testName=? AND PermutationCode=? AND TaskPermutationCode=?");

			ps.setLong(1, runUID.getUID());
			ps.setString(2, taskName);
			ps.setString(3, testName);
			ps.setString(4, testPermutationCode);
			ps.setString(5, taskPermutationCode);

			ResultSet rs = ps.executeQuery();

			if ( !rs.next() )
			{
				PreparedStatement insPs = _conn.prepareStatement("INSERT INTO TestTaskResults (RunId, TestName, TaskName, PermutationCode, " +
																 "TaskPermutationCode, TimeLogged, Information, Result) VALUES (?,?,?,?,?,now(),?,'Uncertain')");

				insPs.setLong(1, runUID.getUID());
				insPs.setString(2, testName);
				insPs.setString(3, taskName);
				insPs.setString(4, testPermutationCode);
				insPs.setString(5, taskPermutationCode);
				insPs.setString(6, information);

                insPs.executeUpdate();

				insPs.close();
			}
			else
			{
				PreparedStatement insPs = _conn.prepareStatement("UPDATE TestTaskResults SET Information=? WHERE RunId=? AND TestName=? AND TaskName=? AND " +
																 "PermutationCode=? AND TaskPermutationCode=?");

				insPs.setString(1, rs.getString("Information") + information);
				insPs.setLong(2, runUID.getUID());
				insPs.setString(3, testName);
				insPs.setString(4, taskName);
				insPs.setString(5, testPermutationCode);
				insPs.setString(6, taskPermutationCode);

                insPs.executeUpdate();

				insPs.close();
			}

			rs.close();
			ps.close();
		}
		catch (SQLException e)
		{
			throw new LoggingServiceException("Failed to log result: "+e);
		}

        return true;
	}

	public boolean initiateTest(String testName,
								RunUID runUID,
								String permutationCode,
								int numberOfTasks) throws LoggingServiceException
	{
		try
		{
			// Update row in TestRuns table
			PreparedStatement ps = _conn.prepareStatement("INSERT INTO TestResults (RunID, TestName, PermutationCode, DateTimeStarted, NumberOfTasks, OverallResult, Information) VALUES "+
														  "(?,?,?,now(),?,'Uncertain',' ')");

			ps.setLong(1, runUID.getUID());
			ps.setString(2, testName);
			ps.setString(3, permutationCode);
			ps.setInt(4, numberOfTasks);

			ps.executeUpdate();
			ps.close();
		}
		catch (SQLException e)
		{
			throw new LoggingServiceException("Failed to initiate test: "+e);
		}

		return true;
	}

	public boolean logTestInformation(String testName,
									  RunUID runUID,
									  String permutationCode,
									  String information) throws LoggingServiceException
	{
		String previousInformation = "";

		try
		{
			// Retrieve the test information
			PreparedStatement ps = _conn.prepareStatement("SELECT * FROM TestResults WHERE RunID=? AND TestName=? AND PermutationCode=?");

			ps.setLong(1, runUID.getUID());
			ps.setString(2, testName);
			ps.setString(3, permutationCode);

			ResultSet results = ps.executeQuery();

			if ( results.next() )
			{
				previousInformation = results.getString("Information");
			}

			results.close();
			ps.close();

			ps = _conn.prepareStatement("UPDATE TestResults SET Information = ? WHERE RunId=? AND TestName=? AND PermutationCode=?");
			previousInformation += information;
			ps.setString(1, previousInformation);
			ps.setLong(2, runUID.getUID());
			ps.setString(3, testName);
			ps.setString(4, permutationCode);
			ps.executeUpdate();

			ps.close();
		}
		catch (SQLException e)
		{
			throw new LoggingServiceException("Failed to log test information: "+e);
		}

		return true;
	}

	public boolean initiateTask(String testName,
								RunUID runUID,
								String taskName,
								String taskPermutationCode,
								String testPermutationCode) throws LoggingServiceException
	{
		try
		{
			// Update row in TestRuns table
			PreparedStatement ps = _conn.prepareStatement("INSERT INTO TestTaskResults (RunID, TestName, TaskName, PermutationCode, TaskPermutationCode, TimeLogged,  Information, Result) VALUES "+
														  "(?,?,?,?,?,now(),' ',?)");

			ps.setLong(1, runUID.getUID());
			ps.setString(2, testName);
			ps.setString(3, taskName);
			ps.setString(4, testPermutationCode);
			ps.setString(5, taskPermutationCode);
			ps.setString(6, "Uncertain");

			ps.executeUpdate();
			ps.close();
		}
		catch (SQLException e)
		{
			throw new LoggingServiceException("Failed to initiate task: "+e);
		}

		return true;
	}

	public boolean testComplete(String testName,
								RunUID runUID,
								String permutationCode) throws LoggingServiceException
	{
		int numberOfTasks = -1;
		boolean returnValue = true;

		try
		{
			/**
			 * Get number of tasks involved in this test
			 */
			PreparedStatement ps = _conn.prepareStatement("SELECT NumberOfTasks FROM TestResults WHERE RunId=? AND TestName=? AND PermutationCode=?");

			ps.setLong(1,runUID.getUID());
			ps.setString(2, testName);
			ps.setString(3, permutationCode);

			ResultSet infoRs = ps.executeQuery();
			if (infoRs.next())
			{
				numberOfTasks = infoRs.getInt("NumberOfTasks");
			}
			else
			{
				System.err.println("Cannot find number of tasks for this test");
				returnValue = false;
			}

			ps.close();
			infoRs.close();

			ps = _conn.prepareStatement("SELECT * FROM TestTaskResults WHERE RunId=? AND TestName=? AND PermutationCode=?");

			ps.setLong(1, runUID.getUID());
			ps.setString(2, testName);
			ps.setString(3, permutationCode);

			ResultSet results = ps.executeQuery();

   			String overAllResult = "Passed";
   			int resultCount = 0;

		    while (results.next())
		    {
		   		if ( ( results.getString("Result") == null ) || (!results.getString("Result").equals("Passed") ) )
		   		{
		   			overAllResult = "Failed";
				}
		   		resultCount++;
		   	}

		   	results.close();
			ps.close();

			if ( resultCount == 0 )
			{
				System.out.println("FAILED DUE TO: Number of results:"+resultCount+", Number of tasks: "+numberOfTasks);
				overAllResult = "Failed";
			}

			System.out.println("TestResults entry does exist - updating DateTimeFinished");

			// Update row in TestResults table
			ps = _conn.prepareStatement("UPDATE TestResults SET DateTimeFinished=now(), OverallResult=? WHERE RunId=? AND TestName=? AND PermutationCode=?");

			ps.setString(1, overAllResult);
			ps.setLong(2, runUID.getUID());
			ps.setString(3, testName);
			ps.setString(4, permutationCode);

			ps.executeUpdate();

		 	ps.close();
		}
		catch (SQLException e)
		{
			throw new LoggingServiceException("Failed to log test complete: "+e);
		}

		return returnValue;
	}

	public boolean logTimeout(String testName,
							  RunUID runUID,
							  String permutationCode) throws LoggingServiceException
	{
		try
		{
			// Update row in TestRuns table
			PreparedStatement ps = _conn.prepareStatement("UPDATE TestResults SET TimedOut=1 WHERE RunId=? AND TestName=? AND PermutationCode=?");

			ps.setLong(1, runUID.getUID());
			ps.setString(2, testName);
			ps.setString(3, permutationCode);

			ps.executeUpdate();
			ps.close();
		}
		catch (SQLException e)
		{
			throw new LoggingServiceException("Failed to log timeout: "+e);
		}

		return true;
	}
}
