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
package org.jboss.dtf.tools.qa2dtf;

import java.util.*;
import java.io.*;

public class QA2DTF
{
    public final static String   HEADER = "<test_set>\n\t<default_timeout value=\"480\"/>\n\t<description>Test Definitions generated using QA2DTF</description>\n\n";
    public final static String   FOOTER = "</test_set>\n";

    private final static String   CONFIG_FILE_SUFFIX = ".conf";

    private final static String   DIR_PARAMETER = "-dir";
    private final static String   OUTPUT_PARAMETER = "-outputprefix";
    private final static String   OUTPUT_DIR_PARAMETER = "-outputdir";
    private final static String   CONFIG_PARAMETER = "-config";
	private final static String   LOCATION_PARAMETER = "-location";
	private final static String	  TASK_RUNNER_PARAMETER = "-taskrunner";
    private final static String   TOPLEVEL_PARAMETER = "-toplevelfilename";
    private final static String   DEFAULT_TASK_RUNNER = "JavaTaskRunner";
	private final static String   DEFAULT_DTF_TASK_RUNNER = "UnitTestTaskRunner";

    private final static int      FIRST_PHASE = 0;

    private static String       _filenamePrefix = "";
    private static String       _topLevelFilename = null;
    private static PrintStream  _topLevelOut = null;
    private static String       _outputDir = ".";
	private static Hashtable	_location = new Hashtable();
	private static Hashtable	_taskRunner = new Hashtable();

	public static void setLocations(Hashtable locations)
	{
		_location = locations;
	}

	public static void setTaskRunners(Hashtable taskRunner)
	{
		_taskRunner = taskRunner;
	}

	public static boolean setProperties(String filename)
	{
		try
		{
			Properties p = System.getProperties();
			p.load(new FileInputStream(filename));
			System.setProperties(p);
		}
		catch (Exception e)
		{
			return false;
		}

		return true;
	}

    public static void setTopLevelFilename(String filename)
    {
        _topLevelFilename = filename;
    }

    public static void setPrefix(String prefix)
    {
        _filenamePrefix = prefix;
    }

    public static void setOutputDir(String dir)
    {
        _outputDir = dir;
    }

	public static String getTaskRunner(CommandLine javaCmd)
	{
		String rt = (String)_taskRunner.get( javaCmd.getTaskName() );

		return rt == null ? (javaCmd.isDTFTask() ? DEFAULT_DTF_TASK_RUNNER : DEFAULT_TASK_RUNNER) : rt;
	}

    public TestDefinition parse(HashSet definedTasks, String group, Properties config, PrintStream out) throws Exception
    {
        TestDefinition test = new TestDefinition( config );

        /**
         * Generate task declarations
         *
         *  For setup tasks
         */
        CommandLine[] setups = test.getSetups();
        for (int count=0;count<setups.length;count++)
        {
            String javaCmd = setups[count].getTaskName();

            if ( !definedTasks.contains( javaCmd ) )
            {
                out.println("\t\t<task id=\""+javaCmd+"\" classname=\""+javaCmd+"\" runner=\""+getTaskRunner(setups[count])+"\" type=\"expect_pass_fail\""+(setups[count].hasParameters() ? "" : "/")+"/>\n");
            }

            definedTasks.add( javaCmd );
        }

        /**
         * Generate task declarations
         *
         *  For server tasks
         */
        CommandLine[] servers = test.getServers();
        for (int count=0;count<servers.length;count++)
        {
            String javaCmd = servers[count].getTaskName();

            if ( !definedTasks.contains( javaCmd ) )
            {
                out.println("\t\t<task id=\""+javaCmd+"\" classname=\""+javaCmd+"\" runner=\""+getTaskRunner(servers[count])+"\" type=\"expect_ready\"/>\n");
            }

            definedTasks.add( javaCmd );
        }

        /**
         * Generate task declarations
         *
         *  For client tasks
         */
        CommandLine[] clients = test.getClients();
        for (int count=0;count<clients.length;count++)
        {
            String javaCmd = clients[count].getTaskName();

            if ( !definedTasks.contains( javaCmd ) )
            {
                out.println("\t\t<task id=\""+javaCmd+"\" classname=\""+javaCmd+"\" runner=\""+getTaskRunner(clients[count])+"\" type=\"expect_pass_fail\"/>\n");
            }

            definedTasks.add( javaCmd );
        }

        /**
         * Generate task declarations
         *
         *  For outcome tasks
         */
        CommandLine[] outcomes = test.getOutcomes();
        for (int count=0;count<outcomes.length;count++)
        {
            String javaCmd = outcomes[count].getTaskName();

            if ( !definedTasks.contains( javaCmd ) )
            {
                out.println("\t\t<task id=\""+javaCmd+"\" classname=\""+javaCmd+"\" runner=\""+getTaskRunner(outcomes[count])+"\" type=\"expect_pass_fail\"/>\n");
            }

            definedTasks.add( javaCmd );
        }

        /**
         * Generate task declarations
         *
         *  For cleanup tasks
         */
        CommandLine[] cleanups = test.getCleanups();
        for (int count=0;count<cleanups.length;count++)
        {
            String javaCmd = cleanups[count].getTaskName();

            if ( !definedTasks.contains( javaCmd ) )
            {
                out.println("\t\t<task id=\""+javaCmd+"\" classname=\""+javaCmd+"\" runner=\""+getTaskRunner(cleanups[count])+"\" type=\"expect_pass_fail\"/>\n");
            }

            definedTasks.add( javaCmd );
        }

        return test;
    }

