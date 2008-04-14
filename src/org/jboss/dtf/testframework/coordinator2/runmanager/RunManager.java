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
/*
 * Copyright (C) 1998, 1999, 2000, 2001, 2002, 2003
 *
 * Arjuna Technologies Ltd.
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: RunManager.java 170 2008-03-25 18:59:26Z jhalliday $
 */

package org.jboss.dtf.testframework.coordinator2.runmanager;

import org.jboss.dtf.testframework.coordinator.InvalidDefinitionFile;
import org.jboss.dtf.testframework.coordinator.TestUndefined;
import org.jboss.dtf.testframework.coordinator.ResourceAllocationFailure;
import org.jboss.dtf.testframework.coordinator2.*;
import org.jboss.dtf.testframework.coordinator2.scheduler.ScheduleInformation;
import org.jboss.dtf.testframework.coordinator2.auditlog.AuditLog;
import org.jboss.dtf.testframework.coordinator2.auditlog.AuditLogEntry;

import org.jboss.dtf.testframework.coordinator2.testmanager.TestManager;
import org.jboss.dtf.testframework.coordinator2.testmanager.NodeTicketReleaseListener;
import org.jboss.dtf.testframework.testnode.RunUID;
import org.jboss.dtf.testframework.serviceregister.ServiceNotFound;
import org.jboss.dtf.testframework.InvalidConfiguration;

import java.net.URL;
import java.util.*;
import java.io.IOException;

import org.jdom.input.DOMBuilder;
import org.jdom.input.SAXBuilder;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.apache.log4j.Logger;

public class RunManager extends Thread implements Comparator, NodeTicketReleaseListener, ResultListener
{
    public final static int            ALL_PERMUTATION_OPTION = 0;
    public final static int            OS_PERMUTATION_OPTION = 0;
    public final static int            PRODUCT_PERMUTATION_OPTION = 0;

    private final static String[]       PERMUTATION_OPTIONS = { "all", "os", "product" };

    private static long                 _currentRunManagerId = 0;
    private static NodeManager			_nodeManager = null;

	private LinkedList					_runList = null;
	private TestDefinitionRepository 	_testRepository;
	private TaskDefinitionRepository	_taskRepository;
	private RunUID						_runId;
    private Logger		                _logger = Logger.getLogger(this.getClass());
	private RunListener					_listener;
	private AuditLog					_auditLog;
	private Hashtable					_auditEntryMap = new Hashtable();
    private boolean						_stopped = false;
	private WeakHashMap					_managers = new WeakHashMap();
    private int							_testsToRun = 0;
	private int							_testsRunning = 0;
    private RunInformation				_runInfo = null;
    private int                         _permutationOption = ALL_PERMUTATION_OPTION;
    private ScheduleInformation         _schedule = null;
    private long                        _runManagerId;
    private HashSet                     _productsInUse = new HashSet();
    private ArrayList                   _combinations = new ArrayList();

	public RunManager( ScheduleInformation schedule,
					   RunListener listener) throws Exception
	{
        synchronized(this)
        {
            _runManagerId = _currentRunManagerId++;
            setName("RunManager["+_runManagerId+"]");
        }

		_runInfo = new RunInformation( schedule.getTestDefinitionsURL(), schedule.getTestSelectionURL(), schedule.getDistributionList(), schedule.getSoftwareVersion() );
		_listener = listener;
        _schedule = schedule;
		_testRepository = new TestDefinitionRepository(schedule.getTestDefinitionsURL());
		_taskRepository = new TaskDefinitionRepository(schedule.getTestDefinitionsURL());

		_runList = new LinkedList();


		parseSelections( schedule.getTestSelectionURL() );
		_testRepository.generatePermutations();

        if ( _logger.isInfoEnabled() )
        {
            _logger.info("Beginning test run...");
        }

        _runId = generateRunUniqueId(schedule.getTestDefinitionsURL(), schedule.getTestSelectionURL(), schedule.getSoftwareVersion(), schedule.getDistributionList());
        _auditLog = new AuditLog( _runId );

        try
        {
		    populateRunList();
        }
        catch (ServiceNotFound e)
        {
            throw new CannotStartRunException("Unable to allocate resources:"+e);
        }

        _runInfo.setRunId(_runId);

        if ( _logger.isInfoEnabled() )
        {
		    _logger.info("Running "+_runList.size()+" test(s)");
        }

		_runInfo.setTotalNumberOfTests(_testsToRun = _runList.size());


		runNextTest();
	}

