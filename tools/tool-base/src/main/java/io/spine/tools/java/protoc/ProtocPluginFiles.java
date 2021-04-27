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

package io.spine.tools.java.protoc;

import io.spine.tools.java.fs.SourceFile;
import io.spine.type.Type;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.protobuf.compiler.PluginProtos.CodeGeneratorResponse.File;

/**
 * Utilities for working with {@link com.google.protobuf.compiler.PluginProtos PluginProtos} files.
 */
public final class ProtocPluginFiles {

    /** Prevents instantiation of this utility class. */
    private ProtocPluginFiles() {
    }

    /**
     * Prepares {@link com.google.protobuf.compiler.PluginProtos.CodeGeneratorResponse.File file}
     * builder with a pre-defined name of the specified {@link Type type}.
     *
     * @param type
     *         Protobuf type to prepare a file builder for
     * @return file builder
     */
    public static File.Builder prepareFile(Type<?, ?> type) {
        checkNotNull(type);
        String fileName = SourceFile.forType(type)
                                    .toString();
        return prepareFile(fileName);
    }

    /**
     * Prepares {@link com.google.protobuf.compiler.PluginProtos.CodeGeneratorResponse.File file}
     * builder with a pre-defined name.
     *
     * @param name
     *         name of the file
     * @return file builder
     */
    public static File.Builder prepareFile(String name) {
        checkNotNull(name);
        // Protoc consumes only `/` path separators.
        String uriStyleName = name.replace('\\', '/');
        File.Builder srcFile = File.newBuilder()
                                   .setName(uriStyleName);
        return srcFile;
    }
}
