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
//
// Copyright (C) 2001,
//
// HP Arjuna Labs
// Newcastle upon Tyne,
// Tyne and Wear,
// UK.
//
// $Id: InputStreamFileWriter.java 170 2008-03-25 18:59:26Z jhalliday $

package org.jboss.dtf.testframework.utils;

import java.io.InputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.BufferedInputStream;

public class InputStreamFileWriter extends Thread
{
	InputStream			_inStream = null;
	FileOutputStream 	_outStream = null;

	public InputStreamFileWriter(InputStream inStr, String filename) throws java.io.FileNotFoundException, java.io.IOException
	{
		/*
		 * Create the directories required to be able to create this file
		 */
		File inFile = new File(filename);
		String directory = filename.substring(0,filename.indexOf(inFile.getName()));
		File inDir = new File(directory);
		inDir.mkdirs();

		_inStream = new BufferedInputStream(inStr);
		_outStream = new FileOutputStream(inFile);

		start();
	}

	public InputStreamFileWriter(InputStream inStr, File directory, String filename) throws java.io.FileNotFoundException, java.io.IOException
	{
		/*
		 * Create the directories required to be able to create this file
		 */
		directory.mkdirs();
        File inFile = new File(directory, filename);

		_inStream = new BufferedInputStream(inStr);
		_outStream = new FileOutputStream(inFile);

		start();
	}

	public void run()
	{
		try
		{
			// Create 32k buffer
			byte[]	buffer = new byte[32768];
			int bytesRead;

			while ( (bytesRead = _inStream.read(buffer)) != -1)
			{
				if (bytesRead != 0)
				{
					_outStream.write(buffer,0,bytesRead);
					_outStream.flush();
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
        finally
        {
            try
            {
                _inStream.close();
                _outStream.close();
            }
            catch (java.io.IOException e)
            {
                e.printStackTrace(System.err);
            }
        }
	}

	public void close() throws java.io.IOException
	{
        _inStream.close();
		_outStream.close();
	}
}
