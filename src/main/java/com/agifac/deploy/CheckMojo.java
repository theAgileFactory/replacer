package com.agifac.deploy;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * Goal that checks the completeness of two properties files
 * 
 * @author Bernard Wittwer (bernard.wittwer@the-agile-factory.com)
 */
@Mojo(name = "check", defaultPhase = LifecyclePhase.GENERATE_SOURCES, requiresProject = false)
public class CheckMojo extends AbstractMojo {

	/**
	 * The source file to be checked
	 * Default : -
	 * Required : yes
	 * 
	 */
	 @Parameter(property = "source", required = true )
	private File source;
	 
	 /**
	  * The reference file to be compared to
	  * Default : -
	  * Required : yes
	  * 
	  */
	 @Parameter(property = "reference", required = true )
	private File reference;
	 
	 /**
	  * Set to true if you want to allow additional properties in source file
	  * Default : false
	  * Required : no
	  */
	@Parameter( defaultValue = "false", property = "allowAdditionalProperties", required = false )
	private String allowAdditionalProperties;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {

		Properties reference = new Properties();
		Properties filter = new Properties();
		InputStream is = null;

		try {
			is = new FileInputStream(this.reference);
			reference.load(is);
			try {
				if (is != null) {
					is.close();
				}
			} catch (Exception e) {
			}
		} catch (Exception e) {
			throw new MojoExecutionException(
					"Error during read of the reference file " + e.getMessage(),
					e);
		} finally {
			try {
				if (is != null) {
					is.close();
				}
			} catch (Exception e) {
			}
		}

		try {
			is = new FileInputStream(this.source);
			filter.load(is);
			try {
				if (is != null) {
					is.close();
				}
			} catch (Exception e) {
			}

		} catch (Exception e) {
			throw new MojoExecutionException(
					"Error during read of the source file " + e.getMessage(), e);
		} finally {
			try {
				if (is != null) {
					is.close();
				}
			} catch (Exception e) {
			}
		}

		List<String> error = new ArrayList<String>();
		for (Object name : reference.keySet()) {
			if (!filter.containsKey(name)) {
				error.add("Missing property '" + name + "' in source file");
			}
		}
		if ("false".equalsIgnoreCase(this.allowAdditionalProperties)) {
			for (Object name : filter.keySet()) {
				if (!reference.containsKey(name)) {
					error.add("Source file use property '"
							+ name
							+ "', but this property doesn't exist in the reference file");
				}
			}
		}
		if (!error.isEmpty()) {
			for (String err : error) {
				getLog().error(err);
			}
			throw new MojoExecutionException("Validation of file has failed");
		}
	}
}
