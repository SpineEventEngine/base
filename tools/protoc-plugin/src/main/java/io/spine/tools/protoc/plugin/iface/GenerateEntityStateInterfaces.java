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

package io.spine.tools.protoc.plugin.iface;

import com.google.common.collect.ImmutableList;
import io.spine.tools.protoc.plugin.CompilerOutput;
import io.spine.tools.protoc.EntityStateConfig;
import io.spine.tools.protoc.plugin.TypeParameter;
import io.spine.tools.protoc.plugin.TypeParameters;
import io.spine.type.MessageType;

import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.util.Exceptions.newIllegalStateException;

/**
 * Marks the provided message type with the {@link io.spine.base.EntityState EntityState} interface
 * if the type is recognized as entity state.
 *
 * <p>Requires the {@code EntityState} to be properly configured with
 * {@link io.spine.annotation.FirstGenericParameter FirstGenericParameter annotation}.
 */
final class GenerateEntityStateInterfaces extends InterfaceGenerationTask {

    GenerateEntityStateInterfaces(EntityStateConfig entityStateConfig) {
        super(entityStateConfig.getValue());
    }

    @Override
    public ImmutableList<CompilerOutput> generateFor(MessageType type) {
        checkNotNull(type);
        if (!type.isEntityState()) {
            return ImmutableList.of();
        }
        return generateInterfacesFor(type);
    }

    @Override
    TypeParameters interfaceParameters(MessageType type) {
        if (!type.isEntityState()) {
            return TypeParameters.of();
        }
        Optional<TypeParameter> firstParameter = readFirstGenericParameter(type);
        if (!firstParameter.isPresent()) {
            throw newIllegalStateException(
                    "The first generic parameter must be defined for the `EntityState` interface. " +
                            "Use `@FirstGenericParameter` with `EntityState` for this purpose.");
        }
        TypeParameter parameter = firstParameter.get();
        return TypeParameters.of(parameter);
    }
}
