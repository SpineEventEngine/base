/*
 * Copyright 2021, TeamDev. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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

package io.spine.test.tools.validate;

import io.spine.type.TypeName;
import io.spine.validate.ConstraintViolation;
import io.spine.validate.MessageWithConstraints;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.google.common.truth.Truth.assertThat;

@DisplayName("Number boundaries constraints should be compiled so that")
class NumberConstraintTest {

    @Test
    @DisplayName("min value is checked")
    void min() {
        assertViolation(
                InterestRate.newBuilder()
                            .setPercent(-3)
                            .buildPartial(),
                "greater than 0.0"
        );
        assertValid(
                InterestRate.newBuilder()
                            .setPercent(117.3F)
                            .build()
        );
    }

    @Test
    @DisplayName("min and max values are checked")
    void minMax() {
        assertViolation(
                Year.newBuilder()
                    .setDayCount(42)
                    .buildPartial(),
                "greater than or equal to 365"
        );
        assertViolation(
                Year.newBuilder()
                    .setDayCount(420)
                    .buildPartial(),
                "less than or equal to 366"
        );
        assertValid(
                Year.newBuilder()
                    .setDayCount(365)
                    .buildPartial()
        );
        assertValid(
                Year.newBuilder()
                    .setDayCount(366)
                    .buildPartial()
        );
    }

    @Test
    @DisplayName("numerical range is checked")
    void range() {
        String errorFragment = TypeName.of(Probability.class) + ".value";
        assertViolation(
                Probability.newBuilder()
                           .setValue(1.1)
                           .buildPartial(),
                errorFragment
        );
        assertViolation(
                Probability.newBuilder()
                           .setValue(-0.1)
                           .buildPartial(),
                errorFragment
        );
        assertValid(
                Probability.newBuilder()
                           .setValue(0.0)
                           .buildPartial()
        );
        assertValid(
                Probability.newBuilder()
                           .setValue(1.0)
                           .buildPartial()
        );
    }

    private static void assertViolation(MessageWithConstraints message, String error) {
        List<ConstraintViolation> violations = message.validate();
        assertThat(violations)
                .hasSize(1);
        assertThat(violations.get(0)
                             .getMsgFormat())
                .contains(error);
    }

    private static void assertValid(MessageWithConstraints message) {
        List<ConstraintViolation> violations = message.validate();
        assertThat(violations)
                .isEmpty();
    }
}
