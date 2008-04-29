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

import java.net.MalformedURLException;
import java.net.URL;
import java.io.IOException;
import java.io.BufferedInputStream;
import java.io.FileOutputStream;

public class HTTPDownload
{
    public static String downloadFileToLocalArea(String url) throws MalformedURLException, IOException
	{
		URL dwnldFile = new URL(url);

		BufferedInputStream in = new BufferedInputStream(dwnldFile.openStream());
		System.out.println("Creating local copy "+stripFilename(dwnldFile.getFile()));
		FileOutputStream out = new FileOutputStream(stripFilename(dwnldFile.getFile()));
		byte[] buffer = new byte[32768];
		int bytesRead;

		while ( ( bytesRead = in.read(buffer,0,buffer.length) ) != -1 )
		{
			out.write(buffer,0,bytesRead);
		}
		System.out.println("Complete.");
		out.close();
		in.close();

		return(stripFilename(dwnldFile.getFile()));
	}

    private static String stripFilename(String filename)
	{
		if (filename.indexOf('/')!=-1)
		{
			filename = filename.substring(filename.lastIndexOf('/')+1);
		}

		return(filename);
	}
}
