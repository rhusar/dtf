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
package org.jboss.dtf.tools.scheduletask;

import org.apache.tools.ant.Task;
import org.apache.tools.ant.BuildException;

import java.util.ArrayList;
import java.rmi.Naming;

import org.jboss.dtf.testframework.coordinator2.CoordinatorInterface;
import org.jboss.dtf.testframework.coordinator2.Coordinator;
import org.jboss.dtf.testframework.nameservice.NameServiceInterface;

/*
 * Copyright (C) 1998, 1999, 2000, 2001, 2002, 2003
 *
 * Arjuna Technology Ltd.
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: ScheduleTask.java 170 2008-03-25 18:59:26Z jhalliday $
 */

public class ScheduleTask extends Task
{
	private ArrayList	_schedules = new ArrayList();
    private String		_nameServiceURI = null;

	public void setNameserviceuri(String nameServiceUri)
	{
		_nameServiceURI = nameServiceUri;
	}

	public RunWhenPossibleElement createRunwhenpossible()
	{
		RunWhenPossibleElement rwe;
		_schedules.add(rwe = new RunWhenPossibleElement());

		return rwe;
	}

	public void execute() throws BuildException
	{
		if ( _nameServiceURI == null )
		{
			throw new BuildException("Please specify the RMI NameService URI (nameserviceuri)");
		}

		if ( _schedules.size() == 0 )
		{
			throw new BuildException("No schedule information has been specified");
		}

		try
		{
			NameServiceInterface nameService = (NameServiceInterface)Naming.lookup(_nameServiceURI);
			CoordinatorInterface coordinator = (CoordinatorInterface)nameService.lookup(Coordinator.COORDINATOR_NAME_SERVICE_NAME);

			for (int count=0;count<_schedules.size();count++)
			{
				RunWhenPossibleElement schedule = (RunWhenPossibleElement)_schedules.get(count);

				schedule.invoke(coordinator);
			}
		}
		catch (Exception e)
		{
			throw new BuildException("An unexpected exception has occurred: "+e);
		}
	}
}
