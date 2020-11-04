/*
 * Copyright 2020, TeamDev. All rights reserved.
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

package io.spine.js.generate.field.parser.primitive;

import com.google.common.collect.ImmutableMap;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Descriptors.FieldDescriptor.Type;
import io.spine.js.generate.output.CodeLines;

import java.util.Map;

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
 * The helper class which creates a {@link PrimitiveParser} based on the passed field type.
 */
public final class PrimitiveParsers {

    /**
     * The global map which maps the field {@linkplain FieldDescriptor#getType() type} to the
     * {@link PrimitiveParser} builder instance.
     */
    @SuppressWarnings("BadImport")
    private static final Map<Type, PrimitiveParser.Builder<?>> parsers = parsers();

    /** Prevents the instantiation of this utility class. */
    private PrimitiveParsers() {
    }

    /**
     * Creates the new instance of {@code PrimitiveParser} for the given field type.
     *
     * @param fieldType
     *         the type of the field for which to create the parser
     * @param jsOutput
     *         the {@code JsOutput} to accumulate the generated code
     * @return the new instance of the {@code PrimitiveParser}
     * @throws IllegalStateException
     *         if the parser for the specified type cannot be found
     */
    public static PrimitiveParser createFor(@SuppressWarnings("BadImport") Type fieldType,
                                            CodeLines jsOutput) {
        checkNotNull(fieldType);
        checkNotNull(jsOutput);
        PrimitiveParser.Builder<?> parserBuilder = parsers.get(fieldType);
        checkState(parsers.containsKey(fieldType),
                   "An attempt to get a parser for the unknown Primitive type: %s", fieldType);
        PrimitiveParser parser = parserBuilder
                .setJsOutput(jsOutput)
                .build();
        return parser;
    }

    @SuppressWarnings("BadImport") // For `FieldDescriptor.Type`.
    private static Map<Type, PrimitiveParser.Builder<?>> parsers() {
        Map<Type, PrimitiveParser.Builder<?>> parsers = ImmutableMap
                .<Type, PrimitiveParser.Builder<?>>builder()
                .put(DOUBLE, FloatParser.newBuilder())
                .put(FLOAT, FloatParser.newBuilder())
                .put(INT32, IdentityParser.newBuilder())
                .put(INT64, LongParser.newBuilder())
                .put(UINT32, IdentityParser.newBuilder())
                .put(UINT64, LongParser.newBuilder())
                .put(SINT32, IdentityParser.newBuilder())
                .put(SINT64, LongParser.newBuilder())
                .put(FIXED32, IdentityParser.newBuilder())
                .put(FIXED64, LongParser.newBuilder())
                .put(SFIXED32, IdentityParser.newBuilder())
                .put(SFIXED64, LongParser.newBuilder())
                .put(BOOL, IdentityParser.newBuilder())
                .put(STRING, IdentityParser.newBuilder())
                .put(BYTES, BytesParser.newBuilder())
                .build();
        return parsers;
    }
}
