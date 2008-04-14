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
package org.jboss.dtf.testframework.dtfweb;

import org.jboss.dtf.testframework.coordinator.TestDefinitionRepository;
import org.jboss.dtf.testframework.coordinator.TestDefinition;
import org.jboss.dtf.testframework.dtfweb.exceptions.FileAlreadyExistsException;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.*;
import java.io.PrintStream;
import java.io.FileOutputStream;
import java.io.File;

public class DTFCreateSelection
{
    public final static String  CURRENT_SELECTION_ATTRIBUTE_NAME = "current_selection";

    private final static String GOS_PREFIX = "GOS_";
    private final static String EXPAND_PREFIX = "expand_";
    private final static String EXPAND_SUFFIX = ".x";

    private StoredTestDefs              _testDefs = null;
    private TestDefinitionRepository    _testRepository = null;
    private GroupSelection              _testGroups = null;
    private String                      _name = null;
    private String                      _description = "";
    private String                      _productName = "Unknown";

    public DTFCreateSelection(StoredTestDefs testDefs) throws Exception
    {
        _testDefs = testDefs;
        _testRepository = new TestDefinitionRepository( new URL(testDefs.getURL()) );
        _testGroups = new GroupSelection(new Hashtable(), "","");

        _name = "Selection_"+System.currentTimeMillis();

        HashMap testDefMap = _testRepository.getTestDefinitionsMap();

        findGroups( testDefMap, _testGroups );
    }

    public String getProductName()
    {
        return _productName;
    }

    public void setProductName(String name)
    {
        _productName = name;
    }

    public void setSelectionName(String name)
    {
        _name = name;
    }

    public String getSelectionName()
    {
        return _name;
    }

    public void setDescription(String description)
    {
        _description = description;
    }

    public String getDescription()
    {
        return _description;
    }

    private void resetSelections(GroupSelection testGroups)
    {
        Enumeration e = testGroups.elements();

        while ( e.hasMoreElements() )
        {
            GroupSelection gs = (GroupSelection)e.nextElement();

            gs.clearSelections();

            resetSelections(gs);
        }
    }

    public void handleRequest(HttpServletRequest request)
    {
        Enumeration e = request.getParameterNames();

        resetSelections(_testGroups);
        while ( e.hasMoreElements() )
        {
            String name = (String)e.nextElement();

            System.out.println("Name '"+name+"'");
            if ( name.startsWith(GOS_PREFIX) )
            {
                String temp = name.substring(GOS_PREFIX.length());
                String fullGroupId = temp.substring(0, temp.lastIndexOf('_'));
                String osId = temp.substring( temp.lastIndexOf('_') + 1 );

                GroupSelection gs = getSelection(fullGroupId);

                System.out.println("Group '"+fullGroupId+"' Os '"+osId+"'");

                gs.setSelected(osId,true);
            }
            else
            if ( name.startsWith(EXPAND_PREFIX) )
            {
                System.out.println("Looking for expand group '"+name+"'");
                String expandChoice = name.substring( EXPAND_PREFIX.length() );

                if ( expandChoice.indexOf(EXPAND_SUFFIX) != -1 )
                {
                    expandChoice = expandChoice.substring(0, expandChoice.indexOf(EXPAND_SUFFIX));

                    GroupSelection gs = getSelection(expandChoice);

                    if ( gs != null )
                    {
                        gs.setExpanded(!gs.isExpanded());
                    }
                }
            }
        }

        String name = request.getParameter("selection_name");
        String description = request.getParameter("selection_description");
        String productName = request.getParameter("product_name");

        if ( name != null )
        {
            setSelectionName(name);
        }

        if ( description != null )
        {
            setDescription(description);
        }

        if ( productName != null )
        {
            setProductName(productName);
        }
    }

    public GroupSelection getSelection(String name)
    {
        Hashtable current = getGroups();
        StringTokenizer st = new StringTokenizer(name, "/");
        GroupSelection gs = null;

        while (st.hasMoreTokens())
        {
            String token = st.nextToken();

            gs = (GroupSelection)current.get( token);
            current = gs;
        }

        return gs;
    }


    public Hashtable getGroups()
    {
        return _testGroups;
    }

    public OSDetails[] getSupportedOperatingSystems()
    {
        return DTFResultsManager.getSupportedOSs();
    }

    private void findGroups(HashMap currentTestDefsMap, GroupSelection testGroups)
    {
        Set keySet = currentTestDefsMap.keySet();
        String[] keys = new String[ keySet.size() ];
        keySet.toArray(keys);

        for (int count=0;count<keys.length;count++)
        {
            TestDefinition testDef = (TestDefinition)currentTestDefsMap.get( keys[count] );

            createGroupsFromId(testGroups, testDef);
        }
    }

