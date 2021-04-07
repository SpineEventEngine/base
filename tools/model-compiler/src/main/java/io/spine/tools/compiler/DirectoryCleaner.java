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

package io.spine.tools.compiler;

import com.google.common.collect.ImmutableList;
import io.spine.logging.Logging;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static io.spine.util.Exceptions.newIllegalStateException;

/**
 * Custom {@linkplain java.nio.file.FileVisitor FileVisitor} which recursively deletes
 * the contents of the walked folder.
 */
@SuppressWarnings("RefusedBequest")
// As we define a completely different behavior for the visitor methods.
public final class DirectoryCleaner extends SimpleFileVisitor<Path> implements Logging {

    private final ImmutableList<File> dirs;

    private DirectoryCleaner(ImmutableList<File> dirs) {
        super();
        this.dirs = dirs;
    }

    private DirectoryCleaner(Path path) {
        this(ImmutableList.of(path.toFile()));
    }

    private DirectoryCleaner(List<String> dirs) {
        this(dirs.stream()
                 .map(File::new)
                 .collect(toImmutableList()));
    }

    private void run() {
        for (File dir : dirs) {
            if (dir.exists()) {
                if (dir.isDirectory()) {
                    delete(dir.toPath());
                } else {
                    String msg = "Trying to delete `%s` which is not a directory.";
                    _warn().log(msg, dir.getAbsolutePath());
                }
            }
        }
    }

    /**
     * Deletes directories with the passed names.
     */
    public static void deleteDirs(List<String> dirs) {
        checkNotNull(dirs);
        checkArgument(!dirs.isEmpty(), "The list of directories to remove cannot be empty.");
        DirectoryCleaner cleaner = new DirectoryCleaner(dirs);
        cleaner.run();
    }

    private void delete(Path path) {
        try {
            FileVisitor<Path> visitor = new DirectoryCleaner(path);
            _debug().log("Starting to delete the files recursively in `%s`.", path);
            Files.walkFileTree(path, visitor);
        } catch (IOException e) {
            throw newIllegalStateException(
                    e, "Failed to delete the folder with its contents: `%s`.", path
            );
        }
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        logDeletionOf(file);
        Files.delete(file);
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) {
        _error().withCause(exc).log("Unable to delete `%s`.", file);
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult postVisitDirectory(Path dir, @Nullable IOException e)
            throws IOException {
        if (e == null) {
            logDeletionOf(dir);
            Files.delete(dir);
            return FileVisitResult.CONTINUE;
        } else {
            throw e;
        }
    }

    private void logDeletionOf(Path file) {
        _debug().log("Deleting file `%s`.", file);
    }
}
