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
// Copyright (C) 2001, 2002, 2003
//
// Arjuna Technologies Ltd.
// Newcastle upon Tyne,
// Tyne and Wear,
// UK.
//
// $Id: ProductConfiguration.java 170 2008-03-25 18:59:26Z jhalliday $

package org.jboss.dtf.testframework.productrepository;

import org.jboss.dtf.testframework.utils.ParameterPreprocessor;

import java.util.*;
import java.io.*;

import org.jdom.Element;
import org.jdom.Document;
import org.jdom.output.XMLOutputter;
import org.jdom.input.DOMBuilder;
import org.jdom.input.SAXBuilder;

public final class ProductConfiguration implements Serializable
{
	private final static String PRODUCT_CONFIGURATION_ROOT_ELEMENT_NAME = "product-configuration";
	private final static String PRODUCT_CONFIGURATION_NAME_ATTRIBUTE = "name";
	private final static String PRODUCT_CONFIGURATION_PERMUTATIONID_ATTRIBUTE = "permutationid";
	private final static String CLASSPATH_CONFIGURATIONS_ELEMENT = "classpath-configurations";
	private final static String NODE_CONFIGURATION_ELEMENT = "node-configuration";
	private final static String NODE_CONFIGURATIONS_ELEMENT = "node-configurations";
	private final static String TASK_RUNNER_DEFINITIONS_ELEMENT = "task-runner-definitions";
	private final static String TASK_RUNNER_DEFINITION_ELEMENT = "task-runner";

    private final static String JAR_SUFFIX = ".jar";
	private final static String ZIP_SUFFIX = ".zip";

	private final static String DEFAULT_CLASSPATH_NAME = "main";

	private final static String CLASSPATH_ELEMENT = "classpath";
	private final static String DIRECTORY_ELEMENT = "directory";
	private final static String JAR_ELEMENT = "jar";
	private final static String CLASSPATH_ELEMENT_NAME_ATTRIBUTE = "name";
	private final static String CLASSPATH_NAME_ATTRIBUTE = "name";
	private final static String CLASSPATH_DEFAULT_ATTRIBUTE = "default";

	private String		    _name = "";
	private String			_permutationId = null;
    private Hashtable       _classpaths = new Hashtable();
    private File			_productConfigurationFile = null;
    private Hashtable		_nodeConfigurations = new Hashtable();
	private String			_defaultClasspath = null;
	private Hashtable		_taskRunners = new Hashtable();

	public ProductConfiguration()
	{
		super();
	}

    public void setName(String name)
    {
        _name = name;
    }

    public String getName()
    {
        return _name;
    }

	public String getPermutationId()
	{
		return _permutationId;
	}

	public void setPermutationId(String pId) throws InvalidConfigurationException
	{
		if ( pId == null )
		{
			throw new InvalidConfigurationException("Permutation id not specified - required");
		}
		_permutationId = pId;
	}

    public void deleteClasspath(String name)
    {
        _classpaths.remove(name);
    }

	public String[] getClasspathNames()
	{
		String[] returnValue = new String[_classpaths.size()];
		_classpaths.keySet().toArray(returnValue);

		return returnValue;
	}

    public void setClasspath(String name, ArrayList path, boolean mustBeUnique) throws ClasspathAlreadyExistsException
    {
        if ( ( _classpaths.get(name) != null ) && ( mustBeUnique ) )
        {
            throw new ClasspathAlreadyExistsException("Classpath '"+name+"' already exists");
        }
        _classpaths.put(name, path);
    }

	public void stripClasspath(String name, String classpath, boolean mustBeUnique) throws ClasspathAlreadyExistsException
	{
        ArrayList path = new ArrayList();

		StringTokenizer st = new StringTokenizer( classpath, ";" );

		while (st.hasMoreTokens())
		{
			path.add(st.nextToken());
		}

        setClasspath(name, path, mustBeUnique);
	}

	public ArrayList getClasspathList(String name)
	{
		return (ArrayList)_classpaths.get(name);
	}

    public String getClasspath(String name)
    {
    	String result = null;
        ArrayList classpath = (ArrayList)_classpaths.get(name);

        if ( classpath != null )
        {
            result = "";

            if (classpath.size() > 0)
            {
                result = (String)classpath.get(0);

                for (int count=1;count<classpath.size();count++)
                {
                    result += File.pathSeparator + classpath.get(count);
                }
            }
        }

        return(result);
    }

	public boolean equals(String name)
	{
		return(_name.equalsIgnoreCase(name));
	}

	public void setProductConfigurationFile(File productConfigFile)
	{
		_productConfigurationFile = productConfigFile;
	}

	public File getProductConfigurationFile()
	{
		if ( _productConfigurationFile == null )
		{
			_productConfigurationFile = new File("products/"+getName()+".xml");
		}

		return _productConfigurationFile;
	}

