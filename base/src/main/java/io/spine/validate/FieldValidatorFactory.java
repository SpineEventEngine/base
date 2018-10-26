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

import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Descriptors.FieldDescriptor.JavaType;
import io.spine.code.proto.FieldTypes2;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;

/**
 * Creates {@link FieldValidator}s.
 */
class FieldValidatorFactory {

    private FieldValidatorFactory() {
        // Prevent instantiation of this utility class.
    }

    /**
     * Creates a new validator instance according to the field type and validates the field.
     *
     * <p>The target field of the resulting validator is represented with a linear data structure,
     * i.e. not a map.
     *
     * @param fieldContext
     *         the context of the field to validate
     * @param fieldValue
     *         a value of the field to validate
     * @param strict
     *         if {@code true} validators would always assume that the field is
     *         required
     */
    private static FieldValidator<?> createForLinear(FieldContext fieldContext,
                                                     FieldValue fieldValue,
                                                     boolean strict) {
        JavaType fieldType = fieldContext.getTarget()
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
     * @param fieldType
     *         the required field type
     * @param fieldContext
     *         the context of the field to create validator for
     * @param fieldValue
     *         a value of the field to validate
     * @param strict
     *         if {@code true} validators would always assume that the field is
     *         required
     */
    private static FieldValidator<?> createForLinear(JavaType fieldType,
                                                     FieldContext fieldContext,
                                                     Object fieldValue,
                                                     boolean strict) {
        checkNotNull(fieldType);
        FieldValue wrappedValue = FieldValue.of(fieldValue);
        switch (fieldType) {
            case MESSAGE:
                return new MessageFieldValidator(fieldContext, wrappedValue, strict);
            case INT:
                return new IntegerFieldValidator(fieldContext, wrappedValue);
            case LONG:
                return new LongFieldValidator(fieldContext, wrappedValue);
            case FLOAT:
                return new FloatFieldValidator(fieldContext, wrappedValue);
            case DOUBLE:
                return new DoubleFieldValidator(fieldContext, wrappedValue);
            case STRING:
                return new StringFieldValidator(fieldContext, wrappedValue, strict);
            case BYTE_STRING:
                return new ByteStringFieldValidator(fieldContext, wrappedValue);
            case BOOLEAN:
                return new BooleanFieldValidator(fieldContext, wrappedValue);
            case ENUM:
                return new EnumFieldValidator(fieldContext, wrappedValue);
            default:
                throw fieldTypeIsNotSupported(fieldContext.getTarget());
        }
    }

    /**
     * Creates a new validator instance for a map field.
     *
     * <p>In Protobuf, keys of a map is restricted to primitive types.
     * So, only values of a map are validated.
     *
     * @param fieldContext
     *         the context of the field to create validator for
     * @param value
     *         a value of the field to validate
     * @param strict
     *         if {@code true} validators would always assume that the field is required
     * @see <a href="https://developers.google.com/protocol-buffers/docs/proto3#maps">
     *         Protobuf Maps</a>
     */
    private static FieldValidator<?> createForMap(FieldContext fieldContext,
                                                  FieldValue value,
                                                  boolean strict) {
        FieldDescriptor descriptor = fieldContext.getTarget();
        checkArgument(descriptor.isMapField(),
                      "Field %s is not a map field.",
                      descriptor.getFullName());
        JavaType valuesType = FieldTypes2.valueDescriptor(fieldContext.getTarget())
                                         .getJavaType();
        FieldValidator<?> validator = createForLinear(valuesType,
                                                      fieldContext,
                                                      value,
                                                      strict);
        return validator;
    }

    static FieldValidator<?> create(FieldContext fieldContext,
                                    FieldValue fieldValue) {
        boolean isMap = FieldTypes2.isMap(fieldContext.getTarget());
        return isMap
               ? createForMap(fieldContext, fieldValue, false)
               : createForLinear(fieldContext, fieldValue, false);
    }

    static FieldValidator<?> createStrict(FieldContext fieldContext,
                                          FieldValue fieldValue) {
        boolean isMap = FieldTypes2.isMap(fieldContext.getTarget());
        return isMap
               ? createForMap(fieldContext, fieldValue, true)
               : createForLinear(fieldContext, fieldValue, true);
    }

    private static IllegalArgumentException fieldTypeIsNotSupported(FieldDescriptor descriptor) {
        String msg = format("The field type is not supported for validation: %s",
                            descriptor.getType());
        throw new IllegalArgumentException(msg);
    }
}
