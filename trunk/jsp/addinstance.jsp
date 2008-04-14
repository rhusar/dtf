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
<jsp:useBean id="dtfManager" scope="session" class="org.jboss.dtf.testframework.dtfweb.DTFManagerBean" />
<html>
    <head>
        <title>
            Distributed Testing Framework - Add Instance
        </title>
    </head>

    <body bgcolor="white">

<%
    String serviceURI = request.getParameter("serviceuri");

    if ( serviceURI != null )
    {
%>
            <table align="center" width="100%">
                <tr>
                    <td align="center">
                        <font face="Tahoma" size="2">
<%
        if ( dtfManager.addNewDTFInstance(serviceURI) )
        {
%>
                            New instance '<%=serviceURI%>', added successfully
<%
        }
        else
        {
%>
                            An error occurred while trying to add new instance '<%=serviceURI%>'
<%
        }
%>
                        </font>
                    </td>
                </tr>
                <tr>
                    <td align="center">
                        <a href="default.jsp" onClick="return targetopener(this,true,true)">close</a>
                    </td>
                </tr>
            </table>
<%
    }
    else
    {
%>
        <form action="addinstance.jsp" method="post">

            <p align="center">
                <font face="Tahoma" size="4">
                    <b>Add New DTF Instance</b>
                </font>
            </p>

            <table width="100%">
                <tr>
                    <td>
                        <font face="Tahoma" size="2">
                            New Instance's Name Service URI:
                        </font>
                    </td>
                    <td>
                        <input type="text" tabindex="0" name="serviceuri" size="32"/>
                    </td>
                </tr>
            </table>

        </form>

<%
    }
%>
    </body>
</html>
