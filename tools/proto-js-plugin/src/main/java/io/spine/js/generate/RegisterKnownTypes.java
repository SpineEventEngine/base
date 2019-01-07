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

package io.spine.js.generate;

import com.google.protobuf.Descriptors.FileDescriptor;
import io.spine.code.js.Directory;
import io.spine.code.js.TypeName;
import io.spine.code.proto.FileSet;
import io.spine.code.proto.Type;
import io.spine.code.proto.TypeSet;
import io.spine.js.generate.output.CodeLine;
import io.spine.js.generate.output.CodeLines;
import io.spine.js.generate.output.FileWriter;
import io.spine.js.generate.output.snippet.Comment;

import static io.spine.js.generate.output.CodeLine.emptyLine;
import static java.lang.String.format;

/**
 * A task, which generates the code for registering of messages and enums in known types.
 *
 * <p>The registration is happens by calling a registry from Spine Web.
 */
public final class RegisterKnownTypes extends GenerationTask {

    private static final String KNOWN_TYPES_NAME = "KnownTypes";
    private static final String REGISTER_METHOD_NAME = "register";

    public RegisterKnownTypes(Directory generatedRoot) {
        super(generatedRoot);
    }

    @Override
    protected void generateFor(FileSet fileSet) {
        for (FileDescriptor file : fileSet.files()) {
            CodeLines registrationCode = generateFor(file);
            FileWriter writer = FileWriter.createFor(generatedRoot(), file);
            writer.append(registrationCode);
        }
    }

    private static CodeLines generateFor(FileDescriptor file) {
        TypeSet types = TypeSet.messagesAndEnums(file);
        CodeLines lines = new CodeLines();
        lines.append(emptyLine());
        lines.append(Comment.generatedBySpine());
        lines.append(importKnownTypes());
        for (Type<?, ?> type : types.types()) {
            TypeName typeName = TypeName.from(type.descriptor());
            String registerLine = format("%s.%s(%s, '%s');", KNOWN_TYPES_NAME, REGISTER_METHOD_NAME,
                                         typeName, type.url());
            lines.append(registerLine);
        }
        return lines;
    }

    private static CodeLine importKnownTypes() {
        String line = format("let %s = require('%s').default;",
                             KNOWN_TYPES_NAME,
                             //TODO:2019-01-07:dmitry.grankin: Use the proper path.
                             "/Users/dima/Work/Teamdev/Spine/web/client-js/src/client/known-types.js");
        return CodeLine.of(line);
    }
}
