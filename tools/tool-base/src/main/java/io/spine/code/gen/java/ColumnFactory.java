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

package io.spine.code.gen.java;

import com.google.common.collect.ImmutableList;
import com.google.errorprone.annotations.Immutable;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import io.spine.base.EntityColumn;
import io.spine.code.java.ClassName;
import io.spine.code.proto.FieldDeclaration;
import io.spine.code.proto.FieldName;
import io.spine.tools.protoc.nested.GeneratedNestedClass;
import io.spine.tools.protoc.nested.NestedClassFactory;
import io.spine.type.MessageType;

import java.util.List;

import static io.spine.code.proto.ColumnOption.columnsOf;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;

@Immutable
public final class ColumnFactory implements NestedClassFactory {

    @Override
    public List<GeneratedNestedClass> createFor(MessageType messageType) {
        MethodSpec privateCtor = MethodSpec
                .constructorBuilder()
                .addModifiers(PRIVATE)
                .build();
        TypeSpec.Builder typeSpec = TypeSpec
                .classBuilder("Columns")
                .addModifiers(PUBLIC, STATIC, FINAL)
                .addMethod(privateCtor);
        for (FieldDeclaration column : columnsOf(messageType)) {
            addColumnDeclaration(column, typeSpec);
        }
        String generatedCode = typeSpec.build()
                                       .toString();
        GeneratedNestedClass result = new GeneratedNestedClass(generatedCode);
        return ImmutableList.of(result);
    }

    private static void addColumnDeclaration(FieldDeclaration column, TypeSpec.Builder typeSpec) {
        JavaPoetName entityColumnType = JavaPoetName.of(EntityColumn.class);
        ClassName enclosingClass = column.declaringType()
                                         .javaClassName();
        TypeName enclosingClassName = JavaPoetName.of(enclosingClass)
                                                  .value();
        ParameterizedTypeName typeName = ParameterizedTypeName.get(entityColumnType.className(),
                                                               enclosingClassName);
        JavaPoetName returnType = JavaPoetName.of(typeName);
        FieldName columnName = column.name();
        MethodSpec method = MethodSpec
                .methodBuilder(columnName.javaCase())
                .addModifiers(PUBLIC, STATIC)
                .returns(returnType.value())
                .addStatement("return new $T<>(\"$L\", $T.class)",
                              EntityColumn.class, columnName, enclosingClassName)
                .build();
        typeSpec.addMethod(method);
    }
}
