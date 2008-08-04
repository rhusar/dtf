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
// $Id: JavaTaskRunner.java 170 2008-03-25 18:59:26Z jhalliday $

package org.jboss.dtf.testframework.testnode;

import org.jboss.dtf.testframework.testnode.*;
import org.jboss.dtf.testframework.utils.*;
import org.jboss.dtf.testframework.productrepository.NodeConfiguration;

import java.io.*;
import java.util.ArrayList;
import java.util.Hashtable;

public class JavaTaskRunner extends TaskRunner
{
	private final static String RESULTS_DIRECTORY_PROPERTY = "org.jboss.dtf.testframework.testnode.UnitTestTaskRunner.resultsdirectory";

    private final static String WAIT_FOR_PARAMETER = "wait_for_text";
    private final static String PASS_INDICATOR_PARAMETER = "pass_indicator";
    private final static String FAIL_INDICATOR_PARAMETER = "fail_indicator";
    private final static String PASS_ON_WAIT_FOR_TEXT_PARAMETER = "pass_on_wait_for";
	private final static String PERFORM_NON_ZERO_RETURN_CHECK_PARAMETER = "perform_non_zero_return_check";
	private final static String EXPECT_TEXT_ANYWHERE_IN_LINE_PARAMETER = "text_anywhere";

    private final static String DEFAULT_WAIT_FOR_PARAMETER = "Ready";
    private final static String DEFAULT_PASS_INDICATOR_PARAMETER = "Passed";
    private final static String DEFAULT_FAIL_INDICATOR_PARAMETER = "Failed";
    private final static boolean DEFAULT_PASS_ON_WAIT_FOR_TEXT_PARAMETER = false;
    private final static boolean DEFAULT_PERFORM_NON_ZERO_RETURN_CHECK_PARAMETER = false;
    private final static boolean DEFAULT_EXPECT_TEXT_ANYWHERE_IN_LINE_PARAMETER = false;

    private final static String PASS_RESULT_TEXT = "Passed";
    private final static String FAIL_RESULT_TEXT = "Failed";

	private Process 			_proc = null;
    private String              _waitText = null;
    private String              _passText = null;
    private boolean             _passOnWaitFor = false;
    private String              _failText = null;
	private boolean				_performNonZeroCheck = false;
	private String				_javaHome;
	private boolean				_expectWaitForAnywhere = false;

