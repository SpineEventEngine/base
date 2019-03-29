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

import com.google.protobuf.Any;
import com.google.protobuf.Timestamp;
import io.spine.base.Time;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.protobuf.util.Timestamps.compare;

/**
 * A point in time represented with a certain accuracy.
 *
 * <p>The name of this interface is inspired by the {@link java.time.temporal.Temporal}.
 *
 * <p>Provides a {@linkplain #compareTo(Temporal) default implementation} comparison of two points
 * in time. It is not supposed that the implementation would override this comparison mechanism.
 *
 * @param <T>
 *         the type of itself
 */
public interface Temporal<T extends Temporal<T>> extends Comparable<T> {

    /**
     * Obtains this point in time as a Protobuf {@link Timestamp}.
     *
     * <p>The Protobuf {@code Timestamp} represents the UTC Epoch time. All the implementations
     * should assemble timestamps regarding that fact.
     *
     * @return this is a {@code Timestamp}
     */
    Timestamp toTimestamp();

    /**
     * Packs this point in time into an {@link Any}.
     *
     * @return itself packed as {@code Any}
     */
    Any toAny();

    /**
     * Compares this point in time to the given one.
     *
     * <p>The {@code other} point should have <strong>the exact</strong> runtime type as this one.
     * Otherwise, an {@code IllegalArgumentException} is thrown. The same constraint is applicable
     * to all the other comparison methods of {@code Temporal}.
     *
     * @param other
     *         the value to compare to
     * @return <ul>
     *             <li>an integer greater than 0 if point in time occurs later than the other
     *             <li>an integer less than 0 if point in time occurs earlier than other
     *             <li>and 0 of these points in time are identical
     *         </ul>
     * @implNote Translates both temporal values into {@code Timestamp}s and compares them.
     */
    @Override
    default int compareTo(T other) {
        checkNotNull(other);
        Class<? extends Temporal> thisClass = getClass();
        Class<? extends Temporal> otherClass = other.getClass();
        checkArgument(thisClass == otherClass,
                      "Expected an instance of %s but got %s.",
                      thisClass.getCanonicalName(),
                      otherClass.getCanonicalName());
        Timestamp thisTimestamp = toTimestamp();
        Timestamp otherTimestamp = other.toTimestamp();
        int result = compare(thisTimestamp, otherTimestamp);
        return result;
    }

    /**
     * Checks if this point is time occurs earlier than the other one.
     */
    default boolean isEarlierThan(T other) {
        return compareTo(other) < 0;
    }

    /**
     * Checks if this point is time occurs earlier than the other one or they coincide.
     */
    default boolean isEarlierOrSameAs(T other) {
        return compareTo(other) <= 0;
    }

    /**
     * Checks if this point is time occurs later than the other one.
     */
    default boolean isLaterThan(T other) {
        return compareTo(other) > 0;
    }

    /**
     * Checks if this point is time occurs later than the other one or they coincide.
     */
    default boolean isLaterOrSameAs(T other) {
        return compareTo(other) >= 0;
    }

    /**
     * Checks if this point in time coincides with the given one.
     */
    default boolean isSameAs(T other) {
        return compareTo(other) == 0;
    }

    /**
     * Checks if this point is time lies between the given.
     *
     * <p>All three {@code Temporal}s must exactly the same runtime type. Otherwise,
     * an {@code IllegalArgumentException} is thrown.
     *
     * @param periodStart
     *         lower bound, exclusive
     * @param periodEnd
     *         higher bound, inclusive
     * @return {@code true} if this point in time lies in between the given two
     */
    default boolean isBetween(T periodStart, T periodEnd) {
        checkArgument(periodStart.isEarlierThan(periodEnd),
                      "Period start `%s` must be earlier than period end `%s`.",
                      periodStart,
                      periodEnd);
        return this.isLaterThan(periodStart)
            && this.isEarlierOrSameAs(periodEnd);
    }

    /**
     * Checks that this point in time lies in the future.
     *
     * <p>Uses {@link Time#currentTime()} to determine the "current" time to compare to.
     *
     * @return {@code true} if this point is time is later than the current time,
     *         {@code false} otherwise
     */
    default boolean isInFuture() {
        Timestamp now = Time.currentTime();
        Timestamp thisTime = toTimestamp();
        return compare(thisTime, now) > 0;
    }

    /**
     * Checks that this point in time lies in the past.
     *
     * <p>Uses {@link Time#currentTime()} to determine the "current" time to compare to.
     *
     * @return {@code true} if this point is time is earlier than the current time,
     *         {@code false} otherwise
     */
    default boolean isInPast() {
        Timestamp now = Time.currentTime();
        Timestamp thisTime = toTimestamp();
        return compare(thisTime, now) < 0;
    }
}
