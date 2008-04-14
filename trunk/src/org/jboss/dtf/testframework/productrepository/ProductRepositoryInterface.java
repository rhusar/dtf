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

import java.net.URL;
import java.rmi.RemoteException;

public interface ProductRepositoryInterface extends java.rmi.Remote
{
	/**
	 * Add or update a product configuration in the product repository.
	 *
	 * @param productName
	 * @param productConfiguration
	 * @throws RemoteException
	 */
	public void setProductConfiguration(String productName, ProductConfiguration productConfiguration) throws RemoteException;

	public void remoteProductConfiguration(String productName) throws RemoteException;

    /**
     * Retrieve the product configuration for the given product name.
     *
     * @param productName The name of the product to retrieve.
     */
    public ProductConfiguration getProductConfiguration(String productName) throws java.rmi.RemoteException;

    /**
     * List the products in the repository.
     */
    public String[] getProductNames() throws java.rmi.RemoteException;

    /**
     * Sets the install procedure for a given product in the repository.
     *
     * @param name The unique name of the product the installer is for.
     * @param antURL The URL to the ANT script used to install the product.
     * @return The current product version id.
     */
    public long setProductInstaller(String name, URL antURL) throws java.rmi.RemoteException;

    /**
     * Used to check that the version of a product is the most recent version.
     *
     * @param name The name of the product to check the version against.
     * @param versionId The versionId to check against.
     * @return True if the versionId is the current version, false otherwise.
     */
    public boolean isCurrentVersion(String name, long versionId) throws java.rmi.RemoteException;

    /**
     * Retrieves the URL to the ANT script for installing the product represented
     * by the given unique name.
     *
     * @param name The unique name of the product.
     * @return The URL to the ANT script used to install the product.
     */
    public URL getProductInstaller(String name) throws java.rmi.RemoteException;

    /**
     * Retrieves the current version number for the given product.
     *
     * @param name The unique name of the product.
     * @return The version number of this product.
     */
    public long getCurrentVersion(String name) throws java.rmi.RemoteException;
}
