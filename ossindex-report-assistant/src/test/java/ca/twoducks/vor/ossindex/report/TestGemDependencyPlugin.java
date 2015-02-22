package ca.twoducks.vor.ossindex.report;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;

import org.junit.Test;

import ca.twoducks.vor.ossindex.report.plugins.GemfileDependencyPlugin;

/** Test the gem dependency plugin
 * 
 * @author Ken Duck
 *
 */
public class TestGemDependencyPlugin
{
	@Test
	public void test1()
	{
		GemfileDependencyPlugin dep = new GemfileDependencyPlugin();
		dep.setConfiguration(new TestConfiguration());
		dep.run(new File("samples/test1.gemfile"));
	}
	@Test
	public void test2()
	{
		GemfileDependencyPlugin dep = new GemfileDependencyPlugin();
		dep.setConfiguration(new TestConfiguration());
		dep.run(new File("samples/test2.gemfile"));
	}
	@Test
	public void test3()
	{
		GemfileDependencyPlugin dep = new GemfileDependencyPlugin();
		dep.setConfiguration(new TestConfiguration());
		dep.run(new File("samples/test3.gemfile"));
	}
	@Test
	public void test4()
	{
		GemfileDependencyPlugin dep = new GemfileDependencyPlugin();
		dep.setConfiguration(new TestConfiguration());
		dep.run(new File("samples/test4.gemfile"));
	}
}

/** Class for testing purposes
 * 
 * @author Ken Duck
 *
 */
class TestConfiguration implements IConfiguration
{

	@Override
	public void addDependency(File file, String type, String gemName, String version, String comment)
	{
		System.out.println("Dependency: " + file + " " + gemName + " " + version + " " + comment);
	}

	@Override
	public void addFile(File file) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addDependency(File file, String type, URL url) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addDependency(File file, String string, URI uri, String comment) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addDependency(File file, String string, String groupId,
			String artifactId, String version, String comment) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addDependency(File file, String type, String gemName, URI uri, String version, String comment)
	{
		System.out.println("Dependency: " + file + " " + gemName + " " + version + " " + uri + " " + comment);
	}
	
}