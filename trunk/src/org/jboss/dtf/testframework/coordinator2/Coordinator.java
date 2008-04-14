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
package org.jboss.dtf.testframework.coordinator2;

import org.jboss.dtf.testframework.coordinator2.runmanager.RunManager;
import org.jboss.dtf.testframework.coordinator2.runmanager.RunListener;
import org.jboss.dtf.testframework.coordinator2.runmanager.CannotStartRunException;
import org.jboss.dtf.testframework.coordinator2.scheduler.Scheduler;
import org.jboss.dtf.testframework.coordinator2.scheduler.ScheduleWhenPossible;
import org.jboss.dtf.testframework.coordinator2.scheduler.SchedulerInterface;
import org.jboss.dtf.testframework.coordinator2.scheduler.ScheduleInformation;
import org.jboss.dtf.testframework.nameservice.NameServiceInterface;
import org.jboss.dtf.testframework.serviceregister.ServiceRegisterInterface;
import org.jboss.dtf.testframework.serviceregister.ServiceRegister;
import org.jboss.dtf.testframework.testnode.RunUID;
import org.jboss.dtf.testframework.utils.logging.LoggingService;
import org.jboss.dtf.testframework.utils.logging.LoggingFactory;
import org.jboss.dtf.testframework.productrepository.ProductRepositoryInterface;
import org.jboss.dtf.testframework.productrepository.ProductRepository;

import java.net.URL;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.RemoteException;
import java.rmi.Naming;
import java.io.File;
import java.util.ArrayList;

import org.jdom.input.DOMBuilder;
import org.jdom.input.SAXBuilder;
import org.jdom.Document;
import org.jdom.Element;
import org.apache.log4j.*;
import org.apache.log4j.helpers.DateLayout;

public class Coordinator extends java.rmi.server.UnicastRemoteObject implements CoordinatorInterface, RunListener
{
	public final static String COORDINATOR_NAME_SERVICE_NAME = "Coordinator";

    private final static String COORDINATOR_VERSION_INFORMATION = "Distributed Testing Framework - Coordinator Service v2.1";

	private final static int DEFAULT_MAX_NUMBER_OF_MANAGERS = 10;
	private final static int DEFAULT_MAX_NUMBER_OF_RETRIES = 5;

	private static NameServiceInterface		_nameService = null;
	private static ServiceRegisterInterface _serviceRegistry = null;
	private static ProductRepositoryInterface _productRepository = null;
	private static LoggingService 			_loggingService = null;
	private static int 						_maxNumberOfManagers = DEFAULT_MAX_NUMBER_OF_MANAGERS;
	private static int 						_maxNumberOfRetries = DEFAULT_MAX_NUMBER_OF_RETRIES;

	private final static int getRMIPort()
	{
		String rmiPort = System.getProperty("rmi.port", ""+Registry.REGISTRY_PORT);

		return Integer.parseInt(rmiPort);
	}

	public static NameServiceInterface getNameService()
	{
		return _nameService;
	}

	public static ServiceRegisterInterface getServiceRegistry()
	{
		return _serviceRegistry;
	}

	public static ProductRepositoryInterface getProductRepository()
	{
		return _productRepository;
	}

	public static int getMaximumNumberOfManagers()
	{
		return _maxNumberOfManagers;
	}

	public static int getMaximumNumberOfRetries()
	{
		return _maxNumberOfRetries;
	}

    private Logger      _logger = Logger.getLogger(this.getClass());
	private Scheduler	_scheduler = null;
    private ArrayList   _runManagers = new ArrayList(_maxNumberOfManagers);

	public Coordinator() throws java.rmi.RemoteException
	{
		super();

        configureLogger();

        if ( _logger.isInfoEnabled() )
        {
            _logger.info(COORDINATOR_VERSION_INFORMATION);
        }

		_scheduler = new Scheduler(this, _productRepository);
		_nameService.rebindReference( Scheduler.SCHEDULER_NAME_SERVICE_NAME, _scheduler);
	}

    public final int getNumberOfManagersInUse()
    {
        synchronized (_runManagers)
        {
            return _runManagers.size();
        }
    }

    public final void addRunManager(RunManager r)
    {
        synchronized (_runManagers)
        {
            _runManagers.add(r);
        }
    }

    public boolean isRunInProgress(RunUID runId) throws java.rmi.RemoteException
    {
        synchronized(_runManagers)
        {
            for (int count=0;count<_runManagers.size();count++)
            {
                RunManager rm = (RunManager)_runManagers.get(count);

                if ( rm.getRunId().equals(runId) )
                {
                    return true;
                }
            }
        }

        return false;
    }

    public final RunUID isProductInUse(String productId)
    {
        synchronized(_runManagers)
        {
            for (int count=0;count<_runManagers.size();count++)
            {
                RunManager rm = (RunManager)_runManagers.get(count);

                if ( rm.isProductInUse(productId) )
                {
                    return rm.getRunId();
                }
            }
        }

        return null;
    }

