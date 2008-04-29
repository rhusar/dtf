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
 * Copyright (C) 1998, 1999, 2000, 2001, 2002, 2003
 *
 * Arjuna Technologies Ltd.
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: DownloadArchive.java 170 2008-03-25 18:59:26Z jhalliday $
 */
package org.jboss.dtf.testframework.dtfweb;

import org.jboss.dtf.testframework.testnode.RunUID;
import org.jboss.dtf.testframework.dtfweb.utils.DateUtils;
import org.jboss.dtf.testframework.dtfweb.utils.DBUtils;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.UnavailableException;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.util.zip.ZipOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.ArrayList;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;

public class DownloadArchive extends HttpServlet
{
	private final static String NAME_ATTRIBUTE = "name";
	private final static String TEST_RESULT_ELEMENT_NAME = "test-result";
	private final static String DATE_TIME_STARTED_ELEMENT = "date-time-started";
	private final static String DATE_TIME_FINISHED_ELEMENT = "date-time-finished";
	private final static String PERMUTATION_CODE_ELEMENT = "permutation-code";
	private final static String OVERALL_RESULT_ELEMENT = "overall-result";
	private final static String TIMEDOUT_ATTRIBUTE = "timed-out";
	private final static String TASKS_ELEMENT = "task-results";
	private final static String INFORMATION_ELEMENT = "information";
	private final static String TASK_RESULT_ELEMENT = "task-result";
	private final static String TASK_NAME_ATTRIBUTE = "name";
	private final static String DATE_TIME_LOGGED_ELEMENT = "date-time-logged";
	private final static String RESULT_ELEMENT = "result";
	private final static String TASK_OUTPUT_ELEMENT = "task-output-info";
	private final static String TASK_OUTPUT_INFO_ELEMENT = "task-output";
	private final static String TASK_OUTPUT_TYPE_ATTRIBUTE = "type";

	private DTFResultsManager	_dtfResultsManager;

	/**
	 *
	 * Called by the servlet container to indicate to a servlet that the
	 * servlet is being placed into service.  See {@link Servlet#init}.
	 *
	 * <p>This implementation stores the {@link ServletConfig}
	 * object it receives from the servlet container for later use.
	 * When overriding this form of the method, call
	 * <code>super.init(config)</code>.
	 *
	 * @param config 			the <code>ServletConfig</code> object
	 *					that contains configutation
	 *					information for this servlet
	 *
	 * @exception ServletException 	if an exception occurs that
	 *					interrupts the servlet's normal
	 *					operation
	 *
	 *
	 * @see 				UnavailableException
	 *
	 */

	public void init(ServletConfig config) throws ServletException
	{
		super.init(config);

		_dtfResultsManager = new DTFResultsManager();
	}

	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException
	{
		try
		{
			int archiveId = Integer.parseInt( req.getParameter("archiveid") );
			ArchiveInformation archiveInfo = ArchiveInformation.getArchiveInformation(archiveId);
			if ( archiveInfo != null )
			{
				resp.setContentType("application/zip");
				ZipOutputStream out = new ZipOutputStream(resp.getOutputStream());

				ZipEntry indexEntry = new ZipEntry("index.xml");
				out.putNextEntry(indexEntry);

				createIndex( out, archiveInfo );

				/** Create entries for archived runs **/
				RunUID[] runIds = archiveInfo.getRunIds();

				for (int count=0;count<runIds.length;count++)
				{
					String directoryName = "run_"+runIds[count].getUID()+"/";
					ZipEntry directoryEntry = new ZipEntry(directoryName);
					out.putNextEntry(directoryEntry);
					generateResultsXML(runIds[count], out, directoryName);
				}

				out.closeEntry();
				out.finish();
				out.close();
			}
		}
		catch (Exception e)
		{
			e.printStackTrace(System.err);
			throw new ServletException("Exception: "+e);
		}
	}

