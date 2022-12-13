/*
 * Copyright 2022, TeamDev. All rights reserved.
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

package io.spine.string;

import com.google.common.collect.ImmutableList;
import com.google.common.reflect.TypeToken;

import java.lang.reflect.ParameterizedType;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Registers passed stringifiers at the {@link StringifierRegistry}.
 */
public final class Registrar {

    private final ImmutableList<Stringifier<?>> stringifiers;

    /**
     * Creates an instance for registering passed stringifiers.
     */
    public Registrar(Iterable<Stringifier<?>> stringifiers) {
        checkNotNull(stringifiers);
        this.stringifiers = ImmutableList.copyOf(stringifiers);
        checkArgument(this.stringifiers.size() > 0, "At least one stringifier must be provided.");
    }

    /**
     * Registers stringifiers.
     */
    public void register() {
        var registry = StringifierRegistry.instance();
        stringifiers.forEach((stringifier) -> {
            var dataClass = getDataClass(stringifier.getClass());
            registry.register(stringifier, dataClass);
        });
    }

    /**
     * Obtains the class handled by the passed class of stringifiers.
     */
    @SuppressWarnings("rawtypes")   /* Avoiding the generic hell. */
    private static Class<?> getDataClass(Class<? extends Stringifier> stringifierClass) {
        var supertypeToken = TypeToken.of(stringifierClass)
                                      .getSupertype(Stringifier.class);
        var genericSupertype = (ParameterizedType) supertypeToken.getType();
        var typeArguments = genericSupertype.getActualTypeArguments();
        var typeArgument = typeArguments[0];
        return (Class<?>) typeArgument;
    }
}
