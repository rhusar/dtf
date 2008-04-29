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
/*
 * Copyright (C) 2001, 2002,
 *
 * Hewlett-Packard Arjuna Labs,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: UserDefinedGraphInformation.java 170 2008-03-25 18:59:26Z jhalliday $
 */
package org.jboss.dtf.testframework.dtfweb.performance;

import org.jboss.dtf.testframework.dtfweb.utils.DBUtils;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.sql.*;

public class UserDefinedGraphInformation
{
    protected String        _dataSetName = null;
    protected java.sql.Date _dateTimeCreated = null;
    protected int           _numberOfSeries = 0;

    public final String getName()
    {
        return(_dataSetName);
    }

    public final java.sql.Date getDateTimeCreated()
    {
        return(_dateTimeCreated);
    }

    public final int getNumberOfSeries()
    {
        return(_numberOfSeries);
    }

    public final static ArrayList list()
    {
        ArrayList returnList = null;
        Connection conn = null;

        try
        {
            DataSource ds = DBUtils.getDataSource();
            conn = ds.getConnection();
            Statement s = conn.createStatement();
            ResultSet rs = s.executeQuery("SELECT * FROM SavedPerfSets");

            returnList = new ArrayList();

            while (rs.next())
            {
                UserDefinedGraphInformation udgInfo = new UserDefinedGraphInformation();
                udgInfo._dataSetName = rs.getString("Name");
                udgInfo._dateTimeCreated = rs.getDate("CreatedDateTime");

                PreparedStatement ps = conn.prepareStatement("SELECT Count(*) FROM SavedPerfSetsData WHERE Name=?");

                ps.setString(1, udgInfo._dataSetName);

                ResultSet rs2 = ps.executeQuery();

                if (rs2.next())
                {
                    udgInfo._numberOfSeries = rs2.getInt(1);
                }

                rs2.close();
                ps.close();

                returnList.add(udgInfo);
            }

            rs.close();
            s.close();
        }
        catch (SQLException e)
        {
            System.err.println("ERROR - Failed to load the performance run list");
            e.printStackTrace(System.err);
            returnList = null;
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

        return(returnList);
    }

}
