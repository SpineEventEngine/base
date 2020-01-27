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

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import io.spine.base.EntityColumn;
import io.spine.code.gen.java.FieldJavadoc;
import io.spine.code.gen.java.GeneratedJavadoc;
import io.spine.code.gen.java.GeneratedMethodSpec;
import io.spine.code.gen.java.JavaPoetName;
import io.spine.code.proto.FieldDeclaration;
import io.spine.code.proto.FieldName;

import javax.lang.model.element.Modifier;

/**
 * A spec of the method which returns an entity column as an {@link EntityColumn} instance.
 *
 * <p>The name of the method is a column name in {@code javaCase}.
 */
final class ColumnSpec implements GeneratedMethodSpec {

    private final FieldDeclaration column;

    ColumnSpec(FieldDeclaration column) {
        this.column = column;
    }

    @Override
    public MethodSpec methodSpec(Modifier... modifiers) {
        FieldName name = columnName();
        MethodSpec result = MethodSpec
                .methodBuilder(name.javaCase())
                .addModifiers(modifiers)
                .returns(columnType().value())
                .addJavadoc(javadoc())
                .addStatement(methodBody())
                .build();
        return result;
    }

    private FieldName columnName() {
        return column.name();
    }

    private static JavaPoetName columnType() {
        JavaPoetName result = JavaPoetName.of(EntityColumn.class);
        return result;
    }

    private CodeBlock methodBody() {
        return CodeBlock.of(
                "return new $T(\"$L\")", EntityColumn.class, columnName()
        );
    }

    private CodeBlock javadoc() {
        GeneratedJavadoc javadoc = new FieldJavadoc(this.column, "column");
        return javadoc.spec();
    }
}
