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
package ca.twoducks.vor.ossindex.examples;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.codec.digest.DigestUtils;

import ca.twoducks.vor.ossindex.OssIndexAccessUtils;

/**
 * This sample code illustrates how to access the OSS Index search API to identify
 * third party code in a local directory.
 * 
 * The code generates SHA1 sums for the local files and uses the REST API to
 * search for open source code with matching check sums. Successful searches return
 * a simple URL for requesting additional information.
 * 
 * This URL is visited to retrieve further details about the file, including
 * matching file name and path and the project origin, otherwise "PENDING" is
 * returned as the origin and the first matching artifact/project is returned.
 * 
 * When PENDING is returned OSSIndex initiates a full database search to find the
 * most likely file origin, so running the same query at a future time should
 * provide new results.
 *
 */
public class FileDetails 
{
	/**
	 * Simple common utilities providing access to OSS Index
	 */
	private OssIndexAccessUtils utils = null;

	/**
	 * Regex pattern used to pull file link out of JSON search results.
	 */
	private Pattern p = Pattern.compile(".*\"file_json\": *\"([^\"]+)\".*");
		
	/**
	 * Initialize the host connection.
	 * @throws IOException 
	 */
	public FileDetails() throws IOException
	{
		utils = new OssIndexAccessUtils();
	}
	
	/** Scan the specified file/directory, reporting on any third party.
	 * 
	 * @param file
	 */
	public void scan(File file)
	{
		System.out.println("Identified third party code in " + file);
		recursiveScan(file);
	}

	/** Recursively scan the specified file/directory, reporting on any third party.
	 * 
	 * @param file
	 */
	private void recursiveScan(File file)
	{
		if(file.isFile())
		{
			// Get the SHA1 sum for a file, then check if the MD5 is listed in the
			// OSS Index (indicating it is third party code).
			FileInputStream is = null;
			try
			{
				is = new FileInputStream(file);
				//				String hash = DigestUtils.md5Hex(is);
				String hash = DigestUtils.shaHex(is);
				
				String searchResult = getSearchResult(hash);
				if(searchResult != null)
				{
					URL fileUrl = getFileUrl(searchResult);
					if(fileUrl != null) printFileDetails(fileUrl);
				}
			}
			catch(IOException e)
			{
				System.err.println("[" + e.getMessage() + "] " + file);
			}
			finally
			{
				if(is != null)
				{
					try
					{
						is.close();
					}
					catch (IOException e)
					{
						System.err.println("[" + e.getMessage() + "] " + file);
					}
				}
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


	/** The search results are basically a JSON file with the outer {} trimmed off.
	 * We will use VERY basic REGEX matching to pull values instead of full
	 * JSON parsing for simplicity sake.
	 * 
	 * This uses values from the search result to perform additional queries to OSS Index
	 * to get file details.
	 * 
	 * @param request
	 * @return
	 * @throws MalformedURLException 
	 */
	private URL getFileUrl(String request) throws MalformedURLException
	{
		request = request.replaceAll("[\n\r]", "");
		Matcher m = p.matcher(request);
		if(m.matches())
		{
			return new URL(m.group(1));
		}
		else
		{
			System.err.println("Cannot get file URL from: " + request);
		}
		return null;
	}

	/** Check with the OSS Index REST API to identify third party, and return
	 * the JSON results with the outer braces {} trimmed off.
	 * 
	 * @param md5
	 * @return
	 */
	private String getSearchResult(String md5)
	{
		try
		{
			String buf = utils.get("/api/search/" + md5);
			buf = buf.replaceAll("\\n", "");

			// If the result is an empty JSON object {} there is no hit,
			// otherwise there is.
			buf = buf.trim();
			if(buf.startsWith("{")) buf = buf.substring(1);
			if(buf.endsWith("}")) buf = buf.substring(0, buf.length() - 1);
			buf = buf.trim();
			if(!buf.isEmpty()) return buf;
		}
		catch(IOException e)
		{
			throw new IllegalArgumentException(e);
		}
		return null;
	}
	
	/** Given a URL for a file, print the JSON details known about this file.
	 * 
	 * @param fileUrl
	 * @throws IOException 
	 */
	private void printFileDetails(URL fileUrl) throws IOException
	{
		// Request the file details.
		String buf = utils.getUrl(fileUrl);
		System.out.println(buf);
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
		FileDetails search = new FileDetails();
		search.scan(file);
	}

}
