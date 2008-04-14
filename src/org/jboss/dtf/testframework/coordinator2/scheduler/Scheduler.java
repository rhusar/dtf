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
 * Copyright (C) 1998, 1999, 2000, 2001, 2002
 *
 * Arjuna Technology Ltd.
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: Scheduler.java 170 2008-03-25 18:59:26Z jhalliday $
 */
package org.jboss.dtf.testframework.coordinator2.scheduler;

import org.jboss.dtf.testframework.coordinator2.Coordinator;
import org.jboss.dtf.testframework.coordinator2.CoordinatorBusyException;
import org.jboss.dtf.testframework.coordinator2.CoordinatorInterface;
import org.jboss.dtf.testframework.coordinator2.DeployInformation;
import org.jboss.dtf.testframework.coordinator2.runmanager.CannotStartRunException;
import org.jboss.dtf.testframework.productrepository.ProductRepositoryInterface;
import org.jboss.dtf.testframework.testnode.RunUID;

import java.util.LinkedList;
import java.rmi.RemoteException;
import java.io.*;
import java.net.URL;

import org.apache.log4j.Logger;

/**
 * This class manages the scheduling of test runs.
 *
 * @author Richard A. Begg
 */
public class Scheduler extends java.rmi.server.UnicastRemoteObject implements SchedulerInterface, Runnable
{
	public final static String SCHEDULER_NAME_SERVICE_NAME = "Scheduler";

	private final static String SCHEDULE_FILENAME = "schedule.dat";
	private final static long   SCHEDULER_POLL_TIME = 5000;

	private byte						_localUniqueId = 0;
	private Coordinator 				_coordinator = null;
    private LinkedList					_schedule = null;
	private Thread						_daemon = null;
	private ProductRepositoryInterface  _productRepository = null;
    private Logger                      _logger = Logger.getLogger(this.getClass());

	public Scheduler(Coordinator coordinator, ProductRepositoryInterface productRepository) throws java.rmi.RemoteException
	{
		super();

		_coordinator = coordinator;
		_productRepository = productRepository;

		initialiseSchedule();

        if ( _logger.isInfoEnabled() )
        {
            _logger.info("Scheduler initialisation complete - daemon started");
        }

		_daemon = new Thread(this);
		_daemon.setDaemon(true);
		_daemon.start();
	}

	private void initialiseSchedule()
	{
		try
		{
			File scheduleFile = new File( SCHEDULE_FILENAME );

			if ( scheduleFile.exists() )
			{
                if ( _logger.isDebugEnabled() )
                {
                    _logger.info("Schedule already exists - reading");
                }

				ObjectInputStream in = new ObjectInputStream( new FileInputStream( SCHEDULE_FILENAME ) );
				_schedule = (LinkedList)in.readObject();
				in.close();

				for (int count=0;count<_schedule.size();count++)
				{
					ScheduleInformation s = (ScheduleInformation)_schedule.get(count);
					s.setCoordinator(_coordinator);
				}
			}
			else
			{
                if ( _logger.isDebugEnabled() )
                {
                    _logger.info("Schedule does not exist - creating new schedule");
                }

				_schedule = new LinkedList();
			}
		}
		catch (Exception e)
		{
			_logger.error("Failed to read schedule",e);
		}
	}

	private void persistSchedule()
	{
		try
		{
            if ( _logger.isInfoEnabled() )
            {
                _logger.info("Persisting schedule");
            }

			ObjectOutputStream out = new ObjectOutputStream( new FileOutputStream( SCHEDULE_FILENAME ) );
			out.writeObject(_schedule);
			out.close();
		}
		catch (Exception e)
		{
			_logger.error("Failed to persist schedule", e);
		}
	}

	private void dumpDiary()
	{
		synchronized(_schedule)
		{
			for (int count=0;count<_schedule.size();count++)
			{
                if ( _logger.isInfoEnabled() )
                {
				    _logger.info("["+count+"] : "+ ( _schedule.get(count).toString() ) );
                }
			}
		}
	}

