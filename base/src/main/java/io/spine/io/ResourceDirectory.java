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

import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Predicate;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static io.spine.io.Copy.copyContent;
import static io.spine.io.Copy.copyDir;
import static io.spine.util.Preconditions2.checkNotEmptyOrBlank;

/**
 * A directory with resources in the classpath.
 *
 * @see #get(String, ClassLoader)
 */
public final class ResourceDirectory extends ResourceObject {

    private ResourceDirectory(String path, ClassLoader classLoader) {
        super(path, classLoader);
    }

    /**
     * Creates a new reference to a resource directory at the context of the given class loader.
     *
     * @param path
     *         the path to the resource directory
     * @param classLoader
     *         the class loader relative to which the resource directory is referenced
     */
    public static ResourceDirectory get(String path, ClassLoader classLoader) {
        checkNotNull(path);
        checkNotNull(classLoader);
        checkNotEmptyOrBlank(path);
        return new ResourceDirectory(path, classLoader);
    }

    /**
     * Obtains the path to this directory under resources.
     */
    public Path toPath() {
        @Nullable URL url = locate();
        checkState(url != null, "Unable to locate resource directory: `%s`.", path());
        Path result = Paths.get(url.getPath());
        return result;
    }

    /**
     * Copies the content of the directory to the target directory.
     *
     * @param target
     *         the path to existing directory on the file system
     * @see Copy#copyContent(Path, Path)
     */
    public void copyContentTo(Path target) throws IOException {
        checkTarget(target);
        copyContentTo(target, path -> true);
    }

    /**
     * Copies the content of the directory matching the condition to the target directory.
     *
     * @param matching
     *         the condition for accepting the copied content
     * @param target
     *         the path to existing directory on the file system
     * @see Copy#copyContent(Path, Path, Predicate)
     */
    public void copyContentTo(Path target, Predicate<Path> matching) throws IOException {
        checkTarget(target);
        checkNotNull(matching);
        Path from = toPath();
        copyContent(from, target, matching);
    }

    /**
     * Copies this directory to the target directory.
     *
     * @param target
     *         the path to existing directory on the file system
     * @see Copy#copyDir(Path, Path)
     */
    public void copyTo(Path target) throws IOException {
        checkTarget(target);
        copyTo(target, path -> true);
    }

    /**
     * Copies this directory and its content matching the condition to another directory.
     *
     * @param target
     *         the path to existing directory on the file system
     * @see Copy#copyDir(Path, Path, Predicate)
     */
    public void copyTo(Path target, Predicate<Path> matching) throws IOException {
        checkTarget(target);
        checkNotNull(matching);
        Path from = toPath();
        copyDir(from, target, matching);
    }

    private static void checkTarget(Path target) {
        checkNotNull(target);
        checkArgument(Files.exists(target), "The target directory does not exist: `%s`.", target);
    }

    @Override
    public int hashCode() {
        return path().hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof ResourceDirectory) {
            return super.equals(o);
        }
        return false;
    }
}
