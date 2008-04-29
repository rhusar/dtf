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
package org.jboss.dtf.testframework.coordinator2;

import org.jboss.dtf.testframework.coordinator.IncorrectDefinitionException;
import org.jboss.dtf.testframework.utils.OptionParser;
import org.jboss.dtf.testframework.utils.OptionParserException;

import java.util.Hashtable;
import java.util.ArrayList;
import java.util.List;

import org.jdom.Element;

public class TaskDefinition
{
	public final static int EXPECT_NOTHING = 0,
							EXPECT_READY = 1,
							EXPECT_PASS_FAIL = 2,
							EXPECT_SILENT_PASS_FAIL = 3;

	public final static String[] TYPE_STRINGS = {"EXPECT_NOTHING",
												 "EXPECT_READY",
												 "EXPECT_PASS_FAIL",
												 "EXPECT_SILENT_PASS_FAIL"};

	public final static long    OVERRIDE_PARAMETERS = 1, OVERRIDE_JVM_PARAMETERS = 8,
								APPEND_PARAMETERS = 2,   APPEND_JVM_PARAMETERS = 16,
								PREPEND_PARAMETERS = 4,  PREPEND_JVM_PARAMETERS = 32;

	public final static String[] PARAMETER_CONFIG = { "OVERRIDE_PARAMETERS", "OVERRIDE_JVM_PARAMETERS",
													  "APPEND_PARAMETERS", "APPEND_JVM_PARAMETERS",
													  "PREPEND_PARAMETERS", "PREPEND_JVM_PARAMETERS" };

	public final static long[] PARAMETER_CONFIG_VALUE = {   OVERRIDE_PARAMETERS,    OVERRIDE_JVM_PARAMETERS,
															APPEND_PARAMETERS,      APPEND_JVM_PARAMETERS,
															PREPEND_PARAMETERS,     PREPEND_JVM_PARAMETERS };
	private String _id;
	private String _classname;
	private String _classpath;
	private int _timeout = 0;
	private int _type = 0;
	private String _runner = "";
	private Hashtable _runnerParameters;
	private String[] _parameters = null;
	private String[] _jvmParameters = null;
	private long _parameterSetting = OVERRIDE_PARAMETERS | OVERRIDE_JVM_PARAMETERS;

	public TaskDefinition(Element taskElement, int defaultTimeout) throws IncorrectDefinitionException
	{
		_id = taskElement.getAttributeValue("id");
		_classname = taskElement.getAttributeValue("classname");
		_classpath = taskElement.getAttributeValue("classpath");
		String timeoutStr = taskElement.getAttributeValue("timeout");
		_runner = taskElement.getAttributeValue("runner");
		String paramSetting = taskElement.getAttributeValue("parameter_setting");
		_timeout = defaultTimeout;

		List parameters = taskElement.getChildren("param");
		_parameters = new String[parameters.size()];
		for (int paramCount=0;paramCount<parameters.size();paramCount++)
		{
			Element paramElement = (Element)parameters.get(paramCount);
			_parameters[paramCount] = paramElement.getTextTrim();
		}

		parameters = taskElement.getChildren("jvm_param");

		_jvmParameters = new String[parameters.size()];
		for (int paramCount=0;paramCount<parameters.size();paramCount++)
		{
			Element paramElement = (Element)parameters.get(paramCount);
			_jvmParameters[paramCount] = paramElement.getTextTrim();
		}

		/*
		 * Check for task runner attribute, if this doesn't exist
		 * then check for task  runner  element  within the  task
		 * element.  If  that  doesn't  exist throw an exception.
		 */
		if (_runner == null)
		{
			System.out.println("Looking for 'runner' element");
			Element taskRunnerElement = taskElement.getChild("runner");

			if (taskRunnerElement != null)
			{
				_runner = taskRunnerElement.getAttributeValue("name");

				List taskRunnerParameterElements = taskRunnerElement.getChildren("param");

				_runnerParameters = new Hashtable();

				for (int paramCount=0;paramCount<taskRunnerParameterElements.size();paramCount++)
				{
					Element parameterElement = (Element)taskRunnerParameterElements.get(paramCount);
					_runnerParameters.put( parameterElement.getAttributeValue("name"), parameterElement.getAttributeValue("value") );
				}
			}

			if (_runner == null)
			{
				throw new IncorrectDefinitionException("Task runner not defined for task "+_id);
			}
		}

		if (timeoutStr != null)
		{
			_timeout = Integer.parseInt(timeoutStr);
		}

		String type = taskElement.getAttributeValue("type");

		if ( type == null )
		{
			throw new IncorrectDefinitionException("No type attribute specified for task '"+_id+"'");
		}

		for (int count = 0; count < TYPE_STRINGS.length; count++)
			if (type.equalsIgnoreCase(TYPE_STRINGS[count]))
				_type = count;

		if ( (paramSetting != null) && (paramSetting.length() > 0) )
		{
			try
			{
				OptionParser op = new OptionParser(PARAMETER_CONFIG, PARAMETER_CONFIG_VALUE);
				_parameterSetting = op.parse(paramSetting);
			}
			catch (OptionParserException e)
			{
				throw new IncorrectDefinitionException("The task definition for '"+_id+"' has an incorrect parameter setting: "+e);
			}
		}
	}

	public final long getParameterSettings()
	{
		return(_parameterSetting);
	}

	public final String[] getParameters()
	{
		return(_parameters);
	}

	public final String[] getJVMParameters()
	{
		return(_jvmParameters);
	}

	public final String getRunner()
	{
		return (_runner);
	}

	public final Hashtable getRunnerParameters()
	{
		return (_runnerParameters);
	}

	public final String getClasspath()
	{
		return _classpath;
	}

	public final String getId()
	{
		return (_id);
	}

	public final int getType()
	{
		return (_type);
	}

	public final String getTypeText()
	{
		return (TYPE_STRINGS[_type]);
	}

	public final String getClassName()
	{
		return (_classname);
	}

	public final int getTimeout()
	{
		return (_timeout);
	}

	public final String getNames(TestDefinition test)
	{
		String names = "";

		for (int count = 0; count < test.getNamesRequired(); count++)
		{
			names += test.getName(count) + " ";
		}
		/*
		 * Strip trailing spaces
		 */
		return (names.trim());
	}

	public final String getNames(TestDefinition test,
								 String nameList)
	{
		String names = "";
		String element;
		int resultantArray[] = null;
		ArrayList elements = new ArrayList();
		/*
		 * While there are still commas in the list add each element to the array list
		 */
		while ((nameList != null) && (nameList.indexOf(",") != -1))
		{
			element = nameList.substring(0, nameList.indexOf(",")).trim();
			elements.add(element);
			nameList = nameList.substring(nameList.indexOf(",") + 1).trim();
		}

		if ((nameList != null) && (nameList.length() > 0))
		{
			elements.add(nameList);
		}

		if (elements.size() > 0)
		{
			resultantArray = new int[elements.size()];

			for (int count = 0; count < elements.size(); count++)
				resultantArray[count] = Integer.parseInt((String) elements.get(count));

			names = getNames(test, resultantArray);
		}

		return (names);
	}

	public final void getNames(TestDefinition test,
							   ArrayList results,
							   String prefix)
	{
		for (int count = 0; count < test.getNamesRequired(); count++)
		{
			results.add(prefix + "/" + test.getName(count));
		}
	}

	public final String getNames(TestDefinition test,
								 int[] nameIndexesRequired)
	{
		String names = "";

		for (int count = 0; count < nameIndexesRequired.length; count++)
		{
			names += test.getName(nameIndexesRequired[count] - 1) + " ";
		}
		/*
		 * Strip trailing spaces
		 */
		return (names.trim());
	}

	public final String toString()
	{
		return (this.getId());
	}

	public final String generateUniqueId(String runtimeId)
	{
        return( getId() + ( runtimeId != null ? " ["+runtimeId+"]" : "" ) );
	}

	public final Element serializeToXML()
	{
		Element taskDefElement = new Element("task");
		taskDefElement.setAttribute("id", getId());
		taskDefElement.setAttribute("classname", getClassName());
		taskDefElement.setAttribute("runner", getRunner());
		taskDefElement.setAttribute("type", TYPE_STRINGS[getType()]);
		taskDefElement.setAttribute("timeout", Integer.toString(getTimeout()));

		return (taskDefElement);
	}
}
