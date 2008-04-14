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
<jsp:useBean id="dtfResultsManager" scope="session" class="org.jboss.dtf.testframework.dtfweb.DTFResultsManager" />
<%@ page import="java.util.ArrayList,
				 org.jboss.dtf.testframework.testnode.RunUID,
				 org.jboss.dtf.testframework.dtfweb.WebRunInformation"%>

<%
	boolean success = false;
	boolean attempted = false;

	ArrayList currentArchive = (ArrayList)session.getAttribute("currentarchive");

	if ( ( request.getParameter("created") != null ) && ( currentArchive != null ) )
	{

		String title = request.getParameter("title");
		String comments = request.getParameter("comments");

		attempted = true;
        success = dtfResultsManager.createArchive(title, comments, currentArchive);

		if ( success )
		{
			session.removeAttribute("currentarchive");
		}
	}

	if ( attempted )
	{
%>
	<table width="85%" align="center">
		<tr>
			<td>
				Archive created: <%=success ? "successfully" : "failure"%>
			</td>
		</tr>
	</table>

<%
	}
	else
	{
%>
<form action="default.jsp?page=createarchive" method="post">
	<input type="hidden" name="created" value="yes"/>

	<table width="85%" align="center">
		<tr>
			<td>
				Archive contents:
			</td>
		</tr>
		<tr>
			<td>
				<table width="100%">
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

					</tr>
	<%
		if ( currentArchive != null )
		{
			for (int count=0;count<currentArchive.size();count++)
			{
				RunUID runId = (RunUID)currentArchive.get(count);
				WebRunInformation runInfo = dtfResultsManager.getTestRunInformation(runId.getUID());
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
					</tr>
	<%
			}
		}
	%>
				</table>
			</td>
		</tr>
		<tr>
			<td>
				Title: <input type="text" name="title" size="64"/>
			</td>
		</tr>
		<tr>
			<td>
				Comments:
			</td>
		</tr>
		<tr>
			<td>
				<textarea rows="10" cols="60" name="comments"></textarea>
			</td>
		</tr>
		<tr>
			<td>
				<input type="submit" name="create" value="save"/>
			</td>
		</tr>
	</table>
</form>
<%
	}
%>