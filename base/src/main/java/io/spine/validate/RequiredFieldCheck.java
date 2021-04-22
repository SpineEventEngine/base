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

package io.spine.validate;

import com.google.common.collect.ImmutableSet;
import io.spine.base.FieldPath;
import io.spine.tools.code.proto.FieldDeclaration;

import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.util.Exceptions.newIllegalStateException;
import static io.spine.util.Preconditions2.checkNotEmptyOrBlank;

/**
 * Method object for validating a value.
 */
final class RequiredFieldCheck {

    private static final String ERROR_MESSAGE =
            "Message must have all the required fields set according to the rule: `%s`.";

    private final MessageValue message;
    private final ImmutableSet<Alternative> alternatives;
    private final String optionValue;

    RequiredFieldCheck(String optionValue,
                       ImmutableSet<Alternative> alternatives,
                       MessageValue value) {
        this.message = checkNotNull(value);
        this.optionValue = checkNotEmptyOrBlank(optionValue.trim());
        this.alternatives = checkNotNull(alternatives);
    }

    Optional<ConstraintViolation> perform() {
        boolean matches = alternatives
                .stream()
                .anyMatch(this::allPresent);
        return matches
               ? Optional.empty()
               : Optional.of(ConstraintViolation
                                     .newBuilder()
                                     .setMsgFormat(ERROR_MESSAGE)
                                     .setFieldPath(fieldPath())
                                     .setTypeName(typeName())
                                     .addParam(optionValue)
                                     .build()
        );
    }

    private boolean allPresent(Alternative alternative) {
        for (FieldDeclaration declaration : alternative.fields()) {
            String fieldName = declaration.name().value();
            Optional<FieldValue> field = message.valueOf(fieldName);
            FieldValue value = field.orElseThrow(
                    () -> fieldNotFound(message, fieldName)
            );
            if (value.isDefault()) {
                return false;
            }
        }
        return true;
    }

    private static IllegalStateException
    fieldNotFound(MessageValue containingMessage, String fieldName) {
        return newIllegalStateException(
                "Message `%s` declares a constraint with a field `%s`, which does not exist.",
                containingMessage.declaration(),
                fieldName
        );
    }

    private FieldPath fieldPath() {
        return message.context()
                      .fieldPath();
    }

    private String typeName() {
        return message.declaration()
                      .name()
                      .value();
    }
}
