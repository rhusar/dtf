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
// $Id: CollatorReporter.java 170 2008-03-25 18:59:26Z jhalliday $

package org.jboss.dtf.testframework.coordinator.resultscollator;

import java.io.*;
import java.net.*;

import org.jboss.dtf.testframework.testnode.TaskResultsFilename;

public class CollatorReporter extends ResultsReporter
{

	public CollatorReporter(	String 	ipAddress,
							    int 	port,
							    short	serviceId) throws java.net.UnknownHostException,
													  java.io.IOException
    {
        super(ipAddress,port,serviceId);
    }

    public void sendResultDefinition(ResultDefinition resDef)
	{
		/*
		 * Packet structure
		 *
		 * All strings are null terminated
		 *
		 * 2bytes		TestNode Service Id
		 * 'n'byte(s)	Task Name
		 * 'n'byte(s)	Test Id
		 * 'n'byte(s)   Permutation Code
 		 * 'n'byte(s)	Filename Extension
		 * 4bytes		Filelength
		 * 'n'byte(s)	File Data
		 *
		 */

		/*
		 * Create socket and connect to the given ip address on the given port
		 * then open up an output stream
		 */
		try
		{
    	    Socket 					sckt = null;
    	    ObjectOutputStream	    out = null;
    	    // socket connection to the ResultsCollatorClientHandler
    		sckt = new Socket(_ipAddress, _port);
    		out = new ObjectOutputStream(new BufferedOutputStream(sckt.getOutputStream()));

    		// the directory holding the results on TestNode
    		File resultsDir = new File(resDef._directory);
    		File[] resultFiles = resultsDir.listFiles();
    		boolean firstTimeThru = true, sendFinishedBoolean = false;
    		// get all the files in this directory, and for each file
    		// which starts with task_<our task id>
    		for (int count=0;count<resultFiles.length;count++)
    		{
    			try
    			{
    			    String filename = resultFiles[count].getName();

    			    if ( (filename.startsWith("task_"+resDef._taskId.getTaskId())) || (filename.startsWith("cvrg_")) )
    			    {
                        if ( (!firstTimeThru) && (sendFinishedBoolean) )
                        {
                		    out.writeBoolean( false );
                			out.flush();
                	    }
    			        sendFinishedBoolean = false;

        				out.writeShort(_serviceId);
        				out.writeLong(resDef._runId);
        				out.writeUTF(resDef._taskName);
        				out.writeUTF(resDef._testId);
        				out.writeUTF(resDef._permutationCode);
                        out.writeUTF(TaskResultsFilename.extractTaskOutputType(filename));
        				out.writeUTF("_"+resDef._taskId.getTaskId()+"_"+resDef._taskId.getTestId());
        				writeFile(out, resultFiles[count]);
						resultFiles[count].delete();
        				sendFinishedBoolean = true;
        		    }
    			}
    			catch (java.io.IOException e)
    			{
    				System.out.println("Error while sending results file to ResultsCollator!");
    			}

                firstTimeThru = false;
    		}

    		if (sendFinishedBoolean)
    		{
                out.writeBoolean( true );
            }

        	out.flush();

    		out.close();
    		sckt.close();
    	}
    	catch (UnknownHostException e)
    	{
    	    System.out.println("ERROR: Unknown host '"+_ipAddress+"'");
    	}
        catch (IOException e)
        {
            System.out.println("ERROR: "+e.toString());
        }
	}

	private final void writeFile(ObjectOutputStream out, File file) throws java.io.IOException
	{
		int totalCount = 0;
		FileInputStream in = new FileInputStream(file);
		byte[] buffer = new byte[32768];
		int bytesRead;

    	out.writeLong(file.length());

		while ( (bytesRead = in.read(buffer)) != -1)
		{
			out.write(buffer,0,bytesRead);
			totalCount += bytesRead;
		}
		in.close();
		if (totalCount != file.length())
		    throw new java.io.IOException("Number of bytes read doesn't match file length");
	}
}
