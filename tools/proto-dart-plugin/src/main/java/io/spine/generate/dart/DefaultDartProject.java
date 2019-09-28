/*
 * Copyright 2019, TeamDev. All rights reserved.
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

package io.spine.generate.dart;

import io.spine.code.AbstractDirectory;
import io.spine.code.fs.DefaultProject;

import java.nio.file.Path;

final class DefaultDartProject extends DefaultProject {

    private DefaultDartProject(Path path) {
        super(path);
    }

    static DefaultDartProject at(Path root) {
        return new DefaultDartProject(root);
    }

    DartScrRoot src() {
        return new DartScrRoot(this);
    }

    static final class DartScrRoot extends SourceRoot {

        private DartScrRoot(DefaultProject parent) {
            super(parent, "src");
        }

        SourceSetWithProtobuf mainProto() {
            return new SourceSetWithProtobuf(getMain());
        }

        SourceSetWithProtobuf testProto() {
            return new SourceSetWithProtobuf(getTest());
        }
    }

    static final class SourceSetWithProtobuf extends SourceDir {

        @SuppressWarnings("DuplicateStringLiteralInspection")
        private static final String NAME = "proto";

        private SourceSetWithProtobuf(AbstractDirectory parent) {
            super(parent, NAME);
        }
    }
}
