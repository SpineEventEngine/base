/*
 * Copyright 2019, TeamDev. All rights reserved.
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

import io.spine.tools.gradle.TaskName;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.plugins.PluginContainer;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junitpioneer.jupiter.TempDirectory;
import org.junitpioneer.jupiter.TempDirectory.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static io.spine.tools.gradle.TaskDependencies.dependsOn;
import static io.spine.tools.gradle.TaskName.compileJava;
import static io.spine.tools.gradle.TaskName.compileTestJava;
import static io.spine.tools.gradle.TaskName.generateProto;
import static io.spine.tools.gradle.TaskName.generateTestProto;
import static io.spine.tools.protodoc.BacktickFormatting.BACKTICK;
import static io.spine.tools.protodoc.PreTagFormatting.CLOSING_PRE;
import static io.spine.tools.protodoc.PreTagFormatting.OPENING_PRE;
import static io.spine.tools.protodoc.TestHelper.formatAndAssert;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(TempDirectory.class)
@DisplayName("ProtoJavadocPlugin should")
class ProtoJavadocPluginTest {

    private static final String PLUGIN_ID = "io.spine.tools.protobuf-javadoc-plugin";

    private File testProjectDir;

    private Project project;

    @BeforeEach
    void setUp(@TempDir Path tempDirPath) {
        testProjectDir = tempDirPath.toFile();
        project = newProject();
        project.getPluginManager()
               .apply(PLUGIN_ID);
    }

    @Test
    @DisplayName("apply to project")
    void apply_to_project() {
        PluginContainer plugins = project.getPlugins();
        assertTrue(plugins.hasPlugin(PLUGIN_ID));
    }

    @Test
    @DisplayName("have extension")
    void have_extension() {
        Extension extension = project.getExtensions()
                                     .getByType(Extension.class);
        assertNotNull(extension);
    }

    @Test
    @DisplayName("add formatProtoDoc task")
    void add_task_formatProtoDoc() {
        Task formatProtoDoc = task(TaskName.formatProtoDoc);
        assertNotNull(formatProtoDoc);
        assertTrue(dependsOn(formatProtoDoc, generateProto));
        assertTrue(dependsOn(task(compileJava), formatProtoDoc));
    }

    @Test
    @DisplayName("add formatTestProtoDoc task")
    void add_task_formatTestProtoDoc() {
        Task formatTestProtoDoc = task(TaskName.formatTestProtoDoc);
        assertNotNull(formatTestProtoDoc);
        assertTrue(dependsOn(formatTestProtoDoc, generateTestProto));
        assertTrue(dependsOn(task(compileTestJava), formatTestProtoDoc));
    }

    @Test
    @DisplayName("format generated java sources")
    void format_generated_java_sources() throws IOException {
        String text = "javadoc text";
        String generatedFieldDescription = " <code>field description</code>";
        String textInPreTags = new StringBuilder().append(OPENING_PRE)
                                                  .append(text)
                                                  .append(CLOSING_PRE)
                                                  .append(generatedFieldDescription)
                                                  .toString();
        String expected = getJavadoc(text + generatedFieldDescription);
        String javadocToFormat = getJavadoc(textInPreTags);
        formatAndAssert(expected, javadocToFormat, testProjectDir);
    }

    @Test
    @DisplayName("handle multiline code snippets")
    void handle_multiline_code_snippets_properly() throws IOException {
        String protoDoc = multilineJavadoc(BACKTICK, BACKTICK);
        String javadoc = multilineJavadoc("{@code ", "}");

        formatAndAssert(javadoc, protoDoc, testProjectDir);
    }

    private static String multilineJavadoc(String codeOpening, String codeClosing) {
        String text = new StringBuilder().append("/**")
                                         .append(System.lineSeparator())
                                         .append("Javadoc header")
                                         .append(System.lineSeparator())
                                         .append(OPENING_PRE)
                                         .append(codeOpening)
                                         .append("java snippet")
                                         .append(codeClosing)
                                         .append(CLOSING_PRE)
                                         .append(System.lineSeparator())
                                         .append("Javadoc footer")
                                         .append("*/")
                                         .toString();
        return text;
    }

    private static String getJavadoc(String javadocText) {
        return "/** " + javadocText + " */";
    }

    private Task task(TaskName taskName) {
        return project.getTasks()
                      .getByName(taskName.value());
    }

    private static Project newProject() {
        Project project = ProjectBuilder.builder()
                                        .build();
        project.task(compileJava.value());
        project.task(compileTestJava.value());
        project.task(generateProto.value());
        project.task(generateTestProto.value());
        return project;
    }
}
