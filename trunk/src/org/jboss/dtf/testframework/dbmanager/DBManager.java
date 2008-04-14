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
/*
 * Copyright (C) 2001, 2002,
 *
 * Hewlett-Packard Arjuna Labs,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: DBManager.java 170 2008-03-25 18:59:26Z jhalliday $
 */
package org.jboss.dtf.testframework.dbmanager;

import javax.naming.InitialContext;
import javax.naming.Context;
import java.util.Hashtable;

public class DBManager
{
    public static void main(String[] args)
    {
        String plugin = null;
        String username = null;
        String password = null;
        String url = null;

        if ( args.length == 0 )
        {
            System.out.println("Usage: DBManager [-plugin <classname>] [-url <db-url>] [-username <username>] [-password <password>]");
        }
        else
        {
            for (int count=0;count<args.length;count++)
            {
                if ( ( args[count].equals("-plugin") ) && ( count + 1 < args.length ) )
                {
                    plugin = args[count + 1];
                }
                else
                {
                    if ( ( args[count].equals("-username") ) && ( count + 1 < args.length ) )
                    {
                        username = args[count + 1];
                    }
                    else
                    {
                        if ( ( args[count].equals("-password") ) && ( count + 1 < args.length ) )
                        {
                            password = args[count + 1];
                        }
                        else
                        {
                            if ( ( args[count].equals("-url") ) && ( count + 1 < args.length ) )
                            {
                                url = args[count + 1];
                            }
                        }
                    }
                }
            }

            try
            {
                InitialContext ctxt = new InitialContext();

                DBManagerPlugin pluginImpl = (DBManagerPlugin)Class.forName(plugin).newInstance();
                ctxt.rebind( "jdbc/ResultsDB", pluginImpl.getDataSource(url, username, password) );

                System.out.println("DataSource Registered");
                System.out.println("Ready");
            }
            catch (Exception e)
            {
                System.err.println("Failed to initialise database connection");
                e.printStackTrace(System.err);
            }
        }
    }
}
