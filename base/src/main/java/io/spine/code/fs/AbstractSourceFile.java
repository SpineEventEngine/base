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

package io.spine.code.fs;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.errorprone.annotations.OverridingMethodsMustInvokeSuper;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.io.IoPreconditions.checkExists;
import static io.spine.util.Exceptions.newIllegalStateException;
import static java.nio.file.Files.readAllLines;
import static java.nio.file.Files.write;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;

/**
 * Abstract base for source code files.
 */
public abstract class AbstractSourceFile extends FsObject {

    private @Nullable ImmutableList<String> lines;

    /**
     * Creates a file at the given path.
     */
    protected AbstractSourceFile(Path path) {
        super(path);
    }

    /**
     * Loads the content of the file from the file system.
     */
    @OverridingMethodsMustInvokeSuper
    protected void load() {
        Path path = path();
        checkExists(path.toFile());
        try {
            List<String> loaded = readAllLines(path);
            lines = ImmutableList.copyOf(loaded);
        } catch (IOException e) {
            throw newIllegalStateException(e, "Unable to read the file `%s`.", path);
        }
    }

    /**
     * Rewrites this file.
     */
    @OverridingMethodsMustInvokeSuper
    public void store() {
        Path path = path();
        try {
            write(path, lines(), Charsets.UTF_8, TRUNCATE_EXISTING);
        } catch (IOException e) {
            throw newIllegalStateException(e, "Unable to write to the file `%s`.", path);
        }
    }

    /**
     * Obtains the lines of the {@linkplain #load() loaded} file.
     *
     * @return the content of the file or an empty list, if the file was not loaded
     */
    protected final ImmutableList<String> lines() {
        return this.lines == null
               ? ImmutableList.of()
               : this.lines;
    }

    protected final void update(ImmutableList<String> newLines) {
        checkNotNull(newLines);
        this.lines = newLines;
    }
}
