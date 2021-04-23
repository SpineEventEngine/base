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

package io.spine.tools.mc.java.code.validate;

import com.google.common.collect.Range;

import static com.google.common.collect.BoundType.OPEN;
import static io.spine.tools.mc.java.code.validate.BooleanExpression.fromCode;

/**
 * A bound of a comparable field value.
 */
enum Bound {

    LOWER(">") {
        @Override
        boolean exists(Range<?> range) {
            return range.hasLowerBound();
        }

        @Override
        boolean isExclusive(Range<?> range) {
            return range.lowerBoundType() == OPEN;
        }

        @Override
        <T extends Comparable<?>> T endpoint(Range<T> range) {
            return range.lowerEndpoint();
        }
    },
    UPPER("<") {
        @Override
        boolean exists(Range<?> range) {
            return range.hasUpperBound();
        }

        @Override
        boolean isExclusive(Range<?> range) {
            return range.upperBoundType() == OPEN;
        }

        @Override
        <T extends Comparable<?>> T endpoint(Range<T> range) {
            return range.upperEndpoint();
        }
    };

    private final String sign;

    Bound(String sign) {
        this.sign = sign;
    }

    /**
     * Checks if this bound is declared in the given range.
     *
     * @param range
     *         the range to check
     * @return {@code true} if the bound is declared in the {@code range}, {@code false} otherwise
     */
    abstract boolean exists(Range<?> range);

    /**
     * Checks if this bound is exclusive or not.
     *
     * @param range
     *         the range to check
     * @return {@code true} if the bound declared in the {@code range} is exclusive, {@code false}
     *         otherwise
     */
    abstract boolean isExclusive(Range<?> range);

    /**
     * Obtains the bound value from the given {@code range}.
     */
    abstract <T extends Comparable<?>> T endpoint(Range<T> range);

    /**
     * Produces a {@link BooleanExpression} which evaluates to {@code true} if the given value field
     * evaluates to match this bound, and to {@code false} otherwise.
     *
     * @param field
     *         the field to match
     * @param range
     *         the range to match upon
     * @return bound checking expression
     */
    BooleanExpression matches(FieldAccess field, Range<?> range) {
        return fromCode("$L $L$L $L",
                        field,
                        sign,
                        isExclusive(range) ? "" : "=",
                        endpoint(range));
    }
}
