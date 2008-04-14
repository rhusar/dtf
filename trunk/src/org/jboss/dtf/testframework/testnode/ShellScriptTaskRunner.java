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
// Copyright (C) 2001, 2002, 2003
//
// Arjuna Technologies Ltd.
// Newcastle upon Tyne,
// Tyne and Wear,
// UK.
//

package org.jboss.dtf.testframework.testnode;

import org.jboss.dtf.testframework.testnode.*;
import org.jboss.dtf.testframework.utils.*;
import org.jboss.dtf.testframework.productrepository.NodeConfiguration;

import java.io.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;

import junit.textui.TestRunner;

/**
 * This TaskRunner is intended to execute a shell script.
 *
 * From a java program, we execute a shell script using by calling
 * Runtime.exec(cmd, env, work)
 * where
 * String[] cmd - a shell command line
 * String[] env - a list of environment variables
 * File - a working directory
 *
 * It appears that we need only specify the following host specific elements
 * SHELL = shell command (e.g. /bin/bash)
 * PATH = path of binary commands used in the script
 * Both of these can be defined in the product as sets, and should be.
 *
 * As this is not a java program, and so the following elements are not processed:
 * * JAVA_HOME really is not required for correct execution
 * * product properties are ignored
 * * any task or test JVM parameters.
 *
 * We assume that the working directory will be determined by the path component
 * of the shell command. If there is no such path specified, we throw an exception.
 *
 * @author nrla
 *
 */
public class ShellScriptTaskRunner extends TaskRunner
{
	private final static String WAIT_FOR_PARAMETER = "wait_for_text";
	private final static String DEFAULT_WAIT_FOR_PARAMETER = "Ready";

	private final static String JAVA_HOME_DIRECTORY_PARAMETER = "java_home";

    private final static String PASS_RESULT_TEXT = "Passed";
    private final static String FAIL_RESULT_TEXT = "Failed";

    private final static String SHELL_CMD_VARIABLE_NAME = "SHELL" ;
    private final static String PATH_VARIABLE_NAME = "PATH" ;

	private Process 			_proc = null;
	private String              _waitText = null;
	private String				_javaHome;

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
    	// get the parameters used to initialse this TaskRunner
        Hashtable params = getRunnerParameters();

