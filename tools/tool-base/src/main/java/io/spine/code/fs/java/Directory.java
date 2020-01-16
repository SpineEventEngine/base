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

package io.spine.code.fs.java;

import io.spine.code.AbstractDirectory;
import io.spine.code.SourceCodeDirectory;
import io.spine.code.java.PackageName;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A folder with Java source files.
 */
public final class Directory extends SourceCodeDirectory {

    private static final String ROOT_NAME = "java";

    private Directory(Path path) {
        super(path);
    }

    /**
     * Creates a new instance.
     */
    static Directory at(Path path) {
        checkNotNull(path);
        return new Directory(path);
    }

    /**
     * Creates an instance of the root directory named {@code "java"}.
     */
    public static Directory rootIn(AbstractDirectory parent) {
        checkNotNull(parent);
        Path path = parent.path()
                          .resolve(ROOT_NAME);
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
        String packagePath = packageName.value()
                                        .replace(PackageName.delimiterChar(), File.separatorChar);
        Path path = Paths.get(packagePath);
        return at(path);
    }

    /**
     * Obtains the source code file for the passed name.
     */
    public SourceFile resolve(FileName fileName) {
        Path filePath = path().resolve(fileName.value());
        SourceFile result = SourceFile.of(filePath);
        return result;
    }

    /**
     * Obtains the source code path for the passed file.
     */
    public Path resolve(Path file) {
        Path result = path().resolve(file);
        return result;
    }
}
