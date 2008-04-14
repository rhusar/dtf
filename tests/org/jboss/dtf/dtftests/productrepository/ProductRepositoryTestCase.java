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
package org.jboss.dtf.dtftests.productrepository ;

import org.jboss.dtf.testframework.nameservice.NameServiceInterface;
import org.jboss.dtf.testframework.productrepository.ProductRepositoryInterface;

import junit.framework.TestCase ;
import junit.framework.Assert ;

import java.rmi.Naming;

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
public class ProductRepositoryTestCase extends TestCase
{
	/**
	 * Test basic connectivity to ProductRepository via Java RMI
	 *
	 * @exception NotBoundException
	 * @exception RemoteException
	 * @exception AccessException
	 * @exception MalformedURLException
	 */
	public void testBasicConnection() throws Exception {

		try
		{
			NameServiceInterface nsi = (NameServiceInterface)Naming.lookup("/NameService");
			if ( nsi == null )
			{
				fail("NameService not found") ;
			}

			System.out.println("Retrieving reference to ProductRepository...");
			ProductRepositoryInterface pri = (ProductRepositoryInterface) nsi.lookup("ProductRepository");
			if ( pri == null )
			{
				fail("ProductRepository not found") ;
			}

			System.out.println("ProductRepository connection established") ;
		}
		catch (Exception e)
		{
			e.printStackTrace(System.err);
		}
	}

	public void testGetProductNames() throws Exception {

		try
		{
			NameServiceInterface nsi = (NameServiceInterface)Naming.lookup("/NameService");
			if ( nsi == null )
			{
				fail("NameService not found") ;
			}
			ProductRepositoryInterface pri = (ProductRepositoryInterface) nsi.lookup("ProductRepository");
			if ( pri == null )
			{
				fail("ProductRepository not found") ;
			}

			// obtain the list of product names
			String[] productNames = pri.getProductNames() ;

			assertEquals("Product name not registered",productNames[0], "TestProduct") ;
		}
		catch (Exception e)
		{
			e.printStackTrace(System.err);
		}
	}


}
