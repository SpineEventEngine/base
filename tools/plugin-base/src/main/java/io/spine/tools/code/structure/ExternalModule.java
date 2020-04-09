/*
 * Copyright 2020, TeamDev. All rights reserved.
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

package io.spine.tools.code.structure;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import io.spine.code.fs.DirectoryReference;
import io.spine.code.fs.FileReference;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static com.google.common.base.Preconditions.checkState;
import static io.spine.util.Preconditions2.checkNotEmptyOrBlank;

/**
 * An external library module used in a project.
 *
 * <p>An external module is typically provided by a package manager, such as NPM or Pub.
 */
public final class ExternalModule {

    private final String name;
    private final Set<DirectoryPattern> directories;

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
        this.directories = ImmutableSet.copyOf(directories);
    }

    /**
     * Obtains the file reference within this module.
     *
     * @param fileReference
     *         the relative file reference
     * @return the file reference in this module
     * @throws IllegalStateException
     *         if the file is not provided by the module
     */
    public FileReference fileInModule(FileReference fileReference) {
        Optional<DirectoryPattern> matchingDirectory = matchingDirectory(fileReference);
        checkState(matchingDirectory.isPresent());
        DirectoryReference directory = matchingDirectory.get()
                                                        .transform(fileReference.directory());
        String fileName = fileReference.fileName();
        String path = Joiner.on(FileReference.separator())
                            .join(name, directory, fileName);
        return FileReference.of(path);
    }

    /**
     * Checks if the module provides the referenced file.
     *
     * @param fileReference
     *         the file to check
     * @return {@code true} if the module provides the file
     */
    public boolean provides(FileReference fileReference) {
        boolean result = matchingDirectory(fileReference).isPresent();
        return result;
    }

    /**
     * Obtains the name of the module.
     */
    public String name() {
        return name;
    }

    private Optional<DirectoryPattern> matchingDirectory(FileReference fileReference) {
        DirectoryReference directory = fileReference.directory();
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
        Set<DirectoryPattern> directories = ImmutableSet.of(
                // Directories with handcrafted JS files.
                DirectoryPattern.of("client/parser"),
                // Directories with standard Protobuf files.
                DirectoryPattern.of("proto/google/protobuf/*"),
                // Directories with Spine Protobuf files.
                DirectoryPattern.of("proto/spine/base/*"),
                DirectoryPattern.of("proto/spine/change/*"),
                DirectoryPattern.of("proto/spine/client/*"),
                DirectoryPattern.of("proto/spine/core/*"),
                DirectoryPattern.of("proto/spine/net/*"),
                DirectoryPattern.of("proto/spine/people/*"),
                DirectoryPattern.of("proto/spine/time/*"),
                DirectoryPattern.of("proto/spine/ui/*"),
                DirectoryPattern.of("proto/spine/validate/*"),
                DirectoryPattern.of("proto/spine/web/*"),
                DirectoryPattern.of("proto/spine")
        );
        return new ExternalModule("spine-web", directories);
    }

    /**
     * <a href="https://github.com/SpineEventEngine/users">The Spine Users</a> module.
     */
    public static ExternalModule spineUsers() {
        Set<DirectoryPattern> directories = ImmutableSet.of(
                DirectoryPattern.of("spine/users/*")
        );
        return new ExternalModule("spine-users", directories);
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
