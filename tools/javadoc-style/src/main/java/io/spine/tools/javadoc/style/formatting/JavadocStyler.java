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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.flogger.FluentLogger;
import io.spine.tools.java.fs.FileName;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.util.Exceptions.newIllegalStateException;
import static java.lang.System.lineSeparator;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.delete;
import static java.nio.file.Files.move;
import static java.nio.file.Files.newBufferedReader;
import static java.nio.file.Files.newBufferedWriter;

/**
 * Improves the style of Javadoc code by applying the passed formatting actions.
 */
public final class JavadocStyler {

    private static final FluentLogger logger = FluentLogger.forEnclosingClass();
    private static final String TEMP_FILE_NAME = "temp_file_for_formatting.java";

    /** Formatting actions to perform. */
    private final ImmutableList<Formatting> actions;

    /**
     * Creates an instance with the passed formatting actions.
     */
    @VisibleForTesting
    JavadocStyler(Formatting... actions) {
        this.actions = ImmutableList.copyOf(actions);
    }

    private static JavadocStyler newStyler() {
        return new JavadocStyler(new BacktickedToCode(), new RemovePreTags());
    }

    /**
     * Improves the style for all Java files in the specified directory, including sub-directories.
     */
    public static void applyFormattingAt(Path directory) {
        checkNotNull(directory);
        if (!Files.exists(directory)) {
            logger.atWarning()
                  .log("Cannot perform formatting. The directory `%s` does not exist.", directory);
            return;
        }
        try {
            logger.atFine()
                  .log("Starting Javadocs formatting in `%s`.", directory);
            Files.walkFileTree(directory, new FormattingFileVisitor(newStyler()));
        } catch (IOException e) {
            throw newIllegalStateException(e, "Failed to format the sources in `%s`.", directory);
        }
    }

    /**
     * Formats the Javadocs in the file with the specified path.
     *
     * <p>If the file is not a {@code .java} source, does nothing.
     *
     * @param file the path to the file
     */
    void format(Path file) throws IOException {
        checkNotNull(file);
        if (!FileName.isJava(file)) {
            return;
        }
        Path tempFile = formatIntoTempFileFrom(file);
        delete(file);
        // Rename temp. file after the passed file.
        move(tempFile, tempFile.resolveSibling(file));
    }

    /**
     * Formats the content of the passed file, creating a temp file returned by this method.
     */
    private Path formatIntoTempFileFrom(Path file) throws IOException {
        Path folder = file.getParent();
        Path tempFile = folder.resolve(TEMP_FILE_NAME);
        try (BufferedReader reader = newBufferedReader(file, UTF_8);
             BufferedWriter writer = newBufferedWriter(tempFile, UTF_8)) {

            @Nullable String part = readNextPart(reader);
            while (part != null) {
                writer.write(part);
                writer.newLine();
                part = readNextPart(reader);
            }
        }
        return tempFile;
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
    private @Nullable String readNextPart(BufferedReader reader) throws IOException {
        String firstLine = reader.readLine();
        if (firstLine == null) {
            return null;
        }
        if (!isJavadocBeginning(firstLine)) {
            return firstLine;
        }
        String javadoc = readJavadoc(firstLine, reader);
        String formattedJavadoc = applyActions(javadoc);
        return formattedJavadoc;
    }

    /**
     * Reads the Javadoc block from the passed reader.
     *
     * @param firstLine
     *          the first line of the Javadoc, already obtained from the reader
     * @param reader
     *          the reader with the continuation of the Javadoc text
     * @return the Javadoc text
     * @throws IOException
     *          if the passed reader fails during the reading operations
     */
    private static String readJavadoc(String firstLine, BufferedReader reader)
            throws IOException {
        StringBuilder javadoc = new StringBuilder();
        String currentLine = firstLine;
        while (!containsJavadocEnding(currentLine)) {
            javadoc.append(currentLine)
                   .append(lineSeparator());
            currentLine = reader.readLine();
        }
        javadoc.append(currentLine);
        return javadoc.toString();
    }

    private static boolean isJavadocBeginning(String line) {
        return line.contains("/**");
    }

    private static boolean containsJavadocEnding(String line) {
        return line.contains("*/");
    }

    /**
     * Applies formatting actions to the passed Javadoc text.
     */
    private String applyActions(String text) {
        String currentState = text;
        for (Formatting formatting : actions) {
            currentState = formatting.apply(currentState);
        }
        return currentState;
    }
}
