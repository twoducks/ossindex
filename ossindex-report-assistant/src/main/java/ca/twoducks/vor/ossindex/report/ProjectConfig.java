/**
 *	Copyright (c) 2015 TwoDucks Inc.
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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.csv.CSVPrinter;

/** This class contains information about a project itself, including:
 *   o name
 *   o version - version information if available
 *   o project - URL of 'project' page
 *   o scm - URI of source code repository
 *   o home - URL of 'home' page
 *   o cpe code
 *   o project level license(s)
 *   o identified files (digest pointer)
 *   o comment for user-provided information
 * 
 * @author Ken Duck
 *
 */
public class ProjectConfig
{
	private String name;
	private String description;
	private String version;
	private String project;
	private String scm;
	private String home;
	private Collection<String> cpes;
	private List<String> licenses = new LinkedList<String>();
	private List<String> files = new LinkedList<String>();
	private String comment;
	
	/** Get the project name.
	 * 
	 * @return
	 */
	public String getName()
	{
		return name;
	}
	
	/** Get the project version.
	 * 
	 * @return
	 */
	public String getVersion()
	{
		return version;
	}
	
	/** Get a URL that identifies the project.
	 * 
	 * @return
	 * @throws MalformedURLException
	 */
	public URL getProjectUrl() throws MalformedURLException
	{
		if(project == null) return null;
		return new URL(project);
	}
	
	/** Get the SCM URI that can be used to retrieve the source or build artifact.
	 * 
	 * @return
	 * @throws URISyntaxException
	 */
	public URI getScmUri() throws URISyntaxException
	{
		if(scm == null) return null;
		return new URI(scm);
	}
	
	/** Get the homepage URL if it is different from the project URL.
	 * 
	 * @return
	 * @throws MalformedURLException
	 */
	public URL getHomeUrl() throws MalformedURLException
	{
		if(home == null) return null;
		return new URL(home);
	}
	
	/** Get a list of all CPE matches against the project.
	 * 
	 * @return
	 */
	public Collection<String> getCpes()
	{
		return cpes;
	}
	
	/** Get a list of all licenses identified for the project itself.
	 * 
	 * @return
	 */
	public Collection<String> getLicenses()
	{
		return licenses;
	}
	
	/** Get a list of all file digests that were matched against the project.
	 * 
	 * @return
	 */
	public Collection<String> getFiles()
	{
		return files;
	}
	
	public String getComment()
	{
		return comment;
	}

	/** Export CSV configuration information.
	 * 
	 * @param csvOut
	 * @param lookup File lookup information
	 * @throws IOException 
	 */
	public void exportCsv(CSVPrinter csvOut, Map<String, FileConfig> lookup) throws IOException
	{
		for(String digest: files)
		{
			if(lookup.containsKey(digest))
			{
				FileConfig file = lookup.get(digest);
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
				
				row.add(file.getState());
				row.add(name);
				if(home != null) row.add(home);
				if(project != null) row.add(project);
				else row.add(scm);
				row.add(version);
				row.add(cpes);
				row.add(licenses);

				row.add(file.getLicense());
				row.add(description);
				row.add(digest);
				row.add(file.getComment());
				
				csvOut.printRecord(row);
			}
			else
			{
				System.err.println("Unmatched digest found in project: " + digest);
			}
			
		}
	}
}
