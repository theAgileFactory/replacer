package com.agifac.deploy;

import java.io.File;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.shared.filtering.MavenResourcesFiltering;
import org.codehaus.plexus.archiver.manager.ArchiverManager;


/**
 * Goal that perform the replacement of token in the archive.
 * The replacement is done recursively  into the inner archives.
 * 
 * @author Bernard Wittwer (bernard.wittwer@the-agile-factory.com)
 */
@Mojo(name = "replace", requiresProject = false )
public class ReplaceMojo extends AbstractMojo {

	/**
	 * This is the source file to used
	 * Default : -
	 * Required : yes
	 * 
	 */
	@Parameter(property = "source", required = true, defaultValue = "${file}")
	private File source;	
	
	/**
	 * This is the environment file to be used
	 * Default : -
	 * Required : yes
	 * 
	 */
	@Parameter(property = "env", required = true, defaultValue = "${envfile}")
	private File env;

	/**
	 * The character encoding scheme to be applied when filtering resources.
	 * Default : "UTF-8"
	 * Required : no
	 */
	@Parameter(property = "encoding", required = false, defaultValue = "UTF-8")
	private String encoding;

	/**
	 * This is the resource file name (name of the file in the 
	 * META-INF folder containing
	 * the list of files/archives to process)
	 * Default is "com.agifac.deploy.replacer.resources.properties"
	 * Required : no
	 */
	@Parameter(property = "resourcesFileName", required = false, defaultValue = "com.agifac.deploy.replacer.resources.properties")
	private String resourcesFileName;
	
	/**
	 * This is the mapper file name (name of the file in the 
	 * META-INF folder containing the mapping of the key-values
	 * for the embedded archives)
	 * Default is "com.agifac.deploy.replacer.mapper.properties"
	 * Required : no
	 */
	@Parameter(property = "mapperFileName", required = false, defaultValue = "com.agifac.deploy.replacer.mapper.properties")
	private String mapperFileName;

	/**
	 * This is the list of extension to be considered as properties file
	 * Default : ".xml,.properties"
	 * Required : no
	 */
	@Parameter(property = "propertiesExtension", required = false, defaultValue = ".xml,.properties")
	private String propertiesExtension;

	/**
	 * This is the list of extension to be considered as archive file
	 * Default : ".jar,.war,.sar,.ear,.zip"
	 * Required : no
	 */
	@Parameter(property = "archivesExtension", required = false, defaultValue = ".jar,.war,.sar,.ear,.zip")
	private String archivesExtension;

	/**
	 * This is the destination file to be used. If ommited, a default value will
	 * be used.
	 * Default : "merged-" + source file name 
	 * Required : no
	 */
	@Parameter(property = "target", required = false, defaultValue = "")
	private File target;

	@Component
	protected ArchiverManager archiverManager;

	@Component( role = MavenResourcesFiltering.class, hint = "default" )
	protected MavenResourcesFiltering mavenResourcesFiltering;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		Replacer.mergeConfiguration(getLog(), this.encoding,
				this.mavenResourcesFiltering, this.archiverManager,
				this.archivesExtension, this.resourcesFileName, this.mapperFileName,
				this.source, this.target,
				this.propertiesExtension, this.env);
	}

	public ArchiverManager getArchiverManager() {
		return archiverManager;
	}

	public void setArchiverManager(ArchiverManager archiverManager) {
		this.archiverManager = archiverManager;
	}

	public MavenResourcesFiltering getMavenResourcesFiltering() {
		return mavenResourcesFiltering;
	}

	public void setMavenResourcesFiltering(
			MavenResourcesFiltering mavenResourcesFiltering) {
		this.mavenResourcesFiltering = mavenResourcesFiltering;
	}

}
