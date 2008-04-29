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

/*
 * Copyright (C) 1998, 1999, 2000, 2001, 2002, 2003
 *
 * Arjuna Technologies Ltd.
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: ComparisonReport.java 170 2008-03-25 18:59:26Z jhalliday $
 */

import org.jboss.dtf.testframework.dtfweb.utils.DBUtils;
import java.io.PrintStream;
import java.sql.*;
import java.util.AbstractCollection;
import java.util.ArrayList;
import javax.sql.DataSource;

public class ComparisonReport
{

    public ComparisonReport()
    {
        _results = new ArrayList();
    }

    protected void addResultComparison(ResultComparison resultComparison)
    {
        _results.add(resultComparison);
    }

    private void addInFirstOnly(TestResultInformation resultInfo)
    {
        _results.add(new InOneSetOnly(true, resultInfo));
    }

    private void addInSecondOnly(TestResultInformation resultInfo)
    {
        _results.add(new InOneSetOnly(false, resultInfo));
    }

    public ArrayList getResults()
    {
        return _results;
    }

    public static ComparisonReport compare(long runId, long runId2)
    {
        ComparisonReport report = new ComparisonReport();
        Connection conn = null;
        System.out.println("Generating comparison");
        try
        {
            ArrayList runResults[] = new ArrayList[2];
            runResults[0] = new ArrayList();
            runResults[1] = new ArrayList();
            conn = _pool.getConnection();
            PreparedStatement s = conn.prepareStatement("SELECT * FROM TestResults WHERE TestResults.RunId=? ORDER BY TestResults.DateTimeStarted, TestResults.TestName");
            s.setLong(1, runId);
            ResultSet rs = s.executeQuery();
            int recCount = 0;
            TestResultInformation resultInfo;
            for(; rs.next(); runResults[0].add(resultInfo))
            {
                resultInfo = new TestResultInformation(rs);
                System.out.println("Adding result " + recCount++);
            }

            rs.close();
            s.close();
            s = conn.prepareStatement("SELECT * FROM TestResults WHERE TestResults.RunId=? ORDER BY TestResults.DateTimeStarted, TestResults.TestName");
            s.setLong(1, runId2);
            rs = s.executeQuery();
            recCount = 0;
            for(; rs.next(); runResults[1].add(resultInfo))
            {
                resultInfo = new TestResultInformation(rs);
                System.out.println("Adding result2 " + recCount++);
            }

            rs.close();
            s.close();
            conn.close();
            while(!runResults[0].isEmpty())
            {
                resultInfo = (TestResultInformation)runResults[0].remove(0);
                TestResultInformation resultInfo2 = null;
                System.out.println("Looking for '" + resultInfo.testName + "' (" + runResults[0].size() + " elements remaining)");
                for(int count2 = 0; count2 < runResults[1].size(); count2++)
                {
                    resultInfo2 = (TestResultInformation)runResults[1].get(count2);
                    if(!resultInfo2.isSameTestAs(resultInfo))
                        continue;
                    runResults[1].remove(resultInfo2);
                    break;
                }

                if(resultInfo2 != null && resultInfo2.isSameTestAs(resultInfo))
                {
                    System.out.println("Found creating result comparison");
                    report.addResultComparison(new ResultComparison(resultInfo, resultInfo2));
                } else
                {
                    System.out.println("Not found adding to first only");
                    runResults[0].remove(resultInfo);
                    report.addInFirstOnly(resultInfo);
                }
            }
            for(int count = 0; count < runResults[1].size(); count++)
                report.addInSecondOnly((TestResultInformation)runResults[1].get(count));

        }
        catch(SQLException e)
        {
            System.err.println("ERROR - While retrieving test results");
            e.printStackTrace(System.err);
        }
        finally
        {
            if(conn != null)
                try
                {
                    conn.close();
                }
                catch(SQLException e)
                {
                    e.printStackTrace(System.err);
                }
        }
        System.out.println("Comparison generated");
        return report;
    }

    private static DataSource _pool = DBUtils.getDataSource();
    private ArrayList _results;

}
