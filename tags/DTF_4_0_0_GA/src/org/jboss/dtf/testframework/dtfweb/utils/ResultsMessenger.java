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
package org.jboss.dtf.testframework.dtfweb.utils;

import javax.mail.*;
import javax.activation.*;
import javax.mail.internet.*;

import java.util.Properties;
import java.util.Date;

import org.jboss.dtf.testframework.dtfweb.*;
import org.jboss.dtf.testframework.dtfweb.EmailDetails;

public class ResultsMessenger
{
	private Message	_msg = null;
	private int _msgType = -1;

	public ResultsMessenger(String mailhost, int type) throws Exception
	{
		_msgType = type;

		try
		{
	    	Properties props = System.getProperties();
	    	props.put("mail.smtp.host", mailhost);

		    // Get a Session object
		    Session session = Session.getDefaultInstance(props, null);
			session.setDebug(true);

		    // construct the message
		    _msg = new MimeMessage(session);

			// Setup message
			_msg.setFrom(new InternetAddress("dtf@example.com"));
		    _msg.setHeader("X-Mailer", "dtf-mail");
		    _msg.setSentDate(new Date());
		}
		catch (Exception e)
		{
			throw e;
		}
    }

	public void setRunId(long id) throws Exception
	{
		try
		{
	    	_msg.setSubject("Results from test run "+id);
		}
		catch (Exception e)
		{
			throw e;
		}
	}

    public void addRecipient(String emailAddress) throws Exception
    {
		try
		{
    		_msg.addRecipients(Message.RecipientType.TO,InternetAddress.parse(emailAddress, false));
		}
		catch (Exception e)
		{
			throw e;
		}
	}

	public void send(java.net.URL url) throws Exception
	{
		try
		{
			Multipart multipart = new MimeMultipart();
            BodyPart messageBodyPart = new MimeBodyPart();

			/** Add output body part **/
			messageBodyPart.setDataHandler(new DataHandler(new ByteArrayDataSource(url.openStream(), "text/html")));
            multipart.addBodyPart(messageBodyPart);

			/** Put both parts in the message **/
			_msg.setContent(multipart);

			// send the thing off
	    	Transport.send(_msg);
		}
		catch (Exception e)
		{
			throw e;
		}
	}
}
