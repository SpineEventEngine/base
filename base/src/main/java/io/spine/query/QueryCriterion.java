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

import com.google.protobuf.Message;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.query.ComparisonOperator.EQUALS;
import static io.spine.query.ComparisonOperator.GREATER_OR_EQUALS;
import static io.spine.query.ComparisonOperator.GREATER_THAN;
import static io.spine.query.ComparisonOperator.LESS_OR_EQUALS;
import static io.spine.query.ComparisonOperator.LESS_THAN;

/**
 * Expression which sets some restriction to the value of a record column when querying the records.
 */
abstract class QueryCriterion<R extends Message,
                              V,
                              C extends RecordColumn<R, V>,
                              B extends AbstractQueryBuilder<?, ?, ?, B, ?>> {

    private final C column;
    private final B builder;

    /**
     * Creates a new instance of {@code QueryCriterion}.
     *
     * @param column
     *         the column which actual value to use later in querying
     * @param builder
     *         the builder of an query in scope of which the criterion is created
     */
    QueryCriterion(C column, B builder) {
        this.column = column;
        this.builder = builder;
    }

    /**
     * Creates a new subject parameter of the type specific for the query type being built.
     *
     * @param col
     *         the record column queried
     * @param operator
     *         the comparison operator
     * @param value
     *         the value to which actual column values will be compared
     * @return a new instance of the subject parameter
     */
    protected abstract B addParameter(B builder, C col, ComparisonOperator operator, V value);

    /**
     * Appends an associated query builder with a criterion checking that the value
     * of the associated column equals to the one provided.
     *
     * @param value
     *         the column value to use when querying
     * @return the instance of query builder associated with this criterion
     */
    public B is(V value) {
        checkNotNull(value);
        return addParameter(builder, column, EQUALS, value);
    }

    /**
     * Appends an associated query builder with a criterion checking that the value
     * of the associated column is less than the one provided.
     *
     * @param value
     *         the column value to use when querying
     * @return the instance of query builder associated with this criterion
     */
    public B isLessThan(V value) {
        checkNotNull(value);
        return addParameter(builder, column, LESS_THAN, value);
    }

    /**
     * Appends an associated query builder with a criterion checking that the value
     * of the associated column is less or equal to the one provided.
     *
     * @param value
     *         the column value to use when querying
     * @return the instance of query builder associated with this criterion
     */
    public B isLessOrEqualTo(V value) {
        checkNotNull(value);
        return addParameter(builder, column, LESS_OR_EQUALS, value);
    }

    /**
     * Appends an associated query builder with a criterion checking that the value
     * of the associated column is greater than the one provided.
     *
     * @param value
     *         the column value to use when querying
     * @return the instance of query builder associated with this criterion
     */
    public B isGreaterThan(V value) {
        checkNotNull(value);
        return addParameter(builder, column, GREATER_THAN, value);
    }

    /**
     * Appends an associated query builder with a criterion checking that the value
     * of the associated column is greater or equal to the one provided.
     *
     * @param value
     *         the column value to use when querying
     * @return the instance of query builder associated with this criterion
     */
    public B isGreaterOrEqualTo(V value) {
        checkNotNull(value);
        return addParameter(builder, column, GREATER_OR_EQUALS, value);
    }
}
