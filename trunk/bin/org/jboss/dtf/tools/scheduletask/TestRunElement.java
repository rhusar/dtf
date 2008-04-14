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
package org.jboss.dtf.tools.scheduletask;

/*
 * Copyright (C) 1998, 1999, 2000, 2001, 2002, 2003
 *
 * Arjuna Technology Ltd.
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: TestRunElement.java 170 2008-03-25 18:59:26Z jhalliday $
 */

public class TestRunElement
{
	private String	_testDefsUrl = null;
	private String	_testSelectionsUrl = null;
	private String	_softwareVersion = null;
	private String	_distributionList = "";

	public void setTestdefsurl(String testDefsUrl)
	{
		_testDefsUrl = testDefsUrl;
	}

	public void setTestselectionsurl(String testSelectionsUrl)
	{
		_testSelectionsUrl = testSelectionsUrl;
	}

	public void setDistributionlist(String distributionList)
	{
		_distributionList = distributionList;
	}

	public void setSoftwareversion(String softwareVersion)
	{
		_softwareVersion = softwareVersion;
	}

	public String getTestDefsUrl()
	{
		return _testDefsUrl;
	}

	public String getTestSelectionsUrl()
	{
		return _testSelectionsUrl;
	}

	public String getDistributionList()
	{
		return _distributionList;
	}

	public String getSoftwareVersion()
	{
		return _softwareVersion;
	}
}
