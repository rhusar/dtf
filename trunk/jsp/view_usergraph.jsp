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
                 org.jboss.dtf.testframework.dtfweb.performance.UserDefinedGraph,
                 org.jboss.dtf.testframework.dtfweb.performance.GraphDataServlet"%>
<%
    String widthStr = request.getParameter("width");
    String heightStr = request.getParameter("height");

    UserDefinedGraph userGraph = UserDefinedGraph.getGraph(session);
    int width = widthStr != null ? Integer.parseInt( widthStr ) : 600;
    int height = heightStr != null ? Integer.parseInt( heightStr ) : 400;
    String action = request.getParameter("action");

    if ( ( action != null ) && ( action.equals("delete_series") ) && ( request.getParameter("del_series") != null ) )
    {
        int delSeries = Integer.parseInt( request.getParameter("del_series") );
        userGraph.deleteSeries(delSeries);
    }

    if ( ( action != null ) && ( action.equals("view_saved_graph") ) )
    {
        String name = request.getParameter("name");
        System.out.println("Loading saved graph '"+name+"'");

        if ( name != null )
        {
            userGraph = UserDefinedGraph.load( name );
            userGraph.setGraph(session);
            System.out.println("Loaded saved graph ='"+userGraph+"'");
        }
    }

    if ( request.getParameter("series_to_edit") != null )
    {
        int seriesToEdit = Integer.parseInt( request.getParameter("series_to_edit") );

        userGraph.setSeriesName( seriesToEdit, request.getParameter("new_name") );
    }

    if ( ( action != null ) && ( action.equals("edit_axis_labels") ) )
    {
        userGraph.setXAxisLabel( request.getParameter("x_axis_label") );
        userGraph.setYAxisLabel( request.getParameter("y_axis_label") );
    }

    if ( ( action != null ) && ( action.equals("save") ) )
    {
        String saveName = request.getParameter("save_name");

        if ( ( saveName != null ) && ( saveName.length() > 0 ) )
        {
            userGraph.save( saveName );

            response.sendRedirect("default.jsp?page=saved_graphs");
        }
    }
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
                        size: <a href="default.jsp?page=view_usergraph&width=800&height=500">lrg</a> | <a href="default.jsp?page=view_usergraph&width=600&height=400">med</a> | <a href="default.jsp?page=view_usergraph&width=400&height=300">sml</a>
                    </font>
                  </td>
                  <td align="right">
                    <font face="Tahoma" size="2">
                        export: <a href="servlet/org.jboss.dtf.testframework.dtfweb.performance.CSVDataServlet?perfrunid=usergraph">csv</a>
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
    <td align="center" width="<%=width%>">

      <table width="<%=width%>" height="<%=height%>" border="0" cellspacing="0" cellpadding="5" bordercolor="black">
        <tr>
          <td align="center">
            <img src="servlet/org.jboss.dtf.testframework.dtfweb.performance.GraphDataServlet?perfrunid=usergraph&width=<%=width%>&height=<%=height%>">
          </td>
        </tr>
      </table>

    </td>
    <td>
      <table border="1" cellspacing="0" bordercolor="black">
        <tr>
          <td>
            <form method="post" action="default.jsp?page=view_usergraph&action=edit_axis_labels">
            <table width="100%">
              <tr>
                <th>
                  <font face="Tahoma" size="2">
                    Axis
                  </font>
                </th>
              </tr>
              <tr>
                <td>
                  <font face="Tahoma" size="2">
                    X: <input type="text" name="x_axis_label" value="<%=userGraph.getXAxisLabel()%>" size="16"/>
                  </font>
                </td>
              </tr>
              <tr>
                <td>
                  <font face="Tahoma" size="2">
                    Y: <input type="text" name="y_axis_label" value="<%=userGraph.getYAxisLabel()%>" size="16"/>
                  </font>
                </td>
              </tr>
              <tr>
                <td>
                  <input type="submit" value="set"/>
                </td>
              </tr>
            </table>
            </form>
          </td>
        </tr>
        <tr>
          <td>
            <table width="100%">
              <tr>
                <th>
                  <font face="Tahoma" size="2">
                    Series
                  </font>
                </th>
              </tr>
<%
    long[] series = userGraph.getPerformanceDataList();
%>
              <form method="post" action="default.jsp?page=view_usergraph&action=delete_series">
                <input type="hidden" name="num_series" value="<%=series.length%>"/>
<%
    String editSeries = request.getParameter("edit_series");
    int seriesToEdit = -1;

    if ( editSeries != null )
    {
        seriesToEdit = Integer.parseInt(editSeries);
    }

    for (int count=0;count<series.length;count++)
    {
%>
              <tr>
                <td>
                  <font face="Tahoma" size="2">
                    <font color="<%=GraphDataServlet.getSeriesColor(count)%>">---</font>

                    <%
                        boolean editThisSeries = (seriesToEdit == count);

                        if ( !editThisSeries )
                        {
                    %>
                    <a href="default.jsp?page=view_usergraph&edit_series=<%=count%>">
                        <%=userGraph.getSeriesName(count)%>
                    </a>
                    <%
                        }
                        else
                        {
                    %>
                    <input type="hidden" name="series_to_edit" value="<%=count%>"/>
                    <input type="text" size="16" name="new_name" value="<%=userGraph.getSeriesName(count)%>"/>
                    <%
                        }
                    %>
                    <input type="radio" name="del_series" value="<%=count%>">
                  </font>
                </td>
              </tr>
<%
    }
%>
              <tr>
                <td>
                  <%
                      if ( seriesToEdit != -1)
                      {
                  %>
                          <input type="submit" value="update" name="update_series_name"/>
                  <%
                      }
                  %>
                  <input type="submit" value="del"/>
                </td>
              </tr>
              </form>
              <form method="post" action="default.jsp?page=view_usergraph&action=save">
              <tr>
                <td>
                  <input type="text" name="save_name" value=""/> <input type="submit" value="save"/>
                </td>
              </tr>
              </form>
            </table>
          </td>
        </tr>
      </table>
    </td>
  </tr>
</table>