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

package io.spine.tools.fromjson.generator;

import com.google.common.collect.ImmutableMap;
import com.google.protobuf.Descriptors.EnumDescriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Descriptors.FieldDescriptor.Type;

import java.util.Map;

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
import static io.spine.util.Exceptions.newIllegalStateException;

final class PrimitiveParsers {

    private static final Map<Type, PrimitiveParser> parsers = parsers();

    private PrimitiveParsers() {
    }

    static PrimitiveParser getFor(FieldDescriptor fieldDescriptor) {
        Type type = fieldDescriptor.getType();
        if (type == ENUM) {
            return enumParserFor(fieldDescriptor);
        }
        PrimitiveParser parser = parsers.get(type);
        if (parser == null) {
            System.out.println(
                    "An attempt to get a parser for the unknown Primitive type: %s");
            return new IdentityParser();
        }
        return parser;
    }

    private static PrimitiveParser enumParserFor(FieldDescriptor fieldDescriptor) {
        EnumDescriptor enumType = fieldDescriptor.getEnumType();
        String enumTypeName = enumType.getFullName();

        // todo have separate static util for this.
        String typeWithProtoPrefix = "proto." + enumTypeName;
        return new EnumParser(typeWithProtoPrefix);
    }

    private static Map<Type, PrimitiveParser> parsers() {
        Map<Type, PrimitiveParser> parsers = ImmutableMap
                .<Type, PrimitiveParser>builder()
                .put(DOUBLE, new FloatValueParser())
                .put(FLOAT, new FloatValueParser())
                .put(INT32, new IdentityParser())
                .put(INT64, new LongValueParser())
                .put(UINT32, new IdentityParser())
                .put(UINT64, new LongValueParser())
                .put(SINT32, new IdentityParser())
                .put(SINT64, new LongValueParser())
                .put(FIXED32, new IdentityParser())
                .put(FIXED64, new LongValueParser())
                .put(SFIXED32, new IdentityParser())
                .put(SFIXED64, new LongValueParser())
                .put(BOOL, new IdentityParser())
                .put(STRING, new IdentityParser())
                .put(BYTES, new BytesParser())
                .build();
        return parsers;
    }
}
