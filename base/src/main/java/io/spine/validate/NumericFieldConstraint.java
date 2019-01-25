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

import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * A constraint that is applicable to numeric fields only.
 *
 * @param <V>
 *         a type of values that this constraint is applicable to.
 */
public abstract class NumericFieldConstraint<V extends Number> implements Constraint<FieldValue<V>> {

    @Override
    public List<ConstraintViolation> check(FieldValue<V> fieldValue) {
        ImmutableList<V> actualValues = fieldValue.asList();
        List<ConstraintViolation> violations =
                actualValues.stream()
                            .filter(actualValue -> doesNotSatisfy(actualValue, fieldValue))
                            .map(unsatisfactory -> constraintViolated(fieldValue, unsatisfactory))
                            .collect(toList());
        return violations;
    }

    /**
     * Whether the actual value of the field satisfies this constraint.
     *
     * @param value actual value that is being validated
     * @param fieldValue a value of the field. The difference between this and the first parameter
     *                   is that the repetetive fields are grouped in {@code fieldValue}, but are
     *                   still distinct {@code values}
     * @return {@code true} the specified does not satisfy this constraint
     */
    abstract boolean doesNotSatisfy(V value, FieldValue<V> fieldValue);

    /**
     * A violation that should vbe produced if this constraint is not
     * satisfied.
     */
    abstract ConstraintViolation constraintViolated(FieldValue<V> fieldValue, V actualValue);
}
