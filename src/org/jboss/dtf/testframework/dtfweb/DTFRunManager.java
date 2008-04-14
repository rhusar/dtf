/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 *
 * (C) 2008,
 * @author JBoss Inc.
 */
package org.jboss.dtf.testframework.dtfweb;

import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;

import org.jboss.dtf.testframework.dtfweb.utils.*;
import org.jboss.dtf.testframework.nameservice.*;
import org.jboss.dtf.testframework.serviceregister.*;
import org.jboss.dtf.testframework.testnode.*;
import org.jboss.dtf.testframework.coordinator2.CoordinatorInterface;
import org.jboss.dtf.testframework.coordinator2.CoordinatorBusyException;

import java.rmi.*;

import javax.servlet.http.*;
import javax.sql.DataSource;
import java.sql.*;
import java.util.*;
import java.io.*;

import java.net.URLEncoder;
import java.net.URL;

public class DTFRunManager
{
	private static DataSource _pool = DBUtils.getDataSource();

	public void handleRequest(	HttpServletRequest 	request,
								HttpServletResponse	response)
	{
		Map<String, String> fields = uploadFiles(request);

		boolean valid = true;
		String redirectURL = null;
		String testDefinitionsURL = fields.get("testdefinitionsurl");
		String testSelectionsURL = null;

		if ( (fields.get("createselection.x") != null) && (testDefinitionsURL!=null) )
		{
			redirectURL = "default.jsp?page=createselection&testdefinitionsurl="+URLEncoder.encode(testDefinitionsURL);
		}
		else
		{

			String initiateType = fields.get("initiate");
			String softwareVersion = fields.get("softwareversion");
			String uploadSoftwareFlag = fields.get("uploadsoftware");
			String distributionList = fields.get("distributionlist");

			PredefinedTestRunInformation predefinedTestRun = null;

			if ( (uploadSoftwareFlag!=null) && (uploadSoftwareFlag.equalsIgnoreCase("on")) )
			{
				System.out.println("Calling upload software JAR");
				valid = uploadJARToNodes(fields);
			}

			System.out.println("Initiate Type = "+initiateType);

			if ( (initiateType!=null) && (initiateType.equalsIgnoreCase("initiate")) )
			{
				boolean save = fields.get("saveselection")!=null;
				String saveName = fields.get("save_name");

				predefinedTestRun = new PredefinedTestRunInformation();

				predefinedTestRun.testDefinitionsURL = fields.get("testdefinitionsurl");
				predefinedTestRun.testSelectionsURL = fields.get("testselectionsurl");

				System.out.println("Test Definitions URL : "+predefinedTestRun.testDefinitionsURL);
				System.out.println(" Test Selections URL : "+predefinedTestRun.testSelectionsURL);

				System.out.println("State of save box = "+save);

				valid &= ( ( ((save) && (saveName!=null)) || (!save) ) &&
				            (predefinedTestRun.testDefinitionsURL != null) &&
				            (predefinedTestRun.testSelectionsURL != null) &&
				            (softwareVersion != null) &&
				            (softwareVersion.length() > 0) &&
				            (predefinedTestRun.testDefinitionsURL.length() > 0) &&
				            (predefinedTestRun.testSelectionsURL.length() > 0) );

				System.out.println("Stage 1 state = "+valid);

				if ( (valid) && (save) )
					valid &= saveURLPair(predefinedTestRun.testDefinitionsURL, predefinedTestRun.testSelectionsURL, saveName);

				System.out.println("Stage 2 state = "+valid);
			}
			else
			{
				String predefinedSelection = fields.get("predefined_selection");

				System.out.println("Pre-defined Selection = "+predefinedSelection);

				predefinedTestRun = getPredefinedTestRunInformation(predefinedSelection);

				valid = (predefinedTestRun!=null) &&
				        (softwareVersion != null) &&
				        (softwareVersion.length() > 0);

			}

			if (valid)
			{
				try
				{
					RunUID runId = initiateTestRun(predefinedTestRun,softwareVersion, distributionList);
					if (runId != null)
					{
						redirectURL = "default.jsp?page=view_results&softwareversion="+softwareVersion+"&runid="+runId.getUID();
					}
					else
					{
						redirectURL = "default.jsp?page=manualtestrun&error=unable";
					}
				}
				catch (CoordinatorBusyException e)
				{
					redirectURL = "default.jsp?page=view_results&softwareversion="+softwareVersion+"&scheduled=true";
				}
			}
			else
			{
				redirectURL = "default.jsp?page=manualtestrun&error=invalid";
			}
		}

		try
		{
			System.out.println("Redirecting to "+redirectURL);
			response.sendRedirect(redirectURL);
		}
		catch (IOException e)
		{
			System.err.println("ERROR - While sending redirect to invalid Manual Test Run information");
		}
	}

