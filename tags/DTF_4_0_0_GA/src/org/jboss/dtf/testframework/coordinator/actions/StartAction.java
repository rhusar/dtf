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
 * $Id: StartAction.java 170 2008-03-25 18:59:26Z jhalliday $
 */

package org.jboss.dtf.testframework.coordinator.actions;

import org.jboss.dtf.testframework.coordinator.NoAssociatedData;
import org.jdom.Element;

import java.util.List;

public class StartAction extends PerformAction
{
    private final static String START_TASK_NAME = "start_task";

    protected String    _taskId = null;
    protected String    _runtimeId = null;
    protected String    _location = null;
    protected String[]  _parameterList = null;
    protected String[]  _jvmParameterList = null;
    protected String    _nameList = null;

    public StartAction(String id, String runtimeId, String location,
                       String[] parameterList, String[] jvmParameterList, String nameList)
    {
		super(id, runtimeId, location, parameterList, jvmParameterList, nameList);

        _taskId = id;
        _location = location;
        _parameterList = parameterList;
        _jvmParameterList = jvmParameterList;
        _nameList = nameList;
        _runtimeId = runtimeId;
        _actionType = START_TASK;
    }

    public String toString()
    {
        return("[Start Action TaskId:"+_taskId+", Location:"+_location+", NameList:"+_nameList+", RuntimeId:"+_runtimeId+"]");
    }

    /**
     * Create an XML element which represents this action
     *
     * @return The XML element representing this action.
     */
    public Element serializeToXML() throws NoAssociatedData
    {
        Element actionElement = new Element("start_task");
        actionElement.setAttribute("id", getAssociatedTaskId());
        actionElement.setAttribute("runtimeid", getAssociatedRuntimeTaskId());

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

    public static StartAction getStartAction(Element actionElement)
    {
        StartAction action = null;
        String actionName = actionElement.getName();

        // If the action is a perform task action
        if (actionName.equals(START_TASK_NAME))
        {
            int 	arraySize = 0;
            String 	id = actionElement.getAttributeValue("id"),
                    location = actionElement.getAttributeValue("location"),
                    nameList = actionElement.getAttributeValue("name_list"),
                    runtimeId = actionElement.getAttributeValue("runtime_id"),
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
            arraySize = (singleParameters!=null) ? 1 : 0;
            List parameterElements = actionElement.getChildren("param");
            String parameters[] = new String[arraySize + parameterElements.size()];

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

            action = new StartAction(id,runtimeId,location,parameters,jvmSubParameters,nameList);
        }

        return action;
    }

}
