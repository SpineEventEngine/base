/*
 * Copyright 2018, TeamDev Ltd. All rights reserved.
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

package io.spine.tools.java;

import io.spine.tools.AbstractDirectory;
import io.spine.tools.SourceCodeDirectory;

import java.nio.file.Path;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A folder with Java source files.
 *
 * @author Alexander Yevsyukov
 */
public final class Directory extends SourceCodeDirectory {

    public static final String ROOT_NAME = "java";

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
        final Path path = parent.getPath()
                                .resolve(ROOT_NAME);
        return at(path);
    }

    /**
     * Obtains the source code file for the passed name.
     */
    public SourceFile resolve(FileName fileName) {
        final Path filePath = getPath().resolve(fileName.value());
        final SourceFile result = SourceFile.of(filePath);
        return result;
    }

    /**
     * Obtains the source code path for the passed file.
     */
    public Path resolve(Path file) {
        final Path result = getPath().resolve(file);
        return result;
    }
}
