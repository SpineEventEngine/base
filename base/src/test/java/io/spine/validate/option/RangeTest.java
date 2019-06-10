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

import io.spine.test.validate.NumRanges;
import io.spine.test.validate.RangesHolder;
import io.spine.validate.MessageValidatorTest;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.IntStream;
import java.util.stream.Stream;

import static io.spine.validate.MessageValidatorTest.MESSAGE_VALIDATOR_SHOULD;

@Disabled("See https://github.com/SpineEventEngine/base/issues/436")
@DisplayName(MESSAGE_VALIDATOR_SHOULD + "analyze (range) option and")
final class RangeTest extends MessageValidatorTest {

    @DisplayName("find out that hours fit into the defined range")
    @ParameterizedTest
    @MethodSource("validHours")
    void findOutThatHoursFitIntoRange(int hour) {
        NumRanges msg = validRange()
                .setHour(hour)
                .build();
        assertValid(msg);
    }

    @DisplayName("find out that hours fit into the defined external constraint range")
    @ParameterizedTest
    @MethodSource("validHalfDayHours")
    void findOutThatHoursFitIntoExternalConstraintRange(int hour) {
        NumRanges msg = validRange()
                .setHour(hour)
                .build();
        assertValid(holderOf(msg));
    }

    @DisplayName("find out that hours do not fit into the defined range")
    @ParameterizedTest
    @MethodSource("invalidHours")
    void findOutThatHoursDoNotFitIntoRange(int hour) {
        NumRanges msg = validRange()
                .setHour(hour)
                .build();
        assertNotValid(msg);
    }

    @DisplayName("find out that hours do not fit into the defined external constraint range")
    @ParameterizedTest
    @MethodSource("invalidHalfDayHours")
    void findOutThatHoursDoNotFitIntoExternalConstraintRange(int hour) {
        NumRanges msg = validRange()
                .setHour(hour)
                .build();
        assertNotValid(holderOf(msg));
    }

    private static RangesHolder holderOf(NumRanges ranges) {
        return RangesHolder
                .newBuilder()
                .setRanges(ranges)
                .build();
    }

    private static NumRanges.Builder validRange() {
        return NumRanges
                .newBuilder()
                .setHour(1)
                .setAngle(1)
                .setDegree(1)
                .setMinute(1);
    }

    private static Stream<Integer> invalidHalfDayHours() {
        return IntStream.of(-1, 13, 23, 24, Integer.MAX_VALUE, Integer.MIN_VALUE)
                        .boxed();
    }

    private static Stream<Integer> invalidHours() {
        return IntStream.of(-1, 24, Integer.MAX_VALUE, Integer.MIN_VALUE)
                        .boxed();
    }

    private static Stream<Integer> validHours() {
        return IntStream.range(0, 23)
                        .boxed();
    }

    private static Stream<Integer> validHalfDayHours() {
        return IntStream.range(0, 12)
                        .boxed();
    }
}
