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

package io.spine.validate.given;

import com.google.protobuf.Duration;
import com.google.protobuf.Timestamp;
import io.spine.base.Time;

import static io.spine.base.Time.getCurrentTime;
import static io.spine.base.Time.setProvider;

/**
 * @author Mykhailo Drachuk
 */
public class MessageValidatorTestEnv {

    private static final int SECONDS_IN_MINUTE = 60;
    public static final int SECONDS_IN_5_MINUTES = 5 * SECONDS_IN_MINUTE;

    public static final int FIFTY_NANOSECONDS = 50;
    public static final int ZERO_NANOSECONDS = 0;
    
    /** Prevent instantiation of this test environment. */
    private MessageValidatorTestEnv() {
    }

    public static Timestamp currentTimeWithNanos(int nanos) {
        final Timestamp result = timeWithNanos(getCurrentTime(), nanos);
        return result;
    }

    public static Timestamp timeWithNanos(Timestamp time, int nanos) {
        final Timestamp result =
                time.toBuilder()
                    .setNanos(nanos)
                    .build();
        return result;
    }

    public static Duration newDuration(int seconds) {
        final Duration result =
                Duration.newBuilder()
                        .setSeconds(seconds)
                        .build();
        return result;
    }
    
    /**
     * Freezes time for current thread by setting the time provider to a 
     * {@link ConstantTimeProvider}.
     *
     * @param time time to be returned upon {@link Time#getCurrentTime()} call. 
     */
    public static void freezeTime(Timestamp time) {
        final Time.Provider frozenTimeProvider = new ConstantTimeProvider(time);
        setProvider(frozenTimeProvider);
    }

    /**
     * The provider of the current time with value that does not change.
     */
    private static class ConstantTimeProvider implements Time.Provider {
        private final Timestamp timestamp;

        private ConstantTimeProvider(Timestamp timestamp) {
            this.timestamp = timestamp;
        }

        @Override
        public Timestamp getCurrentTime() {
            return timestamp;
        }
    }
}
