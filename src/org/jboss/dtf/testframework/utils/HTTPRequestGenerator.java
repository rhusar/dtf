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
// $Id: HTTPRequestGenerator.java 170 2008-03-25 18:59:26Z jhalliday $

package org.jboss.dtf.testframework.utils;

import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.HashMap;

public class HTTPRequestGenerator
{
	public static String postRequest(	String 	urlString,
										HashMap	data )
	{
		String returnData = "";

		try
		{
			URL url = new URL(urlString);
			URLConnection connection = url.openConnection();
			connection.setDoOutput(true);
			connection.setDoInput(true);

			PrintWriter out = new PrintWriter(connection.getOutputStream());

			Object[] keySet = data.keySet().toArray();
			Object[] values = data.values().toArray();

			for (int count=0;count<keySet.length;count++)
			{
				out.print(URLEncoder.encode((String)keySet[count])+"="+URLEncoder.encode((String)values[count]));

				if ((count+1) < keySet.length)
					out.print("&");
			}
			out.close();

			BufferedReader in = new BufferedReader(
							new InputStreamReader( connection.getInputStream() ) );

			String inputLine;

			while ((inputLine = in.readLine())!=null)
			{
				returnData += inputLine;
			}
			in.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			returnData = null;
		}

		return(returnData);
	}

	public static String getRequest( String 	urlString,
									  HashMap	data )
	{
		String returnData = "";

		try
		{
			Object[] keySet = data.keySet().toArray();
			Object[] values = data.values().toArray();

			for (int count=0;count<keySet.length;count++)
			{
				urlString += (URLEncoder.encode((String)keySet[count])+"="+URLEncoder.encode((String)values[count]));

				if ((count+1) < keySet.length)
					urlString +=("&");
			}

			URL url = new URL(urlString);
			URLConnection connection = url.openConnection();
			connection.setDoOutput(false);
			connection.setDoInput(true);

			BufferedReader in = new BufferedReader(
							new InputStreamReader( connection.getInputStream() ) );

			String inputLine;

			while ((inputLine = in.readLine())!=null)
			{
				returnData += inputLine;
			}
			in.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			returnData = null;
		}

		return(returnData);
	}
}
