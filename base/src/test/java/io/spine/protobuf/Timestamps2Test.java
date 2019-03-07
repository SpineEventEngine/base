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

package io.spine.protobuf;

import com.google.common.base.Converter;
import com.google.common.testing.NullPointerTester;
import com.google.protobuf.Duration;
import com.google.protobuf.Timestamp;
import io.spine.base.Time;
import io.spine.testing.UtilityClassTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static com.google.protobuf.util.Durations.fromSeconds;
import static com.google.protobuf.util.Timestamps.add;
import static com.google.protobuf.util.Timestamps.subtract;
import static io.spine.base.Time.currentTime;
import static io.spine.protobuf.Timestamps2.fromInstant;
import static io.spine.protobuf.Timestamps2.isLaterThan;
import static io.spine.protobuf.Timestamps2.toInstant;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Timestamps2 should")
class Timestamps2Test extends UtilityClassTest<Timestamps2> {

    private static final Duration TEN_SECONDS = fromSeconds(10L);

    Timestamps2Test() {
        super(Timestamps2.class);
    }

    @Override
    protected void configure(NullPointerTester nullTester) {
        nullTester.setDefault(Timestamp.class, Time.currentTime())
                  .setDefault(Instant.class, Instant.now());
    }

    @Nested
    @DisplayName("Check that a Timestamp is")
    class Between {

        @Test
        @DisplayName("between two others")
        void isBetween() {
            Timestamp start = currentTime();
            Timestamp timeBetween = add(start, TEN_SECONDS);
            Timestamp finish = add(timeBetween, TEN_SECONDS);

            boolean isBetween = Timestamps2.isBetween(timeBetween, start, finish);

            assertTrue(isBetween);
        }

        @Test
        @DisplayName("not between two others")
        void isNotBetween() {
            Timestamp start = currentTime();
            Timestamp finish = add(start, TEN_SECONDS);
            Timestamp timeNotBetween = add(finish, TEN_SECONDS);

            boolean isBetween = Timestamps2.isBetween(timeNotBetween, start, finish);

            assertFalse(isBetween);
        }
    }

    @Nested
    @DisplayName("Verify that a Timestamp is")
    class Later {

        @Test
        @DisplayName("later than another")
        void isLater() {
            Timestamp fromPoint = currentTime();
            Timestamp timeToCheck = add(fromPoint, TEN_SECONDS);

            boolean isAfter = isLaterThan(timeToCheck, fromPoint);

            assertTrue(isAfter);
        }

        @Test
        @DisplayName("not later than another")
        void isNotLater() {
            Timestamp fromPoint = currentTime();
            Timestamp timeToCheck = subtract(fromPoint, TEN_SECONDS);

            boolean isAfter = isLaterThan(timeToCheck, fromPoint);

            assertFalse(isAfter);
        }
    }

    private static void assertEqual(Timestamp timestamp, Instant instant) {
        assertEquals(timestamp.getSeconds(), instant.getEpochSecond());
        assertEquals(timestamp.getNanos(), instant.getNano());
    }

    @Test
    @DisplayName("convert Timestamp to Instant")
    void convertToInstant() {
        Timestamp timestamp = Time.currentTime();
        Instant instant = toInstant(timestamp);

        assertEqual(timestamp, instant);
    }

    @Test
    @DisplayName("create Timestamp by Instant")
    void createFromInstant() {
        Instant instant = Instant.now();
        Timestamp timestamp = fromInstant(instant);

        assertEqual(timestamp, instant);
    }

    @Test
    @DisplayName("provide converter to Instant")
    void converterToInstant() {
        Timestamp timestamp = Time.currentTime();
        Converter<Timestamp, Instant> converter = Timestamps2.converter()
                                                             .reverse();
        Instant instant = converter.convert(timestamp);

        // Check forward conversion.
        assertEqual(timestamp, instant);

        // Check backward conversion.
        assertEquals(timestamp, converter.reverse()
                                         .convert(instant));
    }
}