	public void run()
	{
        if ( _logger.isInfoEnabled() )
        {
		    _logger.info("Scheduler daemon started");
        }

		while (true)
		{
			while ( !hasScheduledTests() )
			{
				try
				{
					synchronized(_daemon)
					{
						_daemon.wait(SCHEDULER_POLL_TIME);
					}
				}
				catch (Exception e)
				{
					// Ignore
				}
			}

			dumpDiary();

			ScheduleInformation info = getNextScheduledRun();

            if ( _logger.isInfoEnabled() )
            {
			    _logger.info("Retrieving top of scheduler stack: "+info);
            }

			try
			{
				invokeScheduledRun(info);
			}
			catch (RemoteException e)
			{
				_logger.error("A remote exception was encountered when trying to initiate scheduled test run '"+info.getTestSelectionURL()+"','"+info.getTestDefinitionsURL()+"','"+info+"'", e);

                /** If the scheduled item is finished remove it from the schedule **/
                if ( info.isScheduleFinished() )
                {
                    if ( _logger.isInfoEnabled() )
                    {
                        _logger.info("Scheduled item is complete removing from schedule: "+info);
                    }

                    synchronized(_schedule)
                    {
                        _schedule.remove(info);
                        persistSchedule();
                    }
                }
			}
		}
	}

	private void invokeScheduledRun(ScheduleInformation schedule) throws java.rmi.RemoteException
	{
		try
		{
            if ( _logger.isInfoEnabled() )
            {
			    _logger.info("Initiating scheduled entry '"+schedule+"'");;
            }

			/** Inform the schedule that it has been invoked **/
			schedule.invoke();

			/** If this schedule includes deployment information **/
			if ( schedule.containsDeploymentInformation() )
			{
				DeployInformation di = schedule.getDeployInformation();

				/**
				 * Ensure the product to be deployed against isn't currently in-use
				 * If it is then we can't initiate this scheduled run at this moment
				 * and we'll have to schedule it to finish when the run using this
				 * product is complete.
				 */
				RunUID runUsingProduct = _coordinator.isProductInUse(di.getProductName());

				if ( runUsingProduct != null )
				{
					schedule(_coordinator, new ScheduleOnRunCompletion(runUsingProduct, schedule));

					throw new CannotStartRunException("Cannot start this run at the moment as the product is in use and deployment isn't possible");
				}

				try
				{
					URL url = di.getUrl() != null ? new URL(di.getUrl()) : null;

					/** If url is null then use preset product installer **/
					if ( url == null )
					{
						url = _productRepository.getProductInstaller(di.getProductName());
					}

					if ( _logger.isInfoEnabled() )
					{
						_logger.info("Deploying '"+di.getProductName()+"' using '"+url+"'");
					}

					_productRepository.setProductInstaller(di.getProductName(), url);
				}
				catch (Exception e)
				{
					// Not quite sure what to do with it atm
					_logger.error("Failed to deploy product", e);
				}
			}

			if ( ( schedule.getTestDefinitionsURL() != null ) && ( schedule.getTestSelectionURL() != null ) )
			{
				if ( _logger.isInfoEnabled() )
				{
					_logger.info("Initiating test run '"+schedule.getTestDefinitionsURL()+"','"+schedule.getTestSelectionURL()+"','"+schedule.getDistributionList()+"','"+schedule.getSoftwareVersion()+"'");
				}

				/** Initiate the scheduled run **/
				_coordinator.initiateRun(schedule,
										 false);
			}

			synchronized (_schedule)
			{
				/** If the scheduled item is finished remove it from the schedule **/
				if ( schedule.isScheduleFinished() )
				{
					_schedule.remove(schedule);
					persistSchedule();
				}
			}
		}
		catch (InterruptedException e)
		{
			// The invoke has been interrupted, re-sort schedule and ignore
			synchronized(_schedule)
			{
				sortSchedule();
			}
		}
        catch (CannotStartRunException e)
        {
            /** Could not start run therefore remove it from schedule **/
            synchronized(_schedule)
            {
                _schedule.remove(schedule);
            }
            persistSchedule();
        }
		catch (CoordinatorBusyException e)
		{
            _logger.warn("Coordinator has reported busy - rescheduled for 1 minutes time");

			synchronized (_schedule)
			{
				/** If the scheduled item is finished remove it from the schedule **/
				if ( schedule.isScheduleFinished() )
				{
					_schedule.remove(schedule);
					persistSchedule();
				}
			}

            // Schedule this run again in 1 minutes
            schedule(schedule.getCoordinator(), new ScheduleOneTimeOnly(schedule, 1));
		}
	}

