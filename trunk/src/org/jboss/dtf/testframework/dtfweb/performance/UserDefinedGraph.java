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
 * $Id: UserDefinedGraph.java 170 2008-03-25 18:59:26Z jhalliday $
 */
package org.jboss.dtf.testframework.dtfweb.performance;

import org.jboss.dtf.testframework.dtfweb.utils.DBUtils;

import javax.servlet.http.HttpSession;
import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Hashtable;
import java.sql.*;

public class UserDefinedGraph
{
    private final static String USERGRAPH_PREFIX = "com.arjuna.mw.testframework.performance.usergraph";

    private ArrayList   _series = new ArrayList();
    private Hashtable   _seriesNames = new Hashtable();
    private String      _xAxisLabel = "";
    private String      _dataName = null;
    private String      _yAxisLabel = "";

    public final String getSeriesName(int series)
    {
        return( (String)_seriesNames.get( new Integer(series) ) );
    }

    public final void setSeriesName(int series, String name)
    {
        _seriesNames.put( new Integer(series), name );
    }

    public final String getXAxisLabel()
    {
        return(_xAxisLabel);
    }

    public final String getYAxisLabel()
    {
        return(_yAxisLabel);
    }

    public final void setXAxisLabel(String name)
    {
        _xAxisLabel = name;
    }

    public final void setYAxisLabel(String name)
    {
        _yAxisLabel = name;
    }

    public final boolean contains(long runId)
    {
        return( _series.contains(new Long(runId)) );
    }

    public final void deleteSeries(int series)
    {
        _series.remove(series);
    }

    public final long[] getPerformanceDataList()
    {
        long[] results = new long[_series.size()];

        for (int count=0;count<_series.size();count++)
        {
            results[count] = ((Long)_series.get(count)).longValue();
        }

        return(results);
    }

    public final int addPerformanceData(long perfRunId)
    {
        _series.add(new Long(perfRunId));

        return( _series.indexOf(new Long(perfRunId)) );
    }

    public final static UserDefinedGraph getGraph(HttpSession session)
    {
        return ( (UserDefinedGraph) session.getAttribute(USERGRAPH_PREFIX) );
    }

    public final static void deleteGraph(String name)
    {
        Connection conn = null;

        try
        {
            DataSource ds = DBUtils.getDataSource();
            conn = ds.getConnection();

            PreparedStatement s = conn.prepareStatement("DELETE FROM SavedPerfSets WHERE Name=?");
            s.setString(1, name);
            s.execute();

            s = conn.prepareStatement("DELETE FROM SavedPerfSetsData WHERE Name=?");
            s.setString(1, name);
            s.execute();
        }
        catch (SQLException e)
        {
            System.err.println("ERROR - Failed to load the performance run data set");
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
    }

    public final void setDataName(String name)
    {
        _dataName = name;
    }

    public final String getDataName()
    {
        return(_dataName);
    }

    public final void setGraph(HttpSession session)
    {
        session.setAttribute(USERGRAPH_PREFIX, this);
    }

    public final static UserDefinedGraph createGraph(HttpSession session)
    {
        UserDefinedGraph graph = new UserDefinedGraph();
        session.setAttribute( USERGRAPH_PREFIX, graph );
        return(graph);
    }

    public final static UserDefinedGraph load(String dataSetName)
    {
        UserDefinedGraph udg = new UserDefinedGraph();
        Connection conn = null;

        try
        {
            DataSource ds = DBUtils.getDataSource();
            conn = ds.getConnection();
            PreparedStatement s = conn.prepareStatement("SELECT * FROM SavedPerfSets WHERE Name=?");

            s.setString(1, dataSetName);
            ResultSet rs = s.executeQuery();

            if ( rs.next() )
            {
                udg.setDataName(rs.getString("Name"));
                udg.setXAxisLabel(rs.getString("XAxisLabel"));
                udg.setYAxisLabel(rs.getString("YAxisLabel"));

                PreparedStatement s2 = conn.prepareStatement("SELECT * FROM SavedPerfSetsData WHERE Name=?");

                s2.setString(1, dataSetName);
                ResultSet rs2 = s2.executeQuery();

                while (rs2.next())
                {
                    long perfRunId = rs2.getLong("PerfRunId");
                    String seriesLabel = rs2.getString("SeriesLabel");
                    int seriesId = udg.addPerformanceData(perfRunId);
                    udg.setSeriesName(seriesId, seriesLabel);
                }

                rs2.close();
                s2.close();
            }

            rs.close();
            s.close();
        }
        catch (SQLException e)
        {
            System.err.println("ERROR - Failed to load the performance run data set");
            e.printStackTrace(System.err);
            udg = null;
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

        return(udg);
    }

    public final boolean save(String dataSetName)
    {
        boolean success = true;
        Connection conn = null;

        try
        {
            DataSource ds = DBUtils.getDataSource();
            conn = ds.getConnection();
            PreparedStatement s = conn.prepareStatement("INSERT INTO SavedPerfSets VALUES (?,?,?,?)");

            s.setString(1, dataSetName);
            s.setTimestamp(2, new java.sql.Timestamp(System.currentTimeMillis()));
            s.setString(3, _xAxisLabel);
            s.setString(4, _yAxisLabel);
            s.execute();
            s.close();

            long[] perfRunIds = getPerformanceDataList();

            for (int count=0;count<perfRunIds.length;count++)
            {
                s = conn.prepareStatement("INSERT INTO SavedPerfSetsData VALUES (?,?,?)");

                s.setString(1, dataSetName);
                s.setLong(2, perfRunIds[count]);
                s.setString(3, getSeriesName(count));
                s.execute();

                s.close();
            }
        }
        catch (SQLException e)
        {
            System.err.println("ERROR - Failed to save the performance run data set");
            e.printStackTrace(System.err);
            success = false;
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

        return(success);
    }
}
