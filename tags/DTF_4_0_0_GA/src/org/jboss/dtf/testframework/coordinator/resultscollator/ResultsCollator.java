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
// $Id: ResultsCollator.java 170 2008-03-25 18:59:26Z jhalliday $

package org.jboss.dtf.testframework.coordinator.resultscollator;

import org.jboss.dtf.testframework.utils.FileCaretaker;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.io.File;
import java.io.FileFilter;

public class ResultsCollator extends Thread implements FileFilter
{
	private final static String MAX_DAYS_OLD_PROPERTY = "results.maxage";
	private final static long   MS_IN_A_DAY = 1000 * 60 * 60 * 24;
	private final static String MAX_DAYS_OLD_DEFAULT_VALUE = "3";

	private static FileCaretaker 			_careTaker = null;

	private static final int DEFAULT_COLLATOR_PORT = 8001;
    public  static ArrayList Plugins = new ArrayList();

	private int				_port;
	private boolean			_running = true;
	private String			_directory = null;

	public ResultsCollator(int port, String directory)
	{
		_port = port;
		_directory = directory;

		// create the directory if it does not exist already
		File resultsDir = new File(directory) ;
		if (!resultsDir.isDirectory()) {
			resultsDir.mkdirs() ;
		}

		start();
	}


	/**
	 * Tests whether or not the specified abstract pathname should be
	 * included in a pathname list.
	 *
	 * @param  pathname  The abstract pathname to be tested
	 * @return  <code>true</code> if and only if <code>pathname</code>
	 *          should be included
	 */
	public boolean accept(File pathname)
	{
		return pathname.getName().startsWith("Run_");
	}

	public void run()
	{
		try
		{
			ServerSocket server = new ServerSocket(_port);

			System.out.println("Ready");

			if ( _careTaker == null )
			{
				int maxAge = Integer.parseInt(System.getProperty(MAX_DAYS_OLD_PROPERTY, MAX_DAYS_OLD_DEFAULT_VALUE));

				_careTaker = new FileCaretaker(maxAge * MS_IN_A_DAY, new File(_directory), this);
			}

			/*
			 * Wait for connection from a TestNode
			 */
			while (_running)
			{
				/*
				 * Spawn client handler to handle this connection
				 */
				ResultsCollatorClientHandler plugin = new ResultsCollatorClientHandler(server.accept(),_directory);

				_careTaker.sweep();
			}

			/*
			 * Close the server socket
			 */
			server.close();

		}
		catch (java.io.IOException e)
		{
			System.out.println("ERROR - IOException from server socket");
			e.printStackTrace(System.err);
			System.exit(0);
		}
        catch (Exception e)
        {
            System.out.println("ERROR - Unexpected exception thrown '"+e+"'");
            System.exit(0);
        }
	}

    public static boolean addPlugin(String classname)
    {
        boolean result = false;

        try
        {
            ResultsCollatorPlugin plugin = (ResultsCollatorPlugin)Class.forName(classname).newInstance();

            if ( plugin.initialise() )
            {
                System.out.println("Plugin '"+classname+"' initialised successfully");
                Plugins.add(plugin);
                result = true;
            }
            else
            {
                System.err.println("Plugin '"+classname+"' failed to initialise correctly, ignoring");
            }
        }
        catch (Exception e)
        {
            System.err.println("Failed to initialise plugin '"+classname+"': "+e);
        }

        return result;
    }

	public static void main(String args[])
	{
		String directory = ".";
		int port = DEFAULT_COLLATOR_PORT;

        addPlugin("org.jboss.dtf.testframework.coordinator.resultscollator.FileResultsCollator");

		for (int count=0;count<args.length;count++)
		{

			if (args[count].equalsIgnoreCase("-help"))
			{
				System.out.println("Usage: ResultsCollator {-help} {-dir [RESULT_DIRECTORY]} {-port [PORT NUMBER]} {-plugin [CLASSNAME]}");
				System.exit(0);
			}
			else
			{
				if ( (args[count].equalsIgnoreCase("-dir")) && (count+1<args.length) )
				{
					directory = args[count+1];
				}
				else
				{
					if ( (args[count].equalsIgnoreCase("-port")) && (count+1<args.length) )
					{
						port = Integer.parseInt(args[count+1]);
					}
                    else
                    {
                        if ( (args[count].equalsIgnoreCase("-plugin")) && (count+1<args.length) )
                        {
                            addPlugin(args[count+1]);
                        }
                    }
				}
			}
		}

		System.out.println("Starting results collator on port "+port+" logging to "+directory);
		new ResultsCollator(port,directory);
	}
}
