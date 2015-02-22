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
package ca.twoducks.vor.ossindex.report.plugins;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.codec.digest.DigestUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/** Extract all dependencies from the node.js "package.json" file.
 * 
 * @author Ken Duck
 *
 */
public class NodeDependencyPlugin extends AbstractScanPlugin
{

	/*
	 * (non-Javadoc)
	 * @see ca.twoducks.vor.ossindex.report.IScanPlugin#run(java.io.File)
	 */
	@Override
	public void run(File file)
	{
		if("package.json".equals(file.getName()))
		{
			Reader reader = null;
			Gson gson = new GsonBuilder().create();
			try
			{
				reader = new FileReader(file);
				PackageJson pkg = gson.fromJson(reader, PackageJson.class);
				Map<String,String> deps = pkg.getDependencies();
				processDependencies(file, deps, null);
				
				deps = pkg.getOptionalDependencies();
				processDependencies(file, deps, "optional");
				
				deps = pkg.getDevDependencies();
				processDependencies(file, deps, "dev");
			}
			catch (IOException e)
			{
				System.err.println("Exception parsing " + file + ": " + e.getMessage());
			}
			finally
			{
				try
				{
					reader.close();
				}
				catch (IOException e)
				{
					System.err.println("Exception parsing " + file + ": " + e.getMessage());
				}
			}

		}
	}

	/**
	 * 
	 * @param file
	 * @param deps
	 * @comment A comment to add to each of these dependencies
	 */
	private void processDependencies(File file, Map<String, String> deps, String comment)
	{
		if(deps == null) return;
		
		for(Entry<String,String> entry: deps.entrySet())
		{
			String pkgName = entry.getKey();
			String version = entry.getValue();
			
			// Local system dependencies should be converted to checksums
			if(version.startsWith("file:"))
			{
				try
				{
					version = getChecksum(file, version.substring(5));
					if(version != null)
					{
						config.addDependency(file, "npm", pkgName, version, comment);
					}
				}
				catch(IOException e)
				{
					System.err.println("Exception handling " + version + ": " + e.getMessage());
				}
			}
			else if(version.indexOf("://") > 0)
			{
				try
				{
					config.addDependency(file, "npm", new URI(version), comment);
				}
				catch (URISyntaxException e)
				{
					System.err.println("Exception parsing uri " + version + ": " + e.getMessage());
				}
			}
			else if(version.indexOf('/') > 0)
			{
				try
				{
					config.addDependency(file, "npm", new URI("git://" + version), comment);
				}
				catch (URISyntaxException e)
				{
					System.err.println("Exception parsing uri " + version + ": " + e.getMessage());
				}
			}
			else
			{
				config.addDependency(file, "npm", pkgName, version, comment);
			}
		}
	}

	/** Get the SHA checksum for a file found relative to the specified location
	 * 
	 * @param file File which the path may be relative to
	 * @param path
	 * @return
	 * @throws IOException 
	 */
	private String getChecksum(File file, String path) throws IOException
	{
		File target = null;
		if(path.startsWith("/"))
		{
			target = new File(path);
		}
		else
		{
			target = new File(file, path);
		}
		InputStream is = null;
		try
		{
		is = new FileInputStream(target);
		return DigestUtils.shaHex(is);
		}
		finally
		{
			if(is != null)
			{
				is.close();
			}
		}
	}

}

/** Simple file for getting the contents of a package.xml file using Gson.
 * 
 * @author Ken Duck
 *
 */
class PackageJson
{
	private Map<String,String> dependencies;
	private Map<String,String> devDependencies;
	private Map<String,String> optionalDependencies;
	
	public Map<String,String> getDependencies()
	{
		return dependencies;
	}
	
	public Map<String,String> getOptionalDependencies()
	{
		return optionalDependencies;
	}

	public Map<String,String> getDevDependencies()
	{
		return devDependencies;
	}
}