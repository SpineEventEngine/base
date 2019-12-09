/*
 * Copyright 2019, TeamDev. All rights reserved.
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

package io.spine.validate.option;

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableSet;
import com.google.errorprone.annotations.Immutable;
import io.spine.annotation.Internal;

import java.util.Set;

/**
 * An implementation of {@link ValidatingOptionFactory} which adds validation options for some
 * primitive types.
 *
 * <p>Creates options:
 * <ul>
 *     <li>{@code (pattern)} for {@code string} fields;
 *     <li>{@code (max)}, {@code (min)}, and {@code (range)} for number fields.
 * </ul>
 */
@AutoService(ValidatingOptionFactory.class)
@Internal
@Immutable
public final class PrimitiveValidatingOptionFactory implements ValidatingOptionFactory {

    private static final ImmutableSet<FieldValidatingOption<?>> STRING_OPTIONS =
            ImmutableSet.of(Pattern.create());
    private static final ImmutableSet<FieldValidatingOption<?>> INT_OPTIONS =
            ImmutableSet.of(Max.create(), Min.create(), Range.create());
    private static final ImmutableSet<FieldValidatingOption<?>> LONG_OPTIONS =
            ImmutableSet.of(Max.create(), Min.create(), Range.create());
    private static final ImmutableSet<FieldValidatingOption<?>> FLOAT_OPTIONS =
            ImmutableSet.of(Max.create(), Min.create(), Range.create());
    private static final ImmutableSet<FieldValidatingOption<?>> DOUBLE_OPTIONS =
            ImmutableSet.of(Max.create(), Min.create(), Range.create());

    @Override
    public Set<FieldValidatingOption<?>> forString() {
        return STRING_OPTIONS;
    }

    @Override
    public Set<FieldValidatingOption<?>> forInt() {
        return INT_OPTIONS;
    }

    @Override
    public Set<FieldValidatingOption<?>> forLong() {
        return LONG_OPTIONS;
    }

    @Override
    public Set<FieldValidatingOption<?>> forFloat() {
        return FLOAT_OPTIONS;
    }

    @Override
    public Set<FieldValidatingOption<?>> forDouble() {
        return DOUBLE_OPTIONS;
    }
}