    public final ScheduleInformation getScheduleInformation()
    {
        return _schedule;
    }

	public final RunInformation getRunInformation()
	{
		return _runInfo;
	}

    public final String[] getProductsInUse()
    {
        String[] result = new String[_productsInUse.size()];
        _productsInUse.toArray(result);

        return result;
    }

    /**
     * This method is called by the nodemanager to inform the run manager
     * that nodes have become available which it has registered an interest in.
     * @param freeElement
     * @return True if the node is required, false otherwise
     */
    public synchronized boolean notifyNodeAvailable(OSProductCombination freeElement)
    {
        int count=0;
        boolean runStarted = false;

        if ( _logger.isInfoEnabled() )
        {
            _logger.info("Searching run list for test which can utilize '"+freeElement.toString()+"' (run list size is "+_runList.size()+ " " + ( _stopped ? "but the test run manager has been stopped" : "run manager is still active" ) + "["+_runManagerId+"])");
        }

        while ( !_stopped && (count<_runList.size() && !runStarted) )
        {
            RunListElement element = null;
            try
            {
                element = (RunListElement)_runList.get(count);
                if ( element.getPermutation().contains(freeElement) )
                {
                    ArrayList nodeTickets = _nodeManager.getNodes(element.getPermutation());

                    if ( _logger.isInfoEnabled() )
                    {
                        _logger.info("Nodelist retrieved - test '"+element.getTest().getFullId()+"' in progress");
                    }

                    _runList.remove(count);

                    AuditLogEntry entry = AuditLog.createLogEntry(element.getTest().getFullId(), element.getPermutation().toString());
                    _managers.put(element, new TestManager( _runId, element, nodeTickets, this, _taskRepository, this, entry, _nodeManager ));

                    _testsRunning++;

                    _auditEntryMap.put( element, entry );

                    runStarted = true;
                }
            }
            catch (FatalResourceAllocationFailure e)
            {
                _logger.warn("No nodes exist which can run this test - leaving out");
                _runList.remove(count);

                try
                {
                    Coordinator.getLoggingService().logTestRunInformation("Cannot run test "+element.getTest().getFullId()+" - cannot find any resources to run it!","taskName","testName",getRunId(),"permutationcode","permutationcode");
                }
                catch (Exception ex)
                {
                    // Ignore
                }
            }
            catch (ResourceAllocationFailure e)
            {
                // Ignore it
            }

            count++;
        }

        return runStarted;
    }

