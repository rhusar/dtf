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
 * $Id: PerformAction.java 170 2008-03-25 18:59:26Z jhalliday $
 */

package org.jboss.dtf.testframework.coordinator.actions;

import org.jboss.dtf.testframework.coordinator.Action;
import org.jboss.dtf.testframework.coordinator.NoAssociatedData;
import org.jdom.Element;

import java.util.List;

public class PerformAction extends Action
{
    private final static String PERFORM_TASK_NAME = "perform_task";

    protected String    _taskId = null;
    protected String    _runtimeId = null;
    protected String    _location = null;
    protected String[]  _parameterList = null;
    protected String[]  _jvmParameterList = null;
    protected String    _nameList = null;

    public PerformAction(String id, String runtimeId, String location, String[] parameterList, String[] jvmParameterList, String nameList)
    {
        _taskId = id;
        _runtimeId = runtimeId;
        _location = location;
        _parameterList = parameterList;
        _jvmParameterList = jvmParameterList;
        _nameList = nameList;
        _actionType = PERFORM_TASK;
    }

    public String toString()
    {
        return("[Perform Action TaskId:"+_taskId+", RuntimeId:"+_runtimeId+", Location:"+_location+", NameList:"+_nameList+"]");
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
        return _taskId;
    }

	/**
	 * Sets the task id. associated with this action.
	 *
	 * @param taskId The task id to associate with this task.
	 */
	public void setAssociatedTaskId(String taskId)
	{
		_taskId = taskId;
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
        return _location;
    }

	/**
	 * Sets the location value associated with this action.
	 *
	 * @param location The associated location value
	 */
	public void setLocation(String location)
	{
		_location = location;
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
        return _parameterList;
    }

	/**
	 * Set the parameters to be passed to this task.
	 *
	 * @param parameters The parameters to be passed to this task.
	 */
	public void setParameterList(String[] parameters)
	{
		_parameterList = parameters;
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
        return _jvmParameterList;
    }

	/**
	 * Sets the JVM parameters to be passed to this task.
	 *
	 * @param parameters The JVM parameters to be passed to this task.
	 */
	public void setJVMParameterList(String[] parameters)
	{
		_jvmParameterList = parameters;
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

	/**
	 * Set the runtime id associated with this task.
	 *
	 * @param runtimeId The runtime id. to set.
	 */
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
        return _nameList;
    }

    /**
     * Create an XML element which represents this action
     *
     * @return The XML element representing this action.
     */
    public Element serializeToXML() throws NoAssociatedData
    {
        Element actionElement = new Element("perform_task");
        actionElement.setAttribute("id", getAssociatedTaskId());

        if (_location != null)
        {
            actionElement.setAttribute("location", _location);
        }

        if (_nameList != null)
        {
            actionElement.setAttribute("name_list", _nameList);
        }

        String[] params = getParameterList();
        for (int count=0;count<params.length;count++)
        {
            Element param = new Element("param");
            param.setText(params[count]);

            actionElement.addContent(param);
        }

        String[] jvmParams = getJVMParameterList();
        for (int count=0;count<jvmParams.length;count++)
        {
            Element param = new Element("jvm_param");
            param.setText(jvmParams[count]);

            actionElement.addContent(param);
        }

        return(actionElement);
    }

    public static PerformAction getPerformAction(Element actionElement)
    {
        PerformAction action = null;
        String actionName = actionElement.getName();

        // If the action is a perform task action
        if (actionName.equals(PERFORM_TASK_NAME))
        {
            int 	arraySize = 0;
            String 	id = actionElement.getAttributeValue("id"),
                    runtimeId = actionElement.getAttributeValue("runtime_id"),
                    location = actionElement.getAttributeValue("location"),
                    nameList = actionElement.getAttributeValue("name_list"),
                    jvmParameters = actionElement.getAttributeValue("jvm_parameters");
            String  singleParameters = actionElement.getAttributeValue("parameters");
            List jvmParameterElements = actionElement.getChildren("jvm_param");
            String[] jvmSubParameters = new String[(jvmParameters==null)?jvmParameterElements.size():jvmParameterElements.size()+1];

            if (jvmParameters != null)
            {
                jvmSubParameters[0] = jvmParameters;
                arraySize = 1;
            }

            for (int paramCount=0;paramCount<jvmParameterElements.size();paramCount++)
            {
                jvmSubParameters[arraySize + paramCount] = ((Element)jvmParameterElements.get(paramCount)).getText().trim();
            }

            /*
             * If the parameters attribute was specified ensure the parameters array
             * is big enough to hold it
             */
            arraySize = (singleParameters!=null)?1:0;
            List parameterElements = actionElement.getChildren("param");
            String[] parameters = new String[arraySize + parameterElements.size()];
            /*
             * If the parameters attribute was specified add it to the parameters array
             */
            if (singleParameters!=null)
            {
                parameters[0] = singleParameters.trim();
            }
            for (int parameterCount=0;parameterCount<parameterElements.size();parameterCount++)
            {
                Element parameterElement = (Element)parameterElements.get(parameterCount);
                parameters[arraySize + parameterCount] = parameterElement.getText().trim();
            }

            action = new PerformAction(id,runtimeId,location,parameters,jvmSubParameters,nameList);
        }

        return action;
    }
}
