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
 * $Id: WildCardProcessor.java 170 2008-03-25 18:59:26Z jhalliday $
 */
package org.jboss.dtf.testframework.utils;

import java.io.File;
import java.io.FilenameFilter;
import java.util.StringTokenizer;

public class WildCardProcessor implements FilenameFilter
{
	private static String _wildElement;

	protected WildCardProcessor(String wildElement)
	{
		_wildElement = wildElement;
	}

	public static File[] processWildcard(String wildDir)
	{
		String wildElement = wildDir.substring( wildDir.lastIndexOf('/') + 1 );
		String dir = wildDir.substring(0, wildDir.lastIndexOf('/') + 1);
		File baseDir = new File(dir);

		return baseDir.listFiles(new WildCardProcessor(wildElement));
	}

	/**
	 * Tests if a specified file should be included in a file list.
	 *
	 * @param   dir    the directory in which the file was found.
	 * @param   name   the name of the file.
	 * @return  <code>true</code> if and only if the name should be
	 * included in the file list; <code>false</code> otherwise.
	 */
	public boolean accept(File dir, String name)
	{
		int namep = 0;
		int wildp = 0;
		boolean finished = false;

		while ( !finished )
		{
			if ( _wildElement.charAt(wildp) == '*' )
			{
				int startPoint = ++wildp;
				finished = wildp == _wildElement.length();
				while ( ( !finished ) && ( _wildElement.charAt(wildp) != '*' ) )
				{
					finished = ++wildp == _wildElement.length();
				}

				String wildSub = _wildElement.substring(startPoint, wildp);

				if ( name.substring(namep).indexOf(wildSub) == - 1 )
				{
					return false;
				}
				namep = name.indexOf(wildSub) + wildSub.length();
			}
			else
			{
				int startPoint = wildp;
				while ( ( !finished ) && ( _wildElement.charAt(wildp) != '*' ) )
				{
					finished = ++wildp == _wildElement.length();
				}

				String wildSub = _wildElement.substring(startPoint, wildp);

				if ( !name.substring(namep).startsWith(wildSub) )
				{
					return false;
				}
				namep = namep + wildSub.length();
			}
		}
		return true;
	}

	public static void main(String[] args)
	{
		File[] files = WildCardProcessor.processWildcard("c:/*.sh");

		for (int count=0;count<files.length;count++)
		{
			System.out.println("File["+count+"] = "+files[count]);
		}
	}
}
