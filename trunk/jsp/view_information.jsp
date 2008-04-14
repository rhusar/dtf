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
<%@ page import="java.util.ArrayList,
                 java.net.URLEncoder,
                 org.jboss.dtf.testframework.dtfweb.performance.DTFPerformanceResultManager"%>
<jsp:useBean id="dtfResultsManager" scope="session" class="org.jboss.dtf.testframework.dtfweb.DTFResultsManager" />
<jsp:useBean id="jdbcConnectionPool" scope="application" class="org.jboss.dtf.testframework.dtfweb.utils.JDBCConnectionPool" />
<br>
<%
	long runId = Long.parseLong(request.getParameter("runid"));
	String testName = request.getParameter("testname");
	String taskName = request.getParameter("taskname");
	String permutationCode = request.getParameter("permutationCode");

	org.jboss.dtf.testframework.dtfweb.WebRunInformation runInf = dtfResultsManager.getTestRunInformation(runId);

	if (runInf != null)
	{
%>
<table width="75%" align="center">
  <tr>
    <td width="40%">
      <font face="Tahoma" size="3">
	    <b>Test Run Information<b>
	  </font>
    </td>
    <td>
      &nbsp;
    </td>
    <td>
      &nbsp;
    </td>
  </tr>
  <tr>
    <td>
	  <font face="Tahoma" size="2">
		<b>Start Time:</b> <%=runInf.dateTimeStarted%>
	  </font>
	</td>
	<td>
	  <font face="Tahoma" size="2">
		<b>Finish Time:</b> <%=org.jboss.dtf.testframework.dtfweb.utils.DateUtils.displayDate(runInf.dateTimeFinished)%>
      </font>
    </td>
    <td>
      &nbsp;
    </td>
  </tr>
</table>
<hr width="75%">

<%
  }

  org.jboss.dtf.testframework.dtfweb.TestResultInformation testResultInf = dtfResultsManager.getTestResult(runId, testName, permutationCode);

  if (testResultInf != null)
  {
%>
<table width="75%" align="center">
  <tr>
    <td width="40%">
      <font face="Tahoma" size="3">
	    <b>Test Information<b>
	  </font>
    </td>
    <td>
      &nbsp;
    </td>
    <td>
      &nbsp;
    </td>
  </tr>
  <tr>
    <td>
	  <font face="Tahoma" size="2">
		<b>Start Time:</b> <%=testResultInf.dateTimeStarted%>
	  </font>
	</td>
	<td>
	  <font face="Tahoma" size="2">
		<b>Finish Time:</b> <%=org.jboss.dtf.testframework.dtfweb.utils.DateUtils.displayDate(testResultInf.dateTimeFinished)%>
      </font>
    </td>
    <td>
      &nbsp;
    </td>
  </tr>
  <tr>
    <td width="40%">
      <font face="Tahoma" size="2">
        <b>Test Name:</b> <%=testResultInf.testName%>
      </font>
    </td>
    <td>
      <font face="Tahoma" size="2">
        <b>OS/Product:</b> <%=dtfResultsManager.getOSProductCombination(testResultInf.permutationCode,"|")%>
      </font>
    </td>
    <td>
      &nbsp;
    </td>
  </tr>
</table>
<hr width="75%">
<%
  }

  org.jboss.dtf.testframework.dtfweb.TestTaskResultInformation testTaskResultInf = dtfResultsManager.getTestTaskResult(runId, testName, taskName, permutationCode);

  if (testTaskResultInf != null)
  {
%>
<table width="75%" align="center">
  <tr>
    <td>
      <font face="Tahoma" size="3">
        <b>Task Information<b>
      </font>
    </td>
    <td>
      &nbsp;
    </td>
    <td>
      &nbsp;
    </td>
  </tr>
  <tr>
    <td width="40%">
      <font face="Tahoma" size="2">
        <b>Task Name:</b> <%=testTaskResultInf.taskName%>
      </font>
    </td>
    <td>
      <font face="Tahoma" size="2">
        <b>Task OS/Product:</b> <%=dtfResultsManager.getOSProductCombination(testTaskResultInf.taskPermutationCode,"|")%>
      </font>
    </td>
    <td>
      <font face="Tahoma" size="2">
        <b>Result:</b> <%=testTaskResultInf.result%>
      </font>
    </td>
  </tr>
</table>
<br>
<table width="75%" align="center">
  <tr>
    <td>
      <font face="Tahoma" size="2">
        Task output:
<%
      ArrayList outputTypes = dtfResultsManager.getTestTaskOutputTypes(runId, testName, taskName, permutationCode);

      for (int count=0;count<outputTypes.size();count++)
      {
%>
  <a href="servlet/org.jboss.dtf.testframework.dtfweb.TaskOutputServlet?page=view_taskoutput&runid=<%=runId%>&testname=<%=URLEncoder.encode(testName)%>&permutationCode=<%=URLEncoder.encode(permutationCode)%>&taskname=<%=URLEncoder.encode(taskName)%>&type=<%=URLEncoder.encode((String)outputTypes.get(count))%>">
    <font face="Tahoma" size="2">
        <%=(String)outputTypes.get(count)%>
    </font>
  </a> &nbsp;
<%
      }
%>
<%
        long performanceRunId = DTFPerformanceResultManager.getPerformanceRunId(runId,testName,taskName,permutationCode);

        if ( performanceRunId != DTFPerformanceResultManager.NONE)
        {
%>
        <a href="default.jsp?page=view_data&perfrunid=<%=performanceRunId%>">data</a>
<%
        }
%>
      </font>
    </td>
  </tr>
</table>
<table cellpadding="5" width="75%" align="center" border="1" cellspacing="0" bordercolor="black">
  <tr>
    <td>
      <font face="Courier New" size="2">
        <%=testTaskResultInf.information%>
      </font>
    </td>
  </tr>
</table>
<%
  }

    long performanceRunId = DTFPerformanceResultManager.getPerformanceRunId(runId,testName,taskName,permutationCode);

    if ( performanceRunId != DTFPerformanceResultManager.NONE)
    {
%>
<br>
<table cellpadding="5" width="75%" align="center" border="1" cellspacing="0" bordercolor="black" bgcolor="white">
  <tr>
    <td align="center">
      <img src="servlet/org.jboss.dtf.testframework.dtfweb.performance.GraphDataServlet?perfrunid=<%=performanceRunId%>&width=400&height=300">
    </td>
  </tr>
</table>
<%
    }
%>
<p align="center">
  <font face="Tahoma" size="2" color="Red">
    <a href="default.jsp?page=view_details&runid=<%=runId%>&testname=<%=testName%>&permutationCode=<%=permutationCode%>">back</a>
  </font>
</p>