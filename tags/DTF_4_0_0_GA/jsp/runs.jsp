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
                 java.net.URLEncoder,
                 java.util.ArrayList"%>
<jsp:useBean id="dtfResultsManager" scope="session" class="org.jboss.dtf.testframework.dtfweb.DTFResultsManager" />

<br/>
<%
    String softwareVersion = request.getParameter("softwareversion");
    String function = request.getParameter("function");
    WebRunInformation[] runs = null;
    boolean allowCompare = false;

    softwareVersion = softwareVersion != null ? softwareVersion : "";

    if ( function == null )
    {
        if ( request.getParameter("delete") != null )
        {
            int numberOfItems = Integer.parseInt( request.getParameter("number_of_elements") );

            for (int count=0;count<numberOfItems;count++)
            {
                String runIdStr = request.getParameter("delete_"+count);

                if ( runIdStr != null )
                {
                    long runId = Long.parseLong( runIdStr );

                    dtfResultsManager.deleteTestRun( runId );
                }
            }
        }

        runs = dtfResultsManager.getTestRunsForVersion(request.getParameter("softwareversion"));
    }
    else
    if ( function.equals("sametests") || function.equals("selectCompare") )
    {
        if ( function.equals("selectCompare") )
        {
            allowCompare = true;
        }

        System.out.println("Looking for same tests");
        String runIdStr = request.getParameter("runid");

        if ( runIdStr != null )
        {
            long sameTestsRunId = Long.parseLong(runIdStr);

            runs = dtfResultsManager.getSameTestRuns(sameTestsRunId);

            System.out.println("Found "+runs.length+" run(s)");
        }
    }
%>

<form name="selection" action="<%=allowCompare ? "default.jsp?page=compare" : "default.jsp?page=runs&softwareversion="+URLEncoder.encode(softwareVersion)%>" method="post">
<input type="hidden" name="runid" value="<%=request.getParameter("runid")%>"/>
<table align="center" width="75%" border="0" cellspacing="0" cellpadding="5">

  <tr bgcolor="#939393">

    <td align="center">

<%
    if ( !allowCompare )
    {
%>
		<input type="checkbox" name="selectall" onclick="selectAllCheckBoxes(selectall)"/>
<%
    }
%>

	</td>

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

    <td width="20">

      <font face="Tahoma" size="2">

        Results

      </font>

    </td>

  </tr>

<input type="hidden" name="number_of_elements" value="<%=runs.length%>"/>

<%
   if ( runs != null)
   for (int count=0;count<runs.length;count++)
   {
       if ( !runs[count].softwareVersion.equals(softwareVersion) )
       {
           softwareVersion = runs[count].softwareVersion;
%>
    <tr>
        <td colspan="6" align="center">

            Software Version: <b><%=softwareVersion%></b>

        </td>
    </tr>
<%
       }
%>

  <tr>

    <td align="center">

<%
       if ( allowCompare )
       {
%>
		<input type="radio" name="comparerunid" value="<%=runs[count].runId%>"/>
<%
       }
       else
       {
%>
		<input type="checkbox" name="delete_<%=count%>" value="<%=runs[count].runId%>"/>
<%
       }
%>
	</td>

    <td width="15%">

      <font face="Tahoma" size="1">

        <%=runs[count].dateTimeStarted%>

      </font>

    </td>

    <td width="15%">

      <font face="Tahoma" size="1">

        <%=org.jboss.dtf.testframework.dtfweb.utils.DateUtils.displayDate(runs[count].dateTimeFinished)%>

      </font>

    </td>

    <td>

      <font face="Tahoma" size="2">

        <a href="<%=runs[count].testDefinitionsURL%>"><%=runs[count].testDefinitionsDescription%></a>&nbsp;

      </font>

    </td>

    <td>

      <font face="Tahoma" size="2">

        <a href="<%=runs[count].testSelectionURL%>"><%=runs[count].testSelectionDescription%></a>&nbsp;

      </font>

    </td>

    <td align="center">

      <a href="default.jsp?page=view_results&softwareversion=<%=URLEncoder.encode(softwareVersion)%>&runid=<%=runs[count].runId%>"><font face="Tahoma" size="2" color="black">view</font></a>
<%
	   if ( !dtfResultsManager.isArchived(runs[count].runId))
	   {
%>
      <a href="delete_run.jsp?runid=<%=runs[count].runId%>&softwareversion=<%=URLEncoder.encode(softwareVersion)%>"><font face="Tahoma" size="2" color="black">delete</font></a>
<%
	   }
%>
    </td>

  </tr>

<%

   }

%>
  <tr>

    <td align="center">
<%
    if ( allowCompare )
    {
%>
		<input type="submit" name="compare" value="compare"/>
<%
    }
    else
    {
%>
		<input type="submit" name="delete" value="delete"/>
<%
    }
%>
	</td>

    <td width="15%">

		&nbsp;

    </td>

    <td width="15%">

		&nbsp;

    </td>

    <td>

		&nbsp;

    </td>

    <td>

		&nbsp;

    </td>

    <td align="center">

		&nbsp;

    </td>

  </tr>

</table>
</form>