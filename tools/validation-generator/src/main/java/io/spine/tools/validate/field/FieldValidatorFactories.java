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

package io.spine.tools.validate.field;

import io.spine.code.proto.FieldDeclaration;
import io.spine.tools.validate.code.Expression;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.tools.validate.code.Expression.formatted;
import static java.util.Optional.empty;

public final class FieldValidatorFactories {

    private final Expression messageAccess;

    public FieldValidatorFactories(Expression messageAccess) {
        this.messageAccess = checkNotNull(messageAccess);
    }

    public FieldValidatorFactory forField(FieldDeclaration field) {
        if (!field.isCollection()) {
            return forScalarField(field);
        } else {
            return onViolation -> empty();
        }
    }

    private FieldValidatorFactory
    forScalarField(FieldDeclaration field) {
        Expression fieldAccess =
                formatted("%s.get%s()", messageAccess, field.name().toCamelCase());
        switch (field.javaType()) {
            case STRING:
                return new StringFieldValidatorFactory(field, fieldAccess);
            case INT:
            case LONG:
            case FLOAT:
            case DOUBLE:
                return new NumberFieldValidatorFactory(field, fieldAccess);
            case BOOLEAN:
            case BYTE_STRING:
            case ENUM:
            case MESSAGE:
            default:
                return onViolation -> empty();
        }
    }
}
