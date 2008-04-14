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
package org.jboss.dtf.testframework.dtfweb.mydtf;

import org.jboss.dtf.testframework.dtfweb.StoredTestDefs;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

/*
 * Copyright (C) 1998, 1999, 2000, 2001, 2002, 2003
 *
 * Arjuna Technologies Ltd.
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: MyDTF.java 170 2008-03-25 18:59:26Z jhalliday $
 */

public class MyDTF
{
	private final static String MYDTF_COOKIE_NAME = "MyDTF.id";
	private final static int MS_IN_A_SECOND = 1000;

	private static Hashtable	_userDTFs = new Hashtable();
	private static long			_userId = 0;

	private ArrayList	_testDefs = new ArrayList();
	private long		_startTime;
	private Cookie		_cookie;
    private ArrayList	_softwareVersions = new ArrayList();

	private MyDTF(Cookie ck)
	{
		// Make the constructore Private
		_startTime = System.currentTimeMillis();
		_cookie = ck;
	}

	public boolean isDead()
	{
		return (( System.currentTimeMillis() - _startTime )/MS_IN_A_SECOND) >= _cookie.getMaxAge();
	}

	public boolean contains(StoredTestDefs storedTestDef)
	{
		return _testDefs.contains(storedTestDef);
	}

	public void addRunSoftwareVersion(String softwareVersion)
	{
		_softwareVersions.add(softwareVersion);
	}

	public void removeRunSoftwareVersion(String softwareVersion)
	{
		_softwareVersions.remove(softwareVersion);
	}

	public boolean containsSoftwareVersion(String softwareVersion)
	{
		return _softwareVersions.contains(softwareVersion);
	}

	private String getUserId()
	{
		return _cookie.getValue();
	}

	public ArrayList getSoftwareVersions()
	{
     	return _softwareVersions;
	}

	public void addStoredTestDefs(int id)
	{
        _testDefs.add( StoredTestDefs.getStoredTestDefs(id) );
	}

	public void removeStoredTestDefs(int id)
	{
		_testDefs.remove( StoredTestDefs.getStoredTestDefs(id) );
	}

	public ArrayList getStoredTestDefs()
	{
		return _testDefs;
	}

	public static MyDTF getMyDTF(HttpServletRequest request, HttpServletResponse response)
	{
		Cookie[] cookies = request.getCookies();
		MyDTF dtf = null;

		if ( cookies != null )
		{
		for (int count=0;count<cookies.length;count++)
		{
			if ( cookies[count].getName().equals(MYDTF_COOKIE_NAME) )
			{
				String userId = cookies[count].getValue();

				dtf = (MyDTF)_userDTFs.get(userId);
			}
		}
		}

		if ( dtf == null )
		{
			synchronized(_userDTFs)
			{
				Cookie c = new Cookie(MYDTF_COOKIE_NAME, ""+System.currentTimeMillis()+_userId++);
				c.setMaxAge(60 * 60 * 24 * 365);
				c.setPath("/");
				dtf = new MyDTF(c);
				_userDTFs.put(c.getValue(), dtf);
				response.addCookie(c);
			}
		}

		/** Clean up current cookies **/
		Iterator myDTFIterator = _userDTFs.values().iterator();

		while ( myDTFIterator.hasNext() )
		{
			MyDTF myDTF = (MyDTF)myDTFIterator.next();

			if ( myDTF.isDead() )
			{
				_userDTFs.remove(myDTF.getUserId());
			}
		}

		return dtf;
	}
}
