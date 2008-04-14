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
package org.jboss.dtf.testframework.utils.fsmonitors;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

/*
 * Copyright (C) 1998, 1999, 2000, 2001, 2002, 2003
 *
 * Arjuna Technologies Ltd.
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: DirectoryMonitor.java 170 2008-03-25 18:59:26Z jhalliday $
 */

public class DirectoryMonitor extends Thread
{
	private final static String THREAD_NAME = "DirectoryMonitor";
	private final static int    DEFAULT_UPDATE_PERIOD = 10;
	private final static long   MS_IN_A_SECOND = 1000;

	private ArrayList	_directories = new ArrayList();
    private int			_updatePeriod = DEFAULT_UPDATE_PERIOD;
    private boolean		_running = true;

	public DirectoryMonitor()
	{
		setName(THREAD_NAME);

		start();
	}

	/**
	 * Change the number of seconds between checks
	 * @param time
	 */
	public void setUpdatePeriod(int time)
	{
		if ( time > 0 )
		{
			_updatePeriod = time;
		}
	}

	/**
	 * Add a directory to monitor.
	 * @param dir The directory to monitor
	 * @return True if the directory is successfully added to the monitor.
	 */
	public boolean addDirectory(File dir, DirectoryChangeListener listener)
	{
		if ( dir.isDirectory() )
		{
    		_directories.add(new DirectoryInformation(dir, listener));
		}
		else
			return false;

		return true;
	}

	public void run()
	{
		System.out.println("Directory monitor started");

    	while (_running)
		{
			checkDirectories();

			try
			{
				Thread.sleep(_updatePeriod * MS_IN_A_SECOND);
			}
			catch (InterruptedException e)
			{
				// Ignore
			}
		}
	}

	private void checkDirectories()
	{
		for (int count=0;count<_directories.size();count++)
		{
        	DirectoryInformation info = (DirectoryInformation)_directories.get(count);

        	// check one directory for changes
			info.check();
		}
	}

	private class DirectoryInformation implements Comparator
	{
		private DirectoryChangeListener _listener;
		private File					_directory;
        private CachedFileInformation[]	_contents = null;

		public DirectoryInformation(File dir, DirectoryChangeListener listener)
		{
			_directory = dir;
			_listener = listener;

			check();
		}

		public int compare(Object o1, Object o2)
		{
			File a = (File)o1;
			File b = (File)o2;

			return a.getName().compareTo(b.getName());
		}

		public void check()
		{
            if ( _contents == null )
			{
				File[] newContents = _directory.listFiles();
				Arrays.sort(newContents, this);

				_contents = new CachedFileInformation[newContents.length];
				for (int count=0;count<newContents.length;count++)
				{
					_contents[count] = new CachedFileInformation(newContents[count]);
				}
			}
			else
			{
				File[] newContentsFiles = _directory.listFiles();
				Arrays.sort(newContentsFiles, this);

				CachedFileInformation[] newContents = new CachedFileInformation[newContentsFiles.length];
				for (int count=0;count<newContentsFiles.length;count++)
				{
					newContents[count] = new CachedFileInformation(newContentsFiles[count]);
				}

                int newPointer = 0;
				int oldPointer = 0;
				boolean newComplete = false;
				boolean oldComplete = false;

				// Since RB uses pointers to move through the lists, there are several important special cases here:
				// newContents.length == 0 && _contents.length == 0 (nothing to check)
				// newContents.length > 0 && _contents.length == 0 (all files in new contents added)
				// newContents.length == 0 && _contents.length > 0 (all files in new contents deleted)
				// These have to be take care of specially

				if (newContents.length == 0 && _contents.length == 0)
				{
					// nothing to advise - no changes in file sets
					return ;
				}

				if ((newContents.length > 0) && (_contents.length == 0))
				{
					// all files in newContents are added files
					for (int count=0; count < newContents.length; count++)
					{
						getListener().fileAdded(newContents[count]) ;
					}
					_contents = newContents ;
					return ;
				}

				if ((newContents.length == 0) && (_contents.length > 0))
				{
					// all files in newContents have been deleted
					for (int count=0; count < _contents.length; count++)
					{
						getListener().fileDeleted(_contents[count]) ;
					}
					_contents = newContents ;
					return ;
				}


				while ( !newComplete || !oldComplete )
				{
					/** If the names are the same then compare date/times **/
					if ( newContents[newPointer].compareTo(_contents[oldPointer]) == 0 )
					{
						/** If the date is different then report **/
                        if ( _contents[oldPointer].getCachedLastModified() != newContents[newPointer].getUpdatedLastModified()  )
						{
							getListener().fileChanged(newContents[newPointer]);
						}

						if ( ++newPointer == newContents.length )
						{
							newComplete = true;
							newPointer = newContents.length - 1;
						}
						if ( ++oldPointer == _contents.length )
						{
							oldComplete = true;
							newPointer = _contents.length - 1;
						}
					}
					else
					{
						/** If the new contents is before the old contents we have a new file **/
						if ( newContents[newPointer].compareTo(_contents[oldPointer]) < 0 )
						{
                            getListener().fileAdded(newContents[newPointer]);
							if ( ++newPointer == newContents.length )
							{
								newComplete = true;
								newPointer = newContents.length - 1;
							}
						}
						else
						{
							/** If the new contents is before the old contents we have a deleted file **/
							if ( newContents[newPointer].compareTo(_contents[oldPointer]) > 0 )
							{
								getListener().fileDeleted(_contents[oldPointer]);
								if ( ++oldPointer == _contents.length )
								{
									oldComplete = true;
									newPointer = _contents.length - 1;
								}
							}
						}
					}
				}
				_contents = newContents;
			}
		}

		public File getDirectory()
		{
			return _directory;
		}

		public DirectoryChangeListener getListener()
		{
			return _listener;
		}
	}
}
