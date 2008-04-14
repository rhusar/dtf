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
 * $Id: AuditLog.java 170 2008-03-25 18:59:26Z jhalliday $
 */
package org.jboss.dtf.testframework.coordinator2.auditlog;

import org.jboss.dtf.testframework.testnode.RunUID;

import java.io.PrintStream;
import java.io.FileOutputStream;
import java.io.File;

public class AuditLog
{
	private final static String	AUDIT_LOG_DIRECTORY = "org.jboss.dtf.testframework.coordinator.auditlog.directory";
	private final static String	AUDIT_LOG_DIRECTORY_DEFAULT = "./auditlog/";
	private final static String AUDIT_LOG_FILENAME_PREFIX = "auditlog-";
	private final static String AUDIT_LOG_FILENAME_SUFFIX = ".xml";

	private final static String XML_AUDIT_LOG = "node-audit-log";
	private final static String XML_TEST_LOG_ENTRY = "test-log-entry";
	private final static String XML_TASK_LOG_ENTRY = "task-log-entry";
	private final static String XML_TASK_LOG_NODE_ENTRY = "node-name";

	private RunUID			_runId;
	private PrintStream     _out;

	public AuditLog(RunUID runId) throws java.io.IOException
	{
		_runId = runId;

		File outDir = new File( System.getProperty(AUDIT_LOG_DIRECTORY, AUDIT_LOG_DIRECTORY_DEFAULT) );

		outDir.mkdirs();

		File outFile = new File( outDir, getAuditFilename(_runId) );

		_out = new PrintStream( new FileOutputStream( outFile ) );
		_out.println(getOpeningTag(XML_AUDIT_LOG, "run-id=\""+runId.getUID()+"\""));
	}

	public void addLog(AuditLogEntry entry) throws java.io.IOException
	{
		_out.println( "\t" + getOpeningTag(XML_TEST_LOG_ENTRY, "name=\""+entry.getTestName()+"\" permutation=\""+ entry.getPermutationCode() +"\" time-logged=\""+entry.getDateTimeLogged()+"\"") );

		NodeLogEntry[] nodes = entry.getNodeLog();

		for (int count=0;count<nodes.length;count++)
		{
			_out.println( "\t\t" + getOpeningTag( XML_TASK_LOG_ENTRY, "name=\""+nodes[count].getTaskName()+"\" permutation=\""+ nodes[count].getTaskPermutationCode() +"\" time-logger=\""+nodes[count].getDateTimeLogger()+"\"" ) );

			_out.println( "\t\t\t" + getOpeningTag( XML_TASK_LOG_NODE_ENTRY, null ) );
			_out.println( "\t\t\t\t" + nodes[count].getNodeName() );
			_out.println( "\t\t\t" + getClosingTag( XML_TASK_LOG_NODE_ENTRY ) );

			_out.println( "\t\t" + getClosingTag( XML_TASK_LOG_ENTRY ) );
		}

		_out.println( "\t" + getClosingTag(XML_TEST_LOG_ENTRY) );
	}

	public void close() throws java.io.IOException
	{
		_out.println( getClosingTag(XML_AUDIT_LOG) );
		_out.close();
	}

	private static String getOpeningTag(String text, String attributes)
	{
		return "<" + text + ( attributes != null ? " " + attributes : "" ) + ">";
	}

	private static String getClosingTag(String text)
	{
		return "</" + text + ">";
	}

	public static final String getAuditFilename(RunUID runId)
	{
		return AUDIT_LOG_FILENAME_PREFIX + runId.getUID() + AUDIT_LOG_FILENAME_SUFFIX;
	}

	public static AuditLogEntry createLogEntry(String testName, String permutationCode)
	{
		return new AuditLogEntry(testName, permutationCode);
	}
}
