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
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.csv.CSVPrinter;

/** Any single project may have several matched URIs (repositories). To explain, assume
 * we have three repositories A, B, and C, where each subsequent repository is cloned from
 * the previous.
 * 
 *    A -> B -> C
 * 
 * The report is configured against files that appear in project C, but project C only
 * changed one file, whereas project B changed one or more *different* files, and some
 * files in project A are unchanged by either B or C.
 * 
 * In this situation origin matching will provide projects A, B, and C as results.
 * 
 * It is important that we include all of these projects in the results, since vulnerabilities
 * and other interesting information may have been added at any one of these levels. For
 * this reason we provide a project GROUP which contains the projects. We treat them as
 * one, but can also work with them separately.
 * 
 * For now we group by name, which is imprecise but often sufficient.
 * 
 * @author Ken Duck
 *
 */
public class ProjectGroup
{
	/**
	 * The name of the group
	 */
	private String name;

	/**
	 * Collection of actual project configurations.
	 */
	private Set<ProjectConfig> members = new HashSet<ProjectConfig>();

	public ProjectGroup()
	{
	}

	/**
	 * 
	 * @param name
	 */
	public ProjectGroup(String name)
	{
		this.name = name;
	}

	/** Export CSV configuration information.
	 * 
	 * @param csvOut
	 * @param lookup File lookup information
	 * @throws IOException 
	 */
	public void exportCsv(CSVPrinter csvOut, Map<String, FileConfig> lookup) throws IOException
	{
		for(ProjectConfig project: members)
		{
			project.exportCsv(csvOut, lookup);
		}
	}

	/** Get the project with the specified SCM (which should be unique). If it does not exist
	 * then add one.
	 * 
	 * @param scmUri
	 * @param version
	 * @return
	 */
	public ProjectConfig getProject(String scmUri, String version)
	{
		for(ProjectConfig member: members)
		{
			try
			{
				URI memberUri = member.getScmUri();
				if(scmUri != null)
				{
					if(memberUri != null && scmUri.equals(memberUri.toString()))
					{
						if(version == null || version.isEmpty())
						{
							return member;
						}
						else
						{
							if(version.equals(member.getVersion()))
							{
								return member;
							}
						}
					}
				}
				else
				{
					if(memberUri == null && name.equals(member.getName()))
					{
						return member;
					}
				}
			}
			catch (URISyntaxException e)
			{
				throw new AssertionError(e);
			}
		}

		ProjectConfig config = new ProjectConfig(scmUri, version);
		members.add(config);
		return config;
	}

}