    public static void generateTestDeclaration(TestDefinition test, PrintStream out)
    {
        CommandLine[] setups = test.getSetups();
        CommandLine[] servers = test.getServers();
        CommandLine[] clients = test.getClients();
        CommandLine[] outcomes = test.getOutcomes();
        CommandLine[] cleanups = test.getCleanups();

        /** Get the largest number of names required **/
        int numberOfNamesReq = test.getNumberOfServerNames();

        out.println("\t<test_declaration id=\""+test.getName()+"\" descriptive_name=\"Converted by QA-to-DTF\" author=\"QA2DTF\">");
        out.println("\t\t<description>This test was generated by QA-to-DTF for test '"+test.getName()+"'</description>");
        out.print("\t\t<configuration ");

        if ( numberOfNamesReq > 0 )
        {
            out.print("names_required=\""+numberOfNamesReq+"\"");
        }

        out.println("/>");
        out.println("\t\t<action_list>");

        /**
         * Generate action list elements for starting setup tasks
         */
        for (int count=0;count<setups.length;count++)
        {
            out.println("\t\t\t<perform_task id=\""+ setups[count].getTaskName() +"\" "+setups[count].getLocationText()+">");

            if ( setups[count].hasParameters() )
            {
                String[] parameters = setups[count].getParameters();
                for (int paramCount=0;paramCount<parameters.length;paramCount++)
                {
                    out.println("\t\t\t\t<param>"+parameters[paramCount]+"</param>");
                }
            }

			if ( setups[count].hasJVMParameters() )
			{
				String[] jvmParameters = setups[count].getJVMParameters();
				for (int paramCount=0;paramCount<jvmParameters.length;paramCount++)
				{
					out.println("\t\t\t\t<jvm_param>"+jvmParameters[paramCount]+"</jvm_param>");
				}
			}

            out.println("\t\t\t</perform_task>");

        }

        /**
         * Generate action list elements for starting servers and clients in phases
         */
        int currentPhase = 0;
        boolean hasMorePhases = true;

        while ( hasMorePhases )
        {
            hasMorePhases = false;

            /**
             * Check for servers that start in this phase
             */
            for (int count=0;count<servers.length;count++)
            {
                if ( ( servers[count].getStartPhase() == currentPhase ) ||
                     ( ( servers[count].getStartPhase() == CommandLine.NO_PHASE_DEFINED ) && ( currentPhase == FIRST_PHASE ) ) )
                {
                    out.println("\t\t\t<start_task id=\""+ servers[count].getTaskName() +"\" runtime_id=\"server"+count+"\" "+servers[count].getLocationText()+">");

                    if ( servers[count].hasParameters() )
                    {
                        String[] parameters = servers[count].getParameters();
                        for (int paramCount=0;paramCount<parameters.length;paramCount++)
                        {
                            out.println("\t\t\t\t<param>"+parameters[paramCount]+"</param>");
                        }
                    }

					if ( servers[count].hasJVMParameters() )
					{
						String[] jvmParameters = servers[count].getJVMParameters();
						for (int paramCount=0;paramCount<jvmParameters.length;paramCount++)
						{
							out.println("\t\t\t\t<jvm_param>"+jvmParameters[paramCount]+"</jvm_param>");
						}
					}

                    out.println("\t\t\t</start_task>");
                    hasMorePhases = true;
                }
            }

            /**
             * Check for clients that start in this phase
             */
            for (int count=0;count<clients.length;count++)
            {
                if ( ( clients[count].getStartPhase() == currentPhase ) ||
                     ( ( clients[count].getStartPhase() == CommandLine.NO_PHASE_DEFINED ) && ( currentPhase == FIRST_PHASE ) ) )
                {
                    out.println("\t\t\t<start_task id=\""+ clients[count].getTaskName() +"\" runtime_id=\"client"+count+"\" "+clients[count].getLocationText()+">");

                    if ( clients[count].hasParameters() )
                    {
                        String[] parameters = clients[count].getParameters();
                        for (int paramCount=0;paramCount<parameters.length;paramCount++)
                        {
                            out.println("\t\t\t\t<param>"+parameters[paramCount]+"</param>");
                        }
                    }

					if ( clients[count].hasJVMParameters() )
					{
						String[] jvmParameters = clients[count].getJVMParameters();
						for (int paramCount=0;paramCount<jvmParameters.length;paramCount++)
						{
							out.println("\t\t\t\t<jvm_param>"+jvmParameters[paramCount]+"</jvm_param>");
						}
					}

                    out.println("\t\t\t</start_task>");
                    hasMorePhases = true;
                }
            }

            /**
             * Check for clients that wait in this phase
             */
            for (int count=0;count<clients.length;count++)
            {
                if ( ( clients[count].getWaitPhase() == currentPhase ) ||
                     ( ( clients[count].getWaitPhase() == CommandLine.NO_PHASE_DEFINED ) && ( currentPhase == FIRST_PHASE ) ) )
                {
                    out.println("\t\t\t<wait_for_task runtime_id=\"client"+count+"\"/>");
                    hasMorePhases = true;
                }
            }

            /**
             * Check for servers that terminate in this phase
             */
            for (int count=(servers.length-1);count>=0;count--)
            {
                if ( ( servers[count].getTerminatePhase() == currentPhase ) && ( servers[count].getTerminatePhase() != CommandLine.NO_PHASE_DEFINED ) )
                {
                    out.println("\t\t\t<terminate_task runtime_id=\"server"+count+"\"/>");
                    hasMorePhases = true;
                }
            }

            currentPhase++;
        }


		/**
		 * Perform the outcome tasks
		 */
		for (int count=0;count<outcomes.length;count++)
		{
			out.println("\t\t\t<perform_task id=\""+ outcomes[count].getTaskName() +"\" "+outcomes[count].getLocationText()+">");

			if ( outcomes[count].hasParameters() )
			{
				String[] parameters = outcomes[count].getParameters();
				for (int paramCount=0;paramCount<parameters.length;paramCount++)
				{
					out.println("\t\t\t\t<param>"+parameters[paramCount]+"</param>");
				}
			}

			if ( outcomes[count].hasJVMParameters() )
			{
				String[] jvmParameters = outcomes[count].getJVMParameters();
				for (int paramCount=0;paramCount<jvmParameters.length;paramCount++)
				{
					out.println("\t\t\t\t<jvm_param>"+jvmParameters[paramCount]+"</jvm_param>");
				}
			}

			out.println("\t\t\t</perform_task>");

		}



        /**
         * Check for servers that terminate at the end
         */
        for (int count=(servers.length-1);count>=0;count--)
        {
            if ( servers[count].getTerminatePhase() == CommandLine.NO_PHASE_DEFINED )
            {
                out.println("\t\t\t<terminate_task runtime_id=\"server"+count+"\"/>");
            }
        }

		/**
		 * Perform the cleanup tasks
		 */
		for (int count=0;count<cleanups.length;count++)
		{
			out.println("\t\t\t<perform_task id=\""+ cleanups[count].getTaskName() +"\" "+cleanups[count].getLocationText()+">");

			if ( cleanups[count].hasParameters() )
			{
				String[] parameters = cleanups[count].getParameters();
				for (int paramCount=0;paramCount<parameters.length;paramCount++)
				{
					out.println("\t\t\t\t<param>"+parameters[paramCount]+"</param>");
				}
			}

			if ( cleanups[count].hasJVMParameters() )
			{
				String[] jvmParameters = cleanups[count].getJVMParameters();
				for (int paramCount=0;paramCount<jvmParameters.length;paramCount++)
				{
					out.println("\t\t\t\t<jvm_param>"+jvmParameters[paramCount]+"</jvm_param>");
				}
			}

			out.println("\t\t\t</perform_task>");

		}

        out.println("\t\t</action_list>");
        out.println("\t</test_declaration>\n");
    }

