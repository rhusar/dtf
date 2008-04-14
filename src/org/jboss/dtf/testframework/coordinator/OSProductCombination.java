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
// $Id: OSProductCombination.java 170 2008-03-25 18:59:26Z jhalliday $

package org.jboss.dtf.testframework.coordinator;

/**
 * This class is used to store and Operating System/Product combination so that they
 * can easily be passed between classes.
 */
public class OSProductCombination
{
	private String			_os;
	private String			_product;

    /**
     * Create an OS/Product combination with the given os and product name
     *
     * @param osName The name of the OS
     * @param productName The name of the Product
     */
	public OSProductCombination(String osName, String productName)
	{
		_os = osName;
		_product = productName;
	}


    /**
     * Retrieve the name of the OS from this combination
     *
     * @return The name of the OS from this combination
     */
	public final String getOSName()
	{
		return(_os);
	}

    /**
     * Retrieve the name of the Product from this combination
     *
     * @return The name of the Product from this combination
     */
	public final String getProductName()
	{
		return(_product);
	}

	public final static String padHexNumber(String text, int places)
	{
		if (text.length()<places)
		{
			for (int count=0;count<places-text.length();count++)
				text = "0" + text;
		}

		return(text);
	}

    /**
     * Convert this OS/Product combination into a 4 character hex code
     *
     * @return The permutation code for this OS/Product combination
     */
	public final String toString()
	{
		return(padHexNumber(Long.toHexString(generateChecksum(_os)),2)+padHexNumber(Long.toHexString(generateChecksum(_product)),2));
	}

    /**
     * Generate a checksum from the given text.
     *
     * @param text The string to generate the checksum for.
     * @return The checksum
     */
	public static final long generateChecksum(String text)
	{
		long checkSum = 0, multiplier = 1;

		for (int count=0;count<text.length();count++)
		{
			checkSum = (checkSum + text.charAt(count)*multiplier) % 255;
			multiplier *= 2;
		}

		return(checkSum);
	}
}
