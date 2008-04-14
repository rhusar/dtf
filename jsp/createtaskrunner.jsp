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
<%@ page import="org.jboss.dtf.testframework.productrepository.TaskRunnerConfiguration,
				 org.jboss.dtf.testframework.productrepository.ProductConfiguration"%>
<%
    TaskRunnerConfiguration taskRunner = (TaskRunnerConfiguration)session.getAttribute("new-task-runner");
	ProductConfiguration productConfig = (ProductConfiguration)session.getAttribute("productconfiguration");
	String editName = request.getParameter("edit");
    String taskRunnerName = request.getParameter("taskrunnername");

	if ( editName != null )
	{
		taskRunner = productConfig.getTaskRunnerConfiguration(editName);

		session.setAttribute("new-task-runner", taskRunner);
	}

	if ( taskRunnerName != null )
	{
		taskRunner = new TaskRunnerConfiguration();
		taskRunner.setName(taskRunnerName);

		session.setAttribute("new-task-runner", taskRunner);
	}

	String classname = request.getParameter("classname");
	String logTo = request.getParameter("logto");

	if ( classname != null )
	{
		taskRunner.setClassname(classname);
	}

	if ( logTo != null )
	{
		taskRunner.setLogTo(logTo);
	}

	if ( request.getParameter("add") != null )
	{
		String parameterName = request.getParameter("newparametername");
		String parameterValue = request.getParameter("newparametervalue");

		taskRunner.setParameter(parameterName, parameterValue);
	}
	else
	if ( request.getParameter("delete") != null )
	{
		String selectedParameter = request.getParameter("selected-parameter");

		taskRunner.deleteParameter(selectedParameter);
	}
	else
	if ( request.getParameter("finished") != null )
	{
		productConfig.addTaskRunnerConfiguration(taskRunner.getName(), taskRunner);
		response.sendRedirect("default.jsp?page=productconfig");
	}
%>
<form method="post" action="default.jsp?page=createtaskrunner">
	<table width="75%" align="center" bgcolor="#D1D2FF" border="1" cellspacing="0">
		<tr>
			<td>
				<table width="100%">
					<tr>
						<td>
							Task Runner Name:
						</td>
						<td>
							<%=taskRunner.getName()%>
						</td>
					</tr>
					<tr>
						<td>
							Classname:
						</td>
						<td>
							<input type="text" name="classname" value="<%=taskRunner.getClassname()%>"/>
						</td>
					</tr>
					<tr>
						<td>
							Log to URL:
						</td>
						<td>
							<input type="text" name="logto" value="<%=taskRunner.getLogTo()%>"/>
						</td>
					</tr>
				</table>
			</td>
		</tr>
	</table>
	<br/>
	<table width="75%" align="center" bgcolor="#D1D2FF" border="1" cellspacing="0">
		<tr>
			<td>
				<table width="100%">
					<tr>
						<td>
							Parameters:
						</td>
					</tr>
					<tr>
						<td>
							<table>
								<tr>
									<td>
                                        <input type="submit" name="delete" value="delete"/>
									</td>
									<td>
										<code>name</code>
									</td>
									<td>
										<code>value</code>
									</td>
								</tr>
<%
	String[] parameterNames = taskRunner.getParameterNames();

	for (int count=0;count<parameterNames.length;count++)
	{
%>
								<tr>
									<td align="center">
										<input type="radio" name="selected-parameter" value="<%=parameterNames[count]%>"/>
									</td>
									<td>
										<code><%=parameterNames[count]%></code>
									</td>
									<td width="100%">
										<code><%=taskRunner.getParameter(parameterNames[count])%></code>
									</td>
								</tr>
<%
	}
%>
								<tr>
									<td align="center">
										<input type="radio" name="selected-parameter" disabled/>
									</td>
									<td>
										<input type="text" name="newparametername" size="16"/>
									</td>
									<td>
										<input type="text" name="newparametervalue" size="24"/> <input type="submit" name="add" value="add"/>
									</td>
								</tr>
							</table>
						</td>
					</tr>
					<tr>
						<td>
						 	<input type="submit" name="finished" value="finished"/>
						</td>
					</tr>
				</table>
			</td>
		</tr>
	</table>
</form>