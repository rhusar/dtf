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
// $Id: ParameterPreprocessor.java 170 2008-03-25 18:59:26Z jhalliday $

package org.jboss.dtf.testframework.utils;

import java.util.HashMap;
import java.util.Properties;
import java.util.Hashtable;

/**
 * A preprocessor for parameters.  Takes a list of preprocessor name value pairs then processes string
 * and replaces the $(NAME) variable for the value.  e.g.
 *
 *          Name: 'DIRECTORY' | Value 'C:/TMP'
 *          Name: 'FILENAME'  | Value 'FILENAME.TXT'
 *
 *          '$(DIRECTORY)/$(FILENAME)' preproccesed would be 'C:/TMP/FILENAME.TXT'
 */
public class ParameterPreprocessor
{
	private static Hashtable    _replacements =  new Hashtable();

    public final static void clear()
    {
        _replacements.clear();
    }

    public final static void addReplacements(Hashtable t)
    {
        _replacements.putAll(t);
    }

    /**
     * Add a replacement to the replacements map.
     *
     * @param text The name of the replacement name/value pair.
     * @param replace The value of the replacement name/value pair.
     */
	public final static void addReplacement(String text, String replace)
	{
		synchronized(_replacements)
		{
			_replacements.put(text, replace);
		}
	}

    public final static String preprocessParameters(String text)
    {
        return(preprocessParameters(text,true));
    }

    public final static String[] preprocessParameters(String[] text)
    {
        for (int count=0;count<text.length;count++)
        {
            text[count] = preprocessParameters(text[count]);
        }

        return(text);
    }

	public final static String[] preprocessParameters(String[] text, boolean eraseVars)
	{
		for (int count=0;count<text.length;count++)
		{
			text[count] = preprocessParameters(text[count], eraseVars);
		}

		return(text);
	}

	/**
	 * Search for all $(****) variables and replace them with the correct information
     *
     * @param text The text to preprocess.
     * @return The preprocessed text.
	 */
    public static String preprocessParameters(String text, boolean eraseVars)
    {
        return preprocessParameters(_replacements, text, eraseVars);
    }

    public static String preprocessParameters(Hashtable sets, String text, boolean eraseVars)
	{
		boolean finished = false;
        int position = 0;

		while ( (!finished) && (text!=null) )
		{
			finished = true;
			if (text.substring(position).indexOf("$(")!=-1)
			{
				String key = text.substring(position+text.substring(position).indexOf("$(")+2);
				if (key.indexOf(")")!=-1)
				{
					key = key.substring(0,key.indexOf(")"));
					String replacement;

                    replacement = (String)sets.get(key);

					if (replacement == null)
					{
                        if (eraseVars)
                        {
                            replacement = "";
                        }
                        else
                        {
                            replacement = null;
                            position = position + text.substring(position).indexOf("$(") + 2;
                        }
					}

                    if (replacement != null)
                    {
                        text = text.substring(0,position+text.substring(position).indexOf("$(")) + replacement + text.substring(position+text.substring(position).indexOf("$(")+text.substring(position+ text.substring(position).indexOf("$(")).indexOf(")")+1);
                    }

					finished = false;
				}
			}
		}
		return(text);
	}
}
