package net.masterthought.cucumber;

import static java.util.Arrays.asList;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

/**
 * Goal which generates a Cucumber Report.
 *
 */
@Mojo(name = "generate", defaultPhase = LifecyclePhase.VERIFY)
public class CucumberReportGeneratorMojo extends AbstractMojo {

	/**
	 * Name of the project.
	 *
	 * @parameter expression="${project.name}"
	 * @required
	 */
	@SuppressWarnings("unused")
	private String projectName;

	/**
	 * Build number.
	 *
	 * @parameter expression="${build.number}" default-value="1"
	 */
	@SuppressWarnings("unused")
	private String buildNumber;

	/**
	 * Location of the file.
	 *
	 * @parameter expression="${project.build.directory}/cucumber-reports"
	 * @required
	 */
	@SuppressWarnings("unused")
	private File outputDirectory;

	/**
	 * Location of the file.
	 *
	 * @parameter expression="${project.build.directory}/cucumber.json"
	 * @required
	 */
	@SuppressWarnings("unused")
	private File cucumberOutput;

	/**
	 * Skipped fails
	 *
	 * @parameter expression="false" default-value="false"
	 * @required
	 */
	@SuppressWarnings("unused")
	private Boolean skippedFails;

	/**
	 * Undefined fails
	 *
	 * @parameter expression="false" default-value="false"
	 * @required
	 */
	@SuppressWarnings("unused")
	private Boolean undefinedFails;

	/**
	 * Pending fails
	 *
	 * @parameter expression="false" default-value="false"
	 * @required
	 */
	@SuppressWarnings("unused")
	private Boolean pendingFails;

	/**
	 * Missing fails
	 *
	 * @parameter expression="false" default-value="false"
	 * @required
	 */
	@SuppressWarnings("unused")
	private Boolean missingFails;

	/**
	 * Enable Flash Charts.
	 *
	 * @parameter expression="true"
	 * @required
	 */
	@SuppressWarnings("unused")
	private Boolean enableFlashCharts;

	/**
	 * Skip check for failed build result
	 *
	 * @parameter expression="false" default-value="false"
	 * @required
	 */
	@SuppressWarnings("unused")
	private Boolean checkBuildResult;

	/**
	 * turn off the runWithJenkins indicator
	 *
	 * @parameter expression="false" default-value="false"
	 * @required
	 */
	@SuppressWarnings("FieldCanBeLocal")
	private Boolean runWithJenkins = false;

	/**
	 * since we are assuming flashCharts turn off highCharts
	 *
	 * @parameter expression="false" default-value="false"
	 */
	@SuppressWarnings("unused")
	private Boolean highCharts;

  /**
	 * assume parallel testing defaults to off
	 *
	 * @parameter expression="false" default-value="false"
	 */
	@SuppressWarnings("unused")
	private Boolean parallelTesting;

	public CucumberReportGeneratorMojo()
	{
	}

	@Override
	public void execute() throws MojoExecutionException {
		if (!outputDirectory.exists()) {
			// noinspection ResultOfMethodCallIgnored
			outputDirectory.mkdirs();
		}

		List<String> list = new ArrayList<>();
		for (File jsonFile : cucumberFiles(cucumberOutput)) {
			list.add(jsonFile.getAbsolutePath());
		}

		if (list.isEmpty()) {
			getLog().warn(cucumberOutput.getAbsolutePath() + " does not exist.");
			return;
		}

		try {
			ReportBuilder reportBuilder = new ReportBuilder(list, outputDirectory, "", buildNumber, projectName, skippedFails, pendingFails, undefinedFails,
					missingFails, enableFlashCharts, runWithJenkins, highCharts, parallelTesting);
			getLog().info("About to generate Cucumber report.");
			reportBuilder.generateReports();

			if (checkBuildResult) {
				boolean buildResult = reportBuilder.hasBuildPassed();
				if (!buildResult) {
					throw new MojoExecutionException("BUILD FAILED - Check Report For Details");
				}
			}

		}
		catch (Exception e) {
			throw new MojoExecutionException("Error Found:", e);
		}
	}

	// Normally, I'd keep this private and use mocks for testing the public
	// contract.
	// I'm not sure that the author wants to get that serious with this..
	static Collection<File> cucumberFiles(File file) throws MojoExecutionException {
		if (!file.exists()) {
			return Collections.emptyList();
		}
		if (file.isFile()) {
			// noinspection ArraysAsListWithZeroOrOneArgument
			return asList(file);
		}
		return FileUtils.listFiles(file, new String[] { "json" }, true);
	}
}
