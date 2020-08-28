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

import io.spine.base.EntityState;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.query.ComparisonOperator.EQUALS;
import static io.spine.query.ComparisonOperator.GREATER_OR_EQUALS;
import static io.spine.query.ComparisonOperator.GREATER_THAN;
import static io.spine.query.ComparisonOperator.LESS_OR_EQUALS;
import static io.spine.query.ComparisonOperator.LESS_THAN;

/**
 * A parameter defining how to query an entity by the value of its {@link EntityColumn}.
 *
 * @param <S>
 *         the type of entity state
 * @param <V>
 *         the type of the entity column values
 */
public final class EntitySubjectParameter<S extends EntityState<?>, V>
        extends SubjectParameter<S, EntityColumn<S, V>, V> {

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
    EntitySubjectParameter(EntityColumn<S, V> column, V value, ComparisonOperator operator) {
        super(column, operator, value);
    }

    /**
     * Creates a parameter which compares the actual column values to be equal to the passed value,
     * when querying.
     *
     * @param column
     *         the column which actual values are compared
     * @param value
     *         the value to compare to
     * @param <S>
     *         the type of the state of entities which records are queried
     * @param <V>
     *         the type of the value
     * @return a new instance of a parameter
     */
    public static <S extends EntityState<?>, V> EntitySubjectParameter<S, V>
    is(EntityColumn<S, V> column, V value) {
        checkNotNull(column);
        checkNotNull(value);
        return new EntitySubjectParameter<>(column, value, EQUALS);
    }

    /**
     * Creates a parameter which compares the actual column values to be less than the passed value,
     * when querying.
     *
     * @param column
     *         the column which actual values are compared
     * @param value
     *         the value to compare to
     * @param <S>
     *         the type of the state of entities which records are queried
     * @param <V>
     *         the type of the value
     * @return a new instance of a parameter
     */
    public static <S extends EntityState<?>, V> EntitySubjectParameter<S, V>
    lessThan(EntityColumn<S, V> column, V value) {
        return new EntitySubjectParameter<>(column, value, LESS_THAN);
    }

    /**
     * Creates a parameter which compares the actual column values to be less or equal to
     * the passed value, when querying.
     *
     * @param column
     *         the column which actual values are compared
     * @param value
     *         the value to compare to
     * @param <S>
     *         the type of the state of entities which records are queried
     * @param <V>
     *         the type of the value
     * @return a new instance of a parameter
     */
    public static <S extends EntityState<?>, V> EntitySubjectParameter<S, V>
    lessOrEqualTo(EntityColumn<S, V> column, V value) {
        return new EntitySubjectParameter<>(column, value, LESS_OR_EQUALS);
    }

    /**
     * Creates a parameter which compares the actual column values to be greater than
     * the passed value, when querying.
     *
     * @param column
     *         the column which actual values are compared
     * @param value
     *         the value to compare to
     * @param <S>
     *         the type of the state of entities which records are queried
     * @param <V>
     *         the type of the value
     * @return a new instance of a parameter
     */
    public static <S extends EntityState<?>, V> EntitySubjectParameter<S, V>
    greaterThan(EntityColumn<S, V> column, V value) {
        return new EntitySubjectParameter<>(column, value, GREATER_THAN);
    }

    /**
     * Creates a parameter which compares the actual column values to be greater or equal to
     * the passed value, when querying.
     *
     * @param column
     *         the column which actual values are compared
     * @param value
     *         the value to compare to
     * @param <S>
     *         the type of the state of entities which records are queried
     * @param <V>
     *         the type of the value
     * @return a new instance of a parameter
     */
    public static <S extends EntityState<?>, V> EntitySubjectParameter<S, V>
    greaterOrEqualTo(EntityColumn<S, V> column, V value) {
        return new EntitySubjectParameter<>(column, value, GREATER_OR_EQUALS);
    }
}
