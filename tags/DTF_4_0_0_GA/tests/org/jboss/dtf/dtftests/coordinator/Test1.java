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
 * $Id: Test1.java 170 2008-03-25 18:59:26Z jhalliday $
 */
package org.jboss.dtf.dtftests.coordinator;

import org.jboss.dtf.testframework.nameservice.NameServiceInterface;
import org.jboss.dtf.testframework.coordinator2.CoordinatorInterface;

import java.rmi.Naming;
import java.net.URL;

/**
 * @dtf:name CoordinatorClient
 * @dtf:group CoordinatorTests
 * @dtf:runner JavaTaskRunner
 * @dtf:description This is a test of the coordinator.
 * @dtf:classtype client
 * @dtf:parameters {"foo","bar"}
 * 				   {"sho","bed"}
 * @dtf:dependency classname=com.hp.mwtests.performance.ArjunaResourceTest
 * 				   parameters="boo","shoo"
 * @dtf:names-required 1
 */
public class Test1
{
	public static void main(String[] args)
	{
		try
		{
			System.out.println("Retrieving refernce to coordinator...");
			NameServiceInterface nsi = (NameServiceInterface)Naming.lookup("/NameService");
			CoordinatorInterface coordinator = (CoordinatorInterface)nsi.lookup("Coordinator");

			if ( coordinator != null )
			{
				System.out.println("Coordinator: found");

				for (int count=0;count<1;count++)
				{
					coordinator.run(new URL("http://bob101:8080/frameworktestsdefs.xml"),
									new URL("http://bob101:8080/frameworkselection.xml"),
									"","Test",false);
				}
			}
			else
			{
				System.out.println("Coordinator: not found!");
			}
		}
		catch (Exception e)
		{
			e.printStackTrace(System.err);
		}
	}
}
