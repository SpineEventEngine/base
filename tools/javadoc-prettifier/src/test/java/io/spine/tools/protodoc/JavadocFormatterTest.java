/*
 * Copyright 2020, TeamDev. All rights reserved.
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

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
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

import static io.spine.tools.protodoc.BacktickFormatting.wrapWithCodeTag;
import static java.lang.System.lineSeparator;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("JavadocFormatter should")
class JavadocFormatterTest {

    private static final char BACKTICK = '`';
    private static final String TEXT = "plain text";
    private static final String TEXT_IN_CODE_TAG = wrapWithCodeTag(TEXT);
    private static final String TEXT_IN_BACKTICKS = BACKTICK + TEXT + BACKTICK;

    private File folder;
    private final FormattingAction formatting = new BacktickFormatting();
    private final ImmutableList<FormattingAction> actions = ImmutableList.of(formatting);
    private final JavadocFormatter backtickFormatter = new JavadocFormatter(actions);

    @BeforeEach
    void setUp(@TempDir Path tempDirPath) {
        folder = tempDirPath.toFile();
    }

    @Test
    @DisplayName("ignore files expect java")
    void ignore_files_except_java() throws IOException {
        Path path = Paths.get("Non_existing_file.txt");
        backtickFormatter.format(path);
    }

    @Test
    @DisplayName("format Javadocs")
    void format_Javadocs() throws Exception {
        String javadoc = getJavadoc(TEXT_IN_BACKTICKS);
        String expected = getJavadoc(TEXT_IN_CODE_TAG);
        assertEquals(expected, getFormattingResult(javadoc));
    }

    @Test
    @DisplayName("not format non-Javadoc text")
    void not_format_text_which_is_not_Javadoc() throws Exception {
        assertEquals(TEXT_IN_BACKTICKS, getFormattingResult(TEXT_IN_BACKTICKS));
    }

    private static String getJavadoc(String javadocText) {
        return "/** " + javadocText + " */";
    }

    private String getFormattingResult(String content) throws IOException {
        Path path = createJavaFile();
        Files.write(path, ImmutableList.of(content));

        backtickFormatter.format(path);

        List<String> lines = Files.readAllLines(path, UTF_8);
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
