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

package io.spine.tools.protojs.field.parser;

import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import io.spine.tools.protojs.code.JsWriter;
import io.spine.tools.protojs.knowntypes.ParserMapGenerator;
import io.spine.type.TypeUrl;

import static com.google.protobuf.Descriptors.FieldDescriptor.Type.MESSAGE;

public final class FieldValueParsers {

    private FieldValueParsers() {
    }

    public static FieldValueParser createFor(FieldDescriptor fieldDescriptor, JsWriter jsWriter) {
        if (isMessage(fieldDescriptor)) {
            return isWellKnownType(fieldDescriptor)
                    ? new WellKnownFieldParser(fieldDescriptor, jsWriter)
                    : new MessageFieldParser(fieldDescriptor, jsWriter);
        }
        return new PrimitiveFieldParser(fieldDescriptor, jsWriter);
    }

    private static boolean isMessage(FieldDescriptor fieldDescriptor) {
        FieldDescriptor.Type fieldKind = fieldDescriptor.getType();
        boolean isMessage = fieldKind == MESSAGE;
        return isMessage;
    }

    private static boolean isWellKnownType(FieldDescriptor fieldDescriptor) {
        Descriptor fieldType = fieldDescriptor.getMessageType();
        TypeUrl typeUrl = TypeUrl.from(fieldType);
        boolean isWellKnownType = ParserMapGenerator.JS_PARSER_NAMES.containsKey(typeUrl);
        return isWellKnownType;
    }
}
