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
<jsp:useBean id="jdbcConnectionPool" scope="application" class="org.jboss.dtf.testframework.dtfweb.utils.JDBCConnectionPool" />
<jsp:useBean id="dtfManager" scope="session" class="org.jboss.dtf.testframework.dtfweb.DTFManagerBean" />
<jsp:useBean id="dtfResultsManager" scope="session" class="org.jboss.dtf.testframework.dtfweb.DTFResultsManager" />

<SCRIPT TYPE="text/javascript">
<!--
function popup(mylink, windowname)
{
    if (! window.focus)return true;
    var href;
    if (typeof(mylink) == 'string')
       href=mylink;
    else
       href=mylink.href;
    window.open(href, windowname, 'width=400,height=230,scrollbars=yes');
    return false;
}

function targetopener(mylink, closeme, closeonly)
{
if (! (window.focus && window.opener))return true;
window.opener.focus();
if (! closeonly)window.opener.location.href=mylink.href;
if (closeme)window.close();
return false;
}

function selectAllCheckBoxes(a)
{
	for (a=0;a<document.selection.length;a++)
	{
		if ( document.selection[a].name.indexOf( "delete_" ) != -1 )
		{
			document.selection[a].checked = document.selection.selectall.checked
		}
	}
}
//-->
</SCRIPT>

<%@ page session="true" %>

<%
	dtfManager.handleRequest(request);
	MyDTF myDTF = MyDTF.getMyDTF(request, response);
%>

<html>

<head>

    <title>Distributed Testing Framework</title>
    <link type="text/css" rel="stylesheet" href="style.css">

</head>

<body>

<table border="0" cellpadding="0" cellspacing="0" style="border-collapse: collapse" bordercolor="#111111" width="100%" bgcolor="white" height="100%">
  <tr>
    <td width="79%" bgcolor="white" height="100%" valign="top">

