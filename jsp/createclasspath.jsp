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
				 java.util.ArrayList"%>
<%
	ProductConfiguration productConfig = (ProductConfiguration)session.getAttribute("productconfiguration");
	String classpathToEdit = request.getParameter("toedit");
	String classpathName = (String)session.getAttribute("newclasspath-name");
	ArrayList classpath = (ArrayList)session.getAttribute("newclasspath");

	if ( classpathToEdit != null )
	{
		classpath = productConfig.getClasspathList(classpathToEdit);
		classpathName = classpathToEdit;
		session.setAttribute("newclasspath-name", classpathName);
		session.setAttribute("originalclasspath-name", classpathName);
		session.setAttribute("newclasspath", classpath);

		if ( classpath == null )
		{
			classpath = new ArrayList();
			session.setAttribute("newclasspath", classpath);
		}
	}

	if ( classpath == null )
	{
		classpath = new ArrayList();
		session.setAttribute("newclasspath", classpath);
		session.setAttribute("originalclasspath-name", "");
	}

	String selectedClasspathElementStr = request.getParameter("selectedclasspathelement");
	int selectedClasspathElement = -1;

	if ( selectedClasspathElementStr != null )
	{
		selectedClasspathElement = Integer.parseInt( selectedClasspathElementStr );
	}

	if ( request.getParameter("name") != null )
	{
		classpathName = request.getParameter("name");
	}

	if ( classpathName == null )
	{
		classpathName = "";
	}

	String editElement = request.getParameter("edit");
	int elementToEdit = -1;
	if ( editElement != null )
	{
		elementToEdit = Integer.parseInt(editElement);
	}

	String editedElementIndex = request.getParameter("editedelementindex");

	if ( editedElementIndex != null )
	{
		int index = Integer.parseInt(editedElementIndex);
		classpath.set(index, request.getParameter("editedelement"));
	}

	if ( request.getParameter("add") != null )
	{
		String newElement = request.getParameter("newclasspathelement");
		if ( selectedClasspathElement == -1 )
		{
			classpath.add(newElement.trim());
		}
		else
		{
			classpath.add(selectedClasspathElement, newElement.trim());
		}
	}
	else
	if ( ( request.getParameter("delete") != null ) && ( selectedClasspathElement != -1 ) )
	{
		classpath.remove( selectedClasspathElement );
	}
	else
	if ( request.getParameter("finished") != null )
	{
		String oldName = (String)session.getAttribute("originalclasspath-name");
		// If the classpath name has changed remove the old one first
		if ( ( oldName != null ) && ( !oldName.equals( classpathName )) )
		{
			productConfig.deleteClasspath(oldName);
		}

		/** Store the classpath in the product configuration **/
		productConfig.setClasspath(classpathName, classpath, false);

		/** Clear the session attributes **/
		session.removeAttribute("newclasspath");
		session.removeAttribute("newclasspath-name");

		response.sendRedirect("default.jsp?page=productconfig");
	}
%>
<form action="default.jsp?page=createclasspath" method="post">
<table width="75%" align="center" border="1" bgcolor="#D1D2FF" cellspacing="0">
	<tr>
		<td>
			<table width="100%" bgcolor="#D1D2FF" border="0" align="center">
				<tr>
					<td>
						Name:
					</td>
					<td>
						<input type="text" name="name" value="<%=classpathName%>"  size="32"/> <input type="submit" value="set"/>
					</td>
				</tr>
			</table>
		</td>
	</tr>
</table>
<br/>
<table width="75%" border="1" align="center" bgcolor="#D1D2FF" cellspacing="0">
	<tr>
		<td>
			<table width="100%" bgcolor="#D1D2FF" border="0" align="center">
				<tr>
					<td>
						<input type="submit" name="delete" value="delete"/>
					</td>
					<td width="100%">
						&nbsp;
					</td>
				</tr>

			<%
				for (int count=0;count<classpath.size();count++)
				{
			%>
				<tr>
					<td align="center">
						<input type="radio" name="selectedclasspathelement" value="<%=count%>"/>
					</td>
					<td width="100%" <%=(count == elementToEdit) ? "background=\"gray\"":""%>>
					<%
						if ( count == elementToEdit )
						{
					%>
						<input type="text" size="64" name="editedelement" value="<%=(String)classpath.get(count)%>"/>
						<input type="hidden" name="editedelementindex" value="<%=count%>"/>
					<%
						}
						else
						{
					%>
						<a href="default.jsp?page=createclasspath&edit=<%=count%>"><%=(String)classpath.get(count)%></a>
					<%
						}
					%>
					</td>
				</tr>
			<%
				}
			%>
				<tr>
					<td align="center">
						<input type="radio" name="classpathelement" disabled/>
					</td>
					<td>
						<input type="text" name="newclasspathelement" size="64"/> <input type="submit" name="add" value="add"/>
					</td>
				</tr>
				<tr>
					<td align="center">
						&nbsp;
					</td>
					<td>
						<input type="submit" name="finished" value="finished"/>
					</td>
				</tr>
			</table>
		</td>
	</tr>
</table>
</form>