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
<%@ page import="org.jboss.dtf.testframework.dtfweb.ArchiveInformation"%>
<%
	String function = request.getParameter("function");
	String archiveId = request.getParameter("archiveid");

	if ( ( function != null ) && ( function.equals("delete") ) && ( archiveId != null ) )
	{
		ArchiveInformation.deleteArchive(Integer.parseInt(archiveId));
	}
%>
 <table width="85%" align="center" cellpadding="3">
	<tr bgcolor="#939393">
		<td>
		 	Date/Time
		</td>
		<td>
			Title
		</td>
		<td>
			Options
		</td>
	</tr>
<%
	ArchiveInformation[] archives = ArchiveInformation.getArchives();

	for (int count=0;count<archives.length;count++)
	{
%>
	<tr>
		<td>
		 	<%=archives[count].getDateTimeCreated()%>
		</td>
		<td>
			<%=archives[count].getTitle()%>
		</td>
		<td>
			<a href="default.jsp?page=viewarchive&archiveid=<%=archives[count].getArchiveId()%>">view</a> | <a href="default.jsp?page=archives&function=delete&archiveid=<%=archives[count].getArchiveId()%>">delete</a>
		</td>
	</tr>
<%
	}
%>
</table>