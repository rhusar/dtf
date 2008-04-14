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
                 org.jboss.dtf.testframework.dtfweb.performance.UserDefinedGraph"%>
<%
    long runId = Long.parseLong(request.getParameter("perfrunid"));

    String widthStr = request.getParameter("width");
    String heightStr = request.getParameter("height");

    UserDefinedGraph userGraph = UserDefinedGraph.getGraph(session);
    int width = widthStr != null ? Integer.parseInt( widthStr ) : 600;
    int height = heightStr != null ? Integer.parseInt( heightStr ) : 400;
%>

<table align="center">
  <tr>
    <td>
      <table width="100%" border="1" cellspacing="0" bordercolor="black">
        <tr>
          <td>
              <table width="100%">
                <tr>
                  <td>
                    <font face="Tahoma" size="2">
                        size: <a href="default.jsp?page=view_data&perfrunid=<%=runId%>&width=800&height=500">lrg</a> | <a href="default.jsp?page=view_data&perfrunid=<%=runId%>&width=600&height=400">med</a> | <a href="default.jsp?page=view_data&perfrunid=<%=runId%>&width=400&height=300">sml</a>
                    </font>
                  </td>
<%
    if ( userGraph != null )
    {
%>
                  <td>
                    <font face="Tahoma" size="2">
                        <a href="default.jsp?page=view_other_tests&dataname=<%=userGraph.getDataName()%>">find more results</a>
                    </font>
                  </td>
<%
    }
%>
                  <td align="right">
                    <font face="Tahoma" size="2">
                        export: <a href="servlet/org.jboss.dtf.testframework.dtfweb.performance.CSVDataServlet?perfrunid=<%=runId%>">csv</a>
                    </font>
                  </td>
                </tr>
              </table>
          </td>
        </tr>
      </table>
    </td>
    <td>
    </td>
  </tr>
  <tr>
    <td align="center">

      <table width="<%=width%>" height="<%=height%>" border="0" cellspacing="0" cellpadding="5" bordercolor="black">
        <tr>
          <td align="center">
            <img src="servlet/org.jboss.dtf.testframework.dtfweb.performance.GraphDataServlet?perfrunid=<%=runId%>&width=<%=width%>&height=<%=height%>">
          </td>
        </tr>
      </table>

    </td>
    <td>
      <table border="1" cellspacing="0" bordercolor="black">
        <tr>
          <td>
            <table width="100%">
              <tr>
                <td>
                  <font face="Tahoma" size="2">
                    <a href="default.jsp?page=performance&function=new_graph&perfrunid=<%=runId%>">new</a>
                    <%if ( userGraph != null ) {%> | <a href="default.jsp?page=view_usergraph">view</a> <% } %>
                    <%if ( ( userGraph != null ) && (!userGraph.contains(runId)) ) {%> | <a href="default.jsp?page=performance&function=add_to_graph&perfrunid=<%=runId%>">add</a> <% } %>
                  </font>
                </td>
              </tr>
            </table>
          </td>
        </tr>
      </table>
    </td>
  </tr>
</table>