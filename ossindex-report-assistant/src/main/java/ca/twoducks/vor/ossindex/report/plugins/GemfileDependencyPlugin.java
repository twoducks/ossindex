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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import ca.twoducks.vor.ossindex.report.IConfiguration;

/** Dependency plugin for Gemfiles. This uses *very* dirty parsing techniques.
 * 
 * @author Ken Duck
 *
 */
public class GemfileDependencyPlugin extends AbstractScanPlugin
{
	/*
	 * (non-Javadoc)
	 * @see ca.twoducks.vor.ossindex.report.IScanPlugin#run(java.io.File)
	 */
	@Override
	public void run(File file)
	{
		String fname = file.getName();
		if("Gemfile".equals(fname) || fname.endsWith(".gemfile"))
		{
			BufferedReader in = null;
			try
			{
				GemLineParser parser = new GemLineParser(file, config);
				
				in = new BufferedReader(new FileReader(file));
				String line = in.readLine();
				while(line != null)
				{
					line = line.trim();
					line = line.replaceAll("\"", "'");
					if(line.startsWith("source ")) handleSource(parser, line);
					else if(line.startsWith("gem ")) parser.parse(line);
					else if(line.startsWith("group ")) handleGroup(parser, line);
					else if(line.startsWith("end")) handleEnd(parser, line);

					line = in.readLine();
				}
			}
			catch (IOException e)
			{
				System.err.println("Error parsing " + file + ": " + e.getMessage());
			}
			finally
			{
				try
				{
					in.close();
				}
				catch (IOException e)
				{
					System.err.println("Error parsing " + file + ": " + e.getMessage());
				}
			}
		}
	}

	/** Currently we are assuming that source lines (if there are more than one) override
	 * each other.
	 * 
	 * @param line
	 */
	private void handleSource(GemLineParser parser, String line)
	{
		String source = line.substring(6).trim();
		if(":rubygems".equals(source)) source = "https://rubygems.org";
		else
		{
			String newSource = GemLineParser.getString(source);
			if(newSource != null) source = newSource;
		}
		parser.setSource(source);
	}

	/** Currently we are assuming that groups cannot be nested
	 * 
	 * @param line
	 */
	private void handleGroup(GemLineParser parser, String line)
	{
		parser.setGroup(line.substring(5).trim());
	}

	/** Currently we are assuming that groups are the only thing that "end"
	 * 
	 * @param line
	 */
	private void handleEnd(GemLineParser parser, String line)
	{
		parser.setGroup(null);
	}
}

/** Dirty parser for a single 'gem' line in a gemfile. This will add a
 * dependency in the config file.
 * 
 * @author Ken Duck
 *
 */
class GemLineParser
{
	private static final int TOKEN_UNKNOWN = 1;
	private static final int TOKEN_STRING = 2;
	private static final int TOKEN_GIT = 3;
	private static final int TOKEN_BRANCH = 4;
	private static final int TOKEN_UNSUPPORTED = 5;
	private static final int TOKEN_TAG = 6;
	private static final int TOKEN_PLATFORMS = 7;
	
	private File file;
	private IConfiguration config;
	private String source;
	private String group;
	
	/**
	 * Gem name
	 */
	private String gemName;
	private String version;
	private URI uri;

	/**
	 * 
	 * @param file
	 * @param config
	 */
	public GemLineParser(File file, IConfiguration config)
	{
		this.file = file;
		this.config = config;
	}
	
	public void setSource(String source)
	{
		this.source = source;
	}

	public void setGroup(String group)
	{
		this.group = group;
	}

	/**
	 * Run the parser
	 * 
	 * @param line line to parse.
	 */
	public void parse(String line)
	{
		clear();
		if(line.startsWith("gem "))
		{
			line = line.substring(4).trim();
		}
		parseGem(line);
	}

	private void clear()
	{
		gemName = null;
		version = null;
		uri = null;
	}

	/** Parse out the gem name
	 * 
	 * @param line
	 */
	private void parseGem(String line)
	{
		int index = getSeparatorIndex(line);
		if(index < 0)
		{
			gemName = getString(line);
			parseEnd();
		}
		else
		{
			gemName = getString(line.substring(0, index));
			String rest = line.substring(index + 1).trim();
			switch(getTokenType(rest))
			{
			case TOKEN_STRING:
				parseVersion(rest);
				break;
			default:
				parseNext(rest);
				break;
			}
		}
	}
	
	/** Find the next argument separator
	 * 
	 * @param line
	 * @return
	 */
	private int getSeparatorIndex(String line)
	{
		int commaIndex = line.indexOf(',');
		int startBracketIndex = line.indexOf('[');
		if(startBracketIndex >= 0 && startBracketIndex < commaIndex)
		{
			int endBracketIndex = line.indexOf(']', startBracketIndex);
			commaIndex = line.indexOf(',', endBracketIndex);
			if(commaIndex <= endBracketIndex) commaIndex = -1;
		}
		return commaIndex;
	}

	/**
	 * 
	 * @param line
	 */
	private void parseVersion(String line)
	{
		int index = getSeparatorIndex(line);
		if(index < 0)
		{
			version = getString(line);
			parseEnd();
		}
		else
		{
			version = line.substring(0, index);
			String rest = line.substring(index + 1).trim();
			parseNext(rest);
		}
	}

	/** Figure out the next
	 * 
	 * @param line
	 */
	private void parseNext(String line)
	{
		switch(getTokenType(line))
		{
		case TOKEN_GIT:
			parseGit(line);
			break;
		case TOKEN_TAG:
			parseTag(line);
			break;
		case TOKEN_UNSUPPORTED:
			System.err.println("Unsupported token at " + line);
		case TOKEN_PLATFORMS:
		case TOKEN_UNKNOWN:
			int index = getSeparatorIndex(line);
			if(index < 0) parseEnd();
			else parseNext(line.substring(index + 1).trim());
			break;
		default:
			System.err.println("parse failed at \"" + line + "\"");
			parseEnd();
			break;
		}
	}

	/** Parse a git argument
	 * 
	 *   :git => "git://github.com/thoughtbot/akephalos.git"
	 * 
	 * @param line
	 */
	private void parseGit(String line)
	{
		line = line.substring(4).trim();
		if(line.startsWith("=>"))
		{
			line = line.substring(3).trim();
			int index = getSeparatorIndex(line);
			if(index < 0)
			{
				try
				{
					uri = new URI(getString(line));
				}
				catch(URISyntaxException e)
				{
					System.err.println("Invalid URI: " + getString(line));
				}
				parseEnd();
			}
			else
			{
				parseNext(line.substring(index + 1).trim());
			}
		}
		else
		{
			System.err.println("parse failed at \"" + line + "\"");
			parseEnd();
		}
	}

	/** Parse a tag argument
	 * 
	 *   :tag => '1.4'
	 * 
	 * @param line
	 */
	private void parseTag(String line)
	{
		line = line.substring(4).trim();
		if(line.startsWith("=>"))
		{
			line = line.substring(3).trim();
			int index = getSeparatorIndex(line);
			if(index < 0)
			{
				version = getString(line);
				parseEnd();
			}
			else
			{
				parseNext(line.substring(index + 1).trim());
			}
		}
		else
		{
			System.err.println("parse failed at \"" + line + "\"");
			parseEnd();
		}
	}

	/** Get rid of the beginning and end "'" marks.
	 * 
	 * @param line
	 * @return
	 */
	static String getString(String line)
	{
		if(line.startsWith("'"))
		{
			line = line.substring(1);
		}
		else
		{
			System.err.println("Invalid string (no start quote): " + line);
			return null;
		}
		if(line.endsWith("'"))
		{
			line = line.substring(0, line.length() - 1);
		}
		else
		{
			System.err.println("Invalid string (no end quote): '" + line);
			return null;
		}
		return line;
	}

	/**
	 * 
	 * @param line
	 * @return
	 */
	private int getTokenType(String line)
	{
		if(line.startsWith("'")) return TOKEN_STRING;
		if(line.startsWith(":platforms")) return TOKEN_PLATFORMS;
		if(line.startsWith(":require")) return TOKEN_UNKNOWN;
		if(line.startsWith(":git")) return TOKEN_GIT;
		if(line.startsWith(":branch")) return TOKEN_UNSUPPORTED;
		if(line.startsWith(":tag")) return TOKEN_TAG;
		if(line.startsWith(":ref")) return TOKEN_UNSUPPORTED;
		if(line.startsWith(":path")) return TOKEN_UNKNOWN;
		if(line.startsWith(":group")) return TOKEN_UNKNOWN;
		return 0;
	}

	/** Parsing has ended.
	 * 
	 * @param line
	 */
	private void parseEnd()
	{
		StringBuilder comment = new StringBuilder("{");
		if(source != null) comment.append("\"source\": \"" + source + "\"");
		if(group != null) comment.append("\"group\": \"" + group + "\"");
		comment.append("}");
		
		if(uri != null)
		{
			config.addDependency(file, "bundler", gemName, uri, version, comment.toString());
		}
		else
		{
			config.addDependency(file, "bundler", gemName, version, comment.toString());
		}
	}
}