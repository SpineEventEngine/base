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

import com.google.protobuf.DescriptorProtos.FieldDescriptorProto;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Descriptors.FieldDescriptor.Type;
import io.spine.code.proto.FieldName;
import io.spine.tools.fromjson.js.JsWriter;
import io.spine.type.TypeUrl;

import static com.google.protobuf.DescriptorProtos.FieldDescriptorProto.Label.LABEL_REPEATED;
import static com.google.protobuf.Descriptors.FieldDescriptor.Type.MESSAGE;

final class FieldHandlers {

    private FieldHandlers() {
    }

    static FieldHandler createFor(FieldDescriptor fieldDescriptor, JsWriter jsWriter) {
        JsObjectAccessor jsObjectAccessor = FieldIterators.createFor(fieldDescriptor, jsWriter);
        FieldSetter fieldSetter = FieldSetters.createFor(fieldDescriptor, jsWriter);

        // todo refactor
        FieldDescriptor fieldValueDescriptor = isMap(fieldDescriptor)
                ? getValueDescriptor(fieldDescriptor)
                : fieldDescriptor;

        if (isMessage(fieldValueDescriptor)) {
            return isWellKnownType(fieldValueDescriptor)
                    ? new WellKnownFieldHandler(fieldValueDescriptor.getMessageType(),
                                                fieldDescriptor,
                                                jsObjectAccessor,
                                                fieldSetter,
                                                jsWriter)
                    : new MessageFieldHandler(fieldValueDescriptor.getMessageType(),
                                              fieldDescriptor,
                                              jsObjectAccessor,
                                              fieldSetter,
                                              jsWriter);
        }
        return new PrimitiveFieldHandler(fieldDescriptor, jsObjectAccessor, fieldSetter, jsWriter);
    }

    private static boolean isMessage(FieldDescriptor fieldDescriptor) {
        Type fieldKind = fieldDescriptor.getType();
        boolean isMessage = fieldKind == MESSAGE;
        return isMessage;
    }

    private static boolean isWellKnownType(FieldDescriptor fieldDescriptor) {
        Descriptor fieldType = fieldDescriptor.getMessageType();
        TypeUrl typeUrl = TypeUrl.from(fieldType);
        boolean isWellKnownType = KnownTypeParsersGenerator.WELL_KNOWN_TYPES.contains(typeUrl);
        return isWellKnownType;
    }

    private static boolean isMap(FieldDescriptor fieldDescriptor) {
        FieldDescriptorProto descriptorProto = fieldDescriptor.toProto();
        if (descriptorProto.getLabel() != LABEL_REPEATED) {
            return false;
        }
        if (fieldDescriptor.getType() != MESSAGE) {
            return false;
        }
        Descriptor fieldType = fieldDescriptor.getMessageType();
        String capitalizedName = capitalizedFieldName(fieldDescriptor);
        String supposedNameForMap = capitalizedName + "Entry";
        boolean isMap = fieldType.getName()
                                 .equals(supposedNameForMap);
        return isMap;
    }

    private static FieldDescriptor getValueDescriptor(FieldDescriptor fieldDescriptor) {
        FieldDescriptor valueDescriptor = fieldDescriptor.getMessageType()
                                                         .findFieldByName("value");
        return valueDescriptor;
    }

    private static String capitalizedFieldName(FieldDescriptor fieldDescriptor) {
        String name = fieldDescriptor.getName();
        FieldName fieldName = FieldName.of(name);
        String capitalizedFieldName = fieldName.toCamelCase();
        return capitalizedFieldName;
    }
}
