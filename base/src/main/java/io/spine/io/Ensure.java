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

package io.spine.io;

import com.google.errorprone.annotations.CanIgnoreReturnValue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.io.Files.createParentDirs;
import static io.spine.io.IoPreconditions.checkNotDirectory;
import static io.spine.util.Exceptions.newIllegalStateException;
import static java.nio.file.Files.createDirectories;
import static java.nio.file.Files.exists;
import static java.nio.file.Files.isDirectory;

/**
 * Utilities that arrange conditions required for further I/O operations.
 */
public final class Ensure {

    /** Prevents instantiation of this utility class. */
    private Ensure() {
    }

    /**
     * Ensures that the given file exists.
     *
     * <p>Performs no action if the given file {@linkplain File#exists() already exists}.
     *
     * <p>If the given file does not exist, it is created along with its parent directories,
     * if required.
     *
     * <p>If the passed {@code File} points to the existing directory, an
     * {@link IllegalArgumentException} is thrown.
     *
     * <p>In case of any I/O issues, the respective exceptions are rethrown as
     * {@link IllegalStateException}.
     *
     * @param file
     *         a file to check
     * @return {@code true} if the file did not exist and was successfully created;
     *         {@code false} if the file already existed
     * @throws IllegalArgumentException
     *         if the given file is a directory
     * @throws IllegalStateException
     *         in case of any I/O exceptions that may occur while creating the parent directories
     *         for the file, or while creating the file itself
     */
    @CanIgnoreReturnValue
    public static boolean ensureFile(File file) {
        checkNotNull(file);
        checkNotDirectory(file);
        try {
            if (!file.exists()) {
                createParentDirs(file);
                boolean result = file.createNewFile();
                return result;
            }
            return false;
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Ensures that the file represented by the specified {@code Path exists}.
     *
     * <p>If the file already exists, no action is performed.
     *
     * <p>If the file does not exist, it is created along with its parent if required.
     *
     * <p>If the specified path represents an existing directory, an
     * {@link IllegalArgumentException} is thrown.
     *
     * <p>If any I/O errors occur, an {@link IllegalStateException} is thrown.
     *
     * @param pathToFile
     *         the path to the file to check
     * @return {@code true} if and only if the file represented by the specified path did not exist
     *         and was successfully created
     * @throws IllegalArgumentException
     *         if the given path represents a directory
     * @throws IllegalStateException
     *         if any I/O errors occur
     */
    @CanIgnoreReturnValue
    public static boolean ensureFile(Path pathToFile) {
        checkNotNull(pathToFile);
        boolean result = ensureFile(pathToFile.toFile());
        return result;
    }

    /**
     * Ensures that the specified directory exists, creating it, if it was not done
     * prior to this call.
     *
     * <p>If the passed path exists, but refers to a file, the method
     * throws {@link IllegalStateException}.
     *
     * @return the passed instance
     * @throws IllegalStateException
     *          if the passed path represents existing file, instead of a directory
     */
    @CanIgnoreReturnValue
    public static Path ensureDirectory(Path directory) {
        if (!exists(directory)) {
            try {
                createDirectories(directory);
            } catch (IOException e) {
                throw newIllegalStateException(e, "Unable to create `%s`.", directory);
            }
        } else {
            checkState(
                    isDirectory(directory),
                    "The path `%s` exists, but it is not a directory.", directory
            );
        }
        return directory;
    }
}
