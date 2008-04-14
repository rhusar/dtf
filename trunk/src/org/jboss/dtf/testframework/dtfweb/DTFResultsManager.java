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
import org.jboss.dtf.testframework.dtfweb.*;
import org.jboss.dtf.testframework.testnode.RunUID;

import java.sql.*;
import javax.sql.DataSource;

import java.util.ArrayList;
import java.io.InputStream;

public class DTFResultsManager
{
    private static DataSource          _pool = DBUtils.getDataSource();

	private ProductDetails[]    _supportedProducts = null;
	private OSDetails[]			_supportedOSs = null;

	public void deleteTestRun(long runId)
	{
        Connection conn = null;

		try
		{
			conn = _pool.getConnection();
			Statement s = conn.createStatement();

			s.execute("DELETE FROM TestRuns WHERE RunId="+runId);
			s.execute("DELETE FROM TestResults WHERE RunId="+runId);
			s.execute("DELETE FROM TestTaskResults WHERE RunId="+runId);

			s.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			System.err.println("ERROR - Cannot remove test run '"+runId+"'");
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
	}

	public String[] getSoftwareVersions()
	{
        Connection conn = null;

		try
		{
			conn = _pool.getConnection();
			System.out.println("Retrieving software version table..");
			/*
			 * Get all software information
			 */
			Statement s = conn.createStatement();

			ResultSet rs = s.executeQuery("SELECT DISTINCT SoftwareVersion FROM TestRuns ORDER BY DateTimeStarted DESC");

			ArrayList softwareVersions = new ArrayList();

			while (rs.next())
			{
				softwareVersions.add(rs.getString("SoftwareVersion"));
			}

			rs.close();
			s.close();

			String[] results = new String[softwareVersions.size()];
			System.arraycopy(softwareVersions.toArray(),0,results,0,softwareVersions.size());

			return(results);
		}
		catch (SQLException e)
		{
			System.err.println("ERROR - While retrieving software versions");
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
		return(new String[0]);
	}


	public TestResultInformation[] getResultsForTestRun(long runid, String orderBy)
	{
        Connection conn = null;

		try
		{
			System.out.println("Retrieving results table..");
			/*
			 * Get all software information
			 */
			conn = _pool.getConnection();
			Statement s = conn.createStatement();

			if (orderBy == null)
				orderBy = "PermutationCode, TestName, DateTimeStarted";

			ResultSet rs = s.executeQuery("SELECT * FROM TestResults WHERE RunId="+runid+" ORDER BY "+orderBy);

			ArrayList resultsInf = new ArrayList();

			while (rs.next())
			{
				TestResultInformation resultInf = new TestResultInformation();

				resultInf.runId = rs.getInt("RunId");
				resultInf.testName = rs.getString("TestName");
				resultInf.permutationCode = rs.getString("PermutationCode");
				resultInf.dateTimeStarted = rs.getTimestamp("DateTimeStarted");
				resultInf.dateTimeFinished = rs.getTimestamp("DateTimeFinished");
				resultInf.numberOfTasks = rs.getInt("NumberOfTasks");
				resultInf.overAllResult = rs.getString("OverAllResult");
				resultInf.information = rs.getString("Information");
                resultInf.taskTimedOut = rs.getBoolean("TimedOut");

				resultsInf.add(resultInf);
			}

			rs.close();
			s.close();

			TestResultInformation[] results = new TestResultInformation[resultsInf.size()];
			System.arraycopy(resultsInf.toArray(),0,results,0,resultsInf.size());

			return(results);
		}
		catch (SQLException e)
		{
			System.err.println("ERROR - While retrieving test results");
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
		return(new TestResultInformation[0]);
	}

    public WebRunInformation[] getSameTestRuns(long runId)
    {
        WebRunInformation runInfo = getTestRunInformation(runId);
        Connection conn = null;

		try
		{
			System.out.println("Retrieving software runs table for testdefsurl '"+runInfo.testDefinitionsURL+"'..");
			/*
			 * Get all software information
			 */
			conn = _pool.getConnection();
			Statement s = conn.createStatement();

			ResultSet rs = s.executeQuery("SELECT * FROM TestRuns WHERE TestDefinitions='"+runInfo.testDefinitionsURL+"' ORDER BY SoftwareVersion, DateTimeStarted DESC");

			ArrayList testRuns = new ArrayList();

			while (rs.next())
			{
				WebRunInformation runInf = new WebRunInformation();

				runInf.runId = rs.getLong("RunId");
				runInf.dateTimeFinished = rs.getTimestamp("DateTimeFinished");
				runInf.dateTimeStarted = rs.getTimestamp("DateTimeStarted");
				runInf.testDefinitionsURL = rs.getString("TestDefinitions");
				runInf.testDefinitionsDescription = rs.getString("TestDefinitionsDescription");
				runInf.testSelectionURL = rs.getString("TestSelection");
				runInf.testSelectionDescription = rs.getString("TestSelectionDescription");
				runInf.softwareVersion = rs.getString("SoftwareVersion");
				runInf.information = rs.getString("Information");

				testRuns.add(runInf);
			}

			rs.close();
			s.close();

			System.out.println("Number of runs found '"+testRuns.size()+"'");

			WebRunInformation[] results = new WebRunInformation[testRuns.size()];
			System.arraycopy(testRuns.toArray(),0,results,0,testRuns.size());

			return(results);
		}
		catch (SQLException e)
		{
			System.err.println("ERROR - While retrieving test runs");
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
		return(new WebRunInformation[0]);
    }

	public WebRunInformation[] getTestRunsForVersion(String version)
	{
        Connection conn = null;

		try
		{
			System.out.println("Retrieving software runs table for version '"+version+"'..");
			/*
			 * Get all software information
			 */
			conn = _pool.getConnection();
			Statement s = conn.createStatement();

			ResultSet rs = s.executeQuery("SELECT * FROM TestRuns WHERE SoftwareVersion='"+version+"' ORDER BY DateTimeStarted DESC");

			ArrayList testRuns = new ArrayList();

			while (rs.next())
			{
				WebRunInformation runInf = new WebRunInformation();

				runInf.runId = rs.getLong("RunId");
				runInf.dateTimeFinished = rs.getTimestamp("DateTimeFinished");
				runInf.dateTimeStarted = rs.getTimestamp("DateTimeStarted");
				runInf.testDefinitionsURL = rs.getString("TestDefinitions");
				runInf.testDefinitionsDescription = rs.getString("TestDefinitionsDescription");
				runInf.testSelectionURL = rs.getString("TestSelection");
				runInf.testSelectionDescription = rs.getString("TestSelectionDescription");
				runInf.softwareVersion = rs.getString("SoftwareVersion");
				runInf.information = rs.getString("Information");

				testRuns.add(runInf);
			}

			rs.close();
			s.close();

			System.out.println("Number of runs found '"+testRuns.size()+"'");

			WebRunInformation[] results = new WebRunInformation[testRuns.size()];
			System.arraycopy(testRuns.toArray(),0,results,0,testRuns.size());

			return(results);
		}
		catch (SQLException e)
		{
			System.err.println("ERROR - While retrieving test runs");
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
		return(new WebRunInformation[0]);
	}

	public WebRunInformation getTestRunInformation(long runId)
	{
		WebRunInformation runInf = null;
		Connection conn = null;

		try
		{
			System.out.println("Retrieving run information for Run Id. '"+runId+"'..");

			conn = _pool.getConnection();
			Statement s = conn.createStatement();

			ResultSet rs = s.executeQuery("SELECT * FROM TestRuns WHERE RunId="+runId);

			if (rs.next())
			{
				runInf = new WebRunInformation();

				runInf.runId = rs.getLong("RunId");
				runInf.dateTimeFinished = rs.getTimestamp("DateTimeFinished");
				runInf.dateTimeStarted = rs.getTimestamp("DateTimeStarted");
				runInf.testDefinitionsURL = rs.getString("TestDefinitions");
				runInf.testDefinitionsDescription = rs.getString("TestDefinitionsDescription");
				runInf.testSelectionURL = rs.getString("TestSelection");
				runInf.testSelectionDescription = rs.getString("TestSelectionDescription");
				runInf.softwareVersion = rs.getString("SoftwareVersion");
				runInf.information = rs.getString("Information");
			}

			if(runInf != null)
			{
				rs = s.executeQuery("SELECT COUNT(TestName) FROM TestResults WHERE RunId=" + runId + " AND OverAllResult=\"Failed\"");
				if(rs.next())
					runInf.fails = rs.getInt(1);
				rs.close();
				rs = s.executeQuery("SELECT COUNT(TestName) FROM TestResults WHERE RunId=" + runId + " AND OverAllResult=\"Passed\"");
				if(rs.next())
					runInf.passes = rs.getInt(1);
				rs.close();
			}

			rs.close();
			s.close();
		}
		catch (SQLException e)
		{
			System.err.println("ERROR - While retrieving test run information");
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

		return(runInf);
	}

	public TestTaskResultInformation[] getTestTaskResults(long runId, String testName, String testPermutationCode)
	{
        Connection conn = null;

		System.out.println("Searching for test task results for RunId:"+runId+" TestName:'"+testName+"' PermutationCode:'"+testPermutationCode+"'");
		try
		{
			ArrayList results = new ArrayList();

			conn = _pool.getConnection();
			Statement s = conn.createStatement();

			String resultsSQL = "SELECT * FROM TestTaskResults WHERE RunId="+ runId +" AND TestName='"+ testName +"' AND PermutationCode='"+ testPermutationCode +"' ORDER BY TimeLogged";
			ResultSet testInformation = s.executeQuery(resultsSQL);

			while (testInformation.next())
			{
				TestTaskResultInformation result = new TestTaskResultInformation();

				result.taskName = testInformation.getString("TaskName");
				result.taskPermutationCode = testInformation.getString("TaskPermutationCode");
				result.timeLogged = testInformation.getTimestamp("TimeLogged");
				result.result = testInformation.getString("Result");
				result.information = testInformation.getString("Information");

				results.add(result);
			}

			s.close();

			System.out.println("Number of test task results found '"+results.size()+"'");

			TestTaskResultInformation[] resultArray = new TestTaskResultInformation[results.size()];

			for (int count=0;count<results.size();count++)
				resultArray[count] = (TestTaskResultInformation)results.get(count);

			return(resultArray);
		}
		catch (SQLException e)
		{
			System.err.println("ERROR - While retrieving Test Task results");
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
		return(null);
	}

	public TestTaskResultInformation getTestTaskResult(long runId, String testName, String taskName, String testPermutationCode)
	{
		TestTaskResultInformation result  = null;

		System.out.println("Searching for test task results for RunId:"+runId+" TestName:'"+testName+"' TaskName:'"+taskName+"' PermutationCode:'"+testPermutationCode+"'");
		Connection conn = null;

		try
		{
			conn = _pool.getConnection();
			Statement s = conn.createStatement();

			String resultsSQL = "SELECT * FROM TestTaskResults WHERE RunId="+ runId +" AND TestName='"+ testName +"' AND TaskName='"+ taskName +"' AND PermutationCode='"+ testPermutationCode +"'";
			ResultSet testInformation = s.executeQuery(resultsSQL);

			if (testInformation.next())
			{
				result = new TestTaskResultInformation();

				result.taskName = testInformation.getString("TaskName");
				result.taskPermutationCode = testInformation.getString("TaskPermutationCode");
				result.timeLogged = testInformation.getTimestamp("TimeLogged");
				result.result = testInformation.getString("Result");
				result.information = testInformation.getString("Information");
			}

			s.close();
		}
		catch (SQLException e)
		{
			System.err.println("ERROR - While retrieving Test Task results");
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
		return(result);
	}

	public static OSDetails[] getSupportedOSs()
	{
		ArrayList results = new ArrayList();
		Connection conn = null;

		try
		{
			conn = _pool.getConnection();
			Statement s = conn.createStatement();

			String osSQL = "SELECT * FROM OSs";
			ResultSet osInfo = s.executeQuery(osSQL);

			while (osInfo.next())
			{
				results.add(new OSDetails(osInfo.getString("Id"),osInfo.getString("Name")));
			}

			osInfo.close();
			s.close();
		}
		catch (SQLException e)
		{
			System.err.println("ERROR - Unexpected exception '"+e+"'");
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

		OSDetails[] returnData = new OSDetails[results.size()];
		System.arraycopy(results.toArray(),0,returnData,0,results.size());

		return(returnData);
	}

	public ProductDetails[] getSupportedProducts()
	{
		ArrayList results = new ArrayList();
		Connection conn = null;

		try
		{
			conn = _pool.getConnection();
			Statement s = conn.createStatement();

			String productsSQL = "SELECT * FROM Products";
			ResultSet productInfo = s.executeQuery(productsSQL);

			while (productInfo.next())
			{
				results.add(new ProductDetails(productInfo.getString("Id"),productInfo.getString("Name")));
			}

			productInfo.close();
			s.close();
		}
		catch (SQLException e)
		{
			System.err.println("ERROR - Unexpected exception '"+e+"'");
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

		ProductDetails[] returnData = new ProductDetails[results.size()];
		System.arraycopy(results.toArray(),0,returnData,0,results.size());

		return(returnData);
	}

	/**
	 * Generate the lists of supported OSs and Products
	 * This method would be called to speedup access to OS and Product ids
	 */
	public void setupOSProductTable()
	{
		_supportedOSs = getSupportedOSs();
		_supportedProducts = getSupportedProducts();
	}

	public String getOSName(String id)
	{
		String result = null;
		Connection conn = null;

		if (_supportedOSs == null)
		{
			try
			{
				conn = _pool.getConnection();
				Statement s = conn.createStatement();

				String osSQL = "SELECT * FROM OSs WHERE Id="+id;
				ResultSet osInfo = s.executeQuery(osSQL);

				if (osInfo.next())
				{
					result = osInfo.getString("Name");
				}

				osInfo.close();
				s.close();
			}
			catch (SQLException e)
			{
				System.err.println("ERROR - Unexpected exception '"+e+"'");
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
		}
		else
		{
			int count=0;

			while ( (count<_supportedOSs.length) && (result == null) )
			{
				if (_supportedOSs[count]._id.equals(id))
					result = _supportedOSs[count]._name;
				count++;
			}
		}

		return(result);
	}

	public String getProductName(String id)
	{
		String result = null;
        Connection conn = null;

		if (_supportedProducts == null)
		{
			try
			{
				conn = _pool.getConnection();
				Statement s = conn.createStatement();

				String productSQL = "SELECT * FROM Products WHERE Id="+id;
				ResultSet productInfo = s.executeQuery(productSQL);

				if (productInfo.next())
				{
					result = productInfo.getString("Name");
				}

				productInfo.close();
				s.close();
			}
			catch (SQLException e)
			{
				System.err.println("ERROR - Unexpected exception '"+e+"'");
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
		}
		else
		{
			int count=0;

			while ( (count<_supportedProducts.length) && (result == null) )
			{
				if (_supportedProducts[count]._id.equals(id))
					result = _supportedProducts[count]._name;
				count++;
			}
		}

		return(result);
	}


	public String getOSProductCombination(String permutationCode, String delimiter)
	{
		return permutationCode.replace('_',' ');
	}

    public ArrayList getTestTaskOutputTypes(long runId, String testName, String taskName, String testPermutationCode)
    {
        ArrayList returnValue = new ArrayList();
        Connection conn = null;

        try
        {
            conn = _pool.getConnection();
			Statement s = conn.createStatement();

            System.out.println("Performing search for RunId="+runId+" AND TestId='"+testName+"' AND TaskName='"+taskName+"' AND PermCode='"+testPermutationCode+"'");
            ResultSet taskOutput = s.executeQuery("SELECT OutputType FROM TaskOutput WHERE RunId="+runId+" AND TestId='"+testName+"' AND TaskName='"+taskName+"' AND PermCode='"+testPermutationCode+"'");

            while (taskOutput.next())
            {
                returnValue.add(taskOutput.getString("OutputType"));
            }

            s.close();
        }
        catch (Exception e)
        {
            System.err.println("Error - while retrieving task output");
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

    public InputStream getTestTaskOutput(long runId, String testName, String taskName, String testPermutationCode, String type)
    {
        InputStream returnValue = null;
        Connection conn = null;

        try
        {
            conn = _pool.getConnection();
			Statement s = conn.createStatement();

            System.out.println("Performing search for RunId="+runId+" AND TestId='"+testName+"' AND TaskName='"+taskName+"' AND PermCode='"+testPermutationCode+"' AND TYPE='"+type+"'");
            ResultSet taskOutput = s.executeQuery("SELECT * FROM TaskOutput WHERE RunId="+runId+" AND TestId='"+testName+"' AND TaskName='"+taskName+"' AND PermCode='"+testPermutationCode+"' AND OutputType='"+type+"'");

            if (taskOutput.next())
            {
                returnValue = taskOutput.getBinaryStream("ResultData");
            }

            s.close();
        }
        catch (Exception e)
        {
            System.err.println("Error - while retrieving task output");
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

	public TestResultInformation getTestResult(long runId, String testName, String testPermutationCode)
	{
        Connection conn = null;

		try
		{
			TestResultInformation result = null;

			conn = _pool.getConnection();
			Statement s = conn.createStatement();

			System.out.println("Retrieving test result for RunId:"+runId+" TestName:"+testName+" PermutationCode:"+testPermutationCode);

			String resultsSQL = "SELECT * FROM TestResults WHERE RunId="+ runId +" AND TestName='"+ testName +"' AND PermutationCode='"+ testPermutationCode +"'";
			ResultSet testInformation = s.executeQuery(resultsSQL);

			if (testInformation.next())
			{
				System.out.println("Test results found!");
				result = new TestResultInformation();

				result.runId = testInformation.getInt("RunId");
				result.testName = testInformation.getString("TestName");
				result.permutationCode = testInformation.getString("PermutationCode");
				result.dateTimeStarted = testInformation.getTimestamp("DateTimeStarted");
				result.dateTimeFinished = testInformation.getTimestamp("DateTimeFinished");
				result.numberOfTasks = testInformation.getInt("NumberOfTasks");
				result.overAllResult = testInformation.getString("OverAllResult");
				result.information = testInformation.getString("Information");
			}
			else
			{
				System.out.println("No test result found!");
			}

			s.close();

			return(result);
		}
		catch (SQLException e)
		{
			System.err.println("ERROR - While retrieving Test results");
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
		return(null);
	}

	public int getNextArchiveIdAndIncrement(Connection conn)
	{
		int returnValue = 0;

		try
		{
			conn.setAutoCommit(false);

			Statement s = conn.createStatement();

			ResultSet configTable = s.executeQuery("SELECT ArchiveId FROM archiveid");

			if (!configTable.next())
			{
				System.out.println("No ArchiveId found - initialising");
				returnValue = 0;
				s.execute("INSERT INTO archiveid VALUES (1)");
			}
			else
			{
				returnValue = configTable.getInt("ArchiveId");
				s.execute("UPDATE archiveid set ArchiveId=ArchiveId+1");
			}

			configTable.close();
			s.close();

			conn.commit();
			conn.setAutoCommit(true);
		}
		catch (Exception e)
		{
			System.out.println("Unexpected Exception - While trying to get ArchiveId and increment");
			e.printStackTrace(System.err);
		}

		return returnValue;
	}

	public boolean isArchived(long runId)
	{
		boolean returnValue = false;
        Connection conn = null;

		try
		{
			conn = _pool.getConnection();

			PreparedStatement ps = conn.prepareStatement("SELECT COUNT(ArchiveId) FROM archivedruns WHERE RunId=?");

			ps.setLong(1, runId);

			ResultSet rs = ps.executeQuery();

			if ( rs.next() )
			{
				int count = rs.getInt(1);

				returnValue = ( count > 0 );
			}

			ps.close();
		}
		catch (SQLException e)
		{
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

		return returnValue;
	}

	public boolean createArchive(String title, String comments, ArrayList runIds)
	{
        Connection conn = null;

		try
		{
			conn = _pool.getConnection();

			int archiveId = getNextArchiveIdAndIncrement(conn);

			PreparedStatement ps = conn.prepareStatement("INSERT INTO archivedresults VALUES (?,?,?,?)");

			ps.setInt(1, archiveId);
			ps.setString(2, title);
			ps.setString(3, comments);
			ps.setTimestamp(4, new java.sql.Timestamp(System.currentTimeMillis()));

			ps.executeUpdate();
			ps.close();

			for (int count=0;count<runIds.size();count++)
			{
				RunUID runId = (RunUID)runIds.get(count);

				PreparedStatement ps2 = conn.prepareStatement("INSERT INTO archivedruns VALUES (?,?)");

				ps2.setInt(1, archiveId);
				ps2.setLong(2, runId.getUID());

				ps2.executeUpdate();

				ps2.close();
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace(System.err);
			return false;
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

		return true;
	}

    static
    {
        try
        {
            DTFResultsLogger.createTables();
        }
        catch (Exception e)
        {
            throw new ExceptionInInitializerError("Exception while creating tables: "+e);
        }
    }
}
