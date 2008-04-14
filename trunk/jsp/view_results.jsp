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
<%@ page import="org.jboss.dtf.testframework.dtfweb.WebRunInformation,
                 java.net.URLEncoder"%>
<jsp:useBean id="dtfResultsManager" scope="session" class="org.jboss.dtf.testframework.dtfweb.DTFResultsManager" />
<jsp:useBean id="dtfResultsLogger" class="org.jboss.dtf.testframework.dtfweb.DTFResultsLogger" />
<jsp:useBean id="jdbcConnectionPool" scope="application" class="org.jboss.dtf.testframework.dtfweb.utils.JDBCConnectionPool" />

<br/>

<%
	// Ensure OS and Product tables are in memory
	dtfResultsManager.setupOSProductTable();

	long runId = Long.parseLong(request.getParameter("runid"));
	boolean emailVersion = request.getParameter("emailversion")!=null;
	String comments = request.getParameter("comments");

	int 	numberOfTests,
			numberOfPasses,
			numberOfFails,
			numberOfTimeouts;
	String	testColor;
	String  orderBy = request.getParameter("orderBy");
	String  rootURL = dtfResultsLogger.getRootURL();

	numberOfTests = 0;
	numberOfPasses = 0;
	numberOfFails = 0;
	numberOfTimeouts = 0;

	if (orderBy == null)
	{
		// Nothing currently set for order by preference
		// set as default

		orderBy = "DateTimeStarted";
	}

	WebRunInformation runInf = dtfResultsManager.getTestRunInformation(runId);

	org.jboss.dtf.testframework.dtfweb.TestResultInformation[] results = dtfResultsManager.getResultsForTestRun(runId, orderBy);

	for (int count=0;count<results.length;count++)
	{
	  numberOfTests++;

	  if (results[count].hasTestTimedout())
	  {
		  numberOfTimeouts++;
	  }
      else
	  if (results[count].hasTestPassed())
	  {
		numberOfPasses++;
	  }
	  else
	  {
		if (results[count].hasTestFailed())
		{
		  numberOfFails++;
		}
	  }
	}
%>

<p align="center">

  <font face="Tahoma" size="2">

    Software Version: <b><%=runInf.softwareVersion%></b> Run Id: <b><%=runId%></b>

  </font>

</p>

<table bgcolor="#939393" width="75%" align="center" cellspacing="0" cellpadding="5" border="0">
  <tr>
    <td>
      <table width="100%" cellpadding="0">
	    <tr>
	      <td width="25%">
	        <font face="Tahoma" size="2" color="Black">
	          <b>Number of tests:</b> <%=numberOfTests%>
  		    </font>
		  </td>
	      <td width="25%">
	        <font face="Tahoma" size="2" color="Black">
	          <b>Passes:</b> <%=numberOfPasses%> <% if (numberOfTests>0) { %>(<%=((float)numberOfPasses/numberOfTests)*100%>%)<% } %>
		    </font>
		  </td>
	      <td width="25%">
	        <font face="Tahoma" size="2" color="Black">
	          <b>Fails:</b> <%=numberOfFails%> <% if (numberOfTests>0) { %>(<%=((float)numberOfFails/numberOfTests)*100%>%)<% } %>
		    </font>
		  </td>
	      <td width="25%">
	        <font face="Tahoma" size="2" color="Black">
	          <b>Timeouts:</b> <%=numberOfTimeouts%> <% if (numberOfTests>0) { %>(<%=((float)numberOfTimeouts/numberOfTests)*100%>%)<% } %>
		    </font>
		  </td>
	    </tr>
	  </table>
	</td>
  </tr>
</table>

<%
    if (comments != null)
    {
%>
<p align="center">

    <font face="Tahoma" size="2">

        <%=comments%>

    </font>

</p>
<%
    }
%>

<table align="center" width="75%" border="0" cellspacing="0" cellpadding="5">

  <tr bgcolor="#B3B3B3">

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


  <tr>

    <td width="15%">

      <font face="Tahoma" size="1">

        <%=runInf.dateTimeStarted%>

      </font>

    </td>

    <td width="15%">

      <font face="Tahoma" size="1">

        <%=org.jboss.dtf.testframework.dtfweb.utils.DateUtils.displayDate(runInf.dateTimeFinished)%>

      </font>

    </td>

    <td>

      <font face="Tahoma" size="2">

        <a href="<%=runInf.testDefinitionsURL%>"><%=runInf.testDefinitionsDescription%></a>&nbsp;

      </font>

    </td>

    <td>

      <font face="Tahoma" size="2">

        <a href="<%=runInf.testSelectionURL%>"><%=runInf.testSelectionDescription%></a>&nbsp;

      </font>

    </td>

  </tr>

</table>


<table width="75%" align="center" border="0" cellspacing="0" cellpadding="5">
  <tr>
    <td>
<%
	if ( (runInf.information != null) && (runInf.information.length() > 0) )
	{
%>
		<a href="<%=rootURL%>/default.jsp?page=view_testrun_information&runid=<%=runId%>">
		  <font face="Tahoma" color="black" size="2">
		    run info
		  </font>
		</a>
<%
	}
