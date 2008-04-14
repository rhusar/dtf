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
 * $Id: FileLogger.java 170 2008-03-25 18:59:26Z jhalliday $
 */
package org.jboss.dtf.testframework.coordinator2.logger;

import java.io.PrintStream;
import java.io.FileOutputStream;

public class FileLogger implements Logger
{
	private final static String OUT_LOG_SUFFIX = ".out";
	private final static String ERR_LOG_SUFFIX = ".err";

	private String 		_name = null;
	private PrintStream _log = null;
	private PrintStream _err = null;

	public void initialise(String name) throws LoggerInitialisationException
	{
		_name = name;

		try
		{
			_log = new PrintStream(new FileOutputStream(name + OUT_LOG_SUFFIX));
			_err = new PrintStream(new FileOutputStream(name + ERR_LOG_SUFFIX));
		}
		catch (java.io.IOException e)
		{
            throw new LoggerInitialisationException("An error occurred while creating file streams:"+e);
		}
	}

	public void error(String text)
	{
		System.err.println("["+_name+"|e]:"+text);
		_err.println("["+_name+"|e]:"+text);
	}

	public void log(String text)
	{
		System.out.println("["+_name+"|o]:"+text);
		_log.println("["+_name+"|o]:"+text);
	}

	public void close()
	{
		_log.close();
		_err.close();
	}
}
