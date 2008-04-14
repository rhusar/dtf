package org.jboss.dtf.tools;/*
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
 * $Id: ArchiveTaskOutput.java 170 2008-03-25 18:59:26Z jhalliday $
 */

import javax.sql.DataSource;
import javax.naming.InitialContext;
import javax.naming.Context;
import java.sql.*;
import java.util.zip.ZipOutputStream;
import java.util.zip.ZipEntry;
import java.util.Hashtable;
import java.io.FileOutputStream;
import java.io.InputStream;

public final class ArchiveTaskOutput
{
    public ArchiveTaskOutput(boolean delete, String location, String archive)
    {
        long numberOfRecords = 0;
        long numberOfErrors = 0;
        DataSource ds = null;

        try
        {
            ZipOutputStream zOut = null;

            Hashtable env = new Hashtable();
            env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.fscontext.RefFSContextFactory");
            InitialContext initCtx = new InitialContext(env);

            ds = (DataSource)initCtx.lookup(location);

            String filename = null;
            Connection conn = ds.getConnection();
            Statement s = conn.createStatement();
            ResultSet rs = s.executeQuery("SELECT * FROM TaskOutput ORDER BY RunID, TestId, TaskName, PermCode");

            while ( rs.next() )
            {
                /**
                 * If its the first time through then create the zip archive
                 */
                if ( zOut == null)
                {
                    try
                    {
                        System.out.println("Creating '"+archive+"'...");
                        zOut = new ZipOutputStream( new FileOutputStream( archive ) );
                    }
                    catch (Exception e)
                    {
                        throw new Exception("Failed to create ZIP file '"+archive+"' reason: "+e);
                    }
                }

                try
                {
                    filename =  "Run_"+rs.getLong("RunID") +
                                  "/"+rs.getString("TestId") +
                                  "/"+rs.getString("PermCode") +
                                  "/"+rs.getString("TaskName") + "_" + rs.getString("OutputType");

                    ZipEntry zEntry = new ZipEntry(filename);
                    zOut.putNextEntry(zEntry);

                    int len;
                    byte[] buf = new byte[32768];
                    Blob b = rs.getBlob("ResultData");
                    InputStream bIn = b.getBinaryStream();

                    while ((len = bIn.read(buf)) > 0)
                    {
                        zOut.write(buf, 0, len);
                    }

                    zOut.closeEntry();
                    bIn.close();

                    /**
                     * If the user requested that the data be delete from the table
                     * then do so
                     */
                    if ( delete )
                    {
                        PreparedStatement ps = conn.prepareStatement("DELETE FROM TaskOutput WHERE RunId=? AND TestId=? AND TaskName=? AND PermCode=? AND OutputType=?");

                        ps.setLong( 1, rs.getLong("RunID") );
                        ps.setString( 2, rs.getString("TestId") );
                        ps.setString( 3, rs.getString("TaskName") );
                        ps.setString( 4, rs.getString("PermCode") );
                        ps.setString( 5, rs.getString("OutputType") );

                        ps.execute();
                    }
                    numberOfRecords++;
                }
                catch (Exception e)
                {
                    System.err.println("Failed to archive '"+filename+"', reason: "+e);
                    numberOfErrors++;
                }
            }

            if ( zOut != null )
            {
                zOut.finish();
                zOut.close();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
        }

        System.out.println("Number of records archived: "+numberOfRecords);
        System.out.println("Number of errors generated: "+numberOfErrors);
    }

    public static void main(String[] args)
    {
        String  location = null,
                archive = null;
        boolean delete = false;

        for (int count=0;count<args.length;count++)
        {
            if ( args[count].equalsIgnoreCase("-dblocation") )
            {
                location = args[count + 1];
            }

            if ( args[count].equalsIgnoreCase("-archive") )
            {
                archive = args[count + 1];
            }

            if ( args[count].equalsIgnoreCase("-delete") )
            {
                delete = true;
            }
        }

        if ( (location == null) || (archive == null) )
        {
            System.out.println("Usage: org.jboss.dtf.tools.ArchiveTaskOutput -dblocation <jndi-location> -archive <filename.zip> {-delete}");
        }
        else
        {
            new ArchiveTaskOutput(delete, location, archive);
        }
    }
}
