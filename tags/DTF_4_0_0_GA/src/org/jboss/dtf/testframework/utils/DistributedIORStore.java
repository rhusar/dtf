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
// $Id: DistributedIORStore.java 170 2008-03-25 18:59:26Z jhalliday $

package org.jboss.dtf.testframework.utils;

import org.jboss.dtf.testframework.nameservice.*;

import java.rmi.*;

public class DistributedIORStore implements IORStore
{
	private static NameServiceInterface _nameService;

	/**
	 * Initialises the IOR store.  This informs the IOR store of the NameService it is to use
	 * to store and retrieve names from.
	 * @param URI The URI of the NameService e.g. //bob/NameService
	 */
	public void initialiseStore(String URI)
	{
		try
		{
			_nameService = (NameServiceInterface)Naming.lookup(URI);

			if (_nameService==null)
			{
				System.err.println("Name Service '"+URI+"' Not Found!");
			}
		}
		catch (Exception e)
		{
			e.printStackTrace(System.err);
		}
	}

	/**
	 * Stores an object's IOR against a given unique name.  This method simply calls
	 * NameService.rebindReference.
	 * @param serverName The unique name for this IOR (e.g. \TestGroupA\TestA\ServerA),
	 * @param serverIOR The IOR for this object reference.
	 */
    public void storeIOR(String serverName, String serverIOR)
        throws Exception
    {
    	_nameService.rebindReference(serverName, serverIOR);
    }

	/**
	 * Removes the object's IOR from the name service whos name matches serverName.
	 * @param serverName The name in the NameService to be removed.
	 */
    public void removeIOR(String serverName)
        throws Exception
    {
    	_nameService.unbindReference(serverName);
    }

	/**
	 * Finds the IOR stored in the name service against the given name.
	 * @param serverName The name of the IOR to be found in the NameService.
	 * @returns The IOR of the object stored against the given name.
	 */
    public String loadIOR(String serverName) throws Exception
    {
    	return((String)_nameService.lookup(serverName));
    }
}
