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

import com.google.protobuf.Any;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.truth.extensions.proto.ProtoTruth.assertThat;
import static java.lang.Math.abs;
import static org.junit.jupiter.api.Assertions.assertEquals;

abstract class NumberFieldValidatorTest<V extends Number & Comparable<V>,
                                        T extends NumberFieldValidator<V>> {

    /** A stub value to pass as the first argument to constructor of validators. */
    static final FieldContext fieldContext = FieldContext.create(
            Any.getDescriptor()
               .getFields()
               .get(0)
    );

    private final V positiveValue;
    private final V negativeValue;
    private final T validator;

    NumberFieldValidatorTest(V positiveValue, V negativeValue, T validator) {
        checkArgument(positiveValue.doubleValue() > 0);
        checkArgument(negativeValue.doubleValue() < 0);
        checkArgument(Double.valueOf(positiveValue.doubleValue())
                      .equals(abs(negativeValue.doubleValue())),
                                  "positiveValue and negativeValue must be of the same size");

        this.positiveValue = positiveValue;
        this.negativeValue = negativeValue;
        this.validator = validator;
    }

    @Test
    @DisplayName("convert string to number")
    void stringToNumber() {
        assertEquals(positiveValue, validator.toNumber(positiveValue.toString()));
        assertEquals(negativeValue, validator.toNumber(negativeValue.toString()));
    }

    @Test
    @DisplayName("obtain absolute number value")
    void absolute() {
        assertEquals(positiveValue, validator.getAbs(negativeValue));
    }

    @Test
    @DisplayName("wrap and pack value to Any")
    void wrapToAny() {
        Any any = validator.wrap(positiveValue);
        assertThat(any).isNotEqualToDefaultInstance();
    }
}