        // which parameters must this runner define (for correct operation)?
		String param = (String)params.get( WAIT_FOR_PARAMETER );
		_waitText = (param != null)?param:DEFAULT_WAIT_FOR_PARAMETER;
        param = (String)params.get(JAVA_HOME_DIRECTORY_PARAMETER);
		_javaHome = (param != null) ? param : System.getProperty("java.home");
    }



	public final void runTask() throws Exception
	{
		try
		{
			Runtime rt = Runtime.getRuntime();

            /**
             * Setup this runner
             */
            setupRunner();

            /**
             * Log task start with logging service
             */
            getLoggingService().initiateTask(_testId, _runId, getTaskName(), _taskPermutationCode, _testPermutationCode);

			ArrayList cmdParameters = new ArrayList();
			ArrayList envParameters = new ArrayList();

			Hashtable preprocessedSets = _nodeConfig.getPreprocessedSets() ;
			StringPreprocessor pre = new StringPreprocessor();
			pre.addReplacements(preprocessedSets);

			// set up the shell command and its arguments
			String shellCmd = (String) preprocessedSets.get(SHELL_CMD_VARIABLE_NAME) ;
			if (shellCmd == null) {
				throw new Exception("SHELL variable must be defined in Product") ;
			}
			cmdParameters.add(shellCmd);

			// these parameters have already been preprocessed (in particular by envsets)
			System.out.println("Adding parameters") ;
			for (int count=0;count<_parameters.length;count++) {
				cmdParameters.add(_parameters[count]);
				System.out.println("Adding parameter: " + _parameters[count]) ;
			}

			// set up the environment parameters
			String path = (String) preprocessedSets.get(PATH_VARIABLE_NAME) ;
			if (shellCmd == null) {
				throw new Exception("PATH variable must be defined in Product") ;
			}
			envParameters.add(path) ;

			// set up the working directory
			String shellCommand = _parameters[0] ;
			File cmdFile = new File(shellCommand) ;
			File workingDirectoryFile = cmdFile.getParentFile() ;
			if (workingDirectoryFile == null) {
				throw new Exception("shell command must have a relative path") ;
			}

			// convert all parameter lists to string arrays
			String[] stringCmdParameters = new String[cmdParameters.size()];
			System.arraycopy(cmdParameters.toArray(),0,stringCmdParameters,0,cmdParameters.size());

			String[] stringEnvParameters = new String[envParameters.size()];
			System.arraycopy(envParameters.toArray(),0,stringEnvParameters,0,envParameters.size());

			for (int count=0;count<stringCmdParameters.length;count++)
			{
				System.out.println("cmd parameter ("+count+") = '"+stringCmdParameters[count]+"'");
			}

			for (int count=0;count<stringEnvParameters.length;count++)
			{
				System.out.println("env parameter ("+count+") = '"+stringEnvParameters[count]+"'");
			}

			System.out.println("working directory parameter (" + workingDirectoryFile.getAbsolutePath() + ")") ;

			// fire the command
			_proc = rt.exec(stringCmdParameters, stringEnvParameters, workingDirectoryFile);

            indicateTaskIsRunning();

			new InputStreamFileWriter(_proc.getErrorStream(), new File("results/"+_taskId.getTestId()+"/"), TaskResultsFilename.generateTaskErrorFilename(_taskId.getTaskId()));

			/*
			 * If the test should wait for return
			 */
			if (_testType == TestNodeInterface.WAIT_READY)
			{
				BufferedInputStream in = new BufferedInputStream(_proc.getInputStream());

				// Create the directories that the output will be placed in
				File outDir = new File("results/"+_taskId.getTestId());
				outDir.mkdirs();

				FileOutputStream out = new FileOutputStream("results/"+_taskId.getTestId()+"/"+TaskResultsFilename.generateTaskOutputFilename(_taskId.getTaskId()));
				byte[] buffer = new byte[32768];
				int bytesRead = 0;
				String parseString;

				while ( bytesRead != -1 )
				{
					bytesRead = in.read(buffer,0,buffer.length);
					if (bytesRead>0)
					{
						out.write(buffer,0,bytesRead);
						parseString = new String(buffer,0,bytesRead);

						/*
						 * If Ready has been signalled then
						 * inform listener
						 */
						if (parseString.indexOf(_waitText)!=-1)
						{
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
					BufferedInputStream in = new BufferedInputStream(_proc.getInputStream());

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
                        bytesRead = in.read(buffer,0,buffer.length);

                        if (bytesRead>0)
                        {
                            parseString = new String(buffer,0,bytesRead);
                            out.write(buffer,0,bytesRead);

                            if (_resultListener!=null)
                                _resultListener.taskReturnedData(_taskId, parseString);
                        }

						try
						{
							if (_proc!=null)
								_proc.exitValue();
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
				_proc.exitValue();
			}
			catch (IllegalThreadStateException e)
			{
				System.out.println("Task still running - can't flag finished doing WaitFor");
				try
				{
					// If this exception is thrown the task has completely finished therefore wait for it
					_proc.waitFor();
				}
				catch (Exception ex)
				{
				}
			}

			int exitValue = _proc.exitValue();

			_finished = true;
			readySignalled();

			switch (exitValue)
			{
				case TestRunner.SUCCESS_EXIT :
					/**
					 * Log task result with logging service
					 */
					getLoggingService().logResult(PASS_RESULT_TEXT, getTaskName(), _testId, _runId, _taskPermutationCode, _testPermutationCode);
					break;
				case TestRunner.EXCEPTION_EXIT :
				case TestRunner.FAILURE_EXIT :
				default :
					/**
					 * Log task result with logging service
					 */
					getLoggingService().logResult(FAIL_RESULT_TEXT, getTaskName(), _testId, _runId, _taskPermutationCode, _testPermutationCode);
					break;
			}

			/*
			 * If there is a listener registered then
			 * inform the listener that the task has finished.
			 */
			if (_listener!=null)
			{
				_listener.taskFinished( _taskId, _associatedTestNode, _testPermutationCode, true );
			}

			_proc = null;
		}
		catch (NoSuchTaskId e)
		{
			System.out.println("Informed TestNode task has finished - task unknown");
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw e;
		}
	}

	public final void waitFor() throws InterruptedException
	{
		if (_proc!=null)
		{
			_proc.waitFor();
		}
	}
}
