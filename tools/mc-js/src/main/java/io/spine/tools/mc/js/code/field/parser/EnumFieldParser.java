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

package io.spine.tools.mc.js.code.field.parser;

import com.google.protobuf.Descriptors.EnumDescriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import io.spine.tools.js.code.TypeName;
import io.spine.tools.mc.js.code.CodeWriter;
import io.spine.tools.mc.js.code.snippet.Let;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * The value parser for the proto fields of {@code enum} type.
 */
final class EnumFieldParser implements FieldParser {

    private final TypeName typeName;
    private final CodeWriter writer;

    /**
     * Creates a new {@code EnumFieldParser} for the given field.
     *
     * @param field
     *         the processed field
     * @param writer
     *         the output to store the generated code
     */
    EnumFieldParser(FieldDescriptor field, CodeWriter writer) {
        checkNotNull(field);
        EnumDescriptor enumType = field.getEnumType();
        this.typeName = TypeName.from(enumType);
        this.writer = checkNotNull(writer);
    }

    /**
     * {@inheritDoc}
     *
     * <p>The {@code enum} proto value in JSON is represented as a plain {@code string}.
     * Thus, the parser obtains the JS enum object property using the given {@code string} as
     * an attribute name.
     */
    @Override
    public void parseIntoVariable(String value, String variable) {
        checkNotNull(value);
        checkNotNull(variable);
        writer.append(parsedValue(variable, value));
    }

    private Let parsedValue(String name, String valueToParse) {
        String initializer = typeName.value() + '[' + valueToParse + ']';
        return Let.withValue(name, initializer);
    }
}
