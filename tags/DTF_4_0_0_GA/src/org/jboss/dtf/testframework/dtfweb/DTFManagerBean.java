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

import org.jboss.dtf.testframework.dtfweb.utils.*;
import org.jboss.dtf.testframework.coordinator.OSProductCombination;
import org.jboss.dtf.testframework.productrepository.ProductRepositoryInterface;
import org.jboss.dtf.testframework.productrepository.ProductRepository;
import org.jboss.dtf.testframework.nameservice.NameServiceInterface;
import org.jboss.dtf.testframework.nameservice.NameService;
import org.jboss.dtf.testframework.coordinator2.CoordinatorInterface;
import org.jboss.dtf.testframework.coordinator2.Coordinator;
import org.jboss.dtf.testframework.serviceregister.ServiceRegisterInterface;
import org.jboss.dtf.testframework.serviceregister.ServiceRegister;
import org.jboss.dtf.testframework.testnode.TestNodeInterface;

import java.sql.*;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.sql.DataSource;

import java.util.*;
import java.rmi.Naming;
import java.rmi.RemoteException;

public class DTFManagerBean
{
    private static String      _currentNameService = null;
    private static DataSource  _pool = DBUtils.getDataSource();

	private String	    _pageToDisplay;
    private HashSet     _menus = new HashSet();

	public String[] getNameServiceURIs()
	{
		ArrayList results = new ArrayList();
		String[] returnArray = null;
        Connection conn = null;

		try
		{
			conn = _pool.getConnection();
			Statement s = conn.createStatement();

			ResultSet rs = s.executeQuery("SELECT * FROM KnownNameServices");

			while (rs.next())
			{
				results.add(rs.getString("NameServiceURI"));
			}

			rs.close();
			s.close();

			returnArray = new String[results.size()];

			System.arraycopy(results.toArray(),0,returnArray,0,returnArray.length);
		}
		catch (Exception e)
		{
			System.err.println("ERROR - Failed to retrieve name service URIs");
			returnArray = null;
		}
		finally
        {
            try
            {
                if ( conn != null )
                {
                    conn.close();
                }
            }
            catch (SQLException e)
            {
                e.printStackTrace(System.err);
            }
        }
		return(returnArray);
	}

