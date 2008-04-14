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
 * $Id: TerminateAction.java 170 2008-03-25 18:59:26Z jhalliday $
 */

package org.jboss.dtf.testframework.coordinator.actions;

import org.jboss.dtf.testframework.coordinator.Action;
import org.jboss.dtf.testframework.coordinator.NoAssociatedData;
import org.jdom.Element;

public class TerminateAction extends Action
{
    public static final String TERMINATE_TASK_NAME = "terminate_task";

    protected String    _runtimeId = null;

    public TerminateAction(String runtimeId)
    {
        _runtimeId = runtimeId;
        _actionType = TERMINATE_TASK;
    }

    /**
     * Retrieves the task id. associated with this action, if the action
     * type doesn't have an associated task id. an exception is thrown.
     *
     * @return The associated task id
     * @throws NoAssociatedData Thrown if there is no associated task id.
     */
    public String getAssociatedTaskId() throws NoAssociatedData
    {
        throw new NoAssociatedData();
    }

    /**
     * Retrieves the location value associated with this action, if the action
     * type doesn't have an associated location an exception is thrown.
     *
     * @return The associated location value
     * @throws NoAssociatedData Thrown if there is no associated task id.
     */
    public String getLocation() throws NoAssociatedData
    {
        throw new NoAssociatedData();
    }

    /**
     * Retrieves the parameters to be passed to this action, if no parameters are
     * associated with this action then an exception is thrown.
     *
     * @return The parameters to be passed to this action
     * @throws NoAssociatedData Thrown if there are no parameters for this action.
     */
    public String[] getParameterList() throws NoAssociatedData
    {
        throw new NoAssociatedData();
    }

    /**
     * Retrieves the parameters to be passed to the JVM that executes this action, if no parameters are
     * associated with this action then an exception is thrown.
     *
     * @return The parameters to be passed to the JVM
     * @throws NoAssociatedData Thrown if there are no parameters to pass to the JVM.
     */
    public String[] getJVMParameterList() throws NoAssociatedData
    {
        throw new NoAssociatedData();
    }

    /**
     * Retrieves the associated runtime task id., if one doesn't exist an
     * exception is thrown.
     *
     * @return The associated runtime task id.
     * @throws NoAssociatedData Thrown if there is no associated runtime task id.
     */
    public String getAssociatedRuntimeTaskId() throws NoAssociatedData
    {
        return _runtimeId;
    }

	public void setAssociatedRuntimeTaskId(String runtimeId)
	{
		_runtimeId = runtimeId;
	}

    /**
     * Retrieves the name list for this action, throws an exception if no
     * name list is associated with this action.
     *
     * @return The name list associated with this action.
     * @throws NoAssociatedData Thrown if there is no associated name list.
     */
    public String getAssociatedNameList() throws NoAssociatedData
    {
        throw new NoAssociatedData();
    }

    public String toString()
    {
        return("[Terminate Action RuntimeId:"+_runtimeId+"]");
    }

    /**
     * Create an XML element which represents this action
     *
     * @return The XML element representing this action.
     */
    public Element serializeToXML() throws NoAssociatedData
    {
        Element actionElement = new Element("terminate_task");
        actionElement.setAttribute("runtimeid", getAssociatedRuntimeTaskId());

        return(actionElement);
    }

    public static TerminateAction getTerminateAction(Element actionElement)
    {
        TerminateAction action = null;
        String actionName = actionElement.getName();

        // If the action is a wait for task action
        if (actionName.equals(TERMINATE_TASK_NAME))
        {
            String runtimeId = actionElement.getAttributeValue("runtime_id");

            action = new TerminateAction(runtimeId);
        }

        return action;
    }

}
