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
import com.google.common.flogger.FluentLogger;
import kotlin.io.FilesKt;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static java.nio.file.Files.copy;
import static java.nio.file.Files.createDirectory;
import static java.nio.file.Files.find;
import static java.nio.file.Files.isDirectory;
import static java.nio.file.Files.isRegularFile;

/**
 * Additional utilities for working with files.
 */
public final class Files2 {

    private static final FluentLogger logger = FluentLogger.forEnclosingClass();

    /** Prevents instantiation of this utility class. */
    private Files2() {
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
        copyDir(dir, target, path -> true);
    }


    /**
     * Copies the directory and its contents matching the passed predicate into another directory.
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
     * @param matching
     *         the predicate accepting the copied content
     */
    public static void copyDir(Path dir, Path target, Predicate<Path> matching) throws IOException {
        checkIsDirectory(dir);
        checkIsDirectory(target);

        Path oldParent = dir.getParent();
        ImmutableList<Path> paths = contentOf(dir, matching);
        for (Path path : paths) {
            Path relative = oldParent.relativize(path);
            Path newPath = target.resolve(relative);
            if (isDirectory(path)) {
                createDirectory(newPath);
            } else if (isRegularFile(path)) {
                copy(path, newPath);
            }
        }
    }

    private static void checkIsDirectory(Path dir) {
        checkNotNull(dir);
        checkArgument(isDirectory(dir), "The path `%s` is not a directory", dir);
    }

    /**
     * Obtains all sub-directories and files enclosed the passed directory that match
     * the passed predicate.
     */
    private static
    ImmutableList<Path> contentOf(Path dir, Predicate<Path> matching) throws IOException {
        ImmutableList<Path> paths;
        try (Stream<Path> found =
                     find(dir, Integer.MAX_VALUE, (path, attributes) -> matching.test(path))) {
            paths = found.collect(toImmutableList());
        }
        return paths;
    }

    /**
     * Normalizes and transforms the passed path to an absolute file reference.
     */
    public static File toAbsolute(String path) {
        checkNotNull(path);
        File file = new File(path);
        Path normalized = file.toPath().normalize();
        File result = normalized.toAbsolutePath().toFile();
        return result;
    }

    /**
     * Requests removal of the passed directory when the system shuts down.
     *
     * @implNote This method creates a new {@code Thread} for deleting the passed directory.
     *         That's why calling it should not be taken lightly. If your application creates
     *         several directories that need to be removed when JVM is terminated, consider
     *         gathering them under a common root passed to this method.
     * @see Runtime#addShutdownHook(Thread)
     */
    public static void deleteRecursivelyOnShutdownHook(Path directory) {
        checkNotNull(directory);
        Runtime runtime = Runtime.getRuntime();
        runtime.addShutdownHook(new Thread(() -> deleteRecursively(directory)));
    }

    private static void deleteRecursively(Path directory) {
        boolean success = FilesKt.deleteRecursively(directory.toFile());
        if (!success) {
            logger.atWarning()
                  .log("Unable to delete the directory `%s`.", directory);
        }
    }

    /**
     * Obtains the value of the {@code System} property for a temporary directory.
     */
    @SuppressWarnings("AccessOfSystemProperties")
    public static String systemTempDir() {
        return System.getProperty("java.io.tmpdir");
    }

}
