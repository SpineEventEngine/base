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

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import io.spine.base.SimpleField;
import io.spine.code.java.ClassName;
import io.spine.code.java.SimpleClassName;
import io.spine.code.proto.FieldDeclaration;
import io.spine.code.proto.FieldName;

import javax.lang.model.element.Modifier;

abstract class FieldSpec implements GeneratedMethodSpec {

    private final FieldDeclaration field;
    private final SimpleClassName messageName;

    FieldSpec(FieldDeclaration field, SimpleClassName messageName) {
        this.field = field;
        this.messageName = messageName;
    }

    @Override
    public MethodSpec methodSpec(Modifier... modifiers) {
        MethodSpec result = MethodSpec
                .methodBuilder(fieldName().javaCase())
                .addModifiers(modifiers)
                .returns(returnType().value())
                .addStatement(methodBody())
                .build();
        return result;
    }

    FieldName fieldName() {
        return field.name();
    }

    JavaPoetName returnType() {
        return shouldExposeNestedFields()
               ? nestedFieldsContainer()
               : simpleField();
    }

    private CodeBlock methodBody() {
        return shouldExposeNestedFields()
               ? returnNestedFieldsContainer()
               : returnSimpleField();
    }

    private boolean shouldExposeNestedFields() {
        return shouldExposeNestedFields(field);
    }

    @SuppressWarnings("DuplicateStringLiteralInspection") // Random duplication.
    private JavaPoetName nestedFieldsContainer() {
        SimpleClassName simpleClassName = fieldReturnType();
        JavaPoetName type = JavaPoetName.of(simpleClassName.with("Field"));
        return type;
    }

    private JavaPoetName simpleField() {
        JavaPoetName enclosingMessageName = enclosingMessageName();
        JavaPoetName type = JavaPoetName.parameterized(SimpleField.class, enclosingMessageName);
        return type;
    }

    abstract CodeBlock returnNestedFieldsContainer();

    abstract CodeBlock returnSimpleField();

    private SimpleClassName fieldReturnType() {
        String fieldTypeName = field.javaTypeName();
        SimpleClassName result = ClassName.of(fieldTypeName)
                                          .toSimple();
        return result;
    }

    JavaPoetName enclosingMessageName() {
        return JavaPoetName.of(messageName);
    }

    static boolean shouldExposeNestedFields(FieldDeclaration field) {
        return field.isMessage() && !field.isCollection();
    }
}