	private void createIndex( ZipOutputStream out, ArchiveInformation archiveInfo ) throws Exception
	{
		Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
		Element rootElement = doc.createElement("test-results-archive");

		Element titleElement = doc.createElement("title");
		titleElement.appendChild( doc.createTextNode( archiveInfo.getTitle() ) );
		rootElement.appendChild(titleElement);

		Element commentsElement = doc.createElement("comments");
		commentsElement.appendChild( doc.createTextNode( archiveInfo.getComments() ) );
		rootElement.appendChild(commentsElement);

		Element dateTimeElement = doc.createElement("date-time-created");
		dateTimeElement.appendChild( doc.createTextNode( DateUtils.displayDate( archiveInfo.getDateTimeCreated() ) ) );
		rootElement.appendChild(dateTimeElement);

		Element testRunsElement = doc.createElement("test-runs");
		rootElement.appendChild(testRunsElement);

		RunUID[] runIds = archiveInfo.getRunIds();

		for (int count=0;count<runIds.length;count++)
		{
			Element testRunElement = doc.createElement("test-run");
			testRunElement.setAttribute("id", ""+runIds[count].getUID());

			Element testRunResultFilename = doc.createElement("filename");
			testRunResultFilename.appendChild( doc.createTextNode("run_"+runIds[count].getUID()+"/results.xml") );
			testRunElement.appendChild(testRunResultFilename);

			testRunsElement.appendChild(testRunElement);
		}

		/** Create formatted XML serializer **/
		OutputFormat outFmt = new OutputFormat(doc);
		outFmt.setIndent(4);
		outFmt.setIndenting(true);
		XMLSerializer serializer = new XMLSerializer( new OutputStreamWriter(out), outFmt );
		serializer.serialize(rootElement);
	}

	private void generateResultsXML(RunUID runId, ZipOutputStream out, String directoryName) throws Exception
	{
		Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
		Element rootElement = doc.createElement("test-results");

		TestResultInformation[] info = _dtfResultsManager.getResultsForTestRun(runId.getUID(),null);

		for (int count=0;count<info.length;count++)
		{
			rootElement.appendChild(createTestResultElement(out, doc, directoryName, info[count]));
		}

		ZipEntry resultsEntry = new ZipEntry(directoryName+"results.xml");
		out.putNextEntry(resultsEntry);

		/** Create formatted XML serializer **/
		OutputFormat outFmt = new OutputFormat(doc);
		outFmt.setIndent(4);
		outFmt.setIndenting(true);
		XMLSerializer serializer = new XMLSerializer( new OutputStreamWriter(out), outFmt );
		serializer.serialize(rootElement);
	}


	private Element createTestResultElement(ZipOutputStream out, Document doc, String directoryName, TestResultInformation info)
	{
		Element testResult = doc.createElement( TEST_RESULT_ELEMENT_NAME );
		testResult.setAttribute(NAME_ATTRIBUTE, info.testName);
		testResult.setAttribute(TIMEDOUT_ATTRIBUTE, info.hasTestTimedout() ? "yes" : "no");

		Element dateTimeStarted = doc.createElement( DATE_TIME_STARTED_ELEMENT );
		dateTimeStarted.appendChild( doc.createTextNode( DateUtils.displayDate(info.dateTimeStarted) ) );
		testResult.appendChild(dateTimeStarted);

		Element dateTimeFinished = doc.createElement( DATE_TIME_FINISHED_ELEMENT );
		dateTimeFinished.appendChild( doc.createTextNode( DateUtils.displayDate(info.dateTimeFinished) ) );
		testResult.appendChild(dateTimeFinished);

		Element permutationCode = doc.createElement( PERMUTATION_CODE_ELEMENT );
		permutationCode.appendChild( doc.createTextNode( info.permutationCode ) );
		testResult.appendChild(permutationCode);

		Element result = doc.createElement( OVERALL_RESULT_ELEMENT );
		result.appendChild( doc.createTextNode( info.overAllResult ) );
		testResult.appendChild(result);

		Element information = doc.createElement( INFORMATION_ELEMENT );
		information.appendChild( doc.createTextNode( info.information ) );
		testResult.appendChild(information);

		Element tasks = doc.createElement( TASKS_ELEMENT );
		TestTaskResultInformation[] taskInfo = _dtfResultsManager.getTestTaskResults(info.runId, info.testName, info.permutationCode);

		for (int count=0;count<taskInfo.length;count++)
		{
			tasks.appendChild( createTaskResultElement( out, doc, directoryName, info, taskInfo[count] ) );
		}

		testResult.appendChild(tasks);

		return testResult;
	}

