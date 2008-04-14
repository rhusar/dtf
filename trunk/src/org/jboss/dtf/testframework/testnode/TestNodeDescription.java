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

import org.jboss.dtf.testframework.coordinator2.OSProductCombination;

import java.util.ArrayList;
import java.io.Serializable;

/*
 * Copyright (C) 2001, 2002, 2003
 *
 * Arjuna Technologies Ltd.
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: TestNodeDescription.java 170 2008-03-25 18:59:26Z jhalliday $
 */

public class TestNodeDescription implements Serializable
{
    private String                     _osId;
    private RegistrationDaemon         _regDaemon;

    public TestNodeDescription(String osId, RegistrationDaemon regDaemon)
    {
        _osId = osId;
        _regDaemon = regDaemon;
    }

    public short getServiceId()
    {
        return _regDaemon.getServiceId();
    }

    public String getOSID()
    {
        return _osId;
    }

    public ProductSupportInformation getProductSupportInformation()
    {
        return _regDaemon.getProductSupport();
    }
}
