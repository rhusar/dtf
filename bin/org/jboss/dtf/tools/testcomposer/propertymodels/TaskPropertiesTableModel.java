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
 * $Id: TaskPropertiesTableModel.java 170 2008-03-25 18:59:26Z jhalliday $
 */

package org.jboss.dtf.tools.testcomposer.propertymodels;

import org.jboss.dtf.tools.testcomposer.controls.TaskControl;

import javax.swing.table.AbstractTableModel;

public abstract class TaskPropertiesTableModel extends AbstractTableModel
{
    private static final String[]       COLUMN_NAMES = { "Property", "Value" };
    private static final int            PROPERTY_COLUMN_INDEX = 0;
    private static final int            VALUE_COLUMN_INDEX = 1;

    private TaskControl _associateTaskControl = null;

    public TaskPropertiesTableModel(TaskControl tc)
    {
        _associateTaskControl = tc;
    }

    /**
     *  Returns false.  This is the default implementation for all cells.
     *
     *  @param  rowIndex  the row being queried
     *  @param  columnIndex the column being queried
     *  @return false
     */
    public final boolean isCellEditable(int rowIndex, int columnIndex)
    {
        switch(columnIndex)
        {
            case VALUE_COLUMN_INDEX :
                return isPropertyEditable( rowIndex );
        }

        return(false);
    }

    public abstract boolean isPropertyEditable( int rowIndex );

    /**
     *  Returns <code>Object.class</code> regardless of <code>columnIndex</code>.
     *
     *  @param columnIndex  the column being queried
     *  @return the Object.class
     */
    public Class getColumnClass(int columnIndex)
    {
        return getValueAt(VALUE_COLUMN_INDEX, columnIndex).getClass();
    }

    public final TaskControl getAssociatedTaskControl()
    {
        return(_associateTaskControl);
    }

    /**
     *  Returns a default name for the column using spreadsheet conventions:
     *  A, B, C, ... Z, AA, AB, etc.  If <code>column</code> cannot be found,
     *  returns an empty string.
     *
     * @param column  the column being queried
     * @return a string containing the default name of <code>column</code>
     */
    public final String getColumnName(int column)
    {
         return COLUMN_NAMES[column];
    }

    /**
     * Returns the number of rows in the model. A
     * <code>JTable</code> uses this method to determine how many rows it
     * should display.  This method should be quick, as it
     * is called frequently during rendering.
     *
     * @return the number of rows in the model
     * @see #getColumnCount
     */
    public final int getRowCount()
    {
        return(getPropertyCount());
    }

    public abstract int getPropertyCount();

    /**
     * Returns the number of columns in the model. A
     * <code>JTable</code> uses this method to determine how many columns it
     * should create and display by default.
     *
     * @return the number of columns in the model
     * @see #getRowCount
     */
    public final int getColumnCount()
    {
        return COLUMN_NAMES.length;
    }

    /**
     * Returns the value for the cell at <code>columnIndex</code> and
     * <code>rowIndex</code>.
     *
     * @param	rowIndex	the row whose value is to be queried
     * @param	columnIndex 	the column whose value is to be queried
     * @return	the value Object at the specified cell
     */
    public final Object getValueAt(int rowIndex, int columnIndex)
    {
        switch (columnIndex)
        {
            case PROPERTY_COLUMN_INDEX :
                return getPropertyName(rowIndex);
            case VALUE_COLUMN_INDEX :
                return getPropertyValue(rowIndex);
        }

        return null;
    }

    /**
     * Return the name of the property at this position
     */
    protected abstract String getPropertyName(int propertyIndex);


    /**
     * Return the value of the property at this position.
     */
    protected abstract Object getPropertyValue(int propertyIndex);


}
