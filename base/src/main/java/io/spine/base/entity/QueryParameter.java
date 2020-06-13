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

package io.spine.base.entity;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A parameter defining how to query an entity by the value of its {@link EntityColumn}.
 *
 * @param <S>
 *         the type of entity state
 * @param <V>
 *         the type of the entity column values
 */
public class QueryParameter<S extends EntityState<?>, V> {

    private final EntityColumn<S, V> column;
    private final V value;
    private final ComparisonOperator operator;

    /**
     * Creates an instance of {@code QueryParameter} targeting entities whose column value
     * is compared to the one provided in a specified way.
     *
     * @param column
     *         the column to query
     * @param value
     *         the column value to use when querying
     * @param operator
     *         the operator to use when comparing the actual column value to the provided one
     */
    QueryParameter(EntityColumn<S, V> column, V value, ComparisonOperator operator) {
        checkNotNull(column);
        checkNotNull(value);
        checkNotNull(operator);
        this.column = column;
        this.value = value;
        this.operator = operator;
    }

    /**
     * Returns the column to be queried.
     */
    public EntityColumn<S, V> column() {
        return column;
    }

    /**
     * Returns the value against which the column should be queried.
     */
    public V value() {
        return value;
    }

    /**
     * Returns the operator to compare the actual column value with the one
     * set in this {@code QueryParameter}.
     */
    public ComparisonOperator operator() {
        return operator;
    }
}
