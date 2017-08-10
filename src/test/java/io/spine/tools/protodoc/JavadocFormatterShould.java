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

import com.google.common.base.Joiner;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static io.spine.tools.protodoc.BackTickFormatting.wrapWithCodeTag;
import static io.spine.tools.protodoc.JavaSources.javaExt;
import static java.lang.System.lineSeparator;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;

/**
 * @author Dmytro Grankin
 */
public class JavadocFormatterShould {

    private static final char BACK_TICK = '`';
    private static final String TEXT = "plain text";
    private static final String TEXT_IN_CODE_TAG = wrapWithCodeTag(TEXT);
    private static final String TEXT_IN_BACK_TICKS = BACK_TICK + TEXT + BACK_TICK;

    @Rule
    public final TemporaryFolder folder = new TemporaryFolder();
    private final FormattingAction formatting = new BackTickFormatting();
    private final List<FormattingAction> actions = singletonList(formatting);
    private final JavadocFormatter backTickFormatter = new JavadocFormatter(actions);

    @Test
    public void ignore_files_except_java() {
        final Path path = Paths.get("Non_existing_file.txt");
        backTickFormatter.format(path);
    }

    @Test
    public void format_Javadocs() throws Exception {
        final String javadoc = getJavadoc(TEXT_IN_BACK_TICKS);
        final String expected = getJavadoc(TEXT_IN_CODE_TAG);
        assertEquals(expected, getFormattingResult(javadoc));
    }

    @Test
    public void not_format_text_which_is_not_Javadoc() throws Exception {
        assertEquals(TEXT_IN_BACK_TICKS, getFormattingResult(TEXT_IN_BACK_TICKS));
    }

    private static String getJavadoc(String javadocText) {
        return "/** " + javadocText + " */";
    }

    private String getFormattingResult(String content) throws IOException {
        final Path path = createJavaFile();
        Files.write(path, content.getBytes());

        backTickFormatter.format(path);

        final List<String> lines = Files.readAllLines(path, UTF_8);
        return Joiner.on(lineSeparator())
                     .join(lines);
    }

    private Path createJavaFile() throws IOException {
        final String fileName = "JavadocFormatter_test_file" + javaExt();
        final File file = folder.newFile(fileName);
        return file.toPath();
    }
}