	private boolean uploadJARToNodes(Map<String, String> fields)
	{
		boolean returnValue = false;

        String filename = fields.get("__filename");
        if (filename != null)
		{
			String uploadedFile = getUploadWebDirectory()+filename;
			String defaultNameServiceURI = getDefaultNameServiceURI();
            String uploadXML = fields.get("uploadxml");

			System.out.println("Updating nodes using '"+uploadedFile+"'");
			System.out.println("Name Serivce '"+defaultNameServiceURI+"'");
			System.out.println("Upload Definition File '"+uploadXML+"'");

			try
			{
        		NameServiceInterface nameService = (NameServiceInterface)Naming.lookup(defaultNameServiceURI);
				ServiceRegisterInterface register = (ServiceRegisterInterface)nameService.lookup("/ServiceRegister");

				new TestNodeSoftwareUpdate(register, uploadXML, uploadedFile, "Unknown");

				returnValue = true;
			}
			catch (Exception e)
			{
				System.out.println("ERROR - While trying to update test node software");
				e.printStackTrace();
			}
		}

		return(returnValue);
	}


	private RunUID initiateTestRun(PredefinedTestRunInformation predefinedTestRun, String softwareVersion, String distributionList) throws CoordinatorBusyException
	{
		RunUID runId = null;

		boolean returnValue = true;
		String defaultNameServiceURI = getDefaultNameServiceURI();
        String coordinatorId = null;
        int retryCount = 0;
        int MAX_RETRIES = 10;
        int backOffPeriod = 250;

		System.out.println("Request to initiate test run using the following information:");
		System.out.println("Test Definitions URL : "+predefinedTestRun.testDefinitionsURL);
		System.out.println(" Test Selections URL : "+predefinedTestRun.testSelectionsURL);
		System.out.println("    Software Version : "+softwareVersion);
		System.out.println("   Distribution List : "+distributionList);
		System.out.println("    Name Service URI : "+defaultNameServiceURI);

		try
		{
        	NameServiceInterface nameService = (NameServiceInterface)Naming.lookup(defaultNameServiceURI);

			ServiceRegisterInterface register = (ServiceRegisterInterface)nameService.lookup("/ServiceRegister");

			if (register == null)
			{
				System.err.println("Unable to resolve the service register");
				returnValue = false;
			}
			else
			{
	            System.out.println("Searching for a coordinator...");
			    /*
			     * Find the first coordinator that responds and that is not busy
			     */
	            String[] coordinatorNames = nameService.lookupNames("/Coordinators/");
	        	CoordinatorInterface coordinator = null;
	        	boolean found = false;

	        	while ( (retryCount < MAX_RETRIES) && (!found) )
	        	{
	        	    int count=0;

	            	while ( (count<coordinatorNames.length) && (!found) )
	            	{
	            	    try
	            	    {
	            	        System.out.println("Trying coordinator '"+coordinatorNames[count]+"'");
	                        coordinator = (CoordinatorInterface)nameService.lookup("/Coordinators/"+coordinatorNames[count]);

	                        coordinator.run(new URL( predefinedTestRun.testDefinitionsURL ), new URL( predefinedTestRun.testSelectionsURL ), distributionList, softwareVersion, false);
	                        System.out.println("InitialiseTestRun returned = "+runId);

	            	        found = true;
	            	    }
						catch (CoordinatorBusyException e)
						{
							throw e;
						}
	            	    catch (Exception e)
	            	    {
	            	        System.out.println("Coordinator '"+coordinatorNames[count]+"' not responding..");
	            	    }

	            	    count++;
	            	}

	            	retryCount++;
	            	try
	            	{
	            	    Thread.sleep(backOffPeriod);
	            	    backOffPeriod *= 2;
	            	}
	            	catch (Exception e)
	            	{
	            	}
	            }

	            if (!found)
	            {
	                System.err.println("Unable to find coordinator to run tests with");
	                returnValue = false;
	            }
			}
        }
        catch (Exception e)
        {
            System.err.println("ERROR: "+e.toString());
            e.printStackTrace(System.err);
            returnValue = false;
        }

        return(runId);
	}

