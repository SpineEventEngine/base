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

import com.google.common.collect.ImmutableList;
import com.google.errorprone.annotations.Immutable;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import io.spine.tools.protoc.nested.GeneratedNestedClass;
import io.spine.tools.protoc.nested.NestedClassFactory;
import io.spine.type.MessageType;

import java.util.List;

import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;

@Immutable
public final class TestNestedClassFactory implements NestedClassFactory {

    @Override
    public List<GeneratedNestedClass> createFor(MessageType messageType) {
        ClassName messageClassName = ClassName.get("", messageType.simpleJavaClassName().value());
        MethodSpec ownClass = MethodSpec
                .methodBuilder("messageClass")
                .addModifiers(PUBLIC, STATIC)
                .returns(Class.class)
                .addStatement("return $T.class", messageClassName)
                .addJavadoc("Returns the message class for test purposes.")
                .build();
        TypeSpec spec = TypeSpec
                .classBuilder("SomeNestedClass")
                .addModifiers(PUBLIC, STATIC, FINAL)
                .addMethod(ownClass)
                .build();
        GeneratedNestedClass generatedClass = new GeneratedNestedClass(spec.toString());
        return ImmutableList.of(generatedClass);
    }
}
