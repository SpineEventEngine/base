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

import io.spine.testing.UtilityClassTest;
import io.spine.validate.ComparableNumber;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("NumberConversionChecker should")
final class NumberConversionCheckerTest extends UtilityClassTest<NumberConversionChecker> {

    NumberConversionCheckerTest() {
        super(NumberConversionChecker.class);
    }

    @DisplayName("tell that it is possible to convert")
    @Nested
    final class PossibleToConvert {

        @DisplayName("to byte")
        @Test
        void toByte() {
            assertTrue(NumberConversionChecker.check(Byte.valueOf("1"), Byte.valueOf("2")));
        }

        @DisplayName("to short")
        @ParameterizedTest
        @MethodSource("io.spine.validate.option.NumberConversionCheckerTest#shorts")
        void toShort(Number shortNumber) {
            assertTrue(NumberConversionChecker.check(Short.valueOf("1"), shortNumber));
        }

        @DisplayName("to integer")
        @ParameterizedTest
        @MethodSource("io.spine.validate.option.NumberConversionCheckerTest#integers")
        void toInteger(Number integerNumber) {
            assertTrue(NumberConversionChecker.check(1, integerNumber));
        }

        @DisplayName("to long")
        @ParameterizedTest
        @MethodSource("io.spine.validate.option.NumberConversionCheckerTest#longs")
        void toLong(Number longNumber) {
            assertTrue(NumberConversionChecker.check(1L, longNumber));
        }

        @DisplayName("to float")
        @Test
        void toFloat() {
            assertTrue(NumberConversionChecker.check(1.0f, 3.14f));
        }

        @DisplayName("to double")
        @ParameterizedTest
        @MethodSource("io.spine.validate.option.NumberConversionCheckerTest#doubles")
        void toDouble(Number doubleNumber) {
            assertTrue(NumberConversionChecker.check(1.0d, doubleNumber));
        }
    }

    @DisplayName("tell that it is not possible to convert")
    @Nested
    final class NotPossibleToConvert {

        @DisplayName("it is not possible to convert non-byte to byte")
        @ParameterizedTest
        @MethodSource("io.spine.validate.option.NumberConversionCheckerTest#nonBytes")
        void byteToOthers(Number nonByte) {
            assertFalse(NumberConversionChecker.check(Byte.valueOf("1"), nonByte));
        }

        @DisplayName("it is not possible to convert non-short to short")
        @ParameterizedTest
        @MethodSource("io.spine.validate.option.NumberConversionCheckerTest#nonShorts")
        void shortToOthers(Number nonShort) {
            assertFalse(NumberConversionChecker.check(Short.valueOf("1"), nonShort));
        }

        @DisplayName("it is not possible to convert non-integer to integer")
        @ParameterizedTest
        @MethodSource("io.spine.validate.option.NumberConversionCheckerTest#nonIntegers")
        void integerToOthers(Number nonInteger) {
            assertFalse(NumberConversionChecker.check(1, nonInteger));
        }

        @DisplayName("it is not possible to convert non-long to long")
        @ParameterizedTest
        @MethodSource("io.spine.validate.option.NumberConversionCheckerTest#nonLongs")
        void longToOthers(Number nonLong) {
            assertFalse(NumberConversionChecker.check(1L, nonLong));
        }

        @DisplayName("it is not possible to convert non-float to float")
        @ParameterizedTest
        @MethodSource("io.spine.validate.option.NumberConversionCheckerTest#nonFloats")
        void floatToOthers(Number nonFloat) {
            assertFalse(NumberConversionChecker.check(1.0f, nonFloat));
        }

        @DisplayName("it is not possible to convert non-double to double")
        @ParameterizedTest
        @MethodSource("io.spine.validate.option.NumberConversionCheckerTest#nonDoubles")
        void doubleToOthers(Number nonDouble) {
            assertFalse(NumberConversionChecker.check(1.0d, nonDouble));
        }
    }

    @DisplayName("tell that ComparableNumber instances are automatically unwrapped")
    @Test
    void comparableNumbersAreUnwrapped() {
        ComparableNumber number = new ComparableNumber(3);
        assertTrue(NumberConversionChecker.check(number, number));
    }

    @DisplayName("tell that BigDecimals are not supported")
    @Test
    void bigDecimalsAreNotSupported() {
        assertFalse(NumberConversionChecker.check(BigDecimal.valueOf(1), 1L));
    }

    private static Stream<Number> nonBytes() {
        return Stream.concat(Stream.of(Short.valueOf("1")), nonShorts());
    }

    private static Stream<Number> nonShorts() {
        return Stream.concat(Stream.of(2), nonIntegers());
    }

    private static Stream<Number> nonIntegers() {
        return Stream.concat(Stream.of(3L), nonLongs());
    }

    private static Stream<Number> nonLongs() {
        return Stream.of(4.0, 5.1f);
    }

    private static Stream<Number> nonFloats() {
        return Stream.concat(nonDoubles(), Stream.of(4.0));
    }

    private static Stream<Number> nonDoubles() {
        return Stream.of(Byte.valueOf("1"), Short.valueOf("1"), 2, 3L);
    }

    private static Stream<Number> shorts() {
        return Stream.of(Byte.valueOf("1"), Short.valueOf("2"));
    }

    private static Stream<Number> integers() {
        return Stream.concat(shorts(), Stream.of(2));
    }

    private static Stream<Number> longs() {
        return Stream.concat(integers(), Stream.of(3L));
    }

    private static Stream<Number> doubles() {
        return Stream.of(3.14f, 8.19d);
    }
}
