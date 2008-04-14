<%--
  JBoss, Home of Professional Open Source
  Copyright 2008, Red Hat Middleware LLC, and individual contributors
  as indicated by the @author tags.
  See the copyright.txt in the distribution for a
  full listing of individual contributors.
  This copyrighted material is made available to anyone wishing to use,
  modify, copy, or redistribute it subject to the terms and conditions
  of the GNU Lesser General Public License, v. 2.1.
  This program is distributed in the hope that it will be useful, but WITHOUT A
  WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
  PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
  You should have received a copy of the GNU Lesser General Public License,
  v.2.1 along with this distribution; if not, write to the Free Software
  Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
  MA  02110-1301, USA.

  (C) 2008,
  @author JBoss Inc.
--%>
<%@ page import="org.jboss.dtf.testframework.dtfweb.ArchiveInformation,
				 org.jboss.dtf.testframework.testnode.RunUID,
				 org.jboss.dtf.testframework.dtfweb.WebRunInformation"%>
<%
	int archiveId = Integer.parseInt( request.getParameter("archiveid") );

	ArchiveInformation archiveInfo = ArchiveInformation.getArchiveInformation(archiveId);

	if ( archiveInfo != null )
	{
%>
<table width="85%" align="center" cellpadding="5">
	<tr>
		<td>
			<b>Archived Results</b><br>
			[ <a href="servlet/org.jboss.dtf.testframework.dtfweb.DownloadArchive?archiveid=<%=archiveId%>">download bundle</a> ] [ <a href="servlet/org.jboss.dtf.testframework.dtfweb.DownloadReport?archiveid=<%=archiveId%>">PDF report</a> ]
		</td>
	</tr>
	<tr>
		<td>
			<font size="2">
 				<b>
 					<%=archiveInfo.getTitle()%>
				</b>
			</font>
		</td>
	</tr>
	<tr>
		<td>
			<%=archiveInfo.getComments()%>
		</td>
	</tr>
	<tr>
		<td>
			Archive Contents:
<%
	RunUID[] runIds = archiveInfo.getRunIds();
%>
				<table width="100%" cellpadding="3">

					<tr bgcolor="#939393">

						<td>

						  <font face="Tahoma" size="2">

							Date Started

						  </font>

						</td>

						<td>

						  <font face="Tahoma" size="2">

							Date Finished

						  </font>

						</td>

						<td>

						  <font face="Tahoma" size="2">

							Test Definitions

						  </font>

						</td>

						<td>

						  <font face="Tahoma" size="2">

							Test Selection

						  </font>

						</td>

						<td align="center">

						  <font face="Tahoma" size="2">

							Options

						  </font>

						</td>

					</tr>
	<%
		for (int count=0;count<runIds.length;count++)
		{
			WebRunInformation runInfo = dtfResultsManager.getTestRunInformation(runIds[count].getUID());
	%>
					<tr>
						<td width="15%">

						  <font face="Tahoma" size="1">

							<%=runInfo.dateTimeStarted%>

						  </font>

						</td>

						<td width="15%">

						  <font face="Tahoma" size="1">

							<%=org.jboss.dtf.testframework.dtfweb.utils.DateUtils.displayDate(runInfo.dateTimeFinished)%>

						  </font>

						</td>

						<td>

						  <font face="Tahoma" size="2">

							<a href="<%=runInfo.testDefinitionsURL%>"><%=runInfo.testDefinitionsDescription%></a>&nbsp;

						  </font>

						</td>

						<td>

						  <font face="Tahoma" size="2">

							<a href="<%=runInfo.testSelectionURL%>"><%=runInfo.testSelectionDescription%></a>&nbsp;

						  </font>

						</td>

						<td align="center">

							<a href="default.jsp?page=view_results&runid=<%=runInfo.runId%>">view results</a>

						</td>

					</tr>
	<%
		}
	%>
				</table>
			</td>
		</tr>
</table>
<%
	}
%>