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
 * Copyright (C) 2001, 2002,
 *
 * Hewlett-Packard Arjuna Labs,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: DTFPerformanceResultManager.java 170 2008-03-25 18:59:26Z jhalliday $
 */
package org.jboss.dtf.testframework.dtfweb.performance;

import org.jboss.dtf.testframework.dtfweb.utils.DBUtils;

import javax.sql.DataSource;
import java.sql.*;
import java.awt.*;
import java.util.ArrayList;

public class DTFPerformanceResultManager
{
    private static DataSource  _pool = DBUtils.getDataSource();

    public final static long NONE = -1;

    public static long getPerformanceRunId(long runId, String testName, String taskName, String permutationCode)
    {
        long returnValue = NONE;
        Connection conn = null;

        try
        {
            conn = _pool.getConnection();

            PreparedStatement s = conn.prepareStatement("SELECT * FROM PerfRuns WHERE RunId=? AND TestId=? AND TaskName=? AND PermCode=?");
            s.setLong(1, runId);
            s.setString(2, testName);
            s.setString(3, taskName);
            s.setString(4, permutationCode);

            ResultSet rs = s.executeQuery();

            if (rs.next())
            {
                returnValue = rs.getLong("PerfRunId");
            }

            rs.close();
            s.close();
        }
        catch (SQLException e)
        {
            System.err.println("Failed to retrieve performance run information");
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

    public String getXAxisLabel(long runId, String testName, String taskName, String permutationCode)
    {
        String returnValue = null;
        Connection conn = null;

        try
        {
            conn = _pool.getConnection();

            PreparedStatement s = conn.prepareStatement("SELECT XAxisLabel FROM PerfRuns WHERE RunId=? AND TestId=? AND TaskName=? AND PermCode=?");
            s.setLong(1, runId);
            s.setString(2, testName);
            s.setString(3, taskName);
            s.setString(4, permutationCode);

            ResultSet rs = s.executeQuery();

            if (rs.next())
            {
                returnValue = rs.getString("XAxisLabel");
            }

            rs.close();
            s.close();
        }
        catch (SQLException e)
        {
            System.err.println("Failed to retrieve performance run information");
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

    public String getYAxisLabel(long runId, String testName, String taskName, String permutationCode)
    {
        String returnValue = null;
        Connection conn = null;

        try
        {
            conn = _pool.getConnection();

            PreparedStatement s = conn.prepareStatement("SELECT YAxisLabel FROM PerfRuns WHERE RunId=? AND TestId=? AND TaskName=? AND PermCode=?");
            s.setLong(1, runId);
            s.setString(2, testName);
            s.setString(3, taskName);
            s.setString(4, permutationCode);

            ResultSet rs = s.executeQuery();

            if (rs.next())
            {
                returnValue = rs.getString("YAxisLabel");
            }

            rs.close();
            s.close();
        }
        catch (SQLException e)
        {
            System.err.println("Failed to retrieve performance run information");
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

    public boolean hasPerformanceResults(long runId, String testName, String taskName, String permutationCode)
    {
        boolean returnValue = false;
        Connection conn = null;

        try
        {
            conn = _pool.getConnection();

            PreparedStatement s = conn.prepareStatement("SELECT * FROM PerfRuns WHERE RunId=? AND TestId=? AND TaskName=? AND PermCode=?");
            s.setLong(1, runId);
            s.setString(2, testName);
            s.setString(3, taskName);
            s.setString(4, permutationCode);

            ResultSet rs = s.executeQuery();

            returnValue = rs.next();
        }
        catch (SQLException e)
        {
            System.err.println("Failed to retrieve performance run information");
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

    public static XYDataSeries getPerformanceDataSeries(Color c, long perfRunId)
    {
        XYDataSeries data = null;
        Connection conn = null;

        try
        {
            conn = _pool.getConnection();

            Statement s = conn.createStatement();

            ResultSet rs = s.executeQuery("SELECT * FROM PerfData WHERE PerfRunId="+ perfRunId);

            data = new XYDataSeries(c);

            while (rs.next())
            {
                double x = rs.getDouble("XData");
                double y = rs.getDouble("YData");
                XYDataPoint p = new XYDataPoint(x,y);
                data.addPoint(p);
            }

            rs.close();
        }
        catch (SQLException e)
        {
            System.err.println("Failed to retrieve performance run information");
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
        return(data);
    }

    public static long[] getPerformanceDataWithName(String dataName)
    {
        long[] returnArray = null;
        Connection conn = null;

        try
        {
            conn = _pool.getConnection();

            Statement s = conn.createStatement();

            ResultSet rs = s.executeQuery("SELECT * FROM PerfRuns WHERE DataName='"+ dataName +"'");

            ArrayList results = new ArrayList();

            while (rs.next())
            {
                results.add( new Long( rs.getLong("PerfRunId") ) );
            }

            rs.close();
            s.close();

            returnArray = new long[results.size()];

            for (int count=0;count<returnArray.length;count++)
            {
                returnArray[count] = ((Long)results.get(count)).longValue();
            }
        }
        catch (SQLException e)
        {
            System.err.println("Failed to retrieve performance run information");
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

        return returnArray;
    }

    public static ArrayList getPerformanceData(long perfRunId)
    {
        ArrayList data = null;
        Connection conn = null;

        try
        {
            conn = _pool.getConnection();

            Statement s = conn.createStatement();

            ResultSet rs = s.executeQuery("SELECT * FROM PerfData WHERE PerfRunId="+ perfRunId);

            data = new ArrayList();

            while (rs.next())
            {
                double x = rs.getDouble("XData");
                double y = rs.getDouble("YData");
                XYDataPoint p = new XYDataPoint(x,y);
                data.add(p);
            }

            rs.close();
        }
        catch (SQLException e)
        {
            System.err.println("Failed to retrieve performance run information");
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
        return(data);
    }

}
