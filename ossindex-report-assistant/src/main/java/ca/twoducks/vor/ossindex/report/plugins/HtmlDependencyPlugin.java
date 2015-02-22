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
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.io.FilenameUtils;
import org.cyberneko.html.parsers.DOMParser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import ca.twoducks.vor.ossindex.report.IScanPlugin;

/** This plugin parses HTML files and identifies external "link href" and 
 * "script src" dependencies. These must have absolute URLs with servers
 * specified, which means they are more likely dependencies and not internal
 * code.
 * 
 * These will be listed by name in the configuration file.
 * 
 * @author Ken Duck
 *
 */
public class HtmlDependencyPlugin extends AbstractScanPlugin implements IScanPlugin
{
	private static Set<String> supportedFileTypes = new HashSet<String>();
	static
	{
		supportedFileTypes.add("html");
		supportedFileTypes.add("jsp");
	}
	@Override
	public void run(File file)
	{
		if(fileSupported(file))
		{
			DOMParser parser = null;
			InputStream is = null;
			try
			{
				parser = new DOMParser();
				parser.setFeature("http://xml.org/sax/features/namespaces", false);
				is = new FileInputStream(file);
				InputSource source = new InputSource(is);
				parser.parse(source);
			}
			catch (SAXException | IOException e)
			{
				System.err.println("Exception parsing " + file + ": " + e.getMessage());
			}
			finally
			{
				try
				{
					if(is != null) is.close();
				}
				catch (IOException e)
				{
					System.err.println("Exception parsing " + file + ": " + e.getMessage());
				}
			}
			
			try
			{
				extractLinks(file, parser.getDocument());
			}
			catch (XPathExpressionException e)
			{
				System.err.println("Exception parsing " + file + ": " + e.getMessage());
			}
		}
	}

	/** Is the file within the supported file types.
	 * 
	 * @param file
	 * @return
	 */
	private boolean fileSupported(File file)
	{
		String ext = FilenameUtils.getExtension(file.getName());
		return supportedFileTypes.contains(ext);
	}

	/** Find the external links and add them as dependencies to the configuration.
	 * 
	 * @param document
	 * @throws XPathExpressionException 
	 */
	private void extractLinks(File file, Document document) throws XPathExpressionException
	{
		XPath xPath = XPathFactory.newInstance().newXPath();
		NodeList nodes = (NodeList) xPath.evaluate("//LINK", document.getDocumentElement(), XPathConstants.NODESET);
		extractLink(file, nodes, "href");
		
		nodes = (NodeList) xPath.evaluate("//SCRIPT", document.getDocumentElement(), XPathConstants.NODESET);
		extractLink(file, nodes, "src");

	}

	/** Find the external links and add them as dependencies to the configuration.
	 * 
	 * @param file
	 * @param nodes
	 * @param attribute Attribute that we will find the URL within
	 */
	private void extractLink(File file, NodeList nodes, String attribute)
	{
		if(nodes != null)
		{
			for (int i = 0; i < nodes.getLength(); ++i)
			{
			    Element node = (Element) nodes.item(i);
			    NamedNodeMap map = node.getAttributes();
			    Node hrefNode = map.getNamedItem(attribute);
			    if(hrefNode != null)
			    {
			    	String href = hrefNode.getNodeValue();
			    	if(href.startsWith("http://") || href.startsWith("https://"))
			    	{
			    		try
			    		{
							config.addDependency(file, new URL(href));
						}
			    		catch (MalformedURLException e)
			    		{
			    			System.err.println("Error parsing URL: " + href);
						}
			    	}
			    }
			}
		}
	}
	
}
