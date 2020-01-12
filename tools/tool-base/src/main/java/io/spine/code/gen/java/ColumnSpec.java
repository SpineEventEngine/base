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

import com.squareup.javapoet.MethodSpec;
import io.spine.base.EntityColumn;
import io.spine.code.java.SimpleClassName;
import io.spine.code.proto.FieldDeclaration;
import io.spine.code.proto.FieldName;

import javax.lang.model.element.Modifier;

final class ColumnSpec implements GeneratedMethodSpec {

    private final FieldDeclaration column;
    private final SimpleClassName messageName;

    ColumnSpec(FieldDeclaration column, SimpleClassName messageName) {
        this.column = column;
        this.messageName = messageName;
    }

    @Override
    public MethodSpec methodSpec(Modifier... modifiers) {
        FieldName name = column.name();
        MethodSpec result = MethodSpec
                .methodBuilder(name.javaCase())
                .addModifiers(modifiers)
                .returns(columnType().value())
                .addStatement("return new $T<>(\"$L\", $T.class)",
                              EntityColumn.class, name, enclosingMessageName().value())
                .build();
        return result;
    }

    private JavaPoetName columnType() {
        JavaPoetName result =
                JavaPoetName.parameterized(EntityColumn.class, enclosingMessageName());
        return result;
    }

    private JavaPoetName enclosingMessageName() {
        return JavaPoetName.of(messageName);
    }
}
