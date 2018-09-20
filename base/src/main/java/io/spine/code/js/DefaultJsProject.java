/*
 * Copyright 2018, TeamDev. All rights reserved.
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

package io.spine.code.js;

import io.spine.code.DefaultProject;

import java.io.File;
import java.nio.file.Path;

import static com.google.common.base.Preconditions.checkNotNull;

public final class DefaultJsProject extends DefaultProject {

    private DefaultJsProject(Path path) {
        super(path);
    }

    public static DefaultJsProject at(Path root) {
        checkNotNull(root);
        DefaultJsProject result = new DefaultJsProject(root);
        return result;
    }

    public static DefaultJsProject at(File projectDir) {
        checkNotNull(projectDir);
        return at(projectDir.toPath());
    }

    public GeneratedProtoRoot proto() {
        return new GeneratedProtoRoot(this);
    }

    public static final class GeneratedProtoRoot extends SourceRoot {

        @SuppressWarnings("DuplicateStringLiteralInspection")
        // Same name for different directories.
        private static final String DIR_NAME = "proto";

        private GeneratedProtoRoot(DefaultProject parent) {
            super(parent, DIR_NAME);
        }

        public Directory mainJs() {
            return Directory.rootIn(getMain());
        }

        public Directory testJs() {
            return Directory.rootIn(getTest());
        }
    }
}
