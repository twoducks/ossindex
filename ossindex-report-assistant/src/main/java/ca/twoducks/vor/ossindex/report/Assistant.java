/**
 *	Copyright (c) 2014-2015 TwoDucks Inc.
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
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/** The report assistant prepares a JSON configuration file which may be imported
 * into OSS Index to provide valuable information for a report. It does this by
 * locating all files within the "project" directory and calculating their SHA1
 * sums. The sums are written into the JSON configuration file which may then
 * be uploaded to OSS Index.
 * 
 * The assistant may be run in several different ways:
 * 
 *   o "Generation" mode, wherein an input directory is provided and initial configuration
 *     files are generated.
 *   o "Merge" mode, wherein two configuration files are provided and their data is
 *     merged together to produce a new configuration file.
 * 
 * When run in 'generation' mode the assistant prepares:
 *   o the "public" configuration file, which contains no identifying information
 *     beyond the SHA1 digests
 *   o the "private" configuration file, which contains identifying information and
 *     should be kept privately and is later used to merge detailed results from
 *     OSS Index into the private information file -- providing local context information
 *     to the OSS Index results.
 * 
 * @author Ken Duck
 *
 */
public class Assistant
{
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
	@SuppressWarnings("unused")
	private String exportJson()
	{
		Writer writer = new StringWriter();
		Gson gson = new GsonBuilder().setPrettyPrinting().create();

		gson.toJson(config, writer);

		return writer.toString();
	}

	/** Export both the public and private JSON files to the specified directory.
	 * 
	 * @param dir
	 */
	private void exportJson(File dir) throws IOException
	{
		// Write the private configuration file
		File privateFile = new File(dir, "ossindex.private.json");
		PrintWriter writer = new PrintWriter(new FileWriter(privateFile));
		try
		{
			config.touch();
			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			gson.toJson(config, writer);
		}
		finally
		{
			writer.close();
		}
		
		// Write the public configuration file
		File publicFile = new File(dir, "ossindex.public.json");
		writer = new PrintWriter(new FileWriter(publicFile));
		try
		{
			config.touch();
			Gson gson = new GsonBuilder().setPrettyPrinting()
					.setExclusionStrategies(new PublicExclusionStrategy())
					.create();
			gson.toJson(config, writer);
		}
		finally
		{
			writer.close();
		}
	}
	

	/** Create the initial configuration files by scanning a directory.
	 * 
	 * @param scan
	 * @param outputDir
	 * @throws IOException
	 */
	private static void doScan(String scan, File outputDir) throws IOException
	{
		File scanDir = new File(scan);
		if(!scanDir.exists())
		{
			System.err.println("Cannot find " + scanDir);
			return;
		}

		Assistant assistant = new Assistant();
		assistant.scan(scanDir);
		assistant.exportJson(outputDir);
	}

	/** Merge the specified JSON files together, write a new public/private file
	 * in the output directory.
	 * 
	 * @param inputs
	 * @param output
	 */
	private static void doMerge(String[] inputs, File outputDir)
	{
		System.err.println("This option is not yet supported");
	}

	/** Get the command line options for parsing.
	 * 
	 * @return
	 */
	@SuppressWarnings("static-access")
	public static Options getOptions()
	{
		Options options = new Options();

		options.addOption(new Option( "help", "print this message" ));
		options.addOption(OptionBuilder.withArgName("dir").hasArg().withDescription("directory to scan in order to create new configuration files").create("scan"));
		options.addOption(OptionBuilder.hasArgs(2).withDescription("configuration files to merge together").create("merge"));

		options.addOption(OptionBuilder.withArgName("dir").hasArg().withDescription("output directory").create("D"));
		
		return options;
	}

	/** Main method. Very simple, does not perform sanity checks on input.
	 * 
	 * @param args
	 * @throws IOException 
	 */
	public static void main( String[] args ) throws IOException
	{
		CommandLineParser parser = new BasicParser();
		try
		{
			// parse the command line arguments
			CommandLine line = parser.parse( getOptions(), args );
			if(line.hasOption("help"))
			{
				HelpFormatter formatter = new HelpFormatter();
				formatter.printHelp("assistant", getOptions());
				return;
			}

			// Determine operation type
			boolean doScan = line.hasOption("scan");
			boolean doMerge = line.hasOption("merge");
			if(doScan && doMerge)
			{
				System.err.println( "Only one of 'scan' or 'merge' may be selected");
				return;
			}

			if(doScan)
			{
				// Get the output directory
				if(!line.hasOption("D"))
				{
					System.err.println( "An output directory must be specified");
					return;
				}
				File outputDir = new File(line.getOptionValue("D"));
				if(!outputDir.exists()) outputDir.mkdir();
				if(!outputDir.isDirectory())
				{
					System.err.println("Output option is not a directory: " + outputDir);
					return;
				}

				doScan(line.getOptionValue("scan"), outputDir);
				return;
			}

			if(doMerge)
			{
				// Get the output directory
				if(!line.hasOption("D"))
				{
					System.err.println( "An output directory must be specified");
					return;
				}
				File outputDir = new File(line.getOptionValue("D"));
				if(!outputDir.exists()) outputDir.mkdir();
				if(!outputDir.isDirectory())
				{
					System.err.println("Output option is not a directory: " + outputDir);
					return;
				}
				
				doMerge(line.getOptionValues("merge"), outputDir);
				return;
			}
		}
		catch( ParseException exp )
		{
			System.err.println( "Parsing failed.  Reason: " + exp.getMessage() );
		}

		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("assistant", getOptions());
		return;
	}
}
