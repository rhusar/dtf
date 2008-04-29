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
package org.jboss.dtf.dtftests.sample ;

import org.jboss.dtf.testframework.unittest.Test;

public class ParameterTest extends Test
{
	public void run(String[] args)
	{
		long minExpected = Long.parseLong(System.getProperty("min","-1"));
		long maxExpected = Long.parseLong(System.getProperty("max","-1"));
		long minFound = Long.MAX_VALUE;
		long maxFound = Long.MIN_VALUE;

		if ( minExpected == -1 )
		{
			logInformation("Warning: 'min' property may not have been set");
		}

		if ( maxExpected == -1 )
		{
			logInformation("Warning: 'max' property may not have been set");
		}

		for (int count=0;count<args.length;count++)
		{
			long value = Long.parseLong(args[count]);

			if ( value > maxFound )
				maxFound = value;

			if ( value < minFound )
			{
				minFound = value;
			}
		}

		assertSuccess();

		if ( minExpected != minFound )
		{
			logInformation("Min expected not equals min found (expected:"+minExpected+", found:"+minFound+")");
			assertFailure();
		}

		if ( maxExpected != maxFound )
		{
			logInformation("Max expected not equals max found (expected:"+maxExpected+", found:"+maxFound+")");
			assertFailure();
		}
	}
}
