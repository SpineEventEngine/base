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

package io.spine.tools.mc.js.fs;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import io.spine.tools.fs.ExternalModule;
import io.spine.tools.fs.FileWithImports;

import java.nio.file.Path;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * A JavaScript file present on a file system.
 */
public final class JsFile extends FileWithImports {

    @VisibleForTesting
    public static final String EXTENSION = ".js";

    /**
     * Creates a new instance.
     *
     * @param path
     *         the path to existing JavaScript file
     */
    public JsFile(Path path) {
        super(path);
        String fileName = path.toString();
        checkArgument(fileName.endsWith(EXTENSION),
                      "A JavaScript file is expected. Passed: `%s`.", fileName);
    }

    @Override
    protected boolean isImport(String line) {
        return ImportStatement.declaredIn(line);
    }

    @Override
    protected String resolveImport(String line,
                                   Path generatedRoot,
                                   ImmutableList<ExternalModule> modules) {
        ImportStatement importLine = new ImportStatement(this, line);
        ImportStatement resolved = importLine.resolve(generatedRoot, modules);
        String text = resolved.toString();
        return text;
    }
}
