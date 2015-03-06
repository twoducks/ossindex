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
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
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
public class Configuration implements IConfiguration
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
	 * Lookup table allowing us to find a FileConfig mapping to a specified File. This is useful
	 * when building a configuration.
	 */
	transient private Map<File, FileConfig> fileLookup = new HashMap<File, FileConfig>();
	
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
		fileLookup.put(file,  config);
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
		// Build a file lookup of public files
		Map<String,FileConfig> publicLookup = new HashMap<String,FileConfig>();
		for(FileConfig file: config.files)
		{
			publicLookup.put(file.getDigest(), file);
		}
		
		// Loop through private files, merging data from public when available
		Map<String,FileConfig> privateLookup = new HashMap<String,FileConfig>();
		for(FileConfig file: files)
		{
			String digest = file.getDigest();
			
			if(publicLookup.containsKey(digest))
			{
				file.merge(publicLookup.get(digest));
			}
			privateLookup.put(file.getDigest(), file);
		}
		
		// Assign new file collection to configuration
		files = new LinkedList<FileConfig>();
		files.addAll(privateLookup.values());

		if(projects != null && !projects.isEmpty())
		{
			if(config.projects != null && !config.projects.isEmpty())
			{
				System.err.println("Projects merge not supported. Keeping private version.");
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
	 * @param includeImages Indicates whether images should be included in the CSV output
	 * @param includeArtifacts Indicates whether artifacts should be included in the CSV output
	 * @throws IOException 
	 */
	public void exportCsv(CSVPrinter csvOut, boolean includeArtifacts, boolean includeImages) throws IOException
	{
		// Build a file lookup
		Map<String,FileConfig> lookup = new HashMap<String,FileConfig>();
		for(FileConfig file: files)
		{
			if(!includeArtifacts && file.isArtifact()) continue;
			if(!includeImages && file.isImage()) continue;
			String digest = file.getDigest();
			if(!lookup.containsKey(digest))
			{
				lookup.put(file.getDigest(), file);
			}
			else
			{
				// This is not an issue, really. Duplicates can happen when the file
				// is found in multiple locations. We *may* want to reduce duplicates to
				// reduce the size of the file, but proximity may be useful in identifying
				// files.
				// System.err.println("Duplicate digest: " + digest);
			}
		}
		
		for(Map.Entry<String, ProjectGroup> entry: projects.entrySet())
		{
			ProjectGroup group = entry.getValue();
			group.exportCsv(csvOut, lookup);
		}
		
		
		for(FileConfig file: files)
		{
			if(file.isIgnored())
			{
				List<Object> row = new ArrayList<Object>();
				String path = file.getPath();
				if(path != null && !path.isEmpty())
				{
					row.add(path);
				}
				else
				{
					row.add(file.getName());
				}

				row.add("UNASSIGNED");
				row.add(""); // project name
				row.add(""); // project url
				row.add(""); // project version
				row.add(""); // project cpe
				row.add("File below 200 byte minimum"); // project description
				row.add(file.getDigest());
				row.add(file.getComment());

				csvOut.printRecord(row);


			}
		}
	}

	/** Add a dependency from the specified file to a particular URL. For example, the file
	 * may be an HTML file, and the dependency to a JavaScript or CSS file.
	 * 
	 * @param file
	 * @param type Type of dependency (HTML, Maven, Node, Ruby, Java, etc.)
	 * @param url
	 */
	public void addDependency(File file, String type, URL url)
	{
		try
		{
			addDependency(file, type, url.toURI(), null);
		}
		catch (URISyntaxException e)
		{
			System.err.println("Exception handling URL: " + url);
		}
	}

	/**
	 * 
	 * @param file
	 * @param type Type of dependency (HTML, Maven, Node, Ruby, Java, etc.)
	 * @param uri
	 * @param comment Comment to add with dependency
	 */
	public void addDependency(File file, String type, URI uri, String comment)
	{
		addDependency(file, type, null, uri, null, comment);
	}
	
	/*
	 * (non-Javadoc)
	 * @see ca.twoducks.vor.ossindex.report.IConfiguration#addDependency(java.io.File, java.lang.String, java.lang.String, java.net.URI, java.lang.String)
	 */
	@Override
	public void addDependency(File file, String type, String artifactId, URI uri, String version, String comment)
	{
		if(fileLookup.containsKey(file))
		{
			FileConfig fconf = fileLookup.get(file);
			fconf.addDependency(type, artifactId, uri, version, comment);
		}
		else
		{
			throw new IllegalArgumentException("File must be added to configuration before dependencies are added");
		}
	}
	/**
	 * 
	 * @param file
	 * @param type Type of dependency (HTML, Maven, Node, Ruby, Java, etc.)
	 * @param pkgName
	 * @param version
	 * @param comment Comment to add with dependency
	 */
	public void addDependency(File file, String type, String pkgName, String version, String comment)
	{
		if(fileLookup.containsKey(file))
		{
			FileConfig fconf = fileLookup.get(file);
			fconf.addDependency(type, null, pkgName, version, comment);
		}
		else
		{
			throw new IllegalArgumentException("File must be added to configuration before dependencies are added");
		}
	}

	/**
	 * 
	 * @param file
	 * @param type Type of dependency (HTML, Maven, Node, Ruby, Java, etc.)
	 * @param groupId
	 * @param artifactId
	 * @param version
	 * @param comment Comment to add with dependency
	 */
	public void addDependency(File file, String type, String groupId, String artifactId, String version, String comment)
	{
		if(fileLookup.containsKey(file))
		{
			FileConfig fconf = fileLookup.get(file);
			fconf.addDependency(type, groupId, artifactId, version, comment);
		}
		else
		{
			throw new IllegalArgumentException("File must be added to configuration before dependencies are added");
		}
	}

}
