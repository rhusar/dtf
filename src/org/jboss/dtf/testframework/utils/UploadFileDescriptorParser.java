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
// $Id: UploadFileDescriptorParser.java 170 2008-03-25 18:59:26Z jhalliday $

package org.jboss.dtf.testframework.utils;

import org.w3c.dom.Element;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.StringTokenizer;

public class UploadFileDescriptorParser
{
    private final static int    CVS_MODULE = 0,
                                CVS_TAG = 1,
                                FOR_PRODUCT = 2,
                                OS_BUILT_ON = 3,
                                DATE_TIME_BUILT = 4,
                                COLON = 5,
                                FILENAME = 6;

    public static UploadFileDescriptions parseUploadFileDescriptor(String descURL) throws Exception
    {
        UploadFileDescriptions ufd = new UploadFileDescriptions();
        /*
         * Open an input stream to the descriptor file at the given URL
         */
        try
        {
            System.out.println("Retrieving file descriptors from '"+descURL+"'");
            URL url = new URL(descURL);

            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
            String inLine = null;
            String  cvsModule = "",
                    cvsTag = "",
                    product = "",
                    os = "",
                    dateTime = "",
                    filename = "";

            /*
             * Read a line from the descriptor file
             */
            while ( (inLine = in.readLine()) != null)
            {
                /*
                 * Parse the descriptor string
                 */
                StringTokenizer tokenizer = new StringTokenizer(inLine);

                boolean skipTag = (tokenizer.countTokens() < (FILENAME+1));
                int tokenId = 0;
                while ( (tokenizer.hasMoreTokens()) && (tokenId<=FILENAME) )
                {
                    String token = tokenizer.nextToken();
                    switch (tokenId++)
                    {
                        case CVS_MODULE:
                            cvsModule = token;
                            break;
                        case CVS_TAG:
                            cvsTag = token;
                            break;
                        case FOR_PRODUCT:
                            product = token;
                            break;
                        case OS_BUILT_ON:
                            os = token;
                            break;
                        case DATE_TIME_BUILT:
                            dateTime = token;
                            break;
                        case COLON:
                            if (!token.equals(":"))
                            {
                                throw new Exception("Parsing error while parsing '"+descURL+"'");
                            }
                            break;
                        case FILENAME:
                            filename = token;
                            break;
                    }

                    if ((skipTag) && (tokenId == CVS_TAG))
                        tokenId = FOR_PRODUCT;
                }
                ufd.addFile(cvsModule, cvsTag, product, os, dateTime, filename);
            }
        }
        catch (java.net.MalformedURLException e)
        {
            System.err.println("Malformed URL Exception: '"+descURL+"'");
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
        }

        return(ufd);
    }
}
