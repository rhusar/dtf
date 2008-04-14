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
 * $Id: DTFPerformanceRunInformation.java 170 2008-03-25 18:59:26Z jhalliday $
 */
package org.jboss.dtf.testframework.dtfweb.performance;

import org.jboss.dtf.testframework.dtfweb.utils.DBUtils;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DTFPerformanceRunInformation
{
    private long     _perfRunId;
    private long     _runId;
    private String   _testName;
    private String   _taskName;
    private String   _permutationCode;

    protected DTFPerformanceRunInformation(long perfRunId, long runId, String testName, String taskName, String permutationCode)
    {
        _perfRunId = perfRunId;
        _runId = runId;
        _testName = testName;
        _taskName = taskName;
        _permutationCode = permutationCode;
    }

    public final long getPerformanceRunId()
    {
        return(_perfRunId);
    }

    public final long getTestRunId()
    {
        return(_runId);
    }

    public final String getTestName()
    {
        return(_testName);
    }

    public final String getTaskName()
    {
        return(_taskName);
    }

    public final String getPermutationCode()
    {
        return(_permutationCode);
    }

    public static DTFPerformanceRunInformation getRunInformation(long perfRunId)
    {
        DTFPerformanceRunInformation returnData = null;
        Connection conn = null;

        try
        {
            conn = DBUtils.getDataSource().getConnection();

            Statement s = conn.createStatement();

            ResultSet rs = s.executeQuery("SELECT * FROM PerfRuns WHERE PerfRunID="+perfRunId);

            if (rs.next())
            {
                returnData = new DTFPerformanceRunInformation( rs.getLong("PerfRunId"), rs.getLong("RunID"),
                                                               rs.getString("TestId"), rs.getString("TaskName"),
                                                               rs.getString("PermCode") );
            }

            rs.close();
        }
        catch (SQLException e)
        {
            System.err.println("Failed to retrieve performance run information");
            e.printStackTrace();
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
        return(returnData);
    }

}
