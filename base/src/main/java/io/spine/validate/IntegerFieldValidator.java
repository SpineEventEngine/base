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

import static java.lang.Math.abs;

/**
 * Validates fields of {@link Integer} types.
 *
 * @author Alexander Litus
 */
class IntegerFieldValidator extends NumberFieldValidator<Integer> {

    /**
     * Creates a new validator instance.
     *
     * @param descriptorPath the descriptor path
     * @param fieldValues    values to validate
     */
    IntegerFieldValidator(DescriptorPath descriptorPath, Object fieldValues) {
        super(descriptorPath, FieldValidator.<Integer>toValueList(fieldValues));
    }

    @Override
    protected Integer toNumber(String value) {
        final Integer number = Integer.valueOf(value);
        return number;
    }

    @Override
    protected Integer getAbs(Integer value) {
        final Integer abs = abs(value);
        return abs;
    }
}
