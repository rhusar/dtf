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

// change this package
package org.jboss.dtf.testframework.testnode ;

import org.apache.tools.ant.BuildException ;
import org.apache.tools.ant.Project ;
import org.apache.tools.ant.ProjectHelper ;
import org.apache.tools.ant.DefaultLogger ;
import org.jboss.dtf.testframework.utils.logging.exception.LoggingServiceException;

import java.io.File ;
import java.util.Vector ;
import java.util.Collection;
import java.util.ArrayList;

public class AntTaskRunner extends TaskRunner
{
    public final static String PRODUCT_DIR_PROP = "product.dir";

    public void runTarget(String[] args)
    {
        String result = "Failed";

        try {
            // first find build file
            File buildFile = null;
            Vector<String> targets = new Vector<String> ();

            setProductDir();

            // get and set the user properties
            for (String arg : args)
            {
                String[] kvp = arg.split("=");

                if (kvp.length == 2)
                {
                    String name = kvp[0].trim();
                    String value = kvp[1].trim();

                    if ("buildfile".equals(name))
                    {
                        buildFile = new File(getProductDir().getAbsolutePath() + '/' + value);
                    }
                    else if ("targets".equals(name))
                    {
                        for (String target : value.split(","))
                            targets.add(target.trim());
                    }
                    else if (name.startsWith("-D"))
                    {
                        System.err.println("Ignoring task parameter " + arg + " use jvm_param to set system parameters");
                    }
                }
                else
                {
                    System.err.println("Ignoring invalid task parameter " + arg);
                }
            }

            for (String prop : _jvmParameters)
            {
                if (prop.startsWith("-D"))
                    prop = prop.substring(2);

                if (DebugEnabled)
                    System.out.println("setting jvm property: " + prop);

                String[] kvp = prop.split("=");

                if (kvp.length == 2)
                {
                    String name = kvp[0].trim();
                    String value = kvp[1].trim();

                    if ("java.security.policy".equals(name))
                    {
                        File policyFile = new File(value);

                        if (!policyFile.exists())
                            policyFile = new File(getProductDir().getAbsolutePath() + '/' + value);

                        if (policyFile.exists())
                            System.setProperty(name, policyFile.getAbsolutePath());
                        else
                            System.err.println("Policy file not found: " + policyFile.getAbsolutePath());
                    }
                    else
                        System.setProperty(name, value);
                }
                else
                {
                    System.err.println("Ignoring invalid jvm parameter " + prop);
                }
            }


            if (buildFile == null)
                buildFile = new File("build.xml");
            
            // setup ant project
            Project project = new Project() ;
            project.init() ;
            project.setCoreLoader(null) ;
            project.setUserProperty("ant.version", org.apache.tools.ant.Main.getAntVersion()) ;
            project.setUserProperty("ant.file", buildFile.getAbsolutePath()) ;

            ProjectHelper.configureProject(project, buildFile) ;

            //note: default property value was also set to "reports/html" instead of "."
            String propVal = System.getProperty("org.jboss.dtf.testframework.testnode.UnitTestTaskRunner.resultsdirectory",".") ;
            project.setProperty("dtf.report.dir", propVal) ;

            if (DebugEnabled)
            {
                System.out.println("ant.version:" + org.apache.tools.ant.Main.getAntVersion()) ;
                System.out.println("ant.file:" + buildFile.getAbsolutePath()) ;
                System.out.println("setting property:" + "dtf.report.dir" + "=" + propVal) ;
                System.out.println("executing ant script") ;
            }

            DefaultLogger logger = new DefaultLogger() ;

            logger.setOutputPrintStream(System.out) ;
            logger.setErrorPrintStream(System.err) ;
            logger.setMessageOutputLevel(Project.MSG_INFO) ;

            project.addBuildListener(logger) ;

            // execute the ant script
            if (buildFile.exists())
            {
                project.executeTargets(targets);
                result = "Passed";
            }
            else
            {
                System.err.println("Cannot locate build file");
            }

            indicateTaskIsRunning();

            readySignalled();
        }
        catch (BuildException e)
        {
            System.err.println("Build failed - build exception") ;
            e.printStackTrace() ;
        }
        catch (Exception e)
        {
            System.err.println("Build failed - general exception") ;
            e.printStackTrace() ;
        }
        catch (Error e)
        {
            System.err.println("Build failed - error") ;
            e.printStackTrace() ;
        }

        // report the result of the test
        try
        {
            System.out.println("Task " + _testId + ' ' + result);
            getLoggingService().logResult(result, getTaskName(), _testId, _runId, _taskPermutationCode, _testPermutationCode);
            _finished = true;
            /*
            * If there is a listener registered then
            * inform the listener that the task has finished.
            */
            if (_listener!=null)
                _listener.taskFinished( _taskId, _associatedTestNode, _testPermutationCode, true );
        }
        catch (LoggingServiceException e)
        {
            System.err.println("Unable to report task result: " + e.getMessage()) ;
            e.printStackTrace();
        }
        catch (NoSuchTaskId noSuchTaskId)
        {
            System.out.println("Informed TestNode task has finished - task unknown: " + noSuchTaskId.getMessage());
        }
        catch (java.rmi.RemoteException e)
        {
            System.err.println("Unable to signal task complete: " + e.getMessage()) ;
        }
    }

    private File getProductDir()
    {
        setProductDir();

        return new File(System.getProperty(PRODUCT_DIR_PROP));
    }

    private void setProductDir()
    {
        String dirName = System.getProperty(PRODUCT_DIR_PROP);
        File productDir;

        if (dirName == null)
        {
            String home = System.getenv("DTF_HOME");
            String productName = _productConfig.getName();

            productDir = new File(home + '/' + productName);

            if (!productDir.exists())
                productDir = new File(home + '/' + productName.replaceAll("_", "-"));

            System.setProperty(PRODUCT_DIR_PROP, productDir.getAbsolutePath());

            if (DebugEnabled)
                System.out.println("Product dir is " + System.getProperty(PRODUCT_DIR_PROP));
        }
    }

    public void runTask() throws Exception
    {        
        runTarget(_parameters);
    }

    public boolean terminate()
    {
        return true;
    }

    public void waitFor() throws InterruptedException
    {

    }

}
