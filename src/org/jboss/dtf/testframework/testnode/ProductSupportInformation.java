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
package org.jboss.dtf.testframework.testnode;

import java.io.Serializable;
import java.util.Hashtable;
import java.util.Enumeration;

/*
 * Copyright (C) 1998, 1999, 2000, 2001, 2002, 2003
 *
 * Arjuna Technologies Ltd.
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: ProductSupportInformation.java 170 2008-03-25 18:59:26Z jhalliday $
 */

public class ProductSupportInformation implements Serializable
{
    private Hashtable   _productSupport = new Hashtable();
    private String[]    _supportedProductList = null;

    public void enableProduct(String productName)
    {
		_productSupport.put(productName, new Boolean(true));
    }

    public void disableProduct(String productName)
    {
        _productSupport.put(productName, new Boolean(false));
    }

    public boolean isProductSupported(String productName)
    {
        return !_productSupport.containsKey(productName) || ((Boolean)_productSupport.get(productName)).booleanValue();
    }

    public boolean hasEntries()
    {
        return !_productSupport.isEmpty();
    }

    public Enumeration getProductEnumeration()
    {
        return _productSupport.keys();
    }

    public void setProductList(String[] supportedProductList)
    {
        _supportedProductList = supportedProductList;
        Hashtable backup = new Hashtable(_productSupport);

        _productSupport = new Hashtable();

        for (int count=0;count<supportedProductList.length;count++)
        {
			Boolean value = (Boolean)backup.get(supportedProductList[count]);
            _productSupport.put(supportedProductList[count], value != null ? value : new Boolean(true));
        }
    }

    public String[] getProductList()
    {
        return _supportedProductList;
    }
}
