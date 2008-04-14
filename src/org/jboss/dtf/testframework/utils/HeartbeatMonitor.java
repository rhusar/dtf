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
// $Id: HeartbeatMonitor.java 170 2008-03-25 18:59:26Z jhalliday $

package org.jboss.dtf.testframework.utils;

import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.net.SocketException;
import java.util.HashMap;

public class HeartbeatMonitor extends Thread
{
	private DatagramSocket	_socket;
	private HashMap			_producers = new HashMap();
	private boolean			_stop = false;
	private long			_timeOutValue;
	private int				_port;

	public HeartbeatMonitor(int port, long timeOutMillis) throws IllegalArgumentException, SecurityException, UnknownHostException, SocketException
	{
		_socket = new DatagramSocket(port);
		_timeOutValue = timeOutMillis;
		_port = port;
	}

	public int getPort()
	{
		return(_port);
	}

	public void addProducer(String ipAddress, int serviceId)
	{
		_producers.put(ipAddress+":"+serviceId,new Long(System.currentTimeMillis()));
	}

	public boolean isProducerResponding(String ipAddress, int serviceId)
	{
		synchronized(_producers)
		{
			String key = ipAddress+":"+serviceId;
			long currentTime = System.currentTimeMillis();
			Long value = (Long)_producers.get(key);
			long timeOfLastResponse = 0;
			if (value != null)
			{
				timeOfLastResponse = value.longValue();
			}

			return((currentTime - timeOfLastResponse)<=_timeOutValue);
		}
	}

	public void startMonitor()
	{
		start();
	}

	public void run()
	{
		byte[] buffer = new byte[6];
		DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
		packet.setPort(_port);
		while (!_stop)
		{
			try
			{
				_socket.receive(packet);

				if (packet.getLength()>0)
				{
					String ipAddress = packet.getAddress().getHostAddress();

					synchronized(_producers)
					{
						String data = new String(buffer);
						data = data.trim();
						ipAddress += ":"+Short.parseShort(data,16);
						if (_producers.get(ipAddress)!=null)
						{
							_producers.put(ipAddress, new Long(System.currentTimeMillis()));
						}
					}
				}
			}
			catch (NumberFormatException e)
			{
			}
			catch (java.io.IOException e)
			{
				e.printStackTrace();
			}
		}
		System.out.println("Heartbeat Monitor stopped");
	}
}
