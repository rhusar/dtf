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
 * Copyright (C) 2001, 2002,
 *
 * Hewlett-Packard Arjuna Labs,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: InitiateTestRunAntTask.java 170 2008-03-25 18:59:26Z jhalliday $
 */
package org.jboss.dtf.testframework.anttasks;

import org.apache.tools.ant.Task;
import org.apache.tools.ant.BuildException;
import org.jboss.dtf.testframework.coordinator.InitiateTestRun;

public final class InitiateTestRunAntTask extends Task
{
    protected String    _nameServiceURI = null;
    protected String    _testDefsURL = null;
    protected String    _selectionURL = null;
    protected String    _softwareVersion = null;
    protected String    _distributionList = "";
    protected boolean   _waitTillComplete = false;
	protected boolean	_runWhenPossible = false;

    public void setNameserviceuri(String nameServiceURI)
    {
        _nameServiceURI = nameServiceURI;
    }

    public void setTestdefsurl(String testDefsURL)
    {
        _testDefsURL = testDefsURL;
    }

    public void setSelectionurl(String selectionsURL)
    {
        _selectionURL = selectionsURL;
    }

    public void setSoftwareversion(String softwareVersion)
    {
        _softwareVersion = softwareVersion;
    }

    public void setDistributionlist(String distributionList)
    {
        _distributionList = distributionList;
    }

    public void setWait(String value)
    {
        _waitTillComplete = Boolean.valueOf(value).booleanValue();
    }

	public void setRunwhenpossible(String value)
	{
		_runWhenPossible = Boolean.valueOf(value).booleanValue();
	}

    public void execute() throws BuildException
    {
        if ( _nameServiceURI == null )
        {
            throw new BuildException("Please ensure the name service URI is specified");
        }

        if ( _testDefsURL == null )
        {
            throw new BuildException("Please ensure the test definitions URL is specified");
        }

        if ( _selectionURL == null )
        {
            throw new BuildException("Please ensure the test selections URL is specified");
        }

        if ( _softwareVersion == null )
        {
            throw new BuildException("Please ensure the software version is specified");
        }

        InitiateTestRun.initiate( _nameServiceURI, _testDefsURL, _selectionURL, _softwareVersion, _distributionList, _waitTillComplete, _runWhenPossible );
    }
}
