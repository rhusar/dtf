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
package org.jboss.dtf.testframework.coordinator2.scheduler;

import org.jboss.dtf.testframework.coordinator2.DeployInformation;

import java.net.URL;
import java.util.Calendar;
import java.util.GregorianCalendar;

/*
 * Copyright (C) 1998, 1999, 2000, 2001, 2002, 2003
 *
 * Arjuna Technology Ltd.
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: ScheduleOneTimeOnly.java 170 2008-03-25 18:59:26Z jhalliday $
 */

public class ScheduleOneTimeOnly extends ScheduleTimed
{
	private boolean		_finished = false;

	public ScheduleOneTimeOnly(URL testDefsURL,
				 URL testSelectionsURL,
				 String distributionList,
				 String softwareVersion,
	             DeployInformation deploy,
				 long time)
	{
		super(testDefsURL, testSelectionsURL, distributionList, softwareVersion, deploy, time);
	}

    public ScheduleOneTimeOnly(URL testDefsURL,
                 URL testSelectionsURL,
                 String distributionList,
                 String softwareVersion,
                 DeployInformation deploy,
                 int hour, int minute)
    {
        super(testDefsURL, testSelectionsURL, distributionList, softwareVersion, deploy, hour, minute);
    }

    public ScheduleOneTimeOnly(ScheduleInformation schedule, int minuteOffsetFromNow)
    {
        super(schedule, System.currentTimeMillis() + (minuteOffsetFromNow * 60000) );
    }

	public boolean isScheduleFinished()
	{
		return _finished;
	}

	public void invoke() throws InterruptedException
	{
		super.invoke();

		_finished = true;
	}
}
