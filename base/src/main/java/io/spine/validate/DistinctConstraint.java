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
import com.google.errorprone.annotations.Immutable;
import com.google.errorprone.annotations.ImmutableTypeParameter;
import io.spine.base.FieldPath;
import io.spine.type.TypeName;

import java.util.HashSet;
import java.util.Set;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static io.spine.protobuf.TypeConverter.toAny;

/**
 * A repeated field constraint that requires values to be distinct.
 *
 * @param <T>
 *         type of values that this constraint is applicable to
 */
@Immutable
final class DistinctConstraint<@ImmutableTypeParameter T> extends FieldValueConstraint<T, Boolean> {

    DistinctConstraint(Boolean optionValue) {
        super(optionValue);
    }

    @Override
    public ImmutableList<ConstraintViolation> check(FieldValue<T> fieldValue) {
        ImmutableList<T> values = fieldValue.asList();
        Set<T> duplicates = findDuplicates(values);
        ImmutableList<ConstraintViolation> violations =
                duplicates.stream()
                          .map(duplicate -> distinctViolated(fieldValue, duplicate))
                          .collect(toImmutableList());
        return violations;
    }

    private ConstraintViolation distinctViolated(FieldValue<T> value, T duplicate) {
        FieldPath path = value.context()
                              .fieldPath();
        TypeName declaringTypeName = value.declaration()
                                          .declaringType()
                                          .name();
        return ConstraintViolation
                .newBuilder()
                .setMsgFormat("Values must be distinct.")
                .setFieldPath(path)
                .setFieldValue(toAny(duplicate))
                .setTypeName(declaringTypeName.value())
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
