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
 * $Id: OptionParser.java 170 2008-03-25 18:59:26Z jhalliday $
 */
package org.jboss.dtf.testframework.utils;

import java.util.StringTokenizer;
import java.util.Arrays;
import java.util.Hashtable;

public final class OptionParser
{
    private Hashtable   _stringValueMap = null;

    /**
     * Creates an option parser where strings[index] has a value of values[index].
     *
     * @param strings The string tokens.
     * @param values The string token's values.
     */
    public OptionParser(String[] strings, long[] values) throws OptionParserException
    {
        if ( strings.length != values.length )
        {
            throw new OptionParserException("String array length does not match values array length");
        }

        _stringValueMap = new Hashtable();

        for (int count=0;count<strings.length;count++)
        {
            _stringValueMap.put(strings[count], new Long(values[count]));
        }
    }

    /**
     * Parse a string containing tokens delimited by | (binary OR) and return the
     * long value.
     *
     * @param text The string to parse.
     * @return The string token's values OR'd together.
     */
    public long parse(String text) throws OptionParserException
    {
        long value = 0;
        StringTokenizer st = new StringTokenizer(text, "|");

        while (st.hasMoreTokens())
        {
            String token = st.nextToken().trim();

            Long tokenValue = (Long)_stringValueMap.get(token);

            if (tokenValue == null)
            {
                throw new OptionParserException("Undefined element '"+token+"' found in option string");
            }

            value |= tokenValue.longValue();
        }

        return(value);
    }
}
