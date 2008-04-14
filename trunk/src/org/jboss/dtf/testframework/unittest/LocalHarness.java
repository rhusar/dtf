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
// $Id: LocalHarness.java 170 2008-03-25 18:59:26Z jhalliday $

package org.jboss.dtf.testframework.unittest;

import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class LocalHarness implements HarnessInterface
{

	public void logInformation(String information)
	{
		System.out.println(information);
	}

	public void logTestRunInformation(String information)
	{
		System.out.println(information);
	}

	public void logResult(String result)
	{
		System.out.println(result);
	}

	public boolean registerService(String name, String ior)
	{
		boolean returnValue = true;

		try
		{
            FileOutputStream fout = new FileOutputStream(name);
            fout.write(ior.getBytes());
            fout.close();
		}
		catch (Exception e)
		{
			System.err.println(e);
			returnValue = false;
		}

		return(returnValue);
	}

	public String getService(String name) throws ServiceLookupException
	{
		String returnValue = null;

		try
		{
            BufferedReader fin = new BufferedReader(new InputStreamReader(new FileInputStream(name)));
            returnValue = fin.readLine();
            fin.close();
		}
		catch (Exception e)
		{
			throw new ServiceLookupException("Failed to lookup '"+name+"'");
		}

		return(returnValue);
	}
}
