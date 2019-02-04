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
import com.google.common.collect.ImmutableSet;
import io.spine.base.FieldPath;
import io.spine.option.IfMissingOption;

import java.util.Optional;

import static com.google.protobuf.Descriptors.FieldDescriptor.JavaType;

/**
 * A constraint that, when applied to a field, checks for whether the field is set to a non-default
 * value.
 *
 * @param <T>
 *         type of the value that the constrained field holds
 */
final class RequiredConstraint<T> implements Constraint<FieldValue<T>> {

    /**
     * Types for which field presence of the field value can be checked.
     */
    private final ImmutableSet<JavaType> allowedTypes;

    RequiredConstraint(ImmutableSet<JavaType> allowedTypes) {
        this.allowedTypes = allowedTypes;
    }

    @Override
    public ImmutableList<ConstraintViolation> check(FieldValue<T> value) {
        boolean canNotCheckPresence = !allowedTypes.contains(value.javaType());
        if (canNotCheckPresence) {
            return ImmutableList.of();
        }
        return value.isDefault() ?
               requiredViolated(value) :
               ImmutableList.of();
    }

    private ImmutableList<ConstraintViolation> requiredViolated(FieldValue<T> fieldValue) {
        FieldPath path = fieldValue.context()
                                   .getFieldPath();
        ConstraintViolation violation = ConstraintViolation
                .newBuilder()
                .setMsgFormat(msgFormat(fieldValue))
                .setFieldPath(path)
                .build();
        return ImmutableList.of(violation);
    }

    private String msgFormat(FieldValue<T> fieldValue) {
        IfMissing<T> ifMissing = new IfMissing<>();
        String defaultValue = "Value must be set.";
        Optional<IfMissingOption> ifMissingValue = ifMissing.valueFrom(fieldValue);
        return ifMissingValue.isPresent() ?
               ifMissingValue.get()
                             .getMsgFormat() :
               defaultValue;
    }
}
