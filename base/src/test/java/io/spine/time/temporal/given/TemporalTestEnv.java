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

import static io.spine.protobuf.Timestamps2.parse;

public final class TemporalTestEnv {

    /**
     * Prevents the utility class instantiation.
     */
    private TemporalTestEnv() {
    }

    public static TimestampTemporal past() {
        Timestamp timestamp = parse("1879-03-14T00:00:00Z");
        return TimestampTemporal.from(timestamp);
    }

    public static TimestampTemporal inBetween() {
        Timestamp timestamp = parse("2079-03-14T00:00:00Z");
        return TimestampTemporal.from(timestamp);
    }

    public static TimestampTemporal future() {
        Timestamp timestamp = parse("2879-03-14T00:00:00Z");
        return TimestampTemporal.from(timestamp);
    }
}
