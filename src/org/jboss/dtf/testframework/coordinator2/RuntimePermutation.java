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

import java.util.ArrayList;

public class RuntimePermutation
{
	private ArrayList	_elements = new ArrayList();
    private boolean		_completed = false;
    private int			_retryCount = 0;

    public boolean contains(OSProductCombination o)
    {
        return _elements.contains(o);
    }

	public OSProductCombination[] getPermutationElements()
	{
		OSProductCombination[] returnArray = new OSProductCombination[_elements.size()];
		_elements.toArray(returnArray);

		return returnArray;
	}

	public int getRetryCount()
	{
		return _retryCount;
	}

	public int incrementRetryCount()
	{
		return _retryCount++;
	}

	public OSProductCombination getPermutationCodeForLITask()
	{
		return (OSProductCombination)_elements.get(0);
	}

	public void add(OSProductCombination combination)
	{
    	_elements.add(combination);
	}

	public int getNumberOfNodes()
	{
		return _elements.size();
	}

	public void clear()
	{
		_elements.clear();
	}

	public void setCompleted()
	{
		_completed = true;
	}

	public boolean isCompleted()
	{
		return _completed;
	}

	public String toString()
	{
		String permText = "";

		for (int count=0;count<_elements.size();count++)
		{
			OSProductCombination osProdCombi = (OSProductCombination)_elements.get(count);
			permText += osProdCombi.toString()+"_";
		}

		return permText.substring(0, permText.length() - 1).replace(' ','_');
	}
}
