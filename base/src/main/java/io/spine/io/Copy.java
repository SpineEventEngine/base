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

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static io.spine.io.IoPreconditions.checkIsDirectory;
import static java.nio.file.Files.copy;
import static java.nio.file.Files.createDirectories;
import static java.nio.file.Files.exists;
import static java.nio.file.Files.find;
import static java.nio.file.Files.isDirectory;
import static java.nio.file.Files.isRegularFile;

/**
 * Utilities for copy operations.
 */
public final class Copy {

    /** Prevents instantiation of this utility class. */
    private Copy() {
    }

    /**
     * Copies a whole directory and its contents into another directory.
     *
     * <p>Both paths must point to existing directories.
     *
     * <p>The {@code dir} itself is copied as well. For example, if the {@code dir} path is
     * {@code /my/path/to/folder/foo} and the {@code target} path is {@code /my/other/folder}, as
     * a result of this operation, a {@code /my/other/folder/foo} directory will be created and all
     * the contents of the original {@code dir}, including nested directories, will be copied there.
     *
     * @param dir
     *         the directory to copy
     * @param target
     *         the new parent directory
     */
    public static void copyDir(Path dir, Path target) throws IOException {
        copyDir(dir, target, path -> true);
    }

    /**
     * Copies the directory and its contents matching the passed predicate into another directory.
     *
     * <p>Both paths must point to existing directories.
     *
     * <p>The {@code dir} itself is copied as well. For example, if the {@code dir} path is
     * {@code /my/path/to/folder/foo} and the {@code target} path is {@code /my/other/folder}, as
     * a result of this operation, a {@code /my/other/folder/foo} directory will be created and all
     * the contents of the original {@code dir}, including nested directories, will be copied there.
     *
     * @param dir
     *         the directory to copy
     * @param target
     *         the new parent directory
     * @param matching
     *         the predicate accepting the copied content
     */
    public static void copyDir(Path dir, Path target, Predicate<Path> matching) throws IOException {
        checkIsDirectory(dir);
        checkIsDirectory(target);
        doCopy(dir, target, matching, true);
    }

    /**
     * Copies the content of a directory into another directory.
     *
     * <p>Both paths must point to existing directories.
     *
     * <p>Files under the directory and all nested directories and files under them are copied
     * into the target directory. The directory itself is not copied.
     *
     * @param dir
     *         the directory content of which will be copied
     * @param target
     *         the new parent directory
     */
    public static void copyContent(Path dir, Path target) throws IOException {
        checkIsDirectory(dir);
        checkIsDirectory(target);
        doCopy(dir, target, path -> true, false);
    }

    /**
     * Copies the content of a directory matching the given predicate into another directory.
     *
     * <p>Both paths must point to existing directories.
     *
     * <p>Files under the directory and all nested directories and files under them are copied
     * into the target directory. The directory itself is not copied.
     *
     * @param dir
     *         the directory content of which will be copied
     * @param target
     *         the new parent directory
     * @param matching
     *         the predicate accepting the copied content
     */
    public static void copyContent(Path dir, Path target, Predicate<Path> matching)
            throws IOException {
        checkIsDirectory(dir);
        checkIsDirectory(target);
        doCopy(dir, target, matching, false);
    }

    private static void doCopy(Path dir,
                               Path target,
                               Predicate<Path> matching,
                               boolean withEnclosingDir) throws IOException {
        Path oldParent = withEnclosingDir
                         ? dir.getParent()
                         : dir;
        ImmutableList<Path> paths = contentOf(dir, matching);
        for (Path path : paths) {
            Path relative = oldParent.relativize(path);
            Path newPath = target.resolve(relative);
            if (isDirectory(path)) {
                if (!exists(newPath)) {
                    createDirectories(newPath);
                }
            } else if (isRegularFile(path)) {
                Path containingDir = newPath.getParent();
                if (!exists(containingDir)) {
                    createDirectories(containingDir);
                }
                copy(path, newPath);
            }
        }
    }

    /**
     * Obtains all subdirectories and files enclosed the passed directory that match
     * the passed predicate.
     */
    private static ImmutableList<Path> contentOf(Path dir, Predicate<Path> matching)
            throws IOException {
        BiPredicate<Path, BasicFileAttributes> predicate = (path, attrs) -> matching.test(path);
        try (Stream<Path> found = find(dir, Integer.MAX_VALUE, predicate)) {
            ImmutableList<Path> paths = found.collect(toImmutableList());
            return paths;
        }
    }
}
