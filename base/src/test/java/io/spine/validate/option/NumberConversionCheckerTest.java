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

import io.spine.validate.ComparableNumber;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("NumberConversionChecker should tell that")
final class NumberConversionCheckerTest {

    @DisplayName("it is possible to convert byte to byte")
    @Test
    void byteToByte() {
        assertTrue(NumberConversionChecker.canConvert(Byte.valueOf("1"), Byte.valueOf("2")));
    }

    @DisplayName("it is not possible to convert non-byte to byte")
    @ParameterizedTest
    @MethodSource("nonBytes")
    void byteToOthers(Number nonByte) {
        assertFalse(NumberConversionChecker.canConvert(Byte.valueOf("1"), nonByte));
    }

    @DisplayName("ComparableNumber instances are automatically unwrapped")
    @Test
    void comparableNumbersAreUnwrapped() {
        ComparableNumber number = new ComparableNumber(3);
        assertTrue(NumberConversionChecker.canConvert(number, number));
    }

    @DisplayName("BigDecimals are not supported")
    @Test
    void bigDecimalsAreNotSupported() {
        assertFalse(NumberConversionChecker.canConvert(BigDecimal.valueOf(1), 1L));
    }

    //TODO:2019-06-11:ysergiichuk: Add tests for Short, Integer, Long, Float and Double conversions

    private static Stream<Number> nonBytes() {
        return Stream.of(Short.valueOf("1"), 2, 3L, 4.0, 5.1f);
    }
}
