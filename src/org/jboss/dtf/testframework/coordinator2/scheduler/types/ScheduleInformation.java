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
 * $Id: ScheduleInformation.java 170 2008-03-25 18:59:26Z jhalliday $
 */
package org.jboss.dtf.testframework.coordinator2.scheduler.types;

import org.jboss.dtf.testframework.coordinator2.scheduler.exception.ScheduleException;

import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.io.*;
import java.net.URL;

public abstract class ScheduleInformation implements Serializable
{
    public static final int DAILY = 1, WEEKLY = 2, MONTHLY = 3, ONE_TIME_ONLY = 4;

	private static long ScheduleId = 0;

	private int				_scheduleType;
    private transient File  _file = null;

	private String 			_uniqueId = null;
    private URL				_testDefinitionsURL = null;
	private URL				_testSelectionsURL = null;
	private String			_softwareVersion = null;
	private String			_distributionList = null;
    private Hashtable   	_failures = null;

	public ScheduleInformation(int type)
	{
		_failures = new Hashtable();

		_uniqueId = ""+System.currentTimeMillis()+"_"+(ScheduleId++);
		_scheduleType = type;
	}

	public String getUniqueId()
	{
		return _uniqueId;
	}

	public void setRunInformation(URL testDefsURL, URL testSelectionsURL, String softwareVersion, String distributionList)
	{
		_testDefinitionsURL = testDefsURL;
		_testSelectionsURL = testSelectionsURL;
		_softwareVersion = softwareVersion;
		_distributionList = distributionList;
	}

	public void addFailure(Date timeOfFailure, String reason) throws ScheduleException
	{
    	_failures.put(timeOfFailure, reason);

		serialize();
	}

	public Hashtable getFailures()
	{
		return _failures;
	}

	public URL getTestDefinitionsURL()
	{
		return _testDefinitionsURL;
	}

	public URL getTestSelectionsURL()
	{
		return _testSelectionsURL;
	}

	public String getSoftwareVersion()
	{
		return _softwareVersion;
	}

	public String getDistributionList()
	{
		return _distributionList;
	}

	/**
	 * Retrieves the a Date object containing the time and date this
	 * schedule next becomes active.
	 *
	 * @return The next activation time.
	 */
	public abstract Calendar getNextActivationTime();

	/**
	 * Returns true if the schedule has now finished.
	 *
	 * @return True if the schedule is finished.
	 */
	public abstract boolean isScheduleFinished();

	public File getAssociatedFile()
	{
		return _file;
	}

	public boolean deleteIfNecessary()
	{
		if ( isScheduleFinished() )
		{
			return delete();
		}

		return false;
	}

	public boolean delete()
	{
		boolean returnValue = false;

		if ( _file != null )
		{
			returnValue = _file.delete();
		}

		return returnValue;
	}

	public void associateFile(File file)
	{
		_file = file;
	}

	public void serialize() throws ScheduleException
	{
		if ( _file != null)
			serialize(_file);

		throw new ScheduleException("No file associated with the schedule");
	}

	public void serialize(File outFile) throws ScheduleException
	{
		try
		{
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(outFile));

			oos.writeObject(this);

			oos.close();
		}
		catch (Exception e)
		{
			throw new ScheduleException("Failed to serialize to '"+outFile+"'");
		}

		associateFile(outFile);
	}

	public static ScheduleInformation deserialize(File inFile) throws ScheduleException
	{
		Object inObj = null;

		try
		{
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(inFile));

			inObj = ois.readObject();

			ois.close();
		}
		catch (Exception e)
		{
			throw new ScheduleException("Failed to deserialize '"+inFile+"'");
		}


		if ( !(inObj instanceof ScheduleInformation ) )
		{
			throw new ScheduleException("File '"+inFile+"' does not contain a schedule description");
		}

		ScheduleInformation schedule = (ScheduleInformation)inObj;
		schedule.associateFile(inFile);

		return schedule;
	}
}
