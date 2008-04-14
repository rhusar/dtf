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

import org.jboss.dtf.testframework.unittest.Test ;
import org.apache.tools.ant.BuildException ;
import org.apache.tools.ant.Project ;
import org.apache.tools.ant.ProjectHelper ;
import org.apache.tools.ant.DefaultLogger ;

import java.io.File ;
import java.util.Vector ;

public class AntRunner extends Test
{

    public void run(String[] args)
    {
	this.runTarget(args) ;
    }

    public void runTarget(String[] args)
    {

	try {
	    // first find build file
	    String buildFileLoc = "build.xml" ;

	    for(int i=0; i < args.length; i++) {

		if (args[i].equals("-buildfile")) {
		    buildFileLoc = args[i+1] ;
		    break ;
		}
	    }

	    System.out.println("using build file" + buildFileLoc) ;
	    File buildFile = new File(buildFileLoc) ;

	    // setup ant project
	    Project project = new Project() ;
	    project.init() ;
	    project.setCoreLoader(null) ;

	    // get and set the user properties

	    for(int i=0; i < args.length; i++) {

		if (args[i].startsWith("-D")) {
		    String key = args[i].substring(2, args[i].indexOf("=")) ;
		    String val = args[i].substring(args[i].indexOf("=")+1) ;

		    project.setProperty(key,val) ;
		    System.out.println("setting property:" + key + "=" + val) ;
		}
	    }

	    System.out.println("setting ant.version:" + org.apache.tools.ant.Main.getAntVersion()) ;
	    project.setUserProperty("ant.version", org.apache.tools.ant.Main.getAntVersion()) ;

	    System.out.println("setting ant.file:" + buildFile.getAbsolutePath()) ;
	    project.setUserProperty("ant.file", buildFile.getAbsolutePath()) ;

	    ProjectHelper.configureProject(project, buildFile) ;

	    // get and set the targets to be executed

	    Vector targets = new Vector() ;
	    for(int i=1; i < args.length; i++) {

		if (args[i].startsWith("-D")) {
		    System.out.println("ignoring param for target:" + args[i]) ;
		}
		else if (args[i].startsWith("-buildfile")) {
		    System.out.println("ignoring param for target:" + args[i]) ;
		    i++ ;
		    System.out.println("ignoring param for target:" + args[i]) ;
		}
		else {
		    String target = args[i] ;
		    System.out.println("adding target:" + target) ;
		    targets.addElement(target) ;
		}
	    }

	    //note: default property value was also set to "reports/html" instead of "."
	    String propVal = System.getProperty("org.jboss.dtf.testframework.testnode.UnitTestTaskRunner.resultsdirectory",".") ;
	    project.setProperty("dtf.report.dir", propVal) ;

	    System.out.println("setting property:" + "dtf.report.dir" + "=" + propVal) ;

	    // execute the ant script
	    System.out.println("executing script") ;

	    DefaultLogger logger = new DefaultLogger() ;

	    logger.setOutputPrintStream(System.out) ;
	    logger.setErrorPrintStream(System.err) ;
	    logger.setMessageOutputLevel(Project.MSG_INFO) ;

	    project.addBuildListener(logger) ;

	    project.executeTargets(targets) ;

	    assertSuccess() ;
	}
	catch (BuildException e) {

	    System.err.println("Build failed - build exception") ;
	    e.printStackTrace() ;
	    assertFailure() ;
	}
	catch (Exception e) {

	    System.err.println("Build failed - general exception") ;
	    e.printStackTrace() ;
	    assertFailure() ;
	}
	catch (Error e) {

	    System.err.println("Build failed - error") ;
	    e.printStackTrace() ;
	    assertFailure() ;
	}
    }
}
