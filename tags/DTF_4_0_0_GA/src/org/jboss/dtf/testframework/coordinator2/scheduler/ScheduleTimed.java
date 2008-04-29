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
 * Copyright (C) 1998, 1999, 2000, 2001, 2002, 2003
 *
 * Arjuna Technology Ltd.
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: ScheduleTimed.java 170 2008-03-25 18:59:26Z jhalliday $
 */

package org.jboss.dtf.testframework.coordinator2.scheduler;

import org.jboss.dtf.testframework.coordinator2.DeployInformation;
import java.net.URL;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Calendar;

public abstract class ScheduleTimed extends ScheduleInformation
{
	public final static int EVENT_COMPLETE = 0x00;

	private long 	_time;

	public ScheduleTimed(URL testDefsURL,
				 URL testSelectionsURL,
				 String distributionList,
				 String softwareVersion,
				 DeployInformation deploy,
	             long time)
	{
		super(testDefsURL, testSelectionsURL, distributionList, softwareVersion, deploy);

		_time = time;
	}

    public ScheduleTimed(URL testDefsURL,
                 URL testSelectionsURL,
                 String distributionList,
                 String softwareVersion,
                 DeployInformation deploy,
                 int hour, int minute)
    {
        super(testDefsURL, testSelectionsURL, distributionList, softwareVersion, deploy);

        GregorianCalendar gc = new GregorianCalendar();
        gc.set(Calendar.HOUR_OF_DAY, hour);
        gc.set(Calendar.MINUTE, minute);
        gc.set(Calendar.SECOND, 0);

        _time = gc.getTime().getTime();
    }

    public ScheduleTimed(ScheduleInformation schedule, long time)
    {
        this( schedule.getTestDefinitionsURL(), schedule.getTestSelectionURL(),
              schedule.getDistributionList(), schedule.getSoftwareVersion(),
              schedule.getDeployInformation(), time );
    }

	public long getTimeOfNextEvent()
    {
        return _time;
    }

	public final boolean isBefore(ScheduleTimed next)
	{
		return getTimeOfNextEvent() < next.getTimeOfNextEvent();
	}

	public final boolean isAfter(ScheduleTimed next)
	{
		return getTimeOfNextEvent() > next.getTimeOfNextEvent();
	}

	public void invoke() throws InterruptedException
	{
		long timeToWait = getTimeOfNextEvent() - System.currentTimeMillis();

        if ( timeToWait < 0 )
        {
            timeToWait = 1000;
        }

        System.out.println("Sleeping for "+timeToWait+"ms");

		/** Sleep until it's time to go **/
		Thread.sleep(timeToWait);
	}

	public String toString()
	{
		return "["+new Date(getTimeOfNextEvent()).toString()+"]";
	}
}
