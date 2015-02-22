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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.codec.digest.DigestUtils;

/** Information for an individual file.
 * 
 * @author Ken Duck
 *
 */
public class FileConfig
{
	private String name;
	private String digest;
	private String path;
	private String license;
	private String comment;
	private String state;

	/** 
	 * Indicate whether or not this file was ignored for analysis purposes. This is
	 * often done if the file is too small, since identifying a file origin in this
	 * circumstance is much more error prone.
	 */
	private boolean ignored;
	
	/**
	 * List of dependencies found in the file.
	 */
	private Set<DependencyConfig> dependencies = null;

	/**
	 * 
	 * @param file
	 * @throws IOException
	 */
	public FileConfig(File file) throws IOException
	{
		// Get the SHA1 sum for a file, then check if the MD5 is listed in the
		// OSS Index (indicating it is third party code).
		FileInputStream is = null;
		try
		{
			is = new FileInputStream(file);
			digest = DigestUtils.shaHex(is);
			path = file.getPath();
		}
		finally
		{
			if(is != null)
			{
				is.close();
			}
		}
	}
	
	/** SHA1 digest for the file. Note that this is a platform dependent value.
	 * 
	 * @return
	 */
	public String getDigest()
	{
		return digest;
	}
	
	/** Best known name for the file
	 * 
	 * @return
	 */
	public String getName()
	{
		return name;
	}
	
	/** Local path to the file.
	 * 
	 * @return
	 */
	public String getPath()
	{
		return path;
	}
	
	/** Text name of a license found within the file itself, often in a header comment.
	 * 
	 * @return
	 */
	public String getLicense()
	{
		return license;
	}
	
	/** The comment provides a location for user-formatted information
	 * about the file.
	 * 
	 * @return
	 */
	public String getComment()
	{
		return comment;
	}
	
	/** Merge the data from the given file into the fields that are not currently
	 * filled by anything else.
	 * 
	 * @param file
	 */
	public void merge(FileConfig file)
	{
		if(name == null) name = file.name;
		if(path == null) path = file.path;
		if(license == null) license = file.license;
		if(comment == null) comment = file.comment;
		if(state == null) state = file.state;
		if(file.ignored == true) ignored = true;
		
		// Use the path to get an optimal name
		if(path != null)
		{
			File f = new File(path);
			name = f.getName();
		}
	}
	
	/** Get the analysis state of the file.
	 * 
	 * @return
	 */
	public String getState()
	{
		return state;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o)
	{
		if(o instanceof FileConfig)
		{
			return ((FileConfig)o).digest.equals(digest);
		}
		return false;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		return digest.hashCode();
	}

	/** Returns true if the file was ignored for project identification purposes.
	 * 
	 * @return
	 */
	public boolean isIgnored()
	{
		return ignored;
	}

	/** Add an HTML dependency to the file. This could be an external JavaScript or CSS file.
	 * 
	 * @param type Type of dependency (HTML, Maven, Node, Ruby, Java, etc.)
	 * @param uri
	 */
	public void addDependency(String type, String artifactId, URI uri, String version, String comment)
	{
		DependencyConfig dep = new DependencyConfig(type, artifactId, uri, version);
		dep.setComment(comment);
		if(dependencies == null) dependencies = new HashSet<DependencyConfig>();
		dependencies.add(dep);
	}

	/** Add a package/version to the dependency list.
	 * 
	 * @param type Type of dependency (HTML, Maven, Node, Ruby, Java, etc.)
	 * @param pkgName
	 * @param version
	 */
	public void addDependency(String type, String pkgName, String artifactId, String version, String comment)
	{
		DependencyConfig dep = new DependencyConfig(type, pkgName, artifactId, version);
		dep.setComment(comment);
		if(dependencies == null) dependencies = new HashSet<DependencyConfig>();
		dependencies.add(dep);
	}
}
