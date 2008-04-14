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
<SCRIPT TYPE="text/Javascript">
<!--
function selectAllOfType(b,c)
{
	for (a=0;a<document.testselections.length;a++)
	{
		if ( document.testselections[a].name.lastIndexOf( b ) != -1 )
		{
			document.testselections[a].checked = c.checked
		}
	}
}
//-->
</SCRIPT>

<%@ page import="org.jboss.dtf.testframework.dtfweb.StoredTestDefs,
                 org.jboss.dtf.testframework.coordinator.TestDefinitionRepository,
                 java.net.URL,
                 java.util.Hashtable,
                 java.util.HashMap,
                 java.util.Set,
                 org.jboss.dtf.testframework.dtfweb.DTFCreateSelection,
                 java.util.Enumeration,
                 org.jboss.dtf.testframework.dtfweb.GroupSelection,
                 org.jboss.dtf.testframework.dtfweb.OSDetails,
				 org.jboss.dtf.testframework.productrepository.ProductRepositoryInterface"%>

<%
    DTFCreateSelection selManager = null;
    String testIdStr = request.getParameter("testdef");
    TestDefinitionRepository repository = null;

    if ( testIdStr != null )
    {
        long testId = Long.parseLong( testIdStr );
        StoredTestDefs std = StoredTestDefs.getStoredTestDefs(testId);

        selManager = new DTFCreateSelection(std);

        session.setAttribute(DTFCreateSelection.CURRENT_SELECTION_ATTRIBUTE_NAME, selManager);
    }
    else
    {
        selManager = (DTFCreateSelection)session.getAttribute(DTFCreateSelection.CURRENT_SELECTION_ATTRIBUTE_NAME);
    }

    selManager.handleRequest(request);

    if ( request.getParameter("tosave") != null )
    {
        selManager.save();
        response.sendRedirect("default.jsp?page=management");
    }
%>

<form name="testselections" method="post" action="default.jsp?page=createselection">

<table width="100%" bgcolor="#3366CC">
    <tr>
        <td>
            <font color="White" face="Tahoma" size="2">
                Selection Name: <input type="text" name="selection_name" size="32" value="<%=selManager.getSelectionName()%>"/>
            </font>
        </td>
    </tr>
    <tr>
        <td>
            <font color="White" face="Tahoma" size="2">
                Product Name:
									<select name="product_name">
						<%
							ProductRepositoryInterface pri = dtfManager.getProductRepository();

							if ( pri != null )
							{
								String[] supportedProducts = pri.getProductNames();

								for (int count=0;count<supportedProducts.length;count++)
								{
						%>
										<option <%= (selManager.getProductName() != null) ? (selManager.getProductName().equals(supportedProducts[count]) ? "selected" : "") : ""%> value="<%=supportedProducts[count]%>"><%=supportedProducts[count]%></option>
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
            </font>
        </td>
    </tr>
    <tr>
        <td>
            <font color="White" face="Tahoma" size="2">
                Description:<br>
                <textarea name="selection_description" cols="60" rows="5"><%=selManager.getDescription()%></textarea> <input type="submit" name="tosave" value="save"/>
            </font>
        </td>
    </tr>
</table>
<hr>
<table border="0" cellpadding="2" bgcolor="#3366CC" width="100%">
    <tr>
        <td width="50%">
            &nbsp;
        </td>
        <td>
            <font color="White" face="Tahoma" size="2">
<%
    OSDetails[] oss = selManager.getSupportedOperatingSystems();

    for (int osCount=0;osCount<oss.length;osCount++)
    {
%>
                <input type="checkbox" name="selectos_<%=(char)'A'+osCount%>" onclick="selectAllOfType('<%=oss[osCount]._id%>',selectos_<%=(char)'A'+osCount%>)"/><%=oss[osCount]._name%>
<%
    }
%>
            </font>
        </td>
    </tr>
</table>
<table border="0" cellpadding="2" bgcolor="#3366CC" width="100%">
    <%
        Hashtable groups = selManager.getGroups();
        StringBuffer buffer = new StringBuffer();

        selManager.generateGroupBoxes(0, buffer, groups);
    %>
    <%=buffer.toString()%>
</table>
</form>
