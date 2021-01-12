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

package io.spine.protobuf;

import com.google.common.collect.ImmutableList;
import com.google.common.primitives.Primitives;
import com.google.errorprone.annotations.Immutable;
import io.spine.annotation.Internal;
import io.spine.code.java.ClassName;
import io.spine.code.proto.FieldDeclaration;
import io.spine.code.proto.ScalarType;
import io.spine.type.MessageType;

import java.util.Optional;

import static com.google.common.base.Preconditions.checkState;

/**
 * Reads the type of the first field for a given Protobuf message type.
 *
 * <p>If the type is a primitive type, the corresponding Java wrapper type is returned.
 */
@Internal
@Immutable
public final class FirstMessageField implements DetermineType {

    @Override
    public ClassName apply(MessageType type) {
        ImmutableList<FieldDeclaration> fields = type.fields();
        checkState(fields.size() > 0,
                   "At least one field is required for `FirstMessageField`.");
        FieldDeclaration declaration = fields.get(0);
        ClassName result = toClassName(declaration);
        return result;
    }

    private static ClassName toClassName(FieldDeclaration declaration) {
        ClassName result;
        Optional<ScalarType> maybeScalar = ScalarType.of(declaration.descriptor()
                                                                    .toProto());
        if (maybeScalar.isPresent()) {
            Class<?> scalarType = maybeScalar.get()
                                             .javaClass();
            Class<?> wrapped = Primitives.wrap(scalarType);
            result = ClassName.of(wrapped);
        } else {
            result = ClassName.of(declaration.javaTypeName());
        }
        return result;
    }
}
