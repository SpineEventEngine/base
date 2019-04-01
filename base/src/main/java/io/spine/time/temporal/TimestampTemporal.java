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
import com.google.protobuf.Any;
import com.google.protobuf.Timestamp;
import io.spine.base.Time;
import io.spine.protobuf.AnyPacker;
import io.spine.string.Stringifiers;

import java.io.Serializable;
import java.time.Instant;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.protobuf.util.Timestamps.checkValid;

/**
 * An implementation of {@link Temporal} for the Protobuf {@link Timestamp}.
 */
public final class TimestampTemporal implements Temporal<TimestampTemporal> {

    private final Timestamp value;

    private TimestampTemporal(Timestamp value) {
        this.value = value;
    }

    /**
     * Creates a new instance with the given {@code Timestamp}.
     *
     * <p>The given value must be valid in terms of {@code Timestamps.checkValid(..)}. Otherwise,
     * as {@code IllegalStateException} is thrown.
     */
    public static TimestampTemporal from(Timestamp value) {
        checkNotNull(value);
        checkValid(value);
        return new TimestampTemporal(value);
    }

    /**
     * Creates a new instance from the passed {@link java.time.Instant} value.
     */
    public static TimestampTemporal from(Instant instant) {
        checkNotNull(instant);
        Timestamp timestamp = converter().convert(instant);
        checkNotNull(timestamp);
        return from(timestamp);
    }

    /**
     * Parses a timestamp from an {@code RFC-3339} date-time string.
     *
     * <p>Unlike {@code Timestamps.parse(String)}, this method does not throw a checked exception.
     *
     * @throws IllegalArgumentException if the string is not of required format
     */
    public static TimestampTemporal parse(String rfcString) {
        checkNotNull(rfcString);
        Timestamp timestamp = Stringifiers.forTimestamp()
                                          .reverse()
                                          .convert(rfcString);
        checkNotNull(timestamp);
        return from(timestamp);
    }

    /**
     * Creates a new instance with the {@linkplain Time#currentTime() current} time.
     */
    public static TimestampTemporal now() {
        Timestamp currentTime = Time.currentTime();
        return from(currentTime);
    }

    @Override
    public Timestamp toTimestamp() {
        return value;
    }

    @Override
    public Any toAny() {
        return AnyPacker.pack(value);
    }

    /**
     * Converts the passed timestamp to {@code Instant}.
     */
    public Instant toInstant() {
        Instant instant = converter().reverse().convert(value);
        checkNotNull(instant);
        return instant;
    }

    /**
     * Obtains converter of {@code Timestamp}s to {@code Instant}s.
     */
    public static Converter<Instant, Timestamp> converter() {
        return InstantConverter.INSTANCE;
    }

    /**
     * Converts {@code Timestamp} to {@code Instant}.
     */
    private static final class InstantConverter extends Converter<Instant, Timestamp>
            implements Serializable {

        private static final long serialVersionUID = 0L;
        private static final InstantConverter INSTANCE = new InstantConverter();

        @Override
        protected Timestamp doForward(Instant value) {
            checkNotNull(value);
            Timestamp result = Timestamp
                    .newBuilder()
                    .setSeconds(value.getEpochSecond())
                    .setNanos(value.getNano())
                    .build();
            return result;
        }

        @Override
        protected Instant doBackward(Timestamp value) {
            checkNotNull(value);
            Instant result = Instant.ofEpochSecond(value.getSeconds(), value.getNanos());
            return result;
        }

        private Object readResolve() {
            return INSTANCE;
        }
    }
}
