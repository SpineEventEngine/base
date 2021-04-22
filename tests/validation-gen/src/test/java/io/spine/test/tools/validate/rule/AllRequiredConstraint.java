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

package io.spine.test.tools.validate.rule;

import com.google.common.collect.ImmutableList;
import com.google.errorprone.annotations.Immutable;
import io.spine.tools.code.proto.FieldContext;
import io.spine.tools.code.proto.FieldDeclaration;
import io.spine.validate.ConstraintTranslator;
import io.spine.validate.ConstraintViolation;
import io.spine.validate.CustomConstraint;
import io.spine.validate.FieldValue;
import io.spine.validate.MessageValue;
import io.spine.validate.option.FieldConstraint;

import static java.lang.String.format;

/**
 * A field constraint for collection fields which makes all the field elements to be non-default.
 */
@Immutable
public final class AllRequiredConstraint
        extends FieldConstraint<Boolean>
        implements CustomConstraint {

    AllRequiredConstraint(Boolean optionValue, FieldDeclaration field) {
        super(optionValue, field);
    }

    @Override
    public String errorMessage(FieldContext field) {
        return format("Field `%s` cannot contain default values.", field.targetDeclaration());
    }

    @Override
    public ImmutableList<ConstraintViolation> validate(MessageValue containingMessage) {
        FieldValue value = containingMessage.valueOf(field());
        long count = value.values().count();
        long countOfNonDefault = value.nonDefault().count();
        FieldContext context = value.context();
        return count > countOfNonDefault
               ? ImmutableList.of(
                ConstraintViolation
                        .newBuilder()
                        .setMsgFormat(errorMessage(context))
                        .setTypeName(containingMessage.declaration().name().value())
                        .setFieldPath(context.fieldPath())
                        .build())
               : ImmutableList.of();
    }

    @Override
    public void accept(ConstraintTranslator<?> visitor) {
        visitor.visitCustom(this);
    }
}
