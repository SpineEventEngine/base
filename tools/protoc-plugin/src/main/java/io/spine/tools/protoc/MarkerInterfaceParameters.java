/*
 * Copyright 2018, TeamDev. All rights reserved.
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
import com.google.protobuf.DescriptorProtos.DescriptorProto;

import java.util.stream.Collectors;

/**
 * The generic parameters of the {@link MarkerInterface}.
 *
 * <p>Contrary to the type information contained in a {@link Class} instance, the
 * {@code MarkerInterfaceParameter} carry the logic of how to initialize itself based on the
 * interface implementor.
 */
final class MarkerInterfaceParameters {

    private final ImmutableList<MarkerInterfaceParameter> params;

    private MarkerInterfaceParameters(ImmutableList<MarkerInterfaceParameter> params) {
        this.params = params;
    }

    static MarkerInterfaceParameters of(MarkerInterfaceParameter... parameters) {
        ImmutableList<MarkerInterfaceParameter> params = ImmutableList.copyOf(parameters);
        return new MarkerInterfaceParameters(params);
    }

    static MarkerInterfaceParameters empty() {
        return new MarkerInterfaceParameters(ImmutableList.of());
    }

    /**
     * Initializes parameter values based on the interface implementor.
     *
     * <p>The values are then concatenated to a {@code String} of generated code.
     *
     * <p>Example output: {@code <ProjectId, String>}.
     */
    String getAsStringFor(DescriptorProto implementor) {
        if (params.isEmpty()) {
            return "";
        }
        String result = '<' + initParams(implementor) + '>';
        return result;
    }

    private String initParams(DescriptorProto implementor) {
        return params.stream()
                     .map(param -> param.valueFor(implementor))
                     .collect(Collectors.joining(", "));
    }
}
