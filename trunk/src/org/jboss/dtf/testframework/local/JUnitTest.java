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
 *
 * $Id$
 */
package org.jboss.dtf.testframework.local;

import junit.framework.TestResult;
import org.jboss.dtf.testframework.coordinator2.TestDefinition;

/**
 * An adaptor class that  turns a DTF test into a JUnit Test. Handy for integration with things
 * that understand JUnit but not DTF. See JUnitTestSuite for most of the info.
 *
 * @author Jonathan Halliday (jonathan.halliday@redhat.com)
 */
public class JUnitTest implements junit.framework.Test
{
    LocalTestManager localTestManager;
    TestDefinition testDefinition;

    public JUnitTest(LocalTestManager localTestManager, TestDefinition testDefinition) {
        this.localTestManager = localTestManager;
        this.testDefinition = testDefinition;
    }

    public int countTestCases()
    {
        return 1;
    }

    public void run(TestResult testResult)
    {
        testResult.startTest(this);

        try {
            localTestManager.executeTest(testDefinition);
        } catch(Throwable t) {
            testResult.addError(this, t);
        }

        testResult.endTest(this);
    }
}