<%
    if ( dtfManager.getPageToDisplay().equals("mymanagement") )
	{
%>
		<%@ include file="mymanagement.jsp" %>
<%
	}
	else
	if ( dtfManager.getPageToDisplay().equals("management") )
	{
%>
		<%@ include file="management.jsp" %>
<%
	}
	else
	if ( dtfManager.getPageToDisplay().equals("myversions") )
	{
%>
		<%@ include file="myversions.jsp" %>
<%
	}
	else
	if (dtfManager.getPageToDisplay().equals("versions"))
	{
%>
		  <%@ include file="versions.jsp" %>
<%
	}
	else
	if (dtfManager.getPageToDisplay().equals("view_other_tests"))
	{
%>
		  <jsp:include page="view_other_perf_tests.jsp" flush="true"/>
<%
	}
	else
	if (dtfManager.getPageToDisplay().equals("runs"))
	{
%>
		  <jsp:include page="runs.jsp" flush="true"/>
<%
	}
	else
    if (dtfManager.getPageToDisplay().equals("saved_graphs"))
    {
%>
		  <%@ include file="view_saved_graphs.jsp" %>
<%
    }
    else
    if (dtfManager.getPageToDisplay().equals("confirmaddition"))
    {
%>
		  <%@ include file="confirm_achive_addition.jsp" %>
<%
    }
    else
    if (dtfManager.getPageToDisplay().equals("compare"))
    {
%>
		  <%@ include file="compare.jsp" %>
<%
    }
	else
    if (dtfManager.getPageToDisplay().equals("schedule"))
    {
%>
		  <%@ include file="schedule.jsp" %>
<%
    }
	else
	if (dtfManager.getPageToDisplay().equals("view_results"))
	{
%>
		  <jsp:include page="view_results.jsp" flush="true"/>
<%
	}
	else
	if (dtfManager.getPageToDisplay().equals("createarchive"))
	{
%>
		  <jsp:include page="createarchive.jsp" flush="true"/>
<%
	}
	else
	if (dtfManager.getPageToDisplay().equals("createselection"))
	{
%>
		  <%@ include file="createselection.jsp" %>
<%
	}
	else
	if (dtfManager.getPageToDisplay().equals("view_details"))
	{
%>
		  <jsp:include page="view_details.jsp" flush="true"/>
<%
	}
	else
	if (dtfManager.getPageToDisplay().equals("view_information"))
	{
%>
		  <jsp:include page="view_information.jsp" flush="true"/>
<%
	}
	else
	if (dtfManager.getPageToDisplay().equals("view_data"))
	{
%>
		  <jsp:include page="view_data.jsp" flush="true"/>
<%
	}
	else
	if (dtfManager.getPageToDisplay().equals("performance"))
	{
%>
		  <%@ include file="view_saved_graphs.jsp" %>
<%
	}
	else
	if (dtfManager.getPageToDisplay().equals("createtaskrunner"))
	{
%>
		  <%@ include file="createtaskrunner.jsp" %>
<%
	}
	else
	if (dtfManager.getPageToDisplay().equals("view_usergraph"))
	{
%>
		  <%@ include file="view_usergraph.jsp" %>
<%
	}
	else
	if (dtfManager.getPageToDisplay().equals("productconfig"))
	{
%>
		  <%@ include file="productconfig.jsp" %>
<%
	}
	else
	if (dtfManager.getPageToDisplay().equals("createclasspath"))
	{
%>
		  <%@ include file="createclasspath.jsp" %>
<%
	}
	else
	if (dtfManager.getPageToDisplay().equals("createnodeconfig"))
	{
%>
		  <%@ include file="createnodeconfig.jsp" %>
<%
	}
	else
	if (dtfManager.getPageToDisplay().equals("view_testrun_information"))
	{
%>
		  <jsp:include page="view_testrun_information.jsp" flush="true"/>
<%
	}
	else
	if (dtfManager.getPageToDisplay().equals("management"))
	{
%>
		  <%@ include file="management.jsp" %>
<%
	}
	else
	if (dtfManager.getPageToDisplay().equals("status"))
	{
%>
		  <jsp:include page="frameworkstatus.jsp" flush="true"/>
<%
	}
	else
	if (dtfManager.getPageToDisplay().equals("email_results"))
	{
%>
		  <jsp:include page="email_results.jsp" flush="true"/>
<%
	}
	else
	if (dtfManager.getPageToDisplay().equals("manualtestrun"))
	{
%>
		  <jsp:include page="manualtestrun.jsp" flush="true"/>
<%
	}
	else
	if (dtfManager.getPageToDisplay().equals("setup"))
	{
%>
		  <jsp:include page="setup.jsp" flush="true"/>
<%
	}
	else
	if (dtfManager.getPageToDisplay().equals("deployment"))
	{
%>
		  <%@ include file="deployment.jsp" %>
<%
	}
	else
	if (dtfManager.getPageToDisplay().equals("nodemanager"))
	{
%>
		  <%@ include file="taskmanager.jsp" %>
<%
	}
	else
	if (dtfManager.getPageToDisplay().equals("archives"))
	{
%>
		  <%@ include file="archives.jsp" %>
<%
	}
	else
	if (dtfManager.getPageToDisplay().equals("viewarchive"))
	{
%>
		  <%@ include file="viewarchive.jsp" %>
<%
	}
%>
    </td>
    <td width="3" bgcolor="white"><img src="pixel.gif" width="1" height="1"/></td>
    <td width="1" bgcolor="black"><img src="pixel.gif" width="1" height="1"/></td>
    <td width="21%" height="100%" align="center" valign="top">
	<img src="small-dtf-logo.jpg"/>
    <table border="0" cellpadding="2" bordercolor="#111111" width="100%" cellspacing="3">
      <tr>
        <td width="63%" bgcolor="#000000">
<%
	boolean mydtfMenuShown = dtfManager.isDisplayingMenu("myDTF");
%>
        	<a class="option" href="<%=dtfManager.getToggleURL( request, "myDTF" )%>"><img border="0" src="<%=mydtfMenuShown ? "down.gif" : "up.gif"%>" width="10" height="10"/> myDTF</a>
		</td>
      </tr>
