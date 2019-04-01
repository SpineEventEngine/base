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
package io.spine.protobuf;

import com.google.common.base.Converter;
import com.google.protobuf.Timestamp;
import io.spine.time.temporal.TimestampTemporal;

import java.time.Instant;

/**
 * Utilities class for working with {@link com.google.protobuf.Timestamp Timestamp} values in
 * addition to those available from {@link com.google.protobuf.util.Timestamps Timestamps} utility
 * class from the Protobuf Util library.
 *
 * @deprecated Use {@link TimestampTemporal} instead. Also, consider domain-specific types for time
 *         representation.
 */
@Deprecated
public final class Timestamps2 {

    /** Prevent instantiation of this utility class. */
    private Timestamps2() {
    }

    /**
     * Converts the passed timestamp to {@code Instant}.
     */
    @Deprecated
    public static Instant toInstant(Timestamp timestamp) {
        return TimestampTemporal.from(timestamp)
                                .toInstant();
    }

    /**
     * Calculates if the {@code timestamp} is between the {@code start} and
     * {@code finish} timestamps.
     *
     * @param timestamp
     *         the timestamp to check if it is between the {@code start} and {@code finish}
     * @param start
     *         the first point in time, must be before the {@code finish} timestamp
     * @param finish
     *         the second point in time, must be after the {@code start} timestamp
     * @return {@code true} if the {@code timestamp} is after the {@code start} and before
     *         the {@code finish} timestamps, {@code false} otherwise
     */
    @Deprecated
    public static boolean isBetween(Timestamp timestamp, Timestamp start, Timestamp finish) {
        TimestampTemporal temporal = TimestampTemporal.from(timestamp);
        TimestampTemporal startTemporal = TimestampTemporal.from(start);
        TimestampTemporal finishTemporal = TimestampTemporal.from(finish);
        return temporal.isBetween(startTemporal, finishTemporal);
    }

    /**
     * Calculates if {@code timestamp} is later {@code thanTime} timestamp.
     *
     * @param timestamp
     *         the timestamp to check if it is later then {@code thanTime}
     * @param thanTime
     *         the first point in time which is supposed to be before the {@code timestamp}
     * @return {@code true} if the {@code timestamp} is later than {@code thanTime} timestamp,
     *         {@code false} otherwise
     */
    @Deprecated
    public static boolean isLaterThan(Timestamp timestamp, Timestamp thanTime) {
        TimestampTemporal later = TimestampTemporal.from(timestamp);
        TimestampTemporal earlier = TimestampTemporal.from(thanTime);
        return later.isLaterThan(earlier);
    }

    /**
     * Parses a timestamp from an RFC-3339 date-time string.
     *
     * <p>Unlike {@link com.google.protobuf.util.Timestamps#parse(String) its Protobuf counterpart}
     * this method does not throw a checked exception.
     *
     * @throws IllegalArgumentException
     *         if the string is not of required format
     */
    @Deprecated
    public static Timestamp parse(String str) {
        return TimestampTemporal.parse(str)
                                .toTimestamp();
    }

    /**
     * Creates {@code Timestamp} by the passed {@code Instant} value.
     */
    @Deprecated
    public static Timestamp fromInstant(Instant instant) {
        return TimestampTemporal.from(instant)
                                .toTimestamp();
    }

    /**
     * Obtains converter of {@code Timestamp}s to {@code Instant}s.
     */
    @Deprecated
    public static Converter<Instant, Timestamp> converter() {
        return TimestampTemporal.converter();
    }
}
