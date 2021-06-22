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
import io.spine.tools.gradle.testing.GradleProject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static io.spine.tools.javadoc.style.JavadocStyleTaskName.formatProtoDoc;
import static java.lang.System.lineSeparator;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * A helper class for the {@link JavadocStylePlugin} testing.
 */
final class TestHelper {

    /**
     * The {@code protoJavadoc.mainGenProtoDir} value from the plugin configuration.
     *
     * <p>This value is located in the test {@code build.gradle}.
     */
    private static final String MAIN_GEN_PROTO_LOCATION = "generated/main/java";

    /** Prevents the utility class instantiation. */
    private TestHelper() {
    }

    static void formatAndAssert(String expectedContent, String contentToFormat, File folder)
            throws IOException {
        Path formattedFilePath = format(contentToFormat, folder);
        List<String> formattedLines = Files.readAllLines(formattedFilePath, UTF_8);
        String mergedLines = Joiner.on(lineSeparator())
                                   .join(formattedLines);
        assertEquals(expectedContent, mergedLines);
    }

    private static Path format(String fileContent, File folder) {
        String sourceFile = MAIN_GEN_PROTO_LOCATION + "/TestSource.java";

        executeTask(sourceFile, folder, fileContent);

        Path result = folder.toPath()
                            .resolve(sourceFile);
        return result;
    }

    private static void executeTask(String filePath, File folder, String fileContent) {
        GradleProject project = GradleProject
                .newBuilder()
                .setProjectName("proto-javadoc-test")
                .setProjectFolder(folder)
                .createFile(filePath, ImmutableList.of(fileContent))
                .build();
        project.executeTask(formatProtoDoc);
    }
}
