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

package io.spine.js.generate;

import com.google.common.annotations.VisibleForTesting;
import com.google.protobuf.Descriptors.FileDescriptor;
import io.spine.code.fs.js.Directory;
import io.spine.code.gen.js.MethodReference;
import io.spine.code.gen.js.TypeName;
import io.spine.code.proto.FileSet;
import io.spine.code.proto.TypeSet;
import io.spine.js.generate.output.CodeLine;
import io.spine.js.generate.output.CodeLines;
import io.spine.js.generate.output.FileWriter;
import io.spine.js.generate.output.snippet.Comment;
import io.spine.js.generate.output.snippet.Method;
import io.spine.js.generate.output.snippet.Return;
import io.spine.type.Type;
import io.spine.type.TypeUrl;

import static io.spine.js.generate.output.CodeLine.emptyLine;

/**
 * Generates a method to obtain a {@code TypeUrl} for each type in a {@link FileSet}.
 *
 * <p>The class handles messages and enums of any nesting level.
 */
public class AppendTypeUrlGetter extends GenerationTask {

    private static final String METHOD_NAME = "typeUrl";

    public AppendTypeUrlGetter(Directory generatedRoot) {
        super(generatedRoot);
    }

    @Override
    protected void generateFor(FileSet fileSet) {
        for (FileDescriptor file : fileSet.files()) {
            generateFor(file);
        }
    }

    private void generateFor(FileDescriptor file) {
        CodeLines typeUrlMethods = typeUrlMethods(file);
        FileWriter writer = FileWriter.createFor(generatedRoot(), file);
        writer.append(typeUrlMethods);
    }

    @VisibleForTesting
    static CodeLines typeUrlMethods(FileDescriptor file) {
        CodeLines output = new CodeLines();
        TypeSet types = TypeSet.from(file);
        for (Type type : types.messagesAndEnums()) {
            Snippet method = typeUrlMethod(type);
            output.append(emptyLine());
            output.append(Comment.generatedBySpine());
            output.append(method);
        }
        return output;
    }

    @VisibleForTesting
    static Method typeUrlMethod(Type type) {
        TypeName typeName = TypeName.from(type.descriptor());
        MethodReference reference = MethodReference.onType(typeName, METHOD_NAME);
        Method method = Method
                .newBuilder(reference)
                .appendToBody(returnTypeUrl(type))
                .build();
        return method;
    }

    private static CodeLine returnTypeUrl(Type type) {
        TypeUrl typeUrl = type.url();
        return Return.stringLiteral(typeUrl.value());
    }
}
