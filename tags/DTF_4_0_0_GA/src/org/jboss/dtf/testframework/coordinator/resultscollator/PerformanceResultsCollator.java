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
 * $Id: PerformanceResultsCollator.java 170 2008-03-25 18:59:26Z jhalliday $
 */
package org.jboss.dtf.testframework.coordinator.resultscollator;

import javax.sql.DataSource;
import javax.naming.InitialContext;
import java.sql.*;
import java.io.*;
import java.util.StringTokenizer;
import java.util.ArrayList;
import java.awt.*;

public class PerformanceResultsCollator implements ResultsCollatorPlugin
{
    private static DataSource  _ds = null;
    private final static String PERF_DATA = "<PERF_DATA";

    public boolean initialise()
    {
        boolean returnValue = false;

        try
        {
            InitialContext ctxt = new InitialContext();

            _ds = (DataSource)ctxt.lookup("jdbc/ResultsDB");

            ensureTablesExist();

            returnValue = true;
        }
        catch (Exception e)
        {
            System.err.println("Failed to lookup database datasource: "+e);
        }

        return returnValue;
    }

    private static void ensureTablesExist() throws Exception
    {
        System.out.println("Retrieving database connection...");
        Connection conn = _ds.getConnection();
        Statement s = conn.createStatement();

        try
        {
            System.out.print("Ensuring 'perfruns' table exists: ");
            s.executeUpdate("CREATE TABLE PerfRuns (RunID INT, DataName VARCHAR(255), TestId VARCHAR(255), TaskName VARCHAR(255), PermCode VARCHAR(255), PerfRunId INT, XAxisLabel VARCHAR(255), YAxisLabel VARCHAR(255))");
            System.out.println("Success");
        }
        catch (SQLException e)
        {
            System.out.println("Already exists");
        }

        try
        {
            System.out.print("Ensuring 'perfdata' table exists: ");
            s.executeUpdate("CREATE TABLE PerfData (PerfRunId INT, XData DOUBLE, YData DOUBLE)");
            System.out.println("Success");
        }
        catch (SQLException e)
        {
            System.out.println("Already exists");
        }

        try
        {
            System.out.print("Ensuring 'perfconfig' table exists: ");
            s.executeUpdate("CREATE TABLE PerfConfig (NextPerfRunId INT)");
            s.executeUpdate("INSERT INTO PerfConfig VALUES (0)");

            System.out.println("Success");
        }
        catch (SQLException e)
        {
            System.out.println("Already exists");
        }

        s.close();
        conn.close();
    }

    public void processResults( long          runId,
                                String        testId,
                                String        taskName,
                                String        permCode,
                                String        outputType,
                                int           streamLength,
                                InputStream   in) throws Exception
    {
        try
        {
            /**
             *   <PERF_DATA[name]:x,y:x,y:x,y>
             */

            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String inLine;
            long perfRunId = -1;


            while ( ( inLine = reader.readLine() ) != null )
            {
                if ( inLine.startsWith(PERF_DATA) )
                {
                    inLine = inLine.substring(PERF_DATA.length());

                    String name = inLine.substring(inLine.indexOf('[') + 1);
                    String xAxisLabel = null;
                    String yAxisLabel = null;

                    name = name.substring(0, name.indexOf(']'));

                    if ( inLine.indexOf('{') != -1 )
                    {
                        inLine = inLine.substring( inLine.indexOf('{') + 1 );
                        xAxisLabel = inLine.substring(0, inLine.indexOf('}'));
                        inLine = inLine.substring( inLine.indexOf('{') + 1 );
                        yAxisLabel = inLine.substring(0, inLine.indexOf('}'));
                    }

                    perfRunId = generatePerfRunId(name, runId, testId, taskName, permCode, xAxisLabel, yAxisLabel);

                    inLine = inLine.substring(inLine.indexOf(':') + 1);
                    parse(perfRunId, inLine);
                }
            }

            reader.close();
        }
        catch (IOException e)
        {
            throw new Exception("An unexpected exception occurred while parsing the data: "+e);
        }
    }

    private long generatePerfRunId(String dataName, long runId, String testId, String taskName, String permCode, String xAxisLabel, String yAxisLabel) throws Exception
    {
        long perfRunId = 0;

        System.out.println("Retrieving database connection...");
        Connection conn = _ds.getConnection();
        conn.setAutoCommit(false);

        Statement s = conn.createStatement();

        ResultSet rs = s.executeQuery("SELECT * FROM PerfConfig");

        if ( rs.next() )
        {
            perfRunId = rs.getLong( "NextPerfRunId" );
        }

        s.executeUpdate("UPDATE PerfConfig SET NextPerfRunId=NextPerfRunId+1");
        s.executeUpdate("INSERT INTO PerfRuns VALUES ("+runId+",'"+dataName+"','"+testId+"','"+taskName+"','"+permCode+"',"+perfRunId+",'"+xAxisLabel+"','"+yAxisLabel+"')");

        conn.commit();

        conn.setAutoCommit(true);

        conn.close();

        return(perfRunId);
    }

    private void parse(long perfRunId, String data) throws Exception
    {
        Connection conn = null;

        try
        {
            StringTokenizer st = new StringTokenizer(data,":>");
            System.out.println("Retrieving database connection...");
            conn = _ds.getConnection();

            System.out.println("Updating table...");

            while (st.hasMoreTokens())
            {
                String token = st.nextToken();

                if ( token.indexOf(',') == -1 )
                {
                    throw new Exception("Malformed performance data - no , delimiter");
                }

                PreparedStatement s = conn.prepareStatement("INSERT INTO PerfData VALUES (?,?,?)");
                s.setLong(1, perfRunId);
                s.setDouble(2, Double.parseDouble(token.substring(0, token.indexOf(','))));
                s.setDouble(3, Double.parseDouble(token.substring(token.indexOf(',') +1 )) );
                s.execute();
            }

        }
        catch (SQLException e)
        {
            throw new Exception("Failed to update the database: "+e);
        }
        finally
        {
            if ( conn != null )
            {
                conn.close();
            }
        }
    }
}
