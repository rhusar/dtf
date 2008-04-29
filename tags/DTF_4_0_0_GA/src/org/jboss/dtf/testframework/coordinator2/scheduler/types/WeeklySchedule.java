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
 * $Id: WeeklySchedule.java 170 2008-03-25 18:59:26Z jhalliday $
 */
package org.jboss.dtf.testframework.coordinator2.scheduler.types;

import org.jboss.dtf.testframework.coordinator2.scheduler.exception.ScheduleException;

import java.util.Date;
import java.util.Calendar;

public class WeeklySchedule extends ScheduleInformation
{
	private Date	_startDateTime = null;
	private int		_weeks;
	private int[]	_daysOfWeek;

	public WeeklySchedule(Date startDateTime, int weeks, int[] daysOfWeek)
	{
		super(WEEKLY);

		_startDateTime = startDateTime;
		_weeks = weeks;
		_daysOfWeek = daysOfWeek;
	}

	public Date getStartDateTime()
	{
		return _startDateTime;
	}

	public int getNumberOfWeeks()
	{
		return _weeks;
	}

	public int[] getDaysOfWeek()
	{
		return _daysOfWeek;
	}

	public boolean isOnThisDay(int day)
	{
		for (int count=0;count<_daysOfWeek.length;count++)
		{
			if ( _daysOfWeek[count] == day )
			{
				return true;
			}
		}

		return false;
	}

	/**
	 * Retrieves the a Date object containing the time and date this
	 * schedule next becomes active.
	 *
	 * @return The next activation time.
	 */
	public Calendar getNextActivationTime()
	{
		return null;
	}

	public static WeeklySchedule createSchedule(Date startDateTime, int weeks, int[] daysOfWeek) throws ScheduleException
	{
    	if ( ( daysOfWeek == null ) || ( daysOfWeek.length == 0 ) )
		{
			throw new ScheduleException("WeeklySchedule creation failed - no days of the week specified");
		}

		return new WeeklySchedule(startDateTime, weeks, daysOfWeek);
	}

	/**
	 * Returns true if the schedule has now finished.
	 *
	 * @return True if the schedule is finished.
	 */
	public boolean isScheduleFinished()
	{
		return false;
	}
}
