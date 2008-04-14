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
package org.jboss.dtf.testframework.utils.logging;

import org.jboss.dtf.testframework.testnode.RunUID;

/*
 * Copyright (C) 1998, 1999, 2000, 2001, 2002, 2003
 *
 * Arjuna Technologies Ltd.
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: TestResultLog.java 170 2008-03-25 18:59:26Z jhalliday $
 */

public class TestResultLog
{
	private String _result;
	private String _taskName;
	private String _testName;
	private RunUID _runUID;
	private String _taskPermutationCode;
	private String _testPermutationCode;

	public TestResultLog(String result,
						 String taskName,
						 String testName,
						 RunUID runUID,
						 String taskPermutationCode,
						 String testPermutationCode)
	{
		_result = result;
		_taskName = taskName;
		_testName = testName;
		_runUID = runUID;
		_taskPermutationCode = taskPermutationCode;
		_testPermutationCode = testPermutationCode;
	}

	public String getResult()
	{
		return _result;
	}

	public String getTaskName()
	{
		return _taskName;
	}

	public String getTestName()
	{
		return _testName;
	}

	public RunUID getRunUID()
	{
		return _runUID;
	}

	public String getTaskPermutationCode()
	{
		return _taskPermutationCode;
	}

	public String getTestPermutationCode()
	{
		return _testPermutationCode;
	}
}
