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

package io.spine.tools.validate.number;

import com.google.common.base.Objects;
import io.spine.validate.ComparableNumber;
import org.checkerframework.checker.nullness.qual.Nullable;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Set of numerical boundaries for a certain number.
 */
public final class NumberBoundaries {

    private final @Nullable Boundary min;
    private final @Nullable Boundary max;

    public NumberBoundaries(@Nullable Boundary min,
                            @Nullable Boundary max) {
        this.min = min;
        this.max = max;
        checkConsistentBoundaries();
    }

    private void checkConsistentBoundaries() {
        if (min != null && max != null) {
            ComparableNumber comparableMin = new ComparableNumber(min.value());
            checkArgument(comparableMin.compareTo(max.value()) <= 0,
                          "Lower bound (%s) is greater than the higher bound (%s).",
                          min.value(),
                          max.value());
        }
    }

    /**
     * {@code NumberBoundaries} which apply no constraints to the number.
     */
    public static NumberBoundaries unbound() {
        return new NumberBoundaries(null, null);
    }

    public boolean hasMin() {
        return min != null;
    }

    public Boundary min() {
        return checkNotNull(min);
    }

    public boolean hasMax() {
        return max != null;
    }

    public Boundary max() {
        return checkNotNull(max);
    }

    public boolean isBound() {
        return hasMin() || hasMax();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof NumberBoundaries)) {
            return false;
        }
        NumberBoundaries that = (NumberBoundaries) o;
        return Objects.equal(min, that.min) &&
                Objects.equal(max, that.max);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(min, max);
    }
}
