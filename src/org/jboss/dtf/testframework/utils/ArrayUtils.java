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
 * $Id: ArrayUtils.java 170 2008-03-25 18:59:26Z jhalliday $
 */
package org.jboss.dtf.testframework.utils;

public final class ArrayUtils
{
    public static String[] prependArray(String[] source, String[] additional)
    {
        String[] resultant = new String[source.length + additional.length];

        System.arraycopy(additional, 0, resultant, 0, additional.length);
        System.arraycopy(source, 0, resultant, additional.length, source.length);

        return(resultant);
    }

    public static String[] appendArray(String[] source, String[] additional)
    {
        String[] resultant = new String[source.length + additional.length];

        System.arraycopy(source, 0, resultant, 0, source.length);
        System.arraycopy(additional, 0, resultant, source.length, additional.length);

        return(resultant);
    }
}
