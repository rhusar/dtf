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
 * $Id: OneTimeOnlySchedule.java 170 2008-03-25 18:59:26Z jhalliday $
 */
package org.jboss.dtf.testframework.coordinator2.scheduler.types;

import org.jboss.dtf.testframework.coordinator2.scheduler.exception.ScheduleException;

import java.util.Date;
import java.util.Calendar;

public class OneTimeOnlySchedule extends ScheduleInformation
{
	private Date	_startDateTime = null;

	public OneTimeOnlySchedule(Date startDateTime)
	{
		super(ONE_TIME_ONLY);

		_startDateTime = startDateTime;
	}

	public Date getStartDateTime()
	{
		return _startDateTime;
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

	public static OneTimeOnlySchedule createSchedule(Date startDateTime)
	{
		return new OneTimeOnlySchedule(startDateTime);
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