<%
	if ( mydtfMenuShown )
	{
%>
      <tr>
        <td width="100%" bgcolor="<%=dtfManager.getPageToDisplay().equals("mymanagement") ? "#FFFF99" : "#EDEEFE"%>"><font face="Verdana" size="1">- <a href="default.jsp?page=mymanagement">management</a></font></td>
      </tr>
      <tr>
        <td width="100%" bgcolor="<%=dtfManager.getPageToDisplay().equals("myversions") ? "#FFFF99" : "#EDEEFE"%>"><font face="Verdana" size="1">- <a href="default.jsp?page=myversions">runs</a></font></td>
      </tr>
<%
	}
%>
    </table>
    <table border="0" cellpadding="2" bordercolor="#111111" width="100%" cellspacing="3">
      <tr>
        <td width="63%" bgcolor="#000000">
<%
	boolean optionsMenuShown = dtfManager.isDisplayingMenu("options");
%>
        	<a class="option" href="<%=dtfManager.getToggleURL( request, "options" )%>"><img border="0" src="<%=optionsMenuShown ? "down.gif" : "up.gif"%>" width="10" height="10"/> options</a>
		</td>
      </tr>
<%
	if ( optionsMenuShown )
	{
%>
      <tr>
        <td width="100%" bgcolor="<%=dtfManager.getPageToDisplay().equals("management") ? "#FFFF99" : "#EDEEFE"%>"><font face="Verdana" size="1">- <a href="default.jsp?page=management">management</a></font></td>
      </tr>
      <tr>
        <td width="100%" bgcolor="<%=dtfManager.getPageToDisplay().equals("schedule") ? "#FFFF99" : "#EDEEFE"%>"><font face="Verdana" size="1">- <a href="default.jsp?page=schedule">schedule</a></font></td>
      </tr>
      <tr>
        <td width="100%" bgcolor="<%=dtfManager.getPageToDisplay().equals("versions") ? "#FFFF99" : "#EDEEFE"%>"><font face="Verdana" size="1">- <a href="default.jsp?page=versions">runs</a></font></td>
      </tr>
      <tr>
        <td width="100%" bgcolor="<%=dtfManager.getPageToDisplay().equals("setup") ? "#FFFF99" : "#EDEEFE"%>"><font face="Verdana" size="1">- <a href="default.jsp?page=setup">setup</a></font></td>
      </tr>
      <tr>
        <td width="100%" bgcolor="<%=dtfManager.getPageToDisplay().equals("performance") ? "#FFFF99" : "#EDEEFE"%>"><font face="Verdana" size="1">- <a href="default.jsp?page=performance">performance</a></font></td>
      </tr>
      <tr>
        <td width="100%" bgcolor="<%=dtfManager.getPageToDisplay().equals("deployment") ? "#FFFF99" : "#EDEEFE"%>"><font face="Verdana" size="1">- <a href="default.jsp?page=deployment">deployment</a></font></td>
      </tr>
      <tr>
        <td width="100%" bgcolor="<%=dtfManager.getPageToDisplay().equals("nodemanager") ? "#FFFF99" : "#EDEEFE"%>"><font face="Verdana" size="1">- <a href="default.jsp?page=nodemanager">node manager</a></font></td>
      </tr>
      <tr>
        <td width="100%" bgcolor="<%=dtfManager.getPageToDisplay().equals("archives") ? "#FFFF99" : "#EDEEFE"%>"><font face="Verdana" size="1">- <a href="default.jsp?page=archives">archives</a></font></td>
      </tr>
<%
	}
%>
    </table>
