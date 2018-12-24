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
 * Validates fields of {@link Long} number types.
 */
class LongFieldValidator extends NumberFieldValidator<Long> {

    /**
     * Creates a new validator instance.
     *
     * @param fieldValueChange
     *         the change of the field to validate
     */
    LongFieldValidator(FieldValueChange fieldValueChange) {
        super(fieldValueChange);
    }

    /**
     * Creates a new validator instance.
     *
     * @param fieldValue
     *         the value to validate
     */
    LongFieldValidator(FieldValue fieldValue) {
        super(fieldValue);
    }

    @Override
    protected Long toNumber(String value) {
        Long number = Long.valueOf(value);
        return number;
    }

    @Override
    protected Long getAbs(Long value) {
        Long abs = abs(value);
        return abs;
    }
}
