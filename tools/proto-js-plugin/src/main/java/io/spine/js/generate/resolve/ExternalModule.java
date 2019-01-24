/*
 * Copyright 2019, TeamDev. All rights reserved.
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

package io.spine.js.generate.resolve;

import com.google.common.collect.ImmutableList;
import io.spine.code.js.ImportPath;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkState;
import static io.spine.util.Preconditions2.checkNotEmptyOrBlank;

/**
 * An external JavaScript module used in a project.
 *
 * <p>External means that it is provided by an artifact repository like NPM.
 */
public final class ExternalModule {

    private final String name;
    private final Collection<DirectoryPattern> directories;

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
        this.directories = ImmutableList.copyOf(directories);
    }

    /**
     * Obtains an import path, which references this module.
     *
     * @param importPath
     *         the relative import path
     * @return the import path of the file in this module
     * @throws IllegalStateException
     *         if the file is not provided by the module
     */
    ImportPath pathInModule(ImportPath importPath) {
        Optional<DirectoryPattern> directory = matchingDirectory(importPath);
        checkState(directory.isPresent());
        String directoryName = directory.get()
                                        .directoryName();
        String path = name + ImportPath.separator() + directoryName + ImportPath.separator() +
                importPath.fileName();
        return ImportPath.of(path);
    }

    /**
     * Checks if the module provides imported file.
     *
     * @param importPath
     *         the import to check
     * @return {@code true} if the module provides the file
     */
    boolean provides(ImportPath importPath) {
        boolean result = matchingDirectory(importPath).isPresent();
        return result;
    }

    private Optional<DirectoryPattern> matchingDirectory(ImportPath importPath) {
        String directory = importPath.directory();
        for (DirectoryPattern pattern : directories) {
            if (pattern.matches(directory)) {
                return Optional.of(pattern);
            }
        }
        return Optional.empty();
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
