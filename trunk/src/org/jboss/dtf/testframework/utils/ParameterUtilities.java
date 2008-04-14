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
// $Id: ParameterUtilities.java 170 2008-03-25 18:59:26Z jhalliday $

package org.jboss.dtf.testframework.utils;

import java.util.ArrayList;

public class ParameterUtilities
{
	public static ArrayList splitParameters(String parameters)
	{
		ArrayList results = new ArrayList();
		String newElement = "";

		while ( (parameters.indexOf(" ") != -1) ||
		        (parameters.indexOf("\"") != -1) )
		{
			if (parameters.startsWith("\""))
			{
				newElement = parameters.substring(1,parameters.indexOf("\"")).trim();
				parameters = parameters.substring(parameters.substring(1).indexOf("\"")+1).trim();
			}
			else
			{
				newElement = parameters.substring(0,parameters.indexOf(" ")).trim();
				parameters = parameters.substring(parameters.substring(1).indexOf(" ")+1).trim();
			}
			results.add(newElement);
		}
		results.add(parameters);

		return(results);
	}
}
