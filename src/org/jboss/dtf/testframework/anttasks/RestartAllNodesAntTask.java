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
 * Arjuna Technology Ltd.
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: RestartAllNodesAntTask.java 170 2008-03-25 18:59:26Z jhalliday $
 */
package org.jboss.dtf.testframework.anttasks;

import org.apache.tools.ant.Task;
import org.apache.tools.ant.BuildException;

import java.rmi.Naming;
import java.rmi.RemoteException;

import org.jboss.dtf.testframework.nameservice.NameServiceInterface;
import org.jboss.dtf.testframework.serviceregister.ServiceRegister;
import org.jboss.dtf.testframework.serviceregister.ServiceRegisterInterface;
import org.jboss.dtf.testframework.testnode.TestNodeInterface;

public class RestartAllNodesAntTask extends Task
{
	private String	_nameService = null;
    private boolean _restart = false;

	public void setNameservice(String nameService)
	{
		_nameService = nameService;
	}

	public void setRestart(String bool)
	{
    	_restart = new Boolean(bool).booleanValue();
	}

	/**
	 * Called by the project to let the task do its work. This method may be
	 * called more than once, if the task is invoked more than once.
	 * For example,
	 * if target1 and target2 both depend on target3, then running
	 * "ant target1 target2" will run all tasks in target3 twice.
	 *
	 * @exception BuildException if something goes wrong with the build
	 */
	public void execute() throws BuildException
	{
		super.execute();

		if ( _nameService == null )
		{
			throw new BuildException("Please specify the URI of the name service");
		}

		try
		{
			NameServiceInterface nsi = (NameServiceInterface)Naming.lookup(_nameService);

			ServiceRegisterInterface sri = (ServiceRegisterInterface)nsi.lookup(ServiceRegister.SERVICE_REGISTER_NAME_SERVICE_ENTRY);

			TestNodeInterface[] nodes = sri.getRegister();

			this.log("Shutting down"+(_restart ? " and restarting" : "" )+" all "+nodes.length+" node(s)");
			for (int count=0;count<nodes.length;count++)
			{
				try
				{
                	nodes[count].shutdown( _restart, false );
				}
				catch (RemoteException e)
				{
					this.log("Warning - test node not responding");
				}
			}
		}
		catch (Exception e)
		{
			throw new BuildException("An unexpected exception occurred: "+e);
		}
	}
}
