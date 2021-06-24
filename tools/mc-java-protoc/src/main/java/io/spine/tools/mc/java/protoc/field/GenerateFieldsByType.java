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

package io.spine.tools.mc.java.protoc.field;

import com.google.common.collect.ImmutableList;
import io.spine.tools.java.code.field.FieldFactory;
import io.spine.tools.mc.java.protoc.CompilerOutput;
import io.spine.tools.protoc.JavaClassName;
import io.spine.tools.protoc.TypePattern;
import io.spine.type.MessageType;
import io.spine.type.TypeName;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Generates the strongly-typed fields for the type with the specified {@linkplain TypeName name}.
 */
final class GenerateFieldsByType extends FieldGenerationTask {

    private final TypePattern pattern;

    GenerateFieldsByType(JavaClassName fieldSupertype, TypePattern pattern, FieldFactory factory) {
        super(checkNotNull(fieldSupertype), checkNotNull(factory));
        this.pattern = checkNotNull(pattern);
    }

    @Override
    public ImmutableList<CompilerOutput> generateFor(MessageType type) {
        checkNotNull(type);
        boolean isExpectedType = matches(type);
        if (!isExpectedType) {
            return ImmutableList.of();
        }
        return generateFieldsFor(type);
    }

    private boolean matches(MessageType type) {
        String actual = type.name().value();
        switch (pattern.getValueCase()) {
            case EXPECTED_TYPE:
                String expected = pattern.getExpectedType().getValue();
                return actual.equals(expected);
            case REGEX:
                return actual.matches(pattern.getRegex());
            case VALUE_NOT_SET:
            default:
                throw new IllegalStateException("Empty type pattern.");
        }
    }
}
