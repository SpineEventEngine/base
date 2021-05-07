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

import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Descriptors.FieldDescriptor.Type;
import io.spine.tools.mc.js.code.CodeWriter;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * The value parser for the primitive proto fields.
 *
 * <p>All the fields that are not of the {@code message} or {@code enum} type are handled by this
 * parser.
 */
final class PrimitiveFieldParser implements FieldParser {

    @SuppressWarnings("BadImport") // We use `FieldDescriptor.Type` for brevity.
    private final Type fieldType;
    private final CodeWriter writer;

    /**
     * Creates a new instance for the given field.
     *
     * @param field
     *         the processed field
     * @param writer
     *         the output to store the generated code
     */
    PrimitiveFieldParser(FieldDescriptor field, CodeWriter writer) {
        checkNotNull(field);
        this.fieldType = field.getType();
        this.writer = checkNotNull(writer);
    }

    /**
     * {@inheritDoc}
     *
     * <p>For the primitive field, the {@link PrimitiveParser} implementation is used to convert
     * the field value into the appropriate type.
     */
    @Override
    public void parseIntoVariable(String value, String variable) {
        checkNotNull(value);
        checkNotNull(variable);
        PrimitiveParser parser = PrimitiveParsers.createFor(fieldType, writer);
        parser.parseIntoVariable(value, variable);
    }
}
