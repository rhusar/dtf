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
 * $Id: DeploySoftware.java 170 2008-03-25 18:59:26Z jhalliday $
 */
package org.jboss.dtf.testframework.anttasks;

import org.apache.tools.ant.Task;
import org.apache.tools.ant.BuildException;
import org.jboss.dtf.testframework.nameservice.NameServiceInterface;
import org.jboss.dtf.testframework.productrepository.ProductRepository;
import org.jboss.dtf.testframework.productrepository.ProductRepositoryInterface;

import java.rmi.Naming;
import java.net.URL;
import java.net.MalformedURLException;

public class DeploySoftware extends Task
{
	private String	_product = null;
	private String	_url = null;
	private String  _nameServiceURI = null;

	public void setProduct(String name)
	{
		_product = name;
	}

	public void setUrl(String url)
	{
		_url = url;
	}

	public void setNameservice(String nameService)
	{
		_nameServiceURI = nameService;
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
		if ( _product == null )
		{
			throw new BuildException("Product name not specified");
		}

		if ( _url == null )
		{
			throw new BuildException("ANT script URL not specified");
		}

		if ( _nameServiceURI == null )
		{
			throw new BuildException("The name service URI has not been specified");
		}

		try
		{
			NameServiceInterface nsi = (NameServiceInterface)Naming.lookup(_nameServiceURI);

			ProductRepositoryInterface pri = (ProductRepositoryInterface) nsi.lookup(ProductRepository.PRODUCT_REPOSITORY_NAMESERVICE_NAME);

			long version = pri.setProductInstaller(_product, new URL(_url));

			if ( version != -1 )
			{
				log(_product+" version successfully updated to "+version);
			}
			else
			{
				log(_product+" version failed to be updated");
			}
		}
		catch (MalformedURLException e)
		{
			throw new BuildException("The URL '"+_url+"' is invalid: "+e);
		}
		catch (Exception e)
		{
			throw new BuildException("An unexpected exception occurred: "+e);
		}

	}
}
