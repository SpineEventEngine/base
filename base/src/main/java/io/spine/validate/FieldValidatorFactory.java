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
     * @param fieldValue
     *         a value of the field to validate
     * @param strict
     *         if {@code true} validators would always assume that the field is
     */
    private static FieldValidator<?> createForLinear(FieldValue fieldValue,
                                                     boolean strict) {
        JavaType fieldType = fieldValue.context()
                                       .getTarget()
                                       .getJavaType();
        return createForLinear(fieldType,
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
     * @param fieldValue
     *         a value of the field to validate
     * @param strict
     *         if {@code true} validators would always assume that the field is
     */
    private static FieldValidator<?> createForLinear(JavaType fieldType,
                                                     FieldValue fieldValue,
                                                     boolean strict) {
        checkNotNull(fieldType);
        switch (fieldType) {
            case MESSAGE:
                return new MessageFieldValidator(fieldValue, strict);
            case INT:
                return new IntegerFieldValidator(fieldValue);
            case LONG:
                return new LongFieldValidator(fieldValue);
            case FLOAT:
                return new FloatFieldValidator(fieldValue);
            case DOUBLE:
                return new DoubleFieldValidator(fieldValue);
            case STRING:
                return new StringFieldValidator(fieldValue, strict);
            case BYTE_STRING:
                return new ByteStringFieldValidator(fieldValue);
            case BOOLEAN:
                return new BooleanFieldValidator(fieldValue);
            case ENUM:
                return new EnumFieldValidator(fieldValue);
            default:
                throw fieldTypeIsNotSupported(fieldValue.context()
                                                        .getTarget());
        }
    }

    /**
     * Creates a new validator instance for a map field.
     *
     * <p>In Protobuf, keys of a map is restricted to primitive types.
     * So, only values of a map are validated.
     *
     * @param value
     *         a value of the field to validate
     * @param strict
     *         if {@code true} validators would always assume that the field is required
     * @see <a href="https://developers.google.com/protocol-buffers/docs/proto3#maps">
     *         Protobuf Maps</a>
     */
    private static FieldValidator<?> createForMap(FieldValue value,
                                                  boolean strict) {
        JavaType valuesType = FieldTypes2.valueDescriptor(value.context()
                                                               .getTarget())
                                         .getJavaType();
        FieldValidator<?> validator = createForLinear(valuesType,
                                                      value,
                                                      strict);
        return validator;
    }

    static FieldValidator<?> create(FieldValue fieldValue) {
        return fieldValue.isMap()
               ? createForMap(fieldValue, false)
               : createForLinear(fieldValue, false);
    }

    static FieldValidator<?> createStrict(FieldValue fieldValue) {
        return fieldValue.isMap()
               ? createForMap(fieldValue, true)
               : createForLinear(fieldValue, true);
    }

    private static IllegalArgumentException fieldTypeIsNotSupported(FieldDescriptor descriptor) {
        String msg = format("The field type is not supported for validation: %s",
                            descriptor.getType());
        throw new IllegalArgumentException(msg);
    }
}
