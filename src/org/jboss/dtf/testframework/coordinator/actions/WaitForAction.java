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
 * $Id: WaitForAction.java 170 2008-03-25 18:59:26Z jhalliday $
 */

package org.jboss.dtf.testframework.coordinator.actions;

import org.jdom.Element;
import org.jboss.dtf.testframework.coordinator.NoAssociatedData;

public class WaitForAction extends TerminateAction
{
    private final static String WAIT_FOR_TASK_NAME = "wait_for_task";

    public WaitForAction(String runtimeId)
    {
        super(runtimeId);

        _actionType = WAIT_FOR_TASK;
    }

    public String toString()
    {
        return("[WaitFor Action RuntimeId:"+_runtimeId+"]");
    }

    /**
     * Create an XML element which represents this action
     *
     * @return The XML element representing this action.
     */
    public Element serializeToXML() throws NoAssociatedData
    {
        Element actionElement = new Element("wait_for_task");
        actionElement.setAttribute("runtimeid", getAssociatedRuntimeTaskId());

        return(actionElement);
    }

    public static WaitForAction getWaitForAction(Element actionElement)
    {
        WaitForAction action = null;
        String actionName = actionElement.getName();

        // If the action is a wait for task action
        if (actionName.equals(WAIT_FOR_TASK_NAME))
        {
            String runtimeId = actionElement.getAttributeValue("runtime_id");

            action = new WaitForAction(runtimeId);
        }

        return action;
    }

}
