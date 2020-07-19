/*
 * Copyright 2020, TeamDev. All rights reserved.
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

package io.spine.query;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import io.spine.annotation.Internal;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.query.ComparisonOperator.EQUALS;

/**
 * Allows to specify the values for the {@linkplain CustomSubjectParameter}s.
 */
@Internal
public final class CustomCriterion<S, V, B extends QueryBuilder<?, ?, ?, B, ?>> {

    private final B builder;
    private final CustomColumn<S, V> column;

    /**
     * Creates a new instance of {@code CustomCriterion}.
     *
     * @param column
     *         the column for which the {@link CustomSubjectParameter} should be set
     * @param builder
     *         the builder in scope of which this criterion should exist
     */
    CustomCriterion(CustomColumn<S, V> column, B builder) {
        this.column = column;
        this.builder = builder;
    }

    /**
     * Sets the value which should be equal to the actual column value when querying.
     *
     * <p>Appends the {@code QueryBuilder} associated with this criterion with
     * the {@linkplain CustomSubjectParameter custom subject parameter} based on the specified
     * column and value set by the user.
     *
     * @return the instance of associated query builder
     */
    @CanIgnoreReturnValue
    public B is(V value) {
        checkNotNull(value);
        CustomSubjectParameter<S, V> param = new CustomSubjectParameter<>(column, value, EQUALS);
        return builder.addCustomParameter(param);
    }
}
