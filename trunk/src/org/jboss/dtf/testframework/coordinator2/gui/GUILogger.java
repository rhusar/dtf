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
 * $Id: GUILogger.java 170 2008-03-25 18:59:26Z jhalliday $
 */
package org.jboss.dtf.testframework.coordinator2.gui;

import org.jboss.dtf.testframework.coordinator2.logger.Logger;
import org.jboss.dtf.testframework.coordinator2.logger.LoggerInitialisationException;
import org.jboss.dtf.testframework.coordinator2.logger.LoggerException;

import javax.swing.*;

public class GUILogger implements Logger
{
	private JTextArea	_textArea = null;
	private String		_name = null;

	public GUILogger(JTextArea textArea)
	{
		_textArea = textArea;
	}

	public void initialise(String name) throws LoggerInitialisationException
	{
		_name = name;
	}

	public void log(String text)
	{
		_textArea.append("["+_name+"|o]:" + text + '\n');
		_textArea.setCaretPosition(_textArea.getDocument().getLength());
	}

	public void error(String text)
	{
		_textArea.append("["+_name+"|e]:" + text + '\n');
		_textArea.setCaretPosition(_textArea.getDocument().getLength());
	}

	public void close() throws LoggerException
	{
		CoordinatorFrame.closeLoggerPane(_name);
	}
}
