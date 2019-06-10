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

package io.spine.validate.option;

import com.google.protobuf.Message;
import io.spine.test.validate.DigitsCountNumberFieldValue;
import io.spine.test.validate.IntegerDigits;
import io.spine.test.validate.IntegerDigitsWithExternalConstraint;
import io.spine.validate.MessageValidatorTest;
import io.spine.validate.given.MessageValidatorTestEnv;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.spine.validate.MessageValidatorTest.MESSAGE_VALIDATOR_SHOULD;
import static io.spine.validate.given.MessageValidatorTestEnv.INT_DIGIT_COUNT_EQUAL_MAX;
import static io.spine.validate.given.MessageValidatorTestEnv.INT_DIGIT_COUNT_GREATER_THAN_MAX;
import static io.spine.validate.given.MessageValidatorTestEnv.VALUE;

@DisplayName(MESSAGE_VALIDATOR_SHOULD + "analyze (digits) option and")
class DigitsTest extends MessageValidatorTest {

    private static final double FRACTIONAL_DIGIT_COUNT_GREATER_THAN_MAX = 1.123;
    private static final double FRACTIONAL_DIGIT_COUNT_EQUAL_MAX = 1.12;
    private static final double FRACTIONAL_DIGIT_COUNT_LESS_THAN_MAX = 1.0;

    @Test
    @DisplayName("find out that integral digit count is greater than max")
    void findOutThatIntegralDigitCountIsGreaterThanMax() {
        Message msg = messageFor(INT_DIGIT_COUNT_GREATER_THAN_MAX);
        assertNotValid(msg);
    }

    @Test
    @DisplayName("find out that integral digits count is equal to max")
    void findOutThatIntegralDigitsCountIsEqualToMax() {
        Message msg = messageFor(INT_DIGIT_COUNT_EQUAL_MAX);
        assertValid(msg);
    }

    @Test
    @DisplayName("find out that integral digit count is less than max")
    void findOutThatIntegralDigitCountIsLessThanMax() {
        Message msg = messageFor(MessageValidatorTestEnv.INT_DIGIT_COUNT_LESS_THAN_MAX);
        assertValid(msg);
    }

    @Test
    @DisplayName("find out that fractional digit count is greater than max")
    void findOutThatFractionalDigitCountIsGreaterThanMax() {
        Message msg = messageFor(FRACTIONAL_DIGIT_COUNT_GREATER_THAN_MAX);
        assertNotValid(msg);
    }

    @Test
    @DisplayName("find out that fractional digit count is equal to max")
    void findOutThatFractionalDigitCountIsEqualToMax() {
        Message msg = messageFor(FRACTIONAL_DIGIT_COUNT_EQUAL_MAX);
        assertValid(msg);
    }

    @Test
    @DisplayName("find out that fractional digit count is less than max")
    void findOutThatFractionalDigitCountIsLessThanMax() {
        Message msg = messageFor(FRACTIONAL_DIGIT_COUNT_LESS_THAN_MAX);
        assertValid(msg);
    }

    @Test
    @DisplayName("provide one valid violation if integral digit count is greater than max")
    void provideOneValidViolationIfIntegralDigitCountIsGreaterThanMax() {
        String expectedErrMsg = "Number value is out of bounds, expected: <2 max digits>.<2 max digits>.";
        Message msg = messageFor(INT_DIGIT_COUNT_GREATER_THAN_MAX);
        assertSingleViolation(msg, expectedErrMsg, VALUE);
    }

    @Disabled("See https://github.com/SpineEventEngine/base/issues/432")
    @Test
    @DisplayName("find out that integral digit count is less than max in whole number")
    void findOutThatIntegralDigitCountIsLessThanMaxInWholeNumber() {
        Message msg = integerDigitsFor(88);
        assertValid(msg);
    }

    @Disabled("See https://github.com/SpineEventEngine/base/issues/432")
    @Test
    @DisplayName("find out that integral digits count is equal to max in whole number")
    void findOutThatIntegralDigitsCountIsEqualToMaxInWholeNumber() {
        Message msg = integerDigitsFor(-888);
        assertValid(msg);
    }

    @Disabled("See https://github.com/SpineEventEngine/base/issues/432")
    @Test
    @DisplayName("find out that integral digit count is greater than max in whole number")
    void findOutThatIntegralDigitCountIsGreaterThanMaxInWholeNumber() {
        Message msg = integerDigitsFor(8888);
        assertNotValid(msg);
    }

    @Disabled("See https://github.com/SpineEventEngine/base/issues/432")
    @Test
    @DisplayName("find out that integral digit count is less than max with external constraint")
    void findOutThatIntegralDigitCountIsLessThanMaxWithExternalConstraint() {
        Message msg = integerDigitsWithExternalConstraintFor(8);
        assertValid(msg);
    }

    @Disabled("See https://github.com/SpineEventEngine/base/issues/432")
    @Test
    @DisplayName("find out that integral digits count is equal to max with external constraint")
    void findOutThatIntegralDigitsCountIsEqualToMaxWithExternalConstraint() {
        Message msg = integerDigitsWithExternalConstraintFor(-88);
        assertValid(msg);
    }

    @Disabled("See https://github.com/SpineEventEngine/base/issues/432")
    @Test
    @DisplayName("find out that integral digit count is greater than max with external constraint")
    void findOutThatIntegralDigitCountIsGreaterThanMaxWithExternalConstraint() {
        Message msg = integerDigitsWithExternalConstraintFor(888);
        assertNotValid(msg);
    }

    private static IntegerDigitsWithExternalConstraint
    integerDigitsWithExternalConstraintFor(long value) {
        return IntegerDigitsWithExternalConstraint
                .newBuilder()
                .setDigits(integerDigitsFor(value))
                .build();
    }

    private static IntegerDigits integerDigitsFor(long value) {
        return IntegerDigits
                .newBuilder()
                .setValue(value)
                .build();
    }

    private static Message messageFor(double value) {
        return DigitsCountNumberFieldValue
                .newBuilder()
                .setValue(value)
                .build();
    }
}
