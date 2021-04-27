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

package io.spine.tools.js.code.given;

import io.spine.tools.js.fs.Directory;
import io.spine.code.proto.FileSet;
import io.spine.tools.js.code.GenerationTask;

import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A test implementation of {@link io.spine.tools.js.generate.GenerationTask}.
 */
public class TestGenerationTask extends GenerationTask {

    private boolean sourcesProcessed = false;
    private boolean filesFiltered = false;
    @Nullable
    private FileSet processedFileSet;

    public TestGenerationTask(Directory generatedRoot) {
        super(generatedRoot);
    }

    @Override
    protected void generateFor(FileSet fileSet) {
        sourcesProcessed = true;
        processedFileSet = fileSet;
    }

    @Override
    protected FileSet filter(FileSet fileSet) {
        filesFiltered = true;
        return super.filter(fileSet);
    }

    public boolean areSourcesProcessed() {
        return sourcesProcessed;
    }

    public boolean areFilesFiltered() {
        return filesFiltered;
    }

    public FileSet processedFileSet() {
        return checkNotNull(processedFileSet);
    }
}
