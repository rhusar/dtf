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
<%@ page import="org.jboss.dtf.testframework.dtfweb.DTFResultsManager,
				 org.jboss.dtf.testframework.dtfweb.WebRunInformation,
				 org.jboss.dtf.testframework.dtfweb.DTFResultsLogger"%>
<table width="75%" align="center" border="0" cellspacing="0" cellpadding="5">
	<tr>
		<td align="center">
			<font face="Tahoma" size="2">
				Last 5 test runs performed
			</font>

			<table width="100%" border="0" cellspacing="0" cellpadding="5">
				<tr bgcolor="#939393">
					<td>
						Software Version
					</td>
					<td>
						Date/Time Started
					</td>
					<td>
						Date/Time Finished
					</td>
					<td>
						Test Definitions
					</td>
					<td>
						Test Selections
					</td>
					<td>
						Options
					</td>
				</tr>
<%
	long runId = DTFResultsLogger.getCurrentRunId();

	long start = ( runId - 5 ) < 0 ? 0 : ( runId - 5 );

	for (long count=runId-1;count>=start;count--)
	{
		WebRunInformation runInfo = dtfResultsManager.getTestRunInformation(count);
%>
				<tr>
					<td>
						<a href="default.jsp?page=runs&softwareversion=<%=runInfo.softwareVersion%>"><%=runInfo.softwareVersion%></a>
					</td>
					<td>
						<%=runInfo.dateTimeStarted%>
					</td>
					<td>
						<%=runInfo.dateTimeFinished%>
					</td>
					<td>
						<a href="<%=runInfo.testDefinitionsURL%>"><%=runInfo.testDefinitionsDescription%></a>
					</td>
					<td>
						<a href="<%=runInfo.testSelectionURL%>"><%=runInfo.testSelectionDescription%></a>
					</td>
					<td>
						<a href="default.jsp?page=view_results&softwareversion=<%=runInfo.softwareVersion%>&runid=<%=count%>">view</a>
					</td>
				</tr>
<%
	}
%>
			</table>
		</td>
	</tr>
</table>
