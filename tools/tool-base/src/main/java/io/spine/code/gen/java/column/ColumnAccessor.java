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

package io.spine.code.gen.java.column;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import io.spine.base.EntityColumn;
import io.spine.code.gen.java.GeneratedJavadoc;
import io.spine.code.gen.java.GeneratedMethodSpec;
import io.spine.code.gen.java.JavaPoetName;
import io.spine.code.proto.FieldDeclaration;
import io.spine.code.proto.FieldName;

import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;

/**
 * A spec of the method which returns a {@linkplain EntityColumn strongly-typed entity column}.
 *
 * <p>The name of the method matches the column name in {@code javaCase}.
 */
final class ColumnAccessor implements GeneratedMethodSpec {

    private final FieldDeclaration column;
    private final ClassName entityStateName;
    private final ClassName returningValueName;

    ColumnAccessor(FieldDeclaration column) {
        this.column = column;
        this.entityStateName = JavaPoetName.of(column.declaringType().javaClassName()).className();
        this.returningValueName = JavaPoetName.of(columnType(column)).className();
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
                "return new $T<>($S, $T.class, $T.class)",
                EntityColumn.class,
                columnName(),
                entityStateName,
                returningValueName
        );
    }

    /**
     * Returns the type of the column value in a form suitable for the code generation.
     */
    private static ClassName columnType(FieldDeclaration column) {
        String rawTypeName = column.javaTypeName();
        boolean scalar = column.isScalar();
        io.spine.code.java.ClassName className = io.spine.code.java.ClassName.of(rawTypeName);
        String packageName = scalar ? ""
                                    : className.packageName()
                                               .value();
        return ClassName.get(packageName, className.withoutPackage());
    }

    /**
     * Returns the method Javadoc.
     */
    private GeneratedJavadoc javadoc() {
        return GeneratedJavadoc.twoParagraph(
                CodeBlock.of("Returns the $S column.", column.name()),
                CodeBlock.of("The column Java type is {@code $L}.", column.javaTypeName())
        );
    }
}
