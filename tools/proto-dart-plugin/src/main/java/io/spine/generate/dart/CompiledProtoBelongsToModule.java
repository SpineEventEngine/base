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

import io.spine.code.dart.FileName;
import io.spine.code.proto.ProtoBelongsToModule;
import io.spine.code.proto.SourceFile;

import java.nio.file.Path;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A predicate determining if the given Protobuf file was compiled to JavaScript
 * and belongs to the specified module.
 */
public final class CompiledProtoBelongsToModule extends ProtoBelongsToModule {

    private final Path generatedRoot;

    /**
     * Creates a new instance.
     *
     * @param generatedRoot
     *         the root directory for generated Protobufs
     */
    CompiledProtoBelongsToModule(Path generatedRoot) {
        super();
        this.generatedRoot = generatedRoot;
    }

    @Override
    protected Path resolve(SourceFile file) {
        FileName fileName = FileName.relative(file.descriptor());
        Path filePath = generatedRoot.resolve(fileName.value());
        return filePath;
    }
}
