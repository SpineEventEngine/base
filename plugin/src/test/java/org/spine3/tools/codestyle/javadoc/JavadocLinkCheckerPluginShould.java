/*
 * Copyright 2017, TeamDev Ltd. All rights reserved.
 *
 * Redistribution and use in source and/or binary forms, with or without
 * modification, must retain the above copyright notice and the following
 * disclaimer.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.spine3.tools.codestyle.javadoc;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import org.gradle.api.Nullable;
import org.gradle.internal.impldep.com.esotericsoftware.kryo.NotNull;
import org.gradle.internal.impldep.org.apache.commons.io.FileUtils;
import org.gradle.internal.impldep.org.apache.commons.io.IOUtils;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.BuildTask;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.spine3.gradle.TaskName;
import org.spine3.tools.codestyle.ReportType;

import javax.annotation.CheckForNull;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class JavadocLinkCheckerPluginShould {

    private static final String SOURCE_FOLDER = "src/main/java";
    private static final String WRONG_LINK_FORMAT_MSG = "Wrong link format found";
    private static final String CHECK_JAVADOC_LINK = TaskName.CHECK_FQN.getValue();
    private static final String DEBUG_OPTION = "--debug";
    private static final String COMPILE_LOG = ":compileJava";
    private static final String CHECK_JAVADOC_LOG = ":checkJavadocLink";
    private static final int THRESHOLD = 2;

    private String resourceFolder = "";

    @Rule
    public final TemporaryFolder testProjectDir = new TemporaryFolder();

    public void setUpTestProject(int threshold, ReportType reportType) throws IOException {
        final Path buildGradleFile = testProjectDir.getRoot()
                                                   .toPath()
                                                   .resolve("build.gradle");
        final InputStream input = getBuildFileContent(threshold, reportType);

        final Path testSources = testProjectDir.getRoot()
                                               .toPath()
                                               .resolve(SOURCE_FOLDER);
        Files.copy(input, buildGradleFile);
        Files.createDirectories(testSources);

        ClassLoader classLoader = getClass().getClassLoader();
        final String testFile = "AllowedFqnFormats.java";
        final String resourceFilePath = classLoader.getResource(testFile)
                                                   .getPath();
        final int endIndex = resourceFilePath.length() - testFile.length();
        resourceFolder = resourceFilePath.substring(0, endIndex);
    }

    @Test
    public void fail_build_if_wrong_fqn_name_found() throws IOException {
        setUpTestProject(THRESHOLD, ReportType.ERROR);
        final Path testSources = testProjectDir.getRoot()
                                               .toPath()
                                               .resolve(SOURCE_FOLDER);
        FileUtils.copyDirectory(new File(resourceFolder), new File(testSources.toString()));

        BuildResult buildResult = GradleRunner.create()
                                              .withProjectDir(testProjectDir.getRoot())
                                              .withPluginClasspath()
                                              .withArguments(CHECK_JAVADOC_LINK, DEBUG_OPTION)
                                              .buildAndFail();

        assertTrue(buildResult.getOutput().contains(WRONG_LINK_FORMAT_MSG));
    }

    @Test
    public void allow_correct_fqn_name_format() throws IOException {
        setUpTestProject(THRESHOLD, ReportType.ERROR);
        final Path testSources = testProjectDir.getRoot()
                                               .toPath()
                                               .resolve(SOURCE_FOLDER);
        final Path wrongFqnFormat = Paths.get(testSources.toString() + "/WrongFQNformat.java");
        final Path wrongMultipleFqnFormat = Paths.get(testSources.toString() + "/MultipleWrongFqnLinks.java");
        FileUtils.copyDirectory(new File(resourceFolder), new File(testSources.toString()));
        Files.deleteIfExists(wrongFqnFormat);
        Files.deleteIfExists(wrongMultipleFqnFormat);

        final GradleRunner gradleRunner = GradleRunner.create()
                                                      .withProjectDir(testProjectDir.getRoot())
                                                      .withPluginClasspath()
                                                      .withArguments(CHECK_JAVADOC_LINK, DEBUG_OPTION);
        BuildResult buildResult = gradleRunner.build();

        final List<String> expected = Arrays.asList(COMPILE_LOG, CHECK_JAVADOC_LOG);

        assertEquals(expected, extractTasks(buildResult));
    }

    @Test
    public void warn_by_default_about_wrong_link_formats() throws IOException {
        setUpTestProject(2, ReportType.WARN);
        final Path testSources = testProjectDir.getRoot()
                                               .toPath()
                                               .resolve(SOURCE_FOLDER);
        FileUtils.copyDirectory(new File(resourceFolder), new File(testSources.toString()));

        BuildResult buildResult = GradleRunner.create()
                                              .withProjectDir(testProjectDir.getRoot())
                                              .withPluginClasspath()
                                              .withArguments(CHECK_JAVADOC_LINK, DEBUG_OPTION)
                                              .build();

        final List<String> expected = Arrays.asList(COMPILE_LOG, CHECK_JAVADOC_LOG);

        assertEquals(expected, extractTasks(buildResult));
        assertTrue(buildResult.getOutput()
                              .contains(WRONG_LINK_FORMAT_MSG));
    }

    private static List<String> extractTasks(BuildResult buildResult) {
        return FluentIterable
                .from(buildResult.getTasks())
                .transform(new Function<BuildTask, String>() {
                    @Override
                    public String apply(BuildTask buildTask) {
                        return buildTask.getPath();
                    }
                })
                .toList();
    }

    private InputStream getBuildFileContent(int threshold, ReportType reportType)
            throws IOException {
        final InputStream input =
                getClass().getClassLoader()
                          .getResourceAsStream("projects/JavaDocCheckerPlugin/build.gradle");
        final StringWriter writer = new StringWriter();
        IOUtils.copy(input, writer);

        final String thresholdValue = String.valueOf(threshold);
        final String reportTypeValue = format("\"%s\"", reportType.getValue());

        final String writerContent = writer.toString();
        String result = writerContent.replace("thresholdValue", thresholdValue);
        result = result.replace("reportTypeValue", reportTypeValue);

        final InputStream stream = new ByteArrayInputStream(result.getBytes(UTF_8));
        return stream;
    }
}
