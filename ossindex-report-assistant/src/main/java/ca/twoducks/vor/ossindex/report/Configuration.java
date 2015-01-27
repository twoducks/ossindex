/**
 *	Copyright (c) 2014-2015 TwoDucks Inc.
 *	All rights reserved.
 *	
 *	Redistribution and use in source and binary forms, with or without
 *	modification, are permitted provided that the following conditions are met:
 *	    * Redistributions of source code must retain the above copyright
 *	      notice, this list of conditions and the following disclaimer.
 *	    * Redistributions in binary form must reproduce the above copyright
 *	      notice, this list of conditions and the following disclaimer in the
 *	      documentation and/or other materials provided with the distribution.
 *	    * Neither the name of the <organization> nor the
 *	      names of its contributors may be used to endorse or promote products
 *	      derived from this software without specific prior written permission.
 *	
 *	THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 *	ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *	WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *	DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 *	DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *	(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *	LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *	ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *	(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *	SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package ca.twoducks.vor.ossindex.report;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.csv.CSVPrinter;

/** Represent the configuration of an OSS Index report. This class can be exported
 * to and imported from a suitable JSON file. 
 * 
 * @author Ken Duck
 *
 */
public class Configuration
{
	/**
	 * Timestamp indicating when the configuration file was made/updated
	 */
	@SuppressWarnings("unused")
	private Long timestamp;
	
	/**
	 * List of classes representing individual files.
	 */
	private List<FileConfig> files = new LinkedList<FileConfig>();
	
	/**
	 * Map of project identifier (names) to ProjectGroup. A ProjectGroup collects similar
	 * projects together. For more information see the comment at the head of the ProjectGroup
	 * class itself.
	 */
	private SortedMap<String, ProjectGroup> projects = new TreeMap<String, ProjectGroup>();

	/**
	 * Initialize the configuration and set the creation time stamp.
	 */
	public Configuration()
	{
		touch();
	}

	/** Add the SHA1 sum of a file to the file list.
	 * 
	 * @param file
	 * @throws IOException 
	 */
	public void addFile(File file) throws IOException
	{
		FileConfig config = new FileConfig(file);
		files.add(config);
	}

	/**
	 * Update the configuration's timestamp.
	 */
	public void touch()
	{
		timestamp = (new Date()).getTime();
	}

	/** Merge configuration information from the provided configuration here.
	 * 
	 * "This" configuration should be the private configuration, whereas the
	 * argument should be the public configuration.
	 * 
	 * @param config
	 */
	public void merge(Configuration config)
	{
		// Build a file lookup
		Map<String,FileConfig> lookup = new HashMap<String,FileConfig>();
		for(FileConfig file: config.files)
		{
			lookup.put(file.getDigest(), file);
		}
		
		// Merge the foreign files
		for(FileConfig file: files)
		{
			String digest = file.getDigest();
			
			if(lookup.containsKey(digest))
			{
				file.merge(lookup.get(digest));
			}
			else
			{
				files.add(file);
			}
		}

		if(projects != null && !projects.isEmpty())
		{
			if(config.projects != null && !config.projects.isEmpty())
			{
				System.err.println("Projects merge not supported. Keeping public version.");
			}
		}
		else
		{
			if(config.projects != null && !config.projects.isEmpty())
			{
				projects = config.projects;
			}
		}
	}

	/** Export CSV configuration information.
	 * 
	 * @param csvOut
	 * @throws IOException 
	 */
	public void exportCsv(CSVPrinter csvOut) throws IOException
	{
		// Build a file lookup
		Map<String,FileConfig> lookup = new HashMap<String,FileConfig>();
		for(FileConfig file: files)
		{
			lookup.put(file.getDigest(), file);
		}
		
		for(Map.Entry<String, ProjectGroup> entry: projects.entrySet())
		{
			ProjectGroup group = entry.getValue();
			group.exportCsv(csvOut, lookup);
		}
	}
}
