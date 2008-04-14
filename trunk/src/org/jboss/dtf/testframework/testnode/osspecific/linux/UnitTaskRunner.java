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
// $Id: UnitTaskRunner.java 170 2008-03-25 18:59:26Z jhalliday $

package org.jboss.dtf.testframework.testnode.osspecific.linux;

import org.jboss.dtf.testframework.testnode.*;
import org.jboss.dtf.testframework.utils.*;
import org.jboss.dtf.testframework.productrepository.NodeConfiguration;

import java.io.*;
import java.util.ArrayList;
import java.util.Hashtable;

public class UnitTaskRunner extends TaskRunner
{
	private final static String RESULTS_DIRECTORY_PROPERTY = "org.jboss.dtf.testframework.testnode.UnitTestTaskRunner.resultsdirectory";

	private Process 			_proc = null;
	private String				_javaHome = null;

	public final boolean terminate()
	{
		if (_proc!=null)
		{
			_proc.destroy();
	        return(true);
		}

		return(false);
	}

	private void setupRunner()
	{
		Hashtable params = getRunnerParameters();

		String param = (String)params.get(JAVA_HOME_DIRECTORY_PARAMETER);
		_javaHome = (param != null) ? param : System.getProperty("java.home");
	}

	public final void runTask() throws Exception
	{
		try
		{
			Runtime rt = Runtime.getRuntime();
			int exitValue = -1;

			setupRunner();

			/*
			 * Create array list containing all of the parameters to be passed
			 * to Runtime.exec().  Once this array list is complete create
			 * a string array from its contents.
			 */
			ArrayList execParameters = new ArrayList();

			File javaBinDir = new File(_javaHome, "bin");
			File javaExe = new File(javaBinDir, "java");

			NodeConfiguration nodeConfig = _productConfig.getNodeConfiguration(TestNode.getOSId());
			StringPreprocessor pre = new StringPreprocessor();
			pre.addReplacements(nodeConfig.getPreprocessedSets());

            /**
             * As this is the linux specific java task runner use the linux process handler.
             * This small app executes a process but ensures a TERM signal is translated into
             * a KILL signal to ensure we can stop tasks if necessary.
             */
			System.out.println("Executing without Linuxprochandler") ;
            // execParameters.add("linuxprochandler");

			execParameters.add(getJavaExe());
			execParameters.add("-classpath");
			execParameters.add(getClasspathString());

			Hashtable properties = _nodeConfig.getPreprocessedProperties();
			Object[] propertyNames = properties.keySet().toArray();

			for (int count=0;count<propertyNames.length;count++)
			{
				String propertyName = (String)propertyNames[count];
				String propertyValue = (String)properties.get(propertyName);

			    execParameters.add("-D"+propertyName+"="+propertyValue);
			}

			/** Add results directory property property **/
			execParameters.add("-D" + RESULTS_DIRECTORY_PROPERTY + "=" + new File("results/"+_taskId.getTestId()+"/").getAbsolutePath());

			if (_jvmParameters != null)
				for (int count=0;count<_jvmParameters.length;count++)
					execParameters.add(pre.preprocessParameters(_jvmParameters[count]));

			execParameters.add("org.jboss.dtf.testframework.unittest.Harness");
			execParameters.add(""+_runId.getUID());
			execParameters.add(_taskPermutationCode);
			execParameters.add(_testPermutationCode);
			execParameters.add(_loggingResource);
			execParameters.add(_serviceUtils.getNameServiceURI());
			execParameters.add(_className);
			execParameters.add(_testId);
            execParameters.add(_taskName);

			for (int count=0;count<_parameters.length;count++)
			{
				System.out.println("Param["+count+"] = "+_parameters[count]);
				execParameters.add(_parameters[count]);
			}

			/*
			 * Run the Unit Test Harness
			 */
			String[] stringExecParameters = new String[execParameters.size()];
			System.arraycopy(execParameters.toArray(),0,stringExecParameters,0,execParameters.size());

			// Print out the executed command line
			for (int count=0;count<stringExecParameters.length;count++)
			{
				System.out.print(stringExecParameters[count]+" ");
			}
			System.out.println();

			/*
			 * Execute the unit test harness
			 */
			_proc = rt.exec(stringExecParameters);

            indicateTaskIsRunning();

			InputStreamFileWriter errFile = new InputStreamFileWriter(_proc.getErrorStream(), new File("results/"+_taskId.getTestId()+"/"), TaskResultsFilename.generateTaskErrorFilename(_taskId.getTaskId()));

	        System.out.println("Started Task ("+_testId+"/"+_className+")");

			/*
			 * If the test should wait for return
			 */
			if (_testType == TestNodeInterface.WAIT_READY)
			{
				InputStream in = _proc.getInputStream();

				// Create the directories that the output will be placed in
				File outDir = new File("results/"+_taskId.getTestId());
				outDir.mkdirs();

				FileOutputStream out = new FileOutputStream("results/"+_taskId.getTestId()+"/"+TaskResultsFilename.generateTaskOutputFilename(_taskId.getTaskId()));
				byte[] buffer = new byte[32768];
				int bytesRead = 0;
				String parseString;

				while ( bytesRead != -1 )
				{
                    try
                    {
					    bytesRead = in.read(buffer,0,buffer.length);
                    }
                    catch (java.io.IOException e)
                    {
                        // Ignore this too - although report it
                        System.err.println("If this exception is a bad file descriptor it can be ignored safely");
                        e.printStackTrace(System.err);

                        bytesRead = -1;
                    }
                    catch (NullPointerException e)
                    {
                        // Ignore
                        bytesRead = -1;
                    }

					if (bytesRead>0)
					{
						out.write(buffer,0,bytesRead);
						parseString = new String(buffer,0,bytesRead);

						/*
						 * If Ready has been signalled then
						 * inform listener
						 */
						if (parseString.indexOf("Ready")!=-1)
						{
                            System.out.println("#### Ready signalled at task runner");
							readySignalled();
						}
					}
				}

				_finished = true;

				in.close();
				out.close();
			}
			else
			{
				try
				{
					InputStream in = _proc.getInputStream();

					// Create the directories that the output will be placed in
					File outDir = new File("results/"+_taskId.getTestId());
					outDir.mkdirs();
					FileOutputStream out = new FileOutputStream("results/"+_taskId.getTestId()+"/"+TaskResultsFilename.generateTaskOutputFilename(_taskId.getTaskId()));
					boolean taskFinished = false;
					byte[] buffer = new byte[32768];
					int bytesRead = 0;
					String parseString;

					while ( ( bytesRead != -1 ) && ( (!taskFinished) || (taskFinished&&(in.available()>0)) ) )
					{
//						while (in.available()>0)
//						{
                            try
                            {
							    bytesRead = in.read(buffer,0,buffer.length);
                            }
                            catch (java.io.IOException e)
                            {
                                // Ignore this too - although report it
                                System.err.println("If this exception is a bad file descriptor it can be ignored safely");
                                e.printStackTrace(System.err);

                                bytesRead = -1;
                            }
                            catch (NullPointerException e)
                            {
                                // Ignore
                                bytesRead = -1;
                            }

							if (bytesRead > 0)
							{
								parseString = new String(buffer,0,bytesRead);
								out.write(buffer,0,bytesRead);
								if (_resultListener!=null)
									_resultListener.taskReturnedData(_taskId, parseString);
							}
//						}

						try
						{
							if (_proc!=null)
								exitValue = _proc.exitValue();
							taskFinished = true;
						}
						catch (IllegalThreadStateException e)
						{
						}
					}

					in.close();
					out.close();
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}

			try
			{
				exitValue = _proc.exitValue();
			}
			catch (IllegalThreadStateException e)
			{
                System.out.println("Task still running - can't flag finished doing WaitFor");
                // If this exception is thrown the task has completely finished therefore wait for it
                System.out.println("wait loop entered "+_proc);
                boolean completed = false;

                while ( !completed )
                {
                    try
                    {
                        exitValue = _proc.exitValue();
                        completed = true;
                    }
                    catch (Exception ex)
                    {
                        System.out.println("Task still hasn't completed - staying in wait loop "+_proc);
                        Thread.sleep(1000);
                    }
                }
                System.out.println("wait loop COMPLETED "+_proc);
                System.out.println("Task has completed - waitFor complete");
			}

			_proc = null;

			System.out.println("Task Finished ("+_testId+"/"+_className+")");

			_finished = true;
			readySignalled();
			errFile.close();

			boolean failure = (exitValue != org.jboss.dtf.testframework.unittest.Harness.RETURN_VALUE_SUCCESS);
			System.out.println("Task Failure Status: failure = " + failure) ;

			/*
			 * If there is a listener registered then
			 * inform the listener that the task has finished.
			 */
			if (_listener!=null)
			{
			    try
			    {
				    _listener.taskFinished( _taskId, _associatedTestNode, _testPermutationCode, failure );
				}
				catch (NoSuchTaskId e)
           		{
			        System.out.println("Informed TestNode task has finished - task unknown");
		        }
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw e;
		}
	}

	/**
	 * This method is called by TestNode to wait for a running process. The problem is that if the
	 * executing task gets locked up, this cases the TestNode to block forever.
	 */
    public final void waitFor() throws InterruptedException
    {
        if (_proc!=null)
        {
            System.out.println("_proc.waitFor() "+_proc);

            System.out.println(_taskName+" wait loop entered "+_proc);
            boolean completed = false;

            while ( !completed )
            {
                try
                {
                    _proc.exitValue();
                    completed = true;
                }
                catch (NullPointerException e)
                {
                    completed = ( _proc == null );
                }
                catch (IllegalThreadStateException ex)
                {
                    System.out.println(_taskName+" Task still hasn't completed - staying in wait loop "+_proc);
                    Thread.sleep(1000);
                }
            }
            System.out.println(_taskName+" wait loop COMPLETED "+_proc);
        }
    }
}