    private void createGroupsFromId(GroupSelection testGroups, TestDefinition testDef)
    {
        String fullGroupId = "";

        System.out.println("Generating groups from '"+testDef.getGroupId()+"'");
        StringTokenizer st = new StringTokenizer(testDef.getGroupId()+"/", "/");

        while (st.hasMoreTokens())
        {
            String token = st.nextToken();

            fullGroupId += token + "/";

            if ( testGroups.get( token ) != null )
            {
                testGroups = (GroupSelection)testGroups.get( token );
            }
            else
            {
                GroupSelection newGroup;

                testGroups.put( token, newGroup = new GroupSelection(new Hashtable(), token, fullGroupId) );

                testGroups = newGroup;
            }
        }

        String testName = testDef.getId();
        testGroups.put( testName, new GroupSelection(null, testName, fullGroupId+"/"+testName) );
    }

    public void generateGroupBox(int depth, GroupSelection gs, StringBuffer buffer) throws java.io.IOException
    {
        buffer.append("<tr><td width=\"50%\"><font face=\"Verdana\" size= color=\"White\">");

        for (int count=0;count<depth;count++)
        {
            buffer.append("<blockquote>");
        }

        if ( !gs.isEmpty() )
        {
            buffer.append("<input type=\"image\" name=\"expand_"+gs.getFullGroupName()+"\" src=\""+(gs.isExpanded()?"contract":"expand")+".gif\" border=0/>");
        }

        buffer.append("&nbsp;"+gs.getGroupName());

        for (int count=0;count<depth;count++)
        {
            buffer.append("</blockquote>");
        }

        buffer.append("</font></td><td width=\"50%\"><table><tr>");

        OSDetails[] oss = getSupportedOperatingSystems();

        for (int osCount=0;osCount<oss.length;osCount++)
        {
            buffer.append("<td><font face=\"Verdana\" size=2 color=\"#FFFFFF\"><input type=\"checkbox\" name=\"GOS_"+gs.getFullGroupName()+"_"+oss[osCount]._id+"\" "+(gs.isSelected(oss[osCount]._id)?"checked":"")+">"+oss[osCount]._name+"</font></td>");
        }

        buffer.append("</tr></table></td></tr>");
    }

    public void generateGroupBoxes(int depth, StringBuffer buffer, Hashtable groups)
    {
        Enumeration groupElements = groups.elements();

        while (groupElements.hasMoreElements())
        {
            GroupSelection gs = (GroupSelection)groupElements.nextElement();

            try
            {
                generateGroupBox(depth, gs,buffer);

                if ( gs.isExpanded() )
                {
                    generateGroupBoxes(depth+1, buffer, gs);
                }
            }
            catch (java.io.IOException e)
            {
                e.printStackTrace(System.err);
            }
        }
    }

    private String generateSelectionsXML(boolean deleteIfExists) throws FileAlreadyExistsException
    {
        String returnURL = null;

        try
        {
            File outFile = new File(DTFRunManager.getUploadFileDirectory()+"/"+_testDefs.getId()+"_"+getSelectionName()+".xml");

            if ( outFile.exists() )
            {
                if ( deleteIfExists )
                {
                    outFile.delete();
                }
                else
                {
                    throw new FileAlreadyExistsException("The file '"+outFile.getName()+"' already exists");
                }
            }

            PrintStream out = new PrintStream( new FileOutputStream(outFile) );

            out.println("<test_selection>");
            out.println("\t<description>"+getDescription()+"</description>");

            OSDetails[] oss = getSupportedOperatingSystems();

            for (int osCount=0;osCount<oss.length;osCount++)
            {
                out.println("\t<os id=\""+oss[osCount]._name+"\">");

                out.println("\t\t<product id=\""+getProductName()+"\">");

                outputGroupSelections( _testGroups, out, 3, oss[osCount]._id, false );

                out.println("\t\t</product>");

                out.println("\t</os>");
            }

            out.println("\t</test_selection>");

            out.close();

            returnURL = DTFRunManager.getUploadWebDirectory()+"/"+_testDefs.getId()+"_"+getSelectionName()+".xml";
        }
        catch (java.io.IOException e)
        {
            System.err.println("An unexpected exception occurred when trying to generate the selections file: "+e);
            e.printStackTrace(System.err);
        }

        return returnURL;
    }

    private void outputGroupSelections( GroupSelection testGroups, PrintStream out, int depth, String osId, boolean parentSelected )
    {
        Enumeration e = testGroups.elements();
        String indent = "";

        for (int count=0;count<depth;count++)
        {
            indent += '\t';
        }

        while ( e.hasMoreElements() )
        {
            GroupSelection gs = (GroupSelection)e.nextElement();

            if ( gs.size() > 0 )
            {
                out.println( indent + "<test_group id=\""+ gs.getGroupName() +"\">");

                outputGroupSelections( gs, out, depth + 1, osId, parentSelected || gs.isSelected(osId) );

                out.println( indent + "</test_group>");
            }
            else
            {
                System.out.println("Group = "+gs.getGroupName() +" osId="+ osId);
                out.println( indent + "<test id=\""+ gs.getGroupName() +"\" selected=\""+ (gs.isSelected(osId) || parentSelected) +"\"/>" );
            }
        }
    }

    public boolean save()
    {
        boolean result = false;

        try
        {
            String url = generateSelectionsXML(false);

            result = _testDefs.createSelections(getSelectionName(),getProductName(),url,getDescription()) != null;
        }
        catch (FileAlreadyExistsException e)
        {
            result = false;
        }

        return result;
    }
}
