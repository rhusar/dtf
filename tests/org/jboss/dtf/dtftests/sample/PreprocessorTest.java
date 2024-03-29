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


import org.jboss.dtf.testframework.unittest.*;

public class PreprocessorTest extends Test
{
	public void run(String[] args)
	{
		if (args.length != 2)
		{
			logInformation("Expecting 2 parameters got '"+args.length+"'");
			assertFailure();
		}
		else
		{
			logInformation("Parameter 1: '"+args[0]+"'");
			logInformation("Parameter 2: '"+args[1]+"'");

			if ( args[0].equals(args[1]) )
			{
				logInformation("They match");
				assertSuccess();
			}
			else
			{
				logInformation("Do not match");
				assertFailure();
			}
		}
	}

	public static void main(String[] args)
	{
		PreprocessorTest test = new PreprocessorTest();
		test.initialise(null, null, args, new LocalHarness());
		test.runTest();
	}
}
