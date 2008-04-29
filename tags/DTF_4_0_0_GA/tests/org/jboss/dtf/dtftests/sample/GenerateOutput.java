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

public class GenerateOutput extends Test
{
	private final static int DEFAULT_SIZE = 1024;

	public void run(String[] args)
	{
		int size = DEFAULT_SIZE;

		if ( args.length > 0 )
		{
			size = Integer.parseInt(args[0]);
		}

		logInformation("Writing "+size+" lines to standard out");
		byte[] buffer = new byte[1024];

		java.util.Arrays.fill(buffer, (byte)'A');

		for (int count=0;count<size;count++)
		{
			try
			{
				System.out.write(buffer);
			}
			catch (Exception e)
			{
				System.err.println("Failed to output buffer: "+e);
				assertFailure();
			}
		}

		System.out.println("Finished");

		assertSuccess();
	}

	public static void main(String[] args)
	{
		GenerateOutput go = new GenerateOutput();
		go.initialise( null, null, args, new org.jboss.dtf.testframework.unittest.LocalHarness() );
		go.runTest();
	}
}
