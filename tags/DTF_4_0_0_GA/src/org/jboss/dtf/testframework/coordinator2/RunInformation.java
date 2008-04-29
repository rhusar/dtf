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
 * Arjuna Technologies Ltd.
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: RunInformation.java 170 2008-03-25 18:59:26Z jhalliday $
 */
package org.jboss.dtf.testframework.coordinator2;

import org.jboss.dtf.testframework.testnode.RunUID;

import java.net.URL;
import java.io.Serializable;

public class RunInformation implements Serializable
{
	private URL		_testDefsURL;
	private URL 	_testSelectionURL;
	private String  _distributionList;
	private String	_softwareVersion;
    private RunUID  _runId;
	private String	_currentStatus = "";
	private int		_numberOfTestsRemaining = 0;
	private int		_totalNumberOfTests = 0;

	public RunInformation( URL testDefsURL,
					   			URL testSelectionsURL,
					   			String distributionList,
					   			String softwareVersion)
	{
		_testDefsURL = testDefsURL;
		_testSelectionURL = testSelectionsURL;
		_distributionList = distributionList;
		_softwareVersion = softwareVersion;
	}

	public URL getTestDefinitionsURL()
	{
		return _testDefsURL;
	}

	public URL getTestSelectionURL()
	{
		return _testSelectionURL;
	}

	public String getDistributionList()
	{
		return _distributionList;
	}

	public String getSoftwareVersion()
	{
		return _softwareVersion;
	}

	public void setSoftwareVersion(String s)
	{
		_softwareVersion = s;
	}

    public RunUID getRunId()
    {
        return _runId;
    }

    public void setRunId(RunUID runId)
    {
        _runId = runId;
    }

	public synchronized String getCurrentStatus()
	{
		return _currentStatus;
	}

	public synchronized void setCurrentStatus(String status)
	{
		_currentStatus = status;
	}

	public int getNumberOfTestsRemaining()
	{
		return _numberOfTestsRemaining;
	}

	public int getTotalNumberOfTests()
	{
		return _totalNumberOfTests;
	}

	public void setNumberOfTestsRemaining(int numberOfTests)
	{
		_numberOfTestsRemaining = numberOfTests;
	}

	public void setTotalNumberOfTests(int numberOfTests)
	{
		_totalNumberOfTests = numberOfTests;
	}

	public float getPercentageComplete()
	{
		return ((float)(getTotalNumberOfTests() - getNumberOfTestsRemaining()) / getTotalNumberOfTests());
	}

    public String toString()
    {
        return this.getTestDefinitionsURL()+", "+this.getTestSelectionURL()+", "+this.getSoftwareVersion();
    }
}