	private synchronized boolean runNextTest()
	{
        boolean ensureNodesExist = false;
        boolean couldRunTest = false;

        if ( _logger.isInfoEnabled() )
        {
		    _logger.info("Running next test...");
        }

		if ( _runList.size() > 0 )
		{
			boolean runStarted = false;

            if ( !_stopped )
            {
				if ( _testsRunning == 0 )
				{
					_runInfo.setCurrentStatus("Waiting for nodes | Tests Remaining: "+_testsToRun+" of "+_runInfo.getTotalNumberOfTests());
					_runInfo.setNumberOfTestsRemaining(_testsToRun);
				}

                while ( (!runStarted) && (!couldRunTest) )
                {
                    for (int count=0;count<_runList.size();count++)
                    {
                        RunListElement element = null;
                        try
                        {
                            element = (RunListElement)_runList.get(count);
                            ArrayList nodeTickets = _nodeManager.getNodes(element.getPermutation());

                            synchronized(_managers)
                            {
                                if ( !_stopped )
                                {
                                    if ( _logger.isInfoEnabled() )
                                    {
                                        _logger.info("Nodelist retrieved - test '"+element.getTest().getFullId()+"' in progress");
                                    }

                                    _runList.remove(count);

                                    AuditLogEntry entry = AuditLog.createLogEntry(element.getTest().getFullId(), element.getPermutation().toString());
                                    _managers.put(element, new TestManager( _runId, element, nodeTickets, this, _taskRepository, this, entry, _nodeManager ));

									_runInfo.setCurrentStatus("Active Test Managers: "+(++_testsRunning)+" | Tests Remaining: "+_testsToRun+" of "+_runInfo.getTotalNumberOfTests());
									_runInfo.setNumberOfTestsRemaining(_testsToRun);

                                    _auditEntryMap.put( element, entry );

                                    runStarted = true;
                                }
                            }
                        }
                        catch (FatalResourceAllocationFailure e)
                        {
                            _logger.warn("No nodes exist which can run this test - leaving out");

                            _runList.remove(count);

                            try
                            {
                                Coordinator.getLoggingService().logTestRunInformation("Cannot run test "+element.getTest().getFullId()+" - cannot find any resources to run it!","taskName","testName",getRunId(),"permutationcode","permutationcode");
                            }
                            catch (Exception ex)
                            {
                                // Ignore
                            }
                        }
                        catch (ResourceAllocationFailure e)
                        {
                            // Ignore it

                            if (ensureNodesExist)
                            {
                                if ( !_nodeManager.ensureNodesExist(element.getPermutation()) )
                                {
                                    _logger.warn("Nodes do not exist to run this test");
                                    couldRunTest = false;
                                }
                                else
                                {
                                    couldRunTest = true;
                                }
                            }
                        }
                    }


                    /** If we didn't start a run and there are no runs in progress then we have a problem **/
                    if ( ( !_stopped ) && ( !runStarted ) && ( !couldRunTest )&& ( _testsRunning == 0 ) )
                    {
                        if ( ensureNodesExist )
                        {
                            _logger.error("Could not start a testrun and no tests are running - must be no testnodes available!");

                            try
                            {
                                Coordinator.getLoggingService().logTestRunInformation("Could not start a testrun and no tests are running - must be no testnodes available!","taskName","testName",getRunId(),"permutationcode","permutationcode");
                            }
                            catch (Exception e)
                            {
                                // Ignore
                            }

                            testRunComplete();
                            break;
                        }
                        else
                        {
                            _logger.error("Could not start a test run - ensuring testnodes exist that could run one of the tests");
                            ensureNodesExist = true;
                        }
                    }
                }
            }

			return true;
		}

		return false;
	}

	/**
	 * This method stops the run this runmanager is currently running.
	 * If the waitForTestToComplete parameter is set to false then the
	 * tests are stopped immediated otherwise the tests are stopped
	 * after the current test execution is complete.
	 *
	 * @param waitForTestToComplete
	 * @return
	 */
	public boolean stopRun(boolean waitForTestToComplete)
	{
		try
		{
			Coordinator.getLoggingService().logTestRunInformation("Test run was stopped prematurely by the user","taskName","testName",getRunId(),"permutationcode","permutationcode");
		}
		catch (Exception e)
		{
			// Ignore
		}

		if ( waitForTestToComplete )
		{
            if ( _logger.isInfoEnabled() )
            {
			    _logger.info("Request to stop run received, waiting for current tests to finish");
            }

			_stopped = true;
		}
		else
		{
            if ( _logger.isInfoEnabled() )
            {
			    _logger.info("Request to stop run received, stopping tests immediately");
            }

            synchronized(_managers)
            {
                _stopped = true;

                Object[] managers = _managers.values().toArray();
                for (int count=0;count<managers.length;count++)
                {
                    ((TestManager)managers[count]).stopTest();
                }
            }
        }

        synchronized(_managers)
        {
            if ( _managers.size() == 0 )
            {
                if ( _logger.isInfoEnabled() )
                {
                    _logger.info("No test managers in use - test run complete");
                }

                testRunComplete();
            }
        }

		return true;
	}

	public RunUID getRunId()
	{
		return _runId;
	}

	/**
	 * Populate the runlist with RunListElements
	 */
	private void populateRunList() throws ServiceNotFound
	{
		/**
		 * Sort the list of tests into nodes required order
		 */
		Collection testCollection = _testRepository.getTestDefinitionsMap().values();
		TestDefinition[] tests = new TestDefinition[testCollection.size()];
		testCollection.toArray(tests);

        Arrays.sort(tests, this);

		/**
		 * Populate the run list
		 */
		for (int count=0;count<tests.length;count++)
		{
			ArrayList permutations = tests[count].getPermutations();

			for (int pCount=0;pCount<permutations.size();pCount++)
			{
				RuntimePermutation perm = (RuntimePermutation)permutations.get(pCount);

				_runList.add( new RunListElement(tests[count], perm) );
			}
		}

        for (int cCount=0;cCount<_combinations.size();cCount++)
        {
            OSProductCombination ospc = (OSProductCombination)_combinations.get(cCount);
            _nodeManager.registerInterest(this, ospc);
        }

	}

