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

package org.jboss.dtf.tools.qa2dtf;

import java.util.Properties;
import java.util.StringTokenizer;
import java.util.ArrayList;

public class CommandLine
{
    private final static String COMMAND_LINE = "CommandLine";
    private final static String START_PHASE = "StartPhase";
    private final static String TERMINATE_PHASE = "TerminatePhase";
    private final static String WAIT_PHASE = "WaitPhase";

    private final static String JAVA_COMMAND = "java";
    private final static String SERVER_NAME_PREFIX = "Server";
    private final static String ORB_FLAGS_VAR = "[ORBFLAGS]";
    private final static String ORB_FLAG_PARAMETER_NAME = "$(ORBFLAGS_1)";
    private final static String ORB_FLAG_PARAMETER_VALUE = "$(ORBFLAGS_2)";

    public final static int     NO_PHASE_DEFINED = -1;

    private String      _commandLine = null;
    private int         _startPhase = NO_PHASE_DEFINED;
    private int         _terminatePhase = NO_PHASE_DEFINED;
    private int         _waitPhase = NO_PHASE_DEFINED;
    private int         _numServerNames = 0;
    private String[]    _parameters = null;
	private String[]	_jvmParams = null;
    private String      _taskName = null;
    private boolean     _javaCommand = false;
    private String		_locationText = null;
	private boolean		_isDTFTask = false;

	public CommandLine(ArrayList jvmParams)
	{
    	_jvmParams = new String[jvmParams.size()];
		jvmParams.toArray(_jvmParams);

		_parameters = new String[0];
	}

    public CommandLine(String cmdLine)
    {
        _commandLine = cmdLine;

        StringTokenizer st = new StringTokenizer(_commandLine, " ");

        _javaCommand = false;

        boolean ignoreVars = true;
        ArrayList params = new ArrayList();
		ArrayList uniqueNameParams = new ArrayList();
		ArrayList jvmParams = new ArrayList();

        while ( st.hasMoreElements() )
        {
            String element = (String)st.nextElement();

            if ( element.startsWith( JAVA_COMMAND ) )
            {
                _javaCommand = true;
            }
            else
            if ( ( element.startsWith("[") ) && (!ignoreVars) )
            {
                if ( element.equals( ORB_FLAGS_VAR ) )
                {
                    params.add( ORB_FLAG_PARAMETER_NAME );
                    params.add( ORB_FLAG_PARAMETER_VALUE );
                }
                else
                {
                    params.add(convertParameter(element));
                }
            }
            else
			if ( element.startsWith("-D") )
			{
				jvmParams.add(element);
			}
			else
            if ( ( !element.startsWith("[") ) && ( !element.startsWith("-D") ) && ( ignoreVars) )
            {
                ignoreVars = false;
                _taskName = element;
            }
            else
            if ( ( !element.startsWith("[") ) && ( !ignoreVars ) )
            {
                if ( element.startsWith(SERVER_NAME_PREFIX) )
                {
                    int number = Integer.parseInt( element.substring( SERVER_NAME_PREFIX.length() ) );

                    _numServerNames = number > _numServerNames ? number : _numServerNames;

                    params.add( "$("+number+")" );
                }
                else
                {
                    params.add( element );
                }
            }
        }

        _parameters = new String[params.size()+uniqueNameParams.size()];
        params.toArray(_parameters);

		String[] tempParams = new String[uniqueNameParams.size()];
		uniqueNameParams.toArray(tempParams);

		System.arraycopy(tempParams,0, _parameters, params.size(), tempParams.length);

		_jvmParams = new String[jvmParams.size()];
		jvmParams.toArray(_jvmParams);
    }

    private String convertParameter(String parameter)
    {
        while ( parameter.indexOf('[') != -1 )
        {
			String varName = parameter.substring( parameter.indexOf('[') + 1 );
			varName = varName.substring(0,varName.indexOf(']'));

			String value = System.getProperty(varName);

			if ( value == null )
			{
            	parameter = parameter.substring( 0, parameter.indexOf('[') ) + "$(" + varName +")" + parameter.substring(parameter.indexOf(']')+1);
			}
			else
			{
				parameter = parameter.substring( 0, parameter.indexOf('[') ) + value + parameter.substring(parameter.indexOf(']')+1);
			}
        }

        return parameter;
    }

    private static String stripVars(String command)
    {
        /** Strip variables marked by [**] **/
        while ( command.indexOf('[') != -1 )
        {
            command = command.substring( 0, command.indexOf('[') ) + command.substring( command.indexOf(']') + 1 );
        }

        /** Replace all double spaces with a single space **/
        while ( command.indexOf("  ") != -1 )
        {
            command = command.substring( 0, command.indexOf("  ") ) + ' ' + command.substring( command.indexOf("  ") + 2 );
        }

        return command;
    }

    public String getRawCommandLine()
    {
        return _commandLine;
    }

    public String getTaskName()
    {
        return _taskName;
    }

	public String getLocationText()
	{
		if ( _locationText == null )
		{
			String location = QA2DTF.getLocation(this.getTaskName());

			return location != null ? "location=\""+location+"\"" : "";
		}

		return _locationText;
	}

	public boolean isDTFTask()
	{
		return _isDTFTask;
	}

	public void setIsDTFTask(boolean task)
	{
		_isDTFTask = task;
	}

	public void setLocationText(String location)
	{
		_locationText = location;
	}

    public int getNumberOfServerNames()
    {
        return _numServerNames;
    }

    public String[] getParameters()
    {
        return _parameters;
    }

	public String[] getJVMParameters()
	{
		return _jvmParams;
	}

	public boolean hasJVMParameters()
	{
		return _jvmParams.length > 0;
	}

    public boolean hasParameters()
    {
        return _parameters.length > 0;
    }

    public int getStartPhase()
    {
        return _startPhase;
    }

    public int getTerminatePhase()
    {
        return _terminatePhase;
    }

    public int getWaitPhase()
    {
        return _waitPhase;
    }

    public void setStartPhase(int phase)
    {
        _startPhase = phase;
    }

    public void setTerminatePhase(int phase)
    {
        _terminatePhase = phase;
    }

    public void setWaitPhase(int phase)
    {
        _waitPhase = phase;
    }

    public void setParameters(String[] parameters)
    {
        _parameters = parameters;
    }

    public void setNumberOfServerNames(int num)
    {
        _numServerNames = num;
    }

    public void setTaskName(String name)
    {
        _taskName = name;
    }

    public static CommandLine getCommandLine(Properties props, String type, int number)
    {
        CommandLine newElement = new CommandLine( props.getProperty( type + COMMAND_LINE + number, "ERROR No CommandLine Specified" ) );

        newElement.setStartPhase( Integer.parseInt( props.getProperty( type + START_PHASE + number, "-1" ) ) );
        newElement.setTerminatePhase( Integer.parseInt( props.getProperty( type + TERMINATE_PHASE + number, "-1" ) ) );
        newElement.setWaitPhase( Integer.parseInt( props.getProperty( type + WAIT_PHASE + number, "-1" ) ) );

        return newElement;
    }
}
