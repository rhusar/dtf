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
 * Arjuna Technology Ltd.
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: DailySchedule.java 170 2008-03-25 18:59:26Z jhalliday $
 */
package org.jboss.dtf.testframework.coordinator2.scheduler.types;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Calendar;

public class DailySchedule extends ScheduleInformation
{
	public static final int EVERY_DAY = 1, WEEKDAYS = 2;

	private Date	_startDateTime = null;
	private int		_performThisTask;

	public DailySchedule(Date startDateTime, int performThisTask)
	{
		super(DAILY);

		_startDateTime = startDateTime;
		_performThisTask = performThisTask;
	}

	public Date getStartDateTime()
	{
		return _startDateTime;
	}

	public int getPerformThisTask()
	{
		return _performThisTask;
	}

	/**
	 * Retrieves the a Date object containing the time and date this
	 * schedule next becomes active.
	 *
	 * @return The next activation time.
	 */
	public Calendar getNextActivationTime()
	{
		Calendar returnValue = null;
		GregorianCalendar today = new GregorianCalendar();
		GregorianCalendar startDateTime = new GregorianCalendar();
		startDateTime.setTime(_startDateTime);

		/** Has the start date/time passed? **/
		if ( startDateTime.before(today) )
		{
			switch (getPerformThisTask())
			{
				case EVERY_DAY:
					/**
					 * Are we before todays kick off?
					 */
					if ( compareTime( today, startDateTime ) < 0 )
					{
						/** Return todays date with the time it should start **/
						returnValue = buildTime(today, startDateTime);
					}
					else
					{
						/** Return tomorrows date with the time it should start **/
						today.add(Calendar.DAY_OF_MONTH, 1);
						returnValue = buildTime(today, startDateTime);
					}
					break;

				case WEEKDAYS:
					/** If today is a weekday **/
					if ( isWeekDay(today) )
					{
						/** Have we already passed the date/time **/
						if ( compareTime(today, startDateTime) > 0 )
						{
							today.add(Calendar.DAY_OF_MONTH, 1);

							/**
							 * Set the time/date to be tomorrow at the next kick off time
							 */
							today = (GregorianCalendar)buildTime(today, startDateTime);
						}
						else
						{
							today = (GregorianCalendar)buildTime(today, startDateTime);
						}
					}

					/**
					 * Find the next weekday
					 */
					while (!isWeekDay(today))
					{
						today.add(Calendar.DAY_OF_MONTH, 1);
					}

					returnValue = today;
					break;

			}
		}
		else
			returnValue = startDateTime;

		return returnValue;
	}

	/**
	 * Returns true if the schedule has now finished.
	 *
	 * @return True if the schedule is finished.
	 */
	public boolean isScheduleFinished()
	{
		/** Daily Schedules do not finish **/
		return false;
	}

	private static Calendar buildTime(Calendar date, Calendar time)
	{
		Calendar returnValue = (Calendar)date.clone();

		returnValue.set(Calendar.HOUR, time.get(Calendar.HOUR));
		returnValue.set(Calendar.MINUTE, time.get(Calendar.MINUTE));
		returnValue.set(Calendar.SECOND, time.get(Calendar.SECOND));
		returnValue.set(Calendar.MILLISECOND, time.get(Calendar.MILLISECOND));

		return returnValue;
	}

	private static boolean isWeekDay(Calendar date)
	{
		int day = date.get(Calendar.DAY_OF_WEEK);

		return !( day == Calendar.SATURDAY || day == Calendar.SUNDAY );
	}

	private static int compareTime(Calendar a, Calendar b)
	{
		int aHour = a.get(Calendar.HOUR);
		int aMin = a.get(Calendar.MINUTE);
		int aSec = a.get(Calendar.SECOND);

		long aVal = ( aSec * 1000 ) + ( aMin * 1000 * 60 ) + ( aHour * 1000 * 60 * 60 );

		int bHour = b.get(Calendar.HOUR);
		int bMin = b.get(Calendar.MINUTE);
		int bSec = b.get(Calendar.SECOND);

		long bVal = ( bSec * 1000 ) + ( bMin * 1000 * 60 ) + ( bHour * 1000 * 60 * 60 );

		if ( aVal > bVal )
		{
			return 1;
		}

		if ( aVal == bVal )
		{
			return 0;
		}

		return -1;
	}

	public static DailySchedule createSchedule(Date startDateTime, int performThisTask)
	{
		return new DailySchedule(startDateTime, performThisTask);
	}
}