	public void setNodeConfiguration(String osId, NodeConfiguration nodeConfig)
	{
		_nodeConfigurations.put(osId, nodeConfig);
	}

	public boolean supportsOs(String osId)
	{
		return _nodeConfigurations.get(osId) != null;
	}

	public NodeConfiguration getNodeConfiguration(String osId)
	{
		return (NodeConfiguration)_nodeConfigurations.get(osId);
	}

	public String[] getNodeConfigurations()
	{
		String[] nodeConfigs = new String[_nodeConfigurations.size()];
		_nodeConfigurations.keySet().toArray(nodeConfigs);

		return nodeConfigs;
	}

	public synchronized void serializeXML() throws IOException
	{
		Element root = new Element(PRODUCT_CONFIGURATION_ROOT_ELEMENT_NAME);
		Document doc = new Document(root);

		/** Set the product configuration name attribute **/
		root.setAttribute(PRODUCT_CONFIGURATION_NAME_ATTRIBUTE, getName());

		/** Set the product permutation id. attribute **/
		root.setAttribute(PRODUCT_CONFIGURATION_PERMUTATIONID_ATTRIBUTE, getPermutationId());

		/** Create the classpaths **/
		Enumeration e = _classpaths.keys();

		/** Create classpath-configurations element **/
		Element classpathConfigurations = new Element(CLASSPATH_CONFIGURATIONS_ELEMENT);
		root.addContent(classpathConfigurations);

		while (e.hasMoreElements())
		{
			String classpathName = (String)e.nextElement();
			ArrayList classpathList = (ArrayList)_classpaths.get(classpathName);

			Element classpathElement = new Element(CLASSPATH_ELEMENT);
			classpathConfigurations.addContent(classpathElement);
			classpathElement.setAttribute(CLASSPATH_ELEMENT_NAME_ATTRIBUTE, classpathName);

			if ( _defaultClasspath != null )
			{
				classpathElement.setAttribute(CLASSPATH_DEFAULT_ATTRIBUTE, _defaultClasspath);
			}

			for (int count=0;count<classpathList.size();count++)
			{
				Element jarDirElement;

				if ( isJarOrZip((String)classpathList.get(count) ) )
				{
					jarDirElement = new Element( JAR_ELEMENT );
					jarDirElement.setAttribute( CLASSPATH_NAME_ATTRIBUTE, (String)classpathList.get(count) );
				}
				else
				{
					jarDirElement = new Element( DIRECTORY_ELEMENT );
					jarDirElement.setAttribute( CLASSPATH_NAME_ATTRIBUTE, (String)classpathList.get(count) );
				}

				classpathElement.addContent(jarDirElement);
			}
		}

		/** Create node configurations element **/
		Element nodeConfigurations = new Element(NODE_CONFIGURATIONS_ELEMENT);
		root.addContent(nodeConfigurations);
		Enumeration nodeEnum = _nodeConfigurations.keys();

		while (nodeEnum.hasMoreElements())
		{
			NodeConfiguration nc = (NodeConfiguration)_nodeConfigurations.get((String)nodeEnum.nextElement());

			nodeConfigurations.addContent(nc.serializeXML());
		}

		/** Create the task runner definitions element **/
		Element taskRunnerDefinitions = new Element(TASK_RUNNER_DEFINITIONS_ELEMENT);
		root.addContent(taskRunnerDefinitions);
		Enumeration taskRunnerEnum = _taskRunners.keys();

		while ( taskRunnerEnum.hasMoreElements() )
		{
			String runnerName = (String)taskRunnerEnum.nextElement();
			TaskRunnerConfiguration taskRunnerConfig = (TaskRunnerConfiguration)_taskRunners.get(runnerName);

			taskRunnerDefinitions.addContent(taskRunnerConfig.serializeXML());
		}

		/** Write the configuration to the file **/
		XMLOutputter outputter = new XMLOutputter();
		OutputStream outStr = new FileOutputStream(getProductConfigurationFile());
		outputter.output( doc, outStr );
		outStr.close();
	}

	public void addTaskRunnerConfiguration(String name, TaskRunnerConfiguration taskRunner)
	{
		_taskRunners.put(name, taskRunner);
	}

	public TaskRunnerConfiguration getTaskRunnerConfiguration(String name)
	{
		return (TaskRunnerConfiguration)_taskRunners.get(name);
	}

	public void deleteTaskRunner(String selectedTaskRunner)
	{
		_taskRunners.remove(selectedTaskRunner);
	}

	public String[] getTaskRunnerConfigurations()
	{
		String[] taskRunnerNmes = new String[_taskRunners.size()];
		_taskRunners.keySet().toArray(taskRunnerNmes);

		return taskRunnerNmes;
	}

