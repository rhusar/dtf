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
 * $Id: DatabaseResultsCollator.java 170 2008-03-25 18:59:26Z jhalliday $
 */
package org.jboss.dtf.testframework.coordinator.resultscollator;

import javax.naming.InitialContext;
import javax.naming.Context;
import javax.sql.DataSource;
import java.io.InputStream;
import java.io.File;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.util.Hashtable;

public class DatabaseResultsCollator implements ResultsCollatorPlugin
{
    private DataSource  _ds = null;

    public boolean initialise()
    {
        boolean success = false;

        try
        {
            InitialContext ctxt = new InitialContext();

            _ds = (DataSource)ctxt.lookup("jdbc/ResultsDB");

            ensureTablesExist();

            success = true;
        }
        catch (Exception e)
        {
            System.err.println("Failed to lookup database datasource: "+e);
        }

        return success;
    }

    private void ensureTablesExist() throws Exception
    {
        System.out.println("Retrieving database connection...");
        Connection conn = _ds.getConnection();
        Statement s = conn.createStatement();

        try
        {
            System.out.print("Ensuring 'taskoutput' table exists: ");
            s.executeUpdate("CREATE TABLE TaskOutput (RunID INT, INDEX(RunID), TestId VARCHAR(255), INDEX(TestId), TaskName VARCHAR(255), INDEX(TaskName), PermCode VARCHAR(255), OutputType VARCHAR(255), ResultData BLOB)");
            System.out.println("Success");
        }
        catch (SQLException e)
        {
            System.out.println("Already exists");
        }

        s.close();
        conn.close();
    }

    public void processResults(   long          runId,
                                  String        testId,
                                  String        taskName,
                                  String        permCode,
                                  String        outputType,
                                  int           streamLength,
                                  InputStream   in) throws Exception
    {
        Connection conn = null;
        try
        {
            System.out.println("Retrieving database connection...");
            conn = _ds.getConnection();

            System.out.println("Updating table...");
            PreparedStatement s = conn.prepareStatement("INSERT INTO TaskOutput VALUES (?,?,?,?,?,?)");
            s.setLong(1, runId);
            s.setString(2, testId);
            s.setString(3, taskName);
            s.setString(4, permCode);
            s.setString(5, outputType);
            s.setBinaryStream(6, in, streamLength);
            s.execute();

            conn.close();
        }
        catch (SQLException e)
        {
            throw new Exception("Failed to update the database: "+e);
        }
        finally
        {
            if ( conn != null )
            {
                conn.close();
            }
        }
    }
}
