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
import java.io.IOException;
import java.net.URI;
import java.net.URL;

/** Allows us to swap in different configurations. Specifically for testing.
 * 
 * @author Ken Duck
 *
 */
public interface IConfiguration
{
	/**
	 * 
	 * @param file
	 */
	void addFile(File file) throws IOException;
	
	/**
	 * 
	 * @param file
	 * @param type Type of dependency (HTML, Maven, Node, Ruby, Java, etc.)
	 * @param gemName
	 * @param version
	 * @param comment
	 */
	void addDependency(File file, String type, String gemName, String version, String comment);

	/**
	 * 
	 * @param file
	 * @param type Type of dependency (HTML, Maven, Node, Ruby, Java, etc.)
	 * @param url
	 */
	void addDependency(File file, String type, URL url);

	/**
	 * 
	 * @param file
	 * @param type Type of dependency (HTML, Maven, Node, Ruby, Java, etc.)
	 * @param gemName
	 * @param uri
	 * @param version 
	 * @param comment
	 */
	void addDependency(File file, String type, String gemName, URI uri, String version, String comment);
	
	/**
	 * 
	 * @param file
	 * @param type Type of dependency (HTML, Maven, Node, Ruby, Java, etc.)
	 * @param uri
	 * @param comment
	 */
	void addDependency(File file, String type, URI uri, String comment);

	/**
	 * 
	 * @param file
	 * @param type Type of dependency (HTML, Maven, Node, Ruby, Java, etc.)
	 * @param groupId
	 * @param artifactId
	 * @param version
	 * @param comment
	 */
	void addDependency(File file, String type, String groupId, String artifactId, String version, String comment);



}
