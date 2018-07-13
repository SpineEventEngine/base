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

import com.google.common.collect.ImmutableList;

/**
 * A base for floating point number field validators.
 *
 * @author Alexander Litus
 */
abstract class FloatFieldValidatorBase<V extends Number & Comparable<V>>
         extends NumberFieldValidator<V> {

    private static final String INVALID_ID_TYPE_MSG =
                                "Entity ID field must not be a floating point number.";

    /**
     * Creates a new validator instance.
     *
     * @param fieldContext the context of the field to validate
     * @param fieldValues  values to validate
     */
    protected FloatFieldValidatorBase(FieldContext fieldContext,
                                      ImmutableList<V> fieldValues) {
        super(fieldContext, fieldValues);
    }

    @Override
    @SuppressWarnings("RefusedBequest")
    protected void validateEntityId() {
        V value = getValues().get(0);
        ConstraintViolation violation = ConstraintViolation.newBuilder()
                                                                 .setMsgFormat(INVALID_ID_TYPE_MSG)
                                                                 .setFieldPath(getFieldPath())
                                                                 .setFieldValue(wrap(value))
                                                                 .build();
        addViolation(violation);
    }
}
