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
// $Id: ServiceWatchdog.java 170 2008-03-25 18:59:26Z jhalliday $

package org.jboss.dtf.testframework.servicemanager;

import org.jboss.dtf.testframework.utils.LogWriter;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.util.ArrayList;

public class ServiceWatchdog extends Thread
{
	private static final int 	SERVICE_FAILURE = 2;
    private static final int    SERVICE_COMPLETE = 0;

	private LogWriter 	_serviceLogWriter = new LogWriter();
	private String		_serviceName;
	private String  	_serviceClass;
	private String[]	_params;
	private Process 	_serviceProc;
	private boolean 	_signalledReady = false;
	private boolean		_keepAlive = true;
    private String[]  	_properties = null;
	private String[]	_settings = null;
	private ClasspathConfiguration _classpath = null;

	public ServiceWatchdog( String serviceName,
							String serviceClass,
							String[] params,
							String[] properties,
							String[] settings,
							long timeout,
							ClasspathConfiguration classpath) throws Exception
	{
		_serviceName = serviceName;
		_serviceClass = serviceClass;
        _properties = properties;
		_params = params;
		_settings = settings;
		_classpath = classpath;
		_serviceLogWriter.openLog(serviceName.replace(' ','_')+"_service.log");
		this.start();
		waitForSignalOrTimeout(serviceName, timeout);
	}

	private synchronized void waitForSignalOrTimeout(String serviceName, long timeout) throws Exception
	{
		wait(timeout*1000);

		if (!_signalledReady)
		{
			stopService();
			throw new Exception("Service '"+serviceName+"' did not start correctly");
		}
	}

	private synchronized void signal()
	{
		notifyAll();
	}

	public void run()
	{
        String jdk_home = System.getProperty("com.arjuna.mw.testframework.jdk.home", "");

		System.out.println("Service Watchdog started for '"+_serviceName+"'");

        if (jdk_home.length() > 0)
        {
            jdk_home += '/';
            System.out.println("JDK Home set to '"+jdk_home+"'");
        }

		_serviceLogWriter.writeLog("Service Watchdog started for '"+_serviceName+"'");

		while (_keepAlive)
		{
			try
			{
				/*
				 * Start Java Service passing parameters
				 */
				Runtime rt = Runtime.getRuntime();

				_serviceLogWriter.writeLog("Starting service '"+_serviceName+"'");

				ArrayList parameters = new ArrayList();

				parameters.add( jdk_home+"java" );

				if ( _classpath != null )
				{
					parameters.add( "-cp" );
					parameters.add( _classpath.getClasspath() );
				}

				if ( _classpath != null )
				{
					parameters.add( "-Djava.class.path="+_classpath.getClasspath() );
				}

				if ( _settings != null )
				{
					for (int count=0;count<_settings.length;count++)
					{
						parameters.add( _settings[count] );
					}
				}

				if ( _properties != null )
				{
					for (int count=0;count<_properties.length;count++)
					{
						parameters.add( _properties[count] );
					}
				}

				parameters.add( _serviceClass );

				if ( _params != null )
				{
					for (int count=0;count<_params.length;count++)
					{
						parameters.add( _params[count] );
					}
				}

				String[] execParams = new String[ parameters.size() ];
				parameters.toArray(execParams);

				_serviceProc = rt.exec(execParams);

				/*
				 * Wait for service to signal 'Ready'
				 */
				new ErrorStreamHandler(_serviceProc.getErrorStream(),_serviceLogWriter);
				BufferedReader in = new BufferedReader(new InputStreamReader(_serviceProc.getInputStream()),65535);
				String inLine;
				boolean finished = false;
				int exitValue = 0;

				while ( (inLine = in.readLine()) != null )
				{
					_serviceLogWriter.writeLog("[Output]:"+inLine);
					if (inLine.equals("Ready"))
					{
						_signalledReady = true;
						signal();
					}
					else
						System.out.println("["+_serviceName+"_out]:"+inLine);
				}

				while (!finished)
				{
					try
					{
						exitValue = _serviceProc.exitValue();
						finished = true;
					}
					catch (IllegalThreadStateException e)
					{
					}
				}

				System.out.println("Exit value "+exitValue+" reported");
				/*
				 * Service report back via the exit value their state on finishing
				 *
                 *     SERVICE_COMPLETE   |  The service has completed and doesn't want to be restarted
				 * 		SERVICE_FAILURE   |  The service failed and doesn't want to be restarted
				 *
				 * Any other value will indicate that the service is ok with being restarted
				 */
				switch(exitValue)
				{
                    case SERVICE_COMPLETE :
                        _keepAlive = false;
                        break;
					case SERVICE_FAILURE :
						System.out.println("Service '"+_serviceName+"' reported SERVICE_FAILURE");
						_keepAlive = false;
						break;
				}

				in.close();
				signal();
			}
			catch (java.io.IOException e)
			{
				System.out.println("ERROR: "+e.toString());
			}

			if (_keepAlive)
			{
				_classpath.ensureCacheUptodate();
			}
		}
		_serviceLogWriter.closeLog();
	}

	public void stopService()
	{
		_keepAlive = false;
		if (_serviceProc!=null)
		{
			_serviceProc.destroy();
		}
	}

	private class ErrorStreamHandler extends Thread
	{
	    private InputStream _errorStream = null;
	    private LogWriter   _serviceLogWriter = null;

	    public ErrorStreamHandler(InputStream is, LogWriter logWriter)
	    {
	        _errorStream = is;
	        _serviceLogWriter = logWriter;
	        start();
	    }

	    public void run()
	    {
	        try
	        {
				BufferedReader in = new BufferedReader(new InputStreamReader(_errorStream));
				String inLine;

				while ( (inLine = in.readLine()) != null )
				{
					_serviceLogWriter.writeLog("[Error]:"+inLine);
					System.out.println("["+_serviceName+"_err]:"+inLine);
				}

				in.close();
            }
            catch (java.io.IOException e)
            {
            }
        }
     }
}
