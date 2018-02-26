/*
 * Copyright 2018, TeamDev Ltd. All rights reserved.
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

package io.spine.base.given;

import com.google.protobuf.Duration;
import com.google.protobuf.Timestamp;
import io.spine.base.Time;

/**
 * @author Mykhailo Drachuk
 */
public class TimeTestEnv {

    private static final int SECONDS_IN_1_MINUTE = 60;
    private static final int SECONDS_IN_5_MINUTES = 5 * SECONDS_IN_1_MINUTE;

    public static final Duration DURATION_1_MINUTE = newDuration(SECONDS_IN_1_MINUTE);
    public static final Duration DURATION_5_MINUTES = newDuration(SECONDS_IN_5_MINUTES);

    private static Duration newDuration(int seconds) {
        return Duration.newBuilder()
                       .setSeconds(seconds)
                       .build();
    }

    /** Prevent instantiation of this test environment. */
    private TimeTestEnv() {
        // Does nothing.
    }

    public static class ConstantTimeProvider implements Time.Provider {
        private final Timestamp timestamp;

        public ConstantTimeProvider(Timestamp timestamp) {
            this.timestamp = timestamp;
        }

        @Override
        public Timestamp getCurrentTime() {
            return timestamp;
        }
    }
}
