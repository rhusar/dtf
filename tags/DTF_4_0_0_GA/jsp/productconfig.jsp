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
<%@ page import="org.jboss.dtf.testframework.productrepository.ProductConfiguration,
				 java.rmi.registry.LocateRegistry,
				 java.rmi.registry.Registry,
				 org.jboss.dtf.testframework.dtfweb.DTFManagerBean,
				 java.rmi.RemoteException"%>
<%
	String action = request.getParameter("action");
	ProductConfiguration productConfig = (ProductConfiguration)session.getAttribute("productconfiguration");

	if ( ( action != null) && ( action.equals("create") ) )
	{
		productConfig = new ProductConfiguration();
		session.setAttribute("productconfiguration", productConfig);
	}

	if ( productConfig == null )
	{
		String editProductName = request.getParameter("editproductname");

		if ( editProductName != null )
		{
			productConfig = dtfManager.getProductRepository().getProductConfiguration( editProductName );
			session.setAttribute("productconfiguration", productConfig);
		}
	}

	String productName = ( productConfig != null ? productConfig.getName() : null );
	String permutationId = ( productConfig != null ? productConfig.getPermutationId() : null );
	productName = ( productName != null ? productName : "" );
	permutationId = ( permutationId != null ? permutationId : "" );

	if ( action != null )
	{
		if ( action.equals("taskrunnerconfig") )
		{
			String selectedTaskRunner = request.getParameter("taskrunnerconfig");

			if ( request.getParameter("finished") != null )
			{
				try
				{
					dtfManager.getProductRepository().setProductConfiguration(productConfig.getName(), productConfig);
					session.removeAttribute("productconfiguration");
					response.sendRedirect("default.jsp?page=deployment");
				}
				catch (RemoteException e)
				{
					e.printStackTrace(System.err);
					response.sendRedirect("default.jsp?page=productconfig&error=failedtoset");
				}
			}
			else
			if ( request.getParameter("create") != null )
			{
				String name = request.getParameter("newtaskrunnername");

				if ( name != null )
				{
					response.sendRedirect("default.jsp?page=createtaskrunner&taskrunnername="+name);
				}
			}
			else
			if ( request.getParameter("edit") != null )
			{
				response.sendRedirect("default.jsp?page=createtaskrunner&edit="+selectedTaskRunner);
			}
			else
			if ( request.getParameter("delete") != null )
			{
				productConfig.deleteTaskRunner(selectedTaskRunner);
			}
		}
		else
		if ( action.equals("setproductname") )
		{
			productName = request.getParameter("productname");
			permutationId = request.getParameter("permutationid");
            productConfig.setName( productName );
			productConfig.setPermutationId( permutationId );
		}
		else
		if ( action.equals("classpath") )
		{
			String selected = request.getParameter("classpath");

			if ( request.getParameter("create") != null )
			{
				response.sendRedirect("default.jsp?page=createclasspath");
			}
			else
			if ( ( request.getParameter("edit") != null ) && ( selected != null ) )
			{
				response.sendRedirect("default.jsp?page=createclasspath&toedit="+selected);
			}
			else
			if ( ( request.getParameter("delete") != null ) && ( selected != null ) )
			{
				productConfig.deleteClasspath(selected);
			}
		}
		else
		if ( action.equals("nodeconfig") )
		{
			String selected = request.getParameter("nodeconfig");

			if ( request.getParameter("create") != null )
			{
				response.sendRedirect("default.jsp?page=createnodeconfig");
			}
			else
			if ( ( request.getParameter("edit") != null ) && ( selected != null ) )
			{
				response.sendRedirect("default.jsp?page=createnodeconfig&toedit="+selected);
			}
			else
			if ( ( request.getParameter("delete") != null ) && ( selected != null ) )
			{
				productConfig.deleteNodeConfig(selected);
			}
			else
			if ( ( request.getParameter("copy") != null ) && ( selected != null ) )
			{
				productConfig.createCopy(selected);
			}
		}
	}
%>

