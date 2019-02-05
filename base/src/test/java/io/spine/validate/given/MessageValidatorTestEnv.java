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

package io.spine.validate.given;

import com.google.common.collect.ImmutableList;
import com.google.protobuf.ByteString;
import com.google.protobuf.Duration;
import com.google.protobuf.ProtocolStringList;
import com.google.protobuf.StringValue;
import com.google.protobuf.Timestamp;
import io.spine.base.FieldPath;
import io.spine.base.Time;
import io.spine.validate.ConstraintViolation;

import static com.google.protobuf.util.Timestamps.add;
import static com.google.protobuf.util.Timestamps.subtract;
import static io.spine.base.Identifier.newUuid;
import static io.spine.base.Time.getCurrentTime;
import static io.spine.base.Time.setProvider;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class MessageValidatorTestEnv {

    public static final double EQUAL_MIN = 16.5;
    public static final double GREATER_THAN_MIN = EQUAL_MIN + 5;
    public static final double LESS_THAN_MIN = EQUAL_MIN - 5;

    public static final double EQUAL_MAX = 64.5;
    public static final double GREATER_THAN_MAX = EQUAL_MAX + 5;

    public static final double INT_DIGIT_COUNT_GREATER_THAN_MAX = 123.5;
    public static final double INT_DIGIT_COUNT_EQUAL_MAX = 12.5;

    public static final String VALUE = "value";
    public static final String EMAIL = "email";
    public static final String OUTER_MSG_FIELD = "outer_msg_field";
    public static final String LESS_THAN_MIN_MSG = "Number must be greater than or equal to 16.5.";
    public static final String GREATER_MAX_MSG = "Number must be less than or equal to 64.5.";
    public static final String MATCH_REGEXP_MSG = "String must match the regular expression '%s'.";

    public static final int SECONDS_IN_MINUTE = 60;

    public static final int SECONDS_IN_5_MINUTES = 5 * SECONDS_IN_MINUTE;
    public static final int FIFTY_NANOSECONDS = 50;
    public static final int ZERO_NANOSECONDS = 0;
    public static final double INT_DIGIT_COUNT_LESS_THAN_MAX = 1.5;
    public static final double LESS_THAN_MAX = EQUAL_MAX - 5;

    /** Prevent instantiation of this test environment. */
    private MessageValidatorTestEnv() {
    }

    public static Timestamp currentTimeWithNanos(int nanos) {
        Timestamp result = timeWithNanos(getCurrentTime(), nanos);
        return result;
    }

    public static Timestamp timeWithNanos(Timestamp time, int nanos) {
        Timestamp result =
                time.toBuilder()
                    .setNanos(nanos)
                    .build();
        return result;
    }

    public static Duration newDuration(int seconds) {
        Duration result =
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
        Time.Provider frozenTimeProvider = new ConstantTimeProvider(time);
        setProvider(frozenTimeProvider);
    }

    public static void assertFieldPathIs(ConstraintViolation violation, String... expectedFields) {
        FieldPath path = violation.getFieldPath();
        ProtocolStringList actualFields = path.getFieldNameList();
        assertEquals(expectedFields.length, actualFields.size());
        assertEquals(ImmutableList.copyOf(expectedFields), ImmutableList.copyOf(actualFields));
    }

    public static Timestamp getFuture() {
        Timestamp future = add(getCurrentTime(), newDuration(SECONDS_IN_5_MINUTES));
        return future;
    }

    public static Timestamp getPast() {
        Timestamp past = subtract(getCurrentTime(), newDuration(SECONDS_IN_5_MINUTES));
        return past;
    }

    public static StringValue newStringValue() {
        return StringValue.of(newUuid());
    }

    public static ByteString newByteString() {
        ByteString bytes = ByteString.copyFromUtf8(newUuid());
        return bytes;
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
