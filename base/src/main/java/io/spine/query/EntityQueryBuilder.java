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

import java.util.function.Function;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Base type for builders of queries which are aimed to fetch the records of entity states.
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
                                         Q extends EntityQuery<I, S, B>>
        extends AbstractQueryBuilder<I, S, EntitySubjectParameter<S, ?>, B, Q> {

    /**
     * Creates an new instance of the {@code EntityQueryBuilder}.
     *
     * @param idType
     *         the type of entity identifiers
     * @param stateType
     *         the type of entity state types
     */
    protected EntityQueryBuilder(Class<I> idType, Class<S> stateType) {
        super(idType, stateType);
    }

    /**
     * Sets the value for the custom column.
     *
     * @param column
     *         the custom column
     * @param value
     *         the value to use in querying
     * @param <V>
     *         the type of the column values
     * @return this instance of builder for chaining
     */
    public final <V> B where(CustomColumn<?, V> column, V value) {
        return column.in(thisRef())
                     .is(value);
    }

    /**
     * Builds a query on top of this entity query builder and transforms it according
     * to the logic of the passed transformer.
     *
     * <p>This method is a syntax sugar for a convenient method chaining for those who wishes to use
     * the produced query in their own transformation flow.
     *
     * @param transformer
     *         function transforming the query
     * @param <T>
     *         the type of the resulting object
     * @return a transformed query instance
     */
    public final <T> T build(Function<EntityQuery<?, ?, ?>, T> transformer) {
        checkNotNull(transformer);
        Q query = build();
        T result = transformer.apply(query);
        return result;
    }
}
