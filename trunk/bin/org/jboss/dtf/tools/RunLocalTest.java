package org.jboss.dtf.tools;/*
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
 * $Id: RunLocalTest.java 170 2008-03-25 18:59:26Z jhalliday $
 */

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class RunLocalTest extends Thread
{
    protected InputStream _in = null;
    protected boolean _out = false;

    public RunLocalTest(InputStream in, boolean out)
    {
        _in = in;
        _out = out;
    }

    public void run()
    {
        try
        {
            BufferedInputStream in = new BufferedInputStream(_in);
            byte[] buffer = new byte[32768];
            int bytesRead;

            while ((bytesRead = in.read(buffer)) != -1)
            {
                OutputStream out = _out ? System.out : System.err;
                out.write(buffer, 0, bytesRead);
            }

            in.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void main(String[] args)
    {
        try
        {
            Runtime rt = Runtime.getRuntime();
            Process p = rt.exec("java " + args[0]);

            RunLocalTest rlt = new RunLocalTest(p.getInputStream(), true);
            RunLocalTest rlt2 = new RunLocalTest(p.getErrorStream(), false);

            rlt.start();
            rlt2.start();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
