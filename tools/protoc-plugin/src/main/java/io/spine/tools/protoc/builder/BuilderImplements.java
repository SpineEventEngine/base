/*
 * Copyright 2020, TeamDev. All rights reserved.
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

package io.spine.tools.protoc.builder;

import com.google.protobuf.compiler.PluginProtos.CodeGeneratorResponse.File;
import io.spine.protobuf.ValidatingBuilder;
import io.spine.tools.protoc.AbstractCompilerOutput;
import io.spine.tools.protoc.GeneratedClass;
import io.spine.tools.protoc.InsertionPoint;
import io.spine.tools.protoc.InterfaceParameters;
import io.spine.tools.protoc.ProtocPluginFiles;
import io.spine.type.MessageType;

import static java.lang.String.format;

/**
 * An insertion point which adds the {@link ValidatingBuilder} interface to the list of implemented
 * interfaces of the {@code Builder} of the given message type.
 */
final class BuilderImplements extends AbstractCompilerOutput {

    private BuilderImplements(File file) {
        super(file);
    }

    static BuilderImplements implementValidatingBuilder(MessageType targetType) {
        String insertionPointName = InsertionPoint.builder_implements.forType(targetType);
        String content = mixinFor(targetType);
        File file = ProtocPluginFiles.prepareFile(targetType)
                                     .setInsertionPoint(insertionPointName)
                                     .setContent(content)
                                     .build();
        return new BuilderImplements(file);
    }

    private static String mixinFor(MessageType type) {
        String generic = InterfaceParameters.of(new GeneratedClass())
                                            .asStringFor(type);
        return format("%s%s,", ValidatingBuilder.class.getName(), generic);
    }
}
