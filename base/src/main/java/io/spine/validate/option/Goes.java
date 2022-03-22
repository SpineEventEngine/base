/*
 * Copyright 2022, TeamDev. All rights reserved.
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

package io.spine.validate.option;

import com.google.errorprone.annotations.Immutable;
import io.spine.code.proto.FieldContext;
import io.spine.code.proto.FieldDeclaration;
import io.spine.logging.Logging;
import io.spine.option.GoesOption;
import io.spine.option.OptionsProto;
import io.spine.validate.Constraint;

import static java.lang.String.format;

/**
 * An option that defines field bond to another field within the message.
 */
@Immutable
public final class Goes
        extends FieldValidatingOption<GoesOption>
        implements Logging {

    private Goes() {
        super(OptionsProto.goes);
    }

    public static Goes create() {
        return new Goes();
    }

    @Override
    public Constraint constraintFor(FieldContext field) {
        return new GoesConstraint(field.targetDeclaration(), optionValue(field));
    }

    @Override
    public boolean shouldValidate(FieldContext field) {
        return super.shouldValidate(field)
                && canBeRequired(field)
                && canPairedBeRequired(field);
    }

    private boolean canBeRequired(FieldContext context) {
        var field = context.targetDeclaration();
        var result = checkType(field);
        if (!result) {
            _warn().log(
                    "Field `%s` cannot be checked for presence. `(goes).with` is obsolete.",
                    field
            );
        }
        return result;
    }

    private boolean canPairedBeRequired(FieldContext context) {
        var option = optionValue(context);
        var pairedFieldName = option.getWith().trim();
        if (pairedFieldName.isEmpty()) {
            return false;
        }
        var field = context.targetDeclaration();
        var messageType = field.declaringType();
        var pairedField = messageType.field(pairedFieldName);
        var result = checkType(field);
        if (!result) {
            _warn().log(
                    "Field `%s` paired with `%s` cannot be checked for presence. " +
                            "`(goes).with` at %s is obsolete.",
                    pairedField, field, field
            );
        }
        return result;
    }

    private static boolean checkType(FieldDeclaration field) {
        var type = field.javaType();
        return field.isCollection() || Required.CAN_BE_REQUIRED.contains(type);
    }
}
