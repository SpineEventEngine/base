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

import com.google.common.collect.ImmutableList;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.protobuf.FieldMask;
import com.google.protobuf.Message;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.util.Preconditions2.checkPositive;

/**
 * An abstract base for the builders of {@link AbstractQuery} implementations.
 *
 * @param <I>
 *         the type of identifiers of the records which are queried
 * @param <R>
 *         the type of queried records
 * @param <P>
 *         the type of query parameters to use when composing the query
 * @param <B>
 *         the type of the {@code AbstractQueryBuilder} implementation
 * @param <Q>
 *         the type of {@code AbstractQuery} implementation
 */
abstract class AbstractQueryBuilder<I,
                                    R extends Message,
                                    P extends QueryParameter<?, ?>,
                                    B extends AbstractQueryBuilder<I, R, P, B, Q>,
                                    Q extends AbstractQuery<I, R, ?>> {

    private IdParameter<I> id = IdParameter.empty();

    private final List<P> parameters = new ArrayList<>();

    private final List<OrderBy<?, R>> ordering = new ArrayList<>();

    @MonotonicNonNull
    private Integer limit;

    @MonotonicNonNull
    private FieldMask mask;

    /**
     * Returns this instance of {@code EntityQueryBuilder}.
     */
    protected abstract B thisRef();

    /**
     * Creates a new instance of {@link EntityQuery} on top of this {@code EntityQueryBuilder}.
     */
    public abstract Q build();

    /**
     * Returns the criterion for the record identifiers.
     */
    IdParameter<I> id() {
        return id;
    }

    /**
     * Returns the criteria for the record fields.
     */
    ImmutableList<P> parameters() {
        return ImmutableList.copyOf(parameters);
    }

    /**
     * Returns the ordering directives to be applied to the resulting dataset.
     */
    ImmutableList<OrderBy<?, R>> ordering() {
        return ImmutableList.copyOf(ordering);
    }

    /**
     * Returns the maximum number of records in the resulting dataset.
     *
     * <p>Returns {@code null} if the limit is not set.
     */
    @Nullable Integer limit() {
        return limit;
    }

    /**
     * Returns the field mask to be applied to each of the resulting records.
     */
    @Nullable FieldMask mask() {
        return mask;
    }

    /**
     * Sets the maximum number of records in the resulting dataset.
     *
     * <p>The expected value must be positive.
     *
     * <p>If this method is not called, the limit value remains unset.
     */
    @CanIgnoreReturnValue
    public final B limit(int numberOfRecords) {
        checkPositive(numberOfRecords,
                      "Query limit expected to be positive, but got `%s`.", numberOfRecords);
        this.limit = numberOfRecords;
        return thisRef();
    }

    /**
     * Sets the field mask to be applied to each of the resulting records.
     *
     * <p>If the mask is not set, the query results contain the records as-is.
     */
    @CanIgnoreReturnValue
    public final B withMask(FieldMask mask) {
        this.mask = checkNotNull(mask);
        return thisRef();
    }

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
     */
    @CanIgnoreReturnValue
    public final B orderBy(RecordColumn<R, ?> column, Direction direction) {
        checkNotNull(column);
        checkNotNull(direction);
        ordering.add(new OrderBy<>(column, direction));
        return thisRef();
    }

    /**
     * Adds a parameter by which the records are to be queried.
     */
    @CanIgnoreReturnValue
    B addParameter(P parameter) {
        checkNotNull(parameter);
        parameters.add(parameter);
        return thisRef();
    }

    /**
     * Specifies the criterion for the record identifers.
     */
    B setIdParameter(IdParameter<I> value) {
        id = checkNotNull(value);
        return thisRef();
    }
}
