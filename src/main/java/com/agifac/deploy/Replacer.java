package com.agifac.deploy;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Build;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.apache.maven.shared.filtering.MavenFilteringException;
import org.apache.maven.shared.filtering.MavenResourcesFiltering;
import org.codehaus.plexus.archiver.Archiver;
import org.codehaus.plexus.archiver.ArchiverException;
import org.codehaus.plexus.archiver.UnArchiver;
import org.codehaus.plexus.archiver.ear.EarArchiver;
import org.codehaus.plexus.archiver.jar.JarArchiver;
import org.codehaus.plexus.archiver.manager.ArchiverManager;
import org.codehaus.plexus.archiver.manager.NoSuchArchiverException;
import org.codehaus.plexus.archiver.war.WarArchiver;
import org.codehaus.plexus.archiver.zip.ZipArchiver;

/**
 * 
 * 
 * @author Bernard Wittwer (bernard.wittwer@the-agile-factory.com)
 * 
 */
public class Replacer {

	private static void filter(Log log, String encoding,
			MavenResourcesFiltering mavenResourcesFiltering,
			ArchiverManager archiverManager, String archivesExtension,
			String resourcesFileName, String mapperFileName,
			MavenProjectHelper mavenProjectHelper, File destination,
			MavenProject project, String propertiesExtension,
			MavenSession session, File source, File filter)
			throws MojoExecutionException, MojoFailureException {
		try {
			destination.getParentFile().mkdirs();
			copyFile(source, destination);
		} catch (IOException e) {
			log.error("Unexpected error " + e.getMessage(), e);
			throw new MojoExecutionException("Unexpected error "
					+ e.getMessage(), e);
		}
		Archiver am = null;

		log.debug("archiverManager = " + archiverManager);

		try {
			if (destination.getName().endsWith(".sar")) {
				am = archiverManager.getArchiver("jar");
			} else if (destination.getName().endsWith(".car")) {
				am = archiverManager.getArchiver("jar");
			} else {
				am = archiverManager.getArchiver(destination);
			}
		} catch (NoSuchArchiverException e) {
			log.error("Unexpected error " + e.getMessage(), e);
			throw new MojoExecutionException("Unexpected error "
					+ e.getMessage(), e);
		}
		am.setDestFile(destination);
		File temporaryFolder = new File(new File(
				System.getProperty("java.io.tmpdir")), Replacer.class.getName()
				+ UUID.randomUUID());
		temporaryFolder.mkdirs();
		UnArchiver um = null;
		try {
			if (destination.getName().endsWith(".sar")) {
				um = archiverManager.getUnArchiver("jar");
			} else if (destination.getName().endsWith(".car")) {
				um = archiverManager.getUnArchiver("jar");
			} else {
				um = archiverManager.getUnArchiver(source);
			}
		} catch (NoSuchArchiverException e) {
			log.error("Unexpected error " + e.getMessage(), e);
			throw new MojoExecutionException("Unexpected error "
					+ e.getMessage(), e);
		}
		um.setSourceFile(source);
		um.setDestDirectory(temporaryFolder);
		try {
			um.extract("META-INF/" + resourcesFileName, temporaryFolder);
		} catch (ArchiverException e) {
		}
		File marker = new File(temporaryFolder, "META-INF/" + resourcesFileName);

		try {
			um.extract("META-INF/" + mapperFileName, temporaryFolder);
		} catch (ArchiverException e) {
		}

		File mapper = new File(temporaryFolder, "META-INF/" + mapperFileName);

		File childFilter = null;

		if (mapper.exists()) {

			log.debug("Found child mapper " + mapper.getPath());

			File tmp = new File(new File(System.getProperty("java.io.tmpdir")),
					Replacer.class.getName() + UUID.randomUUID());
			tmp.mkdirs();

			List<Resource> rs = new ArrayList<Resource>();
			Resource r = new Resource();
			r.setDirectory(mapper.getParent());
			r.setFiltering(true);
			r.setTargetPath(tmp.getPath());
			List<String> is = new ArrayList<String>();
			is.add(mapper.getName());
			r.setIncludes(is);
			rs.add(r);

			log.debug("Added ressource " + r);

			log.debug("Resulting filter path " + tmp);

			MavenProject mp = new MavenProject();
			mp.setBuild(new Build());

			log.debug("Adding filter to mapper processing "
					+ filter.getAbsolutePath());
			mp.getBuild().addFilter(filter.getAbsolutePath());
			try {
				mavenResourcesFiltering
						.filterResources(rs, tmp, mp, encoding,
								Collections.EMPTY_LIST, Collections.EMPTY_LIST,
								session);
			} catch (MavenFilteringException e) {
				log.error("Unexpected error " + e.getMessage(), e);
				throw new MojoExecutionException("Unexpected error "
						+ e.getMessage(), e);
			}

			// Filter the mapper
			File mappedfilter = new File(tmp.getPath(), mapper.getName());

			log.debug("Mapped filter is " + mappedfilter);

			childFilter = mappedfilter;

		} else {
			log.debug("No mapper used, propagating parent filter");

			childFilter = filter;
		}

		if (marker.exists()) {
			List<String> paths = new ArrayList<String>();
			try {
				BufferedReader isr = new BufferedReader(new FileReader(marker));
				String nxt = null;
				while ((nxt = isr.readLine()) != null) {
					nxt = nxt.replaceAll("#.*$", "").trim();
					if (!"".equals(nxt)) {
						paths.add(nxt);
					}
				}
				isr.close();
			} catch (FileNotFoundException e) {
				log.error("Unexpected error " + e.getMessage(), e);
				throw new MojoExecutionException("Unexpected error "
						+ e.getMessage(), e);
			} catch (IOException e) {
				log.error("Unexpected error " + e.getMessage(), e);
				throw new MojoExecutionException("Unexpected error "
						+ e.getMessage(), e);
			}
			if ((am instanceof JarArchiver)) {
				((JarArchiver) am).setUpdateMode(true);
			} else if ((am instanceof WarArchiver)) {
				((WarArchiver) am).setUpdateMode(true);
				((WarArchiver) am).setIgnoreWebxml(false);
			} else if ((am instanceof EarArchiver)) {
				((EarArchiver) am).setUpdateMode(true);
			} else if ((am instanceof ZipArchiver)) {
				((ZipArchiver) am).setUpdateMode(true);
			}
			am.setDuplicateBehavior("skip");
			List<String> excludesFile = new ArrayList<String>();
			for (String path : paths) {
				try {
					um.extract(path, temporaryFolder);
				} catch (ArchiverException e) {
				}
				File input = new File(temporaryFolder, path);
				if (input.exists()) {
					log.info("Processing " + path);
					String realName = input.getName();
					excludesFile.add(path);
					if (Replacer.isExtension(realName, archivesExtension)) {
						File tmp = new File(new File(
								System.getProperty("java.io.tmpdir")),
								Replacer.class.getName() + UUID.randomUUID());
						File output = new File(tmp, input.getName());
						filter(log, encoding, mavenResourcesFiltering,
								archiverManager, archivesExtension,
								resourcesFileName, mapperFileName,
								mavenProjectHelper, output, project,
								propertiesExtension, session, input,
								childFilter);
						try {
							am.addFile(output, path);
						} catch (ArchiverException e) {
							log.error("Unexpected error " + e.getMessage(), e);
							throw new MojoExecutionException(
									"Unexpected error " + e.getMessage(), e);
						}
						deleteOnExitDirectory(tmp);
						deleteOnExitDirectory(output);
					} else {
						List<Resource> rs = new ArrayList<Resource>();
						Resource r = new Resource();
						r.setDirectory(temporaryFolder.getAbsolutePath());
						r.setFiltering(true);
						List<String> is = new ArrayList<String>();
						is.add(path);
						r.setIncludes(is);
						rs.add(r);
						File tmp = new File(new File(
								System.getProperty("java.io.tmpdir")),
								Replacer.class.getName() + UUID.randomUUID());
						tmp.mkdirs();
						try {
							if (project == null) {
								MavenProject mp = new MavenProject();
								mp.setBuild(new Build());
								mp.getBuild().addFilter(
										filter.getAbsolutePath());
								mavenResourcesFiltering.filterResources(rs,
										tmp, mp, encoding,
										Collections.EMPTY_LIST,
										Collections.EMPTY_LIST, session);
							} else {
								mavenResourcesFiltering.filterResources(rs,
										tmp, project, encoding,
										Collections.EMPTY_LIST,
										Collections.EMPTY_LIST, session);
							}
						} catch (MavenFilteringException e) {
							log.error("Unexpected error " + e.getMessage(), e);
							throw new MojoExecutionException(
									"Unexpected error " + e.getMessage(), e);
						}
						try {
							am.addFile(new File(tmp, path), path);
						} catch (ArchiverException e) {
							log.error("Unexpected error " + e.getMessage(), e);
							throw new MojoExecutionException(
									"Unexpected error " + e.getMessage(), e);
						}
						deleteOnExitDirectory(tmp);
					}
					log.info("End processing " + path);
				}
			}
			try {
				am.addArchivedFileSet(source, null, (String[]) excludesFile
						.toArray(new String[excludesFile.size()]));
			} catch (ArchiverException e) {
				log.error("Unexpected error " + e.getMessage(), e);
				throw new MojoExecutionException("Unexpected error "
						+ e.getMessage(), e);
			}
			try {
				am.createArchive();
			} catch (ArchiverException e) {
				log.error("Unexpected error " + e.getMessage(), e);
				throw new MojoExecutionException("Unexpected error "
						+ e.getMessage(), e);
			} catch (IOException e) {
				log.error("Unexpected error " + e.getMessage(), e);
				throw new MojoExecutionException("Unexpected error "
						+ e.getMessage(), e);
			}
		}
		deleteOnExitDirectory(temporaryFolder);
	}

