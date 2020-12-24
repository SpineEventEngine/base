/*
 * Copyright 2020, TeamDev. All rights reserved.
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

package io.spine.tools.protoc;

import com.google.common.collect.ImmutableList;
import com.google.errorprone.annotations.Immutable;
import io.spine.tools.protoc.iface.MessageInterface;
import io.spine.type.Type;

import static java.util.stream.Collectors.joining;

/**
 * The generic parameters of the {@link MessageInterface}.
 *
 * <p>Contrary to the type information contained in a {@link Class} instance, the
 * {@code TypeParameter} carries the logic on how to initialize itself based on the
 * message interface descendant.
 */
@Immutable
public final class TypeParameters {

    private final ImmutableList<TypeParameter> params;

    private TypeParameters(ImmutableList<TypeParameter> params) {
        this.params = params;
    }

    public static TypeParameters of(TypeParameter... parameters) {
        ImmutableList<TypeParameter> params = ImmutableList.copyOf(parameters);
        return new TypeParameters(params);
    }

    public static TypeParameters empty() {
        return new TypeParameters(ImmutableList.of());
    }

    /**
     * Initializes parameter values based on the message interface descendant.
     *
     * <p>The values are then concatenated to a {@code String} of generated code.
     *
     * <p>Example output: {@code <ProjectId, String>}.
     */
    public String asStringFor(Type<?, ?> type) {
        if (params.isEmpty()) {
            return "";
        }
        String result = '<' + initParams(type) + '>';
        return result;
    }

    private String initParams(Type<?, ?> type) {
        return params.stream()
                     .map(param -> param.valueFor(type))
                     .collect(joining(", "));
    }
}
