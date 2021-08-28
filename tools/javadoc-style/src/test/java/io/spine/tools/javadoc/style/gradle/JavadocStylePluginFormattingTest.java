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

package io.spine.tools.javadoc.style.gradle;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import io.spine.tools.gradle.testing.GradleProject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;
import static io.spine.tools.javadoc.style.formatting.BacktickedToCode.BACKTICK;
import static io.spine.tools.javadoc.style.formatting.RemovePreTags.CLOSING_PRE;
import static io.spine.tools.javadoc.style.formatting.RemovePreTags.OPENING_PRE;
import static io.spine.tools.javadoc.style.gradle.JavadocStyleTaskName.formatProtoDoc;
import static java.lang.System.lineSeparator;
import static java.nio.charset.StandardCharsets.UTF_8;

@DisplayName("`JavadocStylePlugin` should format generated Javadoc sources with")
class JavadocStylePluginFormattingTest {

    /**
     * The {@code protoJavadoc.mainGenProtoDir} value from the plugin configuration.
     *
     * <p>This value is located in the test {@code build.gradle}.
     */
    private static final String MAIN_GEN_PROTO_LOCATION = "generated/main/java";

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

    private static String singleLineJavadoc(String javadocText) {
        return "/** " + javadocText + " */";
    }

    private static String multilineJavadoc(String codeStartTag, String codeEndTag) {
        return String.format(
                "/**%nJavadoc header%n" +
                        "<pre>%s" + "java snippet" + "%s</pre>%n" +
                        "Javadoc footer" +
                        "*/",
                codeStartTag, codeEndTag);
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