<form action="default.jsp?page=productconfig&action=setproductname" method="post">
	<table width="75%" align="center" border="1" cellspacing="0" bordercolor="black">
		<tr>
			<td>
				<table width="100%">
					<tr>
						<td>
							Product Name:
						</td>
						<td>
							<input type="name" name="productname" size="32" value="<%=productName%>"/>
						</td>
					</tr>
					<tr>
						<td>
							Permutation Id:
						</td>
						<td>
							<input type="name" name="permutationid" size="32" value="<%=permutationId%>"/> <input type="submit" value="set"/>
						</td>
					</tr>
				</table>
			</td>
		</tr>
	</table>
</form>
<br/>
<form action="default.jsp?page=productconfig&action=classpath" method="post">
	<table width="75%" align="center" border="1" cellspacing="0" bordercolor="black">
		<tr>
			<td>
				<table width="100%">
					<tr>
						<td>
							Classpaths:
						</td>
						<td>
							<select name="classpath">
							<%
								String[] classpathNames = productConfig.getClasspathNames();

                                if ( classpathNames != null )
                                {
                                    for (int count=0;count<classpathNames.length;count++)
                                    {
							%>
									<option><%=classpathNames[count]%></option>
							<%
                                    }
                                }
							%>
							</select> <input type="submit" name="edit" <%=classpathNames.length > 0 ? "" : "disabled"%> value="edit"/>
							<input type="submit" name="create" value="create">
							<input type="submit" name="delete" <%=classpathNames.length > 0 ? "" : "disabled"%> value="delete">
						</td>
					</tr>
				</table>
			</td>
		</tr>
	</table>
</form>
<br/>
<form action="default.jsp?page=productconfig&action=nodeconfig" method="post">
	<table width="75%" align="center" border="1" cellspacing="0" bordercolor="black">
		<tr>
			<td>
				<table width="100%">
					<tr>
						<td>
							Node Configurations:
						</td>
						<td>
							<select name="nodeconfig">
							<%
								String[] nodeConfigNames = productConfig.getNodeConfigurations();

								for (int count=0;count<nodeConfigNames.length;count++)
								{
							%>
									<option><%=nodeConfigNames[count]%></option>
							<%
								}
							%>
							</select> <input type="submit" name="edit" <%=nodeConfigNames.length > 0 ? "" : "disabled"%> value="edit"/>
							<input type="submit" name="create" <%=productConfig.getClasspathNames().length > 0 ? "" : "disabled"%> value="create">
							<input type="submit" name="copy" <%=nodeConfigNames.length > 0 ? "" : "disabled"%> value="copy">
							<input type="submit" name="delete" <%=nodeConfigNames.length > 0 ? "" : "disabled"%> value="delete">
						</td>
					</tr>
				</table>
			</td>
		</tr>
	</table>
</form>
<br/>
<form action="default.jsp?page=productconfig&action=taskrunnerconfig" method="post">
	<table width="75%" align="center" border="1" cellspacing="0" bordercolor="black">
		<tr>
			<td>
				<table width="100%">
					<tr>
						<td>
							Task Runner Configurations:
						</td>
						<td>
							<select name="taskrunnerconfig">
							<%
								String[] taskRunnerConfigs = productConfig.getTaskRunnerConfigurations();

								for (int count=0;count<taskRunnerConfigs.length;count++)
								{
							%>
									<option><%=taskRunnerConfigs[count]%></option>
							<%
								}
							%>
							</select> <input type="submit" name="edit" <%=taskRunnerConfigs.length > 0 ? "" : "disabled"%> value="edit"/>
							<input type="submit" name="delete" <%=nodeConfigNames.length > 0 ? "" : "disabled"%> value="delete">
							| <input type="text" name="newtaskrunnername" size="16"> <input type="submit" name="create" value="create">
						</td>
					</tr>
				</table>
			</td>
		</tr>
	</table>

	<table width="75%" align="center" border="0">
		<tr>
			<td align="right">
            	<input type="submit" name="finished" value="finished"/>
			</td>
		</tr>
	</table>
</form>