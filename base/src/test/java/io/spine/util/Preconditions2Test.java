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

package io.spine.util;

import com.google.protobuf.Message;
import com.google.protobuf.StringValue;
import io.spine.testing.TestValues;
import io.spine.testing.UtilityClassTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;
import static io.spine.testing.Assertions.assertIllegalArgument;
import static io.spine.testing.Assertions.assertIllegalState;
import static io.spine.testing.Assertions.assertNpe;
import static io.spine.testing.TestValues.newUuidValue;
import static io.spine.testing.TestValues.randomString;
import static io.spine.util.Preconditions2.checkBounds;
import static io.spine.util.Preconditions2.checkNotDefaultArg;
import static io.spine.util.Preconditions2.checkNotDefaultState;
import static io.spine.util.Preconditions2.checkNotEmptyOrBlank;
import static io.spine.util.Preconditions2.checkPositive;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("`Preconditions2` utility class should")
class Preconditions2Test extends UtilityClassTest<Preconditions2> {

    Preconditions2Test() {
        super(Preconditions2.class);
    }

    @Nested
    @DisplayName("Check that a `String` is")
    class StringArg {

        private void assertThrowsOn(String arg) {
            assertIllegalArgument(() -> checkNotEmptyOrBlank(arg));
        }

        private void assertThrowsWithMessage(String arg, String errorMessage) {
            var exception = assertIllegalArgument(() -> checkNotEmptyOrBlank(arg, errorMessage));
            assertThat(exception).hasMessageThat()
                                 .contains(errorMessage);
        }

        @Test
        @DisplayName("null")
        void nullStr() {
            assertNpe(() -> checkNotEmptyOrBlank(null));

            var errorTemplateBase = randomString();
            var errorArg = new String[]{randomString(), randomString()};
            var errorTemplate = errorTemplateBase + "%s %s";

            var exception = assertThrows(
                    NullPointerException.class,
                    () -> checkNotEmptyOrBlank(null, errorTemplate, errorArg[0], errorArg[1])
            );

            var assertExceptionMessage = assertThat(exception).hasMessageThat();
            assertExceptionMessage.contains(errorTemplateBase);
            assertExceptionMessage.contains(errorArg[0]);
            assertExceptionMessage.contains(errorArg[1]);
        }

        @Test
        @DisplayName("empty")
        void emptyString() {
            assertThrowsOn("");
            assertThrowsWithMessage("", randomString());
        }

        @Test
        @DisplayName("blank")
        void blankString() {
            assertThrowsOn(" ");
            assertThrowsWithMessage(" ", randomString());

            assertThrowsOn("  ");
            assertThrowsWithMessage("  ", randomString());

            assertThrowsOn("   ");
            assertThrowsWithMessage("   ", randomString());
        }
    }

    @Nested
    @DisplayName("check that a value is positive")
    class PositiveValue {

        private void assertThrowsOn(long value) {
            var exception =
                    assertThrows(IllegalArgumentException.class,
                                 () -> checkPositive(value));
            assertThat(exception)
                    .hasMessageThat()
                    .contains(String.valueOf(value));
        }

        private void assertThrowsWithMessage(long value, String errorMessage) {
            var exception =
                    assertThrows(IllegalArgumentException.class,
                                 () -> checkPositive(value, errorMessage));
            assertThat(exception).hasMessageThat()
                                 .contains(errorMessage);
        }

        @Test
        @DisplayName("rejecting zero")
        void zero() {
            assertThrowsOn(0);
            assertThrowsWithMessage(0, randomString());
        }

        @Test
        @DisplayName("rejecting negative values")
        void negative() {
            assertThrowsOn(-1);
            assertThrowsWithMessage(-100, randomString());
        }

        @Test
        @DisplayName("accepting and returning it")
        void positive() {
            var expected = TestValues.longRandom(1, 100_000);
            assertThat(checkPositive(expected))
                    .isEqualTo(expected);
        }
    }

    @Test
    @DisplayName("throw if checked value out of bounds")
    void throwExceptionIfCheckedValueOutOfBounds() {
        assertIllegalArgument(() -> checkBounds(10, "checked value", -5, 9));
    }

    @Nested
    @DisplayName("check that a message is not in the default state")
    class NotDefaultMessage {

        private final Message defaultValue = StringValue.getDefaultInstance();
        private String customErrorMessage;

        @BeforeEach
        void createCustomErrorMessage() {
            customErrorMessage = randomString();
        }

        @Test
        @DisplayName("throwing `IllegalStateException` for state transition checks")
        void stateChecking() {
            assertIllegalState(() -> checkNotDefaultState(defaultValue));

            var exception =
                    assertThrows(IllegalStateException.class,
                                 () -> checkNotDefaultState(defaultValue, customErrorMessage));
            assertThat(exception).hasMessageThat()
                                 .contains(customErrorMessage);
        }

        @Test
        @DisplayName("throwing `IllegalArgumentException` for argument checks")
        void argumentChecking() {
            assertIllegalArgument(() -> checkNotDefaultArg(defaultValue));
            var exception =
                    assertThrows(IllegalArgumentException.class,
                                 () -> checkNotDefaultArg(defaultValue, customErrorMessage));
            assertThat(exception)
                    .hasMessageThat()
                    .contains(customErrorMessage);
        }

        @Test
        @DisplayName("return non-default value on check")
        void returnValue() {
            var nonDefault = newUuidValue();
            assertEquals(nonDefault, checkNotDefaultArg(nonDefault));
            assertEquals(nonDefault, checkNotDefaultArg(nonDefault, customErrorMessage));
            assertEquals(nonDefault, checkNotDefaultState(nonDefault));
            assertEquals(nonDefault, checkNotDefaultState(nonDefault, customErrorMessage));
        }
    }
}
