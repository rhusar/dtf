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
package org.jboss.dtf.tools.scheduletask;

import org.apache.tools.ant.BuildException;

import java.net.URL;
import java.net.MalformedURLException;

import org.jboss.dtf.testframework.coordinator2.CoordinatorInterface;
import org.jboss.dtf.testframework.coordinator2.DeployInformation;
import org.jboss.dtf.testframework.coordinator2.scheduler.ScheduleWhenPossible;

/*
 * Copyright (C) 1998, 1999, 2000, 2001, 2002, 2003
 *
 * Arjuna Technology Ltd.
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: RunWhenPossibleElement.java 170 2008-03-25 18:59:26Z jhalliday $
 */

public class RunWhenPossibleElement
{
	private DeployElement _deploy = null;
	private TestRunElement _testRun = null;

	public void addDeploy(DeployElement de)
	{
		_deploy = de;
	}

	public void addTestrun(TestRunElement tre)
	{
		_testRun = tre;
	}

	public void invoke(CoordinatorInterface coodinator) throws BuildException
	{
		ScheduleWhenPossible scheduleEntry = null;
		DeployInformation deployInfo = null;

		if (_deploy != null)
		{
			deployInfo = new DeployInformation(_deploy.getProductname(), _deploy.getInstaller());
		}

		if (_testRun != null)
		{
			try
			{
				scheduleEntry = new ScheduleWhenPossible(new URL(_testRun.getTestDefsUrl()), new URL(_testRun.getTestSelectionsUrl()), _testRun.getDistributionList(), _testRun.getSoftwareVersion(), deployInfo);
			}
			catch (MalformedURLException e)
			{
				throw new BuildException("The URL specified is malformed: " + e);
			}
		}

		try
		{
			coodinator.getScheduler().schedule(coodinator, scheduleEntry);
			System.out.println("Schedule updated");
		}
		catch (Exception e)
		{
			throw new BuildException("Failed to create schedule: " + e);
		}
	}
}
