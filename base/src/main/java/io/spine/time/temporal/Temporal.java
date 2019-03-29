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

import com.google.protobuf.Timestamp;
import io.spine.base.Time;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.protobuf.util.Timestamps.compare;

public interface Temporal<T extends Temporal<T>> extends Comparable<T> {

    Timestamp toTimestamp();

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

    default boolean isEarlierThan(T other) {
        return compareTo(other) < 0;
    }

    default boolean isEarlierOrSameAs(T other) {
        return compareTo(other) <= 0;
    }

    default boolean isLaterThan(T other) {
        return compareTo(other) > 0;
    }

    default boolean isLaterOrSameAs(T other) {
        return compareTo(other) >= 0;
    }

    default boolean isSameAs(T other) {
        return compareTo(other) == 0;
    }

    default boolean isBetween(T periodStart, T periodEnd) {
        checkArgument(periodStart.isEarlierThan(periodEnd),
                      "Period start `%s` must be earlier than period end `%s`.",
                      periodStart,
                      periodEnd);
        return this.isLaterThan(periodStart)
            && this.isEarlierOrSameAs(periodEnd);
    }

    default boolean isInFuture() {
        Timestamp now = Time.currentTime();
        Timestamp thisTime = toTimestamp();
        return compare(thisTime, now) > 0;
    }

    default boolean isInPast() {
        Timestamp now = Time.currentTime();
        Timestamp thisTime = toTimestamp();
        return compare(thisTime, now) < 0;
    }
}
