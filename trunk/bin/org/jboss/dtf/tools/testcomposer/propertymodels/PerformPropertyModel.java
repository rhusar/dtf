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
 * $Id: PerformPropertyModel.java 170 2008-03-25 18:59:26Z jhalliday $
 */

package org.jboss.dtf.tools.testcomposer.propertymodels;

import org.jboss.dtf.tools.testcomposer.controls.TaskControl;
import org.jboss.dtf.tools.testcomposer.controls.ParametersType;


public class PerformPropertyModel extends TaskPropertiesTableModel
{
    private final static String[] PROPERTY_NAMES = { "Task Name:", "Parameters:", "JVM Parameters:", "Location:", "Timeout:" };
    private final static boolean[] PROPERTY_IS_EDITABLE = { true, true, true, true, true };
    private final static int      CLASSNAME_PROPERTY_INDEX = 0;
    private final static int      PARAMETERS_PROPERTY_INDEX = 1;
    private final static int      JVM_PARAMETERS_PROPERTY_INDEX = 2;
    private final static int      LOCATION_PROPERTY_INDEX = 3;
    private final static int      TIMEOUT_PROPERTY_INDEX = 4;

    public PerformPropertyModel(TaskControl tc)
    {
        super(tc);
    }

    public int getPropertyCount()
    {
        return PROPERTY_NAMES.length;
    }

    /**
     * Return the name of the property at this position
     */
    protected String getPropertyName(int propertyIndex)
    {
        return PROPERTY_NAMES[propertyIndex];
    }

    /**
     * Return the value of the property at this position.
     */
    protected Object getPropertyValue(int propertyIndex)
    {
        switch(propertyIndex)
        {
            case CLASSNAME_PROPERTY_INDEX :
                return getAssociatedTaskControl().getTaskName();
            case LOCATION_PROPERTY_INDEX :
                return getAssociatedTaskControl().getLocationParameter();
            case TIMEOUT_PROPERTY_INDEX :
                return ""; //getAssociatedTaskControl().getTimeout();
            case PARAMETERS_PROPERTY_INDEX :
                return ParametersType.getParametersType(getAssociatedTaskControl().getParameters());
            case JVM_PARAMETERS_PROPERTY_INDEX :
                return ParametersType.getParametersType(getAssociatedTaskControl().getJVMParameters());
        }

        return "##ERROR##";
    }

    public boolean isPropertyEditable(int rowIndex)
    {
        return PROPERTY_IS_EDITABLE[rowIndex];
    }
}
