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

public class TaskB extends Test
{
    public final static String WELLKNOWN_NAME = "TaskA.hostname";

    public void run(String[] args)
    {
	try
	    {
		String hostname = getService( WELLKNOWN_NAME );

		logInformation("Registered hostname '" + hostname + "' this nodes hostname '" +
			       java.net.InetAddress.getLocalHost().getHostName()+"'");

		// the test is successful if the two tests run on the same host
		if ( hostname.equals(java.net.InetAddress.getLocalHost().getHostName()) )
		    {
			assertSuccess();
		    }
		else
		    {
			assertFailure();
		    }
	    }
	catch (Exception e)
	    {
		assertFailure();
	    }
    }
}
