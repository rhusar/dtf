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
 * $Id: DefaultLogHandler.java 170 2008-03-25 18:59:26Z jhalliday $
 */
package org.jboss.dtf.testframework.coordinator2.logger;

import java.util.Hashtable;

public class DefaultLogHandler implements LogHandler
{
	private final static String DEFAULT_LOGGER_PROPERTY = "org.jboss.dtf.testframework.coordinator2.logger";
	private final static String DEFAULT_LOGGER_IMPLEMENTATION = "org.jboss.dtf.testframework.coordinator2.logger.DefaultLogger";

	private Hashtable	_loggers = new Hashtable();

	public Logger getLogger(String name)
	{
		Logger logger = (Logger)_loggers.get(name);

		try
		{
			if ( logger == null )
			{
				String loggerImpl = System.getProperty( DEFAULT_LOGGER_PROPERTY, DEFAULT_LOGGER_IMPLEMENTATION );

				logger = (Logger)Class.forName( loggerImpl ).newInstance();
                logger.initialise(name);
				_loggers.put(name, logger);
			}
		}
		catch (Exception e)
		{
			System.err.println("Failed to create logger '"+name+"': "+e);
		}

		return logger;
	}
}
