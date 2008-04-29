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
<%@ page import="org.jboss.dtf.testframework.dtfweb.DTFManagerBean,
				 org.jboss.dtf.testframework.serviceregister.ServiceRegisterInterface,
				 org.jboss.dtf.testframework.testnode.TestNodeInterface,
				 org.jboss.dtf.testframework.testnode.TestNodeDescription,
                 org.jboss.dtf.testframework.testnode.ProductSupportInformation,
                 java.util.Enumeration"%>
<%
	int displayLog = -1;
	String productNameLog = null;
	String function = request.getParameter("function");

	boolean coordinatorRunning = false;

	try
	{
		DTFManagerBean.getCoordinator().isBusy();

		coordinatorRunning = true;
	}
	catch (Throwable e)
	{
		//Ignore
	}

	if ( function != null )
	{
		if ( function.equals("shutdowncoordinator") )
		{
			DTFManagerBean.getCoordinator().shutdown();
			response.sendRedirect("default.jsp?page=nodemanager");
		}
		else
		if ( function.equals("restartcoordinator") )
		{
			DTFManagerBean.getCoordinator().restart();
			response.sendRedirect("default.jsp?page=nodemanager");
		}
		else
		if ( function.equals("displaylog") )
		{
			productNameLog = request.getParameter("productname");
			displayLog = Integer.parseInt( request.getParameter("node") );
		}
		else
		if ( function.equals("restartall") )
		{
			try
			{
            	DTFManagerBean.restartAllNodes();

				response.sendRedirect("default.jsp?page=nodemanager");
			}
			catch (Exception e)
			{
			}
		}
		else
		if ( function.equals("restartnode") )
		{
			short nodeId = Short.parseShort( request.getParameter("nodeid") );

			DTFManagerBean.restartNode(nodeId,true,false);

			response.sendRedirect("default.jsp?page=nodemanager");
		}
		else
		if ( function.equals("shutdownnode") )
		{
			boolean onComplete = request.getParameter("oncomplete") != null;
			short nodeId = Short.parseShort( request.getParameter("nodeid") );

			DTFManagerBean.restartNode(nodeId,false,onComplete);

			response.sendRedirect("default.jsp?page=nodemanager");
		}
	}
%>
<p><font face="Verdana" size="2"><a href="default.jsp?page=nodemanager&function=restartall">restart all nodes</a> <% if ( coordinatorRunning ) { %> | <a href="default.jsp?page=nodemanager&function=restartcoordinator">restart coordinator</a> | <a href="default.jsp?page=nodemanager&function=shutdowncoordinator">shutdown coordinator</a> <% } %></font></p>

<%
	ServiceRegisterInterface serviceRegistry = DTFManagerBean.getServieRegistry();

	if ( serviceRegistry != null )
	{
		TestNodeInterface[] node = serviceRegistry.getRegister();

		for (int count=0;count<node.length;count++)
		{
			try
			{
				String[] activeTaskList = node[count].getActiveTaskList();
				TestNodeDescription descr = node[count].getNodeDescription();
%>
<p><font face="Verdana" size="2">Node: </font><b><font face="Courier New" size="2"><%=node[count].getName()%></font></b><br/>
<font face="Verdana" size="2">Options:</font> <b><a href="default.jsp?page=nodemanager&function=restartnode&nodeid=<%=descr.getServiceId()%>"><code>restart</code></a></b> | <b><a href="default.jsp?page=nodemanager&function=shutdownnode&nodeid=<%=descr.getServiceId()%>"><code>shutdown now</code></a></b>
| <b><a href="default.jsp?page=nodemanager&function=shutdownnode&oncomplete=yes&nodeid=<%=descr.getServiceId()%>"><code>shutdown on completion</code></a></b><br/>
<font face="Verdana" size="2">OS:</font> <b><code><%=descr.getOSID()%></code></b><br/>
<font face="Verdana" size="2">Hostname:</font> <b><code><%=node[count].getHostAddress()%></code></b><br/>
<font face="Verdana" size="2">Products:</font> <code> <table cellspacing="3" cellpadding="3"> <tr>
<%
        ProductSupportInformation productSupportInfo = descr.getProductSupportInformation();
        Enumeration productEnum = productSupportInfo.getProductEnumeration();
		int productCount = 0;
		while (productEnum.hasMoreElements())
		{
            String productName = (String)productEnum.nextElement();
            boolean supported = productSupportInfo.isProductSupported(productName);

			if ( ( productCount++ % 5 ) == 0 )
			{
%>
				</tr><tr>
<%
			}
%>
 			<td bgcolor="<%=supported ? "blue" : "gray"%>"><a href="default.jsp?page=nodemanager&function=displaylog&productname=<%=productName%>&node=<%=descr.getServiceId()%>"><font color="white"><b><%=supported ? "" : "<em>"%><%=productName%><%=supported ? "" : "</em>"%></b></font></a></td>
<%
		}
%>
</tr></table></code> </p>
<%
		if ( displayLog == descr.getServiceId() )
		{
			String logURL = "servlet/org.jboss.dtf.testframework.dtfweb.ShowLogOutput?node="+displayLog+"&product="+productNameLog;
%>
<table border="0" cellpadding="0" cellspacing="0" style="border-collapse: collapse" bordercolor="#111111" width="100%">
  <tr>
    <td bgcolor="#D7D7FF"><b><font face="Verdana" size="2">Deploy log for <%=productNameLog%></font></b></td>
  </tr>
  <tr>
    <td bgcolor="#FFFCE4"><b><font face="Verdana" size="1">Output:</font></b></td>
  </tr>
  <tr>
  	<td bgcolor="#FFFCE4">
	  <PRE><jsp:include page="<%=logURL%>" flush="true"/></PRE>
	</td>
  </tr>
  <tr>
    <td bgcolor="#FFFCE4"><b><font face="Verdana" size="1">Error:</font></b></td>
  </tr>
  <tr>
  	<td bgcolor="#FFFCE4">
	  <% logURL += "&error=true"; %>
	  <PRE><jsp:include page="<%=logURL%>" flush="true"/></PRE>
	</td>
  </tr>
</table>
<%
		}
%>
<table border="0" cellpadding="0" cellspacing="0" style="border-collapse: collapse" bordercolor="#111111" width="100%">
  <tr>
    <td bgcolor="#D7D7FF"><b><font face="Verdana" size="2">Task details</font></b></td>
  </tr>
<%

				for (int taskCount=0;taskCount<activeTaskList.length;taskCount++)
				{
%>
  <tr>
    <td><%=activeTaskList[taskCount]%></td>
  </tr>
<%
				}
			}
			catch (Exception e)
			{     %>
				<%=e.toString()%><%
			}
%>
</table>
<%
		}
	}
%>
