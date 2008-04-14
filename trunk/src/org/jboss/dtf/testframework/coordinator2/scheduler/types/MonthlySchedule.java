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
 * $Id: MonthlySchedule.java 170 2008-03-25 18:59:26Z jhalliday $
 */
package org.jboss.dtf.testframework.coordinator2.scheduler.types;

import org.jboss.dtf.testframework.coordinator2.scheduler.exception.ScheduleException;

import java.util.Date;
import java.util.Calendar;

public class MonthlySchedule extends ScheduleInformation
{
	private Date	_startTime = null;
	private int		_dayOfMonth;
	private int[]	_months;

	public MonthlySchedule(Date startTime, int dayOfMonth, int[] months)
	{
		super(MONTHLY);

		_startTime = startTime;
		_dayOfMonth = dayOfMonth;
		_months = months;
	}

	public Date getStartTime()
	{
		return _startTime;
	}

	public int getDayOfMonth()
	{
		return _dayOfMonth;
	}

	public int[] getMonths()
	{
		return _months;
	}

	public boolean isOnThisMonth(int month)
	{
		for (int count=0;count<_months.length;count++)
		{
			if ( _months[count] == month )
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

	/**
	 * Returns true if the schedule has now finished.
	 *
	 * @return True if the schedule is finished.
	 */
	public boolean isScheduleFinished()
	{
		return false;
	}

	public static MonthlySchedule createSchedule(Date startTime, int dayOfMonth, int[] months) throws ScheduleException
	{
		if ( ( months == null ) || ( months.length == 0 ) )
		{
			throw new ScheduleException("MonthlySchedule creation failed - no months specified");
		}

		return new MonthlySchedule(startTime, dayOfMonth, months);
	}
}
