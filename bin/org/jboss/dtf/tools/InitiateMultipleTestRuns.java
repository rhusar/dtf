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
 * $Id: InitiateMultipleTestRuns.java 170 2008-03-25 18:59:26Z jhalliday $
 */

import org.jboss.dtf.testframework.coordinator.InitiateTestRun;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;

public class InitiateMultipleTestRuns
{
    private final static String TEST_DEFINITIONS_SUFFIX = "-testdefs.xml";
    private final static String TEST_SELECTIONS_SUFFIX = "-selections.xml";

    private final static String URL_PARAMETER = "-url";
    private final static String NAME_SERVICE_PARAMETER = "-nameservice";
    private final static String VERSION_PARAMETER = "-version";
    private final static String DISTRIBUTION_PARAMETER = "-distribution";
    private final static String WAIT_PARAMETER = "-wait";
	private final static String RUN_WHEN_POSSIBLE_PARAMETER = "-runwhenpossible";
    private final static String HELP_PARAMETER = "-help";

    private final static String DESCRIPTION_FILENAME = "DESCR";

    public static void main(String[] args)
    {
        ArrayList testsToRun = new ArrayList();
        HashSet foundTestDefsList = new HashSet();
        HashSet foundSelectionsList = new HashSet();

        try
        {
            String url = null;
            String nameService = null;
            String softwareVersion = null;
            String distributionList = null;
            boolean wait = false;
			boolean runWhenPossible = false;

            for (int count=0;count<args.length;count++)
            {
				if ( args[count].equalsIgnoreCase( RUN_WHEN_POSSIBLE_PARAMETER ) )
				{
					runWhenPossible = true;
				}

                if ( args[count].equalsIgnoreCase( URL_PARAMETER ) )
                {
                    url = args[count + 1];
                }

                if ( args[count].equalsIgnoreCase( NAME_SERVICE_PARAMETER ) )
                {
                    nameService = args[count + 1];
                }

                if ( args[count].equalsIgnoreCase( VERSION_PARAMETER ) )
                {
                    softwareVersion = args[count + 1];
                }

                if ( args[count].equalsIgnoreCase( DISTRIBUTION_PARAMETER ) )
                {
                    distributionList = args[count + 1];
                }

                if ( args[count].equalsIgnoreCase( WAIT_PARAMETER ) )
                {
                    wait = true;
                }

                if ( args[count].equalsIgnoreCase( HELP_PARAMETER ) )
                {
                    System.out.println("Usage: InitiaiteMultipleTestRuns {-url [Description File URL]} {-nameservice [Name Service URI]} {-version [Software Version]} {-distribution [Distribution List]} {-wait}");
                    System.exit(0);
                }
            }

            if ( url == null )
            {
                System.out.println("Please ensure you specify the URL parameter ("+URL_PARAMETER+")");
                System.exit(1);
            }

            if ( nameService == null )
            {
                System.out.println("Please ensure you specify the name service parameter ("+NAME_SERVICE_PARAMETER+")");
                System.exit(1);
            }

            if ( softwareVersion == null )
            {
                System.out.println("Please ensure you specify the software version parameter ("+VERSION_PARAMETER+")");
                System.exit(1);
            }

            URL descrURL = new URL(url+"/"+DESCRIPTION_FILENAME);

            BufferedReader in = new BufferedReader(new InputStreamReader(descrURL.openStream()));
            String inLine;

            while ((inLine = in.readLine()) != null)
            {
                if (inLine.endsWith(TEST_DEFINITIONS_SUFFIX))
                {
                    /*
                     * Strip name from test definitions filename
                     */
                    String name = inLine.substring(0, inLine.indexOf(TEST_DEFINITIONS_SUFFIX));

                    /*
                     * See if we've come across this name before
                     * if we have then add it to the list of tests
                     * to run otherwise just add it to the found
                     * list.
                     */
                    if (foundSelectionsList.contains(name))
                    {
                        System.out.println("Adding '" + name + "' to list of tests to run");
                        testsToRun.add(name);
                    }
                    else
                    {
                        foundTestDefsList.add(name);
                    }
                }

                if (inLine.endsWith(TEST_SELECTIONS_SUFFIX))
                {
                    /*
                     * Strip name from test selections filename
                     */
                    String name = inLine.substring(0, inLine.indexOf(TEST_SELECTIONS_SUFFIX));

                    /*
                     * See if we've come across this name before
                     * if we have then add it to the list of tests
                     * to run otherwise just add it to the found
                     * list.
                     */
                    if (foundTestDefsList.contains(name))
                    {
                        System.out.println("Adding '" + name + "' to list of tests to run");
                        testsToRun.add(name);
                    }
                    else
                    {
                        foundSelectionsList.add(name);
                    }
                }
            }
            in.close();

            System.out.println("Initiating tests:");

            for (int count=0;count<testsToRun.size();count++)
            {
                String testToRun = (String)testsToRun.get(count);
                System.out.println("\tStarting: "+testToRun);

                boolean success = InitiateTestRun.initiate( nameService, url+"/"+testToRun+TEST_DEFINITIONS_SUFFIX, url+"/"+testToRun+TEST_SELECTIONS_SUFFIX, softwareVersion, distributionList != null ? distributionList : "", (count+1) < testsToRun.size() ? true : wait, runWhenPossible);
                System.out.println("\tCompleted: "+testToRun+" "+(success ? "[success]":"[failure]"));
            }
        }
        catch (Exception e)
        {
            System.err.println("ERROR - " + e);
            System.exit(1);
        }
    }
}
