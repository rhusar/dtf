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
import java.io.Serializable;

public class ProductInstallerDetails implements Serializable
{
    private String  _name = null;
    private URL     _installingAntScript = null;
    private long    _currentVersionId = -1;

    public ProductInstallerDetails(String name, URL installerAntScript)
    {
        this(name, installerAntScript, 0x00);
    }

    public ProductInstallerDetails(String name, URL installerAntScript, long version)
    {
        _name = name;
        _installingAntScript = installerAntScript;
        _currentVersionId = version;
    }

    public String getName()
    {
        return _name;
    }

    public URL getInstallingAntScriptURL()
    {
        return _installingAntScript;
    }

    public void setInstallingAntScriptURL(URL antScript)
    {
        _installingAntScript = antScript;
        _currentVersionId++;
    }

    public long getVersionId()
    {
        return _currentVersionId;
    }
}
