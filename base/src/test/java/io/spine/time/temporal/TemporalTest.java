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

package io.spine.time.temporal;

import com.google.protobuf.Timestamp;
import io.spine.base.Time;
import io.spine.time.temporal.given.InstantTemporal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static com.google.common.collect.BoundType.CLOSED;
import static com.google.common.collect.BoundType.OPEN;
import static com.google.common.collect.Range.range;
import static com.google.common.truth.Truth.assertThat;
import static io.spine.protobuf.Timestamps2.fromInstant;
import static io.spine.time.temporal.given.TemporalTestEnv.future;
import static io.spine.time.temporal.given.TemporalTestEnv.inBetween;
import static io.spine.time.temporal.given.TemporalTestEnv.past;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Temporal should")
class TemporalTest {

    @Test
    @DisplayName("compare to an instance of same type")
    void compare() {
        TimestampTemporal greater = future();
        TimestampTemporal lesser = past();
        int comparisonResult = greater.compareTo(lesser);
        assertThat(comparisonResult).isGreaterThan(0);
    }

    @Test
    @DisplayName("give consistent replies on less/greater than requests")
    void ltGtRelations() {
        TimestampTemporal later = future();
        TimestampTemporal earlier = past();

        assertThat(later).isGreaterThan(earlier);

        assertTrue(later.isLaterThan(earlier));
        assertTrue(later.isLaterOrSameAs(earlier));
        assertFalse(later.isEarlierThan(earlier));
        assertFalse(later.isEarlierOrSameAs(earlier));

        assertFalse(earlier.isLaterThan(later));
        assertFalse(earlier.isLaterOrSameAs(later));
        assertTrue(earlier.isEarlierThan(later));
        assertTrue(earlier.isEarlierOrSameAs(later));

        assertTrue(earlier.isEarlierOrSameAs(earlier));
        assertTrue(earlier.isLaterOrSameAs(earlier));
        assertTrue(earlier.isSameAs(earlier));
    }

    @Test
    @DisplayName("compare to given bounds")
    void bounds() {
        TimestampTemporal past = past();
        TimestampTemporal inBetween = inBetween();
        TimestampTemporal future = future();

        assertThat(inBetween).isIn(range(past, OPEN, future, CLOSED));

        assertTrue(inBetween.isBetween(past, future));
        assertFalse(past.isBetween(inBetween, future));
        assertFalse(future.isBetween(past, inBetween));

        assertFalse(past.isBetween(past, future));
        assertTrue(future.isBetween(past, future));
    }

    @Test
    @DisplayName("ensure first bound is lower")
    void correctBounds() {
        assertThrows(IllegalArgumentException.class,
                     () -> inBetween().isBetween(future(), past()));
        assertThrows(IllegalArgumentException.class,
                     () -> inBetween().isBetween(future(), future()));
    }

    @Test
    @DisplayName("fail to compare to a different type of Temporal")
    @SuppressWarnings("unchecked") // Supposed to fail.
    void failWithDifferentTypes() {
        Instant instant = Instant.now();
        Timestamp timestamp = fromInstant(instant);
        Temporal instantTemporal = new InstantTemporal(instant);
        Temporal timestampTemporal = TimestampTemporal.from(timestamp);

        assertThrows(IllegalArgumentException.class,
                     () -> instantTemporal.compareTo(timestampTemporal));
    }

    @Nested
    @DisplayName("compare to current time and")
    class RelativeTime {

        @BeforeEach
        void setUp() {
            Timestamp frozenTime = inBetween().toTimestamp();
            Time.setProvider(() -> frozenTime);
        }

        @AfterEach
        void tearDown() {
            Time.resetProvider();
        }

        @Test
        @DisplayName("tell if in the future")
        void tellFuture() {
            assertTrue(future().isInFuture());
        }

        @Test
        @DisplayName("tell if in the past")
        void tellPast() {
            assertTrue(past().isInPast());
        }
    }
}
