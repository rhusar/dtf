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
/**
 * Arjuna Technologies Ltd.
 *
 * @author Richard A. Begg
 */

package org.jboss.dtf.testframework.dtfweb;

import java.util.Hashtable;
import java.util.HashSet;
import java.util.Set;

public class GroupSelection extends Hashtable
{
    private String      _groupName = null;
    private HashSet     _selection = new HashSet();
    private String      _fullGroupName = null;
    private boolean     _expanded = false;

    public GroupSelection(Hashtable table, String groupName, String fullGroupName)
    {
        _groupName = groupName;
        _fullGroupName = fullGroupName;
    }

    public String getGroupName()
    {
        return _groupName;
    }

    public String getFullGroupName()
    {
        return _fullGroupName;
    }

    public boolean isSelected(String key)
    {
        System.out.println("Testing selection for '"+key+": "+_selection+": "+_selection.contains(key));
        return _selection.contains(key);
    }

    public boolean isExpanded()
    {
        return _expanded;
    }

    public void setExpanded(boolean expanded)
    {
        _expanded = expanded;
    }

    public void clearSelections()
    {
        _selection.clear();
    }

    public void setSelected(String key, boolean selected)
    {
        System.out.println("Group '"+getFullGroupName()+"' key '"+key+"' expanded="+selected);
        if ( selected )
        {
            _selection.add( key );
        }
        else
        {
            _selection.remove( key );
        }
    }
}
