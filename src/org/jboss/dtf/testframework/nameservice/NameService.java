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
// $Id: NameService.java 170 2008-03-25 18:59:26Z jhalliday $

package org.jboss.dtf.testframework.nameservice;

import org.jboss.dtf.testframework.nameservice.*;

import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.io.*;

public class NameService extends java.rmi.server.UnicastRemoteObject implements NameServiceInterface
{
	private NameServiceTreeNode _rootNode = new NameServiceTreeNode("/");

	public NameService() throws java.rmi.RemoteException
	{
		super();
	}

	private String[] stripTreeNodes( String name )
	{
		String[] nodes;
		String 	 nameCopy = name;
		int 	 nodeCount = 0;

		/*
		 * Strip the intial /
		 */
		if (nameCopy.indexOf("/")==0)
		{
			nameCopy = nameCopy.substring(1);
			name = name.substring(1);
		}

		/*
		 * Count the number of nodes in the name
		 * (e.g. '/a/b/c' = 3)
		 */
		while (nameCopy.indexOf("/")!=-1)
		{
			nodeCount++;
			nameCopy = nameCopy.substring(nameCopy.indexOf("/")+1);
		}

        if (nameCopy.length()>0)
            nodeCount++;

		nodes = new String[nodeCount];

		nodeCount = 0;
		nameCopy = name;

		while (nameCopy.indexOf("/")!=-1)
		{
			nodes[nodeCount] = nameCopy.substring(0,nameCopy.indexOf("/"));
			nameCopy = nameCopy.substring(nameCopy.indexOf("/")+1);
			nodeCount++;
		}

		if (nameCopy.length()>0)
		{
		    nodes[nodeCount] = nameCopy;
		}

		return(nodes);
	}

	public void rebindReference( String 	name,
							     Object		obj ) throws java.rmi.RemoteException
	{
		String[] nodes = stripTreeNodes( name );
		try
		{
			NameServiceTreeNode newNode = _rootNode.ensureNodesExist(nodes,false);

			newNode.setData(obj);
		}
		catch (NameAlreadyBound e)
		{
		}
		catch (NameNotBound e)
		{
			NameServiceTreeNode newNode = null;

			try
			{
				newNode = _rootNode.ensureNodesExist(nodes,true);
			}
			catch (NameNotBound nnb)
			{
			}
			catch (NameAlreadyBound nae)
			{
			}
			newNode.setData(obj);
		}
	}


	public void bindReference( String name, Object obj )
							throws java.rmi.RemoteException, NameAlreadyBound
	{
		String[] nodes = stripTreeNodes( name );

		try
		{
			NameServiceTreeNode newNode = _rootNode.ensureNodesExist(nodes,false);
			throw new NameAlreadyBound(name);
		}
		catch (NameNotBound e)
		{
			NameServiceTreeNode newNode = null;

			try
			{
				newNode = _rootNode.ensureNodesExist(nodes,true);
				newNode.setData(obj);
			}
			catch (NameNotBound nnb)
			{
			}
			catch (NameAlreadyBound nae)
			{
			}
		}
	}

	public void unbindReference( String name )
							throws java.rmi.RemoteException, NameNotBound
	{
		String[] nodes = stripTreeNodes( name );
		try
		{
			_rootNode.removeEndNode(nodes);
		}
		catch (NameNotBound e)
		{
			throw e;
		}
	}

	public Object lookup( String name )
							throws java.rmi.RemoteException, NameNotBound
	{
		String[] nodes = stripTreeNodes( name );
		Object obj = null;

		try
		{
			NameServiceTreeNode oldNode = _rootNode.ensureNodesExist(nodes,false);
			obj = oldNode.getData();
		}
		catch (NameAlreadyBound e)
		{
			// Ignore as this should never be thrown
		}
		catch (NameNotBound e)
		{
			throw e;
		}

		return(obj);
	}

	public String[] lookupNames ( String directory )
							throws java.rmi.RemoteException, NameNotBound
	{
		String[] nodes = stripTreeNodes( directory );
		Object obj = null;
		String[] returnData = null;

		try
		{
			NameServiceTreeNode     oldNode = _rootNode.ensureNodesExist(nodes,false);
			NameServiceTreeNode[]   objs = oldNode.getChildren();

			returnData = new String[objs.length];

			for (int count=0;count<objs.length;count++)
			{
			    returnData[count] = objs[count].getName();
			}
		}
		catch (NameAlreadyBound e)
		{
			// Ignore as this should never be thrown
		}
		catch (NameNotBound e)
		{
			throw e;
		}

		return(returnData);
	}

    public static void initialiseRegistry()
    {
        try
        {
			int rmiPort = getRMIPort();
            System.out.println("Creating RMI registry on port "+rmiPort);
            LocateRegistry.createRegistry(rmiPort);
        }
        catch (RemoteException ex)
        {
        }
    }

	private final static int getRMIPort()
	{
		String rmiPort = System.getProperty("rmi.port", ""+Registry.REGISTRY_PORT);

		return Integer.parseInt(rmiPort);
	}

	public static void main(String args[])
	{
		try
		{
			String nameServiceRMIName = "NameService";

			for (int count=0;count<args.length;count++)
			{
				if (args[count].equalsIgnoreCase("-help"))
				{
					System.out.println("Usage: NameService [-help] -[name <name>]");
					System.exit(0);
				}

				if (args[count].equalsIgnoreCase("-name"))
				{
					nameServiceRMIName = args[count+1];
					System.out.println("RMI name set to '"+nameServiceRMIName+"'");
				}
			}

            initialiseRegistry();

			NameService nameService = new NameService();
		    Naming.rebind("//"+java.net.InetAddress.getLocalHost().getHostName()+":"+getRMIPort()+"/"+nameServiceRMIName, nameService);
		    System.out.println("Ready");
		}
		catch (Exception e)
		{
			e.printStackTrace();
			System.exit(0);
		}
	}
}

