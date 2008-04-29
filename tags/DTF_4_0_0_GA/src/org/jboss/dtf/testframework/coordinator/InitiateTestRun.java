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

import org.jboss.dtf.testframework.nameservice.NameServiceInterface;
import org.jboss.dtf.testframework.serviceregister.ServiceRegisterInterface;
import org.jboss.dtf.testframework.coordinator2.CoordinatorInterface;
import org.jboss.dtf.testframework.coordinator2.CoordinatorBusyException;

import java.rmi.Naming;
import java.net.URL;

/**
 * Command line tool used to initiate a testrun.
 *
 * @author Richard A. Begg
 */
public class InitiateTestRun
{
    public static boolean initiate(String nameServiceURI, String testDefsURL, String selectionURL, String softwareVersion, String distributionList, boolean waitTillComplete, boolean runWhenPossible)
    {
        int retryCount = 0;
        int MAX_RETRIES = 10;
        int backOffPeriod = 250;

        try
        {
            NameServiceInterface nameService = (NameServiceInterface) Naming.lookup(nameServiceURI);

            ServiceRegisterInterface register = (ServiceRegisterInterface) nameService.lookup("/ServiceRegister");

            if (register == null)
            {
                System.out.println("Unable to resolve the service register");
                return(false);
            }

            System.out.println("Searching for a coordinator...");
            CoordinatorInterface coordinator = (CoordinatorInterface)nameService.lookup("Coordinator");

            coordinator.run(new URL( testDefsURL ), new URL( selectionURL ), distributionList, softwareVersion, waitTillComplete);
        }
        catch (CoordinatorBusyException e)
        {
            if ( runWhenPossible )
            {
                System.out.println("Coordinator busy, scheduled for later execution");
            }
            else
            {
                System.err.println("Coordinator busy - not scheduling");

                return false;
            }
        }
        catch (Exception e)
        {
            System.err.println("ERROR: " + e.toString());
            e.printStackTrace(System.err);
            return(false);
        }

        return(true);
    }

    public static void main(String args[])
    {
        if (args.length < 4)
        {
            System.out.println("InitiateTestRun Usage: org.jboss.dtf.testframework.coordinator.InitiateTestRun [URI of NameService] [url://TestDefs.xml] [url://Selection.xml] [Software Version] {-dist <distribution list>} {-wait} {-runwhenpossible}");
            System.out.println("\t-wait : wait for the test run to complete before returning");
			System.out.println("\t-runwhenpossible : if the coordinator is busy then run this test run when possible");
            System.out.println("\t-dist : specify the email distribution list that will receive the results (; separated)");
            System.exit(1);
        }

        boolean waitTillComplete = false;
		boolean runWhenPossible = false;
        String distributionList = "";

        for (int count=0;count<args.length;count++)
        {
            if (args[count].equalsIgnoreCase("-wait"))
            {
                waitTillComplete = true;
            }
			if (args[count].equalsIgnoreCase("-runwhenpossible"))
			{
				runWhenPossible = true;
			}
            if (args[count].equalsIgnoreCase("-dist"))
            {
                distributionList = args[count+1];
            }
        }

        if (initiate(args[0], args[1], args[2], args[3], distributionList, waitTillComplete, runWhenPossible))
        {
            System.exit(11);
        }
    }
}