	private void testRunComplete()
	{
		try
		{
			_auditLog.close();
		}
		catch (Exception e)
		{
			_logger.error("Failed to close audit log", e);
		}

		try
		{
			Coordinator.getLoggingService().testRunComplete(_runId);
		}
		catch (Exception e)
		{
			_logger.error("Failed to log test run complete");
		}

        _nodeManager.unregisterInterest(this);

		if ( _logger.isInfoEnabled() )
        {
            _logger.info("Test run complete.");
        }

		_listener.runComplete(this);
	}


    /**
     * Release the node ticket.
     *
     * @param nodeList The list of nodes to release.
     */
    public void releaseNodeTicket(ArrayList nodeList, OSProductCombination[] elements)
    {
        for (int count=0;count<nodeList.size();count++)
        {
            TestNodeTicket tnt = (TestNodeTicket)nodeList.get(count);

            tnt.setNotInUse();

            _nodeManager.notifyNodesReleased(tnt);
        }
	}

	private void parseSelections( URL testSelectionsURL ) throws InvalidDefinitionFile
	{
		try
		{
			SAXBuilder xmlBuilder = new SAXBuilder();
            Document doc = null;
            try {
                doc = xmlBuilder.build(testSelectionsURL);
            } catch(IOException e) {
                throw new InvalidDefinitionFile(e.toString());
            }

			/*
			 * Retrieve root element
			 */
			Element root = doc.getRootElement();

            /** Retrieve options element **/
            Element options = root.getChild("options");

            if ( options != null )
            {
                String permutations = options.getAttributeValue("permutations");

                for (int count=0;count<PERMUTATION_OPTIONS.length;count++)
                {
                    if ( permutations.equalsIgnoreCase(PERMUTATION_OPTIONS[count]) )
                    {
                        _permutationOption = count;
                    }
                }
            }

			/*
			 * Retrieve list of all OS elements
			 */
			List osList = root.getChildren("os");

			for (int osCount=0;osCount<osList.size();osCount++)
			{
				Element osElement = (Element)osList.get(osCount);
				String osId = osElement.getAttributeValue("id");

				/*
				 * Retrieve list of all Product elements within this OS element
				 */
				List productList = osElement.getChildren("product");
				for (int productCount=0;productCount<productList.size();productCount++)
				{
					Element productElement = (Element)productList.get(productCount);
					String productId = productElement.getAttributeValue("id");

					/*
					 * Retrieve list of all test groups within this product element
					 */
					List testGroupList = productElement.getChildren("test_group");

					for (int testGroupCount=0;testGroupCount<testGroupList.size();testGroupCount++)
					{
						Element testGroupElement = (Element)testGroupList.get(testGroupCount);
						String groupId = testGroupElement.getAttributeValue("id");

						parseTestGroupSelections(groupId, testGroupElement, osId, productId);
					}
				}
			}

            if ( _logger.isInfoEnabled() )
            {
			    _logger.info("Complete");
            }
		}
		catch (TestUndefined e)
		{
			_logger.error("ERROR: The selection file contains a test which is not defined ", e);
			throw new InvalidDefinitionFile("The selection file contains a test which is not defined");
		}
		catch (JDOMException e)
		{
			_logger.error("ERROR: Incorrect test selection file", e);
			throw new InvalidDefinitionFile("Incorrect test selection file");
		}
	}

