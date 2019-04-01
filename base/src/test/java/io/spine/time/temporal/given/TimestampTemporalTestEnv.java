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

package io.spine.time.temporal.given;

import com.google.protobuf.Timestamp;
import io.spine.time.temporal.TimestampTemporal;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;

public final class TimestampTemporalTestEnv {

    /**
     * Prevents the utility class instantiation.
     */
    private TimestampTemporalTestEnv() {
    }

    public static void assertEqual(TimestampTemporal timestamp, Instant instant) {
        assertEqual(timestamp.toTimestamp(), instant);
    }

    public static void assertEqual(Timestamp timestamp, Instant instant) {
        assertEquals(timestamp.getSeconds(), instant.getEpochSecond());
        assertEquals(timestamp.getNanos(), instant.getNano());
    }
}
