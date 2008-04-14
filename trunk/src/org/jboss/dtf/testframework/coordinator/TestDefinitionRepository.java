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
// $Id: TestDefinitionRepository.java 170 2008-03-25 18:59:26Z jhalliday $

package org.jboss.dtf.testframework.coordinator;

import org.jboss.dtf.testframework.coordinator.*;

import org.jdom.input.*;
import org.jdom.*;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.net.URL;

public class TestDefinitionRepository
{
    private final static String IMPORT_ELEMENT = "import";
    private final static String IMPORT_ELEMENTS_TEST_SET_ATTRIBUTE = "test-set";

	private HashMap _testDefinitions = new HashMap();
    private String  _description = null;


    public TestDefinitionRepository()
    {
    }

	public TestDefinitionRepository(URL testDefinitionFile) throws Exception
	{
        parse(testDefinitionFile);
    }

    private void parse(URL testDefinitionFile) throws Exception
    {
		try
		{
			SAXBuilder xmlBuilder = new SAXBuilder();
			Document doc = xmlBuilder.build(testDefinitionFile);

			/*
			 * Retrieve root element
			 */
			Element root = doc.getRootElement();

            Element descElement = root.getChild("description");

            if ( descElement != null )
                _description = descElement.getTextTrim();
            else
                _description = "";

            /**
             * Find all import references
             */
            List imports = root.getChildren( IMPORT_ELEMENT );

            for (int count=0;count<imports.size();count++)
            {
                Element importElement = (Element)imports.get(count);
                String testSetAttr = importElement.getAttributeValue( IMPORT_ELEMENTS_TEST_SET_ATTRIBUTE );

                parse(new URL(testDefinitionFile, testSetAttr));
            }

			getAllTestDeclarations(root);
		}
		catch (JDOMException e)
		{
			e.printStackTrace();
			System.out.println("\nERROR: Incorrect test definition file");
            throw new Exception("Incorrect test definition file");
		}
	}

    public String getDescription()
    {
        return _description;
    }

	public boolean verifyRepository(TaskDefinitionRepository taskDefRepos)
	{
		Object[] testDefs = _testDefinitions.values().toArray();
		int count=0;
		TestDefinition testDef = null;
		boolean valid = true;

		while (count<testDefs.length)
		{
			try
			{
				testDef = (TestDefinition)testDefs[count++];
				testDef.verifyTest(taskDefRepos);
			}
			catch (TaskNotFound e)
			{
				System.err.println("Test XML definition is incorrect for test '"+testDef.getGroupId()+"/"+testDef.getId()+"'");
				System.err.println("Reason: "+e.toString());
				valid = false;
			}
		}

		return(valid);
	}


	public HashMap getTestDefinitionsMap()
	{
		return(_testDefinitions);
	}

	public TestDefinition getTestDefinition(String groupId, String testId)
	{
		return((TestDefinition)_testDefinitions.get(TestDefinition.generateFullId(groupId,testId)));
	}

	private void getAllTestDeclarations(Element root) throws JDOMException
	{
		List testGroupList = root.getChildren("test_group");

		for (int testGroupCount=0;testGroupCount<testGroupList.size();testGroupCount++)
		{
			Element testGroup = (Element)testGroupList.get(testGroupCount);
			String groupId = testGroup.getAttributeValue("name");

			parseTestDefinitionsGroup( groupId, testGroup );
		}
	}

	private void parseTestDefinitionsGroup( String groupId, Element testGroup )
	{
		/*
		 * Retrieve list of all test_group elements
		 */
		List testGroupList = testGroup.getChildren("test_group");

		for (int testGroupCount=0;testGroupCount<testGroupList.size();testGroupCount++)
		{
			Element subTestGroup = (Element)testGroupList.get(testGroupCount);
			String subGroupId = subTestGroup.getAttributeValue("name");

			parseTestDefinitionsGroup( groupId+"/"+subGroupId, subTestGroup );
		}

		/*
		 * Retrieve list of all test declaration elements
		 */
		List testDeclarationList = testGroup.getChildren("test_declaration");

		for (int testCount=0;testCount<testDeclarationList.size();testCount++)
		{
			Element testDeclarationElement = (Element)testDeclarationList.get(testCount);
			TestDefinition testDeclaration = createTestDefinition(groupId, testDeclarationElement);

			_testDefinitions.put(testDeclaration.getFullId(), testDeclaration);
		}
	}