	public String[] getPredefinedRunNames()
	{
		String[] results = null;
		Connection conn = null;

		try
		{
			conn = _pool.getConnection();

			Statement s = conn.createStatement();

			/*
			 * Ensure the saveName hasn't already been used
			 */
			ResultSet rs = s.executeQuery("select * from PredefinedRuns");
			ArrayList list = new ArrayList();

			while (rs.next())
			{
				list.add(rs.getString("Name"));
			}

			s.close();

			results = new String[list.size()];
			System.arraycopy(list.toArray(),0,results,0,list.size());
		}
		catch (SQLException e)
		{
			System.out.println("ERROR - While retrieving predefined run names");
		}
		finally
        {
            if ( conn != null )
            {
                try
                {
                    conn.close();
                }
                catch (SQLException e)
                {
                    e.printStackTrace(System.err);
                }
            }
        }
		return(results);
	}


	private boolean saveURLPair(String testDefinitionsURL, String testSelectionsURL, String saveName)
	{
		boolean returnValue = false;
        Connection conn = null;

		try
		{
			conn = _pool.getConnection();

			Statement s = conn.createStatement();

			/*
			 * Ensure the saveName hasn't already been used
			 */
			ResultSet rs = s.executeQuery("select * from PredefinedRuns where Name='"+saveName+"'");

			if (!rs.next())
			{
				s.execute("insert into PredefinedRuns values('"+saveName+"','"+testDefinitionsURL+"','"+testSelectionsURL+"')");

				returnValue = true;
			}

			s.close();
		}
		catch (SQLException e)
		{
			System.err.println("ERROR - While trying to save URL pair");
		}
        finally
        {
            if ( conn != null )
            {
                try
                {
                    conn.close();
                }
                catch (SQLException e)
                {
                    e.printStackTrace(System.err);
                }
            }
        }

		return(returnValue);
	}

	private PredefinedTestRunInformation getPredefinedTestRunInformation(String predefinedSelection)
	{
		PredefinedTestRunInformation returnValue = null;
		Connection conn = null;

		try
		{
			conn = _pool.getConnection();

			Statement s = conn.createStatement();

			/*
			 * Ensure the saveName hasn't already been used
			 */
			ResultSet rs = s.executeQuery("select * from PredefinedRuns where Name='"+predefinedSelection+"'");

			if (rs.next())
			{
				returnValue = new PredefinedTestRunInformation();
				returnValue.testDefinitionsURL = rs.getString("TestDefinitionsURL");
				returnValue.testSelectionsURL = rs.getString("TestSelectionsURL");
			}

			rs.close();
			s.close();
		}
		catch (SQLException e)
		{
			System.err.println("ERROR - While retrieving predefined test run information");
			returnValue = null;
		}
        finally
        {
            if ( conn != null )
            {
                try
                {
                    conn.close();
                }
                catch (SQLException e)
                {
                    e.printStackTrace(System.err);
                }
            }
        }

		return(returnValue);
	}

