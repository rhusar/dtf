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
// $Id: ClientHandler.java 170 2008-03-25 18:59:26Z jhalliday $

package org.jboss.dtf.testframework.coordinator.resultscollator;

import java.net.Socket;
import java.io.ObjectInputStream;
import java.io.FileOutputStream;
import java.io.File;
import java.io.BufferedInputStream;

public class ClientHandler extends Thread
{
	private Socket 					_socket = null;
	private ObjectInputStream 	    _in = null;
	private String					_resDirectory = null;

	public ClientHandler(Socket sckt, String resultDirectory) throws java.io.IOException
	{
	    throw new Error("NOT USED!");
	}


	private final boolean readFile(String directory, String filename, ObjectInputStream in, long fileLength) throws java.io.IOException
	{
		byte[] buffer = new byte[32768];
		int byteRead = 0;
		long byteCount = 0;
		int expectedRead = buffer.length;
		File outFile = new File(directory);
		outFile.mkdirs();
		FileOutputStream out = new FileOutputStream(new File(outFile,filename));

		while ( (byteCount<fileLength) && ((byteRead = in.read(buffer,0,expectedRead)) != -1) )
		{
			out.write(buffer,0,byteRead);
			byteCount+=byteRead;

			expectedRead = ((byteCount + buffer.length) > fileLength) ? (int)(fileLength - byteCount) : buffer.length;
		}
		out.close();

		return byteRead==-1;
	}

	public void run()
	{
		/*
		 * Packet structure
		 *
		 * All strings are null terminated
		 *
		 * 2bytes		TestNode Service Id
		 * 4bytes       RunId
		 * 'n'byte(s)	Task Name
		 * 'n'byte(s)	Test Id
		 * 'n'byte(s)   Permutation Code
		 * 'n'byte(s)	Filename Extension
		 * 4bytes		Filelength
		 * 'n'byte(s)	File Data
		 *
		 */
		try
		{
			boolean finished = false;
			while (!finished)
			{
				int nodeId = _in.readShort();
				long runId = _in.readLong();
				String taskName = _in.readUTF();
				String testId = _in.readUTF();
				String permCode = _in.readUTF();
				String filenameExt = _in.readUTF();;
				long fileLength =  _in.readLong();

                String filename = taskName+"_"+filenameExt;
                String directory = _resDirectory+"/Run_"+runId+"/"+testId+"/"+permCode;
                String relativeDirectory = "./Run_"+runId+"/"+testId+"/"+permCode;
				readFile(directory, filename, _in, fileLength);

				HTMLIndexCreator.addResultToIndex(_resDirectory+"/Run_"+runId+"_index.html", runId, relativeDirectory+"/"+filename, nodeId, taskName, testId, permCode);

				finished = _in.readBoolean();
			}
			_in.close();
			_socket.close();
		}
		catch (java.io.IOException e)
		{
		    e.printStackTrace(System.err);
		}
	}
}
