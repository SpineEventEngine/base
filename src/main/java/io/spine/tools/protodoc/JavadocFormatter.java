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

import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.spine.tools.protodoc.JavaSources.isJavaFile;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * A formatter for Javadocs.
 *
 * <p>The formatter executes {@linkplain FormattingAction formatting actions}
 * for the Javadoc lines in a source file.
 *
 * @author Dmytro Grankin
 */
class JavadocFormatter {

    /**
     * A pattern for multi-lined Javadoc.
     */
    private static final Pattern PATTERN_JAVADOC = Pattern.compile("(?s)/\\*\\*.*?\\*/");

    /**
     * The formatting actions to perform.
     */
    private final List<FormattingAction> actions;

    JavadocFormatter(List<FormattingAction> actions) {
        this.actions = actions;
    }

    /**
     * Formats the Javadocs in the file with the specified path.
     *
     * <p>If the file is not a {@code .java} source, does noting.
     *
     * @param path the path to the file
     */
    void format(Path path) {
        if (!isJavaFile(path)) {
            return;
        }

        final File file = path.toFile();
        final String content = readFileContent(file);
        final String formattedContent = formatJavadocs(content);
        writeToFile(file, formattedContent);
    }

    /**
     * Formats Javadocs in the specified content of the file.
     *
     * @param sourceContent the content of a {@code .java} source
     * @return the source content with formatted Javadocs
     */
    private String formatJavadocs(CharSequence sourceContent) {
        final Matcher matcher = PATTERN_JAVADOC.matcher(sourceContent);
        final StringBuffer buffer = new StringBuffer(sourceContent.length() * 2);
        while (matcher.find()) {
            final String partToFormat = matcher.group();
            final String replacement = formatText(partToFormat);
            matcher.appendReplacement(buffer, replacement);
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }

    /**
     * Obtains formatted representation of the specified text.
     *
     * @param text the text to format
     * @return the formatted representation
     */
    private String formatText(String text) {
        String currentState = text;
        for (FormattingAction action : actions) {
            final List<String> lines = Collections.singletonList(currentState);
            currentState = action.execute(lines).get(0);
        }
        return currentState;
    }

    private static void writeToFile(File file, CharSequence content) {
        try {
            Files.write(content, file, UTF_8);
        } catch (IOException e) {
            final String msg = String.format("Cannot write the content to the file `%s`.", file);
            throw new IllegalStateException(msg, e);
        }
    }

    private static String readFileContent(File file) {
        try {
            return Files.toString(file, UTF_8);
        } catch (IOException e) {
            final String msg = String.format("Cannot read the content of the file `%s`.", file);
            throw new IllegalStateException(msg, e);
        }
    }
}
