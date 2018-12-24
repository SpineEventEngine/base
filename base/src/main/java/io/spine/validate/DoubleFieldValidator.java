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

import static java.lang.Math.abs;

/**
 * Validates fields of {@link Double} types.
 */
final class DoubleFieldValidator extends FloatFieldValidatorBase<Double> {

    /**
     * Creates a new validator instance.
     *
     * @param fieldValue
     *         the value to validate
     */
    DoubleFieldValidator(FieldValue fieldValue) {
        super(fieldValue);
    }

    /**
     * Creates a new validator instance.
     *
     * @param fieldValueChange
     *         the value to validate
     */
    DoubleFieldValidator(FieldValueChange fieldValueChange) {
        super(fieldValueChange);
    }

    @Override
    protected Double toNumber(String value) {
        Double min = Double.valueOf(value);
        return min;
    }

    @Override
    protected Double getAbs(Double value) {
        Double abs = abs(value);
        return abs;
    }
}
