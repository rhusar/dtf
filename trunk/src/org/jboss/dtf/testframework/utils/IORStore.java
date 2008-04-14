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
// $Id: IORStore.java 170 2008-03-25 18:59:26Z jhalliday $

package org.jboss.dtf.testframework.utils;

import org.jboss.dtf.testframework.nameservice.*;

import java.rmi.*;

public interface IORStore
{
	public void initialiseStore(String URI);

    public void storeIOR(String serverName, String serverIOR)  throws Exception;

    public void removeIOR(String serverName) throws Exception;

    public String loadIOR(String serverName) throws Exception;
}
