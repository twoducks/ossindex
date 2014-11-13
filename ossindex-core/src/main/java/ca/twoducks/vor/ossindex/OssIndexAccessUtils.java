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
package ca.twoducks.vor.ossindex;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

/** Common utility code for OSSIndex access. This class identifies the appropriate
 * server for API access, and provides a simple wrapper for requests.
 * 
 * @author Ken Duck
 *
 */
public class OssIndexAccessUtils
{
	/**
	 * Location of host with configuration information. This configuration is
	 * intended for future use where we may redirect subsequent queries to
	 * other servers for scalability purposes, or if the API server is moved
	 * for other reasons.
	 */
	private static String CONFIG_HOST = "https://ossindex.net";

	/**
	 * Actual API host we will be talking with.
	 */
	private String host = null;

	/** Constructor.
	 * 
	 * Ask the config server for the appropriate API host. Fall back to the
	 * configuration host if required.
	 * 
	 * @throws IOException
	 */
	public OssIndexAccessUtils() throws IOException
	{
		host = getRedirectHost();
		if(host == null) host = CONFIG_HOST;
	}
	
	/** Override the configuration host. Intended for testing.
	 * 
	 * @param host
	 */
	public static void setConfigHost(String host)
	{
		CONFIG_HOST = host;
	}

	/** Get the data at the specified path at OSS Index
	 * 
	 * @param path
	 * @return
	 * @throws IOException 
	 * @throws MalformedURLException 
	 */
	public String get(String path) throws MalformedURLException, IOException
	{
		return getUrl(new URL(host + path));
	}

	/** Ask the server for the appropriate API host to use.
	 * 
	 * @return
	 * @throws IOException 
	 * @throws  
	 */
	private String getRedirectHost() throws IOException
	{
		String host = null;
		InputStream is = null;

		try
		{
			String buf = getUrl(new URL(CONFIG_HOST + "/conf/api-host.properties"));
			String hostname = null;
			int port = 0;
			Properties props = new Properties();
			is = new ByteArrayInputStream(buf.getBytes());
			props.load(is);
			hostname = props.getProperty("hostname");
			if(props.containsKey("port")) port = Integer.parseInt(props.getProperty("port"));

			if(hostname != null)
			{
				if(port > 0) host = "https://" + hostname + ":" + port;
				else host = "https://" + hostname;
			}
		}
		catch(FileNotFoundException e)
		{
			// If there is no properties file at the host, ignore
		}
		finally
		{
			if(is != null)
			{
				is.close();
			}
		}

		return host;
	}

	/** Request JSON data for the specified URL.
	 * 
	 * @param url
	 * @return
	 * @throws IOException
	 */
	public String getUrl(URL url) throws IOException
	{
		BufferedReader rd = null;
		try
		{
			rd = new BufferedReader(new InputStreamReader(url.openStream()));

			StringBuffer result = new StringBuffer();
			String line = "";
			while ((line = rd.readLine()) != null)
			{
				result.append(line).append('\n');
			}

			return result.toString();
		}
		finally
		{
			try
			{
				if(rd != null) rd.close();
			}
			catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
