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

package io.spine.code.gen.java;

import com.google.common.collect.ImmutableList;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import io.spine.base.EntityColumn;
import io.spine.code.proto.FieldDeclaration;
import io.spine.code.proto.FieldName;
import io.spine.type.MessageType;

import javax.lang.model.element.Modifier;

import static io.spine.code.proto.ColumnOption.columnsOf;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;

final class ColumnSpec {

    private final MessageType messageType;
    private final ImmutableList<FieldDeclaration> columns;

    private ColumnSpec(MessageType messageType) {
        this.messageType = messageType;
        this.columns = columnsOf(messageType);
    }

    static ColumnSpec of(MessageType messageType) {
        return new ColumnSpec(messageType);
    }

    TypeSpec asTypeSpec(Modifier... modifiers) {
        TypeSpec result = TypeSpec
                .classBuilder("Columns")
                .addModifiers(modifiers)
                .addMethod(PrivateCtor.spec())
                .addMethods(columns())
                .build();
        return result;
    }

    private Iterable<MethodSpec> columns() {
        ImmutableList.Builder<MethodSpec> builder = ImmutableList.builder();
        columns.forEach(column -> builder.add(columnSpec(column)));
        return builder.build();
    }

    private MethodSpec columnSpec(FieldDeclaration column) {
        FieldName name = column.name();
        MethodSpec result = MethodSpec
                .methodBuilder(name.javaCase())
                .addModifiers(PUBLIC, STATIC)
                .returns(columnType().value())
                .addStatement("return new $T<>(\"$L\", $T.class)",
                              EntityColumn.class, name, simpleMessageName().value())
                .build();
        return result;
    }

    private JavaPoetName columnType() {
        JavaPoetName result = JavaPoetName.parameterized(EntityColumn.class, simpleMessageName());
        return result;
    }

    private JavaPoetName simpleMessageName() {
        JavaPoetName result = JavaPoetName.of(messageType.simpleJavaClassName());
        return result;
    }
}