    protected static ArrayList parseFiles(QA2DTF parser, HashSet definedTasks, String dir, String group, PrintStream out)
    {
        ArrayList tests = new ArrayList();
        File[] fileList = new File( dir ).listFiles();

		if ( ( group == null ) || ( group.length() == 0 ) )
		{
			group = new File(dir).getName();
		}
		else
		{
			group = group + File.separatorChar + new File(dir).getName();
		}

        for (int fileCount=0;fileCount<fileList.length;fileCount++)
        {
            if ( fileList[fileCount].isDirectory() )
            {
                if ( containsConfig(fileList[fileCount]) )
				{
					try
					{
						String filename = _filenamePrefix + fileList[fileCount].getName() +"-qa-testdefs.xml";

						File outDir = new File(_outputDir + File.separatorChar + group );
						outDir.mkdirs();

						File outFile = new File( outDir, filename.toLowerCase() );

						System.out.println("Generating '"+outFile+"' from '"+dir+"' ["+group+"]");

						out = new PrintStream( new FileOutputStream( outFile ) );

						out.println(HEADER);

						if ( _topLevelFilename != null )
						{
							addImport(filename);
						}
					}
					catch (Exception e)
					{
						System.err.println("ERROR - Failed to create new file: "+e);
						System.exit(1);
					}

					out.println("\t<test_group name=\""+ fileList[fileCount].getName() +"\">");
					definedTasks = new HashSet();
					out.println("\t<task_declaration>");
					ArrayList parsedTests = parseFiles( parser, definedTasks, fileList[fileCount].getAbsolutePath(), group, out );
					out.println("\t</task_declaration>");

					for (int count=0;count<parsedTests.size();count++)
					{
						TestDefinition test = (TestDefinition)parsedTests.get(count);
						generateTestDeclaration(test,out);
					}

					out.println("\t</test_group>");
					out.println("</test_set>");

					out.close();
				}
				else
				{
					parseFiles( parser, definedTasks, fileList[fileCount].getAbsolutePath(), group, out );
				}
            }
            else
            {
                if ( fileList[fileCount].getName().endsWith( CONFIG_FILE_SUFFIX ) )
                {
                    try
                    {
                        System.out.println("Loading: '"+fileList[fileCount]+"'");
                        Properties config = new Properties();
                        config.load( new FileInputStream( fileList[fileCount] ) );
                        tests.add( parser.parse(definedTasks,group,config,out) );
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace(System.err);
                        System.err.println("An error occurred while parsing '"+fileList[fileCount]+"': "+e);
                        System.exit(1);
                    }
                }
            }
        }

        return tests;
    }

