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
import com.google.protobuf.Descriptors.OneofDescriptor;
import io.spine.base.FieldPath;

import java.util.List;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Validates a {@linkplain OneofDescriptor Oneof}.
 *
 * <p>Only a single field from a {@code Oneof} is validated — the field that is actually set.
 * If none of fields is set, a constraint violation is created.
 *
 * @see <a href="https://developers.google.com/protocol-buffers/docs/proto3#oneof">Oneof documentation</a>
 */
class OneofValidator {

    private final OneofDescriptor oneof;
    private final MessageValue message;

    OneofValidator(OneofDescriptor oneof, MessageValue message) {
        this.oneof = checkNotNull(oneof);
        this.message = checkNotNull(message);
    }

    ImmutableList<ConstraintViolation> validate() {
        ImmutableList.Builder<ConstraintViolation> violations = ImmutableList.builder();
        Optional<FieldValue> populatedField = message.valueOf(oneof);
        if (!populatedField.isPresent()) {
            violations.add(noneFieldIsSet());
        } else {
            List<ConstraintViolation> fieldViolations = validateField(populatedField.get());
            violations.addAll(fieldViolations);
        }
        return violations.build();
    }

    private ConstraintViolation noneFieldIsSet() {
        FieldPath oneofPath = message.context()
                                     .getFieldPath()
                                     .toBuilder()
                                     .addFieldName(oneof.getName())
                                     .build();
        ConstraintViolation requiredFieldNotFound = ConstraintViolation
                .newBuilder()
                .setMsgFormat("None of the %s Oneof fields is set.")
                .addParam(oneof.getName())
                .setFieldPath(oneofPath)
                .build();
        return requiredFieldNotFound;
    }

    private static List<ConstraintViolation> validateField(FieldValue field) {
        FieldValidator<?> validator = field.createValidator();
        return validator.validate();
    }
}
