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
// $Id: NameServiceInterface.java 170 2008-03-25 18:59:26Z jhalliday $

package org.jboss.dtf.testframework.nameservice;

import org.jboss.dtf.testframework.nameservice.*;

/**
 * The interface for a simple NameService.
 */
public interface NameServiceInterface extends java.rmi.Remote
{
	/**
	 * Binds a string name to an object reference.  If the name is already bound then an
	 * exception will be thrown.
	 * @param name The string name to bind the object to.
	 * @param obj The object reference to bind.
	 * @exception NameAlreadyBound Thrown if the name is
	 * already bound to an object reference
	 */
	public void bindReference( String 	name,
							   Object	obj ) throws java.rmi.RemoteException, NameAlreadyBound;

	/**
	 * Binds a string name to an object reference.  If the name is already bound then its
	 * binding is altered.
	 * @param name The string name to bind the object to.
	 * @param obj The object reference to bind.

	 */
	public void rebindReference( String 	name,
							     Object		obj ) throws java.rmi.RemoteException;

	/**
	 * Unbinds a string name from an object reference.
	 * @param name The string name to unbind.
	 * @exception NameNotBound Thrown if the name is not
	 * bound to an object reference.
	 */
	public void unbindReference( String name ) throws java.rmi.RemoteException, NameNotBound;

	/**
	 * Looks for the object reference which bound to the given name
	 * @param name The string name to look up.
	 * @exception NameNotBound Thrown if the name is not
	 * bound to an object reference.
	 */
	public Object lookup( String name ) throws java.rmi.RemoteException, NameNotBound;

	/**
	 * Retrieves a list of object reference which are bound in the given directory
	 * @param directory The directory to look within
	 * @returns An array of object reference names that exist with the given directory.
	 */
	public String[] lookupNames( String directory ) throws java.rmi.RemoteException, NameNotBound;
}
