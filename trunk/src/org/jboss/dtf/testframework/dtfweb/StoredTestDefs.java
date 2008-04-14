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

import org.jboss.dtf.testframework.dtfweb.utils.DBUtils;
import org.jboss.dtf.testframework.dtfweb.mydtf.MyDTF;
import org.jboss.dtf.testframework.coordinator.TestDefinitionRepository;
import org.jboss.dtf.testframework.productrepository.ProductRepositoryInterface;
import org.jboss.dtf.testframework.coordinator2.scheduler.ScheduleInformation;

import javax.sql.DataSource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletOutputStream;
import java.sql.*;
import java.util.ArrayList;
import java.io.File;
import java.io.PrintWriter;
import java.net.URL;
import java.lang.String;

public class StoredTestDefs
{
    private static DataSource  _pool = DBUtils.getDataSource();

    private long        _testId;
    private String      _name;
    private String      _url;
    private String      _description;

    protected StoredTestDefs(long id, String name, String url, String description)
    {
        _testId = id;
        _name = name;
        _url = url;
        _description = description;
    }

	public boolean equals(Object o)
	{
		if ( o instanceof StoredTestDefs )
		{
			StoredTestDefs o2 = (StoredTestDefs)o;

			return o2.getId() == getId();
		}

		return false;
	}

    public long getId()
    {
        return _testId;
    }

    public String getName()
    {
        return _name;
    }

    public String getURL()
    {
        return _url;
    }

    public String getDescription()
    {
        return _description;
    }

    public StoredTestSelections createSelections(String name, String productName, String url, String description)
    {
        return StoredTestSelections.createSelections(this,getId(),name,productName,url,description);
    }


