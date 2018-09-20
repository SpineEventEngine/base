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

package io.spine.code.js;

import com.google.protobuf.DescriptorProtos.FieldDescriptorProto;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.protobuf.DescriptorProtos.FieldDescriptorProto.Label.LABEL_REPEATED;
import static com.google.protobuf.Descriptors.FieldDescriptor.Type.ENUM;
import static com.google.protobuf.Descriptors.FieldDescriptor.Type.MESSAGE;

/**
 * A utility to work with Protobuf field {@linkplain FieldDescriptor descriptors}.
 *
 * @author Dmytro Kuzmin
 */
@SuppressWarnings("DuplicateStringLiteralInspection") // Duplication with unrelated modules.
public final class FieldDescriptors {

    /**
     * The suffix of the class representing the {@code message} type of the {@code map} field.
     */
    private static final String ENTRY_SUFFIX = "Entry";

    /**
     * The field of the {@code map} message type which represents the {@code map} key.
     */
    private static final String MAP_ENTRY_KEY = "key";

    /**
     * The field of the {@code map} message type which represents the {@code map} value.
     */
    private static final String MAP_ENTRY_VALUE = "value";

    /** Prevents instantiation of this utility class. */
    private FieldDescriptors() {
    }

    /**
     * Checks if the given field is of {@code message} type.
     *
     * @param fieldDescriptor
     *         the descriptor of the field to check
     * @return {@code true} if the field is of {@code message} type, {@code false} otherwise
     */
    public static boolean isMessage(FieldDescriptor fieldDescriptor) {
        checkNotNull(fieldDescriptor);
        boolean isMessage = fieldDescriptor.getType() == MESSAGE;
        return isMessage;
    }

    /**
     * Checks if the given field is of {@code enum} type.
     *
     * @param fieldDescriptor
     *         the descriptor of the field to check
     * @return {@code true} if the field is of {@code enum} type, {@code false} otherwise
     */
    public static boolean isEnum(FieldDescriptor fieldDescriptor) {
        checkNotNull(fieldDescriptor);
        boolean isMessage = fieldDescriptor.getType() == ENUM;
        return isMessage;
    }

    /**
     * Checks if the given field is a {@code repeated} proto field.
     *
     * <p>Although {@code map} fields technically count as {@code repeated}, this method will
     * return {@code false} for them.
     *
     * @param fieldDescriptor
     *         the descriptor of the field to check
     * @return {@code true} if the field is a {@code repeated} proto field, {@code false} otherwise
     */
    public static boolean isRepeated(FieldDescriptor fieldDescriptor) {
        checkNotNull(fieldDescriptor);
        FieldDescriptorProto proto = fieldDescriptor.toProto();
        boolean isRepeated = proto.getLabel() == LABEL_REPEATED && !isMap(fieldDescriptor);
        return isRepeated;
    }

    /**
     * Checks if the given field is a {@code map} proto field.
     *
     * @param fieldDescriptor
     *         the descriptor of the field to check
     * @return {@code true} if the field is a {@code map} proto field and {@code false} otherwise
     */
    public static boolean isMap(FieldDescriptor fieldDescriptor) {
        checkNotNull(fieldDescriptor);
        FieldDescriptorProto proto = fieldDescriptor.toProto();
        if (proto.getLabel() != LABEL_REPEATED) {
            return false;
        }
        if (fieldDescriptor.getType() != MESSAGE) {
            return false;
        }
        Descriptor fieldType = fieldDescriptor.getMessageType();
        String mapTypeName = FieldName.from(fieldDescriptor) + ENTRY_SUFFIX;
        boolean isMap = fieldType.getName()
                                 .equals(mapTypeName);
        return isMap;
    }

    /**
     * Obtains the key descriptor for the {@code map} field.
     *
     * @param fieldDescriptor
     *         the {@code map} field for which to obtain key descriptor
     * @return the key descriptor for the specified {@code map} field
     * @throws IllegalStateException
     *         if the specified field is not a {@code map} proto field
     */
    public static FieldDescriptor keyDescriptor(FieldDescriptor fieldDescriptor) {
        checkArgument(isMap(fieldDescriptor),
                      "Trying to get key descriptor for the non-map field %s.",
                      fieldDescriptor.getName());
        FieldDescriptor descriptor = fieldDescriptor.getMessageType()
                                          .findFieldByName(MAP_ENTRY_KEY);
        return descriptor;
    }

    /**
     * Obtains the value descriptor for the {@code map} field.
     *
     * @param fieldDescriptor
     *         the {@code map} field for which to obtain value descriptor
     * @return the value descriptor for the specified {@code map} field
     * @throws IllegalStateException
     *         if the specified field is not a {@code map} proto field
     */
    public static FieldDescriptor valueDescriptor(FieldDescriptor fieldDescriptor) {
        checkArgument(isMap(fieldDescriptor),
                      "Trying to get value descriptor for the non-map field %s.",
                      fieldDescriptor.getName());
        FieldDescriptor descriptor = fieldDescriptor.getMessageType()
                                                    .findFieldByName(MAP_ENTRY_VALUE);
        return descriptor;
    }
}
