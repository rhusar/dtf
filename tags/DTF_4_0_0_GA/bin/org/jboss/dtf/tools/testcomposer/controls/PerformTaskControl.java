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
 * $Id: PerformTaskControl.java 170 2008-03-25 18:59:26Z jhalliday $
 */

package org.jboss.dtf.tools.testcomposer.controls;

import org.jboss.dtf.testframework.coordinator.actions.StartAction;
import org.jboss.dtf.testframework.coordinator.actions.PerformAction;
import org.jboss.dtf.tools.testcomposer.ComponentSizeChangeListener;
import org.jdom.Element;

import java.util.List;

public class PerformTaskControl extends TaskControl
{
    public static String TypeText = TYPE_TEXT[PERFORM];

    public PerformTaskControl(ComponentSizeChangeListener testDesignPane)
    {
        super(testDesignPane);
        _type = TaskControl.PERFORM;
    }

    public static PerformTaskControl getTask(Element definition)
    {
        PerformTaskControl ptc = new PerformTaskControl(null);

        Element taskName = definition.getChild("id");
        ptc.setTaskName(taskName.getText());

        Element location = definition.getChild("location");
        ptc.setTaskName(location.getText());

        Element parameters = definition.getChild("parameters");
        List params = parameters.getChildren("param");
        for (int count=0;count<params.size();count++)
        {
            ptc.addParam(((Element)params.get(count)).getText());
        }

        Element jvmParameters = definition.getChild("jvm-parameters");
        List jvmParams = jvmParameters.getChildren("param");
        for (int count=0;count<jvmParams.size();count++)
        {
            ptc.addJVMParam(((Element)jvmParams.get(count)).getText());
        }

        return(ptc);
    }

    public org.jboss.dtf.testframework.coordinator.Action getAction()
    {
        return(new PerformAction(this.getTaskName(),this.getRuntimeId(),this.getLocationParameter(),this.getParameters(),this.getJVMParameters(),this.getNameList()));
    }
}