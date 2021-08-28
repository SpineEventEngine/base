/*
 * Copyright 2021, TeamDev. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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

package io.spine.tools.javadoc.style;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import io.spine.tools.gradle.TaskName;
import io.spine.tools.gradle.testing.GradleProject;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.plugins.PluginContainer;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;
import static io.spine.tools.gradle.JavaTaskName.compileJava;
import static io.spine.tools.gradle.JavaTaskName.compileTestJava;
import static io.spine.tools.gradle.ProtobufTaskName.generateProto;
import static io.spine.tools.gradle.ProtobufTaskName.generateTestProto;
import static io.spine.tools.gradle.TaskDependencies.dependsOn;
import static io.spine.tools.javadoc.style.BacktickFormatting.BACKTICK;
import static io.spine.tools.javadoc.style.JavadocStyleTaskName.formatProtoDoc;
import static io.spine.tools.javadoc.style.JavadocStyleTaskName.formatTestProtoDoc;
import static io.spine.tools.javadoc.style.PreTagFormatting.CLOSING_PRE;
import static io.spine.tools.javadoc.style.PreTagFormatting.OPENING_PRE;
import static java.lang.System.lineSeparator;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("`JavadocStylePlugin` should")
class JavadocStylePluginTest {

    /**
     * The {@code protoJavadoc.mainGenProtoDir} value from the plugin configuration.
     *
     * <p>This value is located in the test {@code build.gradle}.
     */
    private static final String MAIN_GEN_PROTO_LOCATION = "generated/main/java";

    private static final String PLUGIN_ID = "io.spine.javadoc-style";

    private static Project project = null;

    @BeforeAll
    static void createProjectWithPlugin() {
        project = ProjectBuilder.builder()
                .build();
        project.task(compileJava.name());
        project.task(compileTestJava.name());
        project.task(generateProto.name());
        project.task(generateTestProto.name());
        project.getPluginManager()
               .apply(PLUGIN_ID);
    }

    @Nested
    @DisplayName("apply plugin to a project")
    class Applying {

        @Test
        @DisplayName("by the plugin ID")
        void applyToProject() {
            PluginContainer plugins = project.getPlugins();
            assertTrue(plugins.hasPlugin(PLUGIN_ID));
        }

        @Test
        @DisplayName("add its extension to a project")
        void haveExtension() {
            JavadocStyleExtension extension =
                    project.getExtensions()
                           .getByType(JavadocStyleExtension.class);
            assertNotNull(extension);
        }
    }

    @Nested
    @DisplayName("add a custom task to a project")
    class Tasks {

        @Test
        @DisplayName("called `formatProtoDoc`")
        void addTaskFormatProtoDoc() {
            Task task = task(formatProtoDoc);
            assertNotNull(task);
            assertTrue(dependsOn(task, generateProto));
            assertTrue(dependsOn(task(compileJava), task));
        }

        @Test
        @DisplayName("called `formatTestProtoDoc`")
        void addTaskFormatTestProtoDoc() {
            Task task = task(formatTestProtoDoc);
            assertNotNull(task);
            assertTrue(dependsOn(task, generateTestProto));
            assertTrue(dependsOn(task(compileTestJava), task));
        }

        private Task task(TaskName taskName) {
            return project.getTasks()
                          .getByName(taskName.name());
        }
    }

    @Nested
    @DisplayName("format generated Javadoc sources with")
    class FormattingCode {

        @Test
        @DisplayName("single-line code snippet")
        void formatGeneratedJavaSources(@TempDir Path testProjectDir) throws IOException {
            String text = "javadoc text";
            String generatedFieldDescription = " <code>field description</code>";
            String textInPreTags = OPENING_PRE + text + CLOSING_PRE + generatedFieldDescription;
            String expected = singleLineJavadoc(text + generatedFieldDescription);
            String javadocToFormat = singleLineJavadoc(textInPreTags);
            formatAndAssert(expected,
                            javadocToFormat,
                            testProjectDir.toFile(),
                            "SingleLineJavadocTest.java");
        }

        @Test
        @DisplayName("multi-line code snippet")
        void handleMultilineCodeSnippetsProperly(@TempDir Path testProjectDir) throws IOException {
            String protoDoc = multilineJavadoc(BACKTICK, BACKTICK);
            String javadoc = multilineJavadoc("{@code ", "}");

            formatAndAssert(javadoc,
                            protoDoc,
                            testProjectDir.toFile(),
                            "MultiLineJavadocTest.java");
        }

        private String singleLineJavadoc(String javadocText) {
            return "/** " + javadocText + " */";
        }

        private String multilineJavadoc(String codeStartTag, String codeEndTag) {
            return String.format(
                    "/**%nJavadoc header%n" +
                            "<pre>%s" + "java snippet" + "%s</pre>%n" +
                            "Javadoc footer" +
                            "*/",
                    codeStartTag, codeEndTag);
        }
    }

    static void formatAndAssert(String expectedContent,
                                String contentToFormat,
                                File folder,
                                String fileName)
            throws IOException {
        Path formattedFilePath = createAndFormatFile(contentToFormat, folder, fileName);
        List<String> formattedLines = Files.readAllLines(formattedFilePath, UTF_8);
        String mergedLines = Joiner.on(lineSeparator())
                                   .join(formattedLines);
        assertThat(mergedLines)
                .isEqualTo(expectedContent);
    }

    private static Path createAndFormatFile(String fileContent, File folder, String fileName) {
        String sourceFile = MAIN_GEN_PROTO_LOCATION + '/' + fileName;

        executeTask(sourceFile, folder, fileContent);

        Path result = folder.toPath()
                            .resolve(sourceFile);
        return result;
    }

    private static void executeTask(String filePath, File folder, String fileContent) {
        GradleProject project = GradleProject.newBuilder()
                .setProjectName("proto-javadoc-test")
                .setProjectFolder(folder)
                .createFile(filePath, ImmutableList.of(fileContent))
                .build();
        project.executeTask(formatProtoDoc);
    }
}
