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
package org.jboss.dtf.tools.simplemerge;

import org.apache.tools.ant.Task;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.types.FileSet;

import java.util.ArrayList;
import java.io.*;

/*
 * Copyright (C) 1998, 1999, 2000, 2001, 2002, 2003
 *
 * Arjuna Technology Ltd.
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: SimpleMerge.java 170 2008-03-25 18:59:26Z jhalliday $
 */

public class SimpleMerge extends Task
{
	private ArrayList	_files = new ArrayList();
	private String		_dest = null;

	public void addFileset(FileSet f)
	{
		_files.add(f);
	}

	public void setDest(String filename)
	{
		_dest = filename;
	}

	private void copyTestGroups(PrintStream out, File f) throws java.io.IOException
	{
		BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
		String inLine;
		boolean copy = false;

		while ( ( inLine = in.readLine() ) != null )
		{
			if ( inLine.indexOf("<test_group") != -1 && !copy )
			{
				copy = true;
			}
			else
			if ( inLine.indexOf("</test_set>") != -1 && copy )
			{
				copy = false;
			}

			if ( copy )
			{
				out.println(inLine);
			}
		}

		in.close();
	}

	public void execute() throws BuildException
	{
    	if ( _dest == null )
		{
			throw new BuildException("'Dest' attribute not specified");
		}

		if ( _files.size() == 0 )
		{
			throw new BuildException("No files specified for merging");
		}

		try
		{
			PrintStream out = new PrintStream(new FileOutputStream(_dest));

			out.println("<test_set>\n\t<default_timeout value=\"480\"/>\n\t<description>Test Definitions merged</description>");

			for (int count=0;count<_files.size();count++)
			{
				FileSet fs = (FileSet)_files.get(count);

				DirectoryScanner ds = fs.getDirectoryScanner(this.getProject());
				String[] files = ds.getIncludedFiles();

				for (int fCount=0;fCount<files.length;fCount++)
				{
					File f = new File(ds.getBasedir(), files[fCount]);

					copyTestGroups(out, f);
				}
			}

			out.println("</test_set>");

			out.close();
		}
		catch (Exception e)
		{
			throw new BuildException("An unexpected exception occurred: "+e);
		}
	}
}
