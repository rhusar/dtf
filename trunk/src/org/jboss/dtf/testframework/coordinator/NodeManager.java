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
//
// Copyright (C) 2001,
//
// HP Arjuna Labs
// Newcastle upon Tyne,
// Tyne and Wear,
// UK.
//
// $Id: NodeManager.java 170 2008-03-25 18:59:26Z jhalliday $

package org.jboss.dtf.testframework.coordinator;

import java.util.HashMap;
import java.util.ArrayList;

import org.jboss.dtf.testframework.testnode.TestNodeInterface;
import org.jboss.dtf.testframework.testnode.TaskIdInterface;
import org.jboss.dtf.testframework.testnode.TestNodeBusy;
import org.jboss.dtf.testframework.serviceregister.ServiceRegisterInterface;
import org.jboss.dtf.testframework.serviceregister.ServiceNotFound;
import org.jboss.dtf.testframework.utils.LogWriter;
import org.jboss.dtf.testframework.utils.PermutationGenerator;

/**
 * This class is concerned with supplying test managers with the required test node references
 * required to run a test.
 */
public class NodeManager
{
    protected ServiceRegisterInterface _serviceRegister = null;
    protected HashMap _usedNodes = null;
    protected LogWriter _log = null;


    /**
     * Create a node manager
     *
     * @param serviceRegister The service register to retrieve the node references from.
     * @param usedNodes A hashmap of the nodes used to run tests.
     * @param log The logwriter to log information to.
     */
    public NodeManager(ServiceRegisterInterface serviceRegister,
                       HashMap usedNodes,
                       LogWriter log)
    {
        _serviceRegister = serviceRegister;
        _usedNodes = usedNodes;
        _log = log;
    }

    /**
     * This method uses the exec table and the OS/Product combinations specified in the Test object
     * to generate the testnode permutation codes
     */
    public final PermutationGenerator retrievePermutationGenerator( TestDefinition test, ExecTable taskExecTable )
    {
        HashMap osMap = test.getOSProductMap();
        PermutationGenerator pGen = new PermutationGenerator();

        System.out.println("Creating permutation generator");

        Object[] osId = osMap.keySet().toArray();

        for (int osCount=0;osCount<osId.length;osCount++)
        {
            String osName = (String)osId[osCount];

            System.out.println("OS Name: "+osName);
            HashMap productMap = (HashMap)osMap.get(osName);

            Object[] productId = productMap.keySet().toArray();

            for (int productCount=0;productCount<productId.length;productCount++)
            {
                String productName = (String)productId[productCount];

                pGen.addElement( new OSProductCombination( osName, productName ) );
            }
        }

        pGen.initialise(taskExecTable.getNumberOfDNodes());

        return(pGen);
    }

    public final void retrieveExecTable(    TestDefinition  testDefinition,
                                            ExecTable       taskExecTable )
    {
        ArrayList independentTasks = new ArrayList();
        ArrayList dependentTasks = new ArrayList();
        ArrayList allTasks = new ArrayList();
        HashMap actionNodeMap = new HashMap();

        ArrayList actionList = testDefinition.getActionList();

		for (int actionCount=0;actionCount<actionList.size();actionCount++)
		{
			Action action = (Action)actionList.get(actionCount);

			if ( (action.getType() == Action.PERFORM_TASK) || (action.getType() == Action.START_TASK) )
			{
                try
                {
                    int locationType = action.getLocationType();
                    switch (locationType)
                    {
                        case Action.LOCATION_INDEPENDENT :
                            independentTasks.add(action);
                            break;
                        case Action.LOCATION_DEPENDENT :
                            dependentTasks.add(action);
                            break;
                        case Action.LOCATION_ALL :
                            allTasks.add(action);
                            break;
                    }
                }
                catch (NoAssociatedData e)
                {
                }
            }
        }

        taskExecTable.setNumberOfDNodes(dependentTasks.size());
        taskExecTable.setNumberOfINodes((independentTasks.size()>0) ? 1 : 0);

        int numberOfNodes = taskExecTable.getNumberOfDNodes() + taskExecTable.getNumberOfINodes();
        int column = 0;

		for (int actionCount=0;actionCount<actionList.size();actionCount++)
		{
		    Action[] actionRow = new Action[numberOfNodes];

			Action action = (Action)actionList.get(actionCount);

			if ( (action.getType() == Action.PERFORM_TASK) || (action.getType() == Action.START_TASK) )
			{
			    /*
			     * Is this action performing a location dependent task?
			     */
                if (dependentTasks.contains(action))
                {
                    actionRow[ column ] = action;
                    actionNodeMap.put(action, new Integer(column));
                    column = ( column + 1 ) % taskExecTable.getNumberOfDNodes();
                }
                else
                /*
                 * Is this action performing a location independent task?
                 */
                if (independentTasks.contains(action))
                {
                    actionRow[(numberOfNodes - 1)] = action;
                    actionNodeMap.put(action, new Integer(numberOfNodes - 1));
                }
                else
                /*
                 * Is this action performing a task on all nodes?
                 */
                if (allTasks.contains(action))
                {
                    for (int count=0;count<numberOfNodes;count++)
                        actionRow[count] = action;

                    /*
                     * Indicate this task was run on all nodes
                     */
                    actionNodeMap.put(action, new Integer(-1));
                }
                else
                {
                    try
                    {
                        Action matchAction = testDefinition.getActionWithRuntimeId(action.getLocation());

                        if (matchAction != null)
                        {
                            Integer nodeNum = (Integer)actionNodeMap.get(matchAction);

                            actionNodeMap.put(action, nodeNum);

                            actionRow[nodeNum.intValue()] = action;
                        }
                        else
                        {
                            System.err.println("Cannot find action with runtime id of '"+action.getLocation()+"'");
                        }
                    }
                    catch (NoAssociatedData e)
                    {
                        System.out.println("Unexpected exception: "+e);
                    }
                }
            }

            taskExecTable.add(actionRow);
        }
    }

    public void retrieveNodeList(   PermutationCode permutationCode,
                                    ExecTable       taskExecTable,
                                    NodeList        nodeList ) throws ResourceAllocationFailure
    {
        String pluginClassname = System.getProperty("com.arjuna.mw.testframework.nodemanager.plugin", "org.jboss.dtf.testframework.coordinator.plugins.RoundRobinNodeManager");

        try
        {
            NodeManagerPlugin plugin = (NodeManagerPlugin) Class.forName( pluginClassname ).newInstance();

            plugin.setup( _serviceRegister, _usedNodes, _log );
            plugin.initialise();
            plugin.retrieveNodeList( permutationCode, taskExecTable, nodeList );
        }
        catch (ResourceAllocationFailure r)
        {
            throw r;
        }
        catch (Exception e)
        {
            throw new ResourceAllocationFailure("Failed to load plugin '"+pluginClassname+"': "+e);
        }
    }
}
