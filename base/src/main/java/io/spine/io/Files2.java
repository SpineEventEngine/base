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

import com.google.common.collect.ImmutableList;
import com.google.errorprone.annotations.CanIgnoreReturnValue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.io.Files.createParentDirs;
import static io.spine.util.Exceptions.newIllegalArgumentException;
import static java.nio.file.Files.copy;
import static java.nio.file.Files.createDirectory;
import static java.nio.file.Files.isDirectory;
import static java.nio.file.Files.isRegularFile;

/**
 * Additional utilities for working with files.
 *
 * <p>These utilities are specific to enable Spine working with code files in code generation,
 * project structure analysis, and other tasks that are not covered by well-known file management
 * libraries.
 *
 * <p>For more file-related utilities, please see:
 * <ul>
 *     <li>{@link java.nio.file.Files Files} from NIO
 *     <li>{@link java.nio.file.Paths Paths} from NIO2
 *     <li>{@link com.google.common.io.Files Files from Guava}
 * </ul>
 */
public final class Files2 {

    static final String DOES_NOT_EXIST = "The file `%s` does not exist.";

    /** Prevents instantiation of this utility class. */
    private Files2() {
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
     *         in case of any I/O exceptions
     */
    @CanIgnoreReturnValue
    public static boolean ensureFile(File file) {
        checkNotNull(file);
        try {
            ensureNotFolder(file);
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

    private static void ensureNotFolder(File file) {
        if (file.exists() && file.isDirectory()) {
            throw newIllegalArgumentException("File expected, but a folder found %s",
                                              file.getAbsolutePath());
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
     * Verifies if a passed file exists and has non-zero size.
     */
    public static boolean existsNonEmpty(File file) {
        checkNotNull(file);
        if (!file.exists()) {
            return false;
        }
        boolean nonEmpty = file.length() > 0;
        return nonEmpty;
    }

    /**
     * Ensures that the passed file exists.
     *
     * @return the passed file if it exists
     * @throws IllegalStateException
     *         if the file is missing
     */
    @CanIgnoreReturnValue
    public static File checkExists(File file) throws IllegalStateException {
        checkNotNull(file);
        checkState(file.exists(), DOES_NOT_EXIST, file);
        return file;
    }

    /**
     * Ensures that the file with the passed path exists.
     *
     * @return the passed path if it exists
     * @throws IllegalArgumentException
     *         if the file does not exist
     */
    @CanIgnoreReturnValue
    public static Path checkExists(Path path) throws IllegalArgumentException {
        checkNotNull(path);
        File file = path.toFile();
        checkArgument(file.exists(), DOES_NOT_EXIST, file);
        return path;
    }

    /**
     * Copies a whole directory and its contents into another directory.
     *
     * <p>Both paths must point to an existing directory.
     *
     * <p>The {@code dir} itself is copied as well. For example, if the {@code dir} path is
     * {@code /my/path/to/folder/foo} and the {@code target} path is {@code /my/other/folder}, as
     * a result of this operation, a {@code /my/other/folder/foo} directory will be created and all
     * the contents of the original {@code dir}, including nested directories, will be copied there.
     *
     * @param dir
     *         the dir to copy
     * @param target
     *         the new parent directory
     */
    public static void copyDir(Path dir, Path target) throws IOException {
        checkNotNull(dir);
        checkNotNull(target);
        checkArgument(isDirectory(dir));
        checkArgument(isDirectory(target));

        Path oldParent = dir.getParent();
        ImmutableList<Path> files;
        try (Stream<Path> paths = Files.walk(dir)) {
            files = paths.collect(toImmutableList());
        }
        for (Path file : files) {
            Path relative = oldParent.relativize(file);
            Path newPath = target.resolve(relative);
            if (isDirectory(file)) {
                createDirectory(newPath);
            } else if (isRegularFile(file)) {
                copy(file, newPath);
            }
        }
    }
}
