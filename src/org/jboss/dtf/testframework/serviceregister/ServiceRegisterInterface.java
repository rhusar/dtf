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
// $Id: ServiceRegisterInterface.java 170 2008-03-25 18:59:26Z jhalliday $

package org.jboss.dtf.testframework.serviceregister;

import org.jboss.dtf.testframework.serviceregister.*;
import org.jboss.dtf.testframework.testnode.*;

/**
 * Interface exported by the ServiceRegister.  The service register allows TestNodes
 * to register the products it supports along with the OS it is running on.  The coordinator
 * can then query this register to find TestNodes which match given OS/Product criteria.
 */
public interface ServiceRegisterInterface extends java.rmi.Remote
{
	/**
	 * Adds a TestNode into the service register.
	 * @param osId The string id. for the OS the TestNode is running on.
	 * @param productList An array of product id.'s that the TestNode supports.
	 * @param testNodeReference The object reference of the TestNode.
	 */
	public short registerService( 	String 				osId,
								 	String[]			productList,
								 	TestNodeInterface	testNodeReference ) throws java.rmi.RemoteException;

    /**
     * Updates a TestNode in the service register.
     * @param productList An array of product id.'s that the TestNode supports.
     */
    public void reregisterService(  short              serviceId,
                                    String[]			productList ) throws java.rmi.RemoteException, ServiceNotFound;

	/**
	 * Search the service register for TestNodes which are running on the given OS and supporting
	 * the product given.
	 * @param osID The string id. for the OS the TestNode should be running on.
	 * @param product The string id. for the product the TestNode should support.
	 * @returns A list of TestNode Object references for the TestNodes which support the specified
	 * Product/OS combination
	 * @exception ServiceNotFound Thrown if a TestNode
	 * cannot be found which supports the given OS/Product combination.
	 */
	public TestNodeInterface[] lookupService( String	osId,
								  			  String	product ) throws java.rmi.RemoteException, ServiceNotFound;

	/**
	 * Return the TestNodeInterface for the TestNode that is registered with the given unique id
	 * @param serviceId The unique service identifier
	 * @returns An interface to the requested service
	 */
	public TestNodeInterface lookupService(	short	serviceId ) throws java.rmi.RemoteException, ServiceNotFound;

	/**
	 * Get an array of all TestNode's registered
	 * @returns An array of all TestNode's registered
	 */
	public TestNodeInterface[] getRegister() throws java.rmi.RemoteException;

	/**
	 * Removes a TestNode from the service register.
	 * @param testNodeReference The object reference of the TestNode to remove.
	 * @exception ServiceNotFound Thrown if the object
	 * reference isn't one of a known TestNode.
	 */
	public void deregisterService( String osId, TestNodeInterface testNodeReference ) throws java.rmi.RemoteException, ServiceNotFound;

	/**
	 * Disable a given testnodes support for a given product.  This is used by a testnode so that it can continue
	 * receiving deployments without being involved in test runs.  This will usually occur when a test node fails
	 * to deploy a product.
	 *
	 * @param serviceId The service id. of the testnode who's product support is to be altered.
	 * @param productId The name of the product to disable support of.
	 * @throws java.rmi.RemoteException
	 * @throws ServiceNotFound
	 */
	public void disableProductSupport( short serviceId, String productId ) throws java.rmi.RemoteException, ServiceNotFound;

	/**
	 * Enable a given testnodes support for a given product.  This is used by a testnode so that it will be involved in test runs.
	 * @param serviceId The service id. of the testnode who's product support is to be altered.
	 * @param productId The name of the product to disable support of.
	 * @throws java.rmi.RemoteException
	 * @throws ServiceNotFound
	 */
	public void enableProductSupport( short serviceId, String productId ) throws java.rmi.RemoteException, ServiceNotFound;
}
