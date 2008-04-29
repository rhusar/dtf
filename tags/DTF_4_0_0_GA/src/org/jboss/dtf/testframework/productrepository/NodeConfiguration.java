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
 * Copyright (C) 1998, 1999, 2000, 2001, 2002, 2003
 *
 * Arjuna Technologies Ltd.
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: NodeConfiguration.java 170 2008-03-25 18:59:26Z jhalliday $
 */
package org.jboss.dtf.testframework.productrepository;

import org.jdom.Element;

import java.util.Hashtable;
import java.util.List;
import java.util.Enumeration;
import java.util.ArrayList;
import java.io.Serializable;

import org.jboss.dtf.testframework.utils.StringPreprocessor;

public class NodeConfiguration implements Serializable
{
	private final static String NODE_CONFIGURATION_ELEMENT = "node-configuration";
	private final static String OS_NAME_ATTRIBUTE = "os";
	private final static String PROPERTIES_ELEMENT = "properties";
	private final static String PROPERTY_ELEMENT = "property";
	private final static String PROPERTY_NAME_ATTRIBUTE = "name";
	private final static String PROPERTY_VALUE_ATTRIBUTE = "value";
	private final static String SETS_ELEMENT = "sets";
	private final static String SET_ELEMENT = "set";
	private final static String DEFAULT_CLASSPATH_ELEMENT = "default-classpath";
    private final static String DEFAULT_CLASSPATH_NAME_ATTRIBUTE = "name";
	private final static String JVM_ELEMENT = "jvm";
	private final static String JVM_ID_ATTRIBUTE = "id";
    private final static String NODE_EXCLUSIONS_ELEMENT = "node-exclusions";
	private final static String EXCLUDE_ELEMENT = "exclude";
	private final static String NODE_NAME_ATTRIBUTE = "name";

	private Hashtable	_properties = new Hashtable();
	private Hashtable	_sets = new Hashtable();
	private String		_defaultClasspath;
    private String		_osId = null;
	private String		_oldOsId = null;
    private String		_jvmId = null;
	private ArrayList	_exclusions = new ArrayList();

	public NodeConfiguration()
	{
		super();
	}

	public NodeConfiguration(NodeConfiguration p)
	{
		super();

		_properties = new Hashtable(p._properties);
		_sets = new Hashtable(p._sets);
		_defaultClasspath = new String(p._defaultClasspath);
		_osId = new String(p._osId);
		_jvmId = p._jvmId;
	}

	public void setOs(String osId)
	{
		if ( ( _osId != null ) && ( !_osId.equals(osId) ) )
		{
			_oldOsId = _osId;
		}

		_osId = osId;
	}

	public String getOldOSId()
	{
		if ( _oldOsId == null )
			return null;

		String oldId = new String(_oldOsId);
		_oldOsId = null;
		return oldId;
	}

	public String getOs()
	{
		return _osId;
	}

	public Hashtable getProperties()
	{
		return _properties;
	}

	public Hashtable getSets()
	{
		return _sets;
	}

	public void setJvmId(String jvmId)
	{
		_jvmId = jvmId;
	}

	public String getJvmId()
	{
		return _jvmId;
	}

	public void setProperty(String name, String value)
	{
		_properties.put(name, value);
	}

	public String getProperty(String name)
	{
		return (String)_properties.get(name);
	}

	public void set(String name, String value)
	{
		_sets.put(name, value);
	}

	public Hashtable getPreprocessedSets()
	{
		return preprocessHashtable(_sets,_sets);
	}

	public Hashtable getPreprocessedProperties()
	{
		return preprocessHashtable(_properties,_sets);
	}

	private Hashtable preprocessHashtable(Hashtable table, Hashtable preVars)
	{
		Hashtable processedSets = new Hashtable();
		StringPreprocessor pre = new StringPreprocessor();
		pre.addReplacements(preVars);

		Enumeration e = table.keys();

		while ( e.hasMoreElements() )
		{
			String setName = (String)e.nextElement();
			String setValue = (String)table.get(setName);

			if ( setValue != null )
			{
				setValue = pre.preprocessParameters(setValue);
			}

			processedSets.put(setName, setValue);
		}

		return processedSets;
	}

	public String get(String name)
	{
		return (String)_sets.get(name);
	}

	public void setDefaultClasspath(String name)
	{
		_defaultClasspath = name;
	}

	public String getDefaultClasspath()
	{
		return _defaultClasspath;
	}

	public void excludedNode(String nodeName)
	{
		if ( !_exclusions.contains(nodeName) )
			_exclusions.add(nodeName);
	}

	public void removeExclusion(String nodeName)
	{
		_exclusions.remove(nodeName);
	}

	public String[] getExclusions()
	{
		String[] returnArray = new String[_exclusions.size()];
		_exclusions.toArray(returnArray);

		return returnArray;
	}

	public boolean isNodeExcluded(String nodeName)
	{
		return _exclusions.contains(nodeName);
	}

	public void deleteProperty(String property)
	{
		_properties.remove(property);
	}

	public void deleteSet(String set)
	{
		_sets.remove(set);
	}

