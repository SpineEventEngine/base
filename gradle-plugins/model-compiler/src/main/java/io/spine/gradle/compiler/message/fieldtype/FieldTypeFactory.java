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
import com.google.protobuf.DescriptorProtos.FieldDescriptorProto.Type;
import com.squareup.javapoet.TypeName;

import java.util.AbstractMap;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.protobuf.DescriptorProtos.DescriptorProto;
import static io.spine.gradle.compiler.message.fieldtype.FieldTypes.getEntryNameFor;
import static io.spine.gradle.compiler.message.fieldtype.FieldTypes.isMap;
import static io.spine.gradle.compiler.message.fieldtype.FieldTypes.isRepeated;
import static io.spine.gradle.compiler.message.fieldtype.FieldTypes.trimTypeName;

/**
 * Factory for creation {@link FieldType} instances.
 *
 * @author Dmytro Grankin
 */
public class FieldTypeFactory {

    /** A map from Protobuf type name to Java class FQN. */
    private final Map<String, String> messageTypeMap;
    private final Iterable<DescriptorProto> messageNestedTypes;

    private static final String MAP_EXPECTED_ERROR_MESSAGE = "Map expected.";

    /**
     * Creates new instance.
     *
     * @param messageDescriptor the message descriptor to extract nested types
     * @param messageTypeMap    pre-scanned map with proto types and their appropriate Java classes
     */
    public FieldTypeFactory(DescriptorProto messageDescriptor, Map<String, String> messageTypeMap) {
        this.messageTypeMap = messageTypeMap;
        this.messageNestedTypes = messageDescriptor.getNestedTypeList();
    }

    /**
     * Creates a {@link FieldType} instances based on {@link FieldDescriptorProto}.
     *
     * @param field the proto field descriptor
     * @return the field type
     */
    public FieldType create(FieldDescriptorProto field) {
        if (isMap(field)) {
            return new MapFieldType(getEntryTypeNames(field));
        } else {
            final String fieldTypeName = getFieldTypeName(field);
            return isRepeated(field)
                   ? new RepeatedFieldType(fieldTypeName)
                   : new SingularFieldType(fieldTypeName);
        }
    }

    private String getFieldTypeName(FieldDescriptorProto field) {
        if (field.getType() == Type.TYPE_MESSAGE
                || field.getType() == Type.TYPE_ENUM) {
            final String typeName = trimTypeName(field);
            final String result = messageTypeMap.get(typeName);
            checkNotNull(result,
                         "Cannot find the field type name for %s of type %s",
                         typeName, field.getType());
            return result;
        } else {
            return ProtoScalarType.getJavaTypeName(field.getType());
        }
    }

    /**
     * Returns the key and the value type names for the map field
     * based on the passed nested types.
     *
     * @param map the field representing map
     * @return the entry containing the key and the value type names
     */
    private Map.Entry<TypeName, TypeName> getEntryTypeNames(FieldDescriptorProto map) {
        if (!isMap(map)) {
            throw new IllegalStateException(MAP_EXPECTED_ERROR_MESSAGE);
        }

        final int keyFieldIndex = 0;
        final int valueFieldIndex = 1;

        final DescriptorProto mapEntryDescriptor = getDescriptorForMapField(map);
        final TypeName keyTypeName =
                create(mapEntryDescriptor.getField(keyFieldIndex)).getTypeName();
        final TypeName valueTypeName =
                create(mapEntryDescriptor.getField(valueFieldIndex)).getTypeName();

        return new AbstractMap.SimpleEntry<>(keyTypeName, valueTypeName);
    }

    /**
     * Returns corresponding nested type descriptor for the map field.
     *
     * <p>Based on the fact that a message contains a nested type for
     * every map field. The nested type contains map entry description.
     *
     * @param mapField the field representing map
     * @return the nested type descriptor for map field
     */
    private DescriptorProto getDescriptorForMapField(FieldDescriptorProto mapField) {
        if (!isMap(mapField)) {
            throw new IllegalStateException(MAP_EXPECTED_ERROR_MESSAGE);
        }

        final String entryName = getEntryNameFor(mapField);
        for (DescriptorProto nestedType : messageNestedTypes) {
            if (nestedType.getName()
                          .equals(entryName)) {
                return nestedType;
            }
        }

        throw new IllegalStateException("Nested type for map field should be present.");
    }
}
