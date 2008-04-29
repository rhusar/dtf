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
<%@ page import="org.jboss.dtf.testframework.productrepository.ProductRepositoryInterface,
                 java.net.URL,
                 org.jboss.dtf.testframework.coordinator2.CoordinatorInterface,
                 org.jboss.dtf.testframework.coordinator2.scheduler.ScheduleWhenPossible,
                 org.jboss.dtf.testframework.coordinator2.DeployInformation,
                 java.net.URLEncoder"%>

<%
    ProductRepositoryInterface pri = dtfManager.getProductRepository();
    CoordinatorInterface coordinator = dtfManager.getCoordinator();

    long versionId = -1;
    boolean success = false;
    boolean updated = false;
	String action = request.getParameter("action");

	if ( ( action != null ) && ( action.equals("deploy") ) )
	{
		String productName = request.getParameter("productname");
		URL productInstallerURL = pri.getProductInstaller(productName);

        if ( coordinator != null )
        {
            coordinator.getScheduler().schedule(coordinator, new ScheduleWhenPossible(null,null,null,null, new DeployInformation(productName, productInstallerURL.toExternalForm())));
        }
	}

    String product = request.getParameter("product");
    String antScriptURL = request.getParameter("antscripturl");

    if ( ( product != null ) && ( antScriptURL != null ) && ( coordinator != null ) )
    {
        updated = true;
        try
        {
            coordinator.getScheduler().schedule(coordinator, new ScheduleWhenPossible(null,null,null,null, new DeployInformation(product, antScriptURL)));
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
        }
    }

	String deleteProduct = request.getParameter("deleteproduct");
	if ( deleteProduct != null )
	{
		pri.remoteProductConfiguration(deleteProduct);
	}
%>

<form action="default.jsp?page=deployment" method="post">

<table width="100%" height="100%">
	<tr>
		<td>
			<table align="center" width="95%" border="1" cellspacing="0" cellpadding="0" bordercolor="black" bgcolor="white">
				<tr>
					<td>
						<table width="100%" border="0" cellspacing="3" cellpadding="0">
							<tr bgcolor="#DFE1FD">
								<td>
									<font face="Tahoma" size="2">
										Product Name
									</font>
								</td>
								<td>
									<font face="Tahoma" size="2">
										Current Version
									</font>
								</td>
								<td>
									<font face="Tahoma" size="2">
										Current Installer
									</font>
								</td>
								<td>
									<font face="Tahoma" size="2">
										Options
									</font>
								</td>
							</tr>
			<%
				try
				{
					String[] productNames = pri.getProductNames();

					for (int count=0;count<productNames.length;count++)
					{
						URL currentInstaller = pri.getProductInstaller(productNames[count]);
						long currentVersion = pri.getCurrentVersion(productNames[count]);
			%>
							<tr>
								<td>
									<font face="Tahoma" size="2">
										<%=productNames[count]%>
									</font>
								</td>
								<td>
									<font face="Tahoma" size="2">
										<%=currentVersion%>
									</font>
								</td>
								<td>
									<font face="Tahoma" size="2">
										<%=currentInstaller != null ? currentInstaller.toExternalForm() : "not defined"%>
									</font>
								</td>
								<td align="center">
									<font face="Tahoma" size="2">
										<% if ( currentInstaller != null ) { %>
											| <a href="default.jsp?page=deployment&action=deploy&productname=<%=URLEncoder.encode(productNames[count])%>">deploy</a>
										<% } %>
										| <a href="default.jsp?page=productconfig&editproductname=<%=URLEncoder.encode(productNames[count])%>">configure</a>
										| <a href="default.jsp?page=deployment&deleteproduct=<%=URLEncoder.encode(productNames[count])%>">remove</a> |
									</font>
								</td>
							</tr>
			<%
					}
				}
				catch (Exception e)
				{
					e.printStackTrace(System.err);
				}
			%>
						</table>
					</td>
				</tr>
			</table>
			<table width="100%" border="0" cellspacing="3" cellpadding="0">
				<tr>
					<td align="right">
						<a href="default.jsp?page=productconfig&action=create">create product configuration</a>
					</td>
				</tr>
			</table>
			<br>
			<table border="1" bgcolor="white" align="center" cellspacing="0" cellpadding="5" bordercolor="black">
				<tr>
					<td>
						<table valign="center">
							<tr>
								<td>
									<font face="Tahoma" size="2">
										Product:
									</font>
								</td>
								<td>
									<select name="product">
						<%
							if ( pri != null )
							{
								String[] supportedProducts = pri.getProductNames();

								for (int count=0;count<supportedProducts.length;count++)
								{
						%>
										<option value="<%=supportedProducts[count]%>"><%=supportedProducts[count]%></option>
						<%
								}
							}
							else
							{
						%>
										<option value="##error##">ERROR - Product Repository not found</option>
						<%
							}
						%>
									</select>
								</td>
							</tr>
							<tr>
								<td>
									<font face="Tahoma" size="2">
										ANT Install Script URL:
									</font>
								</td>
								<td>
									<input name="antscripturl" size="32" type="text">
								</td>
							</tr>

							<tr>
								<td>
									&nbsp;
								</td>
								<td>
									<input name="set" value="set" type="submit">
								</td>
							</tr>

						</table>
					</td>
				</tr>
			</table>
		</td>
	</tr>
</table>

</form>