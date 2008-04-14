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
// $Id: ServiceRegister.java 170 2008-03-25 18:59:26Z jhalliday $

package org.jboss.dtf.testframework.serviceregister;

import org.jboss.dtf.testframework.serviceregister.*;
import org.jboss.dtf.testframework.nameservice.*;
import org.jboss.dtf.testframework.utils.*;

import org.jboss.dtf.testframework.testnode.*;

import java.rmi.server.*;
import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.io.*;
import java.util.*;
import java.net.InetAddress;
import java.net.DatagramSocket;
import java.net.DatagramPacket;

public class ServiceRegister extends UnicastRemoteObject implements ServiceRegisterInterface
{
    public final static String SERVICE_REGISTER_NAME_SERVICE_ENTRY = "/ServiceRegister";

    private final static String NAME_SERVICE_URI_PARAMETER = "-nameservice";
    private final static String HELP_PARAMETER = "-help";

	private final static int NUMBER_OF_BROADCAST_PACKETS = 5;
	private final static int BROADCAST_SLEEP_PERIOD = 5000;
	private final static int BROADCAST_PORT = 8989;

	private Hashtable 	_services = new Hashtable();
	private short		_uniqueId = 0;
	private HashMap		_serviceRegister = new HashMap();

	private class ServiceInformation implements java.io.Serializable
	{
		private String[]			_productList;
		private HashSet				_disabledProducts;
		private TestNodeInterface	_testNodeReference;
		private short				_serviceId;
		private String				_osId;

		public ServiceInformation(String osId, String[] productList, TestNodeInterface testNodeRef, short serviceId)
		{
			_osId = osId;
			_serviceId = serviceId;
			_productList = productList;
			_testNodeReference = testNodeRef;
			_disabledProducts = new HashSet();
		}

		public void disableProduct(String product)
		{
			_disabledProducts.add(product);
		}

		public void enableProduct(String product)
		{
			_disabledProducts.remove(product);
		}

		public boolean supportsProduct(String product)
		{
			if ( _disabledProducts.contains(product) )
			{
				return false;
			}

			for (int count=0;count<_productList.length;count++)
			{
				if (_productList[count].equalsIgnoreCase(product))
				{
					return(true);
				}
			}
			return(false);
		}
	}

	public ServiceRegister() throws java.rmi.RemoteException, java.net.UnknownHostException, java.net.SocketException
	{
		super();
	}

	/**
	 * Disable a given testnodes support for a given product.  This is used by a testnode so that it can continue
	 * receiving deployments without being involved in test runs.  This will usually occur when a test node fails
	 * to deploy a product.
	 *
	 * @param serviceId The service id. of the testnode who's product support is to be altered.
	 * @param productId The name of the product to disable support of.
	 * @throws RemoteException
	 * @throws ServiceNotFound
	 */
	public void disableProductSupport(short serviceId, String productId) throws RemoteException, ServiceNotFound
	{
		ServiceInformation service = ((ServiceInformation)_serviceRegister.get(""+serviceId));

		if ( service == null )
		{
			throw new ServiceNotFound("Service "+service+" not found");
		}

		service.disableProduct(productId);
	}

	/**
	 * Enable a given testnodes support for a given product.  This is used by a testnode so that it will be involved in test runs.
	 * @param serviceId The service id. of the testnode who's product support is to be altered.
	 * @param productId The name of the product to disable support of.
	 * @throws RemoteException
	 * @throws ServiceNotFound
	 */
	public void enableProductSupport(short serviceId, String productId) throws RemoteException, ServiceNotFound
	{
		ServiceInformation service = ((ServiceInformation)_serviceRegister.get(""+serviceId));

		if ( service == null )
		{
			throw new ServiceNotFound("Service "+service+" not found");
		}

		service.enableProduct(productId);
	}

	public short registerService( 	String 				osId,
								 	String[]			productList,
								 	TestNodeInterface	testNodeReference ) throws java.rmi.RemoteException
	{
		try
		{
			/** If this node has been registered previously then deregister it **/
			if ( findService(osId, testNodeReference) != null )
			{
				deregisterService(osId, testNodeReference);
			}
		}
		catch (ServiceNotFound e)
		{
			// Can be safely ignored
		}

		System.out.println("Adding service information to registry");
		ServiceInformation newService = new ServiceInformation(osId, productList, testNodeReference,  _uniqueId);

		/*
		 * Find the service list within the services vector for this OS id
		 */
		Vector serviceList = (Vector)_services.get(osId);

		/*
		 * If the service list doesn't exist create one
		 */
		if (serviceList == null)
		{
			serviceList = new Vector();
			_services.put(osId, serviceList);
		}

		/*
		 * Add the service to the list
		 */
		serviceList.addElement(newService);

		_serviceRegister.put(""+_uniqueId, newService);
		System.out.println("Service successfully added to the registry");

		return(_uniqueId++);
	}

