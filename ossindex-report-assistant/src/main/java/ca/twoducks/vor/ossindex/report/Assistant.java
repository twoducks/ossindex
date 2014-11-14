/**
 *	Copyright (c) 2014 TwoDucks Inc.
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
import java.io.StringWriter;
import java.io.Writer;

import ca.twoducks.vor.ossindex.OssIndexAccessUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/** The report assistant prepares a JSON configuration file which may be imported
 * into OSS Index to provide valuable information for a report. It does this by
 * locating all files within the "project" directory and calculating their SHA1
 * sums. The sums are written into the JSON configuration file which may then
 * be uploaded to OSS Index.
 * 
 * @author Ken Duck
 *
 */
public class Assistant
{
	/**
	 * Simple common utilities providing access to OSS Index
	 */
	private OssIndexAccessUtils utils = null;
	
	/**
	 * 
	 */
	Configuration config = new Configuration();

	/**
	 * Initialize the host connection.
	 * @throws IOException 
	 */
	public Assistant() throws IOException
	{
		utils = new OssIndexAccessUtils();
	}

	/** Scan the specified file/directory, reporting on any third party.
	 * 
	 * @param file
	 */
	public void scan(File file)
	{
		recursiveScan(file);
	}

	/** Recursively scan the specified file/directory, collecting the SHA1 sums
	 * 
	 * @param file
	 */
	private void recursiveScan(File file)
	{
		if(file.isFile())
		{
			try
			{
				config.addFile(file);
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		else
		{
			// Recursively do for all sub-folders and files.
			File[] children = file.listFiles();
			for (File child : children)
			{
				recursiveScan(child);
			}
		}
	}

	/** Export the data in JSON format
	 * 
	 * @return
	 */
	private String exportJson()
	{
		Writer writer = new StringWriter();
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		
		gson.toJson(config, writer);
		
		return writer.toString();
	}
	
	/** Main method. Very simple, does not perform sanity checks on input.
	 * 
	 * @param args
	 * @throws IOException 
	 */
	public static void main( String[] args ) throws IOException
	{
		if(args.length != 1)
		{
			System.err.println("Usage: FileDetails <directory>");
			System.exit(-1);
		}
		
		File file = new File(args[0]);
		Assistant assistant = new Assistant();
		assistant.scan(file);
		System.out.println(assistant.exportJson());
	}
}