	private boolean isJarOrZip(String s)
	{
		return s.endsWith(JAR_SUFFIX) || s.endsWith(ZIP_SUFFIX);
	}

	public final static ProductConfiguration deserializeXML(File productConfigFile)  throws InvalidConfigurationException
	{
		ProductConfiguration product = null;

		try
		{
			product = new ProductConfiguration();

			product.setProductConfigurationFile(productConfigFile);

			SAXBuilder xmlBuilder = new SAXBuilder();
			Document doc = xmlBuilder.build(productConfigFile);

			/*
			 * Retrieve root element, then retrieve the test node configuration element
			 */
			Element productConfig = doc.getRootElement();

			/*
			 * Get product configuration's name
			 */
			product.setName( productConfig.getAttributeValue(PRODUCT_CONFIGURATION_NAME_ATTRIBUTE) );

			System.out.println("Adding product '"+product.getName()+"'");

			/*
			 * Get product permutation id.
			 */
			product.setPermutationId( productConfig.getAttributeValue(PRODUCT_CONFIGURATION_PERMUTATIONID_ATTRIBUTE));

			/**
			 * Get classpath-configurations
			 */
			Element classpathConfigurations = productConfig.getChild(CLASSPATH_CONFIGURATIONS_ELEMENT);

			/*
			 * Get classpaths
			 */
			List classpaths = classpathConfigurations.getChildren(CLASSPATH_ELEMENT);

			/*
			 * Create classpath text from element
			 */
			for (int classpathCount=0;classpathCount<classpaths.size();classpathCount++)
			{
				boolean defaultPath = true;
				org.jdom.Element classpath = (org.jdom.Element)classpaths.get(classpathCount);
				String classpathName = classpath.getAttributeValue(CLASSPATH_NAME_ATTRIBUTE);
				String defaultClasspathStr = classpath.getAttributeValue(CLASSPATH_DEFAULT_ATTRIBUTE);

				if ( defaultClasspathStr != null )
				{
					defaultPath = new Boolean(defaultClasspathStr).booleanValue();
				}

				List classpathElements = classpath.getChildren();

				/*
				 * Concatenate the jars from the jar elements
				 * onto the classpath text
				 */
				ArrayList pathList = new ArrayList();
				for (int elementCount = 0; elementCount < classpathElements.size(); elementCount++)
				{
					org.jdom.Element classpathElement = (org.jdom.Element) classpathElements.get(elementCount);

					if ((classpathElement.getName().equalsIgnoreCase(DIRECTORY_ELEMENT)) ||
							(classpathElement.getName().equalsIgnoreCase(JAR_ELEMENT)))
					{
					   pathList.add(classpathElement.getAttributeValue(CLASSPATH_ELEMENT_NAME_ATTRIBUTE));
					}
				}

				System.out.println("Adding classpath '"+classpathName+"'");
				product.setClasspath( classpathName != null ? classpathName : DEFAULT_CLASSPATH_NAME, pathList, true );
			}

			Element nodeConfigurations = productConfig.getChild(NODE_CONFIGURATIONS_ELEMENT);

			List nodeConfigLists = nodeConfigurations.getChildren(NODE_CONFIGURATION_ELEMENT);

			for (int count=0;count<nodeConfigLists.size();count++)
			{
                NodeConfiguration nodeConfig = NodeConfiguration.create((Element)nodeConfigLists.get(count));

				product.setNodeConfiguration(nodeConfig.getOs(), nodeConfig);
			}

			Element taskRunnerConfigurations = productConfig.getChild(TASK_RUNNER_DEFINITIONS_ELEMENT);

			if ( taskRunnerConfigurations != null )
			{
				List taskRunnerList = taskRunnerConfigurations.getChildren(TASK_RUNNER_DEFINITION_ELEMENT);

				for (int count=0;count<taskRunnerList.size();count++)
				{
					TaskRunnerConfiguration taskRunner = TaskRunnerConfiguration.getTaskRunnerConfiguration((Element)taskRunnerList.get(count));

					product.addTaskRunnerConfiguration(taskRunner.getName(), taskRunner);
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace(System.err);
			throw new InvalidConfigurationException("Failed to create product configuration: "+e);
		}

		return product;
	}

	public void deleteNodeConfig(String selected)
	{
		_nodeConfigurations.remove(selected);
	}

	public void createCopy(String selected)
	{
		int count = 1;

		while ( _nodeConfigurations.containsKey( selected+"_copy_"+count ) )
		{
			count++;
		}

		NodeConfiguration newNodeConfig = new NodeConfiguration((NodeConfiguration)_nodeConfigurations.get(selected));
		newNodeConfig.setOs(selected+"_copy_"+count);

		_nodeConfigurations.put(selected+"_copy_"+count, newNodeConfig);
	}
}
