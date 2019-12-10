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
import io.spine.code.proto.FieldContext;
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
public final class RangeConstraint
        extends RangedConstraint<String> {

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
        Matcher rangeMatcher = NUMBER_RANGE.matcher(rangeOption.trim());
        if (!rangeOption.isEmpty()) {
            checkState(rangeMatcher.matches(),
                       "Range '%s' on field `%s` is invalid.", rangeOption, field);
            boolean minInclusive = rangeMatcher.group(1)
                                               .equals("[");
            ComparableNumber minValue = new NumberText(rangeMatcher.group(2)).toNumber();
            ComparableNumber maxValue = new NumberText(rangeMatcher.group(3)).toNumber();
            boolean maxInclusive = rangeMatcher.group(4)
                                               .equals("]");
            if (minInclusive) {
                return maxInclusive ? closed(minValue, maxValue) : closedOpen(minValue, maxValue);
            } else {
                return maxInclusive ? openClosed(minValue, maxValue) : open(minValue, maxValue);
            }
        } else {
            return Range.all();
        }
    }

    @Override
    public String errorMessage(FieldContext field) {
        return format("Field %s is out of range.", field());
    }

    @Override
    protected String compileErrorMessage(Range<ComparableNumber> range) {
        return new StringBuilder(format("Value of `%s`", field()))
                .append(forLowerBound())
                .append(range().lowerEndpoint())
                .append(" and ")
                .append(forUpperBound())
                .append(range().upperEndpoint())
                .append('.')
                .toString();
    }

    private String forLowerBound() {
        String greaterThan = "greater than %s";
        return format(greaterThan, orEqualTo(range().lowerBoundType()));
    }

    private String forUpperBound() {
        String lessThan = "less than %s";
        return format(lessThan, orEqualTo(range().lowerBoundType()));
    }

    @Override
    public void accept(ConstraintTranslator<?> visitor) {
        visitor.visitRange(this);
    }
}
