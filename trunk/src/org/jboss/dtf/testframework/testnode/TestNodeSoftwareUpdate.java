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
// $Id: TestNodeSoftwareUpdate.java 170 2008-03-25 18:59:26Z jhalliday $

package org.jboss.dtf.testframework.testnode;

import org.jboss.dtf.testframework.nameservice.*;
import org.jboss.dtf.testframework.serviceregister.*;

import java.rmi.*;

public class TestNodeSoftwareUpdate
{
	public TestNodeSoftwareUpdate(ServiceRegisterInterface register, String url, String jarFile, String cvsTag) throws java.rmi.RemoteException
	{
		/*
		 * For each TestNode in the register create a software update handler
		 */
		TestNodeInterface[] nodes = register.getRegister();
		UpdateSoftwareHandler[] handlers = new UpdateSoftwareHandler[nodes.length];
		int finishedCount = 0;

		System.out.println("Updating "+nodes.length+" node(s)");

		for (int count=0;count<nodes.length;count++)
		{
			handlers[count] = new UpdateSoftwareHandler(nodes[count]);
		}

		while (finishedCount<nodes.length)
		{
			finishedCount = 0;
			for (int count=0;count<nodes.length;count++)
			{
				if (handlers[count].isAlive())
				{
					try
					{
						handlers[count].join();
					}
					catch (InterruptedException e)
					{
					}
				}
				else
				{
					finishedCount++;
				}
			}
		}
	}

	private class UpdateSoftwareHandler extends Thread
	{
		private TestNodeInterface _testNode = null;
		private boolean _failure = false;

		public UpdateSoftwareHandler(TestNodeInterface testNode)
		{
			_testNode = testNode;
			start();
		}

		public void run()
		{
			try
			{
				if (!_testNode.updateSoftware())
				{
					System.out.println("TestNode '"+_testNode.getName()+"' failed to update correctly, shutting it down");
					_testNode.shutdown(false, false);
				}
				else
				{
					System.out.println("TestNode '"+_testNode.getName()+"' updated successfully, restarting it.");
					_testNode.shutdown(true, false);
				}
			}
			catch (Exception e)
			{
				_failure = true;
			}
		}
	}

	public static void main(String args[])
	{

		if (args.length != 1)
		{
			System.out.println("Usage:  TestNodeSoftwareUpdate [ URI of NameService ]");
			System.exit(0);
		}

		try
		{
			/*
			 * Obtain reference to the NameService and the ServiceRegister
			 */
			System.out.println("Looking up NameService...");
	        NameServiceInterface nameService = (NameServiceInterface)Naming.lookup(args[0]);

			System.out.println("Looking up ServiceRegister...");
			ServiceRegisterInterface register = (ServiceRegisterInterface)nameService.lookup("/ServiceRegister");

			new TestNodeSoftwareUpdate(register, args[1], null, args[2]);
		}
		catch (RemoteException e)
		{
			System.out.println("ERROR - Cannot find RMI services");
		}
		catch (Exception e)
		{
			System.out.println("ERROR: "+e.toString());
		}
	}
}
