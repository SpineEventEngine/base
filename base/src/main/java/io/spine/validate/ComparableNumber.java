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

package io.spine.validate;

import com.google.common.base.Objects;
import com.google.errorprone.annotations.Immutable;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A number that can be compared to another number.
 *
 * <p>Note that for values that are outside {@code Long} range, or when a
 * precision beyond that of {@code Double} is required, instances of this class
 * yield incorrect comparison results.
 */
@Immutable
@SuppressWarnings("ComparableType") // Can be compared to any number.
public final class ComparableNumber extends Number implements Comparable<Number> {

    private static final long serialVersionUID = 0L;
    @SuppressWarnings("Immutable") // effectively
    private final Number value;

    /** Creates a new instance from the specified number. */
    public ComparableNumber(Number value) {
        super();
        this.value = checkNotNull(value);
    }

    /** Converts this number to its textual representation. */
    public NumberText toText() {
        return new NumberText(value);
    }

    /** Returns the actual wrapped number. */
    public Number value() {
        return value;
    }

    @Override
    public int compareTo(Number anotherNumber) {
        checkNotNull(anotherNumber);

        long thisLong = longValue();
        long thatLong = anotherNumber.longValue();
        if (thisLong == thatLong) {
            return Double.compare(doubleValue(), anotherNumber.doubleValue());
        }
        return Long.compare(thisLong, thatLong);
    }

    @Override
    public int intValue() {
        return value.intValue();
    }

    @Override
    public long longValue() {
        return value.longValue();
    }

    @Override
    public float floatValue() {
        return value.floatValue();
    }

    @Override
    public double doubleValue() {
        return value.doubleValue();
    }

    /**
     * Checks if this number is a whole number, i.e. an {@code int} or a {@code long}.
     */
    public boolean isInteger() {
        return value instanceof Integer || value instanceof Long;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ComparableNumber number = (ComparableNumber) o;
        return Objects.equal(value, number.value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
