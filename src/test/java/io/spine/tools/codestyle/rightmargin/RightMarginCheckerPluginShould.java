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
import com.google.common.collect.ImmutableList;
import io.spine.tools.codestyle.CodeStyleCheck;
import io.spine.tools.codestyle.CodeStyleViolation;
import io.spine.tools.codestyle.Given;
import io.spine.tools.codestyle.ReportType;
import io.spine.tools.codestyle.StepConfiguration;
import io.spine.tools.gradle.TaskName;
import org.gradle.internal.impldep.org.apache.commons.io.FileUtils;
import org.gradle.internal.impldep.org.apache.commons.io.IOUtils;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.BuildTask;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Alexander Aleksandrov
 */
public class RightMarginCheckerPluginShould {

    /**
     * A fragment of log error message {@linkplain
     * RightMarginCheck#onViolation(Path, CodeStyleViolation) issued by the check}.
     */
    private static final String LONG_LINE_MSG_FRAGMENT = "is longer than configured limit";

    private static final String CHECK_RIGHT_MARGIN_WRAPPING =
            TaskName.CHECK_RIGHT_MARGIN_WRAPPING.getValue();
    
    private static final int THRESHOLD = 100;

    @Rule
    public final TemporaryFolder testProjectDir = new TemporaryFolder();
    private String resourceFolder = "";

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

    private void setUpTestProject(int threshold, ReportType reportType) throws IOException {
        final Path buildGradleFile = testProjectDir.getRoot()
                                                   .toPath()
                                                   .resolve(Given.buildGradleFile());
        final InputStream input = getBuildFileContent(threshold, reportType);

        final Path testSources = testProjectDir.getRoot()
                                               .toPath()
                                               .resolve(Given.sourceFolder());
        Files.copy(input, buildGradleFile);
        Files.createDirectories(testSources);

        final String resourceFilePath = Given.resourceFilePath();
        final int endIndex = resourceFilePath.length() - Given.testFile()
                                                              .length();
        resourceFolder = resourceFilePath.substring(0, endIndex);
    }

    @Test
    public void warn_by_default_about_long_lines() throws IOException {
        setUpTestProject(THRESHOLD, ReportType.WARN);
        final Path testSources = testProjectDir.getRoot()
                                               .toPath()
                                               .resolve(Given.sourceFolder());
        FileUtils.copyDirectory(new File(resourceFolder), new File(testSources.toString()));

        BuildResult buildResult =
                GradleRunner.create()
                            .withProjectDir(testProjectDir.getRoot())
                            .withPluginClasspath()
                            .withArguments(CHECK_RIGHT_MARGIN_WRAPPING, Given.debugOption())
                            .build();

        final List<String> expected =
                ImmutableList.of(Given.compileLog(), ":checkRightMarginWrapping");

        assertEquals(expected, extractTasks(buildResult));
        assertTrue(buildResult.getOutput()
                              .contains(LONG_LINE_MSG_FRAGMENT));
    }

    @Test
    public void warn_longLines() {
        final File file = new File(Given.resourceFilePath());
        final Path path = Paths.get(file.getAbsolutePath());
        final StepConfiguration configuration = new StepConfiguration();
        configuration.setThreshold(100);
        configuration.setReportType("warn");
        CodeStyleCheck check = new RightMarginCheck(configuration);
        check.process(path);
    }

    private InputStream getBuildFileContent(int threshold, ReportType reportType)
            throws IOException {
        final InputStream input = getClass()
                .getClassLoader()
                .getResourceAsStream("projects/JavaDocCheckerPlugin/build-right-margin.gradle");
        final StringWriter writer = new StringWriter();
        IOUtils.copy(input, writer);

        final String thresholdValue = String.valueOf(threshold);
        final String reportTypeValue = format("\"%s\"", reportType.getValue());

        final String writerContent = writer.toString();
        @SuppressWarnings("DynamicRegexReplaceableByCompiledPattern")
        String result = writerContent.replace("thresholdValue", thresholdValue)
                                     .replace("reportTypeValue", reportTypeValue);

        final InputStream stream = new ByteArrayInputStream(result.getBytes(UTF_8));
        return stream;
    }

}
