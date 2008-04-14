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
 * Copyright (C) 1998, 1999, 2000, 2001, 2002
 *
 * Arjuna Technology Ltd.
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: NodeManager.java 170 2008-03-25 18:59:26Z jhalliday $
 */
package org.jboss.dtf.testframework.coordinator2.runmanager;

import org.jboss.dtf.testframework.coordinator2.RuntimePermutation;
import org.jboss.dtf.testframework.coordinator2.Coordinator;
import org.jboss.dtf.testframework.coordinator2.OSProductCombination;
import org.jboss.dtf.testframework.nameservice.NameServiceInterface;
import org.jboss.dtf.testframework.serviceregister.ServiceRegisterInterface;
import org.jboss.dtf.testframework.serviceregister.ServiceRegister;
import org.jboss.dtf.testframework.serviceregister.ServiceNotFound;
import org.jboss.dtf.testframework.testnode.TestNodeInterface;
import org.jboss.dtf.testframework.testnode.TestNodeDescription;
import org.jboss.dtf.testframework.coordinator.ResourceAllocationFailure;

import java.util.Hashtable;
import java.util.Enumeration;
import java.util.ArrayList;

public class NodeManager
{
	/** Reference to the service register **/
	private ServiceRegisterInterface 	_serviceRegister = null;

	/** Node table **/
	private Hashtable					_osProductNodeMap = new Hashtable();

	/** Node ticket table **/
	private Hashtable					_nodeTickets = new Hashtable();

    /**
     * Interest register - this is map of OSProductCombination -> list of nodes interested
     **/
    private Hashtable                   _interestRegister = new Hashtable();

	public NodeManager() throws Exception
	{
		/**
		 * Retrieve a reference to the service register
		 */
		NameServiceInterface nameService = Coordinator.getNameService();
		_serviceRegister = (ServiceRegisterInterface)nameService.lookup( ServiceRegister.SERVICE_REGISTER_NAME_SERVICE_ENTRY );
	}

	public TestNodeTicket getNodeTicket(TestNodeInterface node)
	{
		return (TestNodeTicket)_nodeTickets.get(node);
	}

    public boolean ensureNodesExist(RuntimePermutation permutation)
    {
        boolean enough = true;

        synchronized(_osProductNodeMap)
        {
            OSProductCombination[] osProdCombis = permutation.getPermutationElements();

            /**
             * Make sure there are enough nodes to use before reserving them for this test
             */
            for (int count=0;count<osProdCombis.length;count++)
            {
                Hashtable productMap = (Hashtable)_osProductNodeMap.get(osProdCombis[count].getOSId());

                ArrayList nodeList = (ArrayList)productMap.get(osProdCombis[count].getProductId());

                enough &= nodeList.size() > 0;
            }
        }

        return enough;
    }

	public synchronized ArrayList getNodes(RuntimePermutation perm) throws ResourceAllocationFailure
	{
		ArrayList returnNodes = new ArrayList();

		synchronized(_osProductNodeMap)
		{
			boolean enough = true;

			OSProductCombination[] osProdCombis = perm.getPermutationElements();

			/**
			 * Make sure there are enough nodes to use before reserving them for this test
			 */
			for (int count=0;count<osProdCombis.length;count++)
			{
				Hashtable productMap = (Hashtable)_osProductNodeMap.get(osProdCombis[count].getOSId());

				ArrayList nodeList = (ArrayList)productMap.get(osProdCombis[count].getProductId());

				/** If the node list is empty - then we cannot run any more tests using this os/product **/
				if ( nodeList == null || nodeList.size() == 0 )
				{
					throw new FatalResourceAllocationFailure("No nodes found for "+osProdCombis[count]);
				}

                int nCount = 0;
				boolean found = false;

				while ( (nCount<nodeList.size()) && (!found) )
				{
					TestNodeTicket tnt = (TestNodeTicket)nodeList.get(nCount++);

					if ( !tnt.isInUse() )
					{
						found = true;
					}
				}

				if ( !found )
				{
					enough = false;
				}
			}

			if ( !enough )
			{
                throw new ResourceAllocationFailure("Not enough nodes to perform test");
			}

			for (int count=0;count<osProdCombis.length;count++)
			{
				Hashtable productMap = (Hashtable)_osProductNodeMap.get(osProdCombis[count].getOSId());

				ArrayList nodeList = (ArrayList)productMap.get(osProdCombis[count].getProductId());
                int nCount = 0;
				boolean found = false;

				while ( (nCount<nodeList.size()) && (!found) )
				{
					TestNodeTicket tnt = (TestNodeTicket)nodeList.get(nCount++);

					if ( !tnt.isInUse() )
					{
						tnt.setInUse();
						returnNodes.add(tnt);

						found = true;
					}
				}
			}
		}

    	return returnNodes;
	}

    public synchronized int getNumberOfInterestedParties(OSProductCombination interest)
    {
        Hashtable products= (Hashtable)_interestRegister.get(interest.getOSId());

        if (products != null)
        {
            PrioritisedRunManagerList parties = (PrioritisedRunManagerList)products.get(interest.getProductId());

            if ( parties != null )
            {
                return parties.size();
            }
        }

        return 0;
    }

    public synchronized void unregisterInterest(RunManager runManager, OSProductCombination interest)
    {
        System.out.println("UnregisterInterest ("+runManager.getRunId().getUID()+","+interest+")");

        /** Remove runmanager from registered parties **/
        Hashtable products = (Hashtable)_interestRegister.get(interest.getOSId());

        if ( products != null )
        {
            PrioritisedRunManagerList parties = (PrioritisedRunManagerList)products.get(interest.getProductId());

            if ( parties != null )
            {
                parties.removeManager(runManager);

                /** If there are now no interested parties then remove the nodes from the nodelist **/
                if ( parties.isEmpty() )
                {
                    Hashtable productNodeMap = (Hashtable)_osProductNodeMap.get(interest.getOSId());
                    productNodeMap.remove(interest.getProductId());
                }
            }
        }
    }

    public synchronized void unregisterInterest(RunManager runManager)
    {
        System.out.println("UnregisterInterest ("+runManager.getRunId().getUID()+")");
        Enumeration e = _interestRegister.keys();
        /** Remove runmanager from registered parties **/
        while (e.hasMoreElements())
        {
            String osId = (String)e.nextElement();
            Hashtable products = (Hashtable)_interestRegister.get(osId);

            Enumeration e2 = products.keys();

            while (e2.hasMoreElements())
            {
                String productId = (String)e2.nextElement();
                PrioritisedRunManagerList parties = (PrioritisedRunManagerList)products.get(productId);

                if ( parties != null )
                {
                    parties.remove(runManager);

                    /** If there are now no interested parties then remove the nodes from the nodelist **/
                    if ( parties.isEmpty() )
                    {
                        Hashtable productNodeMap = (Hashtable)_osProductNodeMap.get(osId);
                        productNodeMap.remove(productId);
                    }
                }
            }
        }
    }

    public synchronized RunManager[] getInterestedParties(OSProductCombination interest)
    {
        RunManager[] returnArray = null;
        Hashtable products = (Hashtable)_interestRegister.get(interest.getOSId());

        if ( products != null)
        {
            PrioritisedRunManagerList parties = (PrioritisedRunManagerList)products.get(interest.getProductId());

            if ( parties != null )
            {
                returnArray = new RunManager[parties.size()];
                parties.toArray(returnArray);
            }
        }

        return returnArray;
    }

    /**
     * This method is called by runmanagers so that they can express an interest in
     * certain OS/Product combinations.  This allows the nodemanager to ensure it has
     * the correct node tickets for the os/product combinations required.
     *
     * @param runManager
     * @param interest
     */
    public synchronized boolean registerInterest(RunManager runManager, OSProductCombination interest) throws ServiceNotFound
    {
        System.out.println("RegisterInterest ("+runManager.getRunInformation()+","+interest+")");
        boolean success = true;

        /** Add the runmanager to the list of interested parties for that OSProductCombination **/
        Hashtable products = (Hashtable)_interestRegister.get(interest.getOSId());

        if ( products == null )
        {
            _interestRegister.put(interest.getOSId(),products = new Hashtable(1));
        }

        PrioritisedRunManagerList listOfRunManagers = (PrioritisedRunManagerList)products.get(interest.getProductId());

        if ( listOfRunManagers == null )
        {
            products.put(interest.getProductId(), listOfRunManagers = new PrioritisedRunManagerList());
        }

        /** If the runmanager hasn't already expressed an interest in this osproductcombination then add it **/
        if ( !listOfRunManagers.contains(runManager))
        {
            listOfRunManagers.add(runManager);
        }

        try
        {
            TestNodeInterface[] nodes = _serviceRegister.lookupService(interest.getOSId(), interest.getProductId());
            Hashtable productNodeMap = (Hashtable)_osProductNodeMap.get(interest.getOSId());

            if ( productNodeMap == null )
            {
                _osProductNodeMap.put(interest.getOSId(), productNodeMap = new Hashtable());
            }

            ArrayList nodeList = (ArrayList) productNodeMap.get(interest.getProductId());

            if ( nodeList == null )
            {
                productNodeMap.put(interest.getProductId(), nodeList = new ArrayList());
            }

            System.out.println("NodeManager registering interest in ["+interest.getOSId()+"] -> ["+interest.getProductId()+"]");
            int addCount = 0;

            for (int count=0;count<nodes.length;count++)
            {
                System.out.println("\tNode:"+nodes[count].getName());

                try
                {
                    if ( nodes[count].ping() )
                    {
                        if ( _nodeTickets.containsKey(nodes[count]) )
                        {
                            TestNodeTicket ticket = (TestNodeTicket)_nodeTickets.get(nodes[count]);

                            if ( !nodeList.contains(ticket) )
                            {
                                nodeList.add(ticket);
                            }
                        }
                        else
                        {
                            TestNodeTicket ticket = new TestNodeTicket(nodes[count]);
                            _nodeTickets.put(nodes[count],ticket);

                            if ( !nodeList.contains(ticket) )
                            {
                                nodeList.add(ticket);
                            }

                            addCount++;
                        }
                    }
                }
                catch (java.rmi.RemoteException e)
                {
                    System.err.println("Node failed to respond - ignoring");
                }
            }

            System.out.println("Added: "+addCount+" node(s)");
        }
        catch (ServiceNotFound e)
        {
            throw new ServiceNotFound("No nodes supporting '"+interest.getOSId()+"'->'"+interest.getProductId()+"'");
        }
        catch (java.rmi.RemoteException e)
        {
            System.out.println("An unexpected exception occurred: "+e);
            e.printStackTrace(System.err);
            success = false;
        }

        return success;
    }

    /**
     * This method is called by the testmanager when it has released nodes.
     * The node manager can then notify interested parties that the nodes
     * are available.
     *
     * @param nodeTicket
     */
    public void notifyNodesReleased(TestNodeTicket nodeTicket)
    {
		if ( nodeTicketIsValid( nodeTicket ) )
		{
			System.out.println("Node released - finding interested parties");

			try
			{
				TestNodeDescription nodeDescription = nodeTicket.getNodeDescription();

				Hashtable products = (Hashtable)_interestRegister.get(nodeDescription.getOSID());

				String[] productList = nodeDescription.getProductSupportInformation().getProductList();

				for (int count=0;count<productList.length;count++)
				{
					PrioritisedRunManagerList interestedRunManagers = (PrioritisedRunManagerList)products.get(productList[count]);

					if ( interestedRunManagers != null )
					{
						/**
						 * Tell each one in turn that the node is available, if a runmanager
						 * cannot use the node continue on to the next run manager until either
						 * no body wants it or a runmanager signals it can use it
						 */
						boolean nodeUsed = false;
						int rmCount = 0;
						while ( rmCount < interestedRunManagers.size() && !nodeUsed )
						{
							RunManager interestedRunManager = (RunManager)interestedRunManagers.get(rmCount);

							if ( interestedRunManager.getRunId() != null )
							{
								System.out.println("Informing run manager ("+interestedRunManager.getRunManagerId()+") - RunId:"+interestedRunManager.getRunId().getUID());
							}

							nodeUsed = interestedRunManager.notifyNodeAvailable(new OSProductCombination(nodeDescription.getOSID(),productList[count]));

							rmCount++;
						}
					}
				}
			}
			catch (java.rmi.RemoteException e)
			{
				System.out.println("ERROR - Failed to retrieve description");
			}
		}
		else
		{
			System.out.println("Not looking for interested parties - node ticket not valid (naughty node)");
		}
    }

	private boolean nodeTicketIsValid(TestNodeTicket nodeTicket)
	{
		return _nodeTickets.containsKey(nodeTicket.getNode());
	}

	/**
	 * Remove the node represented by this node ticket so that it shall not
	 * be used in any further test executions.  This should be called by the
	 * test manager when a testnode has not responded to a task execution
	 * request.
	 *
	 * @param nodeTicket The node ticket representing the node to be removed
	 * from the map.
	 */
	public synchronized boolean removeNode(TestNodeTicket nodeTicket)
	{
		boolean success = false;
        Enumeration osEnum = _osProductNodeMap.keys();

		while ( osEnum.hasMoreElements() )
		{
            String osId = (String)osEnum.nextElement();
			Hashtable productNodeMap = (Hashtable)_osProductNodeMap.get(osId);

			Enumeration productEnumeration = productNodeMap.keys();

			while ( productEnumeration.hasMoreElements() )
			{
				String productId = (String) productEnumeration.nextElement();

				ArrayList nodeList = (ArrayList) productNodeMap.get(productId);

				/** The node could exist in multiple os/product maps **/
				success |= nodeList.remove(nodeTicket);
			}
		}

		/** Remove the node from the tickets map **/
		_nodeTickets.remove(nodeTicket.getNode());

		return success;
	}
}
