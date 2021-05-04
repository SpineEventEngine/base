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

package io.spine.tools.protodoc;

import com.google.common.collect.ImmutableList;
import io.spine.code.fs.java.FileName;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static java.lang.System.lineSeparator;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.newBufferedReader;
import static java.nio.file.Files.newBufferedWriter;

/**
 * A formatter for Javadocs.
 *
 * <p>The formatter executes {@linkplain FormattingAction formatting actions}
 * for the Javadoc lines in a source file.
 */
final class JavadocFormatter {

    private static final String TEMP_FILE_NAME = "temp_file_for_formatting.java";

    /**
     * The formatting actions to perform.
     */
    private final ImmutableList<FormattingAction> actions;

    JavadocFormatter(ImmutableList<FormattingAction> actions) {
        this.actions = actions;
    }

    /**
     * Formats the Javadocs in the file with the specified path.
     *
     * <p>If the file is not a {@code .java} source, does noting.
     *
     * @param path the path to the file
     */
    void format(Path path) throws IOException {
        if (!FileName.isJava(path)) {
            return;
        }

        Path folder = path.getParent();
        Path tempPath = folder.resolve(TEMP_FILE_NAME);

        try (BufferedReader reader = newBufferedReader(path, UTF_8);
             BufferedWriter writer = newBufferedWriter(tempPath, UTF_8)) {

            Optional<String> resultPart = getNextPart(reader);
            while (resultPart.isPresent()) {
                writer.write(resultPart.get());
                writer.newLine();
                resultPart = getNextPart(reader);
            }
        }

        Files.delete(path);
        Files.move(tempPath, tempPath.resolveSibling(path));
    }

    /**
     * Obtains the next part, that should be written to the resulting file.
     *
     * <p>If this part is a Javadoc, then the reformatted Javadoc will be returned.
     *
     * @param reader the reader for the file
     * @return the {@code Optional} of the next part
     *         or {@code Optional.empty()} if if the end of the stream has been reached
     * @throws IOException if an I/O error occurred during reading
     */
    private Optional<String> getNextPart(BufferedReader reader) throws IOException {
        String firstLine = reader.readLine();
        if (firstLine == null) {
            return Optional.empty();
        }

        if (!isJavadocBeginning(firstLine)) {
            return Optional.of(firstLine);
        }

        String javadoc = getJavadoc(firstLine, reader);
        String formattedJavadoc = formatText(javadoc);
        return Optional.of(formattedJavadoc);
    }

    private static String getJavadoc(String firstLine,
                                     BufferedReader reader) throws IOException {
        StringBuilder javadoc = new StringBuilder();

        String currentLine = firstLine;
        while (!containsJavadocEnding(currentLine)) {
            javadoc.append(currentLine)
                   .append(lineSeparator());
            currentLine = reader.readLine();
        }
        return javadoc.append(currentLine)
                      .toString();
    }

    /**
     * Obtains formatted representation of the specified text.
     *
     * @param text the text to format
     * @return the formatted representation
     */
    private String formatText(String text) {
        String currentState = text;
        for (FormattingAction formatting : actions) {
            currentState = formatting.execute(currentState);
        }
        return currentState;
    }

    private static boolean isJavadocBeginning(String line) {
        return line.trim()
                   .contains("/**");
    }

    private static boolean containsJavadocEnding(String line) {
        return line.contains("*/");
    }
}
