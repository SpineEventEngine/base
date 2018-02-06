/*
 * Copyright 2017, TeamDev Ltd. All rights reserved.
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

package io.spine.gradle.compiler.message.fieldtype;

import com.google.protobuf.DescriptorProtos.FieldDescriptorProto;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Utility class for working with types of the Protobuf fields.
 *
 * @author Illia Shepilov
 */
public class FieldTypes {

    public static final String ENTRY_SUFFIX = "Entry";

    /** Prevents instantiation of this utility class .*/
    private FieldTypes() {}

    /**
     * Checks the Protobuf field and determines it is repeated field or not.
     *
     * @param field the descriptor of the field to check
     * @return {@code true} if field is repeated, {@code false} otherwise
     */
    public static boolean isRepeated(FieldDescriptorProto field) {
        checkNotNull(field);
        final boolean result = field.getLabel() == FieldDescriptorProto.Label.LABEL_REPEATED;
        return result;
    }

    /**
     * Checks the Protobuf field and determines it is map field or not.
     *
     * @param field the descriptor of the field to check
     * @return {@code true} if field is map, {@code false} otherwise
     */
    public static boolean isMap(FieldDescriptorProto field) {
        checkNotNull(field);
        final boolean result = field.getTypeName()
                                    .endsWith('.' + getEntryNameFor(field));
        return result;
    }

    /**
     * Constructs the entry name for the map field.
     *
     * <p>For example, proto field with name 'word_dictionary' has 'wordDictionary' json name.
     * Every map field has corresponding entry type.
     * For 'word_dictionary' it would be 'WordDictionaryEntry'
     *
     * @param mapField the field to construct entry name
     * @return the name of the map field
     */
    @SuppressWarnings("DuplicateStringLiteralInspection")
    // It cannot be used as the constant across the project.
    // Although it has the equivalent literal they have the different meaning.
    public static String getEntryNameFor(FieldDescriptorProto mapField) {
        checkNotNull(mapField);

        final String jsonName = mapField.getJsonName();
        final char capitalizedFirstSymbol = Character.toUpperCase(jsonName.charAt(0));
        final String remainingPart = jsonName.substring(1);

        return capitalizedFirstSymbol + remainingPart + ENTRY_SUFFIX;
    }

    /**
     * Checks the Protobuf field and determines it is message type or not.
     *
     * @param fieldDescriptor the descriptor of the field to check
     * @return {@code true} if it is message, {@code false} otherwise
     */
    public static boolean isMessage(FieldDescriptorProto fieldDescriptor) {
        checkNotNull(fieldDescriptor);
        return fieldDescriptor.getType() == FieldDescriptorProto.Type.TYPE_MESSAGE;
    }

    /**
     * Removes the leading dot from the Protobuf type name passed as {@code String}.
     *
     * <p>If there is no leading dots, returns the unmodified parameter.
     *
     * @param fieldDescriptor the field descriptor whose type name to modify
     * @return the type name without leading dot
     */
    public static String trimTypeName(FieldDescriptorProto fieldDescriptor) {
        final String typeName = fieldDescriptor.getTypeName();
        checkNotNull(typeName);

        if (typeName.isEmpty()) {
            return typeName;
        }
        // it has a redundant dot in the beginning
        if (typeName.charAt(0) == '.') {
            return typeName.substring(1);
        }
        return typeName;
    }
}
