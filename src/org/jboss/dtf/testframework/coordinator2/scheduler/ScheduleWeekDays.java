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
import java.util.GregorianCalendar;
import java.util.Calendar;

/*
 * Copyright (C) 1998, 1999, 2000, 2001, 2002, 2003
 *
 * Arjuna Technology Ltd.
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: ScheduleWeekDays.java 170 2008-03-25 18:59:26Z jhalliday $
 */

public class ScheduleWeekDays extends ScheduleTimed
{
    private int _hour;
    private int _minute;

	public ScheduleWeekDays(URL testDefsURL,
				 URL testSelectionsURL,
				 String distributionList,
				 String softwareVersion,
				 DeployInformation deploy,
				 int hour, int minute)
	{
		super(testDefsURL, testSelectionsURL, distributionList, softwareVersion, deploy, 0);

        _hour = hour;
        _minute = minute;
	}

	public long getTimeOfNextEvent()
	{
		/** Get now and event time **/
        GregorianCalendar now = new GregorianCalendar();
		GregorianCalendar eventTime = new GregorianCalendar();

		eventTime.set(Calendar.HOUR_OF_DAY, _hour);
		eventTime.set(Calendar.MINUTE, _minute);
		eventTime.set(Calendar.SECOND, 00);

		/** If we have already passed the event time look to tomorrow **/
		if ( now.after(eventTime) )
		{
			do
			{
				/** Add a day to the hours until we are not in the weekend**/
				eventTime.add(Calendar.HOUR_OF_DAY, 24);
			}
			while ( ( eventTime.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY ) || ( ( eventTime.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY ) ) );
		}

		return eventTime.getTime().getTime();
	}

	public boolean isScheduleFinished()
	{
		return false;
	}
}

