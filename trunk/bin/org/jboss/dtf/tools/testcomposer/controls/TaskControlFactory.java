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
 * $Id: TaskControlFactory.java 170 2008-03-25 18:59:26Z jhalliday $
 */

package org.jboss.dtf.tools.testcomposer.controls;

import org.jdom.Element;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.jboss.dtf.tools.testcomposer.TaskControlCreationException;

import java.io.ByteArrayInputStream;
import java.io.IOException;

public class TaskControlFactory
{
    public static TaskControl getTaskControl(String data) throws TaskControlCreationException, JDOMException
    {
        System.out.println("Retrieving control for '"+data+"'");
        Document doc = null;
        try {
            doc = new org.jdom.input.SAXBuilder().build(new ByteArrayInputStream(data.getBytes()));
        } catch(IOException e) {
            throw new JDOMException(e.toString());
        }

        return(getTaskControl(doc.getRootElement()));
    }

    public static TaskControl getTaskControl(Element definition) throws TaskControlCreationException
    {
        String type = definition.getAttributeValue("type");

        if (type == null)
            throw new TaskControlCreationException("The dom element does not represent a task control");

        if (type.equals(PerformTaskControl.TypeText))
        {
            return(PerformTaskControl.getTask(definition));
        }

        if (type.equals(StartTaskControl.TypeText))
        {
            return(StartTaskControl.getTask(definition));
        }

        if (type.equals(WaitForTaskControl.TypeText))
        {
            return(WaitForTaskControl.getTask(definition));
        }

        if (type.equals(TerminateTaskControl.TypeText))
        {
            return(TerminateTaskControl.getTask(definition));
        }

        throw new TaskControlCreationException("Unknown task control type '"+type+"'");
    }
}
