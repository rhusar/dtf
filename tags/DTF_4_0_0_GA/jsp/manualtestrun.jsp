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
<jsp:useBean id="dtfRunManager" scope="session" class="org.jboss.dtf.testframework.dtfweb.DTFRunManager" />

<br>
<form method="post" enctype="multipart/form-data" action="start_testrun.jsp">
<table align="center" width="50%" border="1" bordercolor="black" cellspacing="0">
<tr>
<td>
<table width="100%" border="0">
  <tr>
    <td bgcolor="#1040A0">
      <font face="Tahoma" size="2" color="white">
        <b>Initiate Manual Test Run</b>
      </font>
    </td>
  </tr>
<%
  String error = request.getParameter("error");

  if ( (error!=null) && (error.equalsIgnoreCase("invalid")) )
  {
%>
  <tr>
    <td bgcolor="#F04010">
      <font face="Tahoma" size="2" color="white">
        <b>error in details</b>
      </font>
    </td>
  </tr>
<%
  }
  else
  if ( (error!=null) && (error.equalsIgnoreCase("unable")) )
  {
%>
  <tr>
    <td bgcolor="#F04010">
      <font face="Tahoma" size="2" color="white">
        <b>unable to start test run</b>
      </font>
    </td>
  </tr>
<%
  }
%>

  <tr>
    <td>
      <table width="100%">
        <tr>
          <td height="30" width="30%">
	        <font face="Tahoma" size="2">
	          Software Version:
	        </font>
  	      </td>
  	      <td>
  	        <input type="text" size="32" value="" name="softwareversion">
  	      </td>
	    </tr>
        <tr height="30">
          <td>
  	      </td>
  	      <td>
  	      </td>
	    </tr>
        <tr>
          <td height="30">
	        <font face="Tahoma" size="2">
	          Upload Software JAR:
	        </font>
  	      </td>
  	      <td>
  	        <input type="checkbox" name="uploadsoftware">
  	        <input type="file" size="24" value="" name="softwarejar">
  	      </td>
	    </tr>
        <tr>
          <td height="30">
	        <font face="Tahoma" size="2">
	          Upload Definition URL:
	        </font>
  	      </td>
  	      <td>
  	        <input type="text" size="32" value="http://" name="uploadxml">
  	      </td>
	    </tr>
        <tr height="30">
          <td>
  	      </td>
  	      <td>
  	      </td>
	    </tr>
        <tr>
          <td>
	        <font face="Tahoma" size="2">
	          Test Definitions URL:
	        </font>
  	      </td>
  	      <td>
  	        <input type="text" size="32" value="http://" name="testdefinitionsurl">
  	      </td>
	    </tr>
	    <tr>
	      <td>
	        <font face="Tahoma" size="2">
	          Test Selections URL:
	        </font>
	      </td>
	      <td>
	        <input type="text" size="32" value="http://" name="testselectionsurl"> <input type="image" align="middle" alt="Create Selection" src="create.gif" name="createselection">
	      </td>
	    </tr>
	    <tr>
	      <td>
	        <font face="Tahoma" size="2">
	          Save :
	        </font>
	      </td>
	      <td>
	        <input type="checkbox" name="saveselection"> <input type="text" size="16" name="save_name"><input type="submit" name="initiate" value="initiate">
	      </td>
	    </tr>
	    <tr valign="top" height="30">
	      <td>
	      </td>
	      <td>
	      </td>
	    </tr>
	    <tr>
	      <td valign="top">
	        <font face="Tahoma" size="2">
	          Use predefined:
	        </font>
	      </td>
	      <td>
	        <select name="predefined_selection">
<%
			String[] savedRunNames = dtfRunManager.getPredefinedRunNames();

			for (int count=0;count<savedRunNames.length;count++)
			{
%>
			  <option value="<%=savedRunNames[count]%>"><%=savedRunNames[count]%></option>
<%
			}
%>
	        </select>
	        <input type="submit" name="initiate" value="initiate predefined">
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