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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static io.spine.protobuf.TypeConverter.toAny;
import static java.util.stream.Collectors.toList;

/**
 * A constraint that, when applied to a repeated field, checks that all of the values are distinct
 * such that no two elements are equal to each other.
 *
 * @param <T>
 *         type of distinct entities
 */
final class DistinctConstraint<T> implements Constraint<FieldValue<T>> {

    @Override
    public List<ConstraintViolation> check(FieldValue<T> fieldValue) {
        ImmutableList<T> values = fieldValue.asList();
        Set<T> duplicates = findDuplicates(values);
        List<ConstraintViolation> violations =
                duplicates.stream()
                          .map(duplicate -> distinctViolated(fieldValue, duplicate))
                          .collect(toList());
        return violations;
    }

    private ConstraintViolation distinctViolated(FieldValue<T> value, T duplicate) {
        FieldPath path = value.context()
                              .getFieldPath();
        return ConstraintViolation
                .newBuilder()
                .setMsgFormat("Values must be distinct.")
                .setFieldPath(path)
                .setFieldValue(toAny(duplicate))
                .build();
    }

    private static <T> Set<T> findDuplicates(Iterable<T> potentialDuplicates) {
        Set<T> uniques = new HashSet<>();
        ImmutableSet.Builder<T> duplicates = ImmutableSet.builder();
        for (T potentialDuplicate : potentialDuplicates) {
            if (uniques.contains(potentialDuplicate)) {
                duplicates.add(potentialDuplicate);
            } else {
                uniques.add(potentialDuplicate);
            }
        }
        return duplicates.build();
    }
}
