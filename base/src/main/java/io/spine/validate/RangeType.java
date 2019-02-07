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

package io.spine.validate;

import com.google.common.collect.Range;

import java.util.function.BiFunction;

import static io.spine.util.Exceptions.newIllegalArgumentException;
import static io.spine.util.Preconditions2.checkNotEmptyOrBlank;
import static java.lang.String.format;

/**
 * A type of range.
 *
 * <p>Can be {@code open} or {@code closed} from both sides, meaning that the edge values
 * is either excluded or included from the range.
 */
enum RangeType {
    CLOSED("[]") {
        @Override
        BiFunction<ComparableNumber,
                ComparableNumber,
                com.google.common.collect.Range<ComparableNumber>>
        rangeFrom() {
            return com.google.common.collect.Range::closed;
        }
    },
    OPEN("()") {
        @Override
        BiFunction<ComparableNumber,
                ComparableNumber,
                com.google.common.collect.Range<ComparableNumber>> rangeFrom() {
            return com.google.common.collect.Range::open;
        }
    },
    OPEN_CLOSED("(]") {
        @Override
        BiFunction<ComparableNumber,
                ComparableNumber,
                com.google.common.collect.Range<ComparableNumber>> rangeFrom() {
            return com.google.common.collect.Range::openClosed;
        }
    },
    CLOSED_OPEN("[)") {
        @Override
        BiFunction<ComparableNumber,
                ComparableNumber,
                com.google.common.collect.Range<ComparableNumber>> rangeFrom() {
            return com.google.common.collect.Range::closedOpen;
        }
    };

    private final String edges;

    RangeType(String edges) {
        this.edges = edges;
    }

    private static RangeType from(String value) {
        for (RangeType type : values()) {
            if (value.equals(type.edges)) {
                return type;
            }
        }
        throw newIllegalArgumentException(format("Could not create a range for edges %s.", value));
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
        return from(String.valueOf(first) + last);
    }

    /**
     * Obtains a function that from two numbers, obtains a range of them, the kind of which
     * depends on the exact type of range.
     */
    abstract BiFunction<ComparableNumber, ComparableNumber, Range<ComparableNumber>>
    rangeFrom();
}
