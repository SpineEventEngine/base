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

package io.spine.base;

import com.google.protobuf.Timestamp;
import org.junit.Test;

import static com.google.protobuf.util.Timestamps.subtract;
import static io.spine.base.Time.getCurrentTime;
import static io.spine.base.Time.resetProvider;
import static io.spine.base.Time.setProvider;
import static io.spine.base.Time.systemTime;
import static io.spine.base.given.TimeTestEnv.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 * @author Mykhailo Drachuk
 */
public class TimeShould {

    @Test
    public void accept_time_provider() {
        final Timestamp fiveMinutesAgo = subtract(getCurrentTime(), DURATION_5_MINUTES);

        setProvider(new ConstantTimeProvider(fiveMinutesAgo));

        assertEquals(fiveMinutesAgo, getCurrentTime());
    }

    @Test
    public void reset_time_provider_to_default() {
        final Timestamp aMinuteAgo = subtract(systemTime(), DURATION_1_MINUTE);

        setProvider(new ConstantTimeProvider(aMinuteAgo));
        resetProvider();

        assertNotEquals(aMinuteAgo, getCurrentTime());
    }

    @Test
    public void obtain_system_time_millis() {
        assertNotEquals(0, systemTime());
    }
}
