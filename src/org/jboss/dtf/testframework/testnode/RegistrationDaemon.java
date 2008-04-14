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
// $Id: RegistrationDaemon.java 170 2008-03-25 18:59:26Z jhalliday $

package org.jboss.dtf.testframework.testnode;

import org.jboss.dtf.testframework.serviceregister.*;
import org.jboss.dtf.testframework.utils.ServiceUtils;

import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.DatagramPacket;
import java.util.Enumeration;
import java.io.Serializable;

public class RegistrationDaemon extends Thread implements Serializable
{
	private final static int LISTENING_PORT = 8989;

	protected boolean					_running;
	protected transient ServiceUtils	_serviceUtils;
	protected TestNodeInterface			_testNode;
	protected String					_osId;
	protected short						_serviceId = -1;
	protected ProductSupportInformation	_productSupport = new ProductSupportInformation();

	public RegistrationDaemon(ServiceUtils serviceUtils, TestNodeInterface testNode, String osId, String[] supportedProductList)
	{
		_running = false;
		_serviceUtils = serviceUtils;
		_testNode = testNode;
		_osId = osId;
		_productSupport.setProductList( supportedProductList );
	}

	public void stopDaemon()
	{
		_running = false;
		deregisterService();
	}


	public void startDaemon()
	{
		if ( (_running) && (!serviceExistsInRegister()) )
		{
			if (registerService())
			{
				System.out.println("Successfully registered testnode with the ServiceRegister");
			}
		}

		_running = true;
		start();
	}

    public ProductSupportInformation getProductSupport()
    {
        return _productSupport;
    }


	public void run()
	{
		DatagramSocket socket = null;

		try
		{
			socket = new DatagramSocket( LISTENING_PORT );
		}
		catch (SocketException e)
		{
			System.err.println("ERROR - Failed top open datagram socket: "+e);
			_running = false;
		}

		while (_running)
		{
			try
			{
				byte[] buffer = new byte[1];
				DatagramPacket packet = new DatagramPacket(buffer,0,buffer.length);
				socket.receive(packet);

				if ( (_running) && (!serviceExistsInRegister()) )
				{
					System.out.println("Not found in ServiceRegister");

					if (registerService())
					{
						System.out.println("Successfully registered testnode with the ServiceRegister");
					}
				}
			}
			catch (Exception e)
			{
				System.err.println("ERROR - General failure: "+e);
			}
		}
	}

	protected boolean serviceExistsInRegister()
	{
		boolean returnValue = true;

		try
		{
			ServiceRegisterInterface register = _serviceUtils.getServiceRegister();

			register.lookupService( getServiceId() );
		}
		catch (Exception e)
		{
			returnValue = false;
		}

		return(returnValue);
	}

    private boolean reregisterService()
    {
        try
        {
            ServiceRegisterInterface register = _serviceUtils.getServiceRegister();

            // Register this testnode with the service register
            register.reregisterService( _serviceId, _productSupport.getProductList());
        }
        catch (Exception e)
        {
            System.err.println("Exception Caught: " + e);
            e.printStackTrace(System.err);
            return(false);
        }

        return(true);
    }

	protected boolean registerService()
	{
		try
		{
			ServiceRegisterInterface register = _serviceUtils.getServiceRegister();

			// Register this testnode with the service register
			_serviceId = register.registerService( _osId, _productSupport.getProductList(), _testNode);

			if ( _productSupport.hasEntries() )
			{
				Enumeration productEnum = _productSupport.getProductEnumeration();

				while (productEnum.hasMoreElements())
				{
					String productName = (String)productEnum.nextElement();
					boolean enabled = _productSupport.isProductSupported(productName);

					if ( enabled )
					{
						enableProduct( productName );
					}
					else
					{
						disableProduct( productName );
					}
				}
			}
		}
		catch (Exception e)
		{
			System.err.println("Exception Caught: " + e);
			e.printStackTrace(System.err);
			return(false);
		}

		return(true);
	}

	protected boolean deregisterService()
	{
		try
		{
			ServiceRegisterInterface register = _serviceUtils.getServiceRegister();
			register.deregisterService(_osId, _testNode);
			System.out.println("Deregistered from Service Registry");
		}
		catch (ServiceNotFound snf)
		{
			// Ignore it
		}
		catch (Exception e)
		{
			System.err.println("Exception Caught: " + e);
			return(false);
		}

		return(true);
	}

	public short getServiceId()
	{
		return(_serviceId);
	}

    public void reregisterService(String[] supportedProductList)
    {
        _productSupport.setProductList(supportedProductList);

        if (!serviceExistsInRegister())
        {
            System.out.println("Not found in ServiceRegister");

            if (registerService())
            {
                System.out.println("Successfully registered testnode with the ServiceRegister");
            }
        }
        else
        {
            System.out.println("Re-registering test node");
            if (reregisterService())
            {
                System.out.println("Successfully registered testnode with the ServiceRegister");
            }
        }
    }

	public void disableProduct(String productName)
	{
		try
		{
            _productSupport.disableProduct(productName);

			if ( _serviceId != -1 )
			{
            	_serviceUtils.getServiceRegister().disableProductSupport(_serviceId, productName);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace(System.err);
		}
	}

	public void enableProduct(String productName)
	{
		try
		{
            _productSupport.enableProduct(productName);

			if ( _serviceId != -1 )
			{
            	_serviceUtils.getServiceRegister().enableProductSupport(_serviceId, productName);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace(System.err);
		}
	}
}
