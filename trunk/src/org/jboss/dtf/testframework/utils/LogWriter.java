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
//
// Copyright (C) 2001,
//
// HP Arjuna Labs
// Newcastle upon Tyne,
// Tyne and Wear,
// UK.
//
// $Id: LogWriter.java 170 2008-03-25 18:59:26Z jhalliday $

package org.jboss.dtf.testframework.utils;

import java.io.*;
import java.util.GregorianCalendar;

public class LogWriter implements FilenameFilter
{
	private final static long MAX_LOG_LENGTH = 1024 * 16384; // 16mb

	private final static String MAX_DAYS_OLD_PROPERTY = "logs.maxage";
	private final static long   MS_IN_A_DAY = 1000 * 60 * 60 * 24;
	private final static String MAX_DAYS_OLD_DEFAULT_VALUE = "3";

	private static FileCaretaker		_logCaretaker = null;

	private String						_filename = null;
	private PrintWriter 				_logFile = null;
	private File						_file = null;

	public LogWriter()
	{
		if ( _logCaretaker == null )
		{
			int maxAge = Integer.parseInt(System.getProperty(MAX_DAYS_OLD_PROPERTY, MAX_DAYS_OLD_DEFAULT_VALUE));

			_logCaretaker = new FileCaretaker(maxAge * MS_IN_A_DAY, new File("."), this);
		}
	}

	/**
	 * Tests if a specified file should be included in a file list.
	 *
	 * @param   dir    the directory in which the file was found.
	 * @param   name   the name of the file.
	 * @return  <code>true</code> if and only if the name should be
	 * included in the file list; <code>false</code> otherwise.
	 */
	public boolean accept(File dir, String name)
	{
		return name.indexOf("_backup-") != -1;
	}


	public final void openLog(String filename)
	{
		_file = new File(_filename = filename);

		if ( _file.exists() )
		{
			_logCaretaker.sweep();

			_file.renameTo(new File(_file.getName() + "_backup-"+getDate()));
			_file = new File(_filename);
		}

		try
		{
			_logFile = new PrintWriter(new FileWriter(_file));
		}
		catch (java.io.IOException e)
		{
			System.out.println("ERROR - Failed to close log file");
			System.exit(0);
		}
	}

	public synchronized final void closeLog()
	{
    	_logFile.close();
	}

	public synchronized final void writeLog(String logText)
	{
		_logFile.write(new java.util.Date().toString()+" - "+logText+"\n");
		_logFile.flush();

		if ( _file.length() > MAX_LOG_LENGTH )
		{
			_logFile.close();

			openLog(_filename);
		}
	}

	private final String getDate()
	{
		GregorianCalendar gc = new GregorianCalendar();

		return  twoChars(gc.get(GregorianCalendar.MONTH)+1) + twoChars(gc.get(GregorianCalendar.DAY_OF_MONTH)) +"_"+ twoChars(gc.get(GregorianCalendar.HOUR_OF_DAY)) +""+ twoChars(gc.get(GregorianCalendar.MINUTE))+""+twoChars(gc.get(GregorianCalendar.SECOND));
	}

	private final String twoChars(int value)
	{
		String rv = ""+value;

		if ( rv.length() < 2)
		{
			rv = '0' + rv;
		}

		return rv;
	}

	public synchronized final void writeLog(Exception e)
	{
    	e.printStackTrace(_logFile);
   		e.printStackTrace(System.err);
		_logFile.flush();
    }
}
