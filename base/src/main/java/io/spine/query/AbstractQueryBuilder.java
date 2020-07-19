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
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.query.LogicalOperator.AND;
import static io.spine.query.LogicalOperator.OR;
import static io.spine.util.Preconditions2.checkPositive;

/**
 * An abstract base for the builders of {@link AbstractQuery} implementations.
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
abstract class AbstractQueryBuilder<I,
                                    R extends Message,
                                    P extends SubjectParameter<R, ?, ?>,
                                    B extends QueryBuilder<I, R, P, B, Q>,
                                    Q extends Query<I, R>>
        implements QueryBuilder<I, R, P, B, Q> {

    private final Class<R> recordType;

    private IdParameter<I> id = IdParameter.empty();

    private final List<QueryPredicate<R>> predicates = new ArrayList<>();

    private QueryPredicate.Builder<R> currentPredicate = QueryPredicate.newBuilder(AND);

    private final List<OrderBy<?, R>> ordering = new ArrayList<>();

    @MonotonicNonNull
    private Integer limit;

    @MonotonicNonNull
    private FieldMask mask;

    AbstractQueryBuilder(Class<R> recordType) {
        this.recordType = recordType;
    }

    /**
     * Returns this instance of builder.
     */
    protected abstract B thisRef();

    @Override
    public Class<R> whichRecordType() {
        return recordType;
    }

    @Override
    public IdParameter<I> whichIds() {
        return id;
    }

    @Override
    public ImmutableList<QueryPredicate<R>> predicates() {
        QueryPredicate<R> currentOne = currentPredicate.build();
        return ImmutableList.<QueryPredicate<R>>builder()
                .addAll(predicates)
                .add(currentOne)
                .build();
    }

    @Override
    public ImmutableList<OrderBy<?, R>> ordering() {
        return ImmutableList.copyOf(ordering);
    }

    @Override
    public @Nullable Integer whichLimit() {
        return limit;
    }

    @Override
    public Optional<FieldMask> whichMask() {
        return Optional.ofNullable(mask);
    }

    @Override
    @CanIgnoreReturnValue
    public final B limit(int numberOfRecords) {
        checkPositive(numberOfRecords,
                      "Query limit expected to be positive, but got `%s`.", numberOfRecords);
        this.limit = numberOfRecords;
        return thisRef();
    }

    @Override
    @CanIgnoreReturnValue
    public final B withMask(FieldMask mask) {
        this.mask = checkNotNull(mask);
        return thisRef();
    }

    @Override
    @CanIgnoreReturnValue
    public final B orderBy(RecordColumn<R, ?> column, Direction direction) {
        checkNotNull(column);
        checkNotNull(direction);
        ordering.add(new OrderBy<>(column, direction));
        return thisRef();
    }

    @Override
    @SafeVarargs
    @SuppressWarnings("ReturnValueIgnored")     // `Either` values are applied independently.
    public final B either(Either<B>... parameters) {
        if (currentPredicate.hasParams()) {
            predicates.add(currentPredicate.build());
        }

        currentPredicate = QueryPredicate.newBuilder(OR);
        for (Either<B> parameter : parameters) {
            parameter.apply(thisRef());
        }
        predicates.add(currentPredicate.build());
        currentPredicate = QueryPredicate.newBuilder(AND);
        return thisRef();
    }

    /**
     * Adds a parameter by which the records are to be queried.
     *
     * @return this instance of query builder, for chaining
     */
    @Override
    @CanIgnoreReturnValue
    public final B addParameter(P parameter) {
        checkNotNull(parameter);
        currentPredicate.add(parameter);
        return thisRef();
    }

    /**
     * Adds a parameter for the {@link CustomColumn}.
     *
     * @return this instance of query builder, for chaining
     */
    @CanIgnoreReturnValue
    @Override
    public final B addCustomParameter(CustomSubjectParameter<?, ?> parameter) {
        checkNotNull(parameter);
        currentPredicate.addCustom(parameter);
        return thisRef();
    }

    /**
     * Sets the value of the identifier parameter.
     *
     * @return this instance of query builder, for chaining
     */
    @Internal
    protected final B setIdParameter(IdParameter<I> value) {
        id = checkNotNull(value);
        return thisRef();
    }

    /**
     * Adds the predicate.
     *
     * @return this instance of query builder, for chaining
     */
    @Internal
    protected final B addPredicate(QueryPredicate<R> value) {
        checkNotNull(value);
        predicates.add(value);
        return thisRef();
    }

    /**
     * Adds the ordering directive.
     *
     * @return this instance of query builder, for chaining
     */
    protected final B addOrdering(OrderBy<?, R> value) {
        checkNotNull(value);
        ordering.add(value);
        return thisRef();
    }
}
