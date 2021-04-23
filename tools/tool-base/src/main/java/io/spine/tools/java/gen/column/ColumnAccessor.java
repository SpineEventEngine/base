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

package io.spine.tools.java.gen.column;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import io.spine.tools.java.gen.JavadocBlock;
import io.spine.tools.java.gen.MethodCodeSpec;
import io.spine.tools.java.gen.JavaPoetName;
import io.spine.code.proto.FieldDeclaration;
import io.spine.code.proto.FieldName;
import io.spine.query.EntityColumn;

import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;

/**
 * A spec of the method which returns a {@linkplain EntityColumn strongly-typed entity column}.
 *
 * <p>The name of the method matches the column name in {@code javaCase}.
 */
final class ColumnAccessor implements MethodCodeSpec {

    private final FieldDeclaration column;
    private final TypeName entityStateName;
    private final TypeName returningValueName;

    ColumnAccessor(FieldDeclaration column) {
        this.column = column;
        this.entityStateName = JavaPoetName.of(column.declaringType()).value();
        this.returningValueName = JavaPoetName.of(column).value().box();
    }

    @Override
    public MethodSpec methodSpec() {
        FieldName name = columnName();
        MethodSpec result = MethodSpec
                .methodBuilder(name.javaCase())
                .addJavadoc(javadoc().spec())
                .addModifiers(PUBLIC, STATIC)
                .returns(columnType())
                .addStatement(methodBody())
                .build();
        return result;
    }

    /**
     * Returns the column name as defined in Protobuf.
     */
    private FieldName columnName() {
        return column.name();
    }

    /**
     * Returns the name of the Java type of a column.
     */
    private ParameterizedTypeName columnType() {
        JavaPoetName result = JavaPoetName.of(EntityColumn.class);
        ParameterizedTypeName parameterizedResult =
                ParameterizedTypeName.get(result.className(), entityStateName, returningValueName);
        return parameterizedResult;
    }

    /**
     * Returns the method body which instantiates the {@link EntityColumn}.
     */
    private CodeBlock methodBody() {
        return CodeBlock.of(
                "return new $T<>($S, $T.class, $T::$L)",
                EntityColumn.class,
                columnName(),
                returningValueName,
                entityStateName,
                "get" + columnName().toCamelCase()
        );
    }

    /**
     * Returns the method Javadoc.
     */
    private JavadocBlock javadoc() {
        return JavadocBlock.twoParagraph(
                CodeBlock.of("Returns the $S column.", column.name()),
                CodeBlock.of("The column Java type is {@code $L}.", column.javaTypeName())
        );
    }
}
