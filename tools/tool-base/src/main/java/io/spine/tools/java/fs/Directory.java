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

package io.spine.tools.java.fs;

import io.spine.code.fs.SourceCodeDirectory;
import io.spine.tools.code.fs.ArtifactSources;
import io.spine.code.java.PackageName;
import io.spine.code.java.SimpleClassName;

import java.nio.file.Path;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.tools.code.fs.DirectoryName.java;

/**
 * A folder with Java source files.
 */
public final class Directory extends SourceCodeDirectory {

    private Directory(Path path) {
        super(path);
    }

    /**
     * Creates a new instance.
     */
    private static Directory at(Path path) {
        checkNotNull(path);
        return new Directory(path);
    }

    /**
     * Creates an instance of the root directory named {@code "java"}.
     */
    public static Directory rootIn(ArtifactSources parent) {
        checkNotNull(parent);
        Path path = java.under(parent.path());
        return at(path);
    }

    /**
     * Obtains the {@code Directory} which corresponds to the given Java package.
     *
     * <p>The path of the directory is relative and starts at
     *
     * @param packageName
     *         the name of the package
     * @return the directory
     */
    public static Directory of(PackageName packageName) {
        checkNotNull(packageName);
        Path path = packageName.toPath();
        return at(path);
    }

    /**
     * Obtains the source code file for the passed name.
     */
    public SourceFile resolve(FileName fn) {
        Path filePath = path().resolve(fn.value());
        return SourceFile.of(filePath);
    }

    /**
     * Obtains the source code file for the type with the passed name.
     */
    public SourceFile resolve(String typeName) {
        checkNotNull(typeName);
        return resolve(FileName.forType(typeName));
    }

    /**
     * Obtains the source file for the passed type.
     */
    public SourceFile resolve(SimpleClassName typeName) {
        checkNotNull(typeName);
        return resolve(typeName.value());
    }

    /**
     * Obtains the source code path for the passed file.
     */
    public Path resolve(Path file) {
        Path result = path().resolve(file);
        return result;
    }
}
