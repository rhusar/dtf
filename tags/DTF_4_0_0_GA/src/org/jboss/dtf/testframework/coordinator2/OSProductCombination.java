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
package org.jboss.dtf.testframework.coordinator2;

import org.jboss.dtf.testframework.productrepository.ProductRepositoryInterface;
import org.jboss.dtf.testframework.productrepository.ProductConfiguration;

import java.util.Hashtable;
import java.rmi.RemoteException;

public class OSProductCombination
{
	private String  _permutationId = null;

	private String	_osId;
	private String 	_productId;

	public OSProductCombination(String osId, String productId)
	{
		_osId = osId;
		_productId = productId;
	}

    public OSProductCombination(OSProductCombination osp)
    {
        _osId = osp.getOSId();
        _productId = osp.getProductId();
    }

    public void setOSId(String id)
    {
        _osId = id;
    }

    public void setProductId(String id)
    {
        _productId = id;
    }

	public String getOSId()
	{
		return _osId;
	}

	public String getProductId()
	{
		return _productId;
	}

    public boolean equals(Object o)
    {
        if ( o instanceof OSProductCombination )
        {
            OSProductCombination other = (OSProductCombination)o;

            return other.toString().equals(this.toString());
        }

        return false;
    }

	public String toString()
	{
		return getPermutationId();
	}

    /**
     * Convert this OS/Product combination into a 4 character hex code
     *
     * @return The permutation code for this OS/Product combination
     */
	public final String getPermutationId()
	{
		if ( _permutationId == null )
		{
			_permutationId = getProductPermutationId(_productId).replace(' ','_');
		}

		return _osId+"_"+_permutationId;
	}

	/**
	 * This method returns the product permutation id. as stored in the product repository.
	 * The information is cached so that multiple calls to a remote server is not necessary.
	 *
	 * @return
	 */
	private final static String getProductPermutationId(String productId)
	{
        ProductRepositoryInterface pri = Coordinator.getProductRepository();
		String permutationId = null;

		permutationId = (String)_localPermutationIdCache.get(productId);

		if ( permutationId == null )
		{
			try
			{
				ProductConfiguration pc = pri.getProductConfiguration(productId);

				if ( pc != null )
				{
					permutationId = pc.getPermutationId();

					// Cache the remote result locally
					_localPermutationIdCache.put(productId, permutationId);
				}
				else
				{
					System.err.println("The product '"+productId+"' is not defined in the product repository");
				}
			}
			catch (RemoteException e)
			{
                System.err.println("Failed to contact the product repository to retrieve the product permutation id");
			}
		}

		return permutationId;
	}

	private static Hashtable _localPermutationIdCache = new Hashtable();
}
