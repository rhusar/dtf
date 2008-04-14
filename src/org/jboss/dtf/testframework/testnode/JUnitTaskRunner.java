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
// $Id: JUnitTaskRunner.java 170 2008-03-25 18:59:26Z jhalliday $

package org.jboss.dtf.testframework.testnode;

import org.jboss.dtf.testframework.testnode.*;
import org.jboss.dtf.testframework.utils.*;
import org.jboss.dtf.testframework.productrepository.NodeConfiguration;

import java.io.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;

import junit.textui.TestRunner;

public class JUnitTaskRunner extends TaskRunner
{
	private final static String WAIT_FOR_PARAMETER = "wait_for_text";
	private final static String DEFAULT_WAIT_FOR_PARAMETER = "Ready";

	private final static String JAVA_HOME_DIRECTORY_PARAMETER = "java_home";
	private final static String JUNIT_TEXT_TASK_RUNNER = "junit.textui.TestRunner";

    private final static String PASS_RESULT_TEXT = "Passed";
    private final static String FAIL_RESULT_TEXT = "Failed";

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
        Hashtable params = getRunnerParameters();

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

			ArrayList execParameters = new ArrayList();

			File javaBinDir = new File(_javaHome, "bin");
			File javaExe = new File(javaBinDir, "java");

			StringPreprocessor pre = new StringPreprocessor();
			pre.addReplacements(_nodeConfig.getPreprocessedSets());

			execParameters.add(javaExe.getAbsolutePath());
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

			if (_jvmParameters != null)
				for (int count=0;count<_jvmParameters.length;count++)
					execParameters.add(pre.preprocessParameters(_jvmParameters[count]));

			execParameters.add(JUNIT_TEXT_TASK_RUNNER);

			execParameters.add(_className);

			String[] stringExecParameters = new String[execParameters.size()];
			System.arraycopy(execParameters.toArray(),0,stringExecParameters,0,execParameters.size());

			for (int count=0;count<stringExecParameters.length;count++)
			{
				System.out.println("Parameter ("+count+") = '"+stringExecParameters[count]+"'");
			}


			_proc = rt.exec(stringExecParameters);

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
