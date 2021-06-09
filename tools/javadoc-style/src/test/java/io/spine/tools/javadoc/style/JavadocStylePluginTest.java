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

import io.spine.testing.TempDir;
import io.spine.tools.gradle.TaskName;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.plugins.PluginContainer;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static io.spine.tools.gradle.JavaTaskName.compileJava;
import static io.spine.tools.gradle.JavaTaskName.compileTestJava;
import static io.spine.tools.javadoc.style.JavadocStyleTaskName.formatProtoDoc;
import static io.spine.tools.javadoc.style.JavadocStyleTaskName.formatTestProtoDoc;
import static io.spine.tools.gradle.ProtobufTaskName.generateProto;
import static io.spine.tools.gradle.ProtobufTaskName.generateTestProto;
import static io.spine.tools.gradle.TaskDependencies.dependsOn;
import static io.spine.tools.javadoc.style.BacktickFormatting.BACKTICK;
import static io.spine.tools.javadoc.style.PreTagFormatting.CLOSING_PRE;
import static io.spine.tools.javadoc.style.PreTagFormatting.OPENING_PRE;
import static io.spine.tools.javadoc.style.TestHelper.formatAndAssert;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("ProtoJavadocPlugin should")
class JavadocStylePluginTest {

    private static final String PLUGIN_ID = "io.spine.javadoc-style";

    private File testProjectDir;

    private Project project;

    @BeforeEach
    void setUp() {
        testProjectDir = TempDir.forClass(getClass());
        project = newProject();
        project.getPluginManager()
               .apply(PLUGIN_ID);
    }

    @Test
    @DisplayName("apply to project")
    void applyToProject() {
        PluginContainer plugins = project.getPlugins();
        assertTrue(plugins.hasPlugin(PLUGIN_ID));
    }

    @Test
    @DisplayName("have extension")
    void haveExtension() {
        JavadocStyleExtension extension = project.getExtensions()
                                                 .getByType(JavadocStyleExtension.class);
        assertNotNull(extension);
    }

    @Test
    @DisplayName("add formatProtoDoc task")
    void addTaskFormatProtoDoc() {
        Task task = task(formatProtoDoc);
        assertNotNull(task);
        assertTrue(dependsOn(task, generateProto));
        assertTrue(dependsOn(task(compileJava), task));
    }

    @Test
    @DisplayName("add formatTestProtoDoc task")
    void addTaskFormatTestProtoDoc() {
        Task task = task(formatTestProtoDoc);
        assertNotNull(task);
        assertTrue(dependsOn(task, generateTestProto));
        assertTrue(dependsOn(task(compileTestJava), task));
    }

    @Test
    @DisplayName("format generated java sources")
    void formatGeneratedJavaSources() throws IOException {
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
    void handleMultilineCodeSnippetsProperly() throws IOException {
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
                      .getByName(taskName.name());
    }

    private static Project newProject() {
        Project project = ProjectBuilder.builder()
                                        .build();
        project.task(compileJava.name());
        project.task(compileTestJava.name());
        project.task(generateProto.name());
        project.task(generateTestProto.name());
        return project;
    }
}