	public final boolean terminate()
	{
        System.out.println(_proc+" Terminate Request - The task is "+(isRunning() ? "still running" : "no longer running"));

		if ( !isRunning() )
		{
			return true;
		}

		if (_proc!=null)
		{
            System.out.println("_proc.destroy() "+_proc);
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
        param = (String)params.get( PASS_INDICATOR_PARAMETER );
        _passText = (param != null)?param:DEFAULT_PASS_INDICATOR_PARAMETER;
        param = (String)params.get( FAIL_INDICATOR_PARAMETER );
        _failText = (param != null)?param:DEFAULT_FAIL_INDICATOR_PARAMETER;
        param = (String)params.get( PASS_ON_WAIT_FOR_TEXT_PARAMETER );
        _passOnWaitFor = (param != null)?(new Boolean(param).booleanValue()) : DEFAULT_PASS_ON_WAIT_FOR_TEXT_PARAMETER;
		param = (String)params.get(PERFORM_NON_ZERO_RETURN_CHECK_PARAMETER);
		_performNonZeroCheck = (param != null)?(new Boolean(param).booleanValue()) : DEFAULT_PERFORM_NON_ZERO_RETURN_CHECK_PARAMETER;
		param = (String)params.get(JAVA_HOME_DIRECTORY_PARAMETER);
		_javaHome = (param != null) ? param : System.getProperty("java.home");
		param = (String)params.get(EXPECT_TEXT_ANYWHERE_IN_LINE_PARAMETER);
		_expectWaitForAnywhere = (param != null)?(new Boolean(param).booleanValue()) : DEFAULT_EXPECT_TEXT_ANYWHERE_IN_LINE_PARAMETER;
	}

	public final void runTask() throws Exception
	{
		try
		{
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

			ParameterPreprocessor.addReplacements(_nodeConfig.getPreprocessedSets());

			execParameters.add(getJavaExe());
			execParameters.add("-classpath");
			execParameters.add(getClasspathString());

            execParameters.add("-DJAVA_HOME="+_javaHome);

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
					execParameters.add(_jvmParameters[count]);

			/** Add results directory property property **/
			execParameters.add("-D" + RESULTS_DIRECTORY_PROPERTY + "=" + new File("results/"+_taskId.getTestId()+"/").getAbsolutePath());

            for (int count=0;count<_parameters.length;count++)
            {
                System.out.println("Parameter ("+count+") = '"+_parameters[count]+"'");
            }

			execParameters.add(_className);
			for (int count=0;count<_parameters.length;count++)
				execParameters.add(_parameters[count]);

			String[] stringExecParameters = new String[execParameters.size()];
			System.arraycopy(execParameters.toArray(),0,stringExecParameters,0,execParameters.size());

            if (DebugEnabled)
            {
                System.out.println("Execution Parameters:");

                for (int count=0;count<stringExecParameters.length;count++)
                {
                    System.out.println("["+count+"]="+stringExecParameters[count]);
                }
            }

            System.out.println("About to call exec");
            _proc = Runtime.getRuntime().exec(stringExecParameters);
            System.out.println("Completed call to exec");

            indicateTaskIsRunning();

            System.out.println("_proc.getErrorStream() "+_proc);
            new InputStreamFileWriter(_proc.getErrorStream(), new File("results/"+_taskId.getTestId()+"/"), TaskResultsFilename.generateTaskErrorFilename(_taskId.getTaskId()));

			try
			{
				/*
				 * If the test should wait for return
				 */
				if (_testType == TestNodeInterface.WAIT_READY)
				{
					System.out.println("_proc.getInputStream() "+_proc);
					BufferedReader in = new BufferedReader(new InputStreamReader(_proc.getInputStream()));

					// Create the directories that the output will be placed in
					File outDir = new File("results/"+_taskId.getTestId());
					outDir.mkdirs();

					BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("results/"+_taskId.getTestId()+"/"+TaskResultsFilename.generateTaskOutputFilename(_taskId.getTaskId()))));
					String parseString;


					while ( ( parseString = in.readLine() ) != null )
					{
						out.write(parseString);
						out.newLine();

						/*
						 * If Ready has been signalled then
						 * inform listener
						 */
						if ( ( parseString.indexOf(_waitText) != -1 ) && ( ( _expectWaitForAnywhere) || ( parseString.indexOf(_waitText) == 0) ) )
						{
							readySignalled();

							if (_passOnWaitFor)
							{
								/**
								 * Log task result with logging service
								 */
								getLoggingService().logResult(PASS_RESULT_TEXT, getTaskName(), _testId, _runId, _taskPermutationCode, _testPermutationCode);
							}
						}

						if ( ( parseString.indexOf(_passText) != -1 ) && ( ( _expectWaitForAnywhere) || ( parseString.indexOf(_passText) == 0) ) )
						{
							/**
							 * Log task result with logging service
							 */
							getLoggingService().logResult(PASS_RESULT_TEXT, getTaskName(), _testId, _runId, _taskPermutationCode, _testPermutationCode);
						}

						if ( ( parseString.indexOf(_failText) != -1 ) && ( ( _expectWaitForAnywhere) || ( parseString.indexOf(_failText) == 0) ) )
						{
							/**
							 * Log task result with logging service
							 */
							getLoggingService().logResult(FAIL_RESULT_TEXT, getTaskName(), _testId, _runId, _taskPermutationCode, _testPermutationCode);
						}
					}

					_finished = true;

					in.close();
					out.close();
				}
				else
				{
                    System.out.println("_proc.getInputStream() "+_proc);
                    BufferedReader in = new BufferedReader(new InputStreamReader(new BufferedInputStream(_proc.getInputStream())));

					// Create the directories that the output will be placed in
					File outDir = new File("results/"+_taskId.getTestId());
					outDir.mkdirs();
					BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("results/"+_taskId.getTestId()+"/"+TaskResultsFilename.generateTaskOutputFilename(_taskId.getTaskId()))));
					boolean taskFinished = false;
					String parseString;

					while ( ( parseString = in.readLine() ) != null )
					{
						out.write(parseString);
						out.newLine();

						if (_resultListener!=null)
							_resultListener.taskReturnedData(_taskId, parseString);

						if ( ( parseString.indexOf(_passText) != -1 ) && ( ( _expectWaitForAnywhere) || ( parseString.indexOf(_passText) == 0) ) )
						{
							/**
							 * Log task result with logging service
							 */
							getLoggingService().logResult(PASS_RESULT_TEXT, getTaskName(), _testId, _runId, _taskPermutationCode, _testPermutationCode);
						}

						if ( ( parseString.indexOf(_failText) != -1 ) && ( ( _expectWaitForAnywhere) || ( parseString.indexOf(_failText) == 0) ) )
						{
							/**
							 * Log task result with logging service
							 */
							getLoggingService().logResult(FAIL_RESULT_TEXT, getTaskName(), _testId, _runId, _taskPermutationCode, _testPermutationCode);
						}

					}

					try
					{
						if (_proc!=null)
                        {
                            System.out.println("_proc.exitValue() "+_proc);
                            _proc.exitValue();
                        }

						taskFinished = true;
					}
					catch (IllegalThreadStateException e)
					{
					}

					in.close();
					out.close();
				}
			}
			catch (IOException e)
			{
				System.err.println("An unexpected IO exception occurred: "+e);
				e.printStackTrace(System.err);
			}
			catch (Exception e)
			{
				System.err.println("An unexpected exception occurred: "+e);
				e.printStackTrace(System.err);
			}

            int exitValue = -1;

			/** Ensure the task has finished **/
            try
			{
                System.out.println("_proc.exitValue() "+_proc);
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

			_finished = true;
			readySignalled();

			if ( ( _performNonZeroCheck ) && ( exitValue !=0 ) )
			{
				/**
				 * Log task result with logging service
				 */
				getLoggingService().logResult(FAIL_RESULT_TEXT, getTaskName(), _testId, _runId, _taskPermutationCode, _testPermutationCode);
				getLoggingService().logTestInformation(_testId,_runId,_testPermutationCode,"Task class not found '"+getTaskName()+"'");
			}

			/*
			 * If there is a listener registered then
			 * inform the listener that the task has finished.
			 */
			if (_listener!=null)
			{
				_listener.taskFinished( _taskId, _associatedTestNode, _testPermutationCode, true );
			}

            System.out.println("_proc = null");
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
			_running = false;
        }
    }

    public final boolean isRunning()
    {
        if ( _proc != null )
        {
            try
            {
                _proc.exitValue();

                return false;
            }
            catch (IllegalThreadStateException e)
            {
                return true;
            }
        }

        return false;
    }
}
