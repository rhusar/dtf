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
 * Copyright (C) 1998, 1999, 2000, 2001, 2002, 2003
 *
 * Arjuna Technologies Ltd.
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: ArchiveInformation.java 170 2008-03-25 18:59:26Z jhalliday $
 */
package org.jboss.dtf.testframework.dtfweb;

import org.jboss.dtf.testframework.dtfweb.utils.DBUtils;
import org.jboss.dtf.testframework.testnode.RunUID;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

public class ArchiveInformation
{
	private int					_archiveId;
	private java.sql.Timestamp	_dateTimeCreated;
	private String				_title;
	private String				_comments;

	protected ArchiveInformation(int archiveId, java.sql.Timestamp dateTimeCreate, String title, String comments)
	{
		_archiveId = archiveId;
		_dateTimeCreated = dateTimeCreate;
		_title = title;
		_comments = comments;
	}

	public int getArchiveId()
	{
		return _archiveId;
	}

	public java.sql.Timestamp getDateTimeCreated()
	{
		return _dateTimeCreated;
	}

	public String getTitle()
	{
		return _title;
	}

	public String getComments()
	{
		return _comments;
	}

	public RunUID[] getRunIds()
	{
		ArrayList runIds = new ArrayList();
        Connection conn = null;

		try
		{
			conn = DBUtils.getDataSource().getConnection();

			PreparedStatement ps = conn.prepareStatement("SELECT * FROM archivedruns WHERE ArchiveId=?");

			ps.setInt(1, _archiveId);

			ResultSet rs = ps.executeQuery();

			while ( rs.next() )
			{
				runIds.add(new RunUID(rs.getInt("RunId")));
			}

			rs.close();
			ps.close();
		}
		catch (Exception e)
		{
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
                catch (java.sql.SQLException e)
                {
                    e.printStackTrace(System.err);
                }
            }
        }

		RunUID[] results = new RunUID[runIds.size()];
		runIds.toArray(results);

		return results;
	}

	public static ArchiveInformation[] getArchives()
	{
		ArrayList archives = new ArrayList();
        Connection conn = null;

		try
		{
			conn = DBUtils.getDataSource().getConnection();

			PreparedStatement ps = conn.prepareStatement("SELECT * FROM archivedresults");

			ResultSet rs = ps.executeQuery();

			while ( rs.next() )
			{
				ArchiveInformation info = new ArchiveInformation(rs.getInt("ArchiveId"), rs.getTimestamp("DateTime"), rs.getString("Name"), rs.getString("Comments"));

				archives.add(info);
			}

			rs.close();
			ps.close();
		}
		catch (Exception e)
		{
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
                catch (Exception e)
                {
                    e.printStackTrace(System.err);
                }
            }
        }

		ArchiveInformation[] results = new ArchiveInformation[archives.size()];
		archives.toArray(results);

		return results;
	}

	public static ArchiveInformation getArchiveInformation(int archiveId)
	{
		ArchiveInformation info = null;

        Connection conn = null;

		try
		{
            conn = DBUtils.getDataSource().getConnection();

			PreparedStatement ps = conn.prepareStatement("SELECT * FROM archivedresults WHERE ArchiveId=?");

			ps.setInt(1, archiveId);

			ResultSet rs = ps.executeQuery();

			if ( rs.next() )
			{
				info = new ArchiveInformation(archiveId, rs.getTimestamp("DateTime"), rs.getString("Name"), rs.getString("Comments"));
			}

			rs.close();

			ps.close();

		}
		catch (Exception e)
		{
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
                catch (java.sql.SQLException e)
                {
                    e.printStackTrace(System.err);
                }
            }
        }

		return info;
	}

	public static void deleteArchive(int archiveId)
	{
		ArchiveInformation info = null;
        Connection conn = null;

		try
		{
			conn = DBUtils.getDataSource().getConnection();

			PreparedStatement ps = conn.prepareStatement("DELETE FROM archivedresults WHERE ArchiveId=?");

			ps.setInt(1, archiveId);
			ps.executeUpdate();
			ps.close();

			ps = conn.prepareStatement("DELETE FROM archivedruns WHERE ArchiveId=?");

			ps.setInt(1, archiveId);
			ps.executeUpdate();
			ps.close();
		}
		catch (Exception e)
		{
			e.printStackTrace(System.err);
		}
        finally
        {
            try
            {
                conn.close();
            }
            catch (java.sql.SQLException e)
            {
                e.printStackTrace(System.err);
            }
        }
	}
}
