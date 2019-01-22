/*
 * Copyright 2019, TeamDev. All rights reserved.
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

/**
 * A base for floating point number field validators.
 */
abstract class FloatFieldValidatorBase<V extends Number & Comparable<V>>
        extends NumberFieldValidator<V> {

    /**
     * Creates a new validator instance.
     *
     * @param fieldValue
     *         the value to validate
     */
    FloatFieldValidatorBase(FieldValue<V> fieldValue) {
        super(fieldValue);
    }

    @Override
    @SuppressWarnings("RefusedBequest")
    protected void validateEntityId() {
        V value = getValues().get(0);
        ConstraintViolation violation = ConstraintViolation
                .newBuilder()
                .setMsgFormat("Entity ID field `%s` must not be a floating point number.")
                .addParam(field().descriptor()
                                 .getFullName())
                .setFieldPath(getFieldPath())
                .setFieldValue(wrap(value))
                .build();
        addViolation(violation);
    }
}
