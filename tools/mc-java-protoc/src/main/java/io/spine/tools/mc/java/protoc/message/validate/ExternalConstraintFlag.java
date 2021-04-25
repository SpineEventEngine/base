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

package io.spine.tools.mc.java.protoc.message.validate;

import com.google.common.base.Objects;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import io.spine.code.proto.FieldDeclaration;
import io.spine.validate.ExternalConstraints;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.squareup.javapoet.ClassName.bestGuess;
import static java.lang.String.format;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.STATIC;

/**
 * A boolean flag in the generated code which signifies whether a field has externally defined
 * constraints.
 *
 * <p>Such a flag is only generated for message fields marked with {@code (validate) = true} since
 * external constraints can only target those fields.
 */
final class ExternalConstraintFlag {

    private final FieldDeclaration declaration;
    private final BooleanExpression field;
    private final ClassName messageClassName;

    ExternalConstraintFlag(FieldDeclaration field) {
        this.declaration = checkNotNull(field);
        String name = format("is%sValidatedExternally", field.name().toCamelCase());
        this.field = BooleanExpression.fromCode(name);
        this.messageClassName = bestGuess(field.declaringType()
                                               .javaClassName()
                                               .toString());
    }

    /**
     * Generated an expression which obtains the value of this flag.
     *
     * <p>The value of the flag must be set when this expression is used. Otherwise,
     * a {@code NullPointerException} is thrown.
     *
     * @return an expression of the primitive value of the flag
     */
    BooleanExpression value() {
        return field;
    }

    /**
     * Obtains this flag as a {@link ClassMember}.
     */
    ClassMember asClassMember() {
        Expression<?> externallyValidated = BooleanExpression.fromCode(
                "$T.isDefinedFor($T.getDescriptor(), $S)",
                ExternalConstraints.class,
                messageClassName,
                declaration.name()
        );
        FieldSpec spec = FieldSpec
                .builder(Boolean.TYPE, field.toString(), PRIVATE, FINAL, STATIC)
                .initializer(externallyValidated.toCode())
                .build();
        return new Field(spec);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ExternalConstraintFlag)) {
            return false;
        }
        ExternalConstraintFlag that = (ExternalConstraintFlag) o;
        return Objects.equal(declaration, that.declaration);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(declaration);
    }
}
