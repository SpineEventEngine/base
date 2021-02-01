/*
 * Copyright 2021, TeamDev. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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

import com.google.common.collect.ImmutableSet;

/**
 * An expression which sets the values of record identifiers to be used
 * in a {@linkplain Query query}.
 *
 * <p>Exists in a context of a corresponding
 * {@linkplain AbstractQueryBuilder query builder} instance.
 *
 * @param <I>
 *         the type of identifiers
 * @param <B>
 *         the type of the {@link AbstractQueryBuilder} implementation
 */
public final class IdCriterion<I, B extends AbstractQueryBuilder<I, ?, ?, B, ?>> {

    private final B builder;

    public IdCriterion(B builder) {
        this.builder = builder;
    }

    public B is(I value) {
        IdParameter<I> parameter = IdParameter.is(value);
        return builder.setIdParameter(parameter);
    }

    /**
     * Creates an instance of this criterion with the passed identifier values.
     */
    @SafeVarargs
    public final B in(I... values) {
        ImmutableSet<I> asSet = ImmutableSet.copyOf(values);
        IdParameter<I> parameter = IdParameter.in(asSet);
        return builder.setIdParameter(parameter);
    }

    /**
     * Creates an instance of this criterion with the passed identifier values.
     */
    public final B in(Iterable<I> values) {
        ImmutableSet<I> asList = ImmutableSet.copyOf(values);
        IdParameter<I> parameter = IdParameter.in(asList);
        return builder.setIdParameter(parameter);
    }
}
