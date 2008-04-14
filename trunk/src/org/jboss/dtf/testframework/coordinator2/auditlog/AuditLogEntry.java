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
 * $Id: AuditLogEntry.java 170 2008-03-25 18:59:26Z jhalliday $
 */
package org.jboss.dtf.testframework.coordinator2.auditlog;

import java.util.ArrayList;
import java.util.Date;

public class AuditLogEntry
{
	private String		_testName;
	private String 		_permutationCode;
    private ArrayList   _nodes;
	private Date		_dateTimeLogged;

	public AuditLogEntry(String	testName,
				  		 String	permutationCode)
	{
		_testName = testName;
		_permutationCode = permutationCode;

		_dateTimeLogged = new Date();

		_nodes = new ArrayList();
	}

	public String getTestName()
	{
		return _testName;
	}

	public String getPermutationCode()
	{
		return _permutationCode;
	}

	public Date getDateTimeLogged()
	{
		return _dateTimeLogged;
	}

	public void addNodeLogEntry(NodeLogEntry	nodeLog)
	{
		_nodes.add(nodeLog);
	}

	public NodeLogEntry[] getNodeLog()
	{
		NodeLogEntry[] results = new NodeLogEntry[_nodes.size()];
		_nodes.toArray(results);
		return results;
	}

	public static NodeLogEntry createNodeEntry(String nodeName, String taskName, String taskPermutationCode)
	{
		return new NodeLogEntry(nodeName, taskName, taskPermutationCode);
	}
}
