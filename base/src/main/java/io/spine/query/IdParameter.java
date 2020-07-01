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
import com.google.errorprone.annotations.Immutable;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A parameter defining how to query records by the value of their identifiers.
 */
@Immutable
public final class IdParameter<I> {

    private final ImmutableList<I> values;

    private IdParameter(ImmutableList<I> values) {
        this.values = values;
    }

    public ImmutableList<I> values() {
        return values;
    }

    public static <I> IdParameter<I> empty() {
        return new IdParameter<>(ImmutableList.of());
    }

    public static <I> IdParameter<I> is(I value) {
        checkNotNull(value);
        return new IdParameter<>(ImmutableList.of(value));
    }

    @SafeVarargs
    public static <I> IdParameter<I> in(I ...values) {
        checkNotNull(values);
        for (I value : values) {
            checkNotNull(value);
        }
        return new IdParameter<>(ImmutableList.copyOf(values));
    }
}
