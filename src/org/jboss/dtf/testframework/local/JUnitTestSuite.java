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
 *
 * $Id$
 */
package org.jboss.dtf.testframework.local;

import junit.framework.TestSuite;
import junit.framework.Test;
import org.jboss.dtf.testframework.coordinator2.TestDefinition;

import java.util.List;

/**
 * An adaptor class that allows DTF tests to be run as though they are JUnit tests, using the
 * lightweight in-process test runner. This is handy for integration with build tools that
 * already understand junit but not DTF. Ant is the primary example:
 *
    <junit printsummary="yes" haltonfailure="yes">

        <sysproperty key="testdefsFile" value="/home/jhalli/IdeaProjects/jboss/jbossts_trunk/qa/testdefs/jbossts-qa-txcore-testdefs.xml"/>
        <sysproperty key="productConfigFile" value="/home/jhalli/IdeaProjects/jboss/jbossts_trunk/qa/config/JBossTS_JTA_QA.xml"/>
        <sysproperty key="testnodeConfigFile" value="/home/jhalli/IdeaProjects/jboss/jbossts_trunk/qa/config/nodeconfig.xml"/>

        <test name="org.jboss.dtf.testframework.local.JUnitTestSuite" outfile="output"/>
    </junit>
 *
 * @author Jonathan Halliday (jonathan.halliday@redhat.com)
 */
public class JUnitTestSuite extends TestSuite
{
    LocalTestManager localTestManager;

    /*
     * Command line invocation, such as from an ant script or terminal session.
     *
     * Requires 3 filepath arguments: The testdefs file to run, the product config file, the node config file.
     * Requires 1 regexp argument: the testname pattern to match. Runs all matching tests in the testdefs file.
     *
     * Sample Usage: java org.jboss.dtf.testframework.local.JUnitTestSuite \
     *   /home/jhalli/IdeaProjects/jboss/jbossts_trunk/qa/testdefs/jbossts-qa-txcore-testdefs.xml \
     *   /home/jhalli/IdeaProjects/jboss/jbossts_trunk/qa/config/JBossTS_JTA_QA.xml \
     *   /home/jhalli/IdeaProjects/jboss/jbossts_trunk/qa/config/nodeconfig.xml
     *   <regexp_testname_pattern>
     *
     * Note that LocalTestManager may also be used in like manner, although with slightly different arguments,
     * just to confuse the unwary.
     *
     */
    public static void main(String args[])
    {
        junit.textui.TestRunner.run(new JUnitTestSuite(args[0], args[1], args[2], args[3]));
    }

    // The suite method is used when frameworks e.g. ant junit task, load the class.
    // Note that we can't pass function args in the normal way, so we resort to
    // configuring via system properties instead...
    public static Test suite()
    {
        return new JUnitTestSuite();
    }

    // The no-args ctor gets its config via system properties instead.
    public JUnitTestSuite()
    {
        super();

        String testdefsFile = System.getProperty("testdefsFile");
        String productConfigFile = System.getProperty("productConfigFile");
        String testnodeConfigFile = System.getProperty("testnodeConfigFile");
        String testnamePattern = System.getProperty("testnamePattern");

        setup(testdefsFile, productConfigFile, testnodeConfigFile, testnamePattern);
    }

    public JUnitTestSuite(String testdefsFile, String productConfigFile, String testnodeConfigFile, String testnamePattern)
    {
        super();

        setup(testdefsFile, productConfigFile, testnodeConfigFile, testnamePattern);
    }

    private void setup(String testdefsFile, String productConfigFile, String testnodeConfigFile, String testnamePattern)
    {
        try
        {
            localTestManager = LocalTestManager.getInstance(testdefsFile, productConfigFile, testnodeConfigFile);
            List<TestDefinition> testDefs = localTestManager.getSingleNodeTestsDefs();
            for (TestDefinition testDefinition : testDefs)
            {
                if(testnamePattern == null || testDefinition.getId().matches(testnamePattern)) {
                    addTest(new JUnitTest(localTestManager, testDefinition));
                } else {
                    System.out.println("skipping non-matched test "+testDefinition.getId());
                }
            }
            System.out.println("Number of tests to run "+this.testCount());
        }
        catch (Exception e)
        {
            throw new ExceptionInInitializerError(e);
        }
    }


}