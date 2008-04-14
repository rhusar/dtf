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

package org.jboss.dtf.testframework.testnode;

import org.apache.tools.ant.*;

import java.net.URL;
import java.io.*;
import java.util.Vector;
import java.util.Properties;
import java.util.Enumeration;

import org.jboss.dtf.testframework.utils.HTTPDownload;
import org.jboss.dtf.testframework.utils.RemoteFileReader;

public class ANTInstaller
{
	private final static String DEPLOY_LOG_DIRECTORY_NAME = "deploylogs";
	private final static String OUT_LOG_SUFFIX = "_deploy.out";
	private final static String ERR_LOG_SUFFIX = "_deploy.err";

	private static File	 _deployLogDir = new File(DEPLOY_LOG_DIRECTORY_NAME);
	private PrintStream  _deployOut = null;
	private PrintStream  _deployErr = null;

	protected ANTInstaller()
	{
		// Protect constructor
	}

	private BuildLogger createLogger(String productName)
	{
		DefaultLogger logger = new DefaultLogger();

		try
		{
			_deployOut = new PrintStream(new FileOutputStream(new File(_deployLogDir, productName+OUT_LOG_SUFFIX)));
			_deployErr = new PrintStream(new FileOutputStream(new File(_deployLogDir, productName+ERR_LOG_SUFFIX)));

			_deployOut.println("Log started "+new java.util.Date().toString());

			logger.setOutputPrintStream(_deployOut);
			logger.setErrorPrintStream(_deployErr);
			logger.setMessageOutputLevel(Project.MSG_INFO);
		}
		catch (Exception e)
		{
			System.err.println("Failed to create log file: "+e);
		}

		return logger;
	}

	private void closeLogs()
	{
		_deployOut.println("-------------------------------------------");
		_deployOut.println("Log completed "+new java.util.Date().toString());
		_deployOut.close();
		_deployErr.close();
	}

    public static boolean install( String productName, URL antScriptURL, Properties userProperties )
    {
        boolean returnValue = true;
        ANTInstaller inst = null;

        try
        {
            /**
             * Setup ANT project
             */
            Project project = new Project();
            project.init();
            project.setCoreLoader(null);

            /**
             * If there are properties to be set then set them
             */
            if ( userProperties != null )
            {
                Enumeration e = userProperties.keys();

                while (e.hasMoreElements())
                {
                    String propertyName = (String)e.nextElement();
                    String propertyValue = (String)userProperties.get(propertyName);

					System.out.println(propertyName + " = " + propertyValue);
                    project.setUserProperty(propertyName, propertyValue);
                }
            }

            project.setUserProperty("ant.version", Main.getAntVersion());

			inst = new ANTInstaller();
			project.addBuildListener(inst.createLogger(productName));
			File buildFile = new File( HTTPDownload.downloadFileToLocalArea( antScriptURL.toExternalForm() ) );
            project.setUserProperty("ant.file", buildFile.getAbsolutePath());
            ProjectHelper.configureProject(project, buildFile);

            Vector targets = new Vector(1);
            targets.addElement(project.getDefaultTarget());

            project.executeTargets(targets);
        }
        catch (BuildException e)
        {
			if ( inst != null )
				inst.logException(e);

            System.err.println("ERROR - An error occurred in the ANT installation script: "+e);
            returnValue = false;
        }
        catch (Exception e)
        {
			if ( inst != null )
				inst.logException(e);
            System.err.println("ERROR - Failed to perform ANT install: "+e);
            e.printStackTrace(System.err);
            returnValue = false;
        }
		finally
		{
			if ( inst != null )
				inst.closeLogs();
		}

        return returnValue;
    }

	private void logException(Exception e)
	{
		e.printStackTrace(_deployErr);
	}

	public static RemoteFileReader getDeployLogErrorStream(String productName) throws java.io.IOException
	{
		return new RemoteFileReader(new File(_deployLogDir, productName+ERR_LOG_SUFFIX));
	}

	public static RemoteFileReader getDeployLogOutputStream(String productName) throws java.io.IOException
	{
		return new RemoteFileReader(new File(_deployLogDir, productName+OUT_LOG_SUFFIX));
	}

	static
	{
		/** Ensure the deploy log directory exists **/
		_deployLogDir.mkdirs();
	}
}
