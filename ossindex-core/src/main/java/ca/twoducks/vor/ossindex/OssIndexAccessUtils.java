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

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

/** Common utility code for OSSIndex access. This class identifies the appropriate
 * server for API access, and provides a simple wrapper for requests.
 * 
 * @author Ken Duck
 *
 */
public class OssIndexAccessUtils
{
	/**
	 * System property to be set to specify the OSSIndex user name.
	 */
	private static final String OSSINDEX_USERNAME = "OSSINDEX_USERNAME";

	/**
	 * System property to be set to specify the OSSIndex password.
	 */
	private static final String OSSINDEX_PASSWORD = "OSSINDEX_PASSWORD";

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

	/**
	 * Client through which the website connection is made
	 */
	private CloseableHttpClient client;

	/**
	 * Client context, required for handling authentication
	 */
	private HttpClientContext localContext;

	/** Constructor.
	 * 
	 * Ask the config server for the appropriate API host. Fall back to the
	 * configuration host if required.
	 * 
	 * The username and password supplied through the OSSINDEX_USERNAME and
	 * OSSINDEX_PASSWORD environment variables will be used.
	 * 
	 * @throws IOException
	 */
	public OssIndexAccessUtils() throws IOException
	{
		init(null, null);
	}

	/** Constructor.
	 * 
	 * Ask the config server for the appropriate API host. Fall back to the
	 * configuration host if required.
	 * 
	 * Supply a username password for authentication.
	 * 
	 * @param username
	 * @param password
	 * @throws IOException
	 */
	public OssIndexAccessUtils(String username, String password) throws IOException
	{
		init(username, password);
	}

	/**
	 * 
	 * @param username
	 * @param password
	 * @throws IOException
	 */
	private void init(String username, String password) throws IOException
	{
		client = HttpClientBuilder.create().build();
		
		host = getRedirectHost();
		if(host == null) host = CONFIG_HOST;

		if(username == null) username = System.getProperty(OSSINDEX_USERNAME);
		if(password == null) password = System.getProperty(OSSINDEX_PASSWORD);

		if(username == null)
		{
			throw new IllegalArgumentException("FATAL: Missing username. The username may be either passed in as an argument to the constructor, or specified as a system property to the JVM (-DOSSINDEX_USERNAME='name')");
		}
		if(password == null)
		{
			throw new IllegalArgumentException("FATAL: Missing password. The password may be either passed in as an argument to the constructor, or specified as a system property to the JVM (-DOSSINDEX_PASSWORD='name')");
		}

		final String user = username;
		final String pass = password;

		CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
		credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(user + ":" + pass));
		localContext = HttpClientContext.create();
		localContext.setCredentialsProvider(credentialsProvider);
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
		catch(HttpStatusException e)
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
		HttpGet httpget = new HttpGet(url.toString());
		CloseableHttpResponse response = client.execute(httpget, localContext);
		try
		{
			int code = response.getStatusLine().getStatusCode();
			switch(code)
			{
			case 200: break;
			default:
				throw new HttpStatusException(url, 404);
			}
			return EntityUtils.toString(response.getEntity());
		}
		finally
		{
			response.close();
		}
	}

}
