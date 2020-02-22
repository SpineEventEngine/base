/*
 * Copyright 2020, TeamDev. All rights reserved.
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

import com.google.common.annotations.VisibleForTesting;
import com.google.protobuf.Timestamp;
import io.spine.annotation.Internal;

import javax.annotation.concurrent.ThreadSafe;
import java.time.Instant;
import java.time.ZoneId;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.concurrent.TimeUnit.MICROSECONDS;

/**
 * Utilities for working with time information.
 */
public final class Time {

    private static Provider timeProvider = SystemTimeProvider.INSTANCE;

    /** Prevents instantiation of this utility class. */
    private Time() {
    }

    /**
     * Obtains current time via the current {@link Time.Provider}.
     *
     * @return current time
     * @see #setProvider(Provider)
     */
    public static synchronized Timestamp currentTime() {
        Timestamp result = timeProvider.currentTime();
        return result;
    }

    /**
     * Obtains system time.
     *
     * <p>The values returned are guaranteed to be different for the consecutive calls in scope
     * of a single JVM. This is achieved by incrementing the default system time millis
     * by the emulated nanosecond value.
     *
     * <p>The nanoseconds are computed in a thread-safe incremental way starting at zero
     * and ending by {@code 999 999}, leaving the millisecond value intact.
     *
     * <p>Unlike {@link #currentTime()}, this method <strong>always</strong> relies on the system
     * milliseconds.
     *
     * @return current system time
     */
    public static Timestamp systemTime() {
        return SystemTimeProvider.INSTANCE.currentTime();
    }

    /**
     * Obtains the current time zone ID.
     *
     * @return the {@link ZoneId} of the current time zone
     */
    public static synchronized ZoneId currentTimeZone() {
        return timeProvider.currentZone();
    }

    /**
     * Sets provider of the current time.
     *
     * <p>The most common scenario for using this method is test cases of code that deals
     * with current time.
     *
     * @param provider
     *         the provider to set
     */
    @Internal
    @VisibleForTesting
    public static synchronized void setProvider(Provider provider) {
        timeProvider = checkNotNull(provider);
    }

    /**
     * Sets the default current time provider that obtains current time based on the system
     * millis and the emulated nanosecond value.
     *
     * @see #systemTime() for more details
     */
    public static synchronized void resetProvider() {
        timeProvider = SystemTimeProvider.INSTANCE;
    }

    /**
     * The provider of the current time.
     *
     * <p>Implement this interface and pass the resulting class to {@link #setProvider(Provider)}
     * in order to change the {@link Time#currentTime()} results.
     */
    @Internal
    public interface Provider {

        /**
         * Obtains the current time in UTC.
         */
        Timestamp currentTime();

        /**
         * Obtains the current time zone ID.
         *
         * @implSpec The default implementation uses the {@link ZoneId#systemDefault()}
         *         zone.
         */
        default ZoneId currentZone() {
            return ZoneId.systemDefault();
        }
    }

    /**
     * Default implementation of current time provider based on {@link Instant#now()} and
     * the emulated nanosecond value.
     *
     * @see IncrementalNanos for more details on the nanosecond emulation
     */
    @VisibleForTesting
    static class SystemTimeProvider implements Provider {

        @VisibleForTesting
        static final Provider INSTANCE = new SystemTimeProvider();

        /** Prevent instantiation from outside. */
        private SystemTimeProvider() {
        }

        @Override
        public Timestamp currentTime() {
            Instant now = Instant.now();
            int nanosOnly = IncrementalNanos.valueForTime(now);
            Timestamp result = Timestamp.newBuilder()
                                        .setSeconds(now.getEpochSecond())
                                        .setNanos(now.getNano() + nanosOnly)
                                        .build();
            return result;
        }
    }

    /**
     * Provides an incremental value of nanoseconds for the local JVM.
     *
     * <p>In most cases, the JVM and underlying OS provides the millisecond-level precision at best.
     * Therefore, the messages produced in such a virtual machine are often stamped
     * with the same time value. However, most of the message-ordering routines require
     * the distinct time values for proper work.
     *
     * <p>This class is designed to emulate the nanoseconds and provide incremental values
     * for the consecutive calls.
     *
     * <p>Due to the limitations of the most storage engines, which round the time values to
     * the nearest or the lowest microsecond, the nanosecond values produced by
     * this class are incremented by {@code 1 000} nanoseconds, i.e. by {@code 1} microsecond
     * per call.
     *
     * <p>The returned nanosecond value starts at {@code 0} and never exceeds {@code 999 999}.
     * It is designed to keep the millisecond value provided by a typical-JVM system clock intact.
     *
     * <p>The nanosecond value is reset for each new passed {@link Instant} value.
     * That allows to receive {@code 1 000} distinct time values per millisecond.
     *
     * <p>In case the upper bound of the nanos is reached, meaning that there were more than
     * {@code 1 000} calls to this class within a millisecond, the nanosecond value is reset
     * back to {@code 0}.
     */
    @ThreadSafe
    static final class IncrementalNanos {

        private static final int MAX_VALUE = 1_000_000;

        @SuppressWarnings("NumericCastThatLosesPrecision")
        private static final int NANOS_PER_MICROSECOND = (int) MICROSECONDS.toNanos(1);

        private static final IncrementalNanos instance = new IncrementalNanos();

        private int counter;
        private Instant previousValue;

        private synchronized int getNextValue(Instant forTime) {
            if (forTime.equals(previousValue)) {
                previousValue = forTime;
                counter += NANOS_PER_MICROSECOND;
                counter = counter % MAX_VALUE;
            } else {
                previousValue = forTime;
                counter = 0;
            }
            return counter;
        }

        /**
         * Obtains the next nanosecond value.
         */
        static int valueForTime(Instant forTime) {
            return instance.getNextValue(forTime);
        }
    }
}