	public String getDefaultNameServiceURI()
	{
		String returnValue = null;
		Connection conn = null;

		try
		{
			conn = _pool.getConnection();

			Statement s = conn.createStatement();

			ResultSet rs = s.executeQuery("select DefaultNameServiceURI from Configuration");

			if (rs.next())
			{
				returnValue = rs.getString("DefaultNameServiceURI");
			}

			rs.close();
			s.close();
		}
		catch (SQLException e)
		{
			System.err.println("ERROR - While trying to retrieve the default name service URI");
		}
		finally
        {
            if ( conn != null )
            {
                try
                {
                    conn.close();
                }
                catch (SQLException e)
                {
                    e.printStackTrace(System.err);
                }
            }
        }
		return(returnValue);
	}

	public static String getUploadFileDirectory()
	{
		String returnValue = null;
		Connection conn = null;

		try
		{
			conn = _pool.getConnection();

			Statement s = conn.createStatement();

			ResultSet rs = s.executeQuery("select UploadDirectory from Configuration");

			if (rs.next())
			{
				returnValue = rs.getString("UploadDirectory");
			}

			rs.close();
			s.close();
		}
		catch (SQLException e)
		{
			System.err.println("ERROR - While trying to retrieve the UploadDirectory");
		}
        finally
        {
            if ( conn != null )
            {
                try
                {
                    conn.close();
                }
                catch (SQLException e)
                {
                    e.printStackTrace(System.err);
                }
            }
        }

		return(returnValue);
	}

	public static String getUploadWebDirectory()
	{
		String returnValue = null;
		Connection conn = null;

		try
		{
			conn = _pool.getConnection();

			Statement s = conn.createStatement();

			ResultSet rs = s.executeQuery("select UploadWebDirectory from Configuration");

			if (rs.next())
			{
				returnValue = rs.getString("UploadWebDirectory");
			}

			rs.close();
			s.close();
		}
		catch (SQLException e)
		{
			System.err.println("ERROR - While trying to retrieve UploadWebDirectory");
		}
        finally
        {
            if ( conn != null )
            {
                try
                {
                    conn.close();
                }
                catch (SQLException e)
                {
                    e.printStackTrace(System.err);
                }
            }
        }

		return(returnValue);
	}

	private Map<String, String> uploadFiles(HttpServletRequest request)
	{
        Map<String, String> fields = new HashMap<String, String>();

		try
		{
            if(!ServletFileUpload.isMultipartContent(request)) {
                return fields;
            }

            FileItemFactory factory = new DiskFileItemFactory();
            ServletFileUpload upload = new ServletFileUpload(factory);
            upload.setFileSizeMax(1024*1024);
            upload.setSizeMax(1024*1024*20);
            List<FileItem> items = upload.parseRequest(request);

            String destDir = getUploadFileDirectory();

            Iterator<FileItem> iter = items.iterator();
            while (iter.hasNext()) {
                FileItem item = iter.next();

                if (item.isFormField()) {
                    fields.put(item.getFieldName(), item.getString());
                } else {
                    File file = new File(destDir+"/"+item.getName());
                    if(!fields.containsKey("__filename")) {
                        fields.put("__filename", item.getName());
                    }
                    item.write(file);
                }
            }
		}
		catch (Exception e)
		{
			System.err.println("ERROR - While retrieving client-side files");
			e.printStackTrace(System.err);
		}

       return fields;
    }

	public TestNodeDetails[] getTestNodeDetails()
	{
		String defaultNameServiceURI = getDefaultNameServiceURI();
		TestNodeDetails[] results = null;

		try
		{
	   		NameServiceInterface nameService = (NameServiceInterface)Naming.lookup(defaultNameServiceURI);
			ServiceRegisterInterface register = (ServiceRegisterInterface)nameService.lookup("/ServiceRegister");

	        TestNodeInterface[] nodes = register.getRegister();

	        results = new TestNodeDetails[nodes.length];

	        for (int count=0;count<nodes.length;count++)
	        {
	            try
	            {
            		results[count] = new TestNodeDetails();
            		results[count]._hostAddress = nodes[count].getHostAddress();
            		results[count]._name = nodes[count].getName();
				}
				catch (RemoteException e)
				{
				}
			}
		}
		catch (Exception e)
		{
			System.out.println("Unexpected Exception - "+e.toString());
			e.printStackTrace();
		}

		return(results);
	}
}
