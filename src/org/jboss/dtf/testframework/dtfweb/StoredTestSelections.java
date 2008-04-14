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
import org.jboss.dtf.testframework.nameservice.NameServiceInterface;
import org.jboss.dtf.testframework.productrepository.ProductRepositoryInterface;
import org.jboss.dtf.testframework.testnode.RunUID;
import org.jboss.dtf.testframework.coordinator2.CoordinatorInterface;
import org.jboss.dtf.testframework.coordinator2.CoordinatorBusyException;
import org.jboss.dtf.testframework.coordinator2.scheduler.ScheduleInformation;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.io.File;
import java.net.URL;

public class StoredTestSelections
{
    private static DataSource  _pool = DBUtils.getDataSource();

    private long            _forTestId;
    private String          _name;
    private String          _url;
    private String          _description;
    private String          _productName;
    private StoredTestDefs  _testDef;

    protected StoredTestSelections(StoredTestDefs testDef, long forTestId, String name, String productName, String url, String description)
    {
        _testDef = testDef;
        _productName = productName;
        _forTestId = forTestId;
        _name = name;
        _url = url;
        _description = description;
    }

    public long getForTestId()
    {
        return _forTestId;
    }

    public String getProductName()
    {
        return _productName;
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

    public StoredTestDefs getTestDefinitions()
    {
        return _testDef;
    }

    public void delete()
    {
        Connection conn = null;

        try
        {
            conn = _pool.getConnection();

            PreparedStatement ps = conn.prepareStatement("DELETE FROM StoredTestSelections WHERE ForTestId=? AND Name=?");

            ps.setLong( 1, getForTestId() );
            ps.setString( 2, getName() );

            ps.executeUpdate();

            ps.close();

            File selectionsFile = new File( DTFRunManager.getUploadFileDirectory()+"/"+getForTestId()+"_"+getName()+".xml" );
            selectionsFile.delete();
        }
        catch (SQLException e)
        {
            System.err.println("An error occurred while trying to delete the test selections for test '"+getForTestId()+"': "+e);
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


    public static StoredTestSelections createSelections(StoredTestDefs testDef, long id, String name, String productName, String url, String description)
    {
        StoredTestSelections sel = null;
        Connection conn = null;
        try
        {
            conn = _pool.getConnection();
            PreparedStatement ps = conn.prepareStatement("INSERT INTO StoredTestSelections VALUES ( ?,?,?,?,? )");

            ps.setLong(1, id);
            ps.setString(2, name);
            ps.setString(3, productName);
            ps.setString(4, url);
            ps.setString(5, description);

            ps.executeUpdate();

            sel = new StoredTestSelections(testDef, id, name, productName, url, description);
        }
        catch (SQLException e)
        {
            System.err.println("Failed to create selections: "+e);
            e.printStackTrace(System.err);

            sel = null;
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

        return sel;
    }

    public void run(ScheduleInformation scheduleInfo) throws CoordinatorBusyException
    {
        NameServiceInterface nsi = DTFManagerBean.getNameServiceInterface();

        if ( nsi != null )
        {
            try
            {
                CoordinatorInterface coordinator = (CoordinatorInterface)nsi.lookup("Coordinator");
                StoredTestDefs testDefs = getTestDefinitions();
                ProductRepositoryInterface productRepository = DTFManagerBean.getProductRepository();

                String productVersion;

                /** If we have been passed a valid software version use that, otherwise generate one **/
                if ( ( scheduleInfo.getSoftwareVersion() == null ) || ( scheduleInfo.getSoftwareVersion().length() == 0) )
                {
                    scheduleInfo.setSoftwareVersion( getProductName() +"_v"+productRepository.getCurrentVersion(getProductName()) );
                }

                System.out.println("Initiating TestDefs:'"+testDefs.getURL()+"', Selections:'"+getURL()+"'");
                coordinator.getScheduler().schedule(coordinator, scheduleInfo);
            }
            catch (Exception e)
            {
                System.err.println("Failed to initiate test run: "+e);
                e.printStackTrace(System.err);
            }
        }
    }

	public static void runScheduledItem(ScheduleInformation scheduleInfo)
	{
		NameServiceInterface nsi = DTFManagerBean.getNameServiceInterface();

		if ( nsi != null )
		{
			try
			{
				CoordinatorInterface coordinator = (CoordinatorInterface)nsi.lookup("Coordinator");
				coordinator.getScheduler().schedule(coordinator, scheduleInfo);
			}
			catch (Exception e)
			{
				System.err.println("Failed to initiate test run: "+e);
				e.printStackTrace(System.err);
			}
		}
	}
}
