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
import com.google.protobuf.Descriptors;
import io.spine.option.OnDuplicate;
import io.spine.option.OnDuplicate.DuplicatePolicy;
import io.spine.option.OptionsProto;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * An option that can be applied to {@code repeated} Protobuf fields to specify that values
 * represented by that {@code repeated} field don't contain duplicates.
 */
final class DistinctFieldOption extends FieldValidatingOption<OnDuplicate.DuplicatePolicy> {

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
        Set<?> duplicates = findDuplicates(potentialDuplicates);
        return ImmutableList.of(duplicateFound(value, duplicates));
    }

    private static Set<?> findDuplicates(Iterable<?> potentialDuplicates) {
        Set<Object> duplicateLess = new HashSet<>();
        ImmutableSet.Builder<Object> duplicates = ImmutableSet.builder();
        for (Object potentialDuplicate : potentialDuplicates) {
            if (duplicateLess.contains(potentialDuplicate)) {
                duplicates.add(potentialDuplicate);
            } else {
                duplicateLess.add(potentialDuplicate);
            }
        }
        return duplicates.build();
    }

    private static <T> ConstraintViolation duplicateFound(FieldValue value, Set<T> duplicates) {
        String fieldName = value.declaration()
                                .name()
                                .value();
        List<String> stringValues = duplicates.stream()
                                              .map(Object::toString)
                                              .collect(Collectors.toList());
        String formatParams = String.join(", ", stringValues);
        String msgFormat =
                "Found a duplicate element in a `distinct` field %s. " +
                "Duplicate elements: " +
                formatParams;
        ConstraintViolation.Builder duplicatesBuilder = ConstraintViolation
                .newBuilder()
                .setMsgFormat(msgFormat)
                .addParam(fieldName);
        stringValues.forEach(duplicatesBuilder::addParam);
        return duplicatesBuilder.build();
    }

    private static DuplicatePolicy policyFor(FieldValue field) {
        return field.valueOf(OptionsProto.onDuplicate)
                    .getDuplicatePolicy();
    }

    @Override
    public DuplicatePolicy getValueFor(FieldValue something) {
        return something.valueOf(OptionsProto.onDuplicate)
                        .getDuplicatePolicy();
    }
}
