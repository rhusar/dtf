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
// $Id: PermutationCode.java 170 2008-03-25 18:59:26Z jhalliday $

package org.jboss.dtf.testframework.coordinator;

import java.util.ArrayList;

/**
 * This class represents a tests permutation code.  When a PermutationCode is created
 * the tasks OSProductCombinations are added in order.  The class is then used to retrieve
 * the task permutation codes
 */
public class PermutationCode
{
	private ArrayList _taskConfigurations = new ArrayList();
	private int		  _index = 0;

    /**
     * Add an OS/Product combination to the list
     *
     * @param taskConfiguration The OS/Product combination to add
     */
	public void addTaskConfiguration(OSProductCombination	taskConfiguration)
	{
		_taskConfigurations.add(taskConfiguration);
	}

    /**
     * Get a non location independent task permutation code
     *
     * @return The OS/Product combination for that task
     */
	public OSProductCombination	getPermutationCodeForNonLITask()
	{
		OSProductCombination result = ((OSProductCombination)_taskConfigurations.get(_index));
	    _index = (_index + 1) % _taskConfigurations.size();
	    return(result);
	}

    /**
     * Reset the OS/Product combination index
     */
    public void reset()
    {
        _index = 0;
    }

    /**
     * Get a location independent task permutation code
     *
     * @return The OS/Product combination for that task
     */
	public OSProductCombination	getPermutationCodeForLITask()
	{
		return((OSProductCombination)_taskConfigurations.get(0));
	}

    /**
     * Generate the test permutation code string
     *
     * @return The permutation code (in hex)
     */
	public final String toString()
	{
		String permutationCode = "";

		/*
		 * Retrieve the OS/Product combination for each task in this test instance
		 */
		for (int count=0;count<_taskConfigurations.size();count++)
		{
			OSProductCombination taskConfig = (OSProductCombination)_taskConfigurations.get(count);

			permutationCode += taskConfig;
		}

		return(permutationCode);
	}

}