	public static String getDefaultNameServiceURI()
	{
		String returnValue = null;
		Connection conn = null;

		try
		{
			conn = _pool.getConnection();
			Statement s = conn.createStatement();

			ResultSet rs = s.executeQuery("SELECT * FROM Configuration");

			if (rs.next())
			{
				returnValue = rs.getString("DefaultNameServiceURI");
			}

			rs.close();
			s.close();
		}
		catch (Exception e)
		{
			e.printStackTrace(System.err);
			System.err.println("ERROR - Cannot retrieve the default name service URI");
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

	public String getUploadDirectory()
	{
		String returnValue = null;
		Connection conn = null;

		try
		{
			conn = _pool.getConnection();
			Statement s = conn.createStatement();

			ResultSet rs = s.executeQuery("SELECT * FROM Configuration");

			if (rs.next())
			{
				returnValue = rs.getString("UploadDirectory");
			}

			rs.close();
			s.close();
		}
		catch (Exception e)
		{
			e.printStackTrace(System.err);
			System.err.println("ERROR - Cannot retrieve the UploadDirectory");
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

	public String getUploadWebDirectory()
	{
		String returnValue = null;
		Connection conn = null ;

		try
		{
			conn = _pool.getConnection();
			Statement s = conn.createStatement();

			ResultSet rs = s.executeQuery("SELECT * FROM Configuration");

			if (rs.next())
			{
				returnValue = rs.getString("UploadWebDirectory");
			}

			rs.close();
			s.close();
		}
		catch (Exception e)
		{
			e.printStackTrace(System.err);
			System.err.println("ERROR - Cannot retrieve the UploadWebDirectory");
		}
		finally
        {
            try
            {
                if ( conn != null )
                {
                    conn.close();
                }
            }
            catch (SQLException e)
            {
                e.printStackTrace(System.err);
            }
        }

		return(returnValue);
	}

	public String getRootURL()
	{
		String returnValue = null;
		Connection conn = null;

		try
		{
			conn = _pool.getConnection();
			Statement s = conn.createStatement();

			ResultSet rs = s.executeQuery("SELECT * FROM Configuration");

			if (rs.next())
			{
				returnValue = rs.getString("RootURL");
			}

			rs.close();
			s.close();
		}
		catch (Exception e)
		{
			e.printStackTrace(System.err);
			System.err.println("ERROR - Cannot retrieve the RootURL");
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

	public EmailDetails[] getEmailRecipients()
	{
		ArrayList results = new ArrayList();
		EmailDetails[] returnArray = null;
		Connection conn = null;

		try
		{
			conn = _pool.getConnection();
			Statement s = conn.createStatement();

			ResultSet rs = s.executeQuery("SELECT * FROM EmailRecipients");

			while (rs.next())
			{
				EmailDetails ed = new EmailDetails(rs.getString("EmailAddress"),rs.getInt("EmailType"));
				results.add(ed);
			}

			rs.close();
			s.close();

			returnArray = new EmailDetails[results.size()];

			System.arraycopy(results.toArray(),0,returnArray,0,returnArray.length);
		}
		catch (Exception e)
		{
			System.err.println("ERROR - Unexpected Exception '"+e+"'");
			returnArray = null;
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

		return(returnArray);
	}

    public boolean addNewDTFInstance(String nameServiceURI)
    {
        boolean success = true;
        Connection conn = null;

        try
		{
			conn = _pool.getConnection();

            Statement s = conn.createStatement();

            s.execute("INSERT INTO KnownNameServices VALUES ('"+nameServiceURI+"')");

            s.close();
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
            success = false;
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

        return success;
    }

	public void processSetupUpdate(HttpServletRequest req)
	{
        Connection conn = null;
		try
		{
            conn = _pool.getConnection();

			Statement s = conn.createStatement();

			if (req.getParameter("delete_product")!=null)
			{
				String numberOfProducts = req.getParameter("number_of_product");

				if (numberOfProducts != null)
				{
					int num = Integer.parseInt(numberOfProducts);

					for (int count=0;count<num;count++)
					{
						String productSelected = req.getParameter("deleteproduct_"+count);

						if (productSelected != null)
						{
							s.execute("DELETE FROM Products WHERE Id='"+productSelected+"'");
						}
					}
				}
			}

			if (req.getParameter("delete_os")!=null)
			{
				String numberOfOS = req.getParameter("number_of_os");

				if (numberOfOS != null)
				{
					int num = Integer.parseInt(numberOfOS);

					for (int count=0;count<num;count++)
					{
						String osSelected = req.getParameter("deleteos_"+count);

						if (osSelected != null)
						{
							s.execute("DELETE FROM OSs WHERE Id='"+osSelected+"'");
						}
					}
				}
			}
			if (req.getParameter("add_os")!=null)
			{
				String osName = req.getParameter("osname");

				if (osName != null)
				{
					s.execute("INSERT INTO OSs VALUES ('"+osName+"','"+osName+"')");
				}
			}

			if (req.getParameter("add_product")!=null)
			{
				String productName = req.getParameter("productname");

				if (productName != null)
				{
					s.execute("INSERT INTO Products VALUES ('"+productName+"','"+productName+"')");
				}
			}

			if (req.getParameter("add_uri")!=null)
			{
				String nameService = req.getParameter("nameserviceuri");

				if (nameService != null)
				{
					s.execute("INSERT INTO KnownNameServices VALUES ('"+nameService+"')");
				}
			}

			if (req.getParameter("delete_uri")!=null)
			{
				String numNameServiceURIs = req.getParameter("numberofnameserviceuris");

				if (numNameServiceURIs != null)
				{
					int numberOfNameServiceURIs = Integer.parseInt(numNameServiceURIs);

					for (int count=0;count<numberOfNameServiceURIs;count++)
					{
						String nameServiceSelected = req.getParameter("nameservice_"+count);

						if (nameServiceSelected != null)
						{
							System.out.println("Deleting recipient '"+nameServiceSelected+"'");
							s.execute("DELETE FROM KnownNameServices WHERE NameServiceURI='"+nameServiceSelected+"'");
						}
					}
				}
			}

			if (req.getParameter("update")!=null)
			{
				String defaultNameServiceURI = req.getParameter("defaultnameserviceuri");
				String uploadDirectory = req.getParameter("uploaddirectory");
				String uploadWebDirectory = req.getParameter("uploadwebdirectory");
				String rootURL = req.getParameter("rooturl");

				if ( (defaultNameServiceURI!=null) && (uploadDirectory!=null) && (uploadWebDirectory!=null) && (rootURL!=null) )
				{
					s.execute("UPDATE Configuration SET DefaultNameServiceURI='"+defaultNameServiceURI+"',"+
					                                   "UploadDirectory='"+uploadDirectory+"',"+
					                                   "UploadWebDirectory='"+uploadWebDirectory+"',"+
					                                   "RootURL='"+rootURL+"'");
				}
			}

			if (req.getParameter("add")!=null)
			{
				String recipient = req.getParameter("emailaddr");
				int type = Integer.parseInt(req.getParameter("emailtype"));

				if (recipient != null)
				{
					s.execute("INSERT INTO EmailRecipients VALUES ('"+recipient+"',"+type+")");
				}
			}

			if (req.getParameter("delete")!=null)
			{
				String numRecipients = req.getParameter("numberofrecipients");

				if (numRecipients != null)
				{
					int numberOfRecipients = Integer.parseInt(numRecipients);

					for (int count=0;count<numberOfRecipients;count++)
					{
						String recipient = req.getParameter("recipient_"+count);

						if (recipient != null)
						{
							System.out.println("Deleting recipient '"+recipient+"'");
							s.execute("DELETE FROM EmailRecipients WHERE EmailAddress='"+recipient+"'");
						}
					}
				}
			}

			s.close();
		}
		catch (Exception e)
		{
			e.printStackTrace(System.err);
			System.err.println("ERROR - Failed to delete recipients");
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
	}

	public void handleRequest(HttpServletRequest req)
	{
		_pageToDisplay = req.getParameter("page");

		if (_pageToDisplay == null)
		{
			_pageToDisplay = "mymanagement";
		}

        String displayMenu = req.getParameter("displaymenu");
        String value = req.getParameter("value");
        String instanceURI = req.getParameter("instance");


        if ( ( displayMenu != null ) && ( value != null ) )
        {
            setDisplayMenu(displayMenu, new Boolean(value).booleanValue());
        }

        if ( instanceURI != null )
        {
            setCurrentNameServiceURI(instanceURI);
        }
	}

	public String getPageToDisplay()
	{
		return(_pageToDisplay);
	}

    public void setDisplayMenu(String menu, boolean display)
    {
        if ( display )
			_menus.remove( menu );
        else
			_menus.add( menu );
    }

    public boolean isDisplayingMenu(String menu)
    {
        return !_menus.contains( menu );
    }

    public static void setCurrentNameServiceURI(String nameService)
    {
        _currentNameService = nameService;
    }

    public static NameServiceInterface getNameServiceInterface()
    {
        NameServiceInterface nsi = null;

        try
        {
            nsi = (NameServiceInterface)Naming.lookup(getCurrentNameServiceURI());
        }
        catch (Exception e)
        {
            System.err.println("Failed to retrieve the name service interface: "+e);
            e.printStackTrace(System.err);
        }

        return nsi;
    }

    public static String    getCurrentNameServiceURI()
    {
        return _currentNameService != null ? _currentNameService : getDefaultNameServiceURI();
    }

	public static ServiceRegisterInterface getServieRegistry()
	{
		ServiceRegisterInterface sri = null;

		try
		{
			NameServiceInterface nsi = (NameServiceInterface)Naming.lookup( getCurrentNameServiceURI() );

			if ( nsi != null )
			{
				sri = (ServiceRegisterInterface)nsi.lookup( ServiceRegister.SERVICE_REGISTER_NAME_SERVICE_ENTRY );
			}
		}
		catch (Exception e)
		{
			e.printStackTrace(System.err);
		}

		return sri;
	}

    public static ProductRepositoryInterface getProductRepository()
    {
        ProductRepositoryInterface pri = null;

        try
        {
            NameServiceInterface nsi = (NameServiceInterface)Naming.lookup( getCurrentNameServiceURI() );

            if ( nsi != null )
            {
                pri = (ProductRepositoryInterface)nsi.lookup( ProductRepository.PRODUCT_REPOSITORY_NAMESERVICE_NAME );
            }
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
        }

        return pri;
    }

	public static CoordinatorInterface getCoordinator()
	{
		CoordinatorInterface coordinator = null;

		try
		{
			NameServiceInterface nsi = (NameServiceInterface)Naming.lookup( getCurrentNameServiceURI() );

			if ( nsi != null )
			{
				coordinator = (CoordinatorInterface)nsi.lookup( Coordinator.COORDINATOR_NAME_SERVICE_NAME );
			}
		}
		catch (Exception e)
		{
			e.printStackTrace(System.err);
		}

		return coordinator;
	}

	public String getToggleURL(HttpServletRequest req, String menu)
	{
        StringBuffer requestURL = req.getRequestURL();
		String query = req.getQueryString();

		/**
		 * If we are currently displaying the menu then the link should remove the menu
		 */

		Hashtable params = getParameters(query);
		params.put( "displaymenu", menu);
		params.put( "value", isDisplayingMenu(menu) ? "false" : "true" );

		return requestURL.toString() + "?" + generateQueryString(params);
	}

	private Hashtable getParameters(String text)
	{
		Hashtable params = new Hashtable();

		if ( text != null )
		{
			StringTokenizer st = new StringTokenizer(text,"&");

			while (st.hasMoreTokens())
			{
				String parameter = st.nextToken();
				String name = parameter.substring(0,parameter.indexOf('='));
				String value = parameter.substring(parameter.indexOf('=')+1);

				params.put(name, value);
			}
		}

		return params;
	}

	private String generateQueryString(Hashtable params)
	{
		String returnValue = "";
		Enumeration e = params.keys();

		while ( e.hasMoreElements() )
		{
			String name = (String) e.nextElement();
			String value = (String) params.get(name);

			returnValue += name+"="+value;

			if ( e.hasMoreElements() )
			{
				returnValue += "&";
			}
		}

		return returnValue;
	}

	public static boolean restartAllNodes() throws Exception
	{
		boolean success = true;
		ServiceRegisterInterface serviceRegistry = DTFManagerBean.getServieRegistry();

		if ( serviceRegistry != null )
		{
			TestNodeInterface[] node = serviceRegistry.getRegister();

			for (int count=0;count<node.length;count++)
			{
				try
				{
					node[count].shutdown(true, false);
				}
				catch (RemoteException e)
				{
					e.printStackTrace(System.err);
					success = false;
				}
			}
		}

		return success;
	}

	public static boolean restartNode(short nodeId, boolean restart, boolean onComplete)
	{
		boolean success = false;
		ServiceRegisterInterface serviceRegistry = DTFManagerBean.getServieRegistry();

		if ( serviceRegistry != null )
		{
			try
			{
				TestNodeInterface node = serviceRegistry.lookupService(nodeId);

				node.shutdown(restart, onComplete);
			}
			catch (Exception e)
			{
				e.printStackTrace(System.err);
				success = false;
			}
		}

		return success;
	}

	static
    {
        try
        {
            DTFResultsLogger.createTables();
        }
        catch (Exception e)
        {
			e.printStackTrace(System.err);
            throw new ExceptionInInitializerError("Exception while creating tables: "+e);
        }
    }
}
