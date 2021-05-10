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

import com.google.common.collect.ImmutableMap;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Descriptors.FieldDescriptor.Type;
import io.spine.tools.mc.js.code.CodeWriter;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.google.protobuf.Descriptors.FieldDescriptor.Type.BOOL;
import static com.google.protobuf.Descriptors.FieldDescriptor.Type.BYTES;
import static com.google.protobuf.Descriptors.FieldDescriptor.Type.DOUBLE;
import static com.google.protobuf.Descriptors.FieldDescriptor.Type.FIXED32;
import static com.google.protobuf.Descriptors.FieldDescriptor.Type.FIXED64;
import static com.google.protobuf.Descriptors.FieldDescriptor.Type.FLOAT;
import static com.google.protobuf.Descriptors.FieldDescriptor.Type.INT32;
import static com.google.protobuf.Descriptors.FieldDescriptor.Type.INT64;
import static com.google.protobuf.Descriptors.FieldDescriptor.Type.SFIXED32;
import static com.google.protobuf.Descriptors.FieldDescriptor.Type.SFIXED64;
import static com.google.protobuf.Descriptors.FieldDescriptor.Type.SINT32;
import static com.google.protobuf.Descriptors.FieldDescriptor.Type.SINT64;
import static com.google.protobuf.Descriptors.FieldDescriptor.Type.STRING;
import static com.google.protobuf.Descriptors.FieldDescriptor.Type.UINT32;
import static com.google.protobuf.Descriptors.FieldDescriptor.Type.UINT64;

/**
 * The value parser for the primitive proto fields.
 *
 * <p>All the fields that are not of the {@code message} or {@code enum} type are handled by this
 * parser.
 */
@SuppressWarnings("BadImport") // We use `FieldDescriptor.Type` for brevity.
final class PrimitiveTypeParser extends AbstractParser {

    /**
     * Maps a field to a factory for the parser of the corresponding primitive type.
     */
    private static final ImmutableMap<Type, ParserFactory> factories = factories();

    /** The type of the field for which to generate the parsing code. */
    private final Type fieldType;

    /**
     * Creates a new instance for the given field.
     *
     * @param field
     *         the processed field
     * @param writer
     *         the output to store the generated code
     */
    PrimitiveTypeParser(FieldDescriptor field, CodeWriter writer) {
        super(writer);
        checkNotNull(field);
        this.fieldType = field.getType();
    }

    /**
     * Creates the new instance for the given field type.
     *
     * @throws IllegalStateException
     *         if the parser for the specified type cannot be found
     */
    static Parser createFor(Type fieldType, CodeWriter writer) {
        checkNotNull(fieldType);
        checkNotNull(writer);
        checkState(factories.containsKey(fieldType),
                   "An attempt to get a parser for the unknown primitive type: `%s`.", fieldType);
        ParserFactory factory = factories.get(fieldType);
        Parser parser = factory.apply(writer);
        return parser;
    }

    /**
     * {@inheritDoc}
     *
     * <p>For the primitive field, the {@link Parser} implementation is used to convert
     * the field value into the appropriate type.
     */
    @Override
    public void parseIntoVariable(String value, String variable) {
        checkNotNull(value);
        checkNotNull(variable);
        Parser parser = createFor(fieldType, writer());
        parser.parseIntoVariable(value, variable);
    }

    private static ImmutableMap<Type, ParserFactory> factories() {
        return ImmutableMap.<Type, ParserFactory>builder()
                .put(BYTES, BytesParser::new)
                .put(DOUBLE, FloatParser::new)
                .put(FLOAT, FloatParser::new)
                .put(INT32, IdentityParser::new)
                .put(INT64, LongParser::new)
                .put(UINT32, IdentityParser::new)
                .put(UINT64, LongParser::new)
                .put(SINT32, IdentityParser::new)
                .put(SINT64, LongParser::new)
                .put(FIXED32, IdentityParser::new)
                .put(FIXED64, LongParser::new)
                .put(SFIXED32, IdentityParser::new)
                .put(SFIXED64, LongParser::new)
                .put(BOOL, IdentityParser::new)
                .put(STRING, IdentityParser::new)
                .build();
    }
}
