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
<jsp:useBean id="jdbcConnectionPool" scope="application" class="org.jboss.dtf.testframework.dtfweb.utils.JDBCConnectionPool" />
<jsp:useBean id="dtfManager" class="org.jboss.dtf.testframework.dtfweb.DTFManagerBean" />
<jsp:useBean id="dtfResultsManager" class="org.jboss.dtf.testframework.dtfweb.DTFResultsManager" />

<form method="post" action="update_setup.jsp">
<table width="75%" align="center">
  <tr height="80" valign="bottom">
    <td>
    </td>
    <td>
      <font face="Tahoma" size="2">
        <b>
          Configuration
        </b>
      </font>
    </td>
  </tr>
  <tr>
    <td align="right">
      <font face="Tahoma" size="2">
        Default NameService URI:
      </font>
    </td>
    <td>
      <input type="text" size="64" value="<%=dtfManager.getDefaultNameServiceURI()%>" name="defaultnameserviceuri">
    </td>
  </tr>
  <tr>
    <td align="right">
      <font face="Tahoma" size="2">
        Upload Directory:
      </font>
    </td>
    <td>
      <input type="text" size="64" value="<%=dtfManager.getUploadDirectory()%>" name="uploaddirectory">
    </td>
  </tr>
  <tr>
    <td align="right">
      <font face="Tahoma" size="2">
        Upload Web Directory:
      </font>
    </td>
    <td>
      <input type="text" size="64" value="<%=dtfManager.getUploadWebDirectory()%>" name="uploadwebdirectory">
    </td>
  </tr>
  <tr>
    <td align="right">
      <font face="Tahoma" size="2">
        Root Web URL:
      </font>
    </td>
    <td>
      <input type="text" size="64" value="<%=dtfManager.getRootURL()%>" name="rooturl">
    </td>
  </tr>
  <tr>
    <td align="right">
      &nbsp;
    </td>
    <td>
      <input type="submit" name="update" value="Update Configuration">
    </td>
  </tr>
  <tr height="80" valign="bottom">
    <td width="10">
	  &nbsp;
    </td>
    <td>
      <font face="Tahoma" size="2">
        <b>
          Supported Operating Systems
        </b>
      </font>
    </td>
  </tr>
  <tr>
    <td>
    </td>
    <td>
      <table width="100%">
<%
	org.jboss.dtf.testframework.dtfweb.OSDetails[] OSs = dtfResultsManager.getSupportedOSs();

	for (int count=0;count<OSs.length;count++)
	{
%>
        <tr>
          <td>
            <input type="Checkbox" name="deleteos_<%=count%>" value="<%=OSs[count]._id%>"> <%=OSs[count]._name%>
          </td>
        </tr>
<%
	}
%>
        <input type="hidden" name="number_of_os" value="<%=OSs.length%>">
	  </table>
	  <input type="submit" name="delete_os" value="delete"><br>
	  <input type="text" name="osname"><input type="submit" name="add_os" value="add OS">
	</td>
  </tr>
</table>
</form>