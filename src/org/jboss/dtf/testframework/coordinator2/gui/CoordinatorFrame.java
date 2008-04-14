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
 * $Id: CoordinatorFrame.java 170 2008-03-25 18:59:26Z jhalliday $
 */
package org.jboss.dtf.testframework.coordinator2.gui;

import org.jboss.dtf.testframework.coordinator2.logger.LogHandler;
import org.jboss.dtf.testframework.coordinator2.logger.Logger;
import org.jboss.dtf.testframework.coordinator2.logger.LoggerInitialisationException;

import javax.swing.*;
import java.awt.*;
import java.util.Hashtable;

public class CoordinatorFrame extends JFrame implements LogHandler
{
	private final static String COORDINATOR_FRAME_TITLE = "Coordinator Log";

	private static JTabbedPane		_tabPane = null;

	private Hashtable		_loggers = new Hashtable();

	public CoordinatorFrame()
	{
		super(COORDINATOR_FRAME_TITLE);

		populateFrame();

		pack();
		show();
	}

	private void populateFrame()
	{
		/**
		 * Create a tabbed pane with one tab for the coordinator logs
		 */
        _tabPane = new JTabbedPane();
		_tabPane.setPreferredSize(new Dimension(400,300));

		this.getContentPane().add(_tabPane);
	}

	public Logger getLogger(String name)
	{
		Logger logger = (Logger)_loggers.get(name);

		try
		{
			if ( logger == null )
			{
				JTextArea loggerArea = createNewLoggerPane(name);
				logger = new GUILogger(loggerArea);

				logger.initialise(name);

				_loggers.put(name, logger);
			}
		}
		catch (LoggerInitialisationException e)
		{
			System.err.println("An error occurred while creating logger '"+name+"': "+e);
		}
		return logger;
	}

	private JTextArea createNewLoggerPane(String name)
	{
		JTextArea coordinatorLog = new JTextArea();
		JScrollPane coordinatorPanel = new JScrollPane(coordinatorLog);
		_tabPane.add( name, coordinatorPanel );

		return coordinatorLog;
	}

	public static void closeLoggerPane(String name)
	{
		for (int count=0;count<_tabPane.getTabCount();count++)
		{
			if ( _tabPane.getTitleAt(count).equals(name) )
			{
				_tabPane.removeTabAt(count);
				break;
			}
		}
	}
}
