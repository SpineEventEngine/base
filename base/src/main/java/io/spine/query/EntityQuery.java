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
 * A common contract for the classes generated for each entity state type, which defines
 * how the entities of this type may be queried.
 *
 * @param <I>
 *         the type of entity identifiers
 * @param <S>
 *         the type of the entity state
 * @param <B>
 *         the type of a particular {@linkplain EntityQueryBuilder query builder} implementation
 *         to create the query instances
 */
public abstract class EntityQuery<I,
                                  S extends EntityState<I>,
                                  B extends EntityQueryBuilder<I, S, B, ?>>
        extends AbstractQuery<I, S, EntitySubjectParameter<S, ?>> {

    private final B builder;

    /**
     * A common constructor contract for all {@code EntityQuery} implementations.
     */
    protected EntityQuery(B builder) {
        super(builder);
        this.builder = builder;
    }

    /**
     * Returns the builder instance on top of which this query has been created.
     */
    public final B toBuilder() {
        return builder;
    }
}
