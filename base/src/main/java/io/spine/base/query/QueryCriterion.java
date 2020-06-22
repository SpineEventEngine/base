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

package io.spine.base.query;

import io.spine.base.entity.EntityColumn;
import io.spine.base.entity.EntityState;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.base.query.ComparisonOperator.EQUALS;
import static io.spine.base.query.ComparisonOperator.GREATER_OR_EQUALS;
import static io.spine.base.query.ComparisonOperator.GREATER_THAN;
import static io.spine.base.query.ComparisonOperator.LESS_OR_EQUALS;
import static io.spine.base.query.ComparisonOperator.LESS_THAN;
import static io.spine.base.query.ComparisonOperator.NOT_EQUALS;

/**
 * An expression which sets the value to compare for the {@link EntityColumn} in scope of
 * a {@link EntityQueryBuilder} when building an {@link EntityQuery}.
 *
 * @param <S>
 *         the type of entity state
 * @param <V>
 *         the type of the column values for this criterion
 * @param <B>
 *         the type of the builder in scope of which this criterion exists
 */
public class QueryCriterion<S extends EntityState<?>,
                            V,
                            B extends EntityQueryBuilder<?, S, B, ?>> {

    private final EntityColumn<S, V> column;
    private final B builder;

    /**
     * Creates a new instance of {@code QueryCriterion}.
     *
     * @param column
     *         the column which actual value to use later in querying
     * @param builder
     *         the builder of an {@link EntityQuery} in scope of which the criterion is created
     */
    public QueryCriterion(EntityColumn<S, V> column, B builder) {
        this.column = column;
        this.builder = builder;
    }

    /**
     * Appends an associated {@link EntityQueryBuilder} with a criterion
     * checking that the value of the associated {@link EntityColumn} equals to the one provided.
     *
     * @param value
     *         the column value to use when querying
     * @return the instance of {@code EntityQueryBuilder} associated with this criterion
     */
    public B is(V value) {
        checkNotNull(value);
        EntityQueryParameter<S, V> parameter = new EntityQueryParameter<>(column, value, EQUALS);
        return builder.addParameter(parameter);
    }

    /**
     * Appends an associated {@link EntityQueryBuilder} with a criterion
     * checking that the value of the associated {@link EntityColumn} does not equal
     * to the one provided.
     *
     * @param value
     *         the column value to use when querying
     * @return the instance of {@code EntityQueryBuilder} associated with this criterion
     */
    public B isNot(V value) {
        checkNotNull(value);
        EntityQueryParameter<S, V> parameter = new EntityQueryParameter<>(column, value,
                                                                          NOT_EQUALS);
        return builder.addParameter(parameter);
    }

    /**
     * Appends an associated {@link EntityQueryBuilder} with a criterion
     * checking that the value of the associated {@link EntityColumn} is less than the one provided.
     *
     * @param value
     *         the column value to use when querying
     * @return the instance of {@code EntityQueryBuilder} associated with this criterion
     */
    public B isLessThan(V value) {
        checkNotNull(value);
        EntityQueryParameter<S, V> parameter = new EntityQueryParameter<>(column, value, LESS_THAN);
        return builder.addParameter(parameter);
    }

    /**
     * Appends an associated {@link EntityQueryBuilder} with a criterion
     * checking that the value of the associated {@link EntityColumn} is less or equal
     * to the one provided.
     *
     * @param value
     *         the column value to use when querying
     * @return the instance of {@code EntityQueryBuilder} associated with this criterion
     */
    public B isLessOrEqualTo(V value) {
        checkNotNull(value);
        EntityQueryParameter<S, V> parameter = new EntityQueryParameter<>(column, value,
                                                                          LESS_OR_EQUALS);
        return builder.addParameter(parameter);
    }

    /**
     * Appends an associated {@link EntityQueryBuilder} with a criterion
     * checking that the value of the associated {@link EntityColumn} is greater than
     * the one provided.
     *
     * @param value
     *         the column value to use when querying
     * @return the instance of {@code EntityQueryBuilder} associated with this criterion
     */
    public B isGreaterThan(V value) {
        checkNotNull(value);
        EntityQueryParameter<S, V> parameter = new EntityQueryParameter<>(column, value,
                                                                          GREATER_THAN);
        return builder.addParameter(parameter);
    }

    /**
     * Appends an associated {@link EntityQueryBuilder} with a criterion
     * checking that the value of the associated {@link EntityColumn} is greater or equal
     * to the one provided.
     *
     * @param value
     *         the column value to use when querying
     * @return the instance of {@code EntityQueryBuilder} associated with this criterion
     */
    public B isGreaterOrEqualTo(V value) {
        checkNotNull(value);
        EntityQueryParameter<S, V> parameter = new EntityQueryParameter<>(column, value,
                                                                          GREATER_OR_EQUALS);
        return builder.addParameter(parameter);
    }
}
