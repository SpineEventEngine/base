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

package io.spine.tools.codestyle.rightmargin;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import io.spine.tools.codestyle.Given;
import io.spine.tools.codestyle.ReportType;
import org.gradle.internal.impldep.org.apache.commons.io.FileUtils;
import org.gradle.internal.impldep.org.apache.commons.io.IOUtils;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.BuildTask;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import io.spine.gradle.TaskName;
import io.spine.tools.codestyle.StepConfiguration;

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

public class RightMarginCheckerPluginShould {
    private static final String LONG_LINE_MSG = "Right margin trespassing found";
    private static final String CHECK_RIGHT_MARGIN_WRAPPING = TaskName.CHECK_RIGHT_MARGIN_WRAPPING.getValue();
    private static final int THRESHOLD = 100;

    private String resourceFolder = "";

    @Rule
    public final TemporaryFolder testProjectDir = new TemporaryFolder();

    public void setUpTestProject(int threshold, ReportType reportType) throws IOException {
        final Path buildGradleFile = testProjectDir.getRoot()
                                                   .toPath()
                                                   .resolve(Given.buildGradleFile());
        final InputStream input = getBuildFileContent(threshold, reportType);

        final Path testSources = testProjectDir.getRoot()
                                               .toPath()
                                               .resolve(Given.sourceFolder());
        Files.copy(input, buildGradleFile);
        Files.createDirectories(testSources);

        ClassLoader classLoader = getClass().getClassLoader();
        final String testFile = Given.testFile();
        final String resourceFilePath = classLoader.getResource(testFile)
                                                   .getPath();
        final int endIndex = resourceFilePath.length() - testFile.length();
        resourceFolder = resourceFilePath.substring(0, endIndex);
    }

    @Test
    public void warn_by_default_about_long_lines() throws IOException {
        setUpTestProject(THRESHOLD, ReportType.WARN);
        final Path testSources = testProjectDir.getRoot()
                                               .toPath()
                                               .resolve(Given.sourceFolder());
        FileUtils.copyDirectory(new File(resourceFolder), new File(testSources.toString()));

        BuildResult buildResult = GradleRunner.create()
                                              .withProjectDir(testProjectDir.getRoot())
                                              .withPluginClasspath()
                                              .withArguments(CHECK_RIGHT_MARGIN_WRAPPING, Given.debugOption())
                                              .build();

        final List<String> expected = Arrays.asList(Given.compileLog(), ":checkRightMarginWrapping");

        assertEquals(expected, extractTasks(buildResult));
        assertTrue(buildResult.getOutput().contains(LONG_LINE_MSG));
    }

    @Test
    public void warn_longLines() throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        final File file = new File(classLoader.getResource(Given.testFile()).getFile());
        final Path path = Paths.get(file.getAbsolutePath());
        final StepConfiguration configuration = new StepConfiguration();
        configuration.setThreshold(100);
        configuration.setReportType("warn");
        RightMarginValidator validator = new RightMarginValidator(configuration);
        validator.validate(path);

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
                          .getResourceAsStream("projects/JavaDocCheckerPlugin/build-right-margin.gradle");
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
