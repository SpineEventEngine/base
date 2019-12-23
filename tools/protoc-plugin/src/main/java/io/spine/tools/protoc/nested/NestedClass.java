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

package io.spine.tools.protoc.nested;

import com.google.protobuf.compiler.PluginProtos.CodeGeneratorResponse.File;
import io.spine.tools.protoc.AbstractCompilerOutput;
import io.spine.tools.protoc.InsertionPoint;
import io.spine.tools.protoc.ProtocPluginFiles;
import io.spine.type.MessageType;

/**
 * A {@link io.spine.tools.protoc.CompilerOutput CompilerOutput} item, which alters a generated
 * message class with a new nested class.
 */
final class NestedClass extends AbstractCompilerOutput {

    private NestedClass(File file) {
        super(file);
    }

    /**
     * Creates a new instance of {@code NestedClass}.
     */
    static NestedClass from(GeneratedNestedClass generatedClass, MessageType messageType) {
        String insertionPoint = InsertionPoint.class_scope.forType(messageType);
        String content = generatedClass.value();
        File.Builder file = ProtocPluginFiles.prepareFile(messageType);
        File result = file.setInsertionPoint(insertionPoint)
                          .setContent(content)
                          .build();
        return new NestedClass(result);
    }
}
