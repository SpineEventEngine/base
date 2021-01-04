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

package io.spine.tools.protoc.field;

import com.google.common.collect.ImmutableList;
import io.spine.code.gen.java.FieldFactory;
import io.spine.code.java.ClassName;
import io.spine.tools.protoc.plugin.CompilerOutput;
import io.spine.tools.protoc.ConfigByType;
import io.spine.type.MessageType;
import io.spine.type.TypeName;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.util.Preconditions2.checkNotDefaultArg;

/**
 * Generates the strongly-typed fields for the type with the specified {@linkplain TypeName name}.
 */
final class GenerateFieldsByType extends FieldGenerationTask {

    private final TypeName expectedType;

    GenerateFieldsByType(ConfigByType config, FieldFactory factory) {
        super(fieldSupertype(checkNotNull(config)), checkNotNull(factory));
        checkNotDefaultArg(config.getPattern());
        this.expectedType = expectedType(config);
    }

    @Override
    public ImmutableList<CompilerOutput> generateFor(MessageType type) {
        checkNotNull(type);
        boolean isExpectedType = expectedType.equals(type.name());
        if (!isExpectedType) {
            return ImmutableList.of();
        }
        return generateFieldsFor(type);
    }

    private static ClassName fieldSupertype(ConfigByType config) {
        String typeName = config.getValue();
        return ClassName.of(typeName);
    }

    private static TypeName expectedType(ConfigByType config) {
        return TypeName.of(config.getPattern().getExpectedType());
    }
}
