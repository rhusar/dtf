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
 */
package org.jboss.dtf.dtftests.dbmanager ;

import junit.framework.TestCase ;
import junit.framework.Assert ;

import java.util.Hashtable ;

import javax.naming.InitialContext ;
import javax.sql.DataSource ;

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
public class DBManagerTestCase extends TestCase
{
	/**
	 * Test lookup of the JDBC DataSource from the FSContext created
	 * by DBManager
	 *
	 * @exception NotBoundException
	 * @exception RemoteException
	 * @exception AccessException
	 * @exception MalformedURLException
	 */
	public void testJNDILookup() throws Exception {

		try
		{
			Hashtable params = new Hashtable() ;
			params.put("java.naming.factory.initial",
					"com.sun.jndi.fscontext.RefFSContextFactory") ;
			params.put("java.naming.provider.url","file:./jndiFSContext") ;

			InitialContext ctx = new InitialContext(params) ;

			DataSource ds = (DataSource) ctx.lookup("jdbc/ResultsDB") ;
			// Object obj = ctx.lookup("jdbc/ResultsDB") ;
			// String className = obj.getClass().getName() ;
			// System.out.println("className = " + className) ;
			// String refClassName = ((Reference)obj).getClassName() ;
			// System.out.println("refClassName = " + refClassName) ;

			System.out.println("DataSource retrieved successfully") ;
		}
		catch (Exception e)
		{
			e.printStackTrace(System.err);
		}
	}

}
