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

import org.jboss.dtf.testframework.coordinator2.TestDefinition;
import org.jboss.dtf.testframework.coordinator2.TaskDefinition;
import org.jboss.dtf.testframework.coordinator2.TaskDefinitionRepository;
import org.jboss.dtf.testframework.coordinator2.TestDefinitionRepository;
import org.jboss.dtf.testframework.coordinator.*;

import java.net.URL;
import java.util.*;

/**
 * A simple DTF test coordinator designed to run DTF tests without requiring a web server, database or
 * other overheads. It will create an in-process TestNode, so won't run any tests requring more than
 * one node. It still spawns the test runners as separate processes though.
 *
 * We aim to reuse as much unaltered DTF code as possible, as a result of which we have to jump through
 * a few unweildy hoops to deal with DTF design issues.
 *
 * @author Jonathan Halliday (jonathan.halliday@redhat.com)
 */
public class LocalTestManager
{
    private TestDefinitionRepository _testDefinitionRepository;
    private TaskDefinitionRepository _taskDefinitionRepository;
    private LocalTestNode _testNode;
    private int expectedPasses;

    /**
     * Command line invocation, such as from an ant script or terminal session.
     *
     * Requires 3 arguments: The testdefs file to run (URL), the product config file (filepath), the node config file (URL).
     * Not the most user friendly API, but the one that maps most directly to the way DTF works. This will run all the tests
     * in the test defs file, on the product given by the product config, using the environment from the node config.
     *
     * Sample Usage: java org.jboss.dtf.testframework.local.LocalTestManager \
     *   file:///home/jhalli/IdeaProjects/jboss/jbossts_trunk/qa/testdefs/jbossts-qa-txcore-testdefs.xml \
     *   /home/jhalli/IdeaProjects/jboss/jbossts_trunk/qa/config/JBossTS_JTA_QA.xml \
     *   file:///home/jhalli/IdeaProjects/jboss/jbossts_trunk/qa/config/nodeconfig.xml
     *
     * Note that JUnitTestSuite may also be used in like manner, although with slightly different arguments, just to confuse the unwary.
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {

        TestDefinitionRepository testDefinitionRepository = new TestDefinitionRepository(new URL(args[0]));
        TaskDefinitionRepository taskDefinitionRepository = new TaskDefinitionRepository(new URL(args[0]));
        testDefinitionRepository.verifyRepository(taskDefinitionRepository);

        LocalTestManager test = new LocalTestManager(testDefinitionRepository, taskDefinitionRepository, args[1], args[2]);
        try {
            test.runAllTests();
        } catch(Exception e) {
            System.out.println("cleaning up...");
            test = null;
            System.gc();
            System.runFinalization();
            // System.exit(1);
            // Note that a RMI issue can cause the JVM not to exit properly here in some cases.
            // probably because we reuse RMI classes but use them locally, so the RMI lifecycle
            // may not be properly managed. More investigation is needed.
            throw e;
        }
    }

    public static LocalTestManager getInstance(String testdefsFile, String productConfigFile, String testnodeConfigFile) throws Exception {
        TestDefinitionRepository testDefinitionRepository = new TestDefinitionRepository(new URL("file://"+testdefsFile));
        TaskDefinitionRepository taskDefinitionRepository = new TaskDefinitionRepository(new URL("file://"+testdefsFile));

        return new LocalTestManager(testDefinitionRepository, taskDefinitionRepository, productConfigFile, "file://"+testnodeConfigFile);
    }

    public LocalTestManager(TestDefinitionRepository testDefinitionRepository, TaskDefinitionRepository taskDefinitionRepository,
                String productConfigurationFileName, String testNodeConfigURL) throws Exception
    {
        _testDefinitionRepository = testDefinitionRepository;
        _taskDefinitionRepository = taskDefinitionRepository;

        // Kludge warning: the logging framework loads plugin using Class.forName, so we can't cleanly configure an
        // instance and pass it in. Thus we use a nasty hack involving static field here...
        System.setProperty("org.jboss.dtf.testframework.logging.plugin", "org.jboss.dtf.testframework.local.LocalLoggingService");
        LocalLoggingService.setLocalTestManager(this);

        _testNode = new LocalTestNode(new URL(testNodeConfigURL), productConfigurationFileName);
    }

    // used by the JUnit integration classes.
    public List<TestDefinition> getSingleNodeTestsDefs() {
        Map<String, TestDefinition> testIDs2testDefs = _testDefinitionRepository.getTestDefinitionsMap();
        List<TestDefinition> testDefs = new LinkedList<TestDefinition>();
        for(Map.Entry<String,TestDefinition> entry : testIDs2testDefs.entrySet()) {
            TestDefinition testDefinition = entry.getValue();
            if(testDefinition.getNumberOfNodesRequired() == 1) {
                testDefs.add(testDefinition);
            } else {
                System.out.println("skipping "+testDefinition.getNumberOfNodesRequired());
            }
        }
        return testDefs;
    }

    public void runAllTests() throws Exception {
        Map<String, TestDefinition> testIDs2testDefs = _testDefinitionRepository.getTestDefinitionsMap();
        for(Map.Entry<String,TestDefinition> entry : testIDs2testDefs.entrySet()) {
            TestDefinition testDefinition = entry.getValue();
            executeTest(testDefinition);
        }
    }

    /**
     * Run the given test. If this returns normally rather than throwing an exception,
     * the test can be assumed to have passed, or at least not actually failed. Probably.
     * I mean, it's not like this is an exact science or anything :-)
     *
     *
     *
     * @param testDefinition
     * @throws Exception
     */
    public void executeTest(TestDefinition testDefinition) throws Exception {
        if(testDefinition == null) {
            throw new IllegalArgumentException("testDefinition must not be null!");
        }

        if(testDefinition.getNumberOfNodesRequired() != 1) {
            System.err.println("Unsupported node count "+testDefinition.getNumberOfNodesRequired()+" for test "+testDefinition.getDescription()+", skipping it");
            return;
        }

        ArrayList<Action> actionList = testDefinition.getActionList();

        synchronized(this) {
            expectedPasses = 0;
        }

        for(Action action : actionList) {

            TaskDefinition taskDefinition = null;
            if(action.getType() == Action.PERFORM_TASK || action.getType() == Action.START_TASK) {
                String groupId = testDefinition.getGroupId();
                String taskIdToPerform	= action.getAssociatedTaskId();
                taskDefinition = _taskDefinitionRepository.getTaskDefinition(groupId, taskIdToPerform);
                if (taskDefinition == null)
                {
                    taskDefinition = _taskDefinitionRepository.getTaskDefinition(taskIdToPerform);
                }
            }

            // for each task we start, we expect to recieve a notification that it passed. We keep count
            // of the number of such notifications outstanding and get upset if it's non-zero at the end.

            switch(action.getType()) {
                case Action.PERFORM_TASK:
                    System.out.println("PERFORM_TASK "+new Date().toString());
                    _testNode.performTask(testDefinition, taskDefinition, action);
                    synchronized (this) {
                        expectedPasses+=1;
                    }
                    break;
                case Action.START_TASK:
                    System.out.println("START_TASK "+new Date().toString());
                    _testNode.startTask(testDefinition, taskDefinition, action);
                    synchronized (this) {
                        expectedPasses+=1;
                    }
                    break;
                case Action.TERMINATE_TASK:
                    System.out.println("TERMINATE_TASK "+new Date().toString());
                    _testNode.terminateTask(action);
                    break;
                case Action.WAIT_FOR_TASK:
                    System.out.println("WAIT_FOR_TASK "+new Date().toString());
                    _testNode.waitForTask(action);
                    break;
                default:
                    throw new IllegalArgumentException("Unknown Action type "+action.getType());
            }
        }

        // make sure the termination state is sane...

        if(_testNode.isActive()) {
            throw new IllegalStateException("Testnode is active when it should not be!");
        }

        if(LocalLoggingService.getTaskCount() != 0) {
            LocalLoggingService.resetTraskCount();
            throw new IllegalStateException("TaskCount is not zero when it should be!");
        }

        if(expectedPasses != 0) {
            throw new Exception("Test failed! "+testDefinition.getId()+" got "+expectedPasses+" fewer task passes than expected");
        }
    }

    // the logger calls back into us using this function...
    public void logTaskResult(String testName, String taskName, String result) {

        if("Passed".equals(result)) {
            synchronized (this) {
                expectedPasses-=1;
            }
        }
    }

}
