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
package org.jboss.dtf.testframework.coordinator;

import org.jboss.dtf.testframework.testnode.RunUID;

public interface CoordinatorInterface extends java.rmi.Remote
{
    /**
	 * Initialises the coordinator retrieving the test definitions from the given URL and
	 * and the test selections from the given selection XML file
	 *
	 * @param testDefinitionURL The URL of a test definition XML file.
	 * @param testSelection The URL of a test selection XML file.
	 * @param softwareVersion A textual version id for the software to be tested.
	 * @param distributionList An email distribution list which will be sent the results of this test run on completion.
     * @return The run id. of the newly created test run.
     * @throws CoordinatorBusy The coordinator is already running tests.
     * @throws InvalidDefinitionFile The test definition file or the test selection file is not valid.
     * @throws java.net.MalformedURLException The test definition or the test selection file has a malformed URL.
	 */
    public RunUID initialiseTestRun(String testDefinitionURL, String testSelectionURL, String softwareVersion, String distributionList, boolean waitTillComplete) throws java.rmi.RemoteException, CoordinatorBusy, InvalidDefinitionFile, java.net.MalformedURLException;

    /**
     * Generates a coordinator descriptor for this coordinator.
     *
     * @returns This coordinators CoordinatorDescriptor
     * @throws java.rmi.RemoteException
     */
    public CoordinatorDescriptor getDescriptor() throws java.rmi.RemoteException;
}
