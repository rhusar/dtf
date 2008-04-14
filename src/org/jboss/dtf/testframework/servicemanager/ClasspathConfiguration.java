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
 * Copyright (C) 1998, 1999, 2000, 2001, 2002
 *
 * Arjuna Technology Ltd.
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: ClasspathConfiguration.java 170 2008-03-25 18:59:26Z jhalliday $
 */
package org.jboss.dtf.testframework.servicemanager;

import org.jdom.Element;

import java.util.List;
import java.util.ArrayList;
import java.util.HashSet;
import java.net.URL;
import java.net.URLConnection;
import java.io.*;

public class ClasspathConfiguration
{
	private final static String DIRECTORY_CLASSPATH_ELEMENT = "directory";
	private final static String JAR_CLASSPATH_ELEMENT = "jar";

	private final static String DIRECTORY_NAME_ATTRIBUTE = "name";
	private final static String JAR_NAME_ATTRIBUTE = "name";
	private final static String JAR_URL_ATTRIBUTE = "url";

	private final static String LAST_MODIFIED_FILE_SUFFIX = ".lmf";

	private static File		_softwareCache = null;

	private ArrayList	_classpath = null;
	private HashSet		_urls = new HashSet();

	public ClasspathConfiguration(Element config) throws InvalidConfigurationException
	{
		List classpathElements = config.getChildren();

		_classpath = new ArrayList();

		for (int count=0;count<classpathElements.size();count++)
		{
			Element classpathElement = (Element)classpathElements.get(count);

			if ( classpathElement.getName().equalsIgnoreCase(DIRECTORY_CLASSPATH_ELEMENT) )
			{
				_classpath.add( classpathElement.getAttributeValue(DIRECTORY_NAME_ATTRIBUTE) );
			}
			else
			if ( classpathElement.getName().equalsIgnoreCase(JAR_CLASSPATH_ELEMENT) )
			{
				String jarName = classpathElement.getAttributeValue(JAR_NAME_ATTRIBUTE);

				if ( jarName != null )
				{
					_classpath.add( new File(jarName).getAbsolutePath() );
				}
				else
				{
					String url = classpathElement.getAttributeValue(JAR_URL_ATTRIBUTE);
					File localFile = getJAR(url);

					if ( localFile != null )
					{
						_classpath.add( localFile.getAbsolutePath() );
					}
					else
						throw new InvalidConfigurationException("Invalid URL '"+url+"'");
				}
			}
		}
	}

	public String getClasspath()
	{
		String returnValue = "";

		for (int count=0;count<_classpath.size();count++)
		{
			returnValue += (String)_classpath.get(count) + File.pathSeparator;
		}

		return returnValue;
	}

	public void ensureCacheUptodate()
	{
		String[] urls = new String[_urls.size()];
    	_urls.toArray(urls);

		for (int count=0;count<urls.length;count++)
		{
			getJAR(urls[count]);
		}
	}

	private File getJAR(String url)
	{
		File localJar = null;

		try
		{
        	URL u = new URL(url);
			URLConnection connection = u.openConnection();

			String localFilename = u.getFile();

            localJar = new File(_softwareCache, localFilename);

			_urls.add(url);

			if ( !localJar.exists() )
			{
				System.out.println("Remote JAR is not cached - caching...");
				retrieveFileToCache(connection, localJar);
			}
			else
			{
				long cachedLastModified = retrieveCachedLastModified(localFilename);

				System.out.println("Remote JAR exists within the cache");
				if ( connection.getLastModified() > cachedLastModified )
				{
					System.out.println("Newer JAR exists retrieving..");
					retrieveFileToCache(connection, localJar);
				}
			}
		}
		catch (FileNotFoundException e)
		{
			System.err.println("ERROR - URL specified in configuration is not correct: "+e);
			localJar = null;
		}
		catch (Exception e)
		{
			System.err.println("ERROR - "+e);
			e.printStackTrace(System.err);
		}

		return localJar;
	}

	private void retrieveFileToCache(URLConnection conn, File localJar) throws Exception
	{
		BufferedInputStream in = new BufferedInputStream( conn.getInputStream() );
		BufferedOutputStream out = new BufferedOutputStream( new FileOutputStream( localJar ) );
		byte[] buffer = new byte[32768];
		int bytesRead = 0;

		while ( ( bytesRead = in.read(buffer) ) != -1 )
		{
			out.write(buffer,0,bytesRead);
		}

		out.close();
		in.close();

		setCachedLastModified(localJar.getName(), conn.getLastModified());
	}

	private long retrieveCachedLastModified(String localJar)
	{
		long returnValue = 0;

		try
		{
			File localLMF = new File(_softwareCache, localJar + LAST_MODIFIED_FILE_SUFFIX);

			BufferedReader in = new BufferedReader( new InputStreamReader( new FileInputStream(localLMF) ) );

			returnValue = Long.parseLong( in.readLine() );

			in.close();
		}
		catch (java.io.FileNotFoundException e)
		{
			// Ignore
		}
		catch (java.io.IOException e)
		{
			System.err.println("ERROR - "+e);
		}

		return returnValue;
	}

	private void setCachedLastModified(String localJar, long lastModified)
	{
		long returnValue = 0;

		try
		{
			File localLMF = new File(_softwareCache, localJar + LAST_MODIFIED_FILE_SUFFIX);

			PrintStream out = new PrintStream( new FileOutputStream(localLMF) );

			out.println(lastModified);

			out.close();
		}
		catch (java.io.IOException e)
		{
			System.err.println("ERROR - "+e);
		}
	}

	static
	{
		_softwareCache = new File("./softwarecache");
		_softwareCache.mkdirs();
	}
}
