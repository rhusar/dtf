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
// $Id: TestNodeInfoPanel.java 170 2008-03-25 18:59:26Z jhalliday $

package org.jboss.dtf.testframework.testnode.GUI;

import java.awt.Label;
import java.awt.GridLayout;
import java.awt.Panel;

public class TestNodeInfoPanel extends Panel
{
	Label	_status;
	Label	_testInfo;
	Label	_taskInfo;

	public TestNodeInfoPanel()
	{
		Label label;

		this.setLayout(new GridLayout(3,2));
		add(label = new Label("Status:"));
		label.setAlignment(Label.RIGHT);
		add(_status = new Label());
		_status.setForeground(java.awt.Color.green);
		add(label = new Label("Current Test:"));
		label.setAlignment(Label.RIGHT);
		add(_testInfo = new Label());
		add(label = new Label("Current Task:"));
		label.setAlignment(Label.RIGHT);
		add(_taskInfo = new Label());
	}

	public final void setStatus(String text)
	{
		_status.setText(text);
	}

	public final void setTest(String text)
	{
		_testInfo.setText(text);
	}

	public final void setTask(String text)
	{
		_taskInfo.setText(text);
	}
}
