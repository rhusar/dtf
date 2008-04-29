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
import org.jboss.dtf.testframework.utils.PerformanceLogger;

public class PerfTest extends Test
{
	public void run(String[] args)
	{
		try
		{
			int startX = Integer.parseInt(System.getProperty("startx","0"));
			int endX = Integer.parseInt(System.getProperty("endx","1000"));
			int incrementX = Integer.parseInt(System.getProperty("incx","50"));
			int startY = Integer.parseInt(System.getProperty("starty","0"));
			int endY = Integer.parseInt(System.getProperty("endy","1000"));
			PerformanceLogger logger = new PerformanceLogger("random-data");

			logInformation("StartX:"+startX+" | EndX:"+endX+" | IncrementX:"+incrementX+" | StartY:"+startY+" | EndY:"+endY);
			for (int count=startX;count<endX;count+=incrementX)
			{
				logger.addData( count, (double)(Math.random() * (endY - startY)) + startY );
			}

			assertSuccess();
			logger.output(System.out);
		}
		catch (Exception e)
		{
			e.printStackTrace(System.err);
			assertFailure();
		}
	}

	public static void main(String[] args)
	{
		PerfTest test = new PerfTest();
		test.initialise( null, null, args, new org.jboss.dtf.testframework.unittest.LocalHarness() );
		test.runTest();
	}
}
