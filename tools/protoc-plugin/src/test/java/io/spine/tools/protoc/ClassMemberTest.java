/*
 * Copyright 2020, TeamDev. All rights reserved.
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
import com.squareup.javapoet.MethodSpec;
import io.spine.code.fs.java.SourceFile;
import io.spine.tools.protoc.method.GeneratedMethod;
import io.spine.type.MessageType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.squareup.javapoet.TypeName.VOID;
import static javax.lang.model.element.Modifier.PUBLIC;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("`ClassMember` should")
final class ClassMemberTest {

    private static final String INSERTION_POINT_FORMAT = "class_scope:%s";

    @DisplayName("create valid compiler output")
    @Test
    void createValidCompilerOutput() {
        MethodSpec spec = MethodSpec
                .methodBuilder("testMethod")
                .addModifiers(PUBLIC)
                .returns(VOID)
                .build();
        GeneratedMethod method = new GeneratedMethod(spec);
        MessageType type = new MessageType(MessageWithClassScopeInsertion.getDescriptor());
        ClassMember result = ClassMember.method(method, type);
        File file = result.asFile();

        assertEquals(spec.toString(), file.getContent());
        assertEquals(insertionPoint(type), file.getInsertionPoint());
        assertEquals(sourceName(type), file.getName());
    }

    private static String sourceName(MessageType type) {
        return SourceFile.forType(type)
                         .toString()
                         .replace('\\', '/');
    }

    private static String insertionPoint(MessageType type) {
        return String.format(INSERTION_POINT_FORMAT, type.name());
    }
}
