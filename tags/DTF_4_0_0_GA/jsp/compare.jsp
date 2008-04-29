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
<%@ page import="java.net.URLEncoder,
                 java.util.ArrayList,
                 org.jboss.dtf.testframework.dtfweb.*"%>
<%
    long firstRunId = 0,
         compareRunId = 0;
    WebRunInformation firstRunInfo = null;
    WebRunInformation compareRunInfo = null;

    try
    {
        firstRunId = Long.parseLong(request.getParameter("runid"));
        compareRunId = Long.parseLong(request.getParameter("comparerunid"));

        firstRunInfo = dtfResultsManager.getTestRunInformation(firstRunId);
        compareRunInfo = dtfResultsManager.getTestRunInformation(compareRunId);

    }
    catch (NullPointerException e)
    {
        response.sendRedirect("default.jsp?page=results");
    }

    ComparisonReport comparisonReport = ComparisonReport.compare(firstRunId, compareRunId);
%>
<table bgcolor="white" width="75%" align="center" cellspacing="0" cellpadding="5" border="0">
  <tr>
    <td>
      <table width="100%" cellpadding="0">
	    <tr>
	      <td width="33%">
	        <font face="Tahoma" size="2" color="Black">
	          Comparison between <a href="default.jsp?page=view_results&runid=<%=firstRunId%>"><%=firstRunId%></a> and <a href="default.jsp?page=view_results&runid=<%=compareRunId%>"><%=compareRunId%></a>
  		    </font>
		  </td>
	    </tr>
	  </table>
	</td>
  </tr>
</table>

<table align="center" width="75%" border="0" cellspacing="0" cellpadding="5">

  <tr bgcolor="#B3B3B3">

    <td>

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


  <tr>

    <td width="7%">

      <font face="Tahoma" size="1">

        <%=firstRunInfo.runId%>

      </font>

    </td>

    <td width="15%">

      <font face="Tahoma" size="1">

        <%=firstRunInfo.dateTimeStarted%>

      </font>

    </td>

    <td width="15%">

      <font face="Tahoma" size="1">

        <%=org.jboss.dtf.testframework.dtfweb.utils.DateUtils.displayDate(firstRunInfo.dateTimeFinished)%>

      </font>

    </td>

    <td>

      <font face="Tahoma" size="2">

        <a href="<%=firstRunInfo.testDefinitionsURL%>"><%=firstRunInfo.testDefinitionsDescription%></a>&nbsp;

      </font>

    </td>

    <td>

      <font face="Tahoma" size="2">

        <a href="<%=firstRunInfo.testSelectionURL%>"><%=firstRunInfo.testSelectionDescription%></a>&nbsp;

      </font>

    </td>

  </tr>

  <tr>

    <td width="7%">

      <font face="Tahoma" size="1">

        <%=compareRunInfo.runId%>

      </font>

    </td>

    <td width="15%">

      <font face="Tahoma" size="1">

        <%=compareRunInfo.dateTimeStarted%>

      </font>

    </td>

    <td width="15%">

      <font face="Tahoma" size="1">

        <%=org.jboss.dtf.testframework.dtfweb.utils.DateUtils.displayDate(compareRunInfo.dateTimeFinished)%>

      </font>

    </td>

    <td>

      <font face="Tahoma" size="2">

        <a href="<%=compareRunInfo.testDefinitionsURL%>"><%=compareRunInfo.testDefinitionsDescription%></a>&nbsp;

      </font>

    </td>

    <td>

      <font face="Tahoma" size="2">

        <a href="<%=compareRunInfo.testSelectionURL%>"><%=compareRunInfo.testSelectionDescription%></a>&nbsp;

      </font>

    </td>

  </tr>

</table>

<table align="center" width="75%" border="0" cellspacing="3" cellpadding="5">
    <tr bgcolor="#B3B3B3">
        <td width="35%">
            <font face="Tahoma" size="1">
                Test Name
            </font>
        </td>
        <td width="20%">
            <table width="100%">
                <tr>
                    <td colspan="2" align="center">
                        <font face="Tahoma" size="1">
                            Test Duration
                        </font>
                    </td>
                </tr>
                <tr>
                    <td align="center">
                        <font face="Tahoma" size="1">
                            First
                        </font>
                    </td>
                    <td align="center">
                        <font face="Tahoma" size="1">
                            Second
                        </font>
                    </td>
                </tr>
            </table>
        </td>
        <td width="20%">
            <table width="100%">
                <tr>
                    <td colspan="2" align="center">
                        <font face="Tahoma" size="1">
                            Permutation Code
                        </font>
                    </td>
                </tr>
                <tr>
                    <td align="center">
                        <font face="Tahoma" size="1">
                            First
                        </font>
                    </td>
                    <td align="center">
                        <font face="Tahoma" size="1">
                            Second
                        </font>
                    </td>
                </tr>
            </table>
        </td>
        <td width="20%">
            <table width="100%">
                <tr>
                    <td colspan="2" align="center">
                        <font face="Tahoma" size="1">
                            Results
                        </font>
                    </td>
                </tr>
                <tr>
                    <td align="center">
                        <font face="Tahoma" size="1">
                            First
                        </font>
                    </td>
                    <td align="center">
                        <font face="Tahoma" size="1">
                            Second
                        </font>
                    </td>
                </tr>
            </table>
        </td>
    </tr>
