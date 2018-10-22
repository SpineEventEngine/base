/*
 * Copyright 2018, TeamDev. All rights reserved.
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

import com.google.common.truth.DefaultSubject;
import com.google.common.truth.Subject;
import com.google.protobuf.Timestamp;
import io.spine.base.Time.SystemTimeProvider;
import io.spine.base.given.ConstantTimeProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;
import static com.google.protobuf.util.Timestamps.subtract;
import static io.spine.base.Time.getCurrentTime;
import static io.spine.base.Time.resetProvider;
import static io.spine.base.Time.setProvider;
import static io.spine.base.Time.systemTime;
import static io.spine.base.given.GivenDurations.DURATION_1_MINUTE;
import static io.spine.base.given.GivenDurations.DURATION_5_MINUTES;
import static io.spine.testing.Tests.assertHasPrivateParameterlessCtor;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DisplayName("Time class should")
class TimeTest {

    @AfterEach
    void tearDown() {
        resetProvider();
    }

    @Test
    @DisplayName("accept TimeProvider")
    void acceptProvider() {
        Timestamp fiveMinutesAgo = subtract(getCurrentTime(), DURATION_5_MINUTES);

        setProvider(new ConstantTimeProvider(fiveMinutesAgo));

        assertCurrentTime().isEqualTo(fiveMinutesAgo);
    }

    private static Subject<DefaultSubject, Object> assertCurrentTime() {
        return assertThat(getCurrentTime());
    }

    @Test
    @DisplayName("resent TimeProvider to default value")
    void reset() {
        Timestamp aMinuteAgo = subtract(systemTime(), DURATION_1_MINUTE);

        setProvider(new ConstantTimeProvider(aMinuteAgo));
        resetProvider();

        assertCurrentTime().isNotEqualTo(aMinuteAgo);
    }

    @Nested
    @DisplayName("Have SystemTimeProvider")
    class SystemTime {

        @Test
        @DisplayName("which is singleton")
        void isSingleton() {
            assertNotNull(SystemTimeProvider.INSTANCE);
            assertHasPrivateParameterlessCtor(SystemTimeProvider.class);
        }
    }

    @Test
    @DisplayName("obtain system time event if TimeProvider is set")
    void gettingSystemTime() {
        setProvider(new ConstantTimeProvider(Timestamp.getDefaultInstance()));

        assertNotEquals(0, systemTime());
    }
}
