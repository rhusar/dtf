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

package org.jboss.dtf.tools.initiatesoftwaredistribute;

import org.apache.tools.ant.Task;
import org.apache.tools.ant.BuildException;

public class InitiateSoftwareDistributeAntTask extends Task
{
    private String  _nameServiceURI = null;
    private String  _antScriptURL = null;
    private String  _productName = null;

    public void setNameservice(String uri)
    {
        _nameServiceURI = uri;
    }

    public void setInstallurl(String url)
    {
        _antScriptURL = url;
    }

    public void setProduct(String name)
    {
        _productName = name;
    }

    public void execute() throws BuildException
    {
        if ( _nameServiceURI == null )
        {
            throw new BuildException("The nameservice attribute was not specified (nameservice)");
        }

        if ( _antScriptURL == null )
        {
            throw new BuildException("The ANT install script was not specified (installurl)");
        }

        if ( _productName == null )
        {
            throw new BuildException("The product name was not specified (product)");
        }

        try
        {
			long versionId = InitiateSoftwareDistribute.initiate(_nameServiceURI, _productName, _antScriptURL);

			log("Software distribute successfull: version id. now "+versionId);
        }
        catch (Exception e)
        {
            throw new BuildException("Failed to initiate software distribution: "+e);
        }
    }
}
