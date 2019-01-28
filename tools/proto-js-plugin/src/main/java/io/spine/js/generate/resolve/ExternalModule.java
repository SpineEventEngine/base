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

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import io.spine.code.js.DirectoryReference;
import io.spine.code.js.FileReference;

import java.util.List;
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
    private final List<DirectoryPattern> directories;

    /**
     * Creates a new instance.
     *
     * @param name
     *         the name of the module
     * @param directories
     *         patterns of directories provided by the module
     */
    public ExternalModule(String name, List<DirectoryPattern> directories) {
        this.name = checkNotEmptyOrBlank(name);
        this.directories = ImmutableList.copyOf(directories);
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
    FileReference fileInModule(FileReference fileReference) {
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
    boolean provides(FileReference fileReference) {
        boolean result = matchingDirectory(fileReference).isPresent();
        return result;
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
