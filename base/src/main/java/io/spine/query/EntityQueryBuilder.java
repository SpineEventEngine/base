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
                                         Q extends EntityQuery<I, S, B>>
        extends AbstractQueryBuilder<I, S, EntitySubjectParameter<S, ?>, B, Q> {

    protected EntityQueryBuilder(Class<S> stateType) {
        super(stateType);
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
}
