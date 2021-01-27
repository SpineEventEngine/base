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
import io.spine.type.Type;

import static java.util.stream.Collectors.joining;

/**
 * A collection of generic parameters to be used for generating a class.
 *
 * @see InterfaceParameter
 */
@Immutable
public final class InterfaceParameters {

    private final ImmutableList<InterfaceParameter> params;

    private InterfaceParameters(ImmutableList<InterfaceParameter> params) {
        this.params = params;
    }

    public static InterfaceParameters of(InterfaceParameter... param) {
        ImmutableList<InterfaceParameter> params = ImmutableList.copyOf(param);
        return new InterfaceParameters(params);
    }

    public static InterfaceParameters empty() {
        return new InterfaceParameters(ImmutableList.of());
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
        String result = '<' + joinFor(type) + '>';
        return result;
    }

    private String joinFor(Type<?, ?> type) {
        return params.stream()
                     .map(param -> param.valueFor(type))
                     .collect(joining(", "));
    }
}
