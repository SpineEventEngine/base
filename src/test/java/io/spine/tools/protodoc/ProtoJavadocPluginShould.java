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

import io.spine.gradle.TaskName;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.plugins.PluginContainer;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;

import static io.spine.gradle.TaskDependencies.dependsOn;
import static io.spine.gradle.TaskName.COMPILE_JAVA;
import static io.spine.gradle.TaskName.COMPILE_TEST_JAVA;
import static io.spine.gradle.TaskName.FORMAT_PROTO_DOC;
import static io.spine.gradle.TaskName.FORMAT_TEST_PROTO_DOC;
import static io.spine.gradle.TaskName.GENERATE_PROTO;
import static io.spine.gradle.TaskName.GENERATE_TEST_PROTO;
import static io.spine.tools.protodoc.BacktickFormatting.BACKTICK;
import static io.spine.tools.protodoc.Given.formatAndAssert;
import static io.spine.tools.protodoc.PreTagFormatting.CLOSING_PRE;
import static io.spine.tools.protodoc.PreTagFormatting.OPENING_PRE;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Dmytro Grankin
 */
public class ProtoJavadocPluginShould {

    private static final String PLUGIN_ID = "io.spine.tools.protobuf-javadoc-plugin";

    @Rule
    public final TemporaryFolder testProjectDir = new TemporaryFolder();
    private Project project;

    @Before
    public void setUp() throws Exception {
        project = newProject();
        project.getPluginManager()
               .apply(PLUGIN_ID);
    }

    @Test
    public void apply_to_project() {
        final PluginContainer plugins = project.getPlugins();
        assertTrue(plugins.hasPlugin(PLUGIN_ID));
    }

    @Test
    public void have_extension() {
        final Extension extension = project.getExtensions()
                                           .getByType(Extension.class);
        assertNotNull(extension);
    }

    @Test
    public void add_task_formatProtoDoc() {
        final Task formatProtoDoc = task(FORMAT_PROTO_DOC);
        assertNotNull(formatProtoDoc);
        assertTrue(dependsOn(formatProtoDoc, GENERATE_PROTO));
        assertTrue(dependsOn(task(COMPILE_JAVA), formatProtoDoc));
    }

    @Test
    public void add_task_formatTestProtoDoc() {
        final Task formatTestProtoDoc = task(FORMAT_TEST_PROTO_DOC);
        assertNotNull(formatTestProtoDoc);
        assertTrue(dependsOn(formatTestProtoDoc, GENERATE_TEST_PROTO));
        assertTrue(dependsOn(task(COMPILE_TEST_JAVA), formatTestProtoDoc));
    }

    @Test
    public void format_generated_java_sources() throws IOException {
        final String text = "javadoc text";
        final String generatedFieldDescription = " <code>field description</code>";
        final String textInPreTags = new StringBuilder().append(OPENING_PRE)
                                                        .append(text)
                                                        .append(CLOSING_PRE)
                                                        .append(generatedFieldDescription)
                                                        .toString();
        final String expected = getJavadoc(text + generatedFieldDescription);
        final String javadocToFormat = getJavadoc(textInPreTags);
        formatAndAssert(expected, javadocToFormat, testProjectDir);
    }

    @Test
    public void handle_multiline_code_snippets_properly() throws IOException {
        final String protoDoc = multilineJavadoc(BACKTICK, BACKTICK);
        final String javadoc = multilineJavadoc("{@code ", "}");

        formatAndAssert(javadoc, protoDoc, testProjectDir);
    }

    private static String multilineJavadoc(String codeOpening, String codeClosing) {
        final String text = new StringBuilder().append("/**")
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
                      .getByName(taskName.getValue());
    }

    private static Project newProject() {
        final Project project = ProjectBuilder.builder()
                                              .build();
        project.task(COMPILE_JAVA.getValue());
        project.task(COMPILE_TEST_JAVA.getValue());
        project.task(GENERATE_PROTO.getValue());
        project.task(GENERATE_TEST_PROTO.getValue());
        return project;
    }
}
