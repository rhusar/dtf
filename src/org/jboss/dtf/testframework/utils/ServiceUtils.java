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
 * $Id: ServiceUtils.java 170 2008-03-25 18:59:26Z jhalliday $
 */
package org.jboss.dtf.testframework.utils;

import org.jboss.dtf.testframework.nameservice.NameServiceInterface;
import org.jboss.dtf.testframework.serviceregister.ServiceNotFound;
import org.jboss.dtf.testframework.serviceregister.ServiceRegisterInterface;
import org.jboss.dtf.testframework.serviceregister.ServiceRegister;
import org.jboss.dtf.testframework.productrepository.ProductRepositoryInterface;
import org.jboss.dtf.testframework.productrepository.ProductRepository;

import java.rmi.Naming;

public class ServiceUtils
{
	private String	_nameServiceURI;

	public ServiceUtils(String nameServiceURI)
	{
		_nameServiceURI = nameServiceURI;
	}

	public String getNameServiceURI()
	{
		return _nameServiceURI;
	}

	/**
	 * Retrieve a reference to the name service.
	 *
	 * @return A reference to the name service.
	 */
	public NameServiceInterface getNameService() throws ServiceNotFound
	{
		NameServiceInterface nameService = null;

		try
		{
			nameService = (NameServiceInterface) Naming.lookup(_nameServiceURI);
		}
		catch (Exception e)
		{
			throw new ServiceNotFound("Failed to retrieve name service reference: "+e);
		}

		return nameService;
	}

	/**
	 * Retrieve a reference to the product repository.
	 *
	 * @return A reference to the product repository.
	 */
	public ProductRepositoryInterface getProductRepository() throws ServiceNotFound
	{
		ProductRepositoryInterface productRepository = null;

		try
		{
			NameServiceInterface nameService = getNameService();

			productRepository = (ProductRepositoryInterface) nameService.lookup( ProductRepository.PRODUCT_REPOSITORY_NAMESERVICE_NAME );
		}
		catch (Exception e)
		{
			throw new ServiceNotFound("Failed to retrieve name service reference: "+e);
		}

		return productRepository;
	}

	/**
	 * Retrieve a reference to the service register.
	 *
	 * @return A reference to the product repository.
	 */
	public ServiceRegisterInterface getServiceRegister() throws ServiceNotFound
	{
		ServiceRegisterInterface serviceRegister = null;

		try
		{
			NameServiceInterface nameService = getNameService();

			serviceRegister = (ServiceRegisterInterface) nameService.lookup( ServiceRegister.SERVICE_REGISTER_NAME_SERVICE_ENTRY );
		}
		catch (Exception e)
		{
			throw new ServiceNotFound("Failed to retrieve name service reference: "+e);
		}

		return serviceRegister;
	}


}
