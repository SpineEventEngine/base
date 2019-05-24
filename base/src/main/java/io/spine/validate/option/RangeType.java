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

package io.spine.validate.option;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Range;
import com.google.errorprone.annotations.Immutable;
import io.spine.validate.ComparableNumber;

import java.io.Serializable;
import java.util.Arrays;
import java.util.function.BiFunction;
import java.util.function.Predicate;

import static io.spine.util.Preconditions2.checkNotEmptyOrBlank;
import static java.lang.String.format;

/**
 * A type of range.
 *
 * <p>Can be {@code open} or {@code closed} from both sides, meaning that the edge values
 * is either excluded or included from the range.
 */
enum RangeType {
    CLOSED('[', ']', Range::closed),
    OPEN('(', ')', Range::open),
    OPEN_CLOSED('(', ']', Range::openClosed),
    CLOSED_OPEN('[', ')', Range::closedOpen);

    private final char left;
    private final char right;
    private final RangeFunction function;

    RangeType(char left, char right, RangeFunction function) {
        this.left = left;
        this.right = right;
        this.function = function;
    }

    /** Obtains the character value of the left border, either {@code "[" }or {@code "("}.*/
    @VisibleForTesting
    public char left() {
        return left;
    }

    /** Obtains the character value of the right border, either {@code "]" }or {@code ")"}.*/
    @VisibleForTesting
    public char right() {
        return right;
    }

    private static RangeType from(char left, char right) {
        Predicate<RangeType> edgesMatch = value -> value.left == left && value.right == right;
        return Arrays.stream(values())
                     .filter(edgesMatch)
                     .findFirst()
                     .orElseThrow(() -> new IllegalArgumentException(
                             format("Could not create a range for edges %s, %s.", left, right)));
    }

    /**
     * Obtains a range instance that is described with the specified {@code String}
     * If such range could not be found, an {@code IllegalStateException} is thrown.
     */
    public static RangeType parse(String value) {
        checkNotEmptyOrBlank(value);
        String trimmed = value.trim();
        char first = trimmed.charAt(0);
        char last = trimmed.charAt(trimmed.length() - 1);
        return from(first, last);
    }

    Range<ComparableNumber> create(ComparableNumber left, ComparableNumber right) {
        return function.apply(left, right);
    }
    
    /**
     * A function that returns a new range between two {@code ComparableNumbers}.
     */
    @Immutable
    private interface RangeFunction
            extends BiFunction<ComparableNumber, ComparableNumber, Range<ComparableNumber>>,
                    Serializable {
    }
}