	public Element serializeXML()
	{
		Element nodeConfigurationElement = new Element(NODE_CONFIGURATION_ELEMENT);

		/** Set OS attribute **/
		nodeConfigurationElement.setAttribute(OS_NAME_ATTRIBUTE, getOs());

		/** Create properties element **/
        Element propertiesElement = new Element(PROPERTIES_ELEMENT);
		nodeConfigurationElement.addContent(propertiesElement);

		/** Create property elements for all properties **/
		Enumeration propertiesEnum = _properties.keys();

		while ( propertiesEnum.hasMoreElements() )
		{
			String propertyName = (String)propertiesEnum.nextElement();
			String propertyValue = (String)_properties.get(propertyName);

			/** Create property element **/
			Element propertyElement = new Element(PROPERTY_ELEMENT);
			propertiesElement.addContent(propertyElement);

			propertyElement.setAttribute(PROPERTY_NAME_ATTRIBUTE, propertyName);
			propertyElement.setAttribute(PROPERTY_VALUE_ATTRIBUTE, propertyValue);
		}

		/** Create sets element **/
        Element setsElement = new Element(SETS_ELEMENT);
		nodeConfigurationElement.addContent(setsElement);

		/** Create set elements for all properties **/
		Enumeration setsEnum = _sets.keys();

		while ( setsEnum.hasMoreElements() )
		{
			String setName = (String)setsEnum.nextElement();
			String setValue = (String)_sets.get(setName);

			/** Create set element **/
			Element setElement = new Element(SET_ELEMENT);
			setsElement.addContent(setElement);

			setElement.setAttribute(PROPERTY_NAME_ATTRIBUTE, setName);
			setElement.setAttribute(PROPERTY_VALUE_ATTRIBUTE, setValue);
		}

		if ( _jvmId != null )
		{
			/** Create jvm id element **/
			Element jvmElement = new Element(JVM_ELEMENT);
			jvmElement.setAttribute(JVM_ID_ATTRIBUTE, _jvmId);
			nodeConfigurationElement.addContent(jvmElement);
		}

		/** Create default classpath element **/
		Element defaultClasspathElement = new Element(DEFAULT_CLASSPATH_ELEMENT);
		defaultClasspathElement.setAttribute(DEFAULT_CLASSPATH_NAME_ATTRIBUTE, getDefaultClasspath());
		nodeConfigurationElement.addContent(defaultClasspathElement);

		/** Create exclusions element **/
		Element exclusionElements = new Element(NODE_EXCLUSIONS_ELEMENT);
		nodeConfigurationElement.addContent(exclusionElements);

		for (int count=0;count<_exclusions.size();count++)
		{
			Element exclude = new Element(EXCLUDE_ELEMENT);
			exclusionElements.addContent(exclude);
			exclude.setAttribute(NODE_NAME_ATTRIBUTE, (String)_exclusions.get(count));
		}

		return nodeConfigurationElement;
	}

	public final static NodeConfiguration create(Element nodeConfigElement)
	{
		NodeConfiguration nodeConfig = new NodeConfiguration();

		/** Set node configuration OS **/
        nodeConfig.setOs(nodeConfigElement.getAttributeValue(OS_NAME_ATTRIBUTE));

        /** Get properties element **/
		Element properties = nodeConfigElement.getChild(PROPERTIES_ELEMENT);

		/** Get property element list **/
		List propertiesList = properties.getChildren(PROPERTY_ELEMENT);

		for (int count=0;count<propertiesList.size();count++)
		{
            Element property = (Element)propertiesList.get(count);
			String name = property.getAttributeValue(PROPERTY_NAME_ATTRIBUTE);
			String value = property.getAttributeValue(PROPERTY_VALUE_ATTRIBUTE);

			nodeConfig.setProperty(name, value);
		}

		/** Get sets element **/
		Element sets = nodeConfigElement.getChild(SETS_ELEMENT);

		/** Get set element list **/
		List setsList = sets.getChildren(SET_ELEMENT);

		for (int count=0;count<setsList.size();count++)
		{
			Element set = (Element)setsList.get(count);
			String name = set.getAttributeValue(PROPERTY_NAME_ATTRIBUTE);
			String value = set.getAttributeValue(PROPERTY_VALUE_ATTRIBUTE);

			nodeConfig.set(name, value);
		}

		/** Get jvm element **/
		Element jvmElement = nodeConfigElement.getChild(JVM_ELEMENT);

		if ( jvmElement != null )
		{
			nodeConfig.setJvmId(jvmElement.getAttributeValue(JVM_ID_ATTRIBUTE));
		}

		/** Get default classpath element **/
		Element defaultClasspath = nodeConfigElement.getChild(DEFAULT_CLASSPATH_ELEMENT);
		nodeConfig.setDefaultClasspath(defaultClasspath.getAttributeValue(DEFAULT_CLASSPATH_NAME_ATTRIBUTE));

		/** Get exclusions **/
		Element nodeExclusionsElement = nodeConfigElement.getChild(NODE_EXCLUSIONS_ELEMENT);

		if ( nodeExclusionsElement != null )
		{
			List nodeExclusions = nodeExclusionsElement.getChildren(EXCLUDE_ELEMENT);

			for (int count=0;count<nodeExclusions.size();count++)
			{
				Element excludeElement = (Element)nodeExclusions.get(count);

				nodeConfig.excludedNode(excludeElement.getAttributeValue(NODE_NAME_ATTRIBUTE));
			}
		}

		return nodeConfig;
	}
}
