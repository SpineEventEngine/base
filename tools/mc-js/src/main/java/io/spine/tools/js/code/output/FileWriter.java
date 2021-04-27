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

package io.spine.tools.js.code.output;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.protobuf.Descriptors.FileDescriptor;
import io.spine.tools.js.fs.Directory;
import io.spine.tools.js.fs.FileName;
import io.spine.tools.js.fs.LibraryFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;

/**
 * A writer of JavaScript code into a file.
 *
 * <p>The class wraps all {@link IOException}s which occur during its operations in the
 * {@link IllegalStateException}.
 */
public final class FileWriter {

    /**
     * The path of the file to write into.
     */
    private final Path path;

    private FileWriter(Path path) {
        this.path = path;
    }

    @VisibleForTesting
    static FileWriter createFor(Directory directory, FileName fileName) {
        Path filePath = directory.resolve(fileName);
        return new FileWriter(filePath);
    }

    /**
     * Creates a new instance which will operate on the specified library file located in
     * the specified directory.
     */
    public static FileWriter createFor(Directory directory, LibraryFile libraryFile) {
        return createFor(directory, libraryFile.fileName());
    }

    /**
     * Creates a new instance which will operate on the file pointed by the file descriptor
     * and located in the specified directory.
     */
    public static FileWriter createFor(Directory directory, FileDescriptor file) {
        FileName fileName = FileName.from(file);
        return createFor(directory, fileName);
    }

    /**
     * Writes the given output to the file.
     *
     * <p>Overwrites the previous file content.
     *
     * @param jsOutput
     *         the {@code JsOutput} to write
     * @throws IllegalStateException
     *         if something went wrong when writing to file
     */
    public void write(CodeLines jsOutput) {
        checkNotNull(jsOutput);
        try {
            Files.write(path,
                        ImmutableList.of(jsOutput.toString()),
                        Charsets.UTF_8,
                        CREATE, TRUNCATE_EXISTING);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Appends the given output to the end of the file.
     *
     * @param jsOutput
     *         the {@code JsOutput} to append
     * @throws IllegalStateException
     *         if something went wrong when writing to file
     */
    public void append(CodeLines jsOutput) {
        checkNotNull(jsOutput);
        try {
            ImmutableList<String> linesToAppend = ImmutableList.of(jsOutput.toString());
            Files.write(path, linesToAppend, Charsets.UTF_8, APPEND);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
