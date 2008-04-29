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
<%@ page import="java.net.URLEncoder"%>
<jsp:useBean id="dtfResultsManager" scope="session" class="org.jboss.dtf.testframework.dtfweb.DTFResultsManager" />
<jsp:useBean id="jdbcConnectionPool" scope="application" class="org.jboss.dtf.testframework.dtfweb.utils.JDBCConnectionPool" />
<br>
<%
	int numberOfTasks = 0,
	  	numberOfPasses = 0,
	  	numberOfFails = 0;
%>

<table align="center" width="75%" border="0" cellspacing="0" cellpadding="5">
  <tr bgcolor="#939393">
    <td width="25%">
      <font face="Tahoma" size="2">
        Date/Time Started
      </font>
    </td>
    <td width="25%">
      <font face="Tahoma" size="2">
        Date/Time Finished
      </font>
    </td>
    <td width="45%">
      <font face="Tahoma" size="2">
        Test Name
      </font>
    </td>
    <td width="10%">
      <font face="Tahoma" size="2">
        Result
      </font>
    </td>
  </tr>
<%
	long 	runId = Long.parseLong(request.getParameter("runid"));
	String 	testName = request.getParameter("testname");
	String	permutationCode = request.getParameter("permutationCode");
	String  testColor = "White";

	org.jboss.dtf.testframework.dtfweb.TestResultInformation testResultInf = dtfResultsManager.getTestResult(runId, testName, permutationCode);

	if (testResultInf != null)
	{
		if (testResultInf.hasTestPassed())
			testColor = "#00C000";
		else
		{
			if (testResultInf.hasTestFailed())
				testColor = "#C00000";
		}
%>
  <tr>
    <td>
      <font face="Tahoma" size="2">
        <%=testResultInf.dateTimeStarted%>
      </font>
    </td>
    <td>
      <font face="Tahoma" size="2">
        <%=org.jboss.dtf.testframework.dtfweb.utils.DateUtils.displayDate(testResultInf.dateTimeFinished)%>
      </font>
    </td>
    <td>
      <font face="Tahoma" size="2">
        <%=testResultInf.testName%>
      </font>
    </td>
    <td>
      <font face="Tahoma" size="2" color="<%=testColor%>">
        <%=testResultInf.overAllResult%>
      </font>
    </td>
  </tr>
<%
	}
%>
</table>

<br>
<table align="center" width="75%" border="0" cellspacing="0" cellpadding="5">
  <tr bgcolor="#939393">
    <td width="25%">
      <font face="Tahoma" size="2">
        Date/Time Logged
      </font>
    </td>
    <td width="45%">
      <font face="Tahoma" size="2">
        Task Name
      </font>
    </td>
    <td>
      <font face="Tahoma" size="2">
        OS/Product Combination
      </font>
    </td>
    <td width="10%">
      <font face="Tahoma" size="2">
        Result
      </font>
    </td>
  </tr>
<%
	org.jboss.dtf.testframework.dtfweb.TestTaskResultInformation[] results = dtfResultsManager.getTestTaskResults(runId,testName,permutationCode);

	for (int resultCount=0;resultCount<results.length;resultCount++)
	{
		if (results[resultCount].hasTestTaskPassed())
		{
			numberOfPasses++;
			testColor = "#00C000";
		}
		else
		{
			if (results[resultCount].hasTestTaskFailed())
			{
				numberOfFails++;
				testColor = "#C00000";
			}
			else
			{
				testColor = "White";
			}
		}
%>
  <tr>
    <td>
      <font face="Tahoma" size="2">
        <%=results[resultCount].timeLogged%>
      </font>
    </td>
    <td>
      <font face="Tahoma" size="2">
        <a href="default.jsp?page=view_information&testname=<%=URLEncoder.encode(testName)%>&runid=<%=runId%>&permutationCode=<%=URLEncoder.encode(permutationCode)%>&taskname=<%=URLEncoder.encode(results[resultCount].taskName)%>"><%=results[resultCount].taskName%></a>
      </font>
    </td>
    <td>
      <font face="Tahoma" size="2">
        <%=dtfResultsManager.getOSProductCombination(results[resultCount].taskPermutationCode,"|")%>
      </font>
    </td>
    <td>
      <font face="Tahoma" size="2" color="<%=testColor%>">
        <%=results[resultCount].result%>
      </font>
    </td>
  </tr>
<%
	}
%>
</table>

<table width="75%" align="center" cellspacing="0" cellpadding="0" border="0">
  <tr>
    <td>
      <table width="100%" cellpadding="0" bgcolor="#939393">
	    <tr>
	      <td width="33%">
	        <font face="Tahoma" size="2" color="Black">
	          <b>Number of tasks:</b> <%=numberOfTasks%>
  		    </font>
		  </td>
	      <td width="33%">
	        <font face="Tahoma" size="2" color="Black">
	          <b>Passes:</b> <%=numberOfPasses%> <% if (numberOfTasks>0) { %>(<%=((float)numberOfPasses/numberOfTasks)*100%>%)<% } %>
		    </font>
		  </td>
	      <td width="34%">
	        <font face="Tahoma" size="2" color="Black">
	          <b>Fails:</b> <%=numberOfFails%> <% if (numberOfTasks>0) { %>(<%=((float)numberOfFails/numberOfTasks)*100%>%)<% } %>
		    </font>
		  </td>
	    </tr>
	  </table>
	</td>
  </tr>
</table>
<br>
<table align="center" width="75%" border="0" cellpadding="0" cellspacing="0">
  <tr>
    <td>
      <font face="Tahoma" size="2">
        <%=testResultInf.information%>
      </font>
    </td>
  </tr>
</table>
<p align="center">
  <font face="Tahoma" size="2" color="Red">
    <a href="default.jsp?page=view_results&runid=<%=runId%>">back</a>
  </font>
</p>