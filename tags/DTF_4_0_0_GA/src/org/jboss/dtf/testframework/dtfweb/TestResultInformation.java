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

import java.sql.SQLException;
import java.sql.ResultSet;

public class TestResultInformation
{
    private final static int PASSED_VALUE = 0, FAILED_VALUE = 1, UNCERTAIN_VALUE = 2;

    private final static String[] RESULT_COLOURS = { "#00C000", "#C00000", "#0090FE" };
    private final static String[] TIMEDOUT_RESULT_COLOURS = { "#CACC20", "#CACC20", "#CACC20" };

	private final static String PASSED = "Passed";
	private final static String FAILED = "Failed";
	private final static String UNCERTAIN = "Uncertain";

	public long 				runId;
	public String				testName;
	public String				permutationCode;
	public java.sql.Timestamp	dateTimeStarted;
	public java.sql.Timestamp	dateTimeFinished;
	public int					numberOfTasks;
	public String				overAllResult;
	public String				information;
    public boolean              taskTimedOut;

	public TestResultInformation()
	{

	}

	public TestResultInformation(ResultSet rs)
		throws SQLException
	{
		runId = rs.getInt("RunId");
		testName = rs.getString("TestName");
		permutationCode = rs.getString("PermutationCode");
		dateTimeStarted = rs.getTimestamp("DateTimeStarted");
		dateTimeFinished = rs.getTimestamp("DateTimeFinished");
		numberOfTasks = rs.getInt("NumberOfTasks");
		overAllResult = rs.getString("OverAllResult");
		information = rs.getString("Information");
		taskTimedOut = rs.getBoolean("TimedOut");
	}

	public boolean isSameTestAs(TestResultInformation resultInfo)
	{
		return testName.equals(resultInfo.testName);
	}

	public boolean hasTestPassed()
	{
		return(overAllResult.equalsIgnoreCase(PASSED));
	}

	public boolean hasTestFailed()
	{
		return(overAllResult.equalsIgnoreCase(FAILED));
	}

	public long getDuration()
	{
		return dateTimeFinished.getTime() - dateTimeStarted.getTime();
	}

    public boolean hasTestTimedout()
    {
        return(taskTimedOut);
    }

    public String getColor()
    {
        if ( hasTestPassed() )
        {
            return( hasTestTimedout() ? TIMEDOUT_RESULT_COLOURS[PASSED_VALUE]:RESULT_COLOURS[PASSED_VALUE] );
        }
        else
        {
            if ( hasTestFailed() )
            {
                return( hasTestTimedout() ? TIMEDOUT_RESULT_COLOURS[FAILED_VALUE]:RESULT_COLOURS[FAILED_VALUE] );
            }
        }

        return( hasTestTimedout() ? TIMEDOUT_RESULT_COLOURS[UNCERTAIN_VALUE]:RESULT_COLOURS[UNCERTAIN_VALUE] );
    }
}
