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
<%@ page import="org.jboss.dtf.testframework.productrepository.NodeConfiguration,
				 org.jboss.dtf.testframework.productrepository.ProductConfiguration,
				 java.util.Hashtable,
				 java.util.Enumeration,
				 org.jboss.dtf.testframework.dtfweb.DTFManagerBean,
				 org.jboss.dtf.testframework.testnode.TestNodeInterface,
				 org.jboss.dtf.testframework.serviceregister.ServiceNotFound"%>
<%
	ProductConfiguration productConfig = (ProductConfiguration)session.getAttribute("productconfiguration");
	NodeConfiguration nodeConfig = (NodeConfiguration)session.getAttribute("nodeconfig");
	String osId = request.getParameter("osid");

	if ( request.getParameter("toedit") != null )
	{
		String toEdit = request.getParameter("toedit");

		nodeConfig = productConfig.getNodeConfiguration(toEdit);
		session.setAttribute("nodeconfig", nodeConfig);
	}

	if ( nodeConfig == null )
	{
		nodeConfig = new NodeConfiguration();
		session.setAttribute("nodeconfig", nodeConfig);
	}

	if ( osId != null )
	{
		nodeConfig.setOs( osId );
	}

	if ( osId == null )
	{
		osId = nodeConfig.getOs();
		osId = ( osId != null ? osId : "" );
	}

	if ( request.getParameter("defaultclasspath") != null )
	{
		nodeConfig.setDefaultClasspath( request.getParameter("defaultclasspath") );
	}

	String action = request.getParameter("action");

	if ( action != null )
	{
		if ( action.equals("jvm") )
		{
			String jvmId = request.getParameter("jvmid");

			nodeConfig.setJvmId(jvmId);
		}
		else
		if ( action.equals("exclusions") )
		{
            if ( request.getParameter("exclude") != null )
			{
				String nodeName = request.getParameter("nodename");

				nodeConfig.excludedNode(nodeName);
			}
			else
			if ( request.getParameter("delete") != null )
			{
                String selected = request.getParameter("selectedexclusion");

				nodeConfig.removeExclusion(selected);
			}
		}
		else
		if ( action.equals("properties") )
		{
			String editPropertyName = request.getParameter("editpropertyname");

			if ( editPropertyName != null)
			{
				String editPropertyValue = request.getParameter("editpropertyvalue");
				nodeConfig.setProperty(editPropertyName, editPropertyValue);
			}

			if ( request.getParameter("add") != null )
			{
				String propertyName = request.getParameter("propertyname");
				String propertyValue = request.getParameter("propertyvalue");

				if ( ( propertyName.length() > 0 ) && ( propertyValue.length() > 0 ) )
				{
					nodeConfig.setProperty(propertyName, propertyValue);
				}
			}
			else
			if ( request.getParameter("deleteproperty") != null )
			{
				String selectedProperty = request.getParameter("selectedproperty");

				System.out.println("SelectedProperty = "+selectedProperty);
				if ( selectedProperty != null )
				{
					nodeConfig.deleteProperty( selectedProperty );
				}
			}
		}
		else
		if ( action.equals("sets") )
		{
			String editSetName = request.getParameter("editsetname");

			if ( editSetName != null)
			{
				String editSetValue = request.getParameter("editsetvalue");
				nodeConfig.set(editSetName, editSetValue);
			}

			if ( request.getParameter("add") != null )
			{
				String setName = request.getParameter("setname");
				String setValue = request.getParameter("setvalue");

				if ( ( setName.length() > 0 ) && ( setValue.length() > 0 ) )
				{
					nodeConfig.set(setName, setValue);
				}
			}
			else
			if ( request.getParameter("deleteset") != null )
			{
				String selectedSet = request.getParameter("selectedset");

				System.out.println("SelectedSet = "+selectedSet);
				if ( selectedSet != null )
				{
					nodeConfig.deleteSet( selectedSet );
				}
			}
			else
			if ( request.getParameter("finished") != null )
			{
				if ( nodeConfig.getOs() != null )
				{
					String oldId = nodeConfig.getOldOSId();
					// If the os id. has changed remove the old one first
					if ( oldId != null )
					{
						productConfig.deleteNodeConfig(oldId);
					}

					productConfig.setNodeConfiguration(nodeConfig.getOs(), nodeConfig);
					session.removeAttribute("nodeconfig");
					response.sendRedirect("default.jsp?page=productconfig");
				}
			}
		}
	}