    public void delete()
    {
        Connection conn = null;

        try
        {
            /**
             * Delete the StoredTestDefs and then delete
             * all of the test selections.
             */
            conn = _pool.getConnection();

            PreparedStatement ps = conn.prepareStatement("DELETE FROM StoredTestdefs WHERE TestId=?");
            ps.setLong( 1, getId() );
            ps.executeUpdate();

            StoredTestSelections[] selections = getTestSelections();
            for (int count=0;count<selections.length;count++)
            {
                selections[count].delete();
            }

            ps.close();
        }
        catch (SQLException e)
        {
            System.err.println("An error occurred while trying to delete the testdefs '"+getId()+"': "+e);
            e.printStackTrace(System.err);
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

    public static StoredTestDefs getStoredTestDefs(long testId)
    {
        StoredTestDefs std = null;
        Connection conn = null;

        try
        {
            conn = _pool.getConnection();
            Statement s = conn.createStatement();

            ResultSet rs = s.executeQuery("SELECT * FROM StoredTestdefs WHERE TestId="+testId);

            if ( rs.next() )
            {
                std = new StoredTestDefs(testId, rs.getString("Name"), rs.getString("URL"), rs.getString("Description"));
            }

            rs.close();
            s.close();
        }
        catch (SQLException e)
        {
            System.err.println("An error occurred while trying to retrieve the testdef '"+testId+"': "+e);
            e.printStackTrace(System.err);
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

        return std;
    }

    public static StoredTestDefs getStoredTestDefs(String name)
    {
        StoredTestDefs std = null;
        Connection conn = null;

        try
        {
            conn = _pool.getConnection();
            PreparedStatement s = conn.prepareStatement("SELECT * FROM StoredTestdefs WHERE Name=?");
            s.setString(1, name);

            ResultSet rs = s.executeQuery();

            if ( rs.next() )
            {
                std = new StoredTestDefs(rs.getInt("TestId"), rs.getString("Name"), rs.getString("URL"), rs.getString("Description"));
            }

            rs.close();
            s.close();
        }
        catch (SQLException e)
        {
            System.err.println("An error occurred while trying to retrieve the testdef '"+name+"': "+e);
            e.printStackTrace(System.err);
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

        return std;
    }

    public static StoredTestDefs[] getStoredTestDefs()
    {
        ArrayList std = new ArrayList();
        Connection conn = null;

        try
        {
            conn = _pool.getConnection();
            Statement s = conn.createStatement();

            ResultSet rs = s.executeQuery("SELECT * FROM StoredTestdefs ORDER BY Name");

            while ( rs.next() )
            {
                std.add( new StoredTestDefs(rs.getLong("TestId"), rs.getString("Name"), rs.getString("URL"), rs.getString("Description")) );
            }

            rs.close();
            s.close();
        }
        catch (SQLException e)
        {
            System.err.println("An error occurred while trying to retrieve all the testdefs: "+e);
            e.printStackTrace(System.err);
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

        StoredTestDefs[] results = new StoredTestDefs[std.size()];
        System.out.println("Number of stored test defs: "+results.length);
        std.toArray(results);

        return results;
    }

	public String writeManagementPanel(String baseUrl, boolean isExpanded, boolean inMyDTF) throws java.io.IOException
	{
		StringBuffer out = new StringBuffer();
		out.append ("<table width=\"100%\" border=\"1\" cellspacing=\"0\" bordercolor=\"#AAAAAA\">"+
			        "<tr><td>"+
					"<table width=\"100%\">"+
					"<tr valign=\"top\"><td width=\"50%\"><font face=\"Tahoma\" size=\"4\" color=\"Black\">"+
					"<a href=\""+baseUrl+"&expandtestdefs="+getId()+"\"><img src=\""+ (isExpanded ? "down.gif" : "up.gif") +"\" border=\"0\"/>"+
					"<b>"+getName()+"</b></a></font><br/>"+
					"&nbsp;&nbsp;[ <a href=\""+getURL()+"\">download</a> | <a href=\""+baseUrl+"&function=delete&testdef="+getId()+"\">delete</a> | ");

		out.append ("<a href=\"default.jsp?page=management&function="+(inMyDTF ? "remove" : "add")+"&testdef="+getId()+"\">"+(inMyDTF ? "remove from":"add to")+" myDTF</a> ]");

		out.append ("</td><td><table border=\"0\" cellspacing=\"0\" width=\"100%\" bgcolor=\"#E7E7E7\">"+
					"<tr><td><font color=\"Black\" face=\"Tahoma\" size=\"2\">"+getDescription()+"</font></td></tr></table></td></tr>");

		if ( isExpanded )
		{
			out.append ("<tr><td align=\"center\">&nbsp;</td><td><form name=\"initiaterun\" action=\""+baseUrl+"&function=selections&testdef="+getId()+"\" method=\"post\">"+
						"<table width=\"100%\" cellspacing=\"0\" cellpadding=\"2\"><tr valign=\"top\"><td align=\"left\" bgcolor=\"black\">"+
						"<font color=\"White\" face=\"Tahoma\" size=\"2\">deployment:</font></td></tr>"+
						"<tr><td><table><tr><td>Product:</td><td>");

			ProductRepositoryInterface pri = DTFManagerBean.getProductRepository();

			out.append ("<select name=\"deploy_product\" "+(pri == null ? "disabled" :"")+">");

			if ( pri != null )
			{
				String[] productNames = pri.getProductNames();
				for (int pCount=0;pCount<productNames.length;pCount++)
				{
					out.append("<option>"+productNames[pCount]+"</option>");
				}
			}
			else
			{
				out.append("<option>No product repository</option>");
			}

			out.append("</select></td></tr></table><table><tr><td>"+
						"<input type=\"radio\" checked name=\"deploytype\" onclick=\"SetState(this,this.form.deployurl)\" value=\"none\"/></td>"+
						"<td>Don't deploy</td></tr><tr><td><input type=\"radio\" name=\"deploytype\" "+(pri == null ? "disabled" : "")+" onclick=\"SetState(this,this.form.deployurl)\" value=\"productconfig\"/>"+
						"</td><td>Redeploy from product repository</td></tr><tr><td><input type=\"radio\" name=\"deploytype\" "+(pri == null ? "disabled" : "")+" onclick=\"SetState(this,this.form.deployurl)\" value=\"userdefined\"/>"+
						"</td><td>Userdefined: <input type=\"text\" name=\"deployurl\" size=\"32\" disabled/></td></tr></table>"+
						"</td></tr><tr valign=\"top\"><td align=\"left\" bgcolor=\"black\"><font color=\"White\" face=\"Tahoma\" size=\"2\">selections:</font></td>"+
					    "</tr><tr valign=\"top\"><td><table><tr><td><input type=\"radio\" name=\"selection_type\" onclick=\"SetSelectionTypeState(this, selection)\" value=\"none\"/>"+
						"</td><td>None</td></tr><tr><td><input type=\"radio\" name=\"selection_type\" checked onclick=\"SetSelectionTypeState(this, selection)\" value=\"selected\"/>"+
						"</td><td><select onChange=\"SelectionChange(this, this.form.deleteButton)\" name=\"selection\" size=\"5\"><option value=\"\">-- Choose --</option>");

			StoredTestSelections[] selections = getTestSelections();

			for (int selCount=0;selCount<selections.length;selCount++)
			{
				out.append("<option value=\""+selections[selCount].getName()+"\">"+selections[selCount].getName()+"</option>");
			}

			out.append ("</select></td></tr></table></td></tr><tr><td><input type=\"submit\" name=\"create\" value=\"create\"/> <input type=\"submit\" name=\"deleteButton\" disabled value=\"delete\"/></td>");
			out.append ("</tr><tr valign=\"top\"><td align=\"left\" bgcolor=\"black\"><font color=\"White\" face=\"Tahoma\" size=\"2\">scheduling:</font>"+
						"</td></tr><tr valign=\"top\"><td align=\"left\"><font color=\"Black\" face=\"Tahoma\" size=\"2\">Priority: <select name=\"priority\"><option value=\""+ScheduleInformation.HIGH_PRIORITY+"\">High</option><option selected value=\""+ScheduleInformation.MEDIUM_PRIORITY+"\">Medium</option><option value=\""+ScheduleInformation.LOW_PRIORITY+"\">Low</option></select>"+
						"</font></td></tr><tr><td><table><tr><td width=\"30\"><input type=\"radio\" checked name=\"scheduletype\" onclick=\"SetScheduleTypeState(this, this.form)\" value=\"whenpossible\"/>"+
						"</td><td width=\"100%\">Run when possible</td></tr><tr><td width=\"30\"><input type=\"radio\" name=\"scheduletype\" onclick=\"SetScheduleTypeState(this, this.form)\" value=\"onceonly\"/>"+
					    "</td><td width=\"100%\">Once Only: <select disabled name=\"onceonly_hour\">");

			for (int hour=0;hour<24;hour++)
			{
				out.append("<option>"+hour+"</option>");
			}

			out.append("</select> : <select disabled name=\"onceonly_minute\">");

			for (int minute=0;minute<60;minute++)
			{
				out.append("<option>"+minute+"</option>");
			}

			out.append ("</select></td></tr><tr><td width=\"30\"><input type=\"radio\" name=\"scheduletype\" onclick=\"SetScheduleTypeState(this, this.form)\" value=\"weekdays\"/>"+
					    "</td><td width=\"100%\">Weekdays: <select disabled name=\"weekdays_hour\">");

			for (int hour=0;hour<24;hour++)
			{
				out.append("<option>"+hour+"</option>");
			}

			out.append("</select> : <select disabled name=\"weekdays_minute\">");


			for (int minute=0;minute<60;minute++)
			{
				out.append("<option>"+minute+"</option>");
			}

			out.append ("</select></td></tr></table></td></tr><tr valign=\"top\"><td align=\"left\" bgcolor=\"black\">"+
					    "<font color=\"White\" face=\"Tahoma\" size=\"2\">options:</font></td></tr><tr><td align=\"left\"><table><tr>"+
					    "<td>email:</td><td width=\"100%\"><input type=\"text\" name=\"emaildistributionlist\" size=\"24\"/></td></tr><tr>"+
						"<td>version:</td><td width=\"100%\"><input type=\"text\" name=\"softwareversion\" size=\"24\"/></td></tr></table>"+
						"</td></tr><tr><td><input type=\"submit\" name=\"set\" value=\"set\"/></td></tr></table></form></td></tr>");
		}

		out.append("</table></td></tr></table>");

		return out.toString();
	}

    public StoredTestSelections getTestSelection( String selectionsName )
    {
        StoredTestSelections result = null;
        Connection conn = null;

        try
        {
            conn = _pool.getConnection();

            PreparedStatement ps = conn.prepareStatement("SELECT * FROM StoredTestSelections WHERE ForTestId=? AND Name=?");

            ps.setLong(1, getId());
            ps.setString(2, selectionsName);

            ResultSet rs = ps.executeQuery();

            if ( rs.next() )
            {
                result = new StoredTestSelections(this,getId(), rs.getString("Name"), rs.getString("ProductName"), rs.getString("URL"), rs.getString("Description") );
            }

            rs.close();
            ps.close();
        }
        catch (SQLException e)
        {
            System.err.println("An error occurred while trying to retrieve the test selections for test '"+getId()+"': "+e);
            e.printStackTrace(System.err);
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

        return result;
    }

    public StoredTestSelections[] getTestSelections()
    {
        ArrayList results = new ArrayList();
        Connection conn = null;

        try
        {
            conn = _pool.getConnection();
            Statement s = conn.createStatement();

            ResultSet rs = s.executeQuery("SELECT * FROM StoredTestSelections WHERE ForTestId="+getId());

            while ( rs.next() )
            {
                results.add(new StoredTestSelections(this, getId(), rs.getString("Name"), rs.getString("ProductName"), rs.getString("URL"), rs.getString("Description") ) );
            }

            rs.close();
            s.close();
        }
        catch (SQLException e)
        {
            System.err.println("An error occurred while trying to retrieve the test selections for test '"+getId()+"': "+e);
            e.printStackTrace(System.err);
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

        StoredTestSelections[] tests = new StoredTestSelections[results.size()];
        results.toArray(tests);

        return tests;
    }

    private static long getNextTestId()
    {
        long nextId = 0;
        Connection conn = null;

        try
        {
            conn = _pool.getConnection();
            Statement s = conn.createStatement();

            ResultSet rs = s.executeQuery("SELECT * FROM NextTestId");

            if ( rs.next() )
            {
                nextId = rs.getLong(1);
                s.executeUpdate("UPDATE NextTestId SET NextId = NextId + 1");
            }
            else
            {
                s.executeUpdate("INSERT INTO NextTestId VALUES (1)");
            }

            rs.close();
            s.close();
        }
        catch (SQLException e)
        {
            System.err.println("An error occurred while trying to increment the next test id: "+e);
            e.printStackTrace(System.err);
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

        return nextId;
    }

    public static StoredTestDefs createStoredTestDefs(HttpServletRequest request)
    {
        StoredTestDefs std = null;

        /**
         * Create a multipart form handler and upload the file into
         * the configured directory.  Then create a stored testdef
         * for this and return it.
         */
        MultipartFormHandler formHandler = new MultipartFormHandler();
        String destDir = DTFRunManager.getUploadFileDirectory();
        formHandler.uploadFiles(request, destDir);
        String rootURL = DTFRunManager.getUploadWebDirectory();
        File uploadedFile = (File)formHandler.getFormDataParameter("upload");
        String newURL = rootURL + '/' + uploadedFile.getName();

        try
        {
            TestDefinitionRepository tdr = new TestDefinitionRepository(new URL(newURL));

            std = createStoredTestDefs(uploadedFile.getName(), newURL, tdr.getDescription());
        }
        catch (Exception e)
        {
            System.err.println("Failed to parse the test definition repository: "+e);
            e.printStackTrace(System.err);
        }

        return std;
    }

    public static StoredTestDefs createStoredTestDefs(String name, String url, String description)
    {
        StoredTestDefs std = null;
        Connection conn = null;

        try
        {
            long id = getNextTestId();

            conn = _pool.getConnection();
            PreparedStatement ps = conn.prepareStatement("INSERT INTO StoredTestdefs VALUES (?,?,?,?)");

            ps.setLong(1, id);
            ps.setString(2, name);
            ps.setString(3, url);
            ps.setString(4, description);

            ps.executeUpdate();

            ps.close();

            std = new StoredTestDefs(id, name, url, description);
        }
        catch (SQLException e)
        {
            System.err.println("An error occurred while trying to create a stored testset definition: "+e);
            e.printStackTrace(System.err);
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

        return std;
    }

    private static void createTables()
    {
        Connection conn = null;

        try
        {
            conn = _pool.getConnection();
            Statement s = conn.createStatement();

            try
            {
                System.out.print("Ensuring 'storedtestdefs' table exists: ");
                s.executeUpdate("CREATE TABLE StoredTestdefs (TestId INT Primary Key, Name VARCHAR(255), URL VARCHAR(255), Description BLOB)");

                System.out.println("Success");
            }
            catch (SQLException e)
            {
                System.out.println("Already exists");
            }

            try
            {
                System.out.print("Ensuring 'nexttestid' table exists: ");
                s.executeUpdate("CREATE TABLE NextTestId (NextId INT)");
                s.executeUpdate("INSERT INTO NextTestIs (0)");

                System.out.println("Success");
            }
            catch (SQLException e)
            {
                System.out.println("Already exists");
            }

            try
            {
                System.out.print("Ensuring 'storedtestselections' table exists: ");
                s.executeUpdate("CREATE TABLE StoredTestSelections (ForTestId INT, Name VARCHAR(255), ProductName VARCHAR(255), URL VARCHAR(255), Description VARCHAR(255))");

                System.out.println("Success");
            }
            catch (SQLException e)
            {
                System.out.println("Already exists");
            }

            s.close();
        }
        catch (SQLException e)
        {
            System.err.println("An error occurred while attempting to create the storedtestdefs table: "+e);
            e.printStackTrace(System.err);
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

    static
    {
        createTables();
    }
}
