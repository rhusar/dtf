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
 * $Id: ScheduleInformation.java 170 2008-03-25 18:59:26Z jhalliday $
 */
package org.jboss.dtf.testframework.coordinator2.scheduler;

import org.jboss.dtf.testframework.coordinator2.RunInformation;
import org.jboss.dtf.testframework.coordinator2.DeployInformation;
import org.jboss.dtf.testframework.coordinator2.CoordinatorInterface;

import java.net.URL;

public abstract class ScheduleInformation extends RunInformation
{
    public final static int                LOW_PRIORITY = 0, MEDIUM_PRIORITY = 1, HIGH_PRIORITY = 2;
    private final static String[]          PRIORITY_TEXT = { "Low", "Medium", "High" };

	private DeployInformation 				_deployInformation = null;
    private transient CoordinatorInterface	_coordinator = null;
	private long							_unqiueId = 0;
    private int                             _priority = MEDIUM_PRIORITY;

	public ScheduleInformation(URL testDefsURL,
				 URL testSelectionsURL,
				 String distributionList,
				 String softwareVersion,
				 DeployInformation deployInfo)
	{
		super(testDefsURL, testSelectionsURL, distributionList, softwareVersion);

		_deployInformation = deployInfo;
	}

    public final int getPriority()
    {
        return _priority;
    }

    public final String getPriorityText()
    {
        return PRIORITY_TEXT[getPriority()];
    }

    public final void setPriority(int priority)
    {
        _priority = priority;
    }

	public final DeployInformation getDeployInformation()
	{
		return _deployInformation;
	}

	public final boolean containsDeploymentInformation()
	{
		return _deployInformation != null;
	}

	public abstract boolean isScheduleFinished();

	public abstract void invoke() throws InterruptedException;

	public final void setCoordinator(CoordinatorInterface coord)
	{
		_coordinator = coord;
	}

	public final CoordinatorInterface getCoordinator()
	{
		return _coordinator;
	}

	public final long getUID()
	{
		return _unqiueId;
	}

	public final void setUID(long uid)
	{
		_unqiueId = uid;
	}

	public String toString()
	{
		return this.getTestDefinitionsURL()+", "+this.getTestSelectionURL()+", "+this.getDistributionList()+", "+this.getSoftwareVersion();
	}
}