<%
	if ( dtfManager.getPageToDisplay().equals("view_results") )
	{
%>
    <table border="0" cellpadding="2" bordercolor="#111111" width="100%" cellspacing="3">
      <tr>
        <td width="63%" bgcolor="#000000">
        	<font face="Verdana" size="1" color="#FFFFFF">
<%
	boolean runMenuShown = dtfManager.isDisplayingMenu("run");
%>
        		<a class="option" href="<%=dtfManager.getToggleURL( request, "run" )%>"><img border="0" src="<%=runMenuShown ? "down.gif" : "up.gif"%>" width="10" height="10"/> run options</a>
			</font>
		</td>
      </tr>
<%
	if ( runMenuShown )
	{
%>
      <tr>
        <td width="100%" bgcolor="#EDEEFE"><font face="Verdana" size="1">- <a href="default.jsp?page=runs&function=sametests&runid=<%=request.getParameter("runid")%>">find runs against same tests</a></font></td>
      </tr>
      <tr>
        <td width="100%" bgcolor="#EDEEFE"><font face="Verdana" size="1">- <a href="default.jsp?page=management&function=rerun&runid=<%=request.getParameter("runid")%>">re-run these tests</a></font></td>
      </tr>
      <tr>
        <td width="100%" bgcolor="#EDEEFE"><font face="Verdana" size="1">- <a href="default.jsp?page=runs&function=selectCompare&runid=<%=request.getParameter("runid")%>">compare these results</a></font></td>
      </tr>
      <tr>
        <td width="100%" bgcolor="#EDEEFE"><font face="Verdana" size="1">- <a href="delete_run.jsp?runid=<%=request.getParameter("runid")%>&softwareversion=<%=request.getParameter("softwareversion")%>">delete these results</a></font></td>
      </tr>
      <tr>
        <td width="100%" bgcolor="#EDEEFE"><font face="Verdana" size="1">- <a href="default.jsp?page=email_results&runid=<%=request.getParameter("runid")%>&softwareversion=<%=request.getParameter("softwareversion")%>">email these results</a></font></td>
      </tr>
<%
	}
%>
    </table>
<%
	}
%>

    <form action="removefromcurrentarchive.jsp" method="post">
    <table border="0" cellpadding="2" bordercolor="#111111" width="100%" cellspacing="3">
      <tr>
        <td width="63%" bgcolor="#000000">
        	<font face="Verdana" size="1" color="#FFFFFF">
<%
	boolean archiveMenuShown = dtfManager.isDisplayingMenu("archive");
%>
        		<a class="option" href="<%=dtfManager.getToggleURL( request, "archive" )%>"><img border="0" src="<%=archiveMenuShown ? "down.gif" : "up.gif"%>" width="10" height="10"/> archive options</a>
			</font>
		</td>
      </tr>
<%
	if ( archiveMenuShown )
	{
		ArrayList currentArchive = (ArrayList)session.getAttribute("currentarchive");

		if ( currentArchive == null )
		{
			session.setAttribute("currentarchive", currentArchive = new ArrayList());
		}

		if ( dtfManager.getPageToDisplay().equals("view_results") )
		{
%>
      <tr>
        <td width="100%" bgcolor="#EDEEFE"><font face="Verdana" size="1">- <a href="addtocurrentarchive.jsp?runid=<%=request.getParameter("runid")%>">add these tests</a></font></td>
      </tr>
	  <input type="hidden" name="number_of_entries" value="<%=currentArchive.size()%>"/>
<%
		}

		if ( currentArchive.size() > 0 )
		{
%>
      <tr>
        <td width="100%" bgcolor="#EDEEFE"><font face="Verdana" size="1">- <a href="default.jsp?page=createarchive">create archive</a></font></td>
      </tr>
<%
		}

		for (int count=0;count<currentArchive.size();count++)
		{
%>
      <tr>
        <td width="100%" bgcolor="#A2A8FA"><input type="checkbox" name="archive_entry_<%=count%>" value="<%=((RunUID)currentArchive.get(count)).getUID()%>"><font face="Verdana" size="1"><a href="default.jsp?page=view_results&runid=<%=((RunUID)currentArchive.get(count)).getUID()%>">Run id. <%=((RunUID)currentArchive.get(count)).getUID()%></a></font></td>
      </tr>
<%
		}

		if ( currentArchive.size() > 0 )
		{
%>
      <tr>
        <td width="100%" bgcolor="#EDEEFE"><input type="image" name="remove" src="remove.gif" alt="Remove selected entries from archive"/></td>
      </tr>
<%
		}
%>
	</form>
      <tr>
        <td width="100%" bgcolor="#EDEEFE"><font face="Verdana" size="1"><form action="default.jsp?page=confirmaddition" method="post">Add:<input type="text" name="start_runid" size="6"/> to <input type="text" name="finish_runid" size="6"/><input type="submit" name="addruns" value="add"/></form></font></td>
      </tr>
<%
    }
%>
    </table>

    </td>
  </tr>
</table>

</body>

</html>
