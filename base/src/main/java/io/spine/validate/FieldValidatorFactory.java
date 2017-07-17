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

package io.spine.validate;

import com.google.common.collect.ImmutableMap;
import com.google.common.primitives.Primitives;
import com.google.protobuf.ByteString;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Descriptors.FieldDescriptor.JavaType;
import com.google.protobuf.Message;
import io.spine.base.FieldPath;

import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Predicates.notNull;
import static com.google.common.collect.Iterables.find;
import static java.lang.String.format;

/**
 * Creates {@link FieldValidator}s.
 *
 * @author Alexander Litus
 */
class FieldValidatorFactory {

    private static final Map<Class<?>, JavaType> SCALAR_FIELD_TYPES =
            ImmutableMap.<Class<?>, JavaType>builder().put(Integer.class, JavaType.INT)
                                                      .put(Long.class, JavaType.LONG)
                                                      .put(Float.class, JavaType.FLOAT)
                                                      .put(Double.class, JavaType.DOUBLE)
                                                      .put(String.class, JavaType.STRING)
                                                      .put(Boolean.class, JavaType.BOOLEAN)
                                                      .build();

    private FieldValidatorFactory() {
        // Prevent instantiation of this utility class.
    }

    /**
     * Creates a new validator instance according to the field type and validates the field.
     *
     * <p>The target field of the resulting validator is represented with a linear data structure,
     * i.e. not a map.
     *
     * @param descriptor    a descriptor of the field to validate
     * @param fieldValue    a value of the field to validate
     * @param rootFieldPath a path to the root field
     * @param strict        if {@code true} validators would always assume that the field is
     */
    private static FieldValidator<?> createForLinear(FieldDescriptor descriptor,
                                                     Object fieldValue,
                                                     FieldPath rootFieldPath,
                                                     boolean strict) {
        return createForLinear(descriptor.getJavaType(),
                               descriptor,
                               fieldValue,
                               rootFieldPath,
                               strict);
    }

    /**
     * Creates a new validator instance according to the field type and validates the field.
     *
     * <p>The target field of the resulting validator is represented with a linear data structure,
     * i.e. not a map.
     *
     * @param fieldType     the required field type
     * @param descriptor    a descriptor of the field to validate
     * @param fieldValue    a value of the field to validate
     * @param rootFieldPath a path to the root field
     * @param strict        if {@code true} validators would always assume that the field is
     *                      required
     */
    private static FieldValidator<?> createForLinear(JavaType fieldType,
                                                     FieldDescriptor descriptor,
                                                     Object fieldValue,
                                                     FieldPath rootFieldPath,
                                                     boolean strict) {
        checkNotNull(fieldType);
        switch (fieldType) {
            case MESSAGE:
                return new MessageFieldValidator(descriptor, fieldValue, rootFieldPath, strict);
            case INT:
                return new IntegerFieldValidator(descriptor, fieldValue, rootFieldPath);
            case LONG:
                return new LongFieldValidator(descriptor, fieldValue, rootFieldPath);
            case FLOAT:
                return new FloatFieldValidator(descriptor, fieldValue, rootFieldPath);
            case DOUBLE:
                return new DoubleFieldValidator(descriptor, fieldValue, rootFieldPath);
            case STRING:
                return new StringFieldValidator(descriptor, fieldValue, rootFieldPath, strict);
            case BYTE_STRING:
                return new ByteStringFieldValidator(descriptor, fieldValue, rootFieldPath);
            case BOOLEAN:
                return new BooleanFieldValidator(descriptor, fieldValue, rootFieldPath);
            case ENUM:
                return new EnumFieldValidator(descriptor, fieldValue, rootFieldPath);
            default:
                throw fieldTypeIsNotSupported(descriptor);
        }
    }

    /**
     * Creates a new validator instance for a map field.
     *
     * @param descriptor    a descriptor of the field to validate
     * @param value         a value of the field to validate
     * @param rootFieldPath a path to the root field
     * @param strict        if {@code true} validators would always assume that the field is
     *                      required
     */
    private static FieldValidator<?> createForMap(FieldDescriptor descriptor,
                                                  Map<?, ?> value,
                                                  FieldPath rootFieldPath,
                                                  boolean strict) {
        checkArgument(descriptor.isMapField(),
                      "Field %s is not a map field.",
                      descriptor.getFullName());
        if (value.isEmpty()) {
            return new EmptyMapFieldValidator(descriptor, rootFieldPath, strict);
        }
        final Object firstValue = find(value.values(), notNull());
        final Class<?> valueClass = firstValue.getClass();
        final Class<?> wrappedValueClass = Primitives.wrap(valueClass);
        JavaType type = SCALAR_FIELD_TYPES.get(wrappedValueClass);
        if (type == null) {
            if (ByteString.class.isAssignableFrom(valueClass)) {
                type = JavaType.BYTE_STRING;
            } else if (Message.class.isAssignableFrom(valueClass)) {
                type = JavaType.MESSAGE;
            } else if (Enum.class.isAssignableFrom(valueClass)) {
                type = JavaType.ENUM;
            } else {
                throw fieldTypeIsNotSupported(descriptor);
            }
        }
        final FieldValidator<?> validator = createForLinear(type,
                                                            descriptor,
                                                            value,
                                                            rootFieldPath,
                                                            strict);
        return validator;
    }

    static FieldValidator<?> create(FieldDescriptor descriptor,
                                    Object fieldValue,
                                    FieldPath rootFieldPath) {
        if (fieldValue instanceof Map) {
            return createForMap(descriptor, (Map<?, ?>) fieldValue, rootFieldPath, false);
        } else {
            return createForLinear(descriptor, fieldValue, rootFieldPath, false);
        }
    }

    static FieldValidator<?> createStrict(FieldDescriptor descriptor,
                                          Object fieldValue,
                                          FieldPath rootFieldPath) {
        if (fieldValue instanceof Map) {
            return createForMap(descriptor, (Map<?, ?>) fieldValue, rootFieldPath, true);
        } else {
            return createForLinear(descriptor, fieldValue, rootFieldPath, true);
        }
    }

    private static IllegalArgumentException fieldTypeIsNotSupported(FieldDescriptor descriptor) {
        final String msg = format("The field type is not supported for validation: %s",
                                  descriptor.getType());
        throw new IllegalArgumentException(msg);
    }
}