	private TestDefinition createTestDefinition(String testGroup, Element testDecElement)
	{
		int numberOfTasksStarted = 0;
		/*
		 * Retrieve id, descriptive name
		 */
		String 	testId = testDecElement.getAttributeValue("id"),
				descriptiveName = testDecElement.getAttributeValue("descriptive_name");
		int 	numNamesRequired = 0;
		Element descriptionElement = testDecElement.getChild("description");
		String  description  = (descriptionElement!=null)?descriptionElement.getText().trim():"";
		Element configElement = testDecElement.getChild("configuration");
		String 	productFlags = "";
		Hashtable runnerParameters = new Hashtable();

		if (configElement != null)
		{
			String namesRequired = configElement.getAttributeValue("names_required");

			if (namesRequired!=null)
				numNamesRequired = Integer.parseInt(namesRequired);

			List runnerConfigElements = configElement.getChildren("runner");

			for (int elementCount=0;elementCount<runnerConfigElements.size();elementCount++)
			{
				Element runnerConfigElement = (Element)runnerConfigElements.get(elementCount);

				String configForTaskRunnerName = runnerConfigElement.getAttributeValue("name");

				Hashtable parameters = new Hashtable();

				List parameterElements = runnerConfigElement.getChildren("param");

				for (int paramCount=0;paramCount<parameterElements.size();paramCount++)
				{
					Element paramElement = (Element)parameterElements.get(paramCount);

					parameters.put( paramElement.getAttributeValue("name"), paramElement.getAttributeValue("value") );
				}
				runnerParameters.put( configForTaskRunnerName, parameters);
			}
		}

		/*
		 * Create TestDeclaration object with this information
		 */
		TestDefinition testDeclaration = new TestDefinition(testGroup, testId, descriptiveName, description, numNamesRequired, runnerParameters);

		/*
		 * Retrieve and interpret the action list for this test
		 */
		Element actionListElement = testDecElement.getChild("action_list");

		List actionList = actionListElement.getChildren();

		for (int count=0;count<actionList.size();count++)
		{
			Element actionElement = (Element)actionList.get(count);

			String actionName = actionElement.getName();

			// If the action is a perform task action
			if (actionName.equals(TestDefinition.PERFORM_TASK_NAME))
			{
				int 	arraySize = 0;
				String 	id = actionElement.getAttributeValue("id"),
                        runtimeId = actionElement.getAttributeValue("runtime_id"),
				        location = actionElement.getAttributeValue("location"),
						nameList = actionElement.getAttributeValue("name_list"),
						jvmParameters = actionElement.getAttributeValue("jvm_parameters");
				String  singleParameters = actionElement.getAttributeValue("parameters");
                List jvmParameterElements = actionElement.getChildren("jvm_param");
                String[] jvmSubParameters = new String[(jvmParameters==null)?jvmParameterElements.size():jvmParameterElements.size()+1];

                if (jvmParameters != null)
                {
                    jvmSubParameters[0] = jvmParameters;
                    arraySize = 1;
                }

                for (int paramCount=0;paramCount<jvmParameterElements.size();paramCount++)
                {
                    jvmSubParameters[arraySize + paramCount] = ((Element)jvmParameterElements.get(paramCount)).getText().trim();
                }

				/*
				 * If the parameters attribute was specified ensure the parameters array
				 * is big enough to hold it
				 */
				arraySize = (singleParameters!=null)?1:0;
				List parameterElements = actionElement.getChildren("param");
				String[] parameters = new String[arraySize + parameterElements.size()];
				/*
				 * If the parameters attribute was specified add it to the parameters array
				 */
				if (singleParameters!=null)
				{
					parameters[0] = singleParameters.trim();
				}
				for (int parameterCount=0;parameterCount<parameterElements.size();parameterCount++)
				{
					Element parameterElement = (Element)parameterElements.get(parameterCount);
					parameters[arraySize + parameterCount] = parameterElement.getText().trim();
				}
				testDeclaration.addPerformTaskAction(id, runtimeId, location, nameList, parameters, jvmSubParameters);
				numberOfTasksStarted++;
			}
			else
			// If the action is a start task action
			if (actionName.equals(TestDefinition.START_TASK_NAME))
			{
				int 	arraySize = 0;
				String 	id = actionElement.getAttributeValue("id"),
				        location = actionElement.getAttributeValue("location"),
						nameList = actionElement.getAttributeValue("name_list"),
						runtimeId = actionElement.getAttributeValue("runtime_id"),
						jvmParameters = actionElement.getAttributeValue("jvm_parameters");
				String  singleParameters = actionElement.getAttributeValue("parameters");
                List jvmParameterElements = actionElement.getChildren("jvm_param");
                String[] jvmSubParameters = new String[(jvmParameters==null)?jvmParameterElements.size():jvmParameterElements.size()+1];

                if (jvmParameters != null)
                {
                    jvmSubParameters[0] = jvmParameters;
                    arraySize = 1;
                }

                for (int paramCount=0;paramCount<jvmParameterElements.size();paramCount++)
                {
                    jvmSubParameters[arraySize + paramCount] = ((Element)jvmParameterElements.get(paramCount)).getText().trim();
                }

				/*
				 * If the parameters attribute was specified ensure the parameters array
				 * is big enough to hold it
				 */
				arraySize = (singleParameters!=null) ? 1 : 0;
				List parameterElements = actionElement.getChildren("param");
				String parameters[] = new String[arraySize + parameterElements.size()];

				/*
				 * If the parameters attribute was specified add it to the parameters array
				 */
				if (singleParameters!=null)
				{
					parameters[0] = singleParameters.trim();
				}

				for (int parameterCount=0;parameterCount<parameterElements.size();parameterCount++)
				{
					Element parameterElement = (Element)parameterElements.get(parameterCount);

					parameters[arraySize + parameterCount] = parameterElement.getText().trim();
				}

				testDeclaration.addStartTaskAction(id, location, nameList, runtimeId, parameters, jvmSubParameters);
				numberOfTasksStarted++;
			}
			else
			// If the action is a wait for task action
			if (actionName.equals(TestDefinition.WAIT_FOR_TASK_NAME))
			{
				String runtimeId = actionElement.getAttributeValue("runtime_id");

				testDeclaration.addWaitForTaskAction(runtimeId);
			}
			else
			// If the action is a terminate task action
			if (actionName.equals(TestDefinition.TERMINATE_TASK_NAME))
			{
				String runtimeId = actionElement.getAttributeValue("runtime_id");

				testDeclaration.addTerminateTaskAction(runtimeId);
			}
		}

		testDeclaration.setNumberOfTasksStarted(numberOfTasksStarted);
		return(testDeclaration);
	}
}
