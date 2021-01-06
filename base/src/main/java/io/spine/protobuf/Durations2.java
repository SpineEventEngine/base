/*
 * Copyright 2021, TeamDev. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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
import com.google.protobuf.Duration;
import com.google.protobuf.util.Durations;
import io.spine.string.Stringifiers;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.protobuf.util.Durations.compare;
import static com.google.protobuf.util.Durations.fromMillis;
import static com.google.protobuf.util.Durations.fromNanos;
import static com.google.protobuf.util.Durations.fromSeconds;
import static com.google.protobuf.util.Durations.toMillis;
import static io.spine.util.Math2.floorDiv;
import static io.spine.util.Math2.safeMultiply;

/**
 * Utility class for working with durations in addition to those available from the
 * {@link com.google.protobuf.util.Durations Durations} class in the Protobuf Util library.
 *
 * <p>Use {@code import static io.spine.protobuf.Durations2.*} for compact initialization
 * like this:
 * <pre>
 *     {@code
 *     Duration d = add(hours(2), minutes(30));
 *     }
 * </pre>
 *
 * @see com.google.protobuf.util.Durations Durations
 */
@SuppressWarnings({"UtilityClass", "ClassWithTooManyMethods"})
public final class Durations2 {

    public static final Duration ZERO = fromMillis(0L);

    /** The count of seconds in one minute. */
    @SuppressWarnings("NumericCastThatLosesPrecision")
    private static final int SECONDS_PER_MINUTE = (int) TimeUnit.MINUTES.toSeconds(1);

    /** The count of minutes in one hour. */
    @SuppressWarnings("NumericCastThatLosesPrecision")
    private static final int MINUTES_PER_HOUR = (int) TimeUnit.HOURS.toMinutes(1);

    /** The count of milliseconds in one second. */
    @SuppressWarnings("NumericCastThatLosesPrecision")
    private static final int MILLIS_PER_SECOND = (int) TimeUnit.SECONDS.toMillis(1);

    /** Prevent instantiation of this utility class. */
    private Durations2() {
    }

    /**
     * Obtains an instance of {@code Duration} representing the passed number of minutes.
     *
     * @param minutes
     *         the number of minutes, positive or negative.
     * @return a non-null {@code Duration}
     */
    public static Duration fromMinutes(long minutes) {
        Duration duration = fromSeconds(safeMultiply(minutes, SECONDS_PER_MINUTE));
        return duration;
    }

    /**
     * Obtains an instance of {@code Duration} representing the passed number of hours.
     *
     * @param hours
     *         the number of hours, positive or negative
     * @return a non-null {@code Duration}
     */
    public static Duration fromHours(long hours) {
        Duration duration = fromMinutes(safeMultiply(hours, MINUTES_PER_HOUR));
        return duration;
    }


    /*
     * Methods for brief computations with Durations like
     *       add(hours(2), minutes(30));
     ******************************************************/

    /**
     * Obtains an instance of {@code Duration} representing the passed number of nanoseconds.
     *
     * @param nanos
     *         the number of nanoseconds, positive or negative
     * @return a non-null {@code Duration}
     */
    public static Duration nanos(long nanos) {
        Duration duration = fromNanos(nanos);
        return duration;
    }

    /**
     * Obtains an instance of {@code Duration} representing the passed number of milliseconds.
     *
     * @param milliseconds
     *         the number of milliseconds, positive or negative
     * @return a non-null {@code Duration}
     */
    public static Duration milliseconds(long milliseconds) {
        return fromMillis(milliseconds);
    }

    /**
     * Obtains an instance of {@code Duration} representing the passed number of seconds.
     *
     * @param seconds
     *         the number of seconds, positive or negative
     * @return a non-null {@code Duration}
     */
    public static Duration seconds(long seconds) {
        return fromSeconds(seconds);
    }

    /**
     * This method allows for more compact code of creation of
     * {@code Duration} instance with minutes.
     */
    public static Duration minutes(long minutes) {
        return fromMinutes(minutes);
    }

    /**
     * This method allows for more compact code of creation of
     * {@code Duration} instance with hours.
     */
    public static Duration hours(long hours) {
        return fromHours(hours);
    }

    /**
     * Adds two durations one of which or both can be {@code null}.
     *
     * <p>This method supplements
     * the {@linkplain com.google.protobuf.util.Durations#add(Duration, Duration) utility} from
     * Protobuf Utils for accepting {@code null}s.
     *
     * @param d1
     *         a duration to add, could be {@code null}
     * @param d2
     *         another duration to add, could be {@code null}
     * @return <ul>
     *             <li>sum of two durations if both of them are {@code non-null}
     *             <li>another {@code non-null} value, if one is {@code null}
     *             <li>{@link #ZERO} if both values are {@code null}
     *         </ul>
     * @see com.google.protobuf.util.Durations#add(Duration, Duration)
     */
    public static Duration add(@Nullable Duration d1, @Nullable Duration d2) {
        if (d1 == null && d2 == null) {
            return ZERO;
        }
        if (d1 == null) {
            return d2;
        }
        if (d2 == null) {
            return d1;
        }
        Duration result = Durations.add(d1, d2);
        return result;
    }

    /**
     * This method allows for more compact code of creation of
     * {@code Duration} instance with hours and minutes.
     */
    public static Duration hoursAndMinutes(long hours, long minutes) {
        Duration result = add(hours(hours), minutes(minutes));
        return result;
    }

