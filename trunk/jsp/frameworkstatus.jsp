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
<jsp:useBean id="dtfRunManager" scope="session" class="org.jboss.dtf.testframework.dtfweb.DTFRunManager" />

<%
	org.jboss.dtf.testframework.coordinator.CoordinatorDescriptor[] coordinators = dtfRunManager.getCoordinatorDetails();
	org.jboss.dtf.testframework.dtfweb.TestNodeDetails[] nodes = dtfRunManager.getTestNodeDetails();
%>

<p align="center">
  <font face="Tahoma" size="2">
    <b>
      Coordinators
    </b>
  </font>
</p>
<table align="center" width="75%" border="1" cellspacing="0" cellpadding="5" bordercolor="gray">
  <tr bgcolor="#A0A0FF">
    <td>
      <font face="Tahoma" size="2" color="Black">
        Name
      </font>
    </td>
    <td>
      <font face="Tahoma" size="2" color="Black">
        Logging URL
      </font>
    </td>
    <td>
      <font face="Tahoma" size="2" color="Black">
        Max #Retries
      </font>
    </td>
    <td>
      <font face="Tahoma" size="2" color="Black">
        Test Run In Progress
      </font>
    </td>
    <td>
      <font face="Tahoma" size="2" color="Black">
        #Queued Tests
      </font>
    </td>
  </tr>
<%
  for (int count=0;count<coordinators.length;count++)
  {
%>
  <tr>
    <td>
      <font face="Tahoma" size="2" color="Black">
        <%=coordinators[count]._name%>&nbsp;
      </font>
    </td>
    <td>
      <font face="Tahoma" size="2" color="Black">
        <a href="<%=coordinators[count]._loggingURL%>"><%=coordinators[count]._loggingURL%></a>&nbsp;
      </font>
    </td>
    <td>
      <font face="Tahoma" size="2" color="Black">
        <%=coordinators[count]._maxNumRetries%>
      </font>
    </td>
    <td>
      <font face="Tahoma" size="2" color="Black">
        <%
          if (coordinators[count]._testInProgress)
          {
        %>
        	<%=coordinators[count]._softwareVersion%> (run id. <%=coordinators[count]._currentRunUID.getUID()%>) (<a href="default.jsp?page=view_results&softwareversion=<%=coordinators[count]._softwareVersion%>&runid=<%=coordinators[count]._currentRunUID.getUID()%>">results</a>)
        <%
          }
          else
          {
        %>
            None
        <%
          }
        %>
      </font>
    </td>
    <td>
      <font face="Tahoma" size="2" color="Black">
        <%=coordinators[count]._numQueuedTests%>
      </font>
    </td>
  </tr>
<%
  }
%>
</table>
<p align="center">
  <font face="Tahoma" size="2">
    <b>
      Test Nodes
    </b>
  </font>
</p>
<table align="center" width="75%" border="1" cellspacing="0" cellpadding="5" bordercolor="gray">
  <tr bgcolor="#A0A0FF">
    <td>
      <font face="Tahoma" size="2" color="Black">
        Name
      </font>
    </td>
    <td>
      <font face="Tahoma" size="2" color="Black">
        Address
      </font>
    </td>
  </tr>
<%
  if (nodes!=null)
  {
    for (int count=0;count<nodes.length;count++)
    {
%>
  <tr>
    <td>
      <font face="Tahoma" size="2" color="Black">
        <%=nodes[count]._name%>
      </font>
    </td>
    <td>
      <font face="Tahoma" size="2" color="Black">
        <%=nodes[count]._hostAddress%>
      </font>
    </td>
  </tr>
<%
    }
  }
%>
</table>