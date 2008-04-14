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
// $Id: NameServiceTreeNode.java 170 2008-03-25 18:59:26Z jhalliday $

package org.jboss.dtf.testframework.nameservice;

import org.jboss.dtf.testframework.nameservice.*;
import java.util.*;

public class NameServiceTreeNode
{
	private Hashtable _children = new Hashtable();
	private String _name = "";
	private transient Object _data = null;
	private boolean _isDirectory = false;

	public NameServiceTreeNode()
	{
	}

	public NameServiceTreeNode(String name)
	{
		_name = name;
		_isDirectory = true;
	}

	public NameServiceTreeNode(String name, Object data)
	{
		_name = name;
		_data = data;
		_isDirectory = false;
	}

	public boolean isDirectory()
	{
		return(_isDirectory);
	}

	public void removeEndNode(String[] nodes) throws NameNotBound
	{
		NameServiceTreeNode current = this;
		int count;

		for (count=0;count<(nodes.length-1);count++)
		{
			current = current.findChild(nodes[count]);

			if (current==null)
			{
				throw new NameNotBound();
			}
		}

		if (current.findChild(nodes[count])!=null)
			current.removeChild(nodes[count]);
		else
			throw new NameNotBound();
	}

	public NameServiceTreeNode ensureNodesExist(String[] nodes, boolean create) throws NameNotBound, NameAlreadyBound
	{
		NameServiceTreeNode current = this, previousNode = this;
		boolean found = false;
		int count=0;

		while ( (count<nodes.length) )
		{
			previousNode = current;
			current = current.findChild(nodes[count]);

			/*
			 * If the node doesn't exist - create it
			 */
			found = true;
			if (current==null)
			{
				found = false;
				if (create)
				{
					current = previousNode.addChild(nodes[count]);
				}
				else
					throw new NameNotBound();
			}
			count++;
		}

		if ( (found) && (create) )
			throw new NameAlreadyBound();

		return(current);
	}

	public void setName(String name)
	{
		_name = name;
	}

	public String getName()
	{
		return(_name);
	}

	public void setData(Object data)
	{
		_data = data;
		_isDirectory = false;
	}

	public Object getData()
	{
		return(_data);
	}

	public NameServiceTreeNode findChild(String name)
	{
		return((NameServiceTreeNode)_children.get(name));
	}

	public boolean removeChild(String name)
	{
		return(_children.remove(name)!=null);
	}

	public NameServiceTreeNode[] getChildren()
	{
		NameServiceTreeNode[] returnArray = new NameServiceTreeNode[_children.size()];
		int count = 0;

     	for (Enumeration e = _children.elements(); e.hasMoreElements() ;) {
        	returnArray[count++] = (NameServiceTreeNode)e.nextElement();
     	}

		return(returnArray);
	}

	public NameServiceTreeNode addChild(String name)
	{
		NameServiceTreeNode node = new NameServiceTreeNode(name);
		_children.put(name,node);
		return(node);
	}
}
