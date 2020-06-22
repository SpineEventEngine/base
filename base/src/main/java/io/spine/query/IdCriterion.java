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

/**
 * An expression which sets the values of entity identifies to be used in {@link EntityQuery}.
 *
 * <p>Exists in a context of a corresponding {@link EntityQueryBuilder} instance.
 *
 * @param <I>
 *         the type of identifiers
 * @param <B>
 *         the type of the {@link EntityQueryBuilder} implementation
 */
public final class IdCriterion<I, B extends EntityQueryBuilder<I, ?, B, ?>> {

    private final B builder;

    public IdCriterion(B builder) {
        this.builder = builder;
    }

    public B is(I value) {
        IdParameter<I> parameter = IdParameter.is(value);
        return builder.setIdParameter(parameter);
    }

    @SafeVarargs
    public final B in(I... values) {
        return builder.setIdParameter(IdParameter.in(values));
    }
}
