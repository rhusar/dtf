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
 * $Id: InitiateSoftwareDistribute.java 170 2008-03-25 18:59:26Z jhalliday $
 */
package org.jboss.dtf.tools.initiatesoftwaredistribute;

import org.jboss.dtf.testframework.nameservice.NameServiceInterface;
import org.jboss.dtf.testframework.productrepository.ProductRepositoryInterface;
import org.jboss.dtf.testframework.productrepository.ProductRepository;

import java.rmi.registry.LocateRegistry;
import java.rmi.RemoteException;
import java.rmi.Naming;
import java.net.URL;

public class InitiateSoftwareDistribute
{
	public static long initiate(String nameServiceURI, String productName, String antScriptURL) throws Exception
	{
		long versionId = -1;
		try
		{
			int rmiPort = 1099 ;
			try
			{
				LocateRegistry.createRegistry(rmiPort);
			}
			catch (RemoteException ex)
			{
			}

			NameServiceInterface nameService = (NameServiceInterface)Naming.lookup(nameServiceURI);
			ProductRepositoryInterface pri = (ProductRepositoryInterface)nameService.lookup( ProductRepository.PRODUCT_REPOSITORY_NAMESERVICE_NAME );
			versionId = pri.setProductInstaller(productName, new URL(antScriptURL));
		}
		catch (Exception e)
		{
			throw new Exception("Failed to initiate software distribution: "+e);
		}

		return versionId;
	}

	public static void main(String[] args)
	{
		String nameService = null;
		String productName = null;
		String antScriptURL = null;

		for (int count=0;count<args.length;count++)
		{
			if ( args[count].equals("-help") )
			{
				System.out.println("Usage: InitiateSoftwareDistribute {-help} {-nameservice <nameservice uri>} {-product <product name>} {-script <ant script URL>}");
			}
			else
			if ( args[count].equals("-nameservice") )
			{
				nameService = args[count + 1];
			}
			else
			if ( args[count].equals("-product") )
			{
				productName = args[count + 1];
			}
			else
			if ( args[count].equals("-script") )
			{
				antScriptURL = args[count + 1];
			}
		}

		if ( nameService == null )
		{
			System.out.println("You have not specified the name service URI");
			System.exit(1);
		}

		if ( productName == null )
		{
			System.out.println("You have not specified the product name");
			System.exit(1);
		}

		if ( antScriptURL == null )
		{
			System.out.println("You have not specified the ANT script URL");
			System.exit(1);
		}

		try
		{
			System.out.println("New version number: " + InitiateSoftwareDistribute.initiate(nameService, productName, antScriptURL));
		}
		catch (Exception e)
		{
			System.err.println("ERROR: "+e);
		}
	}
}
