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

package io.spine.tools.protojs.field;

import com.google.protobuf.DescriptorProtos.FieldDescriptorProto;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import io.spine.code.proto.FieldName;
import io.spine.tools.protojs.knowntypes.ParserMapGenerator;
import io.spine.type.TypeUrl;

import static com.google.protobuf.DescriptorProtos.FieldDescriptorProto.Label.LABEL_REPEATED;
import static com.google.protobuf.Descriptors.FieldDescriptor.Type.MESSAGE;

public final class Fields {

    @SuppressWarnings("DuplicateStringLiteralInspection") // Duplication with unrelated module.
    private static final String ENTRY_SUFFIX = "Entry";

    private Fields() {
    }

    public static boolean isMessage(FieldDescriptor field) {
        boolean isMessage = field.getType() == MESSAGE;
        return isMessage;
    }

    public static boolean isWellKnownType(FieldDescriptor field) {
        Descriptor message = field.getMessageType();
        TypeUrl typeUrl = TypeUrl.from(message);
        boolean isWellKnownType = ParserMapGenerator.hasParser(typeUrl);
        return isWellKnownType;
    }

    public static boolean isRepeated(FieldDescriptor field) {
        FieldDescriptorProto proto = field.toProto();
        boolean isRepeated = proto.getLabel() == LABEL_REPEATED && !isMap(field);
        return isRepeated;
    }

    public static boolean isMap(FieldDescriptor field) {
        FieldDescriptorProto proto = field.toProto();
        if (proto.getLabel() != LABEL_REPEATED) {
            return false;
        }
        if (field.getType() != MESSAGE) {
            return false;
        }
        Descriptor fieldType = field.getMessageType();
        String mapTypeName = capitalizedName(field) + ENTRY_SUFFIX;
        boolean isMap = fieldType.getName()
                                 .equals(mapTypeName);
        return isMap;
    }

    public static String capitalizedName(FieldDescriptor field) {
        FieldDescriptorProto proto = field.toProto();
        String capitalizedName = FieldName.of(proto)
                                          .toCamelCase();
        return capitalizedName;
    }
}