	private static boolean containsConfig(File f)
	{
		String[] filenames = f.list();

		for (int count=0;count<filenames.length;count++)
		{
			if ( filenames[count].endsWith(CONFIG_FILE_SUFFIX) )
			{
				return true;
			}
		}

		return false;
	}

	private static void createTopLevelFile()
    {
        try
        {
            System.out.println("Creating top-level file '"+_topLevelFilename+"'");
            File outFile = new File( _outputDir, _topLevelFilename.toLowerCase() );
            _topLevelOut = new PrintStream(new FileOutputStream(outFile));

            _topLevelOut.println(HEADER);
        }
        catch (Exception e)
        {
            System.err.println("An error occurred while trying to create the top level file: "+e);
            System.exit(1);
        }
    }

    private static void addImport(String name)
    {
        try
        {
            _topLevelOut.println("\t<import test-set=\""+name+"\"/>");
        }
        catch (Exception e)
        {
            System.err.println("An error occurred while trying to add to the top level file: "+e);
            System.exit(1);
        }
    }

    private static void closeTopLevelFile()
    {
        try
        {
            _topLevelOut.println(FOOTER);
            _topLevelOut.close();
        }
        catch (Exception e)
        {
            System.err.println("An error occurred while trying to close the top level file: "+e);
            System.exit(1);
        }
    }

