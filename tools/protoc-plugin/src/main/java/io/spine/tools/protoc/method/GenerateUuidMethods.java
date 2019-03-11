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

package io.spine.tools.protoc.method;

import com.google.common.collect.ImmutableList;
import io.spine.tools.protoc.CompilerOutput;
import io.spine.tools.protoc.UuidGenerateMethod;
import io.spine.type.MessageType;

/**
 * Generates methods for supplied UUID value type based on code generation task
 * {@link UuidGenerateMethod configuration}.
 */
final class GenerateUuidMethods extends AbstractMethodGenerationTask {

    GenerateUuidMethods(MethodFactories methodFactories, UuidGenerateMethod config) {
        super(methodFactories, config.getFactoryName());
    }

    /**
     * Generates new method for supplied {@link io.spine.base.UuidValue UuidValue} Protobuf
     * {@code type}.
     *
     * <p>No methods are generated if the method factory name is empty.
     */
    @Override
    public ImmutableList<CompilerOutput> generateFor(MessageType type) {
        if (isFactoryNameEmpty() || !type.isUuidValue()) {
            return ImmutableList.of();
        }
        return generateMethods(type);
    }
}
