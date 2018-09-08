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
import com.google.protobuf.Descriptors.FieldDescriptor.Type;
import io.spine.code.proto.FieldName;
import io.spine.tools.protojs.knowntypes.ParserMapGenerator;
import io.spine.type.TypeUrl;

import static com.google.protobuf.DescriptorProtos.FieldDescriptorProto.Label.LABEL_REPEATED;
import static com.google.protobuf.Descriptors.FieldDescriptor.Type.MESSAGE;

public final class Fields {

    @SuppressWarnings("DuplicateStringLiteralInspection")
    // Introducing common constant is not reasonable.
    private static final String ENTRY_SUFFIX = "Entry";

    private Fields() {
    }

    public static boolean isMessage(FieldDescriptor fieldDescriptor) {
        Type type = fieldDescriptor.getType();
        boolean isMessage = type == MESSAGE;
        return isMessage;
    }

    public static boolean isWellKnownType(FieldDescriptor fieldDescriptor) {
        Descriptor messageType = fieldDescriptor.getMessageType();
        TypeUrl typeUrl = TypeUrl.from(messageType);
        boolean isWellKnownType = ParserMapGenerator.hasParser(typeUrl);
        return isWellKnownType;
    }

    public static boolean isRepeated(FieldDescriptor fieldDescriptor) {
        FieldDescriptorProto proto = fieldDescriptor.toProto();
        boolean isRepeated = proto.getLabel() == LABEL_REPEATED && !isMap(fieldDescriptor);
        return isRepeated;
    }

    public static boolean isMap(FieldDescriptor fieldDescriptor) {
        FieldDescriptorProto descriptorProto = fieldDescriptor.toProto();
        if (descriptorProto.getLabel() != LABEL_REPEATED) {
            return false;
        }
        if (fieldDescriptor.getType() != MESSAGE) {
            return false;
        }
        Descriptor fieldType = fieldDescriptor.getMessageType();
        String mapTypeName = capitalizedName(fieldDescriptor) + ENTRY_SUFFIX;
        boolean isMap = fieldType.getName()
                                 .equals(mapTypeName);
        return isMap;
    }

    public static String capitalizedName(FieldDescriptor fieldDescriptor) {
        FieldDescriptorProto fieldDescriptorProto = fieldDescriptor.toProto();
        String capitalizedName = FieldName.of(fieldDescriptorProto)
                                          .toCamelCase();
        return capitalizedName;
    }
}
