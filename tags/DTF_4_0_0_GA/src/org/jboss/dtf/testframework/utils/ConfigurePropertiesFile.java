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

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import java.net.URL;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Hashtable;
import java.util.Enumeration;

public class ConfigurePropertiesFile
{
    private final static String PROPERTY_FILE_NODENAME = "property-file";
    private final static String PROPERTY_FILE_NAME_ATTRIBUTE = "name";
    private final static String PROPERTY_CONFIG_NODENAME = "property";
    private final static String PROPERTY_NAME_ATTRIBUTE = "name";

    private final static String ETC_DIR = "/etc";

    public ConfigurePropertiesFile(String filename)
    {
        try
        {
            URL url = null;

            try
            {
                url = new URL( filename );
            }
            catch (Exception e)
            {
                url = new File( filename ).toURL();
            }

            /**
             * Open the configuration file
             */
            DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = db.parse( url.openStream() );

            Element rootElement = doc.getDocumentElement();
            NodeList propertyFiles = rootElement.getChildNodes();

            for (int count=0;count<propertyFiles.getLength();count++)
            {
                Node propertyFile = propertyFiles.item(count);

                if ( propertyFile.getNodeName().equals( PROPERTY_FILE_NODENAME ) )
                {
                    File[] files = findPropertyFiles( propertyFile.getAttributes().getNamedItem( PROPERTY_FILE_NAME_ATTRIBUTE ).getNodeValue() );
                    Properties propsToAdd = new Properties();

                    NodeList properties = propertyFile.getChildNodes();

                    System.out.println("Properties to configure:");
                    for (int propertyCount=0;propertyCount<properties.getLength();propertyCount++)
                    {
                        Node property = properties.item(propertyCount);

                        if ( property.getNodeName().equals( PROPERTY_CONFIG_NODENAME ) )
                        {
                            String propertyName = property.getAttributes().getNamedItem( PROPERTY_NAME_ATTRIBUTE ).getNodeValue();
                            String propertyValue = property.getFirstChild().getNodeValue();

                            System.out.println("\t"+propertyName+" = "+propertyValue);
                            propsToAdd.setProperty( propertyName, propertyValue );
                        }
                    }

                    System.out.println("Updating "+files.length+" files");
                    for (int fileCount=0;fileCount<files.length;fileCount++)
                    {
                        Properties newProps = new Properties( );
                        newProps.putAll( propsToAdd );

                        Enumeration e = newProps.keys();

                        System.out.println("Updating '"+files[fileCount].getAbsolutePath()+"'");
                        ParameterPreprocessor.addReplacement("FILENAME", files[fileCount].getAbsolutePath() );

                        while ( e.hasMoreElements() )
                        {
                            String key = (String)e.nextElement();
                            String value = ParameterPreprocessor.preprocessParameters( (String)newProps.get(key) );

                            System.out.println(key + " = "+ value);
                            newProps.put( key, value );
                        }

                        updatePropertyFile( files[fileCount], newProps );
                    }
                }
            }
        }
        catch (Exception e)
        {
            System.err.println("An unexpected exception was thrown: "+e);
            e.printStackTrace(System.err);
            System.exit(1);
        }
    }

    private boolean setReadWrite(File f)
    {
        boolean success = true;

        try
        {
            if ( !f.canWrite() )
            {
                Runtime rt = Runtime.getRuntime();

                String chmodLocation = System.getProperty("com.arjuna.mw.ts.testframework.chmod.location","");

                Process p = rt.exec(chmodLocation+"chmod u+rw "+f.getAbsolutePath());

                new InputStreamFileWriter(p.getInputStream(), "chmod.out");
                new InputStreamFileWriter(p.getErrorStream(), "chmod.err");

                p.waitFor();
            }
        }
        catch (Exception e)
        {
            success = false;
        }

        return success;
    }

    private void updatePropertyFile( File f, Properties p )  throws java.io.IOException
    {
        FileInputStream in = null;
        FileOutputStream out = null;

        System.out.println("Updating "+f.getName()+" with properties");
        if ( setReadWrite(f) )
        {
            System.out.println("Set file read/write");
            Properties props = new Properties();
            props.load( in = new FileInputStream(f) );
            props.putAll(p);
            in.close();
            props.store( out = new FileOutputStream(f), "");
            out.close();
        }
        else
        {
            throw new java.io.IOException("Failed to set read/write for file '"+f.getAbsolutePath()+"'");
        }
    }

    private void dumpProperties(Properties t)
    {
        Enumeration e = t.keys();

        while (e.hasMoreElements())
        {
            String o = (String) e.nextElement();

            System.out.println("Key '"+o+"' => '"+t.getProperty(o)+"'");
        }
    }

    private File[] findPropertyFiles( String filename )
    {
        ArrayList results = new ArrayList();
        String classpath = System.getProperty( "java.class.path" );

        System.out.println("Searching classpath for ETC dir");
        while ( classpath.indexOf(ETC_DIR) != -1 )
        {
            String dirOnwards = classpath.substring(0, classpath.indexOf(ETC_DIR));
            int positionOfDelimiter = 0;

            if ( dirOnwards.indexOf( File.pathSeparatorChar ) != -1 )
            {
                positionOfDelimiter = dirOnwards.lastIndexOf( File.pathSeparatorChar );
            }
            String dir = classpath.substring(positionOfDelimiter + 1);

            if ( dir.indexOf( File.pathSeparatorChar ) != -1 )
            {
                dir = dir.substring(0, dir.indexOf( File.pathSeparatorChar ) );
            }
            System.out.println("Searching '"+dir+"'");

            File[] fileList = new File(dir).listFiles();

            if ( fileList != null )
            {
                for (int count=0;count<fileList.length;count++)
                {
                    if ( fileList[count].getName().equals(filename) )
                        results.add(fileList[count]);
                }
            }

            classpath = classpath.substring(classpath.indexOf(dir) + dir.length());
        }

        Object[] resArray = results.toArray();
        File[] fileArray = new File[resArray.length];

        System.arraycopy(resArray, 0, fileArray, 0, fileArray.length);

        return fileArray;
    }

    public static void main(String[] args)
    {
        if ( args.length == 0 )
        {
            System.out.println("Usage: org.jboss.dtf.testframework.utils.ConfigurePropertiesFile [ url://config.xml ] {-set property=value}");
        }
        else
        {
            try
            {
                for (int count=0;count<args.length;count++)
                {
                    if ( args[count].equals("-set") )
                    {
                        String setParameter = args[count + 1];
                        String name = setParameter.substring( 0, setParameter.indexOf('=') );
                        String value = setParameter.substring( setParameter.indexOf('=') + 1 );
                        ParameterPreprocessor.addReplacement(name, value);
                    }
                }

                new ConfigurePropertiesFile(args[0]);
            }
            catch (Exception e)
            {
                System.err.println("Error in parameters");
            }
        }
    }
}