	private Element createTaskResultElement( ZipOutputStream out, Document doc, String directoryName, TestResultInformation info, TestTaskResultInformation taskInfo )
	{
		Element taskResult = doc.createElement( TASK_RESULT_ELEMENT );
		taskResult.setAttribute( TASK_NAME_ATTRIBUTE, taskInfo.taskName );

		Element dateTimeStarted = doc.createElement( DATE_TIME_LOGGED_ELEMENT );
		dateTimeStarted.appendChild( doc.createTextNode( DateUtils.displayDate(taskInfo.timeLogged) ) );
		taskResult.appendChild(dateTimeStarted);

		Element permutationCode = doc.createElement( PERMUTATION_CODE_ELEMENT );
		permutationCode.appendChild( doc.createTextNode( taskInfo.taskPermutationCode ) );
		taskResult.appendChild(permutationCode);

		Element information = doc.createElement( INFORMATION_ELEMENT );
		information.appendChild( doc.createTextNode( taskInfo.information ) );
		taskResult.appendChild(information);

		Element result = doc.createElement( RESULT_ELEMENT );
		result.appendChild( doc.createTextNode( taskInfo.result ) );
		taskResult.appendChild(result);

		Element taskOutput = doc.createElement( TASK_OUTPUT_ELEMENT );
		taskResult.appendChild(taskOutput);

        Connection conn = null;

		try
		{
			directoryName = directoryName+info.testName+"/";

			try
			{
				ZipEntry dirEntry = new ZipEntry(directoryName);
				System.out.println("Creating "+directoryName);
				out.putNextEntry(dirEntry);
			}
			catch (ZipException e)
			{
				// Ignore
			}

			conn = DBUtils.getDataSource().getConnection();
			Statement s = conn.createStatement();

			ResultSet taskOutputs = s.executeQuery("SELECT * FROM TaskOutput WHERE RunId="+info.runId+" AND TestId='"+info.testName+"' AND TaskName='"+taskInfo.taskName+"' AND PermCode='"+info.permutationCode+"'");

			while (taskOutputs.next())
			{
				String typeName = taskOutputs.getString("OutputType");
				InputStream in = taskOutputs.getBinaryStream("ResultData");

				String filename = taskInfo.taskName + "." + typeName;
				Element taskOutputType = doc.createElement( TASK_OUTPUT_INFO_ELEMENT );
				taskOutputType.setAttribute( TASK_OUTPUT_TYPE_ATTRIBUTE, typeName );
				taskOutputType.appendChild( doc.createTextNode( directoryName + filename ) );
                taskOutput.appendChild(taskOutputType);

				ZipEntry outputEntry = new ZipEntry(directoryName+filename);
				out.putNextEntry(outputEntry);

				BufferedInputStream bin = new BufferedInputStream(in);
				byte[] buffer = new byte[32768];
				int readIn;

				while ( ( readIn = bin.read(buffer) ) != -1 )
				{
					out.write(buffer, 0, readIn);
				}

				bin.close();
			}

			taskOutputs.close();
			s.close();
		}
		catch (Exception e)
		{
			System.err.println("Error - while retrieving task output");
			e.printStackTrace(System.err);
		}
        finally
        {
            if ( conn != null )
            {
                try
                {
                    conn.close();
                }
                catch (java.sql.SQLException e)
                {
                    e.printStackTrace(System.err);
                }
            }
        }

		return taskResult;
	}
}
