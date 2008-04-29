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
                 java.net.URLEncoder"%>

<table align="center" width="75%" border="1" cellspacing="0" cellpadding="5" bordercolor="gray">

    <tr bgcolor="#A0A0FF">
        <td>
            <font face="Tahoma" size="2">
                Date/Time Created
            </font>
        </td>
        <td>
            <font face="Tahoma" size="2">
                Name
            </font>
        </td>
        <td>
            <font face="Tahoma" size="2">
                Number of Series
            </font>
        </td>
        <td>
            <font face="Tahoma" size="2">
                Options
            </font>
        </td>
    </tr>
<%
    ArrayList udgList = UserDefinedGraphInformation.list();

    for (int count=0;count<udgList.size();count++)
    {
        UserDefinedGraphInformation udgInfo = (UserDefinedGraphInformation)udgList.get(count);
%>
    <tr>
        <td>
            <font face="Tahoma" size="2">
                <%=DateUtils.displayDate(udgInfo.getDateTimeCreated())%>
            </font>
        </td>
        <td>
            <font face="Tahoma" size="2">
                <%=udgInfo.getName()%>
            </font>
        </td>
        <td>
            <font face="Tahoma" size="2">
                <%=udgInfo.getNumberOfSeries()%>
            </font>
        </td>
        <td>
            <font face="Tahoma" size="2">
                <a href="default.jsp?page=view_usergraph&action=view_saved_graph&name=<%=URLEncoder.encode(udgInfo.getName())%>">view</a>
                <a href="default.jsp?page=performance&function=delete_saved_graph&name=<%=URLEncoder.encode(udgInfo.getName())%>">delete</a>
            </font>
        </td>
    </tr>
<%
    }
%>
</table>