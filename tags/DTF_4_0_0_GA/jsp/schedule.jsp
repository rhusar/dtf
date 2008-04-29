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
<%@ page import="org.jboss.dtf.testframework.coordinator2.RunInformation,
                 org.jboss.dtf.testframework.coordinator2.CoordinatorIdleException,
                 org.jboss.dtf.testframework.coordinator2.scheduler.ScheduleInformation,
                 org.jboss.dtf.testframework.coordinator2.CoordinatorInterface,
                 org.jboss.dtf.testframework.dtfweb.DTFManagerBean,
                 org.jboss.dtf.testframework.testnode.RunUID"%>
<%
    CoordinatorInterface coordinator = DTFManagerBean.getCoordinator();

    String unscheduleId = request.getParameter("unschedule");
    String scheduleFunction = request.getParameter("function");

    if ( unscheduleId != null )
    {
        coordinator.getScheduler().unschedule(Long.parseLong(unscheduleId));
    }

    if ( scheduleFunction != null )
    {
        if ( scheduleFunction.startsWith("stop") )
        {
            String runId = request.getParameter("runid");

            if ( runId != null )
            {
                boolean waitToComplete = scheduleFunction.equals("stop");

                coordinator.stopRun(waitToComplete, new RunUID(Long.parseLong(runId)));
            }
        }
    }
%>
<table width="100%" border="0" cellspacing="0" bordercolor="black">
    <tr>
        <td>
			<table>
				<td>
					Test Runs:
				</td>
				<td bgcolor="#FFFF99">
					<code>current</code>
				</td>
				<td bgcolor="#EDEEFE">
					<code>next</code>
				</td>
				<td>
					<code>scheduled</code>
				</td>
			</table>
		</td>
	</tr>

	<tr>
		<td>
			<table width="100%">
				<tr>
					<td>
						Test Definitions
					</td>
					<td>
						Test Selections
					</td>
					<td>
                    	Distribution List
					</td>
					<td>
                        Software Version
					</td>
					<td>
						Schedule Details
					</td>
					<td>
						options
					</td>
				</tr>


<%
	if ( coordinator != null )
	{
		try
		{
			RunInformation[] currentCoordinatorRunInfo = coordinator.getCurrentRunInformation();

			if ( currentCoordinatorRunInfo != null )
			{
                for (int count=0;count<currentCoordinatorRunInfo.length;count++)
                {
%>
				<tr>
					<td colspan="6">
						<table width="100%">
							<tr>
								<td>
									<code><b>Status:</b> <%=currentCoordinatorRunInfo[count].getCurrentStatus()%></code>
									<table cellpadding="0" cellspacing="0" width="1">
										<tr>
											<td bgcolor="#7D42DE" width="1">
												<font color="#7D42DE"><% for (int c=0;c<(10 * currentCoordinatorRunInfo[count].getPercentageComplete());c++) {%>#<%}%></font>
											</td>
											<td bgcolor="#D8C7F5" width="1">
												<font color="#D8C7F5"><% for (int c=0;c<10-(10 * currentCoordinatorRunInfo[count].getPercentageComplete());c++) {%>#<%}%></font>											</td>
											</tr>
									</table>
								</td>
							</tr>
						</table>
					</td>
				</tr>
				<tr bgcolor="#FFFF99">
					<td>
						<a href="<%=currentCoordinatorRunInfo[count].getTestDefinitionsURL()%>"><code><%=currentCoordinatorRunInfo[count].getTestDefinitionsURL()%></code></a>
					</td>
					<td>
						<a href="<%=currentCoordinatorRunInfo[count].getTestSelectionURL()%>"><code><%=currentCoordinatorRunInfo[count].getTestSelectionURL()%></code></a>
					</td>
					<td>
						<code><%=currentCoordinatorRunInfo[count].getDistributionList()%></code>
					</td>
					<td>
						<code><%=currentCoordinatorRunInfo[count].getSoftwareVersion()%></code>
					</td>
					<td>
                        &nbsp;
					</td>
					<td align="center">
						<a href="default.jsp?page=view_results&runid=<%=currentCoordinatorRunInfo[count].getRunId().getUID()%>"><code>view results</code></a>
                        <a href="default.jsp?page=schedule&runid=<%=currentCoordinatorRunInfo[count].getRunId().getUID()%>&function=stop"><code>stop on complete</code></a>
                        <a href="default.jsp?page=schedule&runid=<%=currentCoordinatorRunInfo[count].getRunId().getUID()%>&function=stopnow"><code>stop now</code></a>
					</td>
				</tr>
<%
                }
			}
		}
		catch (CoordinatorIdleException e)
		{
			// Ignore - no run in progress
		}

		ScheduleInformation[] schedule = coordinator.getScheduler().getSchedule();

		if ( schedule.length > 0 )
		{
			for (int count=0;count<schedule.length;count++)
			{
				if ( count == 0 )
				{
%>
				<tr bgcolor="#EDEEFE">
<%
				}
				else
				{
%>
				<tr>
<%
				}
%>
					<td>
						<code><%=schedule[count].getTestDefinitionsURL()%></code>
					</td>
					<td>
						<code><%=schedule[count].getTestSelectionURL()%></code>
					</td>
					<td>
						<code><%=schedule[count].getDistributionList()%></code>
					</td>
					<td>
						<code><%=schedule[count].getSoftwareVersion()%></code>
					</td>
					<td>
						<code><%=schedule[count].toString()%> (Priortiy: <%=schedule[count].getPriorityText()%>)</code>
					</td>
					<td>
						<a href="default.jsp?page=schedule&unschedule=<%=schedule[count].getUID()%>">unschedule</a>
					</td>
				</tr>
<%
			}
		}
	}
%>
           	</table>
		</td>
	</tr>
</table>
