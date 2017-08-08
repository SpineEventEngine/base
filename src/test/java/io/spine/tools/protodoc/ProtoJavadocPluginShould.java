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

package io.spine.tools.protodoc;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import io.spine.gradle.TaskName;
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
import java.util.Arrays;
import java.util.List;

import static io.spine.tools.protodoc.Given.buildGradleFile;
import static io.spine.tools.protodoc.Given.compileLog;
import static io.spine.tools.protodoc.Given.debugOption;
import static io.spine.tools.protodoc.Given.sourceFolder;
import static io.spine.tools.protodoc.Given.testFile;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.assertEquals;

public class ProtoJavadocPluginShould {

    private static final String CHECK_JAVADOC_LINK = TaskName.CHECK_FQN.getValue();
    private static final String CHECK_JAVADOC_LOG = ":checkJavadocLink";

    private String resourceFolder = "";

    @Rule
    @SuppressWarnings("PublicField")    // JUnit rules should be `public`.
    public final TemporaryFolder testProjectDir = new TemporaryFolder();

    public void setUpTestProject() throws IOException {
        final Path buildGradleFile = testProjectDir.getRoot()
                                                   .toPath()
                                                   .resolve(buildGradleFile());
        final InputStream input = getBuildFileContent();

        final Path testSources = testProjectDir.getRoot()
                                               .toPath()
                                               .resolve(sourceFolder());
        Files.copy(input, buildGradleFile);
        Files.createDirectories(testSources);

        ClassLoader classLoader = getClass().getClassLoader();
        final String testFile = testFile();
        final String resourceFilePath = classLoader.getResource(testFile)
                                                   .getPath();
        final int endIndex = resourceFilePath.length() - testFile.length();
        resourceFolder = resourceFilePath.substring(0, endIndex);
    }

    @Test
    public void replace_tags_with_spaces() throws IOException {
        setUpTestProject();
        final Path testSources = testProjectDir.getRoot()
                                               .toPath()
                                               .resolve(sourceFolder());
        FileUtils.copyDirectory(new File(resourceFolder), new File(testSources.toString()));

        final GradleRunner gradleRunner = GradleRunner.create()
                                                      .withProjectDir(testProjectDir.getRoot())
                                                      .withPluginClasspath()
                                                      .withArguments(CHECK_JAVADOC_LINK, debugOption());
        BuildResult buildResult = gradleRunner.build();

        final List<String> expected = Arrays.asList(compileLog(), CHECK_JAVADOC_LOG);
        assertEquals(expected, extractTasks(buildResult));
    }



    private InputStream getBuildFileContent()
            throws IOException {
        final InputStream input =
                getClass().getClassLoader()
                          .getResourceAsStream("projects/ProtobufJavadocPlugin/build.gradle");
        final StringWriter writer = new StringWriter();
        IOUtils.copy(input, writer);
        final String writerContent = writer.toString();

        final InputStream stream = new ByteArrayInputStream(writerContent.getBytes(UTF_8));
        return stream;
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
}
