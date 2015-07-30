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
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import ca.twoducks.vor.ossindex.report.plugins.ChecksumPlugin;
import ca.twoducks.vor.ossindex.report.plugins.GemfileDependencyPlugin;
import ca.twoducks.vor.ossindex.report.plugins.HtmlDependencyPlugin;
import ca.twoducks.vor.ossindex.report.plugins.MavenDependencyPlugin;
import ca.twoducks.vor.ossindex.report.plugins.NodeDependencyPlugin;

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
	 * Option name indicating that dependency information should be extracted from source
	 * and configuration files when possible.
	 */
	private static final String NO_DEPENDENCIES_OPTION = "no_deps";

	private static final String NO_IMAGES_OPTION = "no_images";

	private static final String NO_ARTIFACTS_OPTION = "no_artifacts";

	/**
	 * 
	 */
	Configuration config = new Configuration();
	
	/**
	 * List of scan plugins.
	 */
	private List<IScanPlugin> plugins = new LinkedList<IScanPlugin>();

	/**
	 * Indicate whether dependencies should be exported to the public file.
	 */
	private boolean exportDependencies = true;

	/**
	 * Indicates whether artifacts should be included in the CSV output
	 */
	private boolean includeArtifacts;

	/**
	 * Indicates whether images should be included in the CSV output
	 */
	private boolean includeImages;

	/**
	 * Initialize the host connection.
	 * @throws IOException 
	 */
	public Assistant() throws IOException
	{
	}
	
	/** Indicate whether dependencies should be exported to the public file.
	 * 
	 * @param b
	 */
	private void setExportDependencies(boolean b)
	{
		exportDependencies = b;
	}
	
	/** Indicates whether artifacts should be included in the CSV output.
	 * 
	 * @param b
	 */
	private void setIncludeArtifacts(boolean b)
	{
		includeArtifacts = b;
	}

	/** Indicates whether images should be included in the CSV output.
	 * 
	 * @param b
	 */
	private void setIncludeImages(boolean b)
	{
		includeImages = b;
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
		// Do one of the scan plugins tell us to ignore this folder?
		// This will usually be done if we are going to identify the
		// dependencies from a dependency file.
		if(exportDependencies)
		{
			for(IScanPlugin plugin: plugins)
			{
				if(plugin.ignore(file)) return;
			}
		}
		
		if(file.isFile())
		{
			// Progress information
			System.out.println(file);
			System.out.flush();
			
			// Run through each scan plugin
			for(IScanPlugin plugin: plugins)
			{
				plugin.run(file);
			}
		}
		else
		{
			// Recursively do for all sub-folders and files.
			File[] children = file.listFiles();
			if(children != null)
			{
				for (File child : children)
				{
					if(!Files.isSymbolicLink(child.toPath()))
					{
						recursiveScan(child);
					}
				}
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
					.setExclusionStrategies(new PublicExclusionStrategy(exportDependencies))
					.create();
			gson.toJson(config, writer);
		}
		finally
		{
			writer.close();
		}
	}
	
	/** Merge the two given configuration files.
	 * 
	 * We merge the public into the private file, since the private file will contain
	 * files differentiated by path (which means the same file in multiple locations)
	 * whereas the public file has no such distinction.
	 * 
	 * @param f1
	 * @param f2
	 * @throws IOException
	 */
	private void merge(File publicFile, File privateFile) throws IOException
	{
		config = load(privateFile);
		if(publicFile != null)
		{
			Configuration c1 = load(publicFile);
			config.merge(c1);
		}
	}

	/** Load a configuration from a specified JSON file.
	 * 
	 * @param file
	 * @return
	 * @throws IOException
	 */
	private Configuration load(File file) throws IOException
	{
		if(file.getName().endsWith(".csv"))
		{
			return loadCsv(file);
		}
		else
		{
			Reader reader = new FileReader(file);
			Gson gson = new GsonBuilder().create();
			try
			{
				return gson.fromJson(reader, Configuration.class);
			}
			finally
			{
				reader.close();
			}
		}
	}
	
	/** Convert a CSV file back to a JSON config file.
	 * 
	 * @param file
	 * @return
	 * @throws IOException 
	 */
	private Configuration loadCsv(File file) throws IOException
	{
		Configuration config = new Configuration();
		Reader in = new FileReader(file);
		Iterable<CSVRecord> records = CSVFormat.EXCEL.withHeader().parse(in);
		for (CSVRecord record : records)
		{
		    String path = record.get("Path");
		    String state = record.get("State");
		    if(!"UNASSIGNED".equals(state))
		    {
			    String projectName = record.get("Project Name");
			    // There could be 3 "projectUri" fields. In order:
			    //   SCM,Project,Home
			    //
			    // SCM should always have a value, others *may*
			    String[] projectUris = parseList(record.get("Project URI"));
			    String scmUri = projectUris[0];
			    String projectUri = null;
			    String homeUri = null;
			    if(projectUris.length > 1 && !projectUris[1].trim().isEmpty()) projectUri = projectUris[1].trim();
			    if(projectUris.length > 2 && !projectUris[2].trim().isEmpty()) homeUri = projectUris[2].trim();
			    
			    String version = record.get("Version");
			    String[] cpes = parseList(record.get("CPEs"));
			    String[] projectLicenses = parseList(record.get("Project Licenses"));
			    String fileLicense = record.get("File License");
			    String projectDescription = record.get("Project Description");
			    String digest = record.get("Digest");
			    String comment = record.get("Comment");
			    
			    String overrideName = null;
			    String overrideLicense = null;
			    try {overrideName = record.get("Override Name");} catch(IllegalArgumentException e) {}
			    try {overrideLicense = record.get("Override License");} catch(IllegalArgumentException e) {}
			    
			    // Override applicable fields
			    if(overrideName != null && !overrideName.trim().isEmpty())
			    {
			    	projectName = overrideName;
			    	scmUri = null;
			    	projectUri = null;
			    	homeUri = null;
			    	version = null;
			    	cpes = new String[0];
		    		projectLicenses = new String[0];
		    		projectDescription = null;
			    	if(overrideLicense != null)
			    	{
			    		projectLicenses = new String[] {overrideLicense};
			    	}
			    }
			    
			    // Add the checksum to the file list
			    FileConfig fileConfig = config.addFile(digest);
	
			    if(path != null && !path.isEmpty())
			    {
			    	File aFile = new File(path);
			    	File parent = aFile.getParentFile();
			    	if(parent == null) fileConfig.setName(path);
			    	else fileConfig.setPath(path);
			    }
			    
			    if(fileLicense != null && !fileLicense.isEmpty()) fileConfig.setLicense(fileLicense);
			    if(comment != null && !comment.isEmpty()) fileConfig.setComment(comment);
			    if(state != null && !state.isEmpty()) fileConfig.setState(state);
			    
			    ProjectGroup group = config.getGroup(projectName);
			    ProjectConfig project = group.getProject(scmUri, version);
			    
			    // If the project has not been defined yet then set its values
			    if(project.getName() == null)
			    {
				    project.setName(projectName);
				    if(projectUri != null) project.setProjectUri(projectUri);
				    if(homeUri != null) project.setHomeUri(homeUri);
				    
				    if(cpes != null)
				    {
				    	for(String cpe: cpes)
				    	{
				    		project.addCpe(cpe);
				    	}
				    }
				    
				    if(projectLicenses != null)
				    {
				    	for(String license: projectLicenses)
				    	{
				    		project.addLicense(license);
				    	}
				    }
				    
				    if(projectDescription != null && !projectDescription.isEmpty()) project.setDescription(projectDescription);
			    }
			    
			    // Add the file to the project
			    project.addFile(fileConfig);
		    }
		}
		return config;
	}

	/** Parse a list of values
	 * 
	 * @param s
	 * @return
	 */
	private String[] parseList(String s)
	{
		if(s == null || s.trim().isEmpty()) return new String[0];
		if(s != null && s.startsWith("[") && s.endsWith("]"))
		{
			s = s.substring(1, s.length() - 1);
			return s.split(",");
		}
		else
		{
			throw new IllegalArgumentException("Illegal list definition: " + s);
		}
	}

	/** Export the configuration data into a CSV file. The CSV file may not
	 * contain complete information, but is much easier for a human to work with.
	 * Code will be added to allow conversion from CSV back into the JSON format.
	 * 
	 * @param dir
	 * @throws IOException 
	 */
	private void exportCsv(File dir) throws IOException
	{
		File file = new File(dir, "ossindex.csv");
		CSVFormat format = CSVFormat.EXCEL.withRecordSeparator("\n").withCommentMarker('#');
		FileWriter fout = new FileWriter(file);
		CSVPrinter csvOut = new CSVPrinter(fout, format);
		String[] header = {"Path", "State", "Project Name", "Project URI", "Version", "CPEs", "Project Licenses", "File License", "Project Description", "Digest", "Comment"};
		csvOut.printRecord((Object[])header);
		
		try
		{
			config.exportCsv(csvOut, includeArtifacts, includeImages);
		}
		finally
		{
			fout.close();
			csvOut.close();
		}
		
	}
	
	/** Add a scan plugin
	 * 
	 * @param class1
	 */
	private void addScanPlugin(Class<?> cls)
	{
		try
		{
			IScanPlugin plugin = (IScanPlugin) cls.newInstance();
			plugin.setConfiguration(config);
			plugins.add(plugin);
		}
		catch (InstantiationException | IllegalAccessException e)
		{
			e.printStackTrace();
		}
	}


	/** Create the initial configuration files by scanning a directory.
	 * 
	 * @param scan
	 * @param outputDir
	 * @throws IOException
	 */
	private static void doScan(Assistant assistant, String scan, File outputDir) throws IOException
	{
		File scanDir = new File(scan);
		if(!scanDir.exists())
		{
			System.err.println("Cannot find " + scanDir);
			return;
		}

		assistant.scan(scanDir);
		assistant.exportJson(outputDir);
		assistant.exportCsv(outputDir);
	}

	/** Import a JSON file and output a pretty-printed file along with the CSV file.
	 * 
	 * @param importFile
	 * @param outputDir
	 * @throws IOException
	 */
	private static void doImport(Assistant assistant, String importFile, File outputDir) throws IOException
	{
		File file = new File(importFile);
		if(!file.exists())
		{
			System.err.println("Cannot find " + file);
			return;
		}

		assistant.merge(null, file);
		assistant.exportJson(outputDir);
		assistant.exportCsv(outputDir);
	}

	/** Merge the specified JSON files together, write a new public/private file
	 * in the output directory.
	 * 
	 * @param inputs
	 * @param output
	 * @throws FileNotFoundException 
	 */
	private static void doMerge(Assistant assistant, String[] inputs, File outputDir) throws IOException
	{
		File f1 = new File(inputs[0]);
		File f2 = new File(inputs[1]);
		
		if(!f1.exists() || !f1.isFile()) throw new FileNotFoundException("Missing file: " + f1);
		if(!f2.exists() || !f2.isFile()) throw new FileNotFoundException("Missing file: " + f2);
		
		
		assistant.merge(f1, f2);
		assistant.exportJson(outputDir);
		assistant.exportCsv(outputDir);
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
		options.addOption(OptionBuilder.withArgName("public private").hasArgs(2).withDescription("configuration files to merge together").create("merge"));
		options.addOption(OptionBuilder.withArgName("public").hasArgs(2).withDescription("import a JSON file and export a formatted JSON with a CSV file").create("import"));

		options.addOption(OptionBuilder.withArgName("dir").hasArg().withDescription("output directory").create("D"));
		
		options.addOption(NO_DEPENDENCIES_OPTION, false, "Don't scan source and configuration files to locate possible dependency information");
		options.addOption(NO_IMAGES_OPTION, false, "Don't include images in the CSV output");
		options.addOption(NO_ARTIFACTS_OPTION, false, "Don't include build artifacts in the CSV output");
		
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
			
			// Instantiate assistant
			Assistant assistant = new Assistant();
			
			// Add default plugins
			assistant.addScanPlugin(ChecksumPlugin.class);
			assistant.addScanPlugin(HtmlDependencyPlugin.class);
			assistant.addScanPlugin(NodeDependencyPlugin.class);
			assistant.addScanPlugin(MavenDependencyPlugin.class);
			assistant.addScanPlugin(GemfileDependencyPlugin.class);
			
			if(line.hasOption(NO_DEPENDENCIES_OPTION))
			{
				assistant.setExportDependencies(false);
			}
			else
			{
				assistant.setExportDependencies(true);
			}
			assistant.setIncludeImages(!line.hasOption(NO_IMAGES_OPTION));
			assistant.setIncludeArtifacts(!line.hasOption(NO_ARTIFACTS_OPTION));

			// Determine operation type
			boolean doScan = line.hasOption("scan");
			boolean doMerge = line.hasOption("merge");
			boolean doImport = line.hasOption("import");
			int count = 0;
			if(doScan) count++;
			if(doMerge) count++;
			if(doImport) count++;
			if(count > 1)
			{
				System.err.println( "Only one of 'scan', 'merge', or import may be selected");
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

				doScan(assistant, line.getOptionValue("scan"), outputDir);
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
				
				doMerge(assistant, line.getOptionValues("merge"), outputDir);
				return;
			}
			
			if(doImport)
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
				
				doImport(assistant, line.getOptionValue("import"), outputDir);
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
