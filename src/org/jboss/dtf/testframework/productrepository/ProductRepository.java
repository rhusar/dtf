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

package org.jboss.dtf.testframework.productrepository;

import java.io.*;
import java.util.Hashtable;
import java.rmi.RemoteException;
import java.rmi.Naming;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.net.URL;

import org.jboss.dtf.testframework.testnode.TestNodeInterface;
import org.jboss.dtf.testframework.nameservice.NameServiceInterface;
import org.jboss.dtf.testframework.serviceregister.ServiceRegisterInterface;
import org.jboss.dtf.testframework.serviceregister.ServiceRegister;
import org.jboss.dtf.testframework.utils.fsmonitors.DirectoryMonitor;
import org.jboss.dtf.testframework.utils.fsmonitors.DirectoryChangeListener;

public class ProductRepository extends java.rmi.server.UnicastRemoteObject implements ProductRepositoryInterface, DirectoryChangeListener
{
    public final static String PRODUCT_REPOSITORY_NAMESERVICE_NAME = "ProductRepository";

    private final static String PRODUCTS_STORE_DIRECTORY = "products";
    private final static String INSTALLER_DETAILS_FILENAME = "installers.cfg";
    private final static String DEFAULT_NAME_SERVICE_URI = "//localhost/NameService";
	private final static String PRODUCT_CONFIG_SUFFIX = ".xml";

    private final static String NAME_SERVICE_URI_PARAMETER = "-nameservice";
    private final static String HELP_PARAMETER = "-help";


    private Hashtable               _products = new Hashtable();
    private Hashtable               _installers = new Hashtable();
    private NameServiceInterface    _nameService = null;
	private DirectoryMonitor		_dirMonitor = new DirectoryMonitor();

	private final static int getRMIPort()
	{
		String rmiPort = System.getProperty("rmi.port", ""+Registry.REGISTRY_PORT);

		return Integer.parseInt(rmiPort);
	}

    public ProductRepository(NameServiceInterface ns) throws RemoteException
    {
        super();

        try
        {
            _nameService = ns;

            /** Retrieve installer table from disk **/
            deserializeInstallers();

			populateProducts();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.exit(1);
        }
    }

	public void fileAdded(File f)
	{
		if ( f.getName().endsWith(PRODUCT_CONFIG_SUFFIX) )
		{
			System.out.println("New product definition detected "+f.getName());
			try
			{
				ProductConfiguration productConfig = ProductConfiguration.deserializeXML(f);

				_products.put(productConfig.getName(), productConfig);

				initialiseProductDistribution(productConfig.getName(), false);
			}
			catch (Exception e)
			{
				System.err.println("Failed to populate the product repository from '"+f.getName()+"': "+e);
			}
		}
	}

	public void fileChanged(File f)
	{
		if ( f.getName().endsWith(PRODUCT_CONFIG_SUFFIX) )
		{
			System.out.println("Product configuration change detected for "+f.getName());
			try
			{
				ProductConfiguration productConfig = ProductConfiguration.deserializeXML(f);

				_products.put(productConfig.getName(), productConfig);

				initialiseProductDistribution(productConfig.getName(), false);
			}
			catch (Exception e)
			{
				System.err.println("Failed to populate the product repository from '"+f.getName()+"': "+e);
			}
		}
	}

	public void fileDeleted(File f)
	{
		// Not supported
	}

	private void populateProducts()
	{
		/** Ensure the products directory exists **/
		File productsDir = new File(PRODUCTS_STORE_DIRECTORY);

		if (!productsDir.isDirectory()) {
			// The products directory does not exist - so create it.
			// If it did not exsit - we could raise an exception and let the user fix it.
			productsDir.mkdirs();
		}

		_dirMonitor.addDirectory(productsDir, this);

		File[] productFiles = productsDir.listFiles();

		try
		{
			for (int count=0;count<productFiles.length;count++)
			{
				if ( productFiles[count].getName().endsWith(PRODUCT_CONFIG_SUFFIX) )
				{
					ProductConfiguration productConfig = ProductConfiguration.deserializeXML(productFiles[count]);

					_products.put( productConfig.getName(), productConfig );
				}
			}
		}
		catch (Exception e)
		{
			System.err.println("Failed to populate the product repository: "+e);
		}
	}

    /**
     * Retrieve the product configuration for the given product name.
     *
     * @param productName The name of the product to retrieve.
     */
    public ProductConfiguration getProductConfiguration(String productName) throws RemoteException
    {
        return (ProductConfiguration) _products.get( productName );
    }

	public void remoteProductConfiguration(String productName) throws RemoteException
	{
		ProductConfiguration productConfig = getProductConfiguration(productName);
		productConfig.getProductConfigurationFile().delete();
		_products.remove(productName);
		_installers.remove(productName);
	}

	public void setProductConfiguration(String productName, ProductConfiguration productConfiguration) throws RemoteException
	{
		try
		{
			productConfiguration.getProductConfigurationFile();
			productConfiguration.serializeXML();
			_products.put(productName, productConfiguration);

			initialiseProductDistribution(productName, false);
		}
		catch (IOException e)
		{
			throw new RemoteException("Failed to persist product configuration: "+e);
		}
	}

    /**
     * List the products in the repository.
     */
    public String[] getProductNames() throws RemoteException
    {
        String[] productNames = new String[_products.keySet().size()];
        _products.keySet().toArray(productNames);

        return productNames;
    }

    /**
     * Sets the install procedure for a given product in the repository.
     *
     * @param name The unique name of the product the installer is for.
     * @param antURL The URL to the ANT script used to install the product.
     * @return The current product version id.
     */
    public long setProductInstaller(String name, URL antURL) throws RemoteException
    {
        ProductInstallerDetails pid = (ProductInstallerDetails)_installers.get(name);

        if ( pid == null )
        {
            pid = new ProductInstallerDetails(name, antURL);

            _installers.put(name, pid);
        }
        else
        {
            pid.setInstallingAntScriptURL(antURL);
        }

        serializeInstallerDetails();

        initialiseProductDistribution(name, true);

        return pid.getVersionId();
    }

    private NameServiceInterface getNameService()
    {
        return _nameService;
    }

    private boolean initialiseProductDistribution(String productName, boolean deploySoftware)
    {
        boolean result = true;
        long successCount = 0;
        long failureCount = 0;

        try
        {
            ServiceRegisterInterface sri = (ServiceRegisterInterface) getNameService().lookup(ServiceRegister.SERVICE_REGISTER_NAME_SERVICE_ENTRY);

            TestNodeInterface[] tni = sri.getRegister();
			NodeUpdateThread[] nut = new NodeUpdateThread[tni.length];

            for (int count=0;count<tni.length;count++)
            {
				System.out.println("Initiating product '"+productName+"' "+ (deploySoftware ? "software" : "configuration") +" update on node #"+count);
				nut[count] = new NodeUpdateThread(tni[count], productName, deploySoftware);
            }

			for (int count=0;count<tni.length;count++)
			{
				nut[count].join();

				System.out.println("Node '"+nut[count].getNodeName()+"' : " + ( nut[count].isSuccess() ? "Success" : "Failure ") );

				if ( !nut[count].isSuccess() )
				{
					System.out.println("Node restart has "+ ( nut[count].hasShutdownFailed() ? "failed" : "succeeded" ) );

					result = false;
					failureCount++;
				}
				else
				{
					successCount++;
				}
			}
        }
        catch (Exception e)
        {
            System.err.println("Failed to initialise product distribution: "+e);
            result = false;
        }

        System.out.println("Product distribution complete, Successes: "+successCount+", Failures: "+failureCount);

        return result;
    }

    /**
     * Retrieves the URL to the ANT script for installing the product represented
     * by the given unique name.
     *
     * @param name The unique name of the product.
     * @return The URL to the ANT script used to install the product, null if this product has no installer.
     */
    public URL getProductInstaller(String name) throws RemoteException
    {
        ProductInstallerDetails pid = (ProductInstallerDetails)_installers.get(name);
        URL returnValue = null;

        if ( pid != null )
        {
            returnValue = pid.getInstallingAntScriptURL();
        }

        return returnValue;
    }

