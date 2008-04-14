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
<%@ page import="org.jboss.dtf.testframework.dtfweb.DTFResultsLogger"%>
<%
	// Make sure we have been submitted an email list
	if ( request.getParameter("submitted") != null )
	{
		long runId = Long.parseLong( request.getParameter("runid") );
		String distList = request.getParameter("distlist");
        String comments = request.getParameter("comments");
		String softwareVersion = request.getParameter("softwareversion");

        DTFResultsLogger logger = new DTFResultsLogger();
		logger.emailResults(distList, runId, comments);

		response.sendRedirect("default.jsp?page=view_results&softwareversion=" + softwareVersion + "&runid=" + runId);
	}
%>
<form action="default.jsp?page=email_results" method="post">
<input type="hidden" name="submitted" value="true"/>
<input type="hidden" name="runid" value="<%=request.getParameter("runid")%>"/>
<input type="hidden" name="softwareversion" value="<%=request.getParameter("softwareversion")%>"/>
<table width="75%" align="center">
	<tr>
		<td>
			Please enter a ; delimited distribution list:<input type="text" name="distlist" size="32"/>
        </td>
    </tr>
    <tr>
        <td>
            Comments: <input type="text" name="comments" size="32"/>
            <input type="submit" value="send"/>
		</td>
	</tr>
</table>
</form>

