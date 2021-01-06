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

package io.spine.validate.option;

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableSet;
import com.google.errorprone.annotations.Immutable;
import io.spine.annotation.Internal;

import java.util.Set;

import static com.google.common.collect.Sets.union;

/**
 * An implementation of {@link ValidatingOptionFactory} which adds validation options for numbers.
 *
 * <p>Creates {@code (max)}, {@code (min)}, and {@code (range)} options for number fields.
 */
@AutoService(ValidatingOptionFactory.class)
@Internal
@Immutable
public final class NumberOptionFactory implements StandardOptionFactory {

    private static final ImmutableSet<FieldValidatingOption<?>> NUMBER_OPTIONS =
            ImmutableSet.of(Max.create(), Min.create(), Range.create());
    private static final ImmutableSet<FieldValidatingOption<?>> COLLECTION_OPTIONS =
            ImmutableSet.of(Required.create(false), Goes.create(), Distinct.create());

    @Override
    public Set<FieldValidatingOption<?>> forInt() {
        return union(NUMBER_OPTIONS, COLLECTION_OPTIONS);
    }

    @Override
    public Set<FieldValidatingOption<?>> forLong() {
        return union(NUMBER_OPTIONS, COLLECTION_OPTIONS);
    }

    @Override
    public Set<FieldValidatingOption<?>> forFloat() {
        return union(NUMBER_OPTIONS, COLLECTION_OPTIONS);
    }

    @Override
    public Set<FieldValidatingOption<?>> forDouble() {
        return union(NUMBER_OPTIONS, COLLECTION_OPTIONS);
    }
}
