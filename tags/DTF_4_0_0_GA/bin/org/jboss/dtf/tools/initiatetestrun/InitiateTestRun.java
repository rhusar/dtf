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
/*
 * Copyright (C) 1998, 1999, 2000, 2001, 2002
 *
 * Arjuna Technologies Ltd.
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: InitiateTestRun.java 170 2008-03-25 18:59:26Z jhalliday $
 */
package org.jboss.dtf.tools.initiatetestrun;

import org.jboss.dtf.testframework.nameservice.NameServiceInterface;
import org.jboss.dtf.testframework.coordinator2.Coordinator;
import org.jboss.dtf.testframework.coordinator2.CoordinatorInterface;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.RemoteException;
import java.rmi.Naming;
import java.net.URL;

import org.apache.tools.ant.Task;
import org.apache.tools.ant.BuildException;

public class InitiateTestRun extends Task
{
	private String 	_testDefsURL = null;
	private String 	_testSelectionsURL = null;
	private String 	_nameServiceURI = null;
	private String 	_softwareVersion = null;
	private String 	_distributionList = "";
	private boolean _waitToComplete = false;
	private int		_rmiPort = Registry.REGISTRY_PORT;

	public void execute() throws BuildException
	{
		if ( _nameServiceURI == null )
		{
			throw new BuildException("You have not specified the nameservice to use");
		}

		if ( _testDefsURL == null )
		{
			throw new BuildException("You have not specified the test defintions file to use");
		}

		if ( _testSelectionsURL == null )
		{
			throw new BuildException("You have not specified the test selections file to use");
		}

		if ( _softwareVersion == null )
		{
			throw new BuildException("You have not specified the software version to log against");
		}

		try
		{
			try
			{
				LocateRegistry.createRegistry(_rmiPort);
			}
			catch (RemoteException ex)
			{
			}

			NameServiceInterface nameService = (NameServiceInterface)Naming.lookup(_nameServiceURI);

			CoordinatorInterface coordinator = (CoordinatorInterface)nameService.lookup(Coordinator.COORDINATOR_NAME_SERVICE_NAME);

			coordinator.run(new URL(_testDefsURL), new URL(_testSelectionsURL), _distributionList, _softwareVersion, _waitToComplete);
		}
		catch (Exception e)
		{
			throw new BuildException("Failed to initiate test run: "+e);
		}
	}

	public void setTestDefsURL(String testDefsURL)
	{
		_testDefsURL = testDefsURL;
	}

	public void setTestSelectionsURL(String testSelectionsURL)
	{
		_testSelectionsURL = testSelectionsURL;
	}

	public void setRmiPort(String port)
	{
		_rmiPort = Integer.parseInt(port);
	}

	public void setNameServiceURI(String nameServiceURI)
	{
		_nameServiceURI = nameServiceURI;
	}

	public void setSoftwareVersion(String softwareVersion)
	{
		_softwareVersion = softwareVersion;
	}

	public void setDistributionList(String distributionList)
	{
		_distributionList = distributionList;
	}

	public void setWaitToComplete(String wait)
	{
		_waitToComplete = new Boolean(wait).booleanValue();
	}

	public static void main(String[] args) throws Exception
	{
		InitiateTestRun itr = new InitiateTestRun();

		for (int count=0;count<args.length;count++)
		{
			if ( args[count].equals("-help") )
			{
				System.out.println("Usage: InitiateTestRun [-nameservice <nameserviceuri>] [-testdefs <testdefs url>] [-selection <selection url>] [-version <version>] [-distribution <email list>] [-waittocomplete]");
			}
			else
			if ( args[count].equals("-nameservice") )
			{
				itr.setNameServiceURI(args[count + 1]);
			}
			else
			if ( args[count].equals("-testdefs") )
			{
				itr.setTestDefsURL(args[count + 1]);
			}
			else
			if ( args[count].equals("-selection") )
			{
				itr.setTestSelectionsURL(args[count + 1]);
			}
			else
			if ( args[count].equals("-version") )
			{
				itr.setSoftwareVersion(args[count + 1]);
			}
			else
			if ( args[count].equals("-distribution") )
			{
				itr.setDistributionList(args[count + 1]);
			}
			else
			if ( args[count].equals("-waittocomplete") )
			{
				itr.setWaitToComplete("true");
			}
		}

		itr.execute();
	}
}
