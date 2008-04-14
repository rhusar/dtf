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
package org.jboss.dtf.testframework.productrepository;

import org.jdom.Element;

import java.util.List;
import java.util.Hashtable;
import java.util.Enumeration;
import java.io.Serializable;

/*
 * Copyright (C) 1998, 1999, 2000, 2001, 2002, 2003
 *
 * Arjuna Technology Ltd.
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: TaskRunnerConfiguration.java 170 2008-03-25 18:59:26Z jhalliday $
 */

public class TaskRunnerConfiguration implements Serializable
{
	private final static String TASK_RUNNER_CONFIGURATION_NODE_NAME = "task-runner";
	private final static String TASK_RUNNER_NAME_ATTRIBUTE = "name";
	private final static String TASK_RUNNER_CLASSNAME_ATTRIBUTE = "class";
	private final static String TASK_RUNNER_LOG_TO_ATTRIBUTE = "log-to";

	private final static String TASK_RUNNER_PARAMETER_NODE_NAME = "param";
	private final static String TASK_RUNNER_PARAMETER_NAME_ATTRIBUTE = "name";
	private final static String TASK_RUNNER_PARAMETER_VALUE_ATTRIBUTE = "value";

	private String		_name = "";
	private String		_classname = "";
    private String		_logTo = "";
	private Hashtable   _parameters = new Hashtable();

	public void setName(String name)
	{
		_name = name;
	}

	public void setClassname(String classname)
	{
		_classname = classname;
	}

	public void setLogTo(String logTo)
	{
		_logTo = logTo;
	}

	public String getName()
	{
		return _name;
	}

	public String getClassname()
	{
		return _classname;
	}

	public String getLogTo()
	{
		return _logTo;
	}

	public String[] getParameterNames()
	{
		String[] parameterNames = new String[_parameters.size()];
		_parameters.keySet().toArray(parameterNames);

		return parameterNames;
	}

	public void setParameter(String name, String value)
	{
		_parameters.put(name, value);
	}

	public Hashtable getParameters()
	{
		return (Hashtable)_parameters.clone();
	}

	public void deleteParameter(String selectedParameter)
	{
		_parameters.remove(selectedParameter);
	}

	public String getParameter(String name)
	{
		return (String)_parameters.get(name);
	}

	public Element serializeXML()
	{
		Element taskRunnerElement = new Element( TASK_RUNNER_CONFIGURATION_NODE_NAME );

		taskRunnerElement.setAttribute( TASK_RUNNER_NAME_ATTRIBUTE, getName() );
		taskRunnerElement.setAttribute( TASK_RUNNER_CLASSNAME_ATTRIBUTE, getClassname() );
		taskRunnerElement.setAttribute( TASK_RUNNER_LOG_TO_ATTRIBUTE, getLogTo() );

		Enumeration e = _parameters.keys();

		while (e.hasMoreElements())
		{
			String paramName = (String)e.nextElement();

			Element paramElement = new Element( TASK_RUNNER_PARAMETER_NODE_NAME );
			taskRunnerElement.addContent(paramElement);
			paramElement.setAttribute( TASK_RUNNER_PARAMETER_NAME_ATTRIBUTE, paramName );
			paramElement.setAttribute( TASK_RUNNER_PARAMETER_VALUE_ATTRIBUTE, getParameter(paramName) );
		}

		return taskRunnerElement;
	}

	public static TaskRunnerConfiguration getTaskRunnerConfiguration(Element taskRunnerElement)
	{
		TaskRunnerConfiguration trc = null;

		if ( taskRunnerElement.getName().equals( TASK_RUNNER_CONFIGURATION_NODE_NAME ) )
		{
			trc = new TaskRunnerConfiguration();

			trc.setName( taskRunnerElement.getAttributeValue( TASK_RUNNER_NAME_ATTRIBUTE ) );
			trc.setClassname( taskRunnerElement.getAttributeValue( TASK_RUNNER_CLASSNAME_ATTRIBUTE ) );
			trc.setLogTo( taskRunnerElement.getAttributeValue( TASK_RUNNER_LOG_TO_ATTRIBUTE ));

			List paramNodes = taskRunnerElement.getChildren( TASK_RUNNER_PARAMETER_NODE_NAME );

			for (int count=0;count<paramNodes.size();count++)
			{
				Element paramElement = (Element)paramNodes.get(count);

				String paramName = paramElement.getAttributeValue( TASK_RUNNER_PARAMETER_NAME_ATTRIBUTE );
				String paramValue = paramElement.getAttributeValue( TASK_RUNNER_PARAMETER_VALUE_ATTRIBUTE );

				trc.setParameter(paramName, paramValue);
			}
		}

		return trc;
	}
}
