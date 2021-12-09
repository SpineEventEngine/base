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

package io.spine.validate.option;

import com.google.common.collect.Range;
import io.spine.code.proto.FieldDeclaration;
import io.spine.validate.ComparableNumber;
import io.spine.validate.NumberText;

import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Range.closed;
import static com.google.common.collect.Range.closedOpen;
import static com.google.common.collect.Range.open;
import static com.google.common.collect.Range.openClosed;

/**
 * Transforms a string value defined in a field declaration into a {@link Range} of
 * {@link ComparableNumber}s.
 */
final class RangeDecl {

    /**
     * The regular expression for parsing number ranges.
     *
     * <p>Defines four groups:
     * <ol>
     *     <li>The opening bracket (a {@code [} or a {@code (}).
     *     <li>The lower numerical bound.
     *     <li>The higher numerical bound.
     *     <li>The closing bracket (a {@code ]} or a {@code )}).
     * </ol>
     *
     * <p>All the groups as well as a {@code ..} divider between the numerical bounds must be
     * matched. Extra spaces among the groups and the divider are allowed.
     *
     * <p>Examples of a valid number range:
     * <ul>
     *     <li>{@code [0..1]}
     *     <li>{@code ( -17.3 .. +146.0 ]}
     *     <li>{@code [+1..+100)}
     * </ul>
     *
     * <p>Examples of an invalid number range:
     * <ul>
     *     <li>{@code 1..5} - missing brackets.
     *     <li>{@code [0 - 1]} - wrong divider.
     *     <li>{@code [0 . . 1]} - divider cannot be split with spaces.
     *     <li>{@code ( .. 0)} - missing lower bound.
     * </ul>
     */
    private static final Pattern NUMBER_RANGE = Pattern.compile(
            "([\\[(])\\s*([+\\-]?[\\d.]+)\\s*\\.\\.\\s*([+\\-]?[\\d.]+)\\s*([])])"
    );

    private final boolean minInclusive;
    private final ComparableNumber min;
    private final ComparableNumber max;
    private final boolean maxInclusive;

    /**
     * Transforms the passed range declaration into a range of numbers.
     */
    static Range<ComparableNumber> compile(String rangeOptionValue, FieldDeclaration field) {
        var decl = new RangeDecl(rangeOptionValue, field);
        return decl.toRange();
    }

    private RangeDecl(String rangeOptionValue, FieldDeclaration field) {
        var rangeMatcher = NUMBER_RANGE.matcher(rangeOptionValue.trim());
        checkState(rangeMatcher.matches(),
                   "Malformed range `%s` on the field `%s`. " +
                           "Must have a form of `[a..b]` " +
                           "where `a` and `b` are valid literals of the type `%s`. " +
                           "Please see the documentation of the `(range)` option for more details.",
                   rangeOptionValue, field, field.javaTypeName());
        this.minInclusive = "[".equals(rangeMatcher.group(1));
        this.min = new NumberText(rangeMatcher.group(2)).toNumber();
        this.max = new NumberText(rangeMatcher.group(3)).toNumber();
        this.maxInclusive = "]".equals(rangeMatcher.group(4));
    }

    private Range<ComparableNumber> toRange() {
        if (minInclusive) {
            return maxInclusive
                   ? closed(min, max)
                   : closedOpen(min, max);
        } else {
            return maxInclusive
                   ? openClosed(min, max)
                   : open(min, max);
        }
    }
}
