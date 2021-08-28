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

package io.spine.tools.javadoc.style.formatting;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import io.spine.tools.javadoc.style.formatting.BacktickFormatting;
import io.spine.tools.javadoc.style.formatting.JavadocFormatter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static io.spine.tools.javadoc.style.formatting.BacktickFormatting.wrapWithCodeTag;
import static java.lang.System.lineSeparator;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.readAllLines;
import static java.nio.file.Files.write;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("`JavadocFormatter` should")
class JavadocFormatterTest {

    private static final char BACKTICK = '`';
    private static final String TEXT = "plain text";
    private static final String TEXT_IN_CODE_TAG = wrapWithCodeTag(TEXT);
    private static final String TEXT_IN_BACKTICKS = BACKTICK + TEXT + BACKTICK;

    private File folder;

    private final JavadocFormatter backtickFormatter = new JavadocFormatter(
            new BacktickFormatting()
    );

    @BeforeEach
    void setUp(@TempDir Path tempDirPath) {
        folder = tempDirPath.toFile();
    }

    @Test
    @DisplayName("ignore files expect java")
    void processOnlyJava() throws IOException {
        Path path = Paths.get("Non_existing_file.txt");
        backtickFormatter.format(path);
    }

    @Test
    @DisplayName("format Javadocs")
    void formatJavadocs() throws Exception {
        String javadoc = wrapAsJavadoc(TEXT_IN_BACKTICKS);
        String expected = wrapAsJavadoc(TEXT_IN_CODE_TAG);
        assertEquals(expected, applyFormatting(javadoc));
    }

    @Test
    @DisplayName("not format non-Javadoc text")
    void notFormatNonJavadoc() throws Exception {
        assertEquals(TEXT_IN_BACKTICKS, applyFormatting(TEXT_IN_BACKTICKS));
    }

    private static String wrapAsJavadoc(String javadocText) {
        return "/** " + javadocText + " */";
    }

    private String applyFormatting(String content) throws IOException {
        Path path = createJavaFile();
        write(path, ImmutableList.of(content));

        backtickFormatter.format(path);

        List<String> lines = readAllLines(path, UTF_8);
        return Joiner.on(lineSeparator())
                     .join(lines);
    }

    private Path createJavaFile() throws IOException {
        String fileName = "JavadocFormatter_test_file.java";
        Path absoluteFilePath = Paths.get(folder.getAbsolutePath(), fileName);
        Path filePath = Files.createFile(absoluteFilePath);
        return filePath;
    }
}