	/**
	 * Restart the coordinator
	 *
	 * @throws RemoteException
	 */
	public void restart() throws RemoteException
	{
		if ( _logger.isInfoEnabled() )
		{
			_logger.info("Shutting down coordinator for restart - stopping all runmanagers");
		}

		for (int count=0;count<_runManagers.size();count++)
		{
			RunManager runManager = (RunManager)_runManagers.get(count);

			runManager.stopRun(false);
		}

		exit(true);
	}

	/**
	 * Shutdown the coordinator
	 *
	 * @throws RemoteException
	 */
	public void shutdown() throws RemoteException
	{
		if ( _logger.isInfoEnabled() )
		{
			_logger.info("Shutting down coordinator - stopping all runmanagers");
		}

		for (int count=0;count<_runManagers.size();count++)
		{
			RunManager runManager = (RunManager)_runManagers.get(count);

			runManager.stopRun(false);
		}

		exit(false);
	}

	private void exit(final boolean restart)
	{
		new Thread() {

			public void run()
			{
				System.out.println("Exiting "+(restart ? "and restarting" : ""));

				if (restart)
					System.exit(1);
				else
					System.exit(2);
			}

		}.start();
	}

	/**
	 * Stops the currently active run.
	 *
	 * @param waitForTestToComplete If this parameter is true then the run will stop once
	 * the current test has finished.  If false then the run will stop immediately.
	 *
	 * @throws java.rmi.RemoteException
	 * @throws CoordinatorIdleException If the coordinator isn't currently running any
	 * tests then this exception will be thrown.
	 */
	public boolean stopRun(boolean waitForTestToComplete, RunUID runId) throws CoordinatorIdleException, java.rmi.RemoteException
	{
        RunManager runManager = null;

        if ( _logger.isInfoEnabled() )
        {
            _logger.info("A request to stop run "+runId+" has been received"+ (waitForTestToComplete ? " and waiting for test to complete" : "") );
        }

        for (int count=0;count<_runManagers.size();count++)
        {
            RunManager rm = (RunManager)_runManagers.get(count);

            if ( rm.getRunId().equals(runId) )
            {
                runManager = rm;
                break;
            }
        }

		if ( runManager != null )
		{
            runManager.stopRun(waitForTestToComplete);

			return true;
		}

		throw new CoordinatorIdleException("The coordinator is not currently running tests");
	}

    public boolean isBusy() throws RemoteException
    {
        if ( _logger.isDebugEnabled() )
        {
            _logger.debug("Coordinator.isBusy() - Number of managers in use "+getNumberOfManagersInUse()+" against maximum number of managers "+_maxNumberOfManagers);
        }

        return getNumberOfManagersInUse() == _maxNumberOfManagers;
    }

	/**
	 * Start a test run.  The coordinator will run the tests defined in
	 * testDefsURL and selected within the testSelections file.
	 *
	 * @param testDefsURL A URL to the test definitions file.
	 * @param testSelectionsURL A URL to the test selections file.
	 * @param distributionList The email distribution list.
	 * @param softwareVersion The software version to log against.
	 * @throws RemoteException
	 */
	public void run( URL testDefsURL,
					 URL testSelectionsURL,
					 String distributionList,
					 String softwareVersion,
					 boolean waitToComplete) throws RemoteException, CoordinatorBusyException, CannotStartRunException
	{
        if ( _logger.isInfoEnabled() )
        {
            _logger.info("Request received to schedule run '"+testDefsURL.toExternalForm()+"','"+testSelectionsURL.toExternalForm()+"','"+distributionList+"','"+softwareVersion+"'");
        }

		_scheduler.schedule(this, new ScheduleWhenPossible(testDefsURL,testSelectionsURL,distributionList,softwareVersion,null));

        if ( getNumberOfManagersInUse() == _maxNumberOfManagers )
        {
            throw new CoordinatorBusyException("Maximum number of run managers already in use, run scheduled");
        }
	}

	public RunUID initiateRun(  ScheduleInformation schedule,
								boolean waitToComplete ) throws RemoteException, CoordinatorBusyException, CannotStartRunException
	{
		RunUID runId = null;

        if ( _logger.isInfoEnabled() )
        {
            _logger.info("Request received to initiate run "+schedule.toString()+" "+(waitToComplete ? "waiting to complete":"") + "("+getNumberOfManagersInUse()+"/"+_maxNumberOfManagers+" managers)");
        }

        if ( getNumberOfManagersInUse() == _maxNumberOfManagers )
        {
            throw new CoordinatorBusyException("Maximum number of run managers already in use");
        }

		try
		{
			_scheduler.notifyStatus(true);

            if ( _logger.isDebugEnabled() )
            {
                _logger.debug("Creating run manager "+schedule.toString());
            }

			RunManager runManager = new RunManager( schedule, this );

            addRunManager(runManager);

			runId = runManager.getRunId();

            if ( _logger.isInfoEnabled() )
            {
                _logger.debug("Created run manager "+schedule.toString()+" given run manager "+runId);
            }

			if ( waitToComplete )
			{
				try
				{
                    if ( _logger.isDebugEnabled() )
                    {
                        _logger.debug("Waiting for run manager to complete "+runId);
                    }

					synchronized (runManager)
					{
						runManager.wait();
					}
				}
				catch (InterruptedException e)
				{
				}
			}
		}
        catch (CannotStartRunException e)
        {
            _logger.error("Unable to start run", e);
            throw e;
        }
		catch (Exception e)
		{
            _logger.error("Unable to start run", e);
			throw new CannotStartRunException("Failed to initiate test run: "+e);
		}

		return runId;
	}

