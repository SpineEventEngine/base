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
package io.spine.string;

import com.google.common.base.Converter;
import com.google.common.testing.NullPointerTester;
import com.google.protobuf.Duration;
import com.google.protobuf.util.Durations;
import io.spine.protobuf.Durations2;
import io.spine.testing.UtilityClassTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static io.spine.protobuf.Durations2.ZERO;
import static io.spine.protobuf.Durations2.add;
import static io.spine.protobuf.Durations2.fromHours;
import static io.spine.protobuf.Durations2.fromMinutes;
import static io.spine.protobuf.Durations2.getHours;
import static io.spine.protobuf.Durations2.getMinutes;
import static io.spine.protobuf.Durations2.hours;
import static io.spine.protobuf.Durations2.hoursAndMinutes;
import static io.spine.protobuf.Durations2.isGreaterThan;
import static io.spine.protobuf.Durations2.isLessThan;
import static io.spine.protobuf.Durations2.isNegative;
import static io.spine.protobuf.Durations2.isPositive;
import static io.spine.protobuf.Durations2.isPositiveOrZero;
import static io.spine.protobuf.Durations2.isZero;
import static io.spine.protobuf.Durations2.milliseconds;
import static io.spine.protobuf.Durations2.minutes;
import static io.spine.protobuf.Durations2.nanos;
import static io.spine.protobuf.Durations2.seconds;
import static io.spine.protobuf.Durations2.toMinutes;
import static io.spine.protobuf.Durations2.toNanos;
import static io.spine.protobuf.Durations2.toSeconds;
import static io.spine.testing.TestValues.random;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings({"MagicNumber", "ClassCanBeStatic", "InnerClassMayBeStatic"})
@DisplayName("Durations2 should")
class Durations2Test extends UtilityClassTest<Durations2> {

    private final Converter<java.time.Duration, Duration> converter = Durations2.converter();

    Durations2Test() {
        super(Durations2.class);
    }

    @Override
    protected void configure(NullPointerTester nullTester) {
        nullTester.setDefault(Duration.class, Duration.getDefaultInstance());
    }

    @Test
    @DisplayName("convert to String and parse back")
    void toFromString() {
        Duration expected = randomDuration();
        String str = Durations.toString(expected);
        Duration converted = Durations2.parse(str);
        assertEquals(expected, converted);
    }

    private static Duration randomDuration() {
        return add(seconds(random(10000)), nanos(random(100_000)));
    }

    @Test
    @DisplayName("convert to Java Time and back")
    void toFromJavaTime() {
        Duration expected = randomDuration();
        java.time.Duration converted = converter.reverse()
                                                .convert(expected);
        Duration back = converter.convert(converted);
        assertEquals(expected, back);
    }

    @Test
    @DisplayName("have ZERO constant")
    void zeroConstant() {
        assertEquals(0, toNanos(ZERO));
    }

    @Nested
    @DisplayName("Have DSL-like methods")
    class Dsl {
        @Test
        @DisplayName("for seconds")
        void forSeconds() {
            assertEquals(100, seconds(100).getSeconds());
        }
        
        @Test
        @DisplayName("for minutes")
        void forMinutes() {
            assertEquals(5, getMinutes(minutes(5)));
        }

        @Test
        @DisplayName("for hours")
        void forHours() {
            assertEquals(24, getHours(hours(24)));
        }

        @Test
        @DisplayName("for milliseconds")
        void forMillis() {
            assertNotNull(milliseconds(100500L));
        }

        @Test
        @DisplayName("for hours and minutes")
        void hoursMinutesSeconds() {
            long hours = 3;
            long minutes = 25;
            long secondsTotal = hoursToSeconds(hours) + minutesToSeconds(minutes);
            Duration expected = seconds(secondsTotal);

            Duration actual = hoursAndMinutes(hours, minutes);

            assertEquals(expected, actual);
        }

    }

    private static long minutesToSeconds(long minutes) {
        return minutes * 60L;
    }

    private static long hoursToSeconds(long hours) {
        return hours * 60L * 60L;
    }

    @Nested
    @DisplayName("Convert a number of hours")
    class HourConversion {

        private void test(long hours) {
            Duration expected = seconds(hoursToSeconds(hours));
            Duration actual = hours(hours);
            assertEquals(expected, actual);
        }

        @Test
        @DisplayName("zero value")
        void zero() {
            test(0);
        }

        @Test
        @DisplayName("positive value")
        void positive() {
            test(36);
        }

        @Test
        @DisplayName("negative value")
        void negative() {
            test(-384);
        }
    }

    @Nested
    @DisplayName("Fail if")
    class MathError {

        @Test
        @DisplayName("hours value is too big")
        void onHugeHours() {
            assertThrows(
                    ArithmeticException.class,
                    () -> hours(Long.MAX_VALUE)
            );
        }

        @Test
        @DisplayName("minutes value is too big")
        void onHugeMinutes() {
            assertThrows(
                    ArithmeticException.class,
                    () -> minutes(Long.MAX_VALUE)
            );
        }
    }

    @Nested
    @DisplayName("Add")
    class Add {

        @Test
        @DisplayName("two nulls -> ZERO")
        void nullPlusNull() {
            assertEquals(ZERO, add(null, null));
        }

        @Test
        @DisplayName("null returning same instance")
        void sameWithNull() {
            Duration duration = seconds(525);
            assertSame(duration, add(duration, null));
            assertSame(duration, add(null, duration));
        }

        @Test
        @DisplayName("positive durations")
        void positiveDurations() {
            testAddSeconds(25, 5);
            testAddSeconds(300, 338);
        }

        @Test
        @DisplayName("negative durations")
        void negativeDurations() {
            testAddSeconds(-25, -5);
            testAddSeconds(-300, -338);
        }

        @Test
        @DisplayName("both positive and negative durations")
        void negativeAndPositive() {
            testAddSeconds(25, -5);
            testAddSeconds(-300, 338);
        }

        private void testAddSeconds(long seconds1, long seconds2) {
            long secondsTotal = seconds1 + seconds2;
            Duration sumExpected = seconds(secondsTotal);
            Duration sumActual = add(seconds(seconds1), seconds(seconds2));

            assertEquals(sumExpected, sumActual);
        }
    }
    
    @Nested
    @DisplayName("Convert Duration to")
    class Convert {

        @Test
        @DisplayName("nanoseconds")
        void amountOfNanoseconds() {
            assertEquals(10, toNanos(nanos(10)));
            assertEquals(-256, toNanos(nanos(-256)));
        }

        @Test
        @DisplayName("seconds")
        void amountOfSeconds() {
            assertEquals(1, toSeconds(seconds(1)));
            assertEquals(-256, toSeconds(seconds(-256)));
        }

        @Test
        @DisplayName("minutes")
        void amountOfMinutes() {
            assertEquals(1, toMinutes(minutes(1)));
            assertEquals(-256, toMinutes(minutes(-256)));
        }
    }

    @Nested
    @DisplayName("Obtain from Duration")
    class Obtain {

        @Test
        void amountOfHours() {
            assertEquals(1, getHours(fromHours(1)));
            assertEquals(-256, getHours(fromHours(-256)));
        }

        @Test
        void remainderOfMinutes() {
            final long minutesRemainder = 8;
            final long minutesTotal = minutesRemainder + 60; // add 1 hour
            assertEquals(minutesRemainder, getMinutes(fromMinutes(minutesTotal)));
        }
    }

    @Nested
    @DisplayName("Verify if Duration is")
    class Verify {

        @Test
        @DisplayName("positive or zero")
        void positiveOrZero() {
            assertTrue(isPositiveOrZero(seconds(360)));
            assertTrue(isPositiveOrZero(seconds(0)));
            assertFalse(isPositiveOrZero(seconds(-32)));
        }

        @Test
        @DisplayName("positive")
        void positive() {
            assertTrue(isPositive(seconds(360)));
            assertFalse(isPositive(seconds(0)));
            assertFalse(isPositive(seconds(-32)));
        }

        @Test
        @DisplayName("zero")
        void zero() {
            assertTrue(isZero(seconds(0)));
            assertFalse(isZero(seconds(360)));
            assertFalse(isZero(seconds(-32)));
        }

        @Test
        @DisplayName("negative")
        void negative() {
            assertTrue(isNegative(seconds(-32)));
            assertFalse(isNegative(seconds(360)));
            assertFalse(isNegative(seconds(0)));
        }
    }

    @Nested
    @DisplayName("Tell if Duration is")
    class Compare {

        @Test
        @DisplayName("greater")
        void greater() {
            assertTrue(isGreaterThan(seconds(64), seconds(2)));
            assertFalse(isGreaterThan(seconds(2), seconds(64)));
            assertFalse(isGreaterThan(seconds(5), seconds(5)));
        }

        @Test
        @DisplayName("less")
        void less() {
            assertTrue(isLessThan(seconds(2), seconds(64)));
            assertFalse(isLessThan(seconds(64), seconds(2)));
            assertFalse(isLessThan(seconds(5), seconds(5)));
        }
    }
}
