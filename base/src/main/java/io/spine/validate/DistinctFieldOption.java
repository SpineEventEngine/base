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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public final class DistinctFieldOption extends AbstractFieldValidatingOption {

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
    List<ConstraintViolation> doValidate(FieldValue value) {
        int onDuplicate = policyFor(value).getNumber();
        switch (onDuplicate) {
            case 0:
                return ImmutableList.of();
            case 1:
                return eliminateDuplicates(value);
            case 2:
                return checkForDuplicates(value);
            default:
                return ImmutableList.of();
        }
    }

    /**
     * @apiNote mutates the original value
     */
    private List<ConstraintViolation> eliminateDuplicates(FieldValue value) {
        List<?> collect = value.asList()
                               .stream()
                               .distinct()
                               .collect(Collectors.toList());

        Descriptors.FieldDescriptor descriptor = value.context()
                                                      .getTarget();
        value = FieldValue.of(collect, FieldContext.create(descriptor));
        return ImmutableList.of();
    }

    @SuppressWarnings("SuspiciousMethodCalls") //
    private static List<ConstraintViolation> checkForDuplicates(FieldValue value) {
        List<?> potentialDuplicates = new ArrayList<>(value.asList());
        Set<?> duplicateLess = new HashSet<>(value.asList());
        potentialDuplicates.removeAll(duplicateLess);
        List<ConstraintViolation> violations =
                potentialDuplicates.stream()
                                   .distinct()
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
                                      "Duplicate element: %s")
                .addParam(fieldName)
                .addParam(duplicate.toString())
                .build();
        return duplicateFound;
    }

    private static DuplicatePolicy policyFor(FieldValue field) {
        return field.context()
                    .getTarget()
                    .getOptions()
                    .getExtension(OptionsProto.onDuplicate)
                    .getDuplicatePolicy();
    }
}
