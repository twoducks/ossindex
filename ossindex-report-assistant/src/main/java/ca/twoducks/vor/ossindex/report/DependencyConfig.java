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

import java.net.URI;
import java.net.URISyntaxException;

/** Contains information about a file dependency.
 * 
 * The dependency information needs to be packaging system agnostic, but able to handle
 * information that each type of system may supply. For this reason the names of fields
 * may not match every system.
 * 
 * @author Ken Duck
 *
 */
public class DependencyConfig
{
	/**
	 * Type of dependency, eg:
	 *   o HTML
	 *   o Maven
	 *   o Node
	 *   o Ruby
	 *   o Java
	 *   o etc.
	 * 
	 * This will affect how the server side processes the data.
	 */
	private String type;
	
	/**
	 * URI/URL reference for the dependency. This could be a file, git repository, etc.
	 */
	private String ref;
	
	/**
	 * 
	 */
	private String groupId;
	
	/**
	 * A name for the package dependency, if applicable.
	 * For Maven this is the artifactId
	 */
	private String packageName;
	private String artifactId;
	
	/**
	 * Version descriptor. This can define a range of versions. Some possibilities:
	 * 
	 * From https://docs.npmjs.com/files/package.json:
	 * 
	 *   { "foo" : "1.0.0 - 2.9999.9999"
	 *     , "bar" : ">=1.0.2 <2.1.2"
  	 *     , "baz" : ">1.0.2 <=2.3.4"
  	 *     , "boo" : "2.0.1"
  	 *     , "qux" : "<1.0.0 || >=2.3.1 <2.4.5 || >=2.5.2 <3.0.0"
  	 *     , "asd" : "http://asdf.com/asdf.tar.gz"
  	 *     , "til" : "~1.2"
  	 *     , "elf" : "~1.2.3"
  	 *     , "two" : "2.x"
  	 *     , "thr" : "3.3.x"
  	 *     , "lat" : "latest"
  	 *     , "dyl" : "file:../dyl"
  	 *   }
	 */
	private String version;
	
	/**
	 * Comment describing the dependency
	 */
	private String comment;

	/** Construct a dependency to an external HTTP accessible file.
	 * 
	 * @param type 
	 * @param url
	 */
	public DependencyConfig(String type, URI uri)
	{
		this.type = type;
		this.ref = uri.toString();
	}

	/**
	 * 
	 * @param type
	 * @param groupId
	 * @param artifactId
	 * @param version
	 */
	public DependencyConfig(String type, String groupId, String artifactId, String version)
	{
		this.type = type;
		this.groupId = groupId;
		this.version = version;
		
		if("maven".equals(type)) this.artifactId = artifactId;
		else this.packageName = artifactId;
	}

	/** Get the URI corresponding to the dependency (if applicable).
	 * 
	 * @return
	 * @throws URISyntaxException 
	 */
	public URI getUri() throws URISyntaxException
	{
		return new URI(ref);
	}

	/** Add a comment for the dependency
	 * 
	 * @param comment
	 */
	public void setComment(String comment)
	{
		this.comment = comment;
	}
}
