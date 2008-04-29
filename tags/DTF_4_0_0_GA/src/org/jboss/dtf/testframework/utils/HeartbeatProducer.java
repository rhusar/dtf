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
// $Id: HeartbeatProducer.java 170 2008-03-25 18:59:26Z jhalliday $

package org.jboss.dtf.testframework.utils;

import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.net.SocketException;
import java.util.HashMap;

public class HeartbeatProducer extends Thread
{
	private DatagramSocket	_socket;
	private boolean			_stop = false;
	private long			_interval;
	private short			_serviceId;

	public HeartbeatProducer(short serviceId, String heartbeatDetails, int interval) throws IllegalArgumentException, SecurityException, UnknownHostException, SocketException
	{
		_serviceId = serviceId;

		if ( (heartbeatDetails.indexOf(":")!=-1) && (heartbeatDetails.indexOf(":")<heartbeatDetails.length()) )
		{
			String ipAddress = heartbeatDetails.substring(0,heartbeatDetails.indexOf(":"));
			int port = Integer.parseInt(heartbeatDetails.substring(heartbeatDetails.indexOf(":")+1));

			_socket = new DatagramSocket();
			_socket.connect(InetAddress.getByName(ipAddress),port);

			_interval = interval;
		}
		else
			throw new UnknownHostException();
	}

	public HeartbeatProducer(short serviceId, String ipAddress, int port, int interval) throws IllegalArgumentException, SecurityException, UnknownHostException, SocketException
	{
		_serviceId = serviceId;
		_socket = new DatagramSocket();
		_socket.connect(InetAddress.getByName(ipAddress),port);

		_interval = interval;
	}

	public final void startHeartbeat()
	{
		_stop = false;
		start();
	}

	public final void stopHeartBeat()
	{
		_stop = true;
	}

	public final void run()
	{
		String idText = Integer.toHexString(new Short(_serviceId).intValue());
		byte[] buffer = idText.getBytes();
		DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

		while (!_stop)
		{
			try
			{
				_socket.send(packet);

				Thread.sleep(_interval);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}

		System.out.println("Heartbeat stopped.");
	}
}
