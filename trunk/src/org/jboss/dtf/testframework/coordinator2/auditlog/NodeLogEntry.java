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
 * $Id: NodeLogEntry.java 170 2008-03-25 18:59:26Z jhalliday $
 */
package org.jboss.dtf.testframework.coordinator2.auditlog;

import java.util.Date;

public class NodeLogEntry
{
	private String	_nodeName;
	private String	_taskName;
	private String	_taskPermutationCode;
	private Date	_dateTimeLogged;

	public NodeLogEntry(	String	nodeName,
					String	taskName,
					String	taskPermutationCode )
	{
		_nodeName = nodeName;
		_taskName = taskName;
		_taskPermutationCode = taskPermutationCode;

		_dateTimeLogged = new Date();
	}

	public Date getDateTimeLogger()
	{
		return _dateTimeLogged;
	}

	public String getNodeName()
	{
		return _nodeName;
	}

	public String getTaskName()
	{
		return _taskName;
	}

	public String getTaskPermutationCode()
	{
		return _taskPermutationCode;
	}
}
