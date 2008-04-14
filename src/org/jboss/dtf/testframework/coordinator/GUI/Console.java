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
 * $Id: Console.java 170 2008-03-25 18:59:26Z jhalliday $
 */

package org.jboss.dtf.testframework.coordinator.GUI;

import javax.swing.*;
import java.awt.*;

public class Console extends JFrame
{
    protected JTextArea _consoleText = null;

    public Console()
    {
        this.setTitle("Coordinator Console");

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        JScrollPane scroller;

        this.setLocation(0, (int)screenSize.getHeight() - 200);

        this.getRootPane().setLayout(new BorderLayout());
        this.getRootPane().add(scroller = new JScrollPane(_consoleText = new JTextArea("Coordinator Console")));
        this.getRootPane().setPreferredSize(new Dimension((int)screenSize.getWidth(),150));
        scroller.setPreferredSize(this.getRootPane().getVisibleRect().getSize());
        _consoleText.setAutoscrolls(true);
        this.pack();
    }

    public void addText(String text)
    {
        _consoleText.append(text);
        _consoleText.setCaretPosition(_consoleText.getText().length());
    }

    public void addTextLine(String text)
    {
        addText(text+"\n");
    }
}
