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
 * $Id: TestBrowserCellRenderer.java 170 2008-03-25 18:59:26Z jhalliday $
 */

package org.jboss.dtf.tools.testcomposer.frames;

import org.jboss.dtf.testframework.coordinator.TestDefinition;
import org.jboss.dtf.testframework.coordinator.TaskDefinition;

import javax.swing.*;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;

public class TestBrowserCellRenderer extends DefaultTreeCellRenderer
{
    private static ImageIcon _taskDefIcon;
    private static ImageIcon _testDefIcon;
    private static ImageIcon _testGroupIcon;

    public Component getTreeCellRendererComponent(  JTree tree,
                                                    Object value,
                                                    boolean sel,
                                                    boolean expanded,
                                                    boolean leaf,
                                                    int row,
                                                    boolean hasFocus)
    {
        super.getTreeCellRendererComponent( tree, value, sel,
                                            expanded, leaf, row,
                                            hasFocus);

        DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
        if (node.getUserObject() instanceof TestDefinition)
        {
            setIcon(_testDefIcon);
            setToolTipText("Full Name:" +((TestDefinition)node.getUserObject()).getFullId());
        }
        else
        {
            if (node.getUserObject() instanceof TaskDefinition)
            {
                setIcon(_taskDefIcon);
                setToolTipText("Classname:" +((TaskDefinition)node.getUserObject()).getClassName());
            }
            else
            {
                if (node.getUserObject() instanceof String)
                {
                    setIcon(_testGroupIcon);
                }

                setToolTipText(null); //no tool tip
            }
        }

        return this;
    }

    public static Image getTaskDefImage()
    {
        return(_taskDefIcon.getImage());
    }

    static
    {
        _taskDefIcon = new ImageIcon("images/taskdef.gif");
        _testDefIcon = new ImageIcon("images/testdef.gif");
        _testGroupIcon = new ImageIcon("images/testgroup.gif");
    }
}