    /**
     * Used to check that the version of a product is the most recent version.
     *
     * @param name The name of the product to check the version against.
     * @param versionId The versionId to check against.
     * @return True if the versionId is the current version (or if no installer exists), false otherwise.
     */
    public boolean isCurrentVersion(String name, long versionId) throws RemoteException
    {
        ProductInstallerDetails pid = (ProductInstallerDetails)_installers.get(name);
        boolean returnValue = true;

        if ( pid != null )
        {
            returnValue = pid.getVersionId() == versionId;
        }

        return returnValue;
    }

    private void serializeInstallerDetails()
    {
        try
        {
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream( INSTALLER_DETAILS_FILENAME ));
            out.writeObject(_installers);
            out.close();
        }
        catch (java.io.IOException e)
        {
            System.err.println("ERROR - While serializing the installer details table: "+e);
        }
    }

    private void deserializeInstallers()
    {
        try
        {
        	FileInputStream fis ;

        	try {
        		fis = new FileInputStream( INSTALLER_DETAILS_FILENAME ) ;
        	}
        	catch (FileNotFoundException e) {
        		// file was not found - start with an empty product-installer map
        		_installers = new Hashtable() ;
        		return ;
        	}

        	// found an existing file
            ObjectInputStream in = new ObjectInputStream(fis);
            _installers = (Hashtable)in.readObject();
            in.close();
        }
        catch (Exception e)
        {
            System.err.println("ERROR - While deserializing the installer details table: "+e);
        }
    }

    /**
     * Retrieves the current version number for the given product.
     *
     * @param name The unique name of the product.
     * @return The version number of this product (-1 if not found).
     */
    public long getCurrentVersion(String name) throws RemoteException
    {
        ProductInstallerDetails pid = (ProductInstallerDetails)_installers.get(name);
        long returnValue = -1;

        if ( pid != null )
        {
            returnValue = pid.getVersionId();
        }

        return returnValue;
    }

    public static void main(String[] args)
    {
        String nameServiceURI = DEFAULT_NAME_SERVICE_URI;

        System.out.println("Product Repository");

        for (int count=0;count<args.length;count++)
        {
            if ( args[count].equalsIgnoreCase( NAME_SERVICE_URI_PARAMETER ) )
            {
                nameServiceURI = args[count + 1];
            }
            if ( args[count].equalsIgnoreCase( HELP_PARAMETER ) )
            {
                System.out.println("Usage: ProductRepository ["+NAME_SERVICE_URI_PARAMETER+" //hostname/nameservice]");
            }
        }

        try
        {
			int rmiPort = getRMIPort();
            System.out.println("Creating RMI registry on port "+rmiPort);
            LocateRegistry.createRegistry(rmiPort);
        }
        catch (RemoteException ex)
        {
        }

        try
        {
            NameServiceInterface ns = (NameServiceInterface)Naming.lookup(nameServiceURI);

            ProductRepositoryInterface pri = new ProductRepository(ns);

            ns.bindReference(PRODUCT_REPOSITORY_NAMESERVICE_NAME,pri);

            System.out.println("[ repository size: "+pri.getProductNames().length+" ]");
            System.out.println("Ready");
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
        }
    }

	private class NodeUpdateThread extends Thread
	{
		private TestNodeInterface	_tni;
		private String				_productName;
		private String				_nodeName;
		private boolean				_success = false;
		private boolean				_shutdownFailure = false;
        private boolean             _deploySoftware = false;

		public NodeUpdateThread(TestNodeInterface tni, String productName, boolean deploySoftware)
		{
			_tni = tni;
			_productName = productName;
            _deploySoftware = deploySoftware;

			start();
		}

		public boolean isSuccess()
		{
			return !this.isAlive() && _success;
		}

		public boolean hasShutdownFailed()
		{
			return _shutdownFailure;
		}

		public String getNodeName()
		{
			return _nodeName;
		}

		public void run()
		{
			try
			{
				_nodeName = _tni.getName();

				if ( _tni.updateSoftware(_productName, _deploySoftware) )
				{
					_success = true;
				}
				else
				{
					_success = false;
				}
			}
			catch (Exception e)
			{
				_success = false;

				try
				{
					System.err.println("Failed to initiate software update on node '"+_tni.getName()+"' :"+e);

					_shutdownFailure = false;
				}
				catch (Exception e2)
				{
					_shutdownFailure = true;
				}
			}
		}
	}
}