	public SchedulerInterface getScheduler() throws RemoteException
	{
		return _scheduler;
	}

	/**
	 * Retrieve information about the current run in progress.
	 *
	 * @return
	 * @throws RemoteException
	 * @throws CoordinatorIdleException If the coordinator is not currently running any tests.
	 */
	public RunInformation[] getCurrentRunInformation() throws CoordinatorIdleException, RemoteException
	{
		if ( getNumberOfManagersInUse() == 0 )
		{
			throw new CoordinatorIdleException("Coordinator is currently idle");
		}

        RunInformation[] runInformation = null;

        synchronized (_runManagers)
        {
            runInformation = new RunInformation[getNumberOfManagersInUse()];

            for (int count=0;count<_runManagers.size();count++)
            {
                RunManager rm = (RunManager)_runManagers.get(count);

                runInformation[count] = rm.getRunInformation();
            }
        }

		return runInformation;
	}

	/**
	 * Retrieves the configuration from the coordinator's configuration file
	 */
	public static void RetrieveConfiguration()
	{
		System.out.print("Retrieving configuration... ");
		try
		{
			SAXBuilder xmlBuilder = new SAXBuilder();
			Document doc = xmlBuilder.build(new File("coordinator.xml"));
			/*
			 * Retrieve root element
			 */
			Element root = doc.getRootElement();
			Element configurationElement = root.getChild("configuration");

			/*
			 * Retrieve the configuration for this Coordinator
			 */
			Element numManagers = configurationElement.getChild("number_of_managers");
			if (numManagers != null)
			{
				_maxNumberOfManagers = Integer.parseInt(numManagers.getAttributeValue("value"));
			}
			Element maxNumRetries = configurationElement.getChild("max_test_retries");
			if (maxNumRetries != null)
			{
				_maxNumberOfRetries = Integer.parseInt(maxNumRetries.getAttributeValue("value"));
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			System.err.println("\nFailed while reading configuration");
			System.exit(0);
		}

		System.out.println("Complete");
	}

    private void configureLogger()
    {
        try
        {
            Logger.getRootLogger().addAppender(new FileAppender(new PatternLayout("%d [%t] %-5p %c %x - %m\n"), "coordinator.log"));
            Logger.getRootLogger().addAppender(new ConsoleAppender(new PatternLayout("%d [%t] %-5p %c %x - %m\n")));
        }
        catch (Exception e)
        {
            System.err.println("Failed to configure logger: "+e);
            System.exit(0);
        }
    }

    /**
     * This method is called when the run manager has completed its task.
     *
     * @param runManager The run manager that has completed
     */
    public void runComplete(RunManager runManager)
    {
        if ( _logger.isInfoEnabled() )
        {
            _logger.info("Run complete signal received from run manager ("+runManager.getRunId()+")");
        }

		synchronized(runManager)
		{
            _runManagers.remove(runManager);
            System.out.println("Notifying run complete");
			runManager.notifyAll();
            System.out.println("Run complete notification given");
			_scheduler.notifyStatus(false);
		}
	}

	public static LoggingService getLoggingService()
	{
		return _loggingService;
	}

	public static void initialiseRegistry()
	{
		try
		{
			int rmiPort = getRMIPort();
			System.out.println("Creating RMI registry on port "+rmiPort);
			LocateRegistry.createRegistry(rmiPort);
		}
		catch (RemoteException ex)
		{
		}
	}

	public static void main(String[] args)
	{
		System.out.println(COORDINATOR_VERSION_INFORMATION);

		if ( args.length < 1 )
		{
			System.out.println("Usage: org.jboss.dtf.testframework.Coordinator <uri://NameService> {-gui}");
		}
        else
		{
			/**
			 * Initialise the RMI registry
			 */
			initialiseRegistry();

			try
			{
				_nameService = (NameServiceInterface)Naming.lookup(args[0]);
				_serviceRegistry = (ServiceRegisterInterface)_nameService.lookup( ServiceRegister.SERVICE_REGISTER_NAME_SERVICE_ENTRY );
				_productRepository = (ProductRepositoryInterface)_nameService.lookup( ProductRepository.PRODUCT_REPOSITORY_NAMESERVICE_NAME );
				_loggingService = LoggingFactory.getDefaultLogger();

				RetrieveConfiguration();

				Coordinator coord = new Coordinator();
				_nameService.rebindReference( COORDINATOR_NAME_SERVICE_NAME, coord);
				System.out.println("Ready");
			}
			catch (Exception e)
			{
				System.err.println("Failed to lookup nameservice:" +e);
				e.printStackTrace(System.err);
			}
		}
	}
}
