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
package org.jboss.dtf.testframework.utils;

import java.rmi.server.UnicastRemoteObject;
import java.rmi.RemoteException;
import java.io.*;

/*
 * Copyright (C) 1998, 1999, 2000, 2001, 2002, 2003
 *
 * Arjuna Technologies Ltd.
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: RemoteFileReader.java 170 2008-03-25 18:59:26Z jhalliday $
 */

public class RemoteFileReader extends UnicastRemoteObject implements RemoteFileReaderInterface
{
	private BufferedReader	_in;

	public RemoteFileReader(File fileToBeRead) throws java.io.IOException, java.rmi.RemoteException
	{
		 super();

		_in = new BufferedReader(new InputStreamReader(new FileInputStream(fileToBeRead)));
	}

	public String readLine() throws RemoteException
	{
		try
		{
			return _in.readLine();
		}
		catch (java.io.IOException e)
		{
		}

		return null;
	}

	public void close() throws IOException, RemoteException
	{
		_in.close();
	}
}
