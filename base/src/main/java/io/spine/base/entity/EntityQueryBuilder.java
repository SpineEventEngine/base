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

import com.google.common.collect.ImmutableList;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.protobuf.FieldMask;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.util.Preconditions2.checkPositive;

/**
 * An abstract base for builders of an {@code EntityQuery} for a particular entity state type.
 *
 * @param <I>
 *         the type of entity identifiers
 * @param <S>
 *         the type of an entity state
 * @param <B>
 *         the type of a particular {@code EntityQueryBuilder} implementation
 * @param <Q>
 *         the type of a particular {@link EntityQuery} implementation which this builder
 *         is aimed to build
 */
public abstract class EntityQueryBuilder<I,
                                         S extends EntityState<I>,
                                         B extends EntityQueryBuilder<I, S, B, Q>,
                                         Q extends EntityQuery<I, S, B>> {

    private IdParameter<I, S> id = IdParameter.empty();

    private final List<QueryParameter<S, ?>> parameters = new ArrayList<>();

    private final List<OrderBy<?, S, ?>> ordering = new ArrayList<>();

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

    ImmutableList<QueryParameter<S, ?>> parameters() {
        return ImmutableList.copyOf(parameters);
    }

    ImmutableList<OrderBy<?, S, ?>> ordering() {
        return ImmutableList.copyOf(ordering);
    }

    @Nullable Integer limit() {
        return limit;
    }

    @Nullable FieldMask mask() {
        return mask;
    }

    @CanIgnoreReturnValue
    public final B limit(int numberOfRecords) {
        checkPositive(numberOfRecords,
                      "Query `limit` expected to be positive, but got `%s`.", numberOfRecords);
        this.limit = numberOfRecords;
        return thisRef();
    }

    @CanIgnoreReturnValue
    public final B withMask(FieldMask mask) {
        checkNotNull(mask);
        this.mask = mask;
        return thisRef();
    }

    @CanIgnoreReturnValue
    public final B orderBy(EntityColumn<S, ?> column, Direction direction) {
        ordering.add(new OrderBy<>(column, direction));
        return thisRef();
    }

    @CanIgnoreReturnValue
    B addParameter(QueryParameter<S, ?> parameter) {
        parameters.add(parameter);
        return thisRef();
    }

    B setIdParameter(IdParameter<I, S> value) {
        id = value;
        return thisRef();
    }

    public IdCriterion<I, S, B> id() {
        return new IdCriterion<>(thisRef());
    }
}
