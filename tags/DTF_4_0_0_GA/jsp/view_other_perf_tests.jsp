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
<%@ page import="org.jboss.dtf.testframework.dtfweb.performance.UserDefinedGraphInformation,
                 java.util.ArrayList,
                 org.jboss.dtf.testframework.dtfweb.utils.DateUtils,
                 java.net.URLEncoder,
                 org.jboss.dtf.testframework.dtfweb.performance.DTFPerformanceResultManager,
                 org.jboss.dtf.testframework.dtfweb.performance.DTFPerformanceRunInformation"%>

<table align="center" width="75%" border="1" cellspacing="0" cellpadding="5" bordercolor="gray">

    <tr bgcolor="#A0A0FF">
        <td>
            <font face="Tahoma" size="2">
                Test Run Id.
            </font>
        </td>
        <td>
            <font face="Tahoma" size="2">
                Test Name
            </font>
        </td>
        <td>
            <font face="Tahoma" size="2">
                Task Name
            </font>
        </td>
        <td>
            <font face="Tahoma" size="2">
                Permutation
            </font>
        </td>
        <td>
            <font face="Tahoma" size="2">
                Options
            </font>
        </td>
    </tr>
<%
    long perfRunIds[] = DTFPerformanceResultManager.getPerformanceDataWithName(request.getParameter("data_name"));

    for (int count=0;count<perfRunIds.length;count++)
    {
        DTFPerformanceRunInformation runInfo = DTFPerformanceRunInformation.getRunInformation(perfRunIds[count]);
%>
    <tr>
        <td>
            <font face="Tahoma" size="2">
                <%=runInfo.getTestRunId()%>
            </font>
        </td>
        <td>
            <font face="Tahoma" size="2">
                <%=runInfo.getTestName()%>
            </font>
        </td>
        <td>
            <font face="Tahoma" size="2">
                <%=runInfo.getTaskName()%>
            </font>
        </td>
        <td>
            <font face="Tahoma" size="2">
                <%=runInfo.getPermutationCode()%>
            </font>
        </td>
        <td>
            <font face="Tahoma" size="2">
                <a href="default.jsp?page=view_data&perfrunid=<%=runInfo.getPerformanceRunId()%>">view</a>
            </font>
        </td>
    </tr>
<%
    }
%>
</table>