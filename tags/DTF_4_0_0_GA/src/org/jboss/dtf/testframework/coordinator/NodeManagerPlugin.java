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
 * $Id: NodeManagerPlugin.java 170 2008-03-25 18:59:26Z jhalliday $
 */
package org.jboss.dtf.testframework.coordinator;

import org.jboss.dtf.testframework.serviceregister.ServiceRegisterInterface;
import org.jboss.dtf.testframework.utils.LogWriter;

import java.util.HashMap;

public abstract class NodeManagerPlugin
{
    private ServiceRegisterInterface  _serviceRegister = null;
    private HashMap                   _usedNodes = null;
    private LogWriter                 _log = null;

    /**
     * Setup the plugin, this method is invoked by the framework.
     *
     * @param serviceRegister The object reference for the service register.
     * @param usedNodes The hashmap mapping nodes to the tasks used.
     * @param log The logwriter the node manager must use.
     */
    final void setup(   ServiceRegisterInterface serviceRegister,
                        HashMap usedNodes,
                        LogWriter log)
    {
        _serviceRegister = serviceRegister;
        _usedNodes = usedNodes;
        _log = log;
    }

    /**
     * Initialise the plugin - this method is used by the plugin developer to
     * initialise the necessary variables.
     */
    public void initialise()
    {
        /**
         * Do nothing
         */
    }

    /**
     * Retrieve the service register reference.
     *
     * @return An object reference for the frameworks service register.
     */
    public final ServiceRegisterInterface getServiceRegister()
    {
        return(_serviceRegister);
    }

    /**
     * Retrieve the used nodes hashmap.
     *
     * @return The used nodes hashmap.
     */
    public final HashMap getUsedNodesMap()
    {
        return(_usedNodes);
    }

    /**
     * Retrieve the logwriter the plugin must use for information output.
     *
     * @return The logwriter for this plugin.
     */
    public final LogWriter log()
    {
        return(_log);
    }

    /**
     * Generates a list of nodes to use to run the given permutation with the passed execution table.
     *
     * @param permutationCode The permutation of the test to be executed.
     * @param taskExecTable The task execution table containing the actions required for each node (a node
     *                      is a row in the table)
     * @param nodeList The nodes used to execute the tasks (nodeList[0] == taskExecTable[row0])
     */
    public abstract void retrieveNodeList(PermutationCode permutationCode,
                                          ExecTable       taskExecTable,
                                          NodeList        nodeList ) throws ResourceAllocationFailure;
}
