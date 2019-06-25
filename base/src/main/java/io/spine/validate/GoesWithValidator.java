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

package io.spine.validate;

import com.google.common.collect.ImmutableList;
import io.spine.validate.option.Constraint;
import io.spine.validate.option.Goes;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Validates fields with specified {@code (goes)} option.
 *
 * <p>See definition of {@code MessageOptions.goes} in {@code options.proto}.
 */
final class GoesWithValidator {

    private final MessageValue messageValue;

    /**
     * Creates a {@code (goes)} option validator for the supplied Protobuf message.
     */
    GoesWithValidator(MessageValue messageValue) {
        this.messageValue = checkNotNull(messageValue);
    }

    /**
     * Validates fields of the messages against {@code (goes)} validation option.
     */
    @SuppressWarnings({"unchecked", "Immutable"}) // types are effectively immutable and type-safe
    ImmutableList<ConstraintViolation> validate() {
        ImmutableList.Builder<ConstraintViolation> violations = ImmutableList.builder();
        Goes<?> goesFieldOption = Goes.create(messageValue);
        for (FieldValue value : messageValue.fieldsExceptOneofs()) {
            if (goesFieldOption.shouldValidate(value)) {
                Constraint constraint = goesFieldOption.constraintFor(value);
                violations.addAll(constraint.check(value));
            }
        }
        return violations.build();
    }
}
