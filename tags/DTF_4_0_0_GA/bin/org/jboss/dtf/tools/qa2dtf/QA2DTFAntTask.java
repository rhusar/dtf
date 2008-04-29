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
package org.jboss.dtf.tools.qa2dtf;

import org.apache.tools.ant.Task;
import org.apache.tools.ant.BuildException;

import java.util.ArrayList;
import java.util.Hashtable;

/**
 * ANT task wrapper around the QA2DTF task.
 *
 * @version $Id: QA2DTFAntTask.java 170 2008-03-25 18:59:26Z jhalliday $
 * @author Richard A. Begg
 */
public class QA2DTFAntTask extends Task
{
    /** The base directory in which the config files will be found **/
    private String  _baseDir = null;

    /** A prefix for the name of the file to generate **/
    private String  _outputFilenamePrefix = null;

    /** The output directory **/
    private String  _outputDir = null;

    /** The name of the top-level XML file to generate **/
    private String  _topLevelOutputFilename = null;

	/** The location elements **/
	private ArrayList	_locations = new ArrayList();

	/** The task runner elements **/
	private ArrayList 	_taskRunners = new ArrayList();

	/** The properties filename **/
	private String	_propertiesFilename = null;

    public void setBasedir(String dir)
    {
        _baseDir = dir;
    }

    public void setOutputprefix(String filename)
    {
        _outputFilenamePrefix = filename;
    }

    public void setToplevel(String filename)
    {
        _topLevelOutputFilename = filename;
    }

	public void setProperties(String filename)
	{
		_propertiesFilename = filename;
	}

    public void setOutputdir(String dir)
    {
        _outputDir = dir;
    }

	public LocationAntElement createLocation()
	{
        LocationAntElement lae = new LocationAntElement();

		_locations.add(lae);

		return lae;
	}

	public TaskRunnerAntElement createTaskRunner()
	{
        TaskRunnerAntElement lae = new TaskRunnerAntElement();

		_taskRunners.add(lae);

		return lae;
	}

    public void execute() throws BuildException
    {
        if ( _baseDir == null )
        {
            throw new BuildException("The base directory (basedir) has not been specified");
        }

        log("Converting QA configuration into DTF xml");
        log("Prefix: "+_outputFilenamePrefix);
        log("Source: "+_baseDir);

        if ( _outputFilenamePrefix != null )
        {
            QA2DTF.setPrefix(_outputFilenamePrefix);
        }

        if ( _topLevelOutputFilename != null )
        {
            QA2DTF.setTopLevelFilename(_topLevelOutputFilename);
        }

        if ( _outputDir != null )
        {
            QA2DTF.setOutputDir(_outputDir);
        }

		if ( _propertiesFilename != null )
		{
			QA2DTF.setProperties(_propertiesFilename);
		}

		Hashtable locations = new Hashtable();
		for (int count=0;count<_locations.size();count++)
		{
			LocationAntElement lae = (LocationAntElement)_locations.get(count);

			locations.put(lae.getClassname(), lae.getLocation());
		}
		QA2DTF.setLocations(locations);

		Hashtable taskRunners = new Hashtable();
		for (int count=0;count<_taskRunners.size();count++)
		{
			TaskRunnerAntElement lae = (TaskRunnerAntElement)_taskRunners.get(count);

			taskRunners.put(lae.getClassname(), lae.getTaskRunner());
		}
		QA2DTF.setTaskRunners(taskRunners);

        QA2DTF.parseDir( _baseDir );
    }
}