	private void parseTestGroupSelections(String groupName, Element root, String osId, String productId) throws TestUndefined
	{
		/*
		 * Retrieve list of all test groups within this product element
		 */
		List testGroupList = root.getChildren("test_group");
		for (int testGroupCount=0;testGroupCount<testGroupList.size();testGroupCount++)
		{
			Element testGroupElement = (Element)testGroupList.get(testGroupCount);
			String groupId = testGroupElement.getAttributeValue("id");

			parseTestGroupSelections(groupName+"/"+groupId, testGroupElement, osId, productId);
		}

		List testList = root.getChildren("test");
		for (int testCount=0;testCount<testList.size();testCount++)
		{
			Element testElement = (Element)testList.get(testCount);
			String testId = testElement.getAttributeValue("id");
			boolean selected = Boolean.valueOf(testElement.getAttributeValue("selected")).booleanValue();
			TestDefinition testDefinition;

			/*
			 * Ensure we have a test definition stored for this selected test
			 * If we don't throw an TestUndefined exception
			 */
			if ((testDefinition = _testRepository.getTestDefinition(groupName, testId))==null)
			{
				throw new TestUndefined(groupName, testId);
			}

			if (selected)
			{
                OSProductCombination ospc = new OSProductCombination(osId, productId);
				testDefinition.addOSProduct( new OSProductCombination(osId, productId) );
                _productsInUse.add(productId);

                if ( !_combinations.contains(ospc) )
                {
                    _combinations.add(ospc);
                }
			}
		}
	}

	public int compare(Object o1, Object o2)
	{
		TestDefinition t1 = (TestDefinition)o1;
		TestDefinition t2 = (TestDefinition)o2;

        if ( t1.getNumberOfNodesRequired() < t2.getNumberOfNodesRequired() )
		{
			return -1;
		}
		else
		{
			if ( t1.getNumberOfNodesRequired() == t2.getNumberOfNodesRequired() )
			{
				return 0;
			}
			else
				return 1;
		}
	}

	/**
	 * Generates a unique run id. this id. is then used when referencing the web logger.
	 * This method also logs the start of the test run with the web logger.
	 *
	 * @param definitionURL The URL of a test definition XML file.
	 * @param selectionURL The URL of a test selection XML file.
	 * @return The run UID for this run generated by the web logger.
	 */
	static RunUID generateRunUniqueId(URL definitionURL, URL selectionURL, String softwareVersion, String distributionList) throws Exception
	{
		return Coordinator.getLoggingService().initiateTestRun(definitionURL.toExternalForm(),
											   selectionURL.toExternalForm(),
											   softwareVersion,
											   distributionList);
	}

	public synchronized void testResult(RunListElement element, boolean failed)
	{
		_managers.remove(element);

        if ( _logger.isInfoEnabled() )
        {
		    _logger.info("Test '"+element.getTest().getFullId()+"' finished, failed? = "+failed);
        }

		_testsRunning--;

		_runInfo.setCurrentStatus("Active Test Managers: "+_testsRunning+" | Tests Remaining: "+_testsToRun+" of "+_runInfo.getTotalNumberOfTests());
		_runInfo.setNumberOfTestsRemaining(_testsToRun);

		if (failed)
		{
			if ( element.getPermutation().incrementRetryCount() < Coordinator.getMaximumNumberOfRetries() )
			{
                if ( _logger.isInfoEnabled() )
                {
				    _logger.info("Requeing test '"+element.getTest().getFullId()+"' for later execution");
                }
				_runList.add(element);
			}
			else
			{
				_logger.error("Maximum number of retries reached for test '"+element.getTest().getFullId()+" this test could not be run");
				// Mark this as not failed so that the permutation can be marked complete
				failed = false;
			}
		}


		/** If there are no tests running make sure that we run any tests waiting to be run **/
		if ( _testsRunning == 0 )
		{
			runNextTest();
		}

		if (!failed)
		{
			element.getPermutation().setCompleted();
			_testsToRun--;
		}

		try
		{
			AuditLogEntry logEntry = (AuditLogEntry)_auditEntryMap.get(element);
			_auditLog.addLog(logEntry);
            _auditEntryMap.remove(element);
		}
		catch (java.io.IOException e)
		{
			_logger.error("Failed to add log entry",e);
		}

		/** Decrement the number of tests running **/
		if ( ( _testsToRun == 0 ) || ( _stopped && _testsRunning == 0) )
		{
			testRunComplete();
		}
/*		else  Tests are started now when the node manager notifies the run manager that nodes are free
		{
			if ( !_stopped )
			{
				_logger.log("Number of tests still to run: "+_testsToRun);
				runNextTest();
			}
		}*/
	}

    public boolean isProductInUse(String productId)
    {
        return _productsInUse.contains(productId);
    }

    public long getRunManagerId()
    {
        return _runManagerId;
    }

    static
    {
        try
        {
            _nodeManager = new NodeManager();
        }
        catch (Exception e)
        {
            throw new ExceptionInInitializerError("Failed to create NodeManager: "+e);
        }
    }
}