%>
<form method="post" action="default.jsp?page=createnodeconfig">
	<table width="75%" align="center" bgcolor="#D1D2FF" border="1" cellspacing="0">
		<tr>
			<td>
				<table width="100%">
					<tr>
						<td>
							Operating System:
						</td>
						<td>
							<input type="text" name="osid" value="<%=nodeConfig.getOs() != null ? nodeConfig.getOs() : ""%>"/>
						</td>
					</tr>
					<tr>
						<td>
							Default Classpath:
						</td>
						<td>
							<select name="defaultclasspath">
							<%
								String[] classpathNames = productConfig.getClasspathNames();

								for (int count=0;count<classpathNames.length;count++)
								{
							%>
									<option <%=classpathNames[count].equals(nodeConfig.getDefaultClasspath()) ? "selected" : ""%>><%=classpathNames[count]%></option>
							<%
								}
							%>
							</select>
							 <input type="submit" name="set" value="set"/>
						</td>
					</tr>
				</table>
			</td>
		</tr>
	</table>
</form>
<br>
<form method="post" action="default.jsp?page=createnodeconfig&action=properties">
	<table width="75%" align="center" bgcolor="#D1D2FF" border="1" cellspacing="0">
		<tr>
			<td>
				<table width="100%">
					<tr>
						<td>
							Properties:
						</td>
					</tr>
					<tr>
						<td>
							<table width="100%">
								<tr>
									<td>
										<input type="submit" name="deleteproperty" value="delete"/>
									</td>
									<td>
										&nbsp;
									</td>
								</tr>
							<%
								Hashtable properties = nodeConfig.getProperties();
                                String editProperty = request.getParameter("editproperty");
								Enumeration propertiesEnum = properties.keys();

								while ( propertiesEnum.hasMoreElements() )
								{
									String propertyName = (String)propertiesEnum.nextElement();
									String propertyValue = (String)properties.get(propertyName);
							%>
								<tr>
									<td>
										<input type="radio" name="selectedproperty" value="<%=propertyName%>"/>
									</td>
									<td>
							<%
										if ( ( editProperty != null ) && ( editProperty.equals(propertyName) ) )
										{
							%>
											<%=propertyName%> = <input type="text" size="24" name="editpropertyvalue" value="<%=propertyValue%>"/>
											<input type="hidden" name="editpropertyname" value="<%=propertyName%>"/>
							<%
										}
										else
										{
							%>
											<a href="default.jsp?page=createnodeconfig&action=properties&editproperty=<%=propertyName%>"><%=propertyName%> = <%=propertyValue%></a>
							<%
										}
							%>
									</td>
								</tr>
							<%
								}
							%>
								<tr>
									<td>
										<input type="radio" disabled/>
									</td>
									<td>
										<input type="text" name="propertyname" size="24"/> = <input type="text" name="propertyvalue" size="24"/> <input type="submit" name="add" value="add"/>
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
<br>
<form method="post" action="default.jsp?page=createnodeconfig&action=jvm">
	<table width="75%" align="center" border="1" bgcolor="#D1D2FF" cellspacing="0">
		<tr>
			<td>
				<table width="100%">
					<tr>
						<td>
							Java Virtual Machine:
						</td>
						<td>
							<input type="text" name="jvmid" value="<%=nodeConfig.getJvmId()%>"/> <input type="submit" name="set" value="set"/>
						</td>
					</tr>
				</table>
			</td>
		</tr>
	</table>
</form>
<br/>
<form method="post" action="default.jsp?page=createnodeconfig&action=exclusions">
	<table width="75%" align="center" border="1" bgcolor="#D1D2FF" cellspacing="0">
		<tr>
			<td>
				<table width="100%">
					<tr>
						<td>
							Exclusions:
						</td>
					</tr>
					<tr>
						<td>
                            <table width="100%">
								<tr>
									<td>
										<input type="submit" name="delete" value="delete"/>
									</td>
									<td>
										&nbsp;
									</td>
								</tr>
<%
	String[] exclusions = nodeConfig.getExclusions();

	for (int count=0;count<exclusions.length;count++)
	{
%>
								<tr>
									<td align="center">
										<input type="radio" name="selectedexclusion" value="<%=exclusions[count]%>"/>
									</td>
									<td>
                                    	<%=exclusions[count]%>
									</td>
								</tr>
<%
	}
%>
								<tr>
									<td align="center">
										<input type="radio" name="exclusions" disabled/>
									</td>
									<td>
<%
	boolean disableButton = false;

	if ( osId != null )
	{
		try
		{
			TestNodeInterface[] tni = DTFManagerBean.getServieRegistry().lookupService(osId, productConfig.getName());

%>
										<select name="nodename">
<%
			for (int count=0;count<tni.length;count++)
			{
%>
											<option name="<%=tni[count].getName()%>"><%=tni[count].getName()%></option>
<%
			}
%>
										</select>
<%
		}
		catch (ServiceNotFound e)
		{
			disableButton = true;
%>
			<code>No nodes found</code>
<%
		}
	}
%>
										<input type="submit" name="exclude" <%=disableButton ? "disabled" : ""%> value="exclude"/>
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
<br/>
<form method="post" action="default.jsp?page=createnodeconfig&action=sets">
	<table width="75%" align="center" border="1" bgcolor="#D1D2FF" cellspacing="0">
		<tr>
			<td>
				<table width="100%">
					<tr>
						<td>
							Sets:
						</td>
					</tr>
					<tr>
						<td>
							<table width="100%">
								<tr>
									<td>
										<input type="submit" name="deleteset" value="delete"/>
									</td>
									<td>
										&nbsp;
									</td>
								</tr>
							<%
								Hashtable sets = nodeConfig.getSets();
                                String editSet= request.getParameter("editset");
								Enumeration setsEnum = sets.keys();

								while ( setsEnum.hasMoreElements() )
								{
									String setName = (String)setsEnum.nextElement();
									String setValue = (String)sets.get(setName);
							%>
								<tr>
									<td>
										<input type="radio" name="selectedset" value="<%=setName%>"/>
									</td>
									<td>
							<%
										if ( ( editSet != null ) && ( editSet.equals(setName) ) )
										{
							%>
											<%=setName%> = <input type="text" size="24" name="editsetvalue" value="<%=setValue%>"/>
											<input type="hidden" name="editsetname" value="<%=setName%>"/>
							<%
										}
										else
										{
							%>
											<a href="default.jsp?page=createnodeconfig&action=properties&editset=<%=setName%>"><%=setName%> = <%=setValue%></a>
							<%
										}
							%>
									</td>
								</tr>
							<%
								}
							%>
								<tr>
									<td>
										<input type="radio" disabled/>
									</td>
									<td>
										<input type="text" name="setname" size="24"/> = <input type="text" name="setvalue" size="24"/> <input type="submit" name="add" value="add"/>
									</td>
								</tr>
							</table>
						</td>
					</tr>
				</table>
			</td>
		</tr>
	</table>
	<table width="75%" align="center" border="0">
		<tr>
			<td>
				<input type="submit" name="finished" value="finished"/>
			</td>
		</tr>
	</table>
</form>