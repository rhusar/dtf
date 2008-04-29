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

import java.util.*;

public class TestDefinition
{
    private final static String   NUMBER_OF_PREFIX = "NumberOf";

    private final static String   SETUPS_POSTFIX = "Setups";
    private final static String   SERVERS_POSTFIX = "Servers";
    private final static String   CLIENTS_POSTFIX = "Clients";
    private final static String   OUTCOMES_POSTFIX = "Outcomes";
    private final static String   CLEANUPS_POSTFIX = "Cleanups";

    private final static String   SETUP_PREFIX = "Setup";
    private final static String   SERVER_PREFIX = "Server";
    private final static String   CLIENT_PREFIX = "Client";
    private final static String   OUTCOME_PREFIX = "OutcomeCheck";
    private final static String   CLEANUP_PREFIX = "Cleanup";
    private final static String   TEST_NAME = "TestName";

	private final static String	  TRANSACTION_SERVICE_CLASSNAME = "com.hp.mw.ts.jts.TransactionServer";

    private CommandLine[]   _setups = null;
    private CommandLine[]   _servers = null;
    private CommandLine[]   _clients = null;
    private CommandLine[]   _outcomes = null;
    private CommandLine[]   _cleanups = null;

    private String          _name = null;
    private int             _numberOfPhases = 0;

    public TestDefinition(Properties testConf)
    {
        _name = testConf.getProperty( TEST_NAME );

        int numberOfSetups = Integer.parseInt( testConf.getProperty( NUMBER_OF_PREFIX + SETUPS_POSTFIX, "0") );

        _setups = new CommandLine[ numberOfSetups ];
        for (int count=0;count<numberOfSetups;count++)
        {
            _setups[count] = CommandLine.getCommandLine(testConf, SETUP_PREFIX, count);

            _numberOfPhases = ( _setups[count].getTerminatePhase() > _numberOfPhases ) ? _setups[count].getTerminatePhase() : _numberOfPhases;
            _numberOfPhases = ( _setups[count].getWaitPhase() > _numberOfPhases ) ? _setups[count].getWaitPhase() : _numberOfPhases;
        }

        int numberOfServers = Integer.parseInt( testConf.getProperty( NUMBER_OF_PREFIX + SERVERS_POSTFIX, "0") );

        _servers = new CommandLine[ numberOfServers ];
        for (int count=0;count<numberOfServers;count++)
        {
            _servers[count] = CommandLine.getCommandLine(testConf, SERVER_PREFIX, count);

            _numberOfPhases = ( _servers[count].getTerminatePhase() > _numberOfPhases ) ? _servers[count].getTerminatePhase() : _numberOfPhases;
            _numberOfPhases = ( _servers[count].getWaitPhase() > _numberOfPhases ) ? _servers[count].getWaitPhase() : _numberOfPhases;
        }

        int numberOfClients = Integer.parseInt( testConf.getProperty( NUMBER_OF_PREFIX + CLIENTS_POSTFIX, "0") );

        _clients = new CommandLine[ numberOfClients ];
        for (int count=0;count<numberOfClients;count++)
        {
            _clients[count] = CommandLine.getCommandLine(testConf, CLIENT_PREFIX, count);
        }

        int numberOfOutcomes = Integer.parseInt( testConf.getProperty( NUMBER_OF_PREFIX + OUTCOMES_POSTFIX, "0") );

        _outcomes = new CommandLine[ numberOfOutcomes ];
        for (int count=0;count<numberOfOutcomes;count++)
        {
            _outcomes[count] = CommandLine.getCommandLine(testConf, OUTCOME_PREFIX, count);
        }

        int numberOfCleanups = Integer.parseInt( testConf.getProperty( NUMBER_OF_PREFIX + CLEANUPS_POSTFIX, "0") );

        _cleanups = new CommandLine[ numberOfCleanups ];
        for (int count=0;count<numberOfCleanups;count++)
        {
            _cleanups[count] = CommandLine.getCommandLine(testConf, CLEANUP_PREFIX, count);
        }

		processSettings();
    }

	protected void processSettings()
	{
        for (int count=0;count<_servers.length;count++)
		{
			/** If the task is a transaction service start task then insert a register and setup task **/
			if ( _servers[count].getTaskName().equals(TRANSACTION_SERVICE_CLASSNAME) )
			{
				ArrayList jvmParams = new ArrayList();
				int otsServerId = (this.getNumberOfServerNames() + 1);

				jvmParams.add("-Dots.server.bindname=$("+otsServerId+")");

            	/** Insert register task **/
				CommandLine registerTask = new CommandLine(jvmParams);
				registerTask.setTaskName("com.arjuna.ats.qa.Utils.RegisterOTSServer");
				registerTask.setStartPhase(_servers[count].getStartPhase());
				registerTask.setNumberOfServerNames(otsServerId);
				registerTask.setIsDTFTask(true);
				registerTask.setLocationText("location=\"server"+count+"\"");

				/** Insert setup task **/
				CommandLine setupTask = new CommandLine(jvmParams);
				setupTask.setTaskName("com.arjuna.ats.qa.Utils.SetupOTSServer");
				setupTask.setStartPhase(_servers[count].getStartPhase());
				setupTask.setNumberOfServerNames(otsServerId);
				setupTask.setIsDTFTask(true);
				setupTask.setLocationText("location=\"all\"");

				insertServer(count+1, registerTask);
				insertServer(count+2, setupTask);
			}
		}
	}

	private void insertServer(int position, CommandLine task)
	{
		CommandLine[] newArray = new CommandLine[_servers.length + 1];
        System.arraycopy(_servers,0,newArray,0,_servers.length);

		for (int count=newArray.length - 1;count>position;count--)
		{
			newArray[count] = newArray[count - 1];
		}

		newArray[position] = task;

		_servers = newArray;
	}

	public int getNumberOfServerNames()
	{
		int numb = 0;

		for (int count=0;count<_servers.length;count++)
		{
			if ( _servers[count].getNumberOfServerNames() > numb )
			{
				numb = _servers[count].getNumberOfServerNames();
			}
		}

		return numb;
	}
    public String getName()
    {
        return _name;
    }

    public CommandLine[] getSetups()
    {
        return _setups;
    }

    public CommandLine[] getServers()
    {
        return _servers;
    }

    public CommandLine[] getClients()
    {
        return _clients;
    }

    public CommandLine[] getOutcomes()
    {
        return _outcomes;
    }

    public CommandLine[] getCleanups()
    {
        return _cleanups;
    }
}
