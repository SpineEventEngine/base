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

import com.google.common.collect.ImmutableSet;
import com.google.errorprone.annotations.Immutable;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Defines the values of {@link Subject} subject identifiers.
 *
 * @param <I>
 *         the type of the identifers
 */
@Immutable(containerOf = "I")
public final class IdParameter<I> {

    private final ImmutableSet<I> values;

    private IdParameter(ImmutableSet<I> values) {
        this.values = values;
    }

    public ImmutableSet<I> values() {
        return values;
    }

    public static <I> IdParameter<I> empty() {
        return new IdParameter<>(ImmutableSet.of());
    }

    public static <I> IdParameter<I> is(I value) {
        checkNotNull(value);
        return new IdParameter<>(ImmutableSet.of(value));
    }

    /**
     * Creates an instance of {@code IdParameter} with the identifier values restricted
     * to the passed one.
     *
     * @param values
     *         the identifier values to use
     * @param <I>
     *         the type of the identifier values
     * @return a new instance of this type
     */
    public static <I> IdParameter<I> in(ImmutableSet<I> values) {
        checkNotNull(values);
        return new IdParameter<>(values);
    }
}