<%
    ArrayList results = comparisonReport.getResults();
    String testColor = "black";

    for (int count=0;count<results.size();count++)
    {
        Object resObj = results.get(count);

        if ( resObj instanceof InOneSetOnly )
        {
            InOneSetOnly oneSetOnly = (InOneSetOnly)resObj;
            TestResultInformation result = oneSetOnly.getTestResultInfo();
            testColor = result.getColor();

%>
    <tr>
        <td>
            <font face="Tahoma" size="1">
                <%=result.testName%>
            </font>
        </td>
        <td>
            <table width="100%">
                <tr>
                    <td align="center">
                        <font face="Tahoma" size="1">
                            <%=oneSetOnly.isFirstSet() ? (result.getDuration() / 1000) + "second(s)" : "---"%>
                        </font>
                    </td>
                    <td align="center">
                        <font face="Tahoma" size="1">
                            <%=!oneSetOnly.isFirstSet() ? (result.getDuration() / 1000) + "second(s)" : "---"%>
                        </font>
                    </td>
                </tr>
            </table>
        </td>
        <td>
            <table width="100%">
                <tr>
                    <td align="center">
                        <font face="Tahoma" size="1">
                            <%=oneSetOnly.isFirstSet() ? dtfResultsManager.getOSProductCombination(result.permutationCode,"<br>") : "---"%>
                        </font>
                    </td>
                    <td align="center">
                        <font face="Tahoma" size="1">
                            <%=!oneSetOnly.isFirstSet() ? dtfResultsManager.getOSProductCombination(result.permutationCode,"<br>") : "---"%>
                        </font>
                    </td>
                </tr>
            </table>
        </td>
        <td>
            <table width="100%">
                <tr>
                    <td align="center">
                        <font face="Tahoma" size="1" color="<%=testColor%>">
                            <%=oneSetOnly.isFirstSet() ? result.overAllResult : ""%>
                        </font>
                    </td>
                    <td align="center">
                        <font face="Tahoma" size="1" color="<%=testColor%>">
                            <%=!oneSetOnly.isFirstSet() ? result.overAllResult : ""%>
                        </font>
                    </td>
                </tr>
            </table>
        </td>
    </tr>
<%
        }
        else
        {
            ResultComparison resultComparison = (ResultComparison)resObj;
            String test1Color = resultComparison.getFirstTestResult().getColor();
            String test2Color = resultComparison.getSecondTestResult().getColor();
%>
    <tr>
        <td>
            <font face="Tahoma" size="1">
                <%=resultComparison.getFirstTestResult().testName%>
            </font>
        </td>
        <td>
            <table width="100%">
                <tr>
                    <td align="center">
                        <font face="Tahoma" size="1">
                            <%=(resultComparison.getFirstDuration() / 1000) + "second(s)"%>
                        </font>
                    </td>
                    <td align="center">
                        <font face="Tahoma" size="1">
                            <%=(resultComparison.getSecondDuration() / 1000) + "second(s)"%>
                        </font>
                    </td>
                </tr>
            </table>
        </td>
        <td>
            <table width="100%">
                <tr>
                    <td align="center">
                        <font face="Tahoma" size="1">
                            <%=dtfResultsManager.getOSProductCombination(resultComparison.getFirstTestResult().permutationCode,"<br>")%>
                        </font>
                    </td>
                    <td align="center">
                        <font face="Tahoma" size="1">
                            <%=dtfResultsManager.getOSProductCombination(resultComparison.getSecondTestResult().permutationCode,"<br>")%>
                        </font>
                    </td>
                </tr>
            </table>
        </td>
        <td>
            <table width="100%">
                <tr>
                    <td align="center">
                        <font face="Tahoma" size="1" color="<%=test1Color%>">
                            <%=resultComparison.getFirstTestResult().overAllResult%>
                        </font>
                    </td>
                    <td align="center">
                        <font face="Tahoma" size="1" color="<%=test2Color%>">
                            <%=resultComparison.getSecondTestResult().overAllResult%>
                        </font>
                    </td>
                </tr>
            </table>
        </td>
    </tr>
<%
        }
    }
%>
</table>