    /**
     * Updates a TestNode in the service register.
     * @param productList An array of product id.'s that the TestNode supports.
     */
    public void reregisterService(short serviceId,
                                  String[] productList) throws RemoteException, ServiceNotFound
    {
        ServiceInformation service = ((ServiceInformation)_serviceRegister.get(""+serviceId));

        if ( service == null )
            throw new ServiceNotFound();

        if ( service._testNodeReference.ping() )
        {
            TestNodeInterface testNode = service._testNodeReference;

            service._productList = productList;
        }
        else
        {
            removeService(serviceId);
            System.out.println("TestNode is not responding, removing");
            throw new ServiceNotFound();
        }
    }

	public TestNodeInterface lookupService(	short	serviceId ) throws java.rmi.RemoteException, ServiceNotFound
	{
		if (_serviceRegister.get(""+serviceId) == null)
			throw new ServiceNotFound();

		ServiceInformation service = ((ServiceInformation)_serviceRegister.get(""+serviceId));

		if ( service != null )
        {
            if ( service._testNodeReference.ping() )
            {
                TestNodeInterface testNode = (TestNodeInterface)service._testNodeReference;
            }
            else
            {
                removeService(serviceId);
                System.out.println("TestNode is not responding, removing");
                throw new ServiceNotFound();
            }
        }

		return(service._testNodeReference);
	}

	protected boolean removeService(short serviceId)
	{
		ServiceInformation service = (ServiceInformation)_serviceRegister.get(""+serviceId);

		if (service != null)
		{
			Vector serviceList = (Vector)_services.get(service._osId);

			serviceList.remove(service);

			_serviceRegister.remove(""+serviceId);
		}
		else
		{
			return(false);
		}

		return(true);
	}

	public TestNodeInterface[] getRegister() throws java.rmi.RemoteException
	{
		ArrayList results = new ArrayList();
		/*
		 * Get service list
		 */
		Object[] osList = _services.values().toArray();

		for (int count=0;count<osList.length;count++)
		{
			Vector serviceVector = (Vector)osList[count];
			Object[] services = serviceVector.toArray();

			for (int serviceCount=0;serviceCount<services.length;serviceCount++)
			{
				ServiceInformation serviceInfo = (ServiceInformation)services[serviceCount];

                try
                {
                    serviceInfo._testNodeReference.ping();
                    results.add(serviceInfo._testNodeReference);
                }
                catch (Exception e)
                {
                    System.err.println("Testnode not responding - removing from registry");
                    removeService(serviceInfo._serviceId);
                }
			}
		}

		TestNodeInterface[] arrayResult = new TestNodeInterface[results.size()];

		for (int count=0;count<arrayResult.length;count++)
			arrayResult[count] = (TestNodeInterface)results.get(count);

		return(arrayResult);

	}

	public TestNodeInterface[] lookupService( 	String	osId,
								  				String	product ) throws java.rmi.RemoteException, ServiceNotFound
	{
		/*
		 * Get service list for this os
		 */
		Vector serviceList = (Vector)_services.get(osId);

		/*
		 * If no service list exists for this os then throw ServiceNotFound exception
		 */
		if (serviceList == null)
		{
			throw new ServiceNotFound();
		}
		TestNodeInterface[] results = searchServiceList(serviceList, product);
		if (results.length==0)
		{
			throw new ServiceNotFound();
		}
		return(results);
	}

	private TestNodeInterface[] searchServiceList(Vector serviceList, String productToSearchFor)
	{
		Vector results = new Vector();
		Vector servicesToRemove = new Vector();

		Iterator itr = serviceList.iterator();

		while ( itr.hasNext() )
		{
			ServiceInformation service = (ServiceInformation)itr.next();
			System.out.println("Checking service: "+service._osId+","+service._productList);

			try
			{
				if ( service.supportsProduct(productToSearchFor) )
                {
                    if ( service._testNodeReference.ping() )
                    {
                        // Ensure TestNode is responding
                        TestNodeInterface testNode = (TestNodeInterface)service._testNodeReference;
                        results.addElement(service._testNodeReference);
                    }
                    else
                    {
                        servicesToRemove.add(service);
                        System.out.println("TestNode is not responding, removing");
                    }
                }
			}
			catch (java.rmi.RemoteException e)
			{
				servicesToRemove.add(service);
				System.out.println("TestNode is not responding, removing");
			}
		}

		for (int count=0;count<servicesToRemove.size();count++)
		{
			ServiceInformation service = (ServiceInformation)servicesToRemove.elementAt(count);

			removeService(service._serviceId);
		}

		TestNodeInterface[] arrayResult = new TestNodeInterface[results.size()];

		for (int count=0;count<arrayResult.length;count++)
			arrayResult[count] = (TestNodeInterface)results.elementAt(count);

		return(arrayResult);
	}

	private ServiceInformation findService( String osId, TestNodeInterface testNodeReference )
							throws ServiceNotFound
	{
		/*
		 * Get service list for this os
		 */
		Vector serviceList = (Vector)_services.get(osId);

		/*
		 * If no list exists for this OS throw ServiceNotFound exception
		 */
		if (serviceList == null)
		{
			throw new ServiceNotFound();
		}
		else
		{
			/*
			 * Search for object reference
			 */
			int count=0;
			boolean finished = false;
			while ( (count<serviceList.size()) && (!finished) )
			{
				ServiceInformation service = (ServiceInformation)serviceList.elementAt(count);

				/*
				 * If the object reference is found remove it from vector
				 */
				if ( service._testNodeReference.equals(testNodeReference) )
				{
					return service;
				}
				count++;
			}

			/*
			 * If the object reference wasn't found throw ServiceNotFound exception
			 */
			if (!finished)
				throw new ServiceNotFound();
		}

		return null;
	}

	public void deregisterService( String osId, TestNodeInterface testNodeReference )
							throws java.rmi.RemoteException, ServiceNotFound
	{
		/*
		 * Get service list for this os
		 */
		Vector serviceList = (Vector)_services.get(osId);

		/*
		 * If no list exists for this OS throw ServiceNotFound exception
		 */
		if (serviceList == null)
		{
			throw new ServiceNotFound();
		}
		else
		{
			/*
			 * Search for object reference
			 */
			int count=0;
			boolean finished = false;
			while ( (count<serviceList.size()) && (!finished) )
			{
				ServiceInformation service = (ServiceInformation)serviceList.elementAt(count);

				/*
				 * If the object reference is found remove it from vector
				 */
				if ( service._testNodeReference.equals(testNodeReference) )
				{
					serviceList.remove(count);
					_serviceRegister.remove(""+service._serviceId);
					finished = true;
				}
				count++;
			}

			/*
			 * If the object reference wasn't found throw ServiceNotFound exception
			 */
			if (!finished)
				throw new ServiceNotFound();
		}
	}

	private final static int getRMIPort()
	{
		String rmiPort = System.getProperty("rmi.port", ""+Registry.REGISTRY_PORT);

		return Integer.parseInt(rmiPort);
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

	private static void broadcastStartup()
	{
		try
		{
			System.out.println("Broadcasting service register existence..");
			byte broadcastCount = 0;
			DatagramSocket ds = new DatagramSocket();

			while ( broadcastCount < NUMBER_OF_BROADCAST_PACKETS )
			{
				try
				{
					byte[] buffer = new byte[] { broadcastCount ++ };
					DatagramPacket broadcastPacket = new DatagramPacket(buffer, buffer.length, InetAddress.getByName("255.255.255.255"), BROADCAST_PORT);

					ds.send(broadcastPacket);

					Thread.sleep(BROADCAST_SLEEP_PERIOD);
				}
				catch (Exception e)
				{
					System.out.println("Warning - while attempting to send broadcast: "+e);
				}
			}
			System.out.println("Broadcast complete.");
		}
		catch (Exception e)
		{
			System.err.println("ERROR - Could not open datagram socket for broadcast: "+e);
		}
	}

	public static void main(String args[])
	{
		if ( args.length == 0 )
		{
			System.out.println("ServiceRegister Usage: org.jboss.dtf.testframework.serviceregister {-nameservice //hostname/nameservice} {-help}");
			System.exit(0);
		}

		try
		{
			boolean readRegister = true;
			long timeout = 5000;
			String serviceName = "ServiceRegister";
			String nameServiceURI = "//localhost/NameService" ;

			for (int count=0;count<args.length;count++)
			{
				if ( ( args[count].equalsIgnoreCase(NAME_SERVICE_URI_PARAMETER) ))
				{
					nameServiceURI = args[count+1];
				}
				else
				if ( args[count].equalsIgnoreCase(HELP_PARAMETER) )
				{
					System.out.println("Usage: ServiceRegister {-nameservice //hostname/nameservice} {-help}");
					System.exit(0);
				}
			}

            initialiseRegistry();

			ServiceRegister register = new ServiceRegister();

	        NameServiceInterface nameService = (NameServiceInterface)Naming.lookup(nameServiceURI);

			nameService.rebindReference(SERVICE_REGISTER_NAME_SERVICE_ENTRY, register);

		    Naming.rebind("//"+java.net.InetAddress.getLocalHost().getHostName()+":"+getRMIPort()+"/"+serviceName, register);
		    System.out.println("Ready");

			broadcastStartup();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			System.exit(0);
		}
	}
}
