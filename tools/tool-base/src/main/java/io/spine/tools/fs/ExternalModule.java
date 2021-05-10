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

package io.spine.tools.fs;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Ordering;
import com.google.errorprone.annotations.Immutable;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkState;
import static io.spine.tools.fs.FileReference.joiner;
import static io.spine.util.Preconditions2.checkNotEmptyOrBlank;

/**
 * An external library module used in a project.
 *
 * <p>An external module is typically provided by a package manager, such as NPM or Pub.
 */
@Immutable
public final class ExternalModule {

    private final String name;
    private final ImmutableList<DirectoryPattern> directories;

    /**
     * Creates a new instance.
     *
     * @param name
     *         the name of the module
     * @param directories
     *         patterns of directories provided by the module
     */
    public ExternalModule(String name, Collection<DirectoryPattern> directories) {
        this.name = checkNotEmptyOrBlank(name);
        this.directories = Ordering.natural().immutableSortedCopy(directories);
    }

    /**
     * Obtains the file reference within this module.
     *
     * @param file
     *         the relative file reference
     * @return the file reference in this module
     * @throws IllegalStateException
     *         if the file is not provided by the module
     */
    public FileReference fileInModule(FileReference file) {
        Optional<DirectoryPattern> matchingDirectory = matchingDirectory(file);
        checkState(matchingDirectory.isPresent());
        DirectoryReference directory =
                matchingDirectory.get()
                                 .transform(file.directory());
        String fileName = file.fileName();
        String path = joiner().join(name, directory, fileName);
        return FileReference.of(path);
    }

    /**
     * Checks if the module provides the referenced file.
     *
     * @param file
     *         the file to check
     * @return {@code true} if the module provides the file
     */
    public boolean provides(FileReference file) {
        boolean result = matchingDirectory(file).isPresent();
        return result;
    }

    /**
     * Obtains the name of the module.
     */
    public String name() {
        return name;
    }

    private Optional<DirectoryPattern> matchingDirectory(FileReference file) {
        DirectoryReference directory = file.directory();
        for (DirectoryPattern pattern : directories) {
            if (pattern.matches(directory)) {
                return Optional.of(pattern);
            }
        }
        return Optional.empty();
    }

    /**
     * <a href="https://github.com/SpineEventEngine/web">The Spine Web</a> module.
     */
    public static ExternalModule spineWeb() {
        @SuppressWarnings("DuplicateStringLiteralInspection") // also in test code.
        ImmutableList<DirectoryPattern> patterns = DirectoryPattern.listOf(
                // Directories with handcrafted JS files.
                "client/parser",
                // Directories with standard Protobuf files.
                "proto/google/protobuf/*",
                // Directories with Spine Protobuf files.
                "proto/spine/base/*",
                "proto/spine/change/*",
                "proto/spine/client/*",
                "proto/spine/core/*",
                "proto/spine/net/*",
                "proto/spine/people/*",
                "proto/spine/time/*",
                "proto/spine/ui/*",
                "proto/spine/validate/*",
                "proto/spine/web/*",
                "proto/spine"
        );
        return new ExternalModule("spine-web", patterns);
    }

    /**
     * <a href="https://github.com/SpineEventEngine/users">The Spine Users</a> module.
     */
    public static ExternalModule spineUsers() {
        return new ExternalModule("spine-users", DirectoryPattern.listOf("spine/users/*"));
    }

    /**
     * All the modules in {@link #spineWeb()} and {@link #spineUsers()}.
     */
    public static ImmutableList<ExternalModule> predefinedModules() {
        return ImmutableList.of(spineWeb(), spineUsers());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ExternalModule)) {
            return false;
        }
        ExternalModule module = (ExternalModule) o;
        return name.equals(module.name) &&
                directories.equals(module.directories);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, directories);
    }
}
