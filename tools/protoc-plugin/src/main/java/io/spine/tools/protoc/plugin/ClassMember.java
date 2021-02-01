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

package io.spine.tools.protoc;

import com.google.protobuf.compiler.PluginProtos.CodeGeneratorResponse.File;
import io.spine.type.MessageType;

/**
 * A compiler output which alters a generated message with an additional method or nested type.
 *
 * <p>The output is added on the {@link InsertionPoint#class_scope class_scope} insertion point.
 */
public final class ClassMember extends AbstractCompilerOutput {

    private ClassMember(File file) {
        super(file);
    }

    /**
     * Creates a compiler output which alters the generated message with an additional method.
     *
     * @param method
     *         the source code of the added method
     * @param type
     *         the generated message type
     * @return a new instance of the {@code ClassMember} compiler output
     */
    public static ClassMember method(Method method, MessageType type) {
        File response = codeGeneratorResponse(method.toString(), type);
        return new ClassMember(response);
    }

    /**
     * Creates a compiler output which alters the generated message with
     * an additional nested class.
     *
     * @param cls
     *         the source code of the added nested class
     * @param type
     *         the generated message type
     * @return a new instance of the {@code ClassMember} compiler output
     */
    public static ClassMember nestedClass(NestedClass cls, MessageType type) {
        File response = codeGeneratorResponse(cls.toString(), type);
        return new ClassMember(response);
    }

    private static File
    codeGeneratorResponse(String content, MessageType type) {
        String insertionPoint = InsertionPoint.class_scope.forType(type);
        File result = ProtocPluginFiles.prepareFile(type)
               .setInsertionPoint(insertionPoint)
               .setContent(content)
               .build();
        return result;
    }
}
