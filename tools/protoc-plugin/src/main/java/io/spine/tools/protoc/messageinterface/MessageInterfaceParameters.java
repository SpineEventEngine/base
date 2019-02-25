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

package io.spine.tools.protoc.messageinterface;

import com.google.common.collect.ImmutableList;
import com.google.errorprone.annotations.Immutable;
import io.spine.type.Type;

import java.util.stream.Collectors;

/**
 * The generic parameters of the {@link MessageInterface}.
 *
 * <p>Contrary to the type information contained in a {@link Class} instance, the
 * {@code MessageInterfaceParameter} carries the logic on how to initialize itself based on the
 * message interface descendant.
 */
@Immutable
final class MessageInterfaceParameters {

    private final ImmutableList<MessageInterfaceParameter> params;

    private MessageInterfaceParameters(ImmutableList<MessageInterfaceParameter> params) {
        this.params = params;
    }

    static MessageInterfaceParameters of(MessageInterfaceParameter... parameters) {
        ImmutableList<MessageInterfaceParameter> params = ImmutableList.copyOf(parameters);
        return new MessageInterfaceParameters(params);
    }

    static MessageInterfaceParameters empty() {
        return new MessageInterfaceParameters(ImmutableList.of());
    }

    /**
     * Initializes parameter values based on the message interface descendant.
     *
     * <p>The values are then concatenated to a {@code String} of generated code.
     *
     * <p>Example output: {@code <ProjectId, String>}.
     */
    String getAsStringFor(Type<?, ?> type) {
        if (params.isEmpty()) {
            return "";
        }
        String result = '<' + initParams(type) + '>';
        return result;
    }

    private String initParams(Type<?, ?> type) {
        return params.stream()
                     .map(param -> param.valueFor(type))
                     .collect(Collectors.joining(", "));
    }
}
