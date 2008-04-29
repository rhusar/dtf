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
// $Id: ServiceManager.java 170 2008-03-25 18:59:26Z jhalliday $

package org.jboss.dtf.testframework.servicemanager;

import org.jdom.input.*;
import org.jdom.*;

import java.util.List;
import java.io.File;

public class ServiceManager
{
	public ServiceManager(String serviceConfigFile)
	{
		try
		{
			SAXBuilder xmlBuilder = new SAXBuilder();
			Document doc = xmlBuilder.build(new File(serviceConfigFile));

			/*
			 * Retrieve root element, then retrieve the ServiceManager configuration element
			 */
			Element root = doc.getRootElement();

			List serviceConfigs = root.getChildren("service");

			for (int count=0;count<serviceConfigs.size();count++)
			{
				Element serviceConfig = (Element)serviceConfigs.get(count);

				String serviceName = serviceConfig.getAttributeValue("name");
				String serviceClass = serviceConfig.getAttributeValue("class");
				String timeout = serviceConfig.getAttributeValue("timeout");
				String[] parameterArray = null;
				String[] propertyParams = null;
                String[] settingParams = null;

				long timeoutValue = 0;

				if (timeout!=null)
				{
					timeoutValue = Long.parseLong(timeout);
				}

				Element parameters = serviceConfig.getChild("parameters");
				if (parameters != null)
				{
					List params = parameters.getChildren("param");
					parameterArray = new String[params.size()];

					for (int paramCount=0;paramCount<params.size();paramCount++)
					{
						Element param = (Element)params.get(paramCount);

						parameterArray[paramCount] = param.getText();
					}
				}

				Element propertyElements = serviceConfig.getChild("properties");

				if ( propertyElements != null )
				{
					List properties = propertyElements.getChildren("property");
					propertyParams = new String[properties.size()];

					for (int paramCount=0;paramCount<properties.size();paramCount++)
					{
						Element param = (Element)properties.get(paramCount);

						propertyParams[paramCount] = "-D"+param.getAttributeValue("name") + "=" + param.getAttributeValue("value");
						System.out.println("Setting property '"+param.getAttributeValue("name")+"' to '"+param.getAttributeValue("value")+"'");
					}
				}

				Element settingsElements = serviceConfig.getChild("settings");

				if ( settingsElements != null )
				{
					List settings = settingsElements.getChildren("setting");
					settingParams = new String[settings.size()];

					for (int settingCount=0;settingCount<settings.size();settingCount++)
					{
						Element setting = (Element)settings.get(settingCount);

						settingParams[settingCount] = setting.getTextTrim();
						System.out.println("Setting '"+settingParams[settingCount]+"'");
					}
				}

				Element classpathElement = serviceConfig.getChild("classpath");
				ClasspathConfiguration classpath = null;

				if ( classpathElement != null)
				{
					classpath = new ClasspathConfiguration(classpathElement);

					new ServiceWatchdog(serviceName, serviceClass, parameterArray, propertyParams, settingParams, timeoutValue, classpath);
					System.out.println("Service '"+serviceName+"' started successfully");
				}
				else
				{
					System.err.println("Service '"+serviceName+"' - classpath not specified");
				}
			}
		}
		catch (JDOMException e)
		{
			e.printStackTrace(System.err);
			System.out.println("ERROR: Error in configuration file");
			System.exit(0);
		}
		catch (java.lang.Exception e)
		{
			System.out.println("An unexpected exception occurred while starting services - "+e.toString());
			e.printStackTrace(System.err);
			System.exit(0);
		}
	}

	public static void main(String args[])
	{
		if (args.length==0)
		{
			System.out.println("Usage: ServiceManager [ Services.xml ]");
		}
		else
		{
			new ServiceManager( args[0] );
		}
	}
}
