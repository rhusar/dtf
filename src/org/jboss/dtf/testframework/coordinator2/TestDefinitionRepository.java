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
package org.jboss.dtf.testframework.coordinator2;

import org.jdom.input.DOMBuilder;
import org.jdom.input.SAXBuilder;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;

import java.util.HashMap;
import java.util.List;
import java.util.Iterator;
import java.net.URL;

import org.jboss.dtf.testframework.coordinator.TaskNotFound;
import org.jboss.dtf.testframework.coordinator2.exceptions.InvalidPermutationException;

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
			System.out.println("Populating test repository..");

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

    public void generatePermutations() throws InvalidPermutationException
	{
		Iterator testIdItr = _testDefinitions.keySet().iterator();

		while ( testIdItr.hasNext() )
		{
			String testId = (String)testIdItr.next();
			TestDefinition test = (TestDefinition)_testDefinitions.get(testId);

			if ( test != null )
			{
				String[] productIds = test.getPermutationProductIds();

				for (int count=0;count<productIds.length;count++)
				{
					test.generateAllPermutations(productIds[count]);
				}
			}
		}
	}

	public HashMap getTestDefinitionsMap()
	{
		return(_testDefinitions);
	}

	public TestDefinition getTestDefinition(String groupId, String testId)
	{
		return((TestDefinition)_testDefinitions.get(TestDefinition.generateFullId(groupId,testId)));
	}

	private void getAllTestDeclarations(Element root)
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
			TestDefinition testDeclaration = new TestDefinition(groupId, testDeclarationElement);

			_testDefinitions.put(testDeclaration.getFullId(), testDeclaration);
		}
	}
}