    public static void parseDir( String dir )
    {
        QA2DTF parser = new QA2DTF();

        if ( _topLevelFilename != null )
        {
            createTopLevelFile();
        }

        parseFiles( parser, new HashSet(), dir, "", null );

        if ( _topLevelFilename != null )
        {
            closeTopLevelFile();
        }
    }

	public static String getLocation(String taskName)
	{
		return (String)_location.get(taskName);
	}

    public static void main(String[] args)
    {
        if ( args.length == 0 )
        {
            System.out.println( "QA2DTF - QA Configuration File to DTF Test Definition File" );
            System.out.println( );
            System.out.println("Usage: org.jboss.dtf.tools.qa2dtf.QA2DTF {-location <classname>=<location> } { -dir <directory name> } { -outputdir <directory name> } { -outputprefix <filename> } { -config <filename> }");
            System.exit(1);
        }

        try
        {
            for (int count=0;count<args.length;count++)
            {
				if ( (args[count].equalsIgnoreCase(LOCATION_PARAMETER)) && ( count + 1 < args.length ) )
				{
					String name = args[count + 1].substring(0, args[count + 1].indexOf('='));
					String value = args[count + 1].substring(args[count + 1].indexOf('=') + 1);
                    _location.put(name, value);
				}
				else
				if ( (args[count].equalsIgnoreCase(TASK_RUNNER_PARAMETER)) && ( count + 1 < args.length ) )
				{
					String name = args[count + 1].substring(0, args[count + 1].indexOf('='));
					String value = args[count + 1].substring(args[count + 1].indexOf('=') + 1);
					_taskRunner.put(name, value);
				}
				else
                if ( (args[count].equalsIgnoreCase(CONFIG_PARAMETER)) && (count + 1 < args.length) )
                {
                    Properties p = System.getProperties();
                    p.load(new FileInputStream(args[count + 1]));
                    System.setProperties(p);
                }
                else
                if ( (args[count].equalsIgnoreCase(OUTPUT_PARAMETER)) && (count + 1 < args.length) )
                {
                    _filenamePrefix = args[count + 1];
                }
                else
                if ( (args[count].equalsIgnoreCase(TOPLEVEL_PARAMETER)) && (count + 1 < args.length) )
                {
                    _topLevelFilename = args[count + 1];
                }
                else
                if ( (args[count].equalsIgnoreCase(OUTPUT_DIR_PARAMETER)) && (count + 1 < args.length) )
                {
                    _outputDir = args[count + 1];
                }
            }

            for (int count=0;count<args.length;count++)
            {
                if ( (args[count].equalsIgnoreCase(DIR_PARAMETER)) && (count + 1 < args.length) )
                {
                    parseDir( args[count + 1] );
                }
            }

        }
        catch (IOException e)
        {
            System.err.println("An unexpected exception occurred while reading the configuration file '"+args[0]+"': "+e);
            e.printStackTrace();
        }
        catch (Exception e)
        {
            System.err.println("An error occurred while parsing the configuration file: "+e);
            e.printStackTrace();
        }
    }
}
