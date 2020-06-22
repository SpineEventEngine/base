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

import io.spine.base.entity.EntityState;

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
        extends AbstractQueryBuilder<I, S, EntityQueryParameter<S, ?>, B, Q> {
}
