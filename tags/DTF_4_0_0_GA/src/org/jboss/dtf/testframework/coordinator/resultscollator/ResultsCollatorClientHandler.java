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
// $Id: ResultsCollatorClientHandler.java 170 2008-03-25 18:59:26Z jhalliday $

package org.jboss.dtf.testframework.coordinator.resultscollator;

import java.net.ServerSocket;
import java.net.Socket;
import java.io.*;

public final class ResultsCollatorClientHandler extends Thread
{
	private Socket 					_socket = null;
	private ObjectInputStream 	    _in = null;
	private String					_resDirectory = null;

	public ResultsCollatorClientHandler(Socket sckt, String resultDirectory) throws java.io.IOException
	{
	    System.out.println("Client handler created for socket "+sckt);
		_resDirectory = resultDirectory;
		_socket = sckt;
		_in = new ObjectInputStream(new BufferedInputStream(_socket.getInputStream()));
		this.setPriority(Thread.MIN_PRIORITY);
		start();
	}

    protected File readFile(String directory, String filename, ObjectInputStream in, long fileLength) throws java.io.IOException
    {
		int byteRead = 0;
		long byteCount = 0;
		byte[] buffer = new byte[32768];
		int expectedRead = ((byteCount + buffer.length) > fileLength) ? (int)(fileLength - byteCount) : buffer.length;
		File outFileDir = new File(directory);
		outFileDir.mkdirs();
        File outFile = new File(outFileDir, filename);
		FileOutputStream out = new FileOutputStream(outFile);

		while ( (byteCount<fileLength) && ((byteRead = in.read(buffer,0,expectedRead)) != -1) )
		{
			out.write(buffer,0,byteRead);
			byteCount+=byteRead;

			expectedRead = ((byteCount + buffer.length) > fileLength) ? (int)(fileLength - byteCount) : buffer.length;
		}
		out.close();

		return(outFile);
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
                String outputType = _in.readUTF();
				String filenameExt = outputType + _in.readUTF();
				long fileLength =  _in.readLong();

                String filename = taskName+"_"+filenameExt;
                String directory = _resDirectory+"/Run_"+runId+"/"+testId+"/"+permCode;
                String relativeDirectory = "./Run_"+runId+"/"+testId+"/"+permCode;

                File f = readFile(directory, filename, _in, fileLength);

                for (int count=0;count<ResultsCollator.Plugins.size();count++)
                {
                    try
                    {
                        ResultsCollatorPlugin plugin = (ResultsCollatorPlugin)ResultsCollator.Plugins.get(count);
                        plugin.processResults(runId, testId, taskName, permCode, outputType, (int)f.length(), new FileInputStream(f));

						plugin = null;
						System.gc();
                    }
                    catch (Throwable e)
                    {
                        System.err.println("ERROR - Failed to create results collator plugin '"+ResultsCollator.Plugins.get(count)+"'");
                        e.printStackTrace(System.err);
                    }
                }

				finished = _in.readBoolean();
			}
			_in.close();
			_socket.close();
		}
		catch (Exception e)
		{
		    e.printStackTrace(System.err);
		}
	}
}
