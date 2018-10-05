/*
 * Copyright 2018, TeamDev. All rights reserved.
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

package io.spine.js.generate;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.protobuf.Descriptors.FileDescriptor;
import io.spine.code.js.Directory;
import io.spine.code.js.FileName;
import io.spine.code.js.LibraryFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;

/**
 * A tool for writing into JavaScript source files.
 *
 * <p>The class wraps all {@link IOException}s which occur during its operations in the
 * {@link IllegalStateException}.
 */
@SuppressWarnings("WeakerAccess")
// The class belongs to the public API of the package although now usage is only package-local.
public final class JsFileWriter {

    private final Path filePath;

    private JsFileWriter(Path filePath) {
        this.filePath = filePath;
    }

    @VisibleForTesting
    static JsFileWriter createFor(Directory directory, FileName fileName) {
        Path filePath = directory.resolve(fileName);
        return new JsFileWriter(filePath);
    }

    /**
     * Creates a {@code JsFileWriter} which will operate on the specified library file located in
     * the specified directory.
     */
    public static JsFileWriter createFor(Directory directory, LibraryFile libraryFile) {
        return createFor(directory, libraryFile.fileName());
    }

    /**
     * Creates a {@code JsFileWriter} which will operate on the file pointed by the file descriptor
     * and located in the specified directory.
     */
    public static JsFileWriter createFor(Directory directory, FileDescriptor file) {
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
    public void write(JsOutput jsOutput) {
        checkNotNull(jsOutput);
        try {
            Files.write(filePath,
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
    public void append(JsOutput jsOutput) {
        checkNotNull(jsOutput);
        try {
            Files.write(filePath, ImmutableList.of(jsOutput.toString()), Charsets.UTF_8, APPEND);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
