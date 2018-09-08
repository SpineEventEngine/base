/*
 * Copyright 2018, TeamDev. All rights reserved.
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

package io.spine.tools.protojs.code.primitive.parser;

import com.google.common.collect.ImmutableMap;
import com.google.protobuf.Descriptors.EnumDescriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Descriptors.FieldDescriptor.Type;
import io.spine.tools.protojs.code.JsGenerator;

import java.util.Map;

import static com.google.common.base.Preconditions.checkState;
import static com.google.protobuf.Descriptors.FieldDescriptor.Type.BOOL;
import static com.google.protobuf.Descriptors.FieldDescriptor.Type.BYTES;
import static com.google.protobuf.Descriptors.FieldDescriptor.Type.DOUBLE;
import static com.google.protobuf.Descriptors.FieldDescriptor.Type.ENUM;
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
import static io.spine.tools.protojs.types.Types.typeWithProtoPrefix;

public final class PrimitiveParsers {

    private static final Map<Type, PrimitiveParser.Builder> parsers = parsers();

    private PrimitiveParsers() {
    }

    public static PrimitiveParser createFor(FieldDescriptor field, JsGenerator jsGenerator) {
        Type type = field.getType();
        if (type == ENUM) {
            return enumParser(field, jsGenerator);
        }
        return primitiveParser(type, jsGenerator);
    }

    private static PrimitiveParser enumParser(FieldDescriptor field, JsGenerator jsGenerator) {
        EnumDescriptor enumType = field.getEnumType();
        String typeName = typeWithProtoPrefix(enumType);
        PrimitiveParser parser = EnumParser
                .newBuilder()
                .setEnumType(typeName)
                .setJsWriter(jsGenerator)
                .build();
        return parser;
    }

    private static PrimitiveParser primitiveParser(Type type, JsGenerator jsGenerator) {
        PrimitiveParser.Builder parserBuilder = parsers.get(type);
        checkState(parsers.containsKey(type),
                   "An attempt to get a parser for the unknown Primitive type: %s", type);
        PrimitiveParser parser = parserBuilder
                .setJsWriter(jsGenerator)
                .build();
        return parser;
    }

    private static Map<Type, PrimitiveParser.Builder> parsers() {
        Map<Type, PrimitiveParser.Builder> parsers = ImmutableMap
                .<Type, PrimitiveParser.Builder>builder()
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
