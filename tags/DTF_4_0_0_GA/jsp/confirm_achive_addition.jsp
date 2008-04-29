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
                 java.util.ArrayList,
                 org.jboss.dtf.testframework.testnode.RunUID"%>
<%
    long startRunId = 0;
    long finishRunId = 0;
    ArrayList confirmedRuns = null;

    if ( request.getParameter("start_runid") != null )
    {
        try
        {
            startRunId = Long.parseLong(request.getParameter("start_runid"));
            finishRunId = Long.parseLong(request.getParameter("finish_runid"));
        }
        catch (NullPointerException e)
        {
            response.sendRedirect("default.jsp?page=runs");
        }

        if ( finishRunId < startRunId )
        {
            response.sendRedirect("default.jsp?page=runs");
        }

        confirmedRuns = new ArrayList();

        for (long currentRunId=startRunId;currentRunId<finishRunId;currentRunId++)
        {
            WebRunInformation runInfo = dtfResultsManager.getTestRunInformation(currentRunId);

            if ( runInfo != null )
            {
                confirmedRuns.add(runInfo);
            }
        }

        session.setAttribute("confirmedrunslist", confirmedRuns);
    }
    else
    {
        confirmedRuns = (ArrayList)session.getAttribute("confirmedrunslist");

        if ( confirmedRuns == null )
        {
            response.sendRedirect("default.jsp?page=runs");
        }

        if ( request.getParameter("remove") != null )
        {
            int numberOfElements = Integer.parseInt(request.getParameter("number_of_elements"));

            for (int count=numberOfElements-1;count>=0;count--)
            {
                if ( request.getParameter("delete_"+count) != null )
                {
                    confirmedRuns.remove(count);
                }
            }
        }
        else
        if ( request.getParameter("addtoarchive") != null )
        {
            ArrayList currentArchive = (ArrayList)session.getAttribute("currentarchive");

            for (int count=0;count<confirmedRuns.size();count++)
            {
                WebRunInformation runInfo = (WebRunInformation)confirmedRuns.get(count);
                currentArchive.add(new RunUID(runInfo.runId));
            }

            response.sendRedirect("default.jsp?page=runs");
        }
    }
%>

<form name="selection" action="default.jsp?page=confirmaddition" method="post">
<table align="center" width="75%" border="0" cellspacing="0" cellpadding="5">

  <tr bgcolor="#939393">

    <td align="center">

        <input type="submit" name="remove" value="remove"/>

	</td>

    <td width="20">

      <font face="Tahoma" size="2">

        Run Id.

      </font>

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

  </tr>

<input type="hidden" name="number_of_elements" value="<%=confirmedRuns.size()%>"/>
<%
    for (int count=0;count<confirmedRuns.size();count++)
    {

        WebRunInformation runInfo = (WebRunInformation)confirmedRuns.get(count);
%>
  <tr>

    <td align="center">

        <input type="checkbox" name="delete_<%=count%>" value="<%=runInfo.runId%>"/>

	</td>

    <td>

      <font face="Tahoma" size="1">

        <%=runInfo.runId%>

      </font>

    </td>

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
%>
</table>

<p align="center"><input type="submit" name="addtoarchive" value="add to archive"/></p>
</form>