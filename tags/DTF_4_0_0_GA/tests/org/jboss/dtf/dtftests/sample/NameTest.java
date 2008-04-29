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
import java.util.HashSet;

public class NameTest extends Test
{
	public void run(String[] args)
	{
		int numberOfParams = Integer.parseInt(args[0]);

		logInformation("Received "+(args.length-1)+" parameters, expected "+numberOfParams);

		HashSet entries = new HashSet();

		assertSuccess();

		try
		{
			for (int count=1;count<(numberOfParams + 1);count++)
			{
				if ( entries.contains( args[count] ) )
				{
					logInformation("Duplicate entry found ("+count+")");
					assertFailure();
				}

				entries.add( args[count] );
			}
		}
		catch (Exception e)
		{
			logInformation("Exception: "+e);
			assertFailure();
		}
	}
}
