/*
 * Copyright 2020, TeamDev. All rights reserved.
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

package io.spine.base;

import com.google.common.truth.Subject;
import com.google.protobuf.Timestamp;
import io.spine.base.Time.SystemTimeProvider;
import io.spine.base.given.ConstantTimeProvider;
import io.spine.base.given.FakeTimeProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.ZoneId;

import static com.google.common.truth.Truth.assertThat;
import static com.google.protobuf.util.Timestamps.subtract;
import static io.spine.base.Time.currentTime;
import static io.spine.base.Time.resetProvider;
import static io.spine.base.Time.setProvider;
import static io.spine.base.Time.systemTime;
import static io.spine.base.given.GivenDurations.DURATION_1_MINUTE;
import static io.spine.base.given.GivenDurations.DURATION_5_MINUTES;
import static io.spine.testing.Tests.assertHasPrivateParameterlessCtor;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DisplayName("`Time` class should")
class TimeTest {

    @AfterEach
    void tearDown() {
        resetProvider();
    }

    @Test
    @DisplayName("accept `TimeProvider`")
    void acceptProvider() {
        Timestamp fiveMinutesAgo = subtract(currentTime(), DURATION_5_MINUTES);

        setProvider(new ConstantTimeProvider(fiveMinutesAgo));

        assertCurrentTime().isEqualTo(fiveMinutesAgo);
    }

    private static Subject assertCurrentTime() {
        return assertThat(currentTime());
    }

    @Test
    @DisplayName("reset `TimeProvider` to default value")
    void reset() {
        Timestamp aMinuteAgo = subtract(systemTime(), DURATION_1_MINUTE);

        setProvider(new ConstantTimeProvider(aMinuteAgo));
        resetProvider();

        assertCurrentTime().isNotEqualTo(aMinuteAgo);
    }

    @Nested
    @DisplayName("Have `SystemTimeProvider`")
    class SystemTime {

        @Test
        @DisplayName("which is singleton")
        void isSingleton() {
            assertNotNull(SystemTimeProvider.INSTANCE);
            assertHasPrivateParameterlessCtor(SystemTimeProvider.class);
        }

        @Test
        @DisplayName("which emulates nanosecond resolution")
        void providesNanos() {
            @SuppressWarnings("ProtoTimestampGetSecondsGetNano")
            int allNanos = systemTime().getNanos();
            int startingFromMicros = allNanos % 1_000_000;
            assertThat(startingFromMicros).isGreaterThan(0);
        }

        @Test
        @DisplayName("which provides different values for two consecutive calls")
        void differentValuesForConsecutive() {
            assertThat(systemTime()).isNotEqualTo(systemTime());
        }
    }

    @Test
    @DisplayName("obtain system time even if `TimeProvider` is set")
    void gettingSystemTime() {
        setProvider(new ConstantTimeProvider(Timestamp.getDefaultInstance()));

        assertThat(systemTime().getSeconds()).isNotEqualTo(0);
    }

    @Test
    @DisplayName("obtain current time zone")
    void timeZone() {
        ZoneId zoneId = Time.currentTimeZone();
        assertThat(zoneId).isEqualTo(ZoneId.systemDefault());
    }

    @Test
    @DisplayName("obtain the time zone from the provider")
    void customTimeZone() {
        setProvider(new FakeTimeProvider());
        assertThat(Time.currentTimeZone()).isEqualTo(FakeTimeProvider.ZONE);
    }
}
