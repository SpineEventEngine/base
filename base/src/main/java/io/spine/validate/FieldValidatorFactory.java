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

import com.google.protobuf.Descriptors.FieldDescriptor.JavaType;

import static java.lang.String.format;

/**
 * Creates {@link FieldValidator}s.
 */
class FieldValidatorFactory {

    private FieldValidatorFactory() {
        // Prevent instantiation of this utility class.
    }

    /**
     * Creates a new instance of a validator.
     *
     * <p>The exact type of the validator depends on the type of the field, the change of which
     * is being validated.
     *
     * @param change
     *         a change of the field value to validate
     */
    static FieldValidator<?> create(FieldValueChange change) {
        return create(change, false);
    }

    /**
     * Creates a new instance of a validator, that assumes that the field that is being validated
     * is required.
     *
     * <p>The exact type of the validator depends on the type of the field, the change of which
     * is being validated.
     *
     * @param change
     *         a change of the field value to validate
     */
    static FieldValidator<?> createStrict(FieldValueChange change) {
        return create(change, true);
    }

    /**
     * Creates a new instance of a validator.
     *
     * <p>The exact type fo the validator depends o the type of the field that is being validated.
     *
     * @param value
     *         the value of the field to validate
     */
    static FieldValidator<?> create(FieldValue value) {
        FieldValueChange change = FieldValueChange.firstValueEver(value);
        return create(change, false);
    }

    /**
     * Creates a new instance of a validator, that assumes that the field that is being validated
     * is required.
     *
     * <p>The exact type of the validator depends on the type of the field, the change of which
     * is being validated.
     *
     * @param value the value of the field to validate
     *
     */
    static FieldValidator<?> createStrict(FieldValue value) {
        FieldValueChange change = FieldValueChange.firstValueEver(value);
        return create(change, true);
    }

    /**
     * Creates a new validator instance according to the type of the value.
     *
     * @param change
     *         a value of the field to validate
     * @param strict
     *         if {@code true} validators would always assume that the field is
     */
    private static FieldValidator<?> create(FieldValueChange change, boolean strict) {
        JavaType fieldType = change.newValue()
                                   .javaType();
        switch (fieldType) {
            case MESSAGE:
                return new MessageFieldValidator(change, strict);
            case INT:
                return new IntegerFieldValidator(change);
            case LONG:
                return new LongFieldValidator(change);
            case FLOAT:
                return new FloatFieldValidator(change);
            case DOUBLE:
                return new DoubleFieldValidator(change);
            case STRING:
                return new StringFieldValidator(change, strict);
            case BYTE_STRING:
                return new ByteStringFieldValidator(change);
            case BOOLEAN:
                return new BooleanFieldValidator(change);
            case ENUM:
                return new EnumFieldValidator(change);
            default:
                throw fieldTypeIsNotSupported(fieldType);
        }
    }

    private static IllegalArgumentException fieldTypeIsNotSupported(JavaType type) {
        String msg = format("The field type is not supported for validation: %s", type);
        throw new IllegalArgumentException(msg);
    }
}