    /** Convert a duration to the number of nanoseconds. */
    public static long toNanos(Duration duration) {
        /* The sole purpose of this method is minimize the dependencies of the classes
           working with durations. */
        checkNotNull(duration);
        long result = Durations.toNanos(duration);
        return result;
    }

    /** Convert a duration to the number of seconds. */
    public static long toSeconds(Duration duration) {
        checkNotNull(duration);
        long millis = toMillis(duration);
        long seconds = floorDiv(millis, MILLIS_PER_SECOND);
        return seconds;
    }

    /**
     * Converts passed duration to long value of minutes.
     *
     * @param duration
     *         duration to convert
     * @return duration in minutes
     */
    public static long toMinutes(Duration duration) {
        checkNotNull(duration);
        long millis = toMillis(duration);
        long result = (millis / MILLIS_PER_SECOND) / SECONDS_PER_MINUTE;
        return result;
    }

    /**
     * Returns the number of hours in the passed duration.
     *
     * @param value
     *         duration
     * @return number of hours
     */
    public static long getHours(Duration value) {
        checkNotNull(value);
        long hours = toMinutes(value);
        long result = hours / MINUTES_PER_HOUR;
        return result;
    }

    /**
     * Returns the only remainder of minutes from the passed duration subtracting
     * the amount of full hours.
     *
     * @param value
     *         duration
     * @return number of minutes
     */
    public static int getMinutes(Duration value) {
        checkNotNull(value);
        long allMinutes = toMinutes(value);
        long remainder = allMinutes % MINUTES_PER_HOUR;
        int result = Long.valueOf(remainder)
                         .intValue();
        return result;
    }

    /**
     * Returns {@code true} of the passed value is greater or equal zero,
     * {@code false} otherwise.
     */
    public static boolean isPositiveOrZero(Duration value) {
        checkNotNull(value);
        long millis = toMillis(value);
        boolean result = millis >= 0;
        return result;
    }

    /**
     * Returns {@code true} if the passed value is greater than zero,
     * {@code false} otherwise.
     */
    public static boolean isPositive(Duration value) {
        checkNotNull(value);
        boolean secondsPositive = value.getSeconds() > 0;
        boolean nanosPositive = value.getNanos() > 0;
        boolean result = secondsPositive || nanosPositive;
        return result;

    }

    /** Returns {@code true} if the passed value is zero, {@code false} otherwise. */
    public static boolean isZero(Duration value) {
        checkNotNull(value);
        boolean noSeconds = value.getSeconds() == 0;
        boolean noNanos = value.getNanos() == 0;
        boolean result = noSeconds && noNanos;
        return result;
    }

    /**
     * Returns {@code true} if the first argument is greater than the second,
     * {@code false} otherwise.
     */
    public static boolean isGreaterThan(Duration value, Duration another) {
        boolean result = compare(value, another) > 0;
        return result;
    }

    /**
     * Returns {@code true} if the first argument is less than the second,
     * {@code false} otherwise.
     */
    public static boolean isLessThan(Duration value, Duration another) {
        boolean result = compare(value, another) < 0;
        return result;
    }

    /** Returns {@code true} if the passed duration is negative, {@code false} otherwise. */
    public static boolean isNegative(Duration value) {
        checkNotNull(value);
        long nanos = toNanos(value);
        boolean isNegative = nanos < 0;
        return isNegative;
    }

    /**
     * Converts the passed Java Time value.
     */
    public static Duration of(java.time.Duration value) {
        checkNotNull(value);
        return converter().convert(value);
    }

    /**
     * Converts the passed value to Java Time value.
     */
    public static java.time.Duration toJavaTime(Duration value) {
        checkNotNull(value);
        return converter().reverse()
                          .convert(value);
    }

    /**
     * Parses the string with a duration.
     *
     * <p>Unlike {@link com.google.protobuf.util.Durations#parse(String) its Protobuf counterpart},
     * this method does not throw a checked exception.
     *
     * @throws IllegalArgumentException
     *         if the string is not of required format
     */
    public static Duration parse(String str) {
        checkNotNull(str);
        return Stringifiers.forDuration()
                           .reverse()
                           .convert(str);
    }

    /**
     * Obtains the instance of Java Time converter.
     */
    public static Converter<java.time.Duration, Duration> converter() {
        return JtConverter.INSTANCE;
    }

    /**
     * Converts from Java Time {@code Duration} to Protobuf {@code Duration} and back.
     */
    private static final class JtConverter
            extends Converter<java.time.Duration, Duration> implements Serializable {

        private static final long serialVersionUID = 0L;

        private static final JtConverter INSTANCE = new JtConverter();

        @Override
        protected Duration doForward(java.time.Duration duration) {
            Duration.Builder result = Duration
                    .newBuilder()
                    .setSeconds(duration.getSeconds())
                    .setNanos(duration.getNano());
            return result.build();
        }

        @Override
        protected java.time.Duration doBackward(Duration duration) {
            java.time.Duration result = java.time.Duration
                    .ofSeconds(duration.getSeconds(), duration.getNanos());
            return result;
        }

        @Override
        public String toString() {
            return "Durations2.converter()";
        }

        private Object readResolve() {
            return INSTANCE;
        }

    }
}
