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

import org.apache.commons.codec.digest.DigestUtils;

/** Information for an individual file.
 * 
 * @author Ken Duck
 *
 */
public class FileConfig
{
	private String digest;
	private String path;
	private String license;
	private String comment;
	
	/** 
	 * Indicate whether or not this file was ignored for analysis purposes. This is
	 * often done if the file is too small, since identifying a file origin in this
	 * circumstance is much more error prone.
	 */
	@SuppressWarnings("unused")
	private boolean ignored;

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
			
			// Progress information
			System.err.println(file);
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
}
