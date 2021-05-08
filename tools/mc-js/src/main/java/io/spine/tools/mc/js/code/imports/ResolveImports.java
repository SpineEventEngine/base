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

package io.spine.tools.mc.js.code.imports;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableSet;
import com.google.protobuf.Descriptors.FileDescriptor;
import io.spine.code.proto.FileSet;
import io.spine.logging.Logging;
import io.spine.tools.fs.ExternalModule;
import io.spine.tools.js.fs.Directory;
import io.spine.tools.js.fs.FileName;
import io.spine.tools.mc.js.code.task.GenerationTask;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Set;

/**
 * A task to resolve imports in generated files.
 *
 * <p>Supports only {@code CommonJs} imports.
 *
 * <p>The task should be performed last among {@linkplain GenerationTask generation tasks}
 * to ensure that imports won't be modified after execution of this task.
 */
public final class ResolveImports extends GenerationTask implements Logging {

    private final Set<ExternalModule> modules;

    public ResolveImports(Directory generatedRoot, Collection<ExternalModule> modules) {
        super(generatedRoot);
        this.modules = ImmutableSet.copyOf(modules);
    }

    @Override
    protected void generateFor(FileSet fileSet) {
        for (FileDescriptor file : fileSet.files()) {
            FileName fileName = FileName.from(file);
            _debug().log("Resolving imports in the file `%s`.", fileName);
            Path filePath = generatedRoot().resolve(fileName);
            resolveInFile(filePath);
        }
    }

    @VisibleForTesting
    void resolveInFile(Path filePath) {
        JsFile file = new JsFile(filePath);
        file.resolveImports(modules);
    }
}
