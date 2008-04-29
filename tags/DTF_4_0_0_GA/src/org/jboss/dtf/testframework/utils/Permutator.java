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
// $Id: Permutator.java 170 2008-03-25 18:59:26Z jhalliday $

package org.jboss.dtf.testframework.utils;

/**
 * Generates all possible permutations of an array of objects.
 **/
public class Permutator
{
	protected int 		size;
	protected Object[] 	data;
	protected int[]		value;
	private int			numFactorials;
	private int			factCount = 0;

	/**
	 * Constructs a permutator.  Generates the first permutation which can be retrieved
	 * using get().
	 * @param objectList The array of objects to create the permutations of
	 **/
	public Permutator(Object[] objectList)
	{
	  	int count,i,j;
	  	size = objectList.length;

		data = new Object[size];
		value = new int[size];

		for (count=0;count<size;count++)
			value[count] = count;

		System.arraycopy(objectList, 0, data, 0, size);

		numFactorials = factorial(size);
	}

	/**
	 * Generates the next permutation.  If there are no more permutations this method
	 * returns false.
	 * @returns False when there are no more permutations to generate
	 **/
	public boolean getNext()
	{
		if (++factCount<numFactorials)
		{
			int i = size - 1;

			while (value[i-1] >= value[i])
				i = i-1;

			int j = size;

			while (value[j-1] <= value[i-1])
				j = j-1;

			swap(i-1, j-1);

			i++;
			j = size;

			while (i < j)
			{
				swap(i-1, j-1);
				i++;
				j--;
			}
		}

		return(factCount<numFactorials);
	}

	/**
	 * Retrieves the current permutation.
	 * @returns An array of objects representing the current permutation.
	 **/
	public Object[] get()
	{
		Object[] results = new Object[value.length];

		for (int count=0;count<value.length;count++)
			results[count] = data[value[count]];

		return(results);
	}

	private void swap(int a, int b)
	{
		int temp = value[a];
		value[a] = value[b];
		value[b] = temp;
	}


	private int factorial(int a)
	{
		int temp = 1;

		if (a > 1)
	    	for (int i = 1; i <= a; temp *= i++);

	 	return temp;
	}
}