%>
    </td>
  </tr>
</table>
<table align="center" width="75%" border="0" cellspacing="3" cellpadding="5">
  <tr bgcolor="#B3B3B3">
    <td width="25%">
      <font face="Tahoma" size="1">
        <%
          if (!orderBy.equals("DateTimeStarted"))
          {
        %>
        <a href="<%=rootURL%>/default.jsp?runid=<%=request.getParameter("runid")%>&orderBy=DateTimeStarted&page=view_results">
          Date/Time Started
        </a>
        <%
          }
          else
          {
        %>
          Date/Time Started
        <%
          }
        %>
      </font>
    </td>
    <td width="25%">
      <font face="Tahoma" size="1">
        <%
          if (!orderBy.equals("DateTimeFinished"))
          {
        %>
        <a href="<%=rootURL%>/default.jsp?runid=<%=request.getParameter("runid")%>&orderBy=DateTimeFinished&page=view_results">
          Date/Time Finished
        </a>
        <%
          }
          else
          {
        %>
          Date/Time Finished
        <%
          }
        %>
      </font>
    </td>
    <td width="45%">
      <font face="Tahoma" size="1">
        <%
          if (!orderBy.equals("TestName"))
          {
        %>
        <a href="<%=rootURL%>/default.jsp?runid=<%=request.getParameter("runid")%>&orderBy=TestName&page=view_results">
          Test Name
        </a>
        <%
          }
          else
          {
        %>
          Test Name
        <%
          }
        %>
      </font>
    </td>
    <td width="20%">
      <font face="Tahoma" size="1">
        <%
          if (!orderBy.equals("PermutationCode"))
          {
        %>
        <a href="<%=rootURL%>/default.jsp?runid=<%=request.getParameter("runid")%>&orderBy=PermutationCode&page=view_results">
          Permutation Code
        </a>
        <%
          }
          else
          {
        %>
          Permutation Code
        <%
          }
        %>
      </font>
    </td>
    <td width="10%">
      <font face="Tahoma" size="1">
        <a href="<%=rootURL%>/default.jsp?runid=<%=request.getParameter("runid")%>&orderBy=OverallResult&page=view_results">
          Results
        </a>
      </font>
    </td>
  </tr>
<%
   for (int count=0;count<results.length;count++)
   {
     testColor = results[count].getColor();
%>
  <tr>
    <td>
      <font face="Tahoma" size="1">
        <%=results[count].dateTimeStarted.toString()%>
      </font>
    </td>
    <td>
      <font face="Tahoma" size="1">
        <%=org.jboss.dtf.testframework.dtfweb.utils.DateUtils.displayDate(results[count].dateTimeFinished)%>
      </font>
    </td>
    <td>
      <font face="Tahoma" size="1">
        <a href="<%=rootURL%>/default.jsp?page=view_details&testname=<%=URLEncoder.encode(results[count].testName)%>&runid=<%=results[count].runId%>&permutationCode=<%=URLEncoder.encode(results[count].permutationCode)%>"><%=results[count].testName%></a>
      </font>
    </td>
    <td>
      <font face="Tahoma" size="1">
        <%=dtfResultsManager.getOSProductCombination(results[count].permutationCode,"<br>")%>
      </font>
    </td>
    <td>
      <font face="Tahoma" size="1" color="<%=testColor%>">
        <%=results[count].overAllResult%>
      </font>
    </td>
  </tr>
<%
  }
%>
</table>

<table bgcolor="#939393" width="75%" align="center" cellspacing="0" cellpadding="5" border="0">
  <tr>
    <td>
      <table width="100%" cellpadding="0"
	    <tr>
	      <td width="25%">
	        <font face="Tahoma" size="2" color="Black">
	          <b>Number of tests:</b> <%=numberOfTests%>
  		    </font>
		  </td>
	      <td width="25%">
	        <font face="Tahoma" size="2" color="Black">
	          <b>Passes:</b> <%=numberOfPasses%> <% if (numberOfTests>0) { %>(<%=((float)numberOfPasses/numberOfTests)*100%>%)<% } %>
		    </font>
		  </td>
	      <td width="25%">
	        <font face="Tahoma" size="2" color="Black">
	          <b>Fails:</b> <%=numberOfFails%> <% if (numberOfTests>0) { %>(<%=((float)numberOfFails/numberOfTests)*100%>%)<% } %>
		    </font>
		  </td>
	      <td width="25%">
	        <font face="Tahoma" size="2" color="Black">
	          <b>Timeouts:</b> <%=numberOfTimeouts%> <% if (numberOfTests>0) { %>(<%=((float)numberOfTimeouts/numberOfTests)*100%>%)<% } %>
		    </font>
		  </td>
	    </tr>
	  </table>
	</td>
  </tr>
</table>
<%
	if (!emailVersion)
	{
%>
<p align="center">
  <font face="Tahoma" size="2" color="Red">
    <a href="<%=rootURL%>/default.jsp?page=runs&softwareversion=<%=URLEncoder.encode(runInf.softwareVersion)%>">back</a>
  </font>
</p>
<%
	}
%>