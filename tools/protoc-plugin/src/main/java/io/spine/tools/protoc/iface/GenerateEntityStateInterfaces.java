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

package io.spine.tools.protoc.iface;

import com.google.common.collect.ImmutableList;
import com.google.common.primitives.Primitives;
import io.spine.code.java.ClassName;
import io.spine.code.proto.FieldDeclaration;
import io.spine.code.proto.ScalarType;
import io.spine.tools.protoc.CompilerOutput;
import io.spine.tools.protoc.EntityStateConfig;
import io.spine.tools.protoc.InterfaceParameter;
import io.spine.tools.protoc.InterfaceParameters;
import io.spine.type.MessageType;

import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * Marks the provided message type with the {@link io.spine.base.EntityState EntityState} interface
 * if the type is recognized as entity state.
 */
final class GenerateEntityStateInterfaces extends InterfaceGenerationTask {

    GenerateEntityStateInterfaces(EntityStateConfig config) {
        super(config.getValue());
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
    InterfaceParameters interfaceParameters(MessageType type) {
        if (!type.isEntityState()) {
            return InterfaceParameters.empty();
        }
        InterfaceParameter firstParam = firstFieldOf(type);
        return firstParam.toCollection();
    }

    private static InterfaceParameter firstFieldOf(MessageType type) {
        ImmutableList<FieldDeclaration> fields = type.fields();
        checkState(fields.size() > 0,
                   "At least one field is required in an `EntityState` message type.");
        FieldDeclaration declaration = fields.get(0);
        ClassName value = toClassName(declaration);
        return new ExistingInterfaceParameter(value);
    }

    private static ClassName toClassName(FieldDeclaration declaration) {
        Optional<ScalarType> maybeScalar =
                ScalarType.of(declaration.descriptor()
                                         .toProto());
        ClassName result;
        if (maybeScalar.isPresent()) {
            Class<?> scalarType =
                    maybeScalar.get()
                               .javaClass();
            Class<?> wrapped = Primitives.wrap(scalarType);
            result = ClassName.of(wrapped);
        } else {
            result = ClassName.of(declaration.javaTypeName());
        }
        return result;
    }
}
