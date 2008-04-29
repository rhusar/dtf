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
 * $Id: MySQL.java 170 2008-03-25 18:59:26Z jhalliday $
 */
package org.jboss.dtf.testframework.dbmanager.plugins;

import org.jboss.dtf.testframework.dbmanager.DBManagerPlugin;
import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import com.mysql.jdbc.jdbc2.optional.MysqlConnectionPoolDataSource;

import javax.sql.DataSource;

public class MySQL implements DBManagerPlugin
{
    public DataSource getDataSource(String url, String username, String password)
    {
        MysqlConnectionPoolDataSource datasource = new MysqlConnectionPoolDataSource();

        String portStr = null;
        String databaseName = null;
        String hostname = url.substring( 0, url.indexOf(':') );

        if ( hostname != null )
        {
            datasource.setServerName( hostname );
        }

        url = url.substring( url.indexOf(':') + 1 );

        if ( url.indexOf(':') != -1 )
        {
            portStr = url.substring( 0, url.indexOf(':') );
            url = url.substring( url.indexOf(':') + 1 );
        }

        if ( portStr != null )
        {
            datasource.setPort( Integer.parseInt( portStr ) );
        }

        if ( url.length() > 0 )
        {
            databaseName = url;
            url = "";
        }

        if ( databaseName != null )
        {
            datasource.setDatabaseName( databaseName );
        }

        if ( username != null )
        {
            datasource.setUser( username );
        }

        if ( password!= null )
        {
            datasource.setPassword( password );
        }

        return(datasource);
    }
}
