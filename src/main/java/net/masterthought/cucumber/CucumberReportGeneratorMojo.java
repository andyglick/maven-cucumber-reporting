package net.masterthought.cucumber;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.plexus.util.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Goal which generates a Cucumber Report.
 *
 * @goal generate
 * @phase verify
 */
@SuppressWarnings("unused")
public class CucumberReportGeneratorMojo extends AbstractMojo {

  /**
   * Name of the project.
   *
   * @parameter property="project.name"
   * @required
   */
  private String projectName;

  /**
   * Build number.
   *
   * @parameter property="build.number" default-value="1"
   */
  private String buildNumber;

  /**
   * Location of the file.
   *
   * @parameter default-value="${project.build.directory}/cucumber-reports"
   * @required
   */
  private File outputDirectory;

  /**
   * Location of the file.
   *
   * @parameter default-value="${project.build.directory}/cucumber.json"
   * @required
   */
  private File cucumberOutput;

  /**
   * Skip check for failed build result.
   *
   * @parameter default-value="true"
   * @required
   */
  private Boolean checkBuildResult;

  /**
   * Build reports from parallel tests.
   *
   * @parameter property="true" default-value="false"
   * @required
   */
  private Boolean parallelTesting;

  /**
   * Additional attributes to classify current test run.
   *
   * @parameter
   */
  private Map<String, String> classifications;

  public CucumberReportGeneratorMojo()
  {
    super();
  }

  @SuppressWarnings("ResultOfMethodCallIgnored")
  @Override
  public void execute() throws MojoExecutionException {
    if (!outputDirectory.exists()) {
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
      Configuration configuration = new Configuration(outputDirectory, projectName);
      configuration.setBuildNumber(buildNumber);
      configuration.setParallelTesting(parallelTesting);
      if (!MapUtils.isEmpty(classifications)) {
        for (Map.Entry<String, String> entry : classifications.entrySet()) {
          configuration.addClassifications(StringUtils.capitalise(entry.getKey()), entry.getValue());
        }
      }

      ReportBuilder reportBuilder = new ReportBuilder(list, configuration);
      getLog().info("About to generate Cucumber report.");
      Reportable report = reportBuilder.generateReports();

      if (checkBuildResult && report == null) {
        throw new MojoExecutionException("BUILD FAILED - Check Report For Details");
      }

    } catch (Exception e) {
      throw new MojoExecutionException("Error Found:", e);
    }
  }

  // Normally, I'd keep this private and use mocks for testing the public contract.
  // I'm not sure that the author wants to get that serious with this..
  static Collection<File> cucumberFiles(File file) throws MojoExecutionException {
    if (!file.exists()) {
      return Collections.emptyList();
    }
    if (file.isFile()) {
      return Collections.singletonList(file);
    }
    return FileUtils.listFiles(file, new String[] {"json"}, true);
  }
}