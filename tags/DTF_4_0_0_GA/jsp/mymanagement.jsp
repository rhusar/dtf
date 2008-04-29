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
<%@ page import="org.jboss.dtf.testframework.dtfweb.StoredTestDefs,
				 org.jboss.dtf.testframework.dtfweb.mydtf.MyDTF,
				 java.util.ArrayList,
				 org.jboss.dtf.testframework.dtfweb.StoredTestSelections,
				 org.jboss.dtf.testframework.coordinator2.DeployInformation,
				 org.jboss.dtf.testframework.coordinator2.scheduler.ScheduleInformation,
				 java.net.URL,
				 org.jboss.dtf.testframework.coordinator2.scheduler.ScheduleWhenPossible,
				 org.jboss.dtf.testframework.coordinator2.scheduler.ScheduleOneTimeOnly,
				 org.jboss.dtf.testframework.coordinator2.scheduler.ScheduleWeekDays,
				 org.jboss.dtf.testframework.coordinator2.CoordinatorBusyException"%>
<%
	String newExpanded = request.getParameter("expandtestdefs");

	if ( newExpanded != null )
	{
		session.setAttribute("mymanagement-testdefs", newExpanded );
	}

	String expandedStr = (String)session.getAttribute("mymanagement-testdefs");
    long expanded = -1;

	if ( expandedStr != null )
	{
		expanded = Long.parseLong(expandedStr);
	}

	String function = request.getParameter("function");
	String runSuccessText = request.getParameter("runsuccess");
	String scheduled = request.getParameter("scheduled");

	if ( function != null )
	{
		if ( function.equals("add") )
		{
			String testDefId = request.getParameter("testdef");

			myDTF.addStoredTestDefs(Integer.parseInt(testDefId));
		}
		else
		if ( function.equals("remove") )
		{
		    String testDefId = request.getParameter("testdef");

			myDTF.removeStoredTestDefs(Integer.parseInt(testDefId));
		}
	}

	if ( runSuccessText != null )
	{
%>
<table width="100%" border="0" cellspacing="0" bordercolor="black">
    <tr>
        <td>
            <table width="100%">
                <tr>
                    <td>
<%
		if ( scheduled != null )
		{
%>
                        <font face="Tahoma" size="2">
                            Test run initiation: <font color="green">Test Run Scheduled</font>
                        </font>
<%
		}
		else
		{
%>
                        <font face="Tahoma" size="2">
                            Test run initiation: <font color="red">Failure</font>
                        </font>
<%
		}
%>
                    </td>
                </tr>
            </table>
        </td>
    </tr>
</table>
<br/>
<%
	}
%>
<br/>
<br>
<%
	if ( function != null )
	{
		if ( function.equals("delete") )
		{
			long testId = Long.parseLong( request.getParameter("testdef") );
			StoredTestDefs std = StoredTestDefs.getStoredTestDefs(testId);

			if ( std != null )
			{
				std.delete();
			}
		}

		if ( function.equals("selections") )
		{
			long testId = Long.parseLong( request.getParameter("testdef") );
			StoredTestDefs std = StoredTestDefs.getStoredTestDefs(testId);

			if ( std != null )
			{
				if ( request.getParameter("set") != null )
				{
					String emailDistributionList = request.getParameter("emaildistributionlist");
					String softwareVersion = request.getParameter("softwareversion");
					String selection = request.getParameter("selection");
					String scheduleType = request.getParameter("scheduletype");
					StoredTestSelections testSelections = null;
					DeployInformation deployInfo = null;
					ScheduleInformation scheduleEntry = null;
					String deployTypeStr = request.getParameter("deploytype");
					String productName = request.getParameter("deploy_product");
					URL testDefsURL = null;
					URL testSelectionsURL = null;


					if ( ( selection != null ) && ( selection.length() >0 ) )
					{
						testSelections = std.getTestSelection( selection );

						testDefsURL = new URL(testSelections.getTestDefinitions().getURL());
						testSelectionsURL = new URL(testSelections.getURL());
					}

					if ( deployTypeStr.equals("productconfig") )
					{
						deployInfo = new DeployInformation(productName, null);
					}
					else
					if ( deployTypeStr.equals("userdefined"))
					{
						deployInfo = new DeployInformation(productName, request.getParameter("deployurl"));
					}

					if ( scheduleType.equals("whenpossible"))
					{
						scheduleEntry = new ScheduleWhenPossible(	testDefsURL,
																	testSelectionsURL,
																	emailDistributionList,
																	softwareVersion,
																	deployInfo );
					}
					else
					if ( scheduleType.equals("onceonly"))
					{
						scheduleEntry = new ScheduleOneTimeOnly(	testDefsURL,
																	testSelectionsURL,
																	emailDistributionList,
																	softwareVersion,
																	deployInfo,
																	Integer.parseInt( request.getParameter("onceonly_hour") ),
																	Integer.parseInt( request.getParameter("onceonly_minute") ) );
					}
					else
					if ( scheduleType.equals("weekdays"))
					{
						scheduleEntry = new ScheduleWeekDays(		testDefsURL,
																	testSelectionsURL,
																	emailDistributionList,
																	softwareVersion,
																	deployInfo,
																	Integer.parseInt( request.getParameter("weekdays_hour") ),
																	Integer.parseInt( request.getParameter("weekdays_minute") ) );
					}

					String priority = request.getParameter("priority");
					scheduleEntry.setPriority(Integer.parseInt(priority));

					System.out.println("Running test");

					try
					{
						if ( testSelections != null )
							testSelections.run(scheduleEntry);
						else
							StoredTestSelections.runScheduledItem(scheduleEntry);
					}
					catch (CoordinatorBusyException e)
					{
						// Ignore - because it will be scheduled
					}

					response.sendRedirect("default.jsp?page=mymanagement&runsuccess=true&scheduled=true");
				}
				else
				if ( request.getParameter("deleteButton") != null )
				{
					String selection = request.getParameter("selection");

					if ( ( selection != null ) && ( selection.length() >0 ) )
					{
						StoredTestSelections testSelections = std.getTestSelection( selection );
						testSelections.delete();
					}
				}
				else
				if ( request.getParameter("create") != null )
				{
					response.sendRedirect("default.jsp?page=createselection&testdef="+testId);
				}
			}
		}
	}

	ArrayList storedTestDefs = myDTF.getStoredTestDefs();

	for (int count=0;count<storedTestDefs.size();count++)
	{
		StoredTestDefs std = (StoredTestDefs)storedTestDefs.get(count);
		boolean isExpanded = (std.getId() == expanded);
%>
		<%=std.writeManagementPanel("default.jsp?page=mymanagement", isExpanded, myDTF.contains(std))%>
<%
	}
%>