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

import com.google.protobuf.Timestamp;
import io.spine.test.validate.TimeInFutureFieldValue;
import io.spine.test.validate.TimeInPastFieldValue;
import io.spine.test.validate.TimeWithoutOptsFieldValue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.spine.validate.MessageValidatorTest.MESSAGE_VALIDATOR_SHOULD;
import static io.spine.validate.given.MessageValidatorTestEnv.FIFTY_NANOSECONDS;
import static io.spine.validate.given.MessageValidatorTestEnv.VALUE;
import static io.spine.validate.given.MessageValidatorTestEnv.ZERO_NANOSECONDS;
import static io.spine.validate.given.MessageValidatorTestEnv.currentTimeWithNanos;
import static io.spine.validate.given.MessageValidatorTestEnv.freezeTime;
import static io.spine.validate.given.MessageValidatorTestEnv.getFuture;
import static io.spine.validate.given.MessageValidatorTestEnv.getPast;
import static io.spine.validate.given.MessageValidatorTestEnv.timeWithNanos;

@DisplayName(MESSAGE_VALIDATOR_SHOULD + "analyze (when) option and")
class WhenTest extends MessageValidatorTest {

    @Test
    @DisplayName("find out that time is in future")
    void findOutThatTimeIsInFuture() {
        TimeInFutureFieldValue validMsg = TimeInFutureFieldValue.newBuilder()
                                                                .setValue(getFuture())
                                                                .build();
        assertValid(validMsg);
    }

    @Test
    @DisplayName("find out that time is NOT in future")
    void findOutThatTimeIsNotInFuture() {
        TimeInFutureFieldValue invalidMsg = TimeInFutureFieldValue.newBuilder()
                                                                  .setValue(getPast())
                                                                  .build();
        assertNotValid(invalidMsg);
    }

    @Test
    @DisplayName("find out that time is in past")
    void findOutThatTimeIsInPast() {
        TimeInPastFieldValue validMsg = TimeInPastFieldValue.newBuilder()
                                                            .setValue(getPast())
                                                            .build();
        assertValid(validMsg);
    }

    @Test
    @DisplayName("find out that time is NOT in past")
    void findOutThatTimeIsNotInPast() {
        TimeInPastFieldValue invalidMsg = TimeInPastFieldValue.newBuilder()
                                                              .setValue(getFuture())
                                                              .build();
        assertNotValid(invalidMsg);
    }

    @Test
    @DisplayName("find out that time is NOT in the past by nanoseconds")
    void findOutThatTimeIsNotInThePastByNanos() {
        Timestamp currentTime = currentTimeWithNanos(ZERO_NANOSECONDS);
        Timestamp timeInFuture = timeWithNanos(currentTime, FIFTY_NANOSECONDS);
        freezeTime(currentTime);
        TimeInPastFieldValue invalidMsg =
                TimeInPastFieldValue.newBuilder()
                                    .setValue(timeInFuture)
                                    .build();
        assertNotValid(invalidMsg);
    }

    @Test
    @DisplayName("find out that time is in the past by nanoseconds")
    void findOutThatTimeIsInThePastByNanos() {
        Timestamp currentTime = currentTimeWithNanos(FIFTY_NANOSECONDS);
        Timestamp timeInPast = timeWithNanos(currentTime, ZERO_NANOSECONDS);
        freezeTime(currentTime);
        TimeInPastFieldValue invalidMsg =
                TimeInPastFieldValue.newBuilder()
                                    .setValue(timeInPast)
                                    .build();
        assertValid(invalidMsg);
    }

    @Test
    @DisplayName("consider Timestamp field valid if no TimeOption set")
    void considerTimestampFieldIsValidIfNoTimeOptionSet() {
        TimeWithoutOptsFieldValue msg = TimeWithoutOptsFieldValue.getDefaultInstance();
        assertValid(msg);
    }

    @Test
    @DisplayName("provide one valid violation if time is invalid")
    void provideOneValidViolationIfTimeIsInvalid() {
        TimeInFutureFieldValue invalidMsg = TimeInFutureFieldValue.newBuilder()
                                                                  .setValue(getPast())
                                                                  .build();
        assertSingleViolation(invalidMsg, "Timestamp value must be in the future.", VALUE);
    }
}
