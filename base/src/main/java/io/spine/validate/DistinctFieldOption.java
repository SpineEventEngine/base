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
import com.google.protobuf.Descriptors;
import io.spine.option.OnDuplicate.DuplicatePolicy;
import io.spine.option.OptionsProto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

final class DistinctFieldOption extends FieldValidatingOption {

    @Override
    boolean applicableTo(Descriptors.FieldDescriptor field) {
        return field.isRepeated();
    }

    @Override
    ValidationException onInapplicable(Descriptors.FieldDescriptor field) {
        ConstraintViolation inapplicable = ConstraintViolation
                .newBuilder()
                .setMsgFormat("Error for field %s. A non-repeated field cannot be `distinct`.")
                .addParam(field.getFullName())
                .build();
        ValidationException exception = new ValidationException(ImmutableList.of(inapplicable));
        return exception;
    }

    @Override
    List<ConstraintViolation> applyValidatingRules(FieldValue value) {
        return checkForDuplicates(value);
    }

    @Override
    boolean optionPresentFor(FieldValue value) {
        return policyFor(value).getNumber() > 0;
    }

    @SuppressWarnings("SuspiciousMethodCalls")
    private static List<ConstraintViolation> checkForDuplicates(FieldValue value) {
        List<?> potentialDuplicates = new ArrayList<>(value.asList());
        List<ConstraintViolation> violations =
                potentialDuplicates.stream()
                                   .distinct()
                                   .filter(element -> Collections.frequency(potentialDuplicates,
                                                                            element) > 1)
                                   .map(element -> duplicateFound(element, value))
                                   .collect(Collectors.toList());
        return violations;
    }

    private static <T> ConstraintViolation duplicateFound(T duplicate, FieldValue value) {
        String fieldName = value.declaration()
                                .name()
                                .value();
        ConstraintViolation duplicateFound = ConstraintViolation
                .newBuilder()
                .setMsgFormat("Found a duplicate element in a `distinct` field %s. " +
                              "Duplicate element: %s.")
                .addParam(fieldName)
                .addParam(value.asList().toString())
                .addParam(duplicate.toString())
                .build();
        return duplicateFound;
    }

    private static DuplicatePolicy policyFor(FieldValue field) {
        return field.valueOf(OptionsProto.onDuplicate)
                    .getDuplicatePolicy();
    }
}
