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
<jsp:useBean id="jdbcConnectionPool" scope="application" class="org.jboss.dtf.testframework.dtfweb.utils.JDBCConnectionPool" />
<table width="75%" align="center" bordercolor="black" border="1" cellspacing="0" cellpadding="5">
  <tr>
    <td>
<%
	long runId = Long.parseLong(request.getParameter("runid"));

	org.jboss.dtf.testframework.dtfweb.WebRunInformation runInf = dtfResultsManager.getTestRunInformation(runId);

	if ( (runInf.information != null) && (runInf.information.length() > 0) )
	{
%>
		<%=runInf.information%>
<%
	}
%>
    </td>
  </tr>
</table>
<p align="center">
  <a href="default.jsp?page=view_results&runid=<%=runId%>">
    <font face="Tahoma" size="2">
      back
    </font>
  </a>
</p>