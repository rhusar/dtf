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
// $Id: PermutationGenerator.java 170 2008-03-25 18:59:26Z jhalliday $

package org.jboss.dtf.testframework.utils;

import java.util.ArrayList;
import org.jboss.dtf.testframework.coordinator.OSProductCombination;
import org.jboss.dtf.testframework.coordinator.PermutationCode;

public class PermutationGenerator
{
    private ArrayList               _pElements = new ArrayList();
    private int                     _numberOfTasks = 0;
    private int                     _positions[];
    private boolean                 _moreToCome = false;

    public final void addElement(OSProductCombination obj)
    {
        System.out.println("Added OS/Product Combo "+obj.getOSName()+","+obj.getProductName());
        _pElements.add(obj);
    }

    public final void initialise(int numberOfTasks)
    {
        System.out.println("Initialising permutation generator #tasks:"+numberOfTasks);
        _numberOfTasks = numberOfTasks;
        _positions = new int[numberOfTasks];

        java.util.Arrays.fill(_positions, 0);
    }

    public final PermutationCode getPermutation()
    {
        if ( (_numberOfTasks == 0) || (_pElements.size()==0) )
        {
            return(null);
        }
        PermutationCode resultPermCode = new PermutationCode();
        Object[] objs = _pElements.toArray();

        for (int count=0;count<_positions.length;count++)
        {
            resultPermCode.addTaskConfiguration((OSProductCombination)objs[_positions[count]]);
        }

        _moreToCome = increasePositions();
        return(resultPermCode);
    }

    public final boolean hasAnother()
    {
        return(_moreToCome);
    }

    private boolean increasePositions()
    {
        for (int count=_positions.length-1;count>=0;count--)
        {
            if (++_positions[count]==_pElements.size())
            {
                if (count!=0)
                    _positions[count] = 0;
                else
                    return(false);
            }
            else
                break;
        }

        return(true);
    }
}
