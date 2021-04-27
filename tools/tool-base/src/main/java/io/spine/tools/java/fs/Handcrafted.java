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

import io.spine.tools.code.fs.DefaultPaths;

import static io.spine.tools.code.fs.DirectoryName.src;

/**
 * A root source code directory for manually written code.
 *
 * <p>Adds a root directory for the proto code in addition to those exposed
 * by {@code DefaultProject.SourceRoot}.
 */
public class Handcrafted extends JavaSrc {

    Handcrafted(DefaultPaths parent) {
        super(parent, src);
    }

    /**
     * A root for the main proto code.
     */
    public io.spine.tools.code.fs.proto.Directory mainProto() {
        return io.spine.tools.code.fs.proto.Directory.rootIn(main());
    }

    /**
     * A root for the test proto code.
     */
    public io.spine.tools.code.fs.proto.Directory testProto() {
        return io.spine.tools.code.fs.proto.Directory.rootIn(test());
    }
}