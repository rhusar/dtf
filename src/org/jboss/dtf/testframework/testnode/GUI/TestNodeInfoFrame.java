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
//
// Copyright (C) 2001,
//
// HP Arjuna Labs
// Newcastle upon Tyne,
// Tyne and Wear,
// UK.
//
// $Id: TestNodeInfoFrame.java 170 2008-03-25 18:59:26Z jhalliday $

package org.jboss.dtf.testframework.testnode.GUI;

import org.jboss.dtf.testframework.testnode.TaskRunner;

import java.awt.List;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Button;
import java.util.Hashtable;

public class TestNodeInfoFrame extends Frame
{
	private TestNodeInfoPanel 	_infoPanel;
	private List				_taskList;
	private Hashtable				_taskMap;

	public TestNodeInfoFrame()
	{
		super("TestNode Information");

		this.setLayout(new GridLayout(2,1));
		add(_infoPanel = new TestNodeInfoPanel());
		add(_taskList = new List());

		this.setSize(300,150);
		show();
	}

	public final void setStatus(String text)
	{
		_infoPanel.setStatus(text);
	}

	public final void setTest(String text)
	{
		_infoPanel.setTest(text);
	}

	public final void setTask(String text)
	{
		_infoPanel.setTask(text);
		updateTaskList();
	}

	public final void setTaskMap(Hashtable taskMap)
	{
		_taskMap = taskMap;
	}

	public void updateTaskList()
	{
		if (_taskMap != null)
		{
			Object[] keys = _taskMap.keySet().toArray();

			_taskList.removeAll();

			for (int count=0;count<keys.length;count++)
			{
				String key = (String)keys[count];

				TaskRunner tr = (TaskRunner)_taskMap.get(key);

				if (tr != null)
					_taskList.add(tr.toString());
			}
		}
	}
}
