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
import io.spine.tools.fs.DefaultPaths;
import io.spine.tools.fs.SourceDir;

import static io.spine.tools.fs.DirectoryName.generated;
import static io.spine.tools.fs.DirectoryName.grpc;
import static io.spine.tools.fs.DirectoryName.resources;
import static io.spine.tools.fs.DirectoryName.spine;

/**
 * A root directory for the generated code.
 */
public final class GeneratedRoot extends JavaCodeRoot {

    private static final String SPINE_DIR = spine.value();
    private static final String GRPC_DIR = grpc.value();
    private static final String RESOURCES_DIR = resources.value();

    GeneratedRoot(DefaultPaths parent) {
        super(parent, generated.value());
    }

    /**
     * Spine-generated source code directory.
     */
    public SourceCodeDirectory mainSpine() {
        return new SourceDir(getMain(), SPINE_DIR);
    }

    /**
     * Spine-generated source code directory for tests.
     */
    public SourceCodeDirectory testSpine() {
        return new SourceDir(getTest(), SPINE_DIR);
    }

    /**
     * The directory for the source code generated by gRPC.
     */
    public SourceCodeDirectory mainGrpc() {
        return new SourceDir(getMain(), GRPC_DIR);
    }

    /**
     * The directory for the test source code generated by gRPC.
     */
    public SourceCodeDirectory testGrpc() {
        return new SourceDir(getTest(), GRPC_DIR);
    }

    /**
     * The directory for generated resources.
     */
    public SourceCodeDirectory mainResources() {
        return new SourceDir(getMain(), RESOURCES_DIR);
    }

    /**
     * The directory for generated test resources.
     */
    public SourceCodeDirectory testResources() {
        return new SourceDir(getTest(), RESOURCES_DIR);
    }
}