	/**
	 * Called by the coordinator to notify the scheduler when it is
	 * busy and when it is idle.
	 *
	 * @param busy - true when the coordinator is busy.
	 */
	public void notifyStatus(boolean busy)
	{
		if (!busy)
		{
			_daemon.interrupt();
		}
	}

	private ScheduleInformation getNextScheduledRun()
	{
		ScheduleInformation returnValue = null;

		synchronized(_schedule)
		{
        	returnValue = (ScheduleInformation)_schedule.getFirst();
		}

		return returnValue;
	}

	public boolean hasScheduledTests()
    {
     	return _schedule.size() > 0;
    }

	public ScheduleInformation[] getSchedule() throws java.rmi.RemoteException
	{
		ScheduleInformation[] returnArray = null;

		synchronized(_schedule)
		{
			returnArray = new ScheduleInformation[_schedule.size()];
			_schedule.toArray(returnArray);
		}

		return returnArray;
	}

	/**
	 * Adds the given schedule information to the diary.
	 *
	 * @param scheduleInfo
	 * @throws RemoteException
	 */
	public void schedule(CoordinatorInterface coordinator, ScheduleInformation scheduleInfo) throws RemoteException
	{
		scheduleInfo.setCoordinator(coordinator);

		synchronized(_schedule)
		{
            if ( _logger.isInfoEnabled() )
            {
			    _logger.info("Adding schedule run '"+scheduleInfo+"' to schedule");
            }

			scheduleInfo.setUID( (System.currentTimeMillis() << 8) | _localUniqueId++ );
			_schedule.add(scheduleInfo);

			persistSchedule();
			sortSchedule();
		}

		synchronized(_daemon)
		{
			_daemon.notifyAll();
		}

		_daemon.interrupt();
	}

	public boolean unschedule(ScheduleInformation scheduleInfo) throws RemoteException
	{
		boolean returnValue = false;

        if ( _logger.isInfoEnabled() )
        {
            _logger.info("Request received to unschedule '"+scheduleInfo+"'");
        }

		synchronized(_schedule)
		{
			returnValue = _schedule.remove(scheduleInfo);
			persistSchedule();
			sortSchedule();
		}

		return returnValue;
	}

	public boolean unschedule(long scheduleId) throws RemoteException
	{
        if ( _logger.isInfoEnabled() )
        {
            _logger.info("Request received to Unschedule '"+scheduleId+"'");
        }

		for (int count=0;count<_schedule.size();count++)
		{
			ScheduleInformation schedule = (ScheduleInformation)_schedule.get(count);

			if ( schedule.getUID() == scheduleId )
			{
				return unschedule(schedule);
			}
		}

		return false;
	}

	private void sortSchedule()
	{
        if ( _logger.isInfoEnabled() )
        {
		    _logger.info("Sorting schedule (size = "+_schedule.size()+")");
        }

		int startPoint = _schedule.size() - 1;

		for (int count=startPoint;count>0;count--)
		{
			ScheduleInformation current = (ScheduleInformation)_schedule.get(count);
			ScheduleInformation next = (ScheduleInformation)_schedule.get(count - 1);
			boolean swap = false;


			/** If current is when possible and the next isn't then swap them **/
			if ( ( current instanceof ScheduleWhenPossible ) && ( next instanceof ScheduleTimed ) )
			{
				swap = true;
			}
			else
			if ( ( current instanceof ScheduleTimed) && ( next instanceof ScheduleTimed ) )
			{
				swap = ((ScheduleTimed)current).isBefore((ScheduleTimed)next);
			}
			if (swap)
			{
				_schedule.set(count - 1, current);
				_schedule.set(count, next);
			}
		}

        if ( _logger.isInfoEnabled() )
        {
		    _logger.info("Sort complete (size = "+_schedule.size()+")");
        }

	}
}
