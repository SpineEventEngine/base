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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Range;
import com.google.errorprone.annotations.Immutable;
import io.spine.code.proto.FieldDeclaration;
import io.spine.validate.ComparableNumber;
import io.spine.validate.ConstraintTranslator;
import io.spine.validate.NumberText;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Range.closed;
import static com.google.common.collect.Range.closedOpen;
import static com.google.common.collect.Range.open;
import static com.google.common.collect.Range.openClosed;
import static java.lang.String.format;

/**
 * A constraint that checks whether a value fits the ranged described by expressions such as
 * {@code int32 value = 5 [(range) = "[3..5)]}, describing a value that is at least 3 and less
 * than 5.
 */
@Immutable
public final class RangeConstraint extends RangedConstraint<String> {

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
    private static final Pattern NUMBER_RANGE =
            Pattern.compile("([\\[(])\\s*([+\\-]?[\\d.]+)\\s*\\.\\.\\s*([+\\-]?[\\d.]+)\\s*([])])");

    RangeConstraint(String optionValue, FieldDeclaration field) {
        super(optionValue, rangeFromOption(optionValue, field), field);
    }

    @VisibleForTesting
    static Range<ComparableNumber> rangeFromOption(String rangeOption, FieldDeclaration field) {
        return !rangeOption.isEmpty()
               ? compileRange(rangeOption, field)
               : Range.all();
    }

    private static Range<ComparableNumber>
    compileRange(String rangeOption, FieldDeclaration field) {
        Matcher rangeMatcher = NUMBER_RANGE.matcher(rangeOption.trim());
        checkState(rangeMatcher.matches(),
                   "Malformed range `%s` on the field `%s`. " +
                   "Must have a form of `[a..b]` " +
                   "where `a` and `b` are valid literals of the type `%s`. " +
                   "See doc of `(range)` for more details.",
                   rangeOption, field, field.javaTypeName());
        boolean minInclusive = rangeMatcher.group(1).equals("[");
        ComparableNumber minValue = new NumberText(rangeMatcher.group(2)).toNumber();
        ComparableNumber maxValue = new NumberText(rangeMatcher.group(3)).toNumber();
        boolean maxInclusive = rangeMatcher.group(4).equals("]");
        if (minInclusive) {
            return maxInclusive ? closed(minValue, maxValue) : closedOpen(minValue, maxValue);
        } else {
            return maxInclusive ? openClosed(minValue, maxValue) : open(minValue, maxValue);
        }
    }

    @Override
    protected String compileErrorMessage(Range<ComparableNumber> range) {
        return format("The value of the field `%s` is out of range. Must be %s%s and %s%s.",
                      field(),
                      forLowerBound(range), range.lowerEndpoint(),
                      forUpperBound(range), range.upperEndpoint());
    }

    private static String forLowerBound(Range<?> range) {
        return format("greater than %s", orEqualTo(range.lowerBoundType()));
    }

    private static String forUpperBound(Range<?> range) {
        return format("less than %s", orEqualTo(range.upperBoundType()));
    }

    @Override
    public void accept(ConstraintTranslator<?> visitor) {
        visitor.visitRange(this);
    }
}
