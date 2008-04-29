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
package org.jboss.dtf.tools.testselectionsgenerator;

import org.jboss.dtf.testframework.coordinator.TestDefinitionRepository;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class TestSelectionsGenerator
{
    public TestSelectionsGenerator(String filename, String osConfigFilename) throws Exception
    {
        TestDefinitionRepository testRepository = null;

        File f = new File(filename);

        if ( f.exists() )
        {
            testRepository = new TestDefinitionRepository(f.toURL());
        }
        else
        {
            testRepository = new TestDefinitionRepository(new URL(filename));
        }

        /**
         * Retrieve the operating systems and products to be used within this selections file
         */
        ArrayList products = new ArrayList();
        ArrayList os = new ArrayList();
        getOSProductNames(osConfigFilename, products, os);

        PrintStream out = new PrintStream(new FileOutputStream("selections.xml"));

        out.println( "<test_selections>" );

        HashMap testDefsMap = testRepository.getTestDefinitionsMap();
        Set s = testDefsMap.keySet();
        String[] tests = new String[s.size()];
        s.toArray(tests);

        HashMap newMap = new HashMap();
        HashMap current = newMap;

        for (int groupCount=0;groupCount<tests.length;groupCount++)
        {
            String[] splitGroups = splitIntoSubGroups( tests[groupCount] );

            current = newMap;

            for (int count=0;count<splitGroups.length - 1;count++)
            {
                HashMap next = (HashMap)current.get( splitGroups[count] );

                if ( next == null )
                {
                    current.put( splitGroups[count], next = new HashMap() );
                }

                current = next;
            }

            current.put( splitGroups[splitGroups.length -1], testDefsMap.get( tests[groupCount] ) );
        }

        /**
         * For each supported OS
         */
        for (int osCount=0;osCount<os.size();osCount++)
        {
            out.println( "\t<os id=\""+(String)os.get(osCount)+"\">\n");

            /**
             * Add each product
             */
            for (int productCount=0;productCount<products.size();productCount++)
            {
                out.println( "\t\t<product id=\""+(String)products.get(productCount)+"\">\n");

                generateGroups( newMap, out, 3 );

                out.println("\t\t</product>\n");
            }

            out.println( "\t</os>\n" );

        }


        out.close();
    }

    private void generateGroups( HashMap newMap, PrintStream out, int indent )
    {
        Set s = newMap.keySet();
        String[] groups = new String[s.size()];
        s.toArray(groups);

        for (int count=0;count<groups.length;count++)
        {
            out.println( generateTabs(indent) + "<test_group id=\""+ groups[count] +"\">");

            Object obj = newMap.get(groups[count]);

            if ( obj instanceof HashMap )
            {
                generateGroups( (HashMap)obj, out, indent+1 );
            }

            out.println( generateTabs(indent)+ "</test_group>\n");
        }
    }

    private String generateTabs(int count)
    {
        String returnVal = "";

        for (int c=0;c<count;c++)
            returnVal += '\t';

        return returnVal;
    }

    private String[] splitIntoSubGroups(String groupName)
    {
        ArrayList subGroups = new ArrayList();

        while ( groupName.indexOf('/') != -1 )
        {
            subGroups.add( groupName.substring(0, groupName.indexOf('/') ) );

            groupName = groupName.substring( groupName.indexOf('/') + 1 );
        }

        subGroups.add(groupName);

        String[] returnArray = new String[ subGroups.size() ];
        subGroups.toArray(returnArray);

        return returnArray;
    }

    private void getOSProductNames(String filename, ArrayList productNamesList, ArrayList osNamesList) throws Exception
    {
        BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(filename)));
        String inLine;

        while ( ( (inLine = in.readLine() ) != null ) && ( !inLine.startsWith("-") ) )
        {
            osNamesList.add(inLine);
        }

        while ( (inLine = in.readLine() ) != null )
        {
            productNamesList.add(inLine);
        }

        in.close();
    }

    public static void main(String[] args)
    {
        if ( args.length != 2 )
        {
            System.out.println("Usage: TestSelectionsGenerator [url://hostname/testdefs.xml] [os/product-list filename]");
        }
        else
        {
            try
            {
                new TestSelectionsGenerator(args[0],args[1]);
            }
            catch (Exception e)
            {
                System.err.println("Unexpected exception: "+e);
                e.printStackTrace(System.err);
            }
        }
    }
}
