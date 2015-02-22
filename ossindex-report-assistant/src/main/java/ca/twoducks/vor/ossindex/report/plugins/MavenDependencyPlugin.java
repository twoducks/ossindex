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
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.List;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

/** Extract dependencies from the pom.xml file.
 * 
 * @author Ken Duck
 *
 */
public class MavenDependencyPlugin extends AbstractScanPlugin
{

	/*
	 * (non-Javadoc)
	 * @see ca.twoducks.vor.ossindex.report.IScanPlugin#run(java.io.File)
	 */
	@Override
	public void run(File file)
	{
		if("pom.xml".equals(file.getName()))
		{
			Reader reader = null;
			try
			{
				reader = new FileReader(file);
				MavenXpp3Reader xpp3Reader = new MavenXpp3Reader();
				Model model = xpp3Reader.read(reader);
				List<Dependency> depList = model.getDependencies();
				for (Dependency dep : depList)
				{
					String groupId = dep.getGroupId();
					String artifactId = dep.getArtifactId();
					String version = dep.getVersion();
					config.addDependency(file, "maven", groupId, artifactId, version, null);
				}
			}
			catch (IOException | XmlPullParserException e)
			{
				System.err.println("Exception reading POM: " + e.getMessage());
			}
			finally
			{
				if(reader != null)
				{
					try
					{
						reader.close();
					}
					catch (IOException e)
					{
						System.err.println("Exception handling file: " + file);
					}
				}
			}
		}
	}

}
