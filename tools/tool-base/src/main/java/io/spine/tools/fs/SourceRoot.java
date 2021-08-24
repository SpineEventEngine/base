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

package io.spine.tools.fs;

import io.spine.code.fs.SourceCodeDirectory;

import static io.spine.tools.fs.DirectoryName.main;
import static io.spine.tools.fs.DirectoryName.resources;
import static io.spine.tools.fs.DirectoryName.test;

/**
 * A root source code directory in a project or a module.
 */
public class SourceRoot extends SourceDir {

    protected SourceRoot(DefaultPaths parent, String name) {
        super(parent, name);
    }

    protected SourceRoot(DefaultPaths paths, DirectoryName name) {
        super(paths, name);
    }

    /**
     * The directory with the production code.
     */
    @SuppressWarnings("ConfusingMainMethod") // named after the src/main directory.
    public SourceDir main() {
        return new SourceDir(this, main.value());
    }

    /**
     * The directory with the code of tests.
     */
    public SourceDir test() {
        return new SourceDir(this, test.value());
    }

    /**
     * The directory for main resources.
     */
    public SourceCodeDirectory mainResources() {
        return new SourceDir(main(), resources.value());
    }

    /**
     * The directory for test resources.
     */
    public SourceCodeDirectory testResources() {
        return new SourceDir(test(), resources.value());
    }
}
