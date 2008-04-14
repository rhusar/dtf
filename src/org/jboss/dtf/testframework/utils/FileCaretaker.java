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
package org.jboss.dtf.testframework.utils;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;

/*
 * Copyright (C) 1998, 1999, 2000, 2001, 2002, 2003
 *
 * Arjuna Technologies Ltd.
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: FileCaretaker.java 170 2008-03-25 18:59:26Z jhalliday $
 */

/**
 * The FileCaretaker watches a directory and deletes files which are older than
 * a specified age (in ms).FileFilters and FilenameFilters can be used to delete
 * only selected files.
 */
public class FileCaretaker
{
	private long _maxAge;
	private FileFilter _filter = null;
	private FilenameFilter _fileFilter = null;
	private File _directory;
    private Thread _thrd = null;

	protected FileCaretaker(long maxAge, File dir)
	{
		_maxAge = maxAge;

		_directory = dir;
	}

	public FileCaretaker(long maxAge, File dir, FileFilter filter)
	{
		this(maxAge, dir);

		_filter = filter;

		sweep();
	}

	public FileCaretaker(long maxAge, File dir, FilenameFilter filter)
	{
		this(maxAge, dir);

        _fileFilter = filter;

		sweep();
	}

	/**
	 * Set up and start a Thread to delete all files which are out of date.
	 */
	public void sweep()
	{
		if ( _thrd == null || !_thrd.isAlive() )
		{
        	_thrd = new Thread() {

				public void run()
				{
					File[] files;

					if ( _filter != null )
					{
						files = _directory.listFiles(_filter);
					}
					else
					{
						files = _directory.listFiles(_fileFilter);
					}

					long timeNow = System.currentTimeMillis();

					if ( files != null )
					{
						for (int count=0;count<files.length;count++)
						{
							if ( ( timeNow - files[count].lastModified() ) > _maxAge )
							{
								System.out.println("Deleting old file '"+files[count].getName()+"'");

								if ( files[count].isDirectory())
								{
									deleteDirectory(files[count]);
								}
								else
								{
									files[count].delete();
								}
							}
						}
					}
				}
			};

			_thrd.start();
		}
	}

	private void deleteDirectory(File dir)
	{
		File[] files = dir.listFiles();

		for (int count=0;count<files.length;count++)
		{
			if ( files[count].isDirectory() )
			{
				deleteDirectory(files[count]);
			}

			files[count].delete();
		}

		dir.delete();
	}
}
