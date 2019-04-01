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

import com.google.common.base.Converter;
import com.google.protobuf.Timestamp;
import io.spine.base.Time;
import io.spine.string.Stringifiers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static io.spine.time.temporal.given.TimestampTemporalTestEnv.assertEqual;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DisplayName("TimestampTemporal should")
class TimestampTemporalTest {

    @Test
    @DisplayName("convert to Instant")
    void convertToInstant() {
        TimestampTemporal timestamp = TimestampTemporal.now();
        Instant instant = timestamp.toInstant();

        assertEqual(timestamp, instant);
    }

    @Test
    @DisplayName("create from Instant")
    void createFromInstant() {
        Instant instant = Instant.now();
        TimestampTemporal timestamp = TimestampTemporal.from(instant);

        assertEqual(timestamp, instant);
    }

    @Test
    @DisplayName("provide converter to Instant")
    void converterToInstant() {
        Converter<Timestamp, Instant> converter = TimestampTemporal
                .converter()
                .reverse();
        Timestamp timestamp = Time.currentTime();
        Instant instant = converter.convert(timestamp);

        // Check forward conversion.
        assertNotNull(instant);
        assertEqual(timestamp, instant);

        // Check backward conversion.
        assertEquals(timestamp, converter.reverse()
                                         .convert(instant));
    }

    @Test
    @DisplayName("parse RFC-3339 timestamps")
    void parse() {
        Timestamp timestamp = Time.currentTime();
        String rfcString = Stringifiers.forTimestamp()
                                       .convert(timestamp);
        TimestampTemporal parsed = TimestampTemporal.parse(rfcString);
        assertEquals(timestamp, parsed.toTimestamp());
    }
}
