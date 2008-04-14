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
<%@ page import="org.jboss.dtf.testframework.dtfweb.mydtf.MyDTF"%>
<%
	String function = request.getParameter("function");

	if ( function != null )
	{
		if ( function.equals("add") )
		{
         	myDTF.addRunSoftwareVersion( request.getParameter("version") );
		}
		else
		if ( function.equals("remove") )
		{
			myDTF.removeRunSoftwareVersion( request.getParameter("version") );
	   }
	}

    String findRunId = request.getParameter("findrunid");
    boolean triedToFind = false;
    boolean notFound = false;

    if ( findRunId != null )
    {
        try
        {
            long runId = Long.parseLong(findRunId);

            if ( dtfResultsManager.getTestRunInformation(runId) != null )
            {
                response.sendRedirect("default.jsp?page=view_results&runid="+runId);
            }
            else
            {
                triedToFind = true;
                notFound = true;
            }
        }
        catch (Exception e)
        {
            triedToFind = true;
            notFound = true;
        }
    }
%>
<br>
<form action="default.jsp?page=versions" method="post">
<table align="center" width="75%" border="0" cellspacing="0" cellpadding="5">
<%
    if ( triedToFind && notFound )
    {
%>
  <tr>
    <td bgcolor="red">
        <font color="white">Cannot find run <%=findRunId%></font>
    </td>
  </tr>
<%
    }
%>
  <tr>
    <td>
      <font face="Tahoma" size="2">
        <b>
			Find Run: <input type="text" name="findrunid" size="6"/>
		</b>
      </font>
    </td>
  </tr>
  <tr>
    <td bgcolor="#939393">
      <font face="Tahoma" size="2">
        <b>
			Software Version
		</b>
      </font>
    </td>
  </tr>
  <tr>
    <td>
	  <table width="100%">
<%
  String[] versions = dtfResultsManager.getSoftwareVersions();
  boolean toggle = false;

  for (int count=0;count<versions.length;count++)
  {
%>
		<tr bgcolor="<%=toggle ? "#F3F3F3" : "white"%>">
		  <td width="70%">
      	    <font face="Tahoma" size="2">
              <a href="default.jsp?page=runs&softwareversion=<%=versions[count]%>"><%=versions[count]%></a>
            </font>
		  </td>
		  <td>
		    [ <a href="default.jsp?page=versions&function=<%=myDTF.containsSoftwareVersion(versions[count]) ? "remove" : "add"%>&version=<%=versions[count]%>"><%=myDTF.containsSoftwareVersion(versions[count]) ? "remove from" : "add to"%> myDTF</a> ]
		  </td>
		</tr>
<%
	  toggle = !toggle;
  }
%>
	  </table>
    </td>
  </tr>
</table>
</form>
