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
 * Copyright (C) 2001, 2002,
 *
 * Hewlett-Packard Arjuna Labs,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: UtilityFrame.java 170 2008-03-25 18:59:26Z jhalliday $
 */

package org.jboss.dtf.tools.testcomposer;

import org.jboss.dtf.tools.testcomposer.controls.*;

import javax.swing.*;
import java.awt.*;

public class UtilityFrame extends JInternalFrame implements ComponentSizeChangeListener
{
    protected JPanel    _panel = null;

    public UtilityFrame()
    {
        super("Utility Actions", true, true, false, true);

        setPreferredSize(new Dimension(200,350));
        getContentPane().add( new JScrollPane(_panel = new JPanel()) );
        _panel.setLayout(null);
        _panel.setBackground(Color.white);
        pack();
    }

    public void setupForTask(TaskControl controlSelected)
    {
        int maxWidth = 0;

        if ( (controlSelected instanceof PerformTaskControl) ||
             (controlSelected instanceof StartTaskControl) )
        {
            _panel.removeAll();

            WaitForTaskControl waitTask = new WaitForTaskControl(this);
            waitTask.setRuntimeId( controlSelected.getRuntimeId() );
            waitTask.setLocation( 0, 0 );
            waitTask.setTaskName( controlSelected.getTaskName() );
            waitTask.setExpandable( false );
            _panel.add( waitTask );
            maxWidth = waitTask.getWidth();

            TerminateTaskControl termTask = new TerminateTaskControl(this);
            termTask.setRuntimeId(controlSelected.getRuntimeId());
            termTask.setLocation( 0, 25 );
            termTask.setExpandable(false);
            termTask.setTaskName( controlSelected.getTaskName() );
            _panel.add( termTask );
            maxWidth = (termTask.getWidth() > maxWidth)?termTask.getWidth():maxWidth;

            _panel.setPreferredSize( new Dimension(maxWidth, getHeight()) );
        }

        repaint();
    }

    public void componentSizeHasChanged()
    {
    }
}
