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
// $Id: Action.java 170 2008-03-25 18:59:26Z jhalliday $

package org.jboss.dtf.testframework.coordinator;

import org.jdom.Element;

import java.util.ArrayList;

/**
 * This class is used to store information pertaining to an action
 * performed during a test.
 *
 * @author Richard A. Begg
 */
public abstract class Action
{
    /*
     * Location types
     */
	public final static int     LOCATION_INDEPENDENT = 0x00,
	                            LOCATION_DEPENDENT = 0x01,
	                            LOCATION_ALL = 0x02,
	                            LOCATION_SPECIFIC = 0x03;

	public final static String[]   LOCATION_STRINGS = { "INDEPENDENT","DEPENDENT","ALL" };

    /*
     * Action type field values
     */
	public static final int 	PERFORM_TASK = 1,
								START_TASK = 2,
								WAIT_FOR_TASK = 3,
								TERMINATE_TASK = 4;

    /*
     * The type of action this class represents
     */
	public int 			_actionType;

    /**
     * Retrieves the type of action this class represents.
     *
     * @return The type of action this class represents
     * (PERFORM_TASK, START_TASK, WAIT_FOR_TASK, TERMINATE_TASK)
     */
	public final int getType()
	{
		return(_actionType);
	}

	/**
	 * Retrieves the task id. associated with this action, if the action
	 * type doesn't have an associated task id. an exception is thrown.
	 *
	 * @return The associated task id
	 * @throws NoAssociatedData Thrown if there is no associated task id.
	 */
	public abstract String getAssociatedTaskId() throws NoAssociatedData;

	/**
	 * Retrieves the location value associated with this action, if the action
	 * type doesn't have an associated location an exception is thrown.
	 *
	 * @return The associated location value
	 * @throws NoAssociatedData Thrown if there is no associated task id.
	 */
	public abstract String getLocation() throws NoAssociatedData;

	/**
	 * Retrieves the location type value associated with this action, if the action
	 * type doesn't have an associated location an exception is thrown.
	 *
	 * @return The associated location type value
	 * @throws NoAssociatedData Thrown if there is no associated task id.
	 */
	public final int getLocationType() throws NoAssociatedData
	{
	    int returnValue = LOCATION_DEPENDENT;

        String location = getLocation();

        if ( ( location != null ) && ( location.length() > 0 ) )
            returnValue = LOCATION_SPECIFIC;

        for (int count=0;count<LOCATION_STRINGS.length;count++)
        {
            if (LOCATION_STRINGS[count].equalsIgnoreCase(location))
            {
                returnValue=count;
            }
        }

	    return(returnValue);
	}

    /**
     * Retrieves the parameters to be passed to this action, if no parameters are
     * associated with this action then an exception is thrown.
     *
     * @return The parameters to be passed to this action
     * @throws NoAssociatedData Thrown if there are no parameters for this action.
     */
	public abstract String[] getParameterList() throws NoAssociatedData;

    /**
     * Retrieves the parameters to be passed to the JVM that executes this action, if no parameters are
     * associated with this action then an exception is thrown.
     *
     * @return The parameters to be passed to the JVM
     * @throws NoAssociatedData Thrown if there are no parameters to pass to the JVM.
     */
	public abstract String[] getJVMParameterList() throws NoAssociatedData;

    /**
     * Retrieves the associated runtime task id., if one doesn't exist an
     * exception is thrown.
     *
     * @return The associated runtime task id.
     * @throws NoAssociatedData Thrown if there is no associated runtime task id.
     */
	public abstract String getAssociatedRuntimeTaskId() throws NoAssociatedData;

	/**
	 * Retrieves the name list for this action, throws an exception if no
	 * name list is associated with this action.
	 *
	 * @return The name list associated with this action.
	 * @throws NoAssociatedData Thrown if there is no associated name list.
	 */
	public abstract String getAssociatedNameList() throws NoAssociatedData;

    /**
     * Create an XML element which represents this action
     *
     * @return The XML element representing this action.
     */
    public abstract Element serializeToXML() throws NoAssociatedData;
}
