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


@Mojo(name = "replace", requiresProject = false, requiresDirectInvocation = true )
public class ReplaceMojo extends AbstractMojo {

	/**
	 * This is the environment file to be used
	 */
	@Parameter(property = "filter", required = true, defaultValue = "${envfile}")
	private File filter;

	/**
	 * The character encoding scheme to be applied when filtering resources.
	 */
	@Parameter(property = "encoding", required = true, defaultValue = "UTF-8")
	private String encoding;

	/**
	 * This is the configuration file name
	 */
	@Parameter(property = "configurationFileName", required = true, defaultValue = "com.agifac.deploy.replacer.properties")
	private String configurationFileName;

	/**
	 * This is the list of extension to be considered as properties file
	 */
	@Parameter(property = "propertiesExtension", required = true, defaultValue = ".xml,.properties")
	private String propertiesExtension;

	/**
	 * This is the list of extension to be considered as archive file
	 */
	@Parameter(property = "archivesExtension", required = true, defaultValue = ".jar,.war,.sar,.ear,.zip")
	private String archivesExtension;

	/**
	 * This is the source file to used
	 */
	@Parameter(property = "sourceFile", required = true, defaultValue = "${file}")
	private File sourceFile;

	/**
	 * This is the destination file to be used. If ommited, a default value will
	 * be used.
	 */
	@Parameter(property = "destinationFile", required = false, defaultValue = "")
	private File destinationFile;

	@Component
	protected ArchiverManager archiverManager;

	@Component( role = MavenResourcesFiltering.class, hint = "default" )
	protected MavenResourcesFiltering mavenResourcesFiltering;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		Replacer.mergeConfiguration(getLog(), this.encoding,
				this.mavenResourcesFiltering, this.archiverManager,
				this.archivesExtension, this.configurationFileName,
				this.sourceFile, this.destinationFile,
				this.propertiesExtension, this.filter);
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
