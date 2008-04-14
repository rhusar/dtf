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
package org.jboss.dtf.tools.bugreporter;

import org.jboss.dtf.testframework.nameservice.NameServiceInterface;
import org.jboss.dtf.testframework.nameservice.NameNotBound;
import org.jboss.dtf.testframework.coordinator2.Coordinator;
import org.jboss.dtf.testframework.coordinator2.CoordinatorInterface;
import org.jboss.dtf.testframework.coordinator2.CoordinatorIdleException;
import org.jboss.dtf.testframework.serviceregister.ServiceRegisterInterface;
import org.jboss.dtf.testframework.serviceregister.ServiceRegister;
import org.jboss.dtf.testframework.testnode.TestNodeInterface;
import org.jboss.dtf.testframework.dtfweb.utils.ByteArrayDataSource;

import javax.mail.*;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeBodyPart;
import javax.activation.DataHandler;
import java.io.*;
import java.rmi.Naming;
import java.util.jar.JarOutputStream;
import java.util.jar.JarEntry;
import java.util.Properties;
import java.util.Date;

/*
 * Copyright (C) 1998, 1999, 2000, 2001, 2002, 2003
 *
 * Arjuna Technology Ltd.
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: BugReporter.java 170 2008-03-25 18:59:26Z jhalliday $
 */

public class BugReporter
{
	private final static String LOG_FILENAME_SUFFIX = ".log";

    private static void copyFileIntoReport(File file, BufferedOutputStream out) throws Exception
	{
		out.write(("\n\n----------------------------------------------------------------------\n").getBytes());
		out.write(("Log file appended: "+file.getAbsolutePath()+"\n").getBytes());
		out.write(("----------------------------------------------------------------------\n").getBytes());

		BufferedInputStream in = new BufferedInputStream(new FileInputStream(file));
		byte[] buffer = new byte[32768];
		int bytesRead;

		while ( ( bytesRead = in.read(buffer)) != -1 )
		{
			out.write(buffer, 0, bytesRead);
		}

		in.close();
	}

	private static void copyIntoJar(JarOutputStream jar, File bugReportFile) throws Exception
	{
		BufferedInputStream in = new BufferedInputStream(new FileInputStream(bugReportFile));
		byte[] buffer = new byte[32768];
		int bytesRead;
		long pos = 0;
		long size = bugReportFile.length();

		while ( ( bytesRead = in.read(buffer)) != -1 )
		{
			jar.write(buffer, 0, bytesRead);
			pos += bytesRead;

			System.out.print("Progress:"+(int)(((float)pos/size)*100)+"% complete                             \r");
		}

		in.close();
	}

	private static void appendLogFiles(BufferedOutputStream out, File dir) throws Exception
	{
		File[] files = dir.listFiles();

		for (int count=0;count<files.length;count++)
		{
			if ( files[count].getName().endsWith(LOG_FILENAME_SUFFIX) )
			{
				System.out.println("Appending "+files[count].getName());
            	copyFileIntoReport(files[count], out);
			}
			else
			if ( files[count].isDirectory() )
			{
				appendLogFiles(out, files[count]);
			}
		}
	}

	private static void appendFrameworkInformation(NameServiceInterface nameService, BufferedOutputStream out) throws Exception
	{
		out.write(("\n\n----------------------------------------------------------------------\n").getBytes());
		out.write(("Distributed Testing Framework Status\n").getBytes());
		out.write(("----------------------------------------------------------------------\n").getBytes());

		if ( nameService == null )
		{
			out.write(("Name service was not bound - cannot append framework information\n").getBytes());
		}
		else
		{
			String coordStatus = "Coordinator Status:";

			try
			{
				CoordinatorInterface coord = (CoordinatorInterface)nameService.lookup(Coordinator.COORDINATOR_NAME_SERVICE_NAME);

				coordStatus += "<Responding>";
			}
			catch (NameNotBound e)
			{
				coordStatus += "<Not Responding>";
			}

			out.write( (coordStatus+"\n").getBytes());

			String srStatus = "Service Registry:";
            ServiceRegisterInterface sri = null;

			try
			{
				sri = (ServiceRegisterInterface)nameService.lookup(ServiceRegister.SERVICE_REGISTER_NAME_SERVICE_ENTRY);

				srStatus += sri == null ? "<Not Responding>" : "<Responding>";
			}
			catch (NameNotBound e)
			{
				srStatus += "<Not Responding>";
			}

			out.write( (srStatus+"\n").getBytes() );

			if ( sri != null )
			{
				TestNodeInterface[] tni = sri.getRegister();

				out.write( ("Found "+tni.length+" testnode(s)\n").getBytes() );

				for (int count=0;count<tni.length;count++)
				{
					try
					{
						out.write( ("Testnode ["+count+"]: Name = "+tni[count].getName()+", Hostname = "+tni[count].getHostAddress()+"\nTask List:\n").getBytes());

						String[] atl = tni[count].getActiveTaskList();

						for (int tCount=0;tCount<atl.length;tCount++)
						{
							out.write( (atl[count]+"\n").getBytes() );
						}
					}
					catch (Exception e)
					{
						out.write( ("Failed to get information on testnode: "+e+"\n").getBytes());
					}
				}
			}
		}
	}

	private static void emailBugReport(String mailHost, String filename, String bugId)
	{
        try
		{
			Properties props = System.getProperties();
			props.put("mail.smtp.host", mailHost);

			// Get a Session object
			Session session = Session.getDefaultInstance(props, null);

			// construct the message
			MimeMessage msg = new MimeMessage(session);

			// Setup message
			msg.setFrom(new InternetAddress("foo@bar.com"));
			msg.setHeader("X-Mailer", "dtf-mail");
			msg.setSentDate(new Date());
			msg.setRecipient(Message.RecipientType.TO, new InternetAddress("foo@bar.com"));
			msg.setSubject("Bug report id: "+bugId);

			Multipart multipart = new MimeMultipart();
			BodyPart messageBodyPart = new MimeBodyPart();

			/** Add output body part **/
			messageBodyPart.setDataHandler(new DataHandler(new ByteArrayDataSource(new FileInputStream(filename), "application/zip")));
			messageBodyPart.setFileName("bugreport.jar");
			multipart.addBodyPart(messageBodyPart);

			/** Put both parts in the message **/
			msg.setContent(multipart);

			// send the thing off
			Transport.send(msg);
		}
		catch (Exception e)
		{
			System.err.println("Failed to email bug report to DTF developer");
		}
	}

	public static void main(String[] args)
	{
		String dtfHome = System.getProperty("DTF_HOME");

		if ( dtfHome == null )
		{
			System.err.println("Environment variable DTF_HOME is not set");
			System.exit(1);
		}

		File dtfHomeFile = new File(dtfHome);

		if ( !dtfHomeFile.exists() )
		{
			System.err.println("Environment variable DTF_HOME exists but is invalid");
			System.exit(1);
		}

		String nameServiceURI = "//localhost:1094/NameService";
		String mailHost = "cheviot.ncl.ac.uk";

		for (int count=0;count<args.length;count++)
		{
			if (args[count].equals("-nameservice"))
			{
				nameServiceURI = args[count + 1];
				System.out.println("Name service now set to '"+nameServiceURI+"'");
			}
			else
			if (args[count].equals("-smtpserver"))
			{
				mailHost = args[count + 1];
				System.out.println("SMTP server now set to '"+mailHost+"'");
			}
		}

		try
		{
			File bugReportFile = new File("bugreport.txt");
			System.out.println("Generating bug report");

			NameServiceInterface nsi = null;

			try
			{
				nsi = (NameServiceInterface)Naming.lookup(nameServiceURI);
			}
			catch (Exception e)
			{
				// Ignore
			}

        	BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(bugReportFile));

			appendFrameworkInformation(nsi, out);

			appendLogFiles(out, dtfHomeFile);

			out.close();

			System.out.println("Compressing bug report");

            JarOutputStream jar = new JarOutputStream(new FileOutputStream("bugreport.jar"));
			JarEntry bugFile = new JarEntry(bugReportFile.getName());
			bugFile.setSize(bugReportFile.length());
			jar.putNextEntry(bugFile);
			copyIntoJar(jar, bugReportFile);
			jar.closeEntry();
			jar.close();

			System.out.println("Emailing bug report         ");
			String bugId = "DTFB"+Long.toHexString(System.currentTimeMillis());
			emailBugReport(mailHost, "bugreport.jar", bugId);

			System.out.println("The bug has been assigned the id: "+bugId);

			bugReportFile.deleteOnExit();
			new File("bugreport.jar").deleteOnExit();
		}
		catch (Exception e)
		{
			System.err.println("An error occurred when trying to produce the bug report: "+e);
		}
	}
}