	private static void deleteOnExitDirectory(File path) {
		if (path.exists()) {
			path.deleteOnExit();
			File[] files = path.listFiles();
			if (files == null) {
				return;
			}
			for (int i = 0; i < files.length; i++) {
				if (files[i].isDirectory()) {
					deleteOnExitDirectory(files[i]);
				} else {
					files[i].deleteOnExit();
				}
			}
		}
	}

	public static void copyFile(File src, File dest) throws IOException {
		InputStream in = new FileInputStream(src);
		OutputStream out = new FileOutputStream(dest);
		byte[] buf = new byte[1024];
		int len;
		while ((len = in.read(buf)) > 0) {
			out.write(buf, 0, len);
		}
		in.close();
		out.close();
	}

	public static void mergeConfiguration(Log log, String encoding,
			MavenResourcesFiltering mavenResourcesFiltering,
			ArchiverManager archiverManager, String archivesExtension,
			String resourcesFileName, String mapperFileName, File sourceFile,
			File destinationFile, String propertiesExtension, File filter)
			throws MojoExecutionException, MojoFailureException {

		log.debug("destinationFile initial: " + destinationFile);

		if (destinationFile == null) {
			destinationFile = new File(sourceFile.getParentFile(), "merged-"
					+ sourceFile.getName());
		} else {
			destinationFile.getParentFile().mkdirs();
		}

		log.debug("destinationFile final: " + destinationFile);

		/*
		 * List<String> mapperFilters = new ArrayList<String>();
		 * mapperFilters.add("prepare.properties");
		 */

		filter(log, encoding, mavenResourcesFiltering, archiverManager,
				archivesExtension, resourcesFileName, mapperFileName, null,
				destinationFile, null, propertiesExtension, null, sourceFile,
				filter);
	}

	public static boolean isExtension(String realName, String extensions) {
		for (String b : extensions.split("\\s*,\\s*")) {
			if (realName.endsWith(b)) {
				return true;
			}
		}
		return false;
	}
}
