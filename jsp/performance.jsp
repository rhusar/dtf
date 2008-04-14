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
<%@ page import="org.jboss.dtf.testframework.dtfweb.performance.UserDefinedGraph"%>
<%
    String function = request.getParameter("function");

    if ( function != null )
    {

        if ( function.equals("delete_saved_graph") )
        {
            String name = request.getParameter("name");

            if ( name != null )
            {
                UserDefinedGraph.deleteGraph(name);
            }

            response.sendRedirect("default.jsp?page=saved_graphs");
        }
        if ( function.equals("new_graph") )
        {
            long perfRunId = Long.parseLong( request.getParameter("perfrunid") );

            UserDefinedGraph graph = UserDefinedGraph.createGraph(session);

            int seriesNumber = graph.addPerformanceData(perfRunId);
            graph.setSeriesName( seriesNumber, "Performance Run #"+perfRunId );

            response.sendRedirect("default.jsp?page=view_usergraph");
        }

        if ( function.equals("add_to_graph") )
        {
            long perfRunId = Long.parseLong( request.getParameter("perfrunid") );

            UserDefinedGraph graph = UserDefinedGraph.getGraph(session);

            if ( graph != null )
            {
                int seriesNumber = graph.addPerformanceData(perfRunId);
                graph.setSeriesName( seriesNumber, "Performance Run #"+perfRunId );
            }

            response.sendRedirect("default.jsp?page=view_usergraph");
        }
    }
%>
Function: <%=function%>