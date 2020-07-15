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

import com.google.common.collect.ImmutableList;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.protobuf.FieldMask;
import com.google.protobuf.Message;
import io.spine.annotation.Internal;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A builder for instance of {@link Query}.
 *
 * @param <I>
 *         the type of identifiers of the records which are queried
 * @param <R>
 *         the type of queried records
 * @param <P>
 *         the type of subject parameters to use when composing the query
 * @param <B>
 *         the type of the {@code QueryBuilder} implementation
 * @param <Q>
 *         the type of {@code Query} implementation
 */
public interface QueryBuilder<I,
                              R extends Message,
                              P extends SubjectParameter<R, ?, ?>,
                              B extends QueryBuilder<I, R, P, B, Q>,
                              Q extends Query<I, R>> {

    /**
     * Creates a new instance of the query on top of this builder.
     */
    Q build();

    /**
     * Returns the type of the queried records.
     */
    Class<R> whichRecordType();

    /**
     * Returns the criterion for the record identifiers.
     */
    IdParameter<I> whichIds();

    /**
     * Returns the predicates for the record fields.
     */
    ImmutableList<QueryPredicate<R>> predicates();

    /**
     * Returns the ordering directives to be applied to the resulting dataset.
     */
    ImmutableList<OrderBy<?, R>> ordering();

    /**
     * Returns the maximum number of records in the resulting dataset.
     *
     * <p>Returns {@code null} if the limit is not set.
     */
    @Nullable Integer whichLimit();

    /**
     * Returns the field mask to be applied to each of the resulting records.
     */
    @Nullable FieldMask whichMask();

    /**
     * Adds a predicate to be treated in disjunction with the existing predicates.
     *
     * @return this instance of query builder, for chaining
     */
    @SuppressWarnings("unchecked") // See the implementations on the varargs issue.
    B either(Either<B>... parameters);

    /**
     * Sets the maximum number of records in the resulting dataset.
     *
     * <p>The expected value must be positive.
     *
     * <p>If this method is not called, the limit value remains unset.
     *
     * @return this instance of query builder, for chaining
     */
    @CanIgnoreReturnValue
    B limit(int numberOfRecords);

    /**
     * Sets the field mask to be applied to each of the resulting records.
     *
     * <p>If the mask is not set, the query results contain the records as-is.
     *
     * @return this instance of query builder, for chaining
     */
    @CanIgnoreReturnValue
    B withMask(FieldMask mask);

    /**
     * Adds an ordering directive.
     *
     * <p>Each call to this method adds another ordering directive. Directives are applied one
     * after another, each following determining the order of records remained "equal" after
     * the previous ordering.
     *
     * @param column
     *         the field of the message by which the resulting set should be ordered
     * @param direction
     *         the direction of ordering
     * @return this instance of query builder, for chaining
     */
    @CanIgnoreReturnValue
    B orderBy(RecordColumn<R, ?> column, Direction direction);

    /**
     * Adds a parameter by which the records are to be queried.
     *
     * @return this instance of query builder, for chaining
     */
    @CanIgnoreReturnValue
    @Internal
    B addParameter(P parameter);

    /**
     * Adds a parameter for the {@link CustomColumn}.
     *
     * @return this instance of query builder, for chaining
     */
    @CanIgnoreReturnValue
    @Internal
    B addCustomParameter(CustomSubjectParameter<?, ?> parameter);
}
