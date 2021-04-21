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

package io.spine.code;

import java.nio.file.Path;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A directory with source code files.
 */
public abstract class SourceCodeDirectory extends AbstractDirectory {

    /**
     * Creates a new instance at the given path.
     */
    protected SourceCodeDirectory(Path path) {
        super(path);
    }

    /**
     *
     * Creates a sub-directory under the given parent.
     * @param parent
     *          the parent directory for the new one
     * @param name
     *          the short name of the new directory
     */
    protected SourceCodeDirectory(AbstractDirectory parent, String name) {
        super(parent, name);
    }

    public Path resolve(SourceCodeDirectory dir) {
        checkNotNull(dir);
        Path result = path().resolve(dir.path());
        return result;
    }

    public Path resolve(AbstractSourceFile file) {
        checkNotNull(file);
        Path result = path().resolve(file.path());
        return result;
    }
}
