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

package io.spine.tools.dart.fs;

import com.google.common.collect.ImmutableList;
import io.spine.tools.fs.ExternalModule;
import io.spine.tools.fs.FileWithImports;

import java.nio.file.Path;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A Dart source file.
 */
public final class DartFile extends FileWithImports {

    private DartFile(Path path) {
        super(path);
    }

    /**
     * Reads the file from the local file system.
     */
    public static DartFile read(Path path) {
        checkNotNull(path);
        DartFile file = new DartFile(path);
        file.load();
        return file;
    }

    @Override
    protected boolean isImport(String line) {
        return ImportStatement.isDeclaredIn(line);
    }

    @Override
    protected String resolveImport(String line,
                                   Path libPath,
                                   ImmutableList<ExternalModule> modules) {
        ImportStatement statement = new ImportStatement(this, line);
        ImportStatement resolved = statement.resolve(libPath, modules);
        return resolved.text();
    }
}
