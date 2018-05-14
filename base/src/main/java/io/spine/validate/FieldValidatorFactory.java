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

package io.spine.validate;

import com.google.common.collect.ImmutableMap;
import com.google.common.primitives.Primitives;
import com.google.protobuf.ByteString;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Descriptors.FieldDescriptor.JavaType;
import com.google.protobuf.Message;

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
     * @param fieldContext the context of the field to validate
     * @param fieldValue   a value of the field to validate
     * @param strict       if {@code true} validators would always assume that the field is
     *                     required
     */
    private static FieldValidator<?> createForLinear(FieldContext fieldContext,
                                                     Object fieldValue,
                                                     boolean strict) {
        final JavaType fieldType = fieldContext.getTarget()
                                               .getJavaType();
        return createForLinear(fieldType,
                               fieldContext,
                               fieldValue,
                               strict);
    }

    /**
     * Creates a new validator instance according to the field type and validates the field.
     *
     * <p>The target field of the resulting validator is represented with a linear data structure,
     * i.e. not a map.
     *
     * @param fieldType    the required field type
     * @param fieldContext the context of the field to create validator for
     * @param fieldValue   a value of the field to validate
     * @param strict       if {@code true} validators would always assume that the field is
     *                     required
     */
    private static FieldValidator<?> createForLinear(JavaType fieldType,
                                                     FieldContext fieldContext,
                                                     Object fieldValue,
                                                     boolean strict) {
        checkNotNull(fieldType);
        switch (fieldType) {
            case MESSAGE:
                return new MessageFieldValidator(fieldContext, fieldValue, strict);
            case INT:
                return new IntegerFieldValidator(fieldContext, fieldValue);
            case LONG:
                return new LongFieldValidator(fieldContext, fieldValue);
            case FLOAT:
                return new FloatFieldValidator(fieldContext, fieldValue);
            case DOUBLE:
                return new DoubleFieldValidator(fieldContext, fieldValue);
            case STRING:
                return new StringFieldValidator(fieldContext, fieldValue, strict);
            case BYTE_STRING:
                return new ByteStringFieldValidator(fieldContext, fieldValue);
            case BOOLEAN:
                return new BooleanFieldValidator(fieldContext, fieldValue);
            case ENUM:
                return new EnumFieldValidator(fieldContext, fieldValue);
            default:
                throw fieldTypeIsNotSupported(fieldContext.getTarget());
        }
    }

    /**
     * Creates a new validator instance for a map field.
     *
     * @param fieldContext the context of the field to create validator for
     * @param value        a value of the field to validate
     * @param strict       if {@code true} validators would always assume that the field is
     *                     required
     */
    private static FieldValidator<?> createForMap(FieldContext fieldContext,
                                                  Map<?, ?> value,
                                                  boolean strict) {
        final FieldDescriptor descriptor = fieldContext.getTarget();
        checkArgument(descriptor.isMapField(),
                      "Field %s is not a map field.",
                      descriptor.getFullName());
        if (value.isEmpty()) {
            return new EmptyMapFieldValidator(fieldContext, strict);
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
                                                            fieldContext,
                                                            value,
                                                            strict);
        return validator;
    }

    static FieldValidator<?> create(FieldContext fieldContext,
                                    Object fieldValue) {
        return fieldValue instanceof Map
                ? createForMap(fieldContext, (Map<?, ?>) fieldValue, false)
                : createForLinear(fieldContext, fieldValue, false);
    }

    static FieldValidator<?> createStrict(FieldContext fieldContext,
                                          Object fieldValue) {
        return fieldValue instanceof Map
                ? createForMap(fieldContext, (Map<?, ?>) fieldValue, true)
                : createForLinear(fieldContext, fieldValue, true);
    }

    private static IllegalArgumentException fieldTypeIsNotSupported(FieldDescriptor descriptor) {
        final String msg = format("The field type is not supported for validation: %s",
                                  descriptor.getType());
        throw new IllegalArgumentException(msg);
    }
}
