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
// $Id: NodeList.java 170 2008-03-25 18:59:26Z jhalliday $

package org.jboss.dtf.testframework.coordinator;

import java.util.HashMap;

import org.jboss.dtf.testframework.testnode.TestNodeInterface;

/**
 * This class represents a row of nodes which map on to an ExecTable row.
 * Therefore if Action A is in column 1 of the ExecTable this action
 * would be executed on the node in column 1 of this node list.
 */
public class NodeList
{
    protected TestNodeInterface[] _nodes;
    protected String[] _nodeNames;
    /**
     * Create a node list of specified length
     *
     * @param numNodes The length of the node list.
     */
    public NodeList(int numNodes)
    {
        _nodes = new TestNodeInterface[numNodes];
        _nodeNames = new String[numNodes];
    }

    /**
     * Add a node to the list at the given position
     *
     * @param position The position in the list to add the test node.
     * @param node The test node to add to the list.
     */
    public void add(int position, TestNodeInterface node) throws java.rmi.RemoteException
    {
        _nodes[position] = node;
        _nodeNames[position] = node.getName();
    }

    /**
     * Retrieve the node in the list at the given position
     *
     * @param position The position in the list to retrieve the node from.
     * @return The test node at the given position.
     */
    public TestNodeInterface get(int position)
    {
        return(_nodes[position]);
    }

    public String getName(int position)
    {
        return(_nodeNames[position]);
    }
}
