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

package io.spine.tools.validate.field;

import com.google.common.collect.ImmutableList;
import com.google.protobuf.Descriptors.FieldDescriptor.JavaType;
import io.spine.code.proto.FieldDeclaration;
import io.spine.logging.Logging;
import io.spine.option.MaxOption;
import io.spine.option.MinOption;
import io.spine.tools.validate.code.Expression;
import io.spine.tools.validate.number.Boundary;
import io.spine.tools.validate.number.NumberBoundaries;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkState;
import static io.spine.option.OptionsProto.max;
import static io.spine.option.OptionsProto.min;
import static io.spine.option.OptionsProto.range;
import static io.spine.protobuf.Messages.isNotDefault;
import static io.spine.tools.validate.code.Expression.formatted;
import static java.lang.String.format;
import static java.lang.String.valueOf;

/**
 * A {@link FieldValidatorFactory} for number fields.
 */
public final class NumberFieldValidatorFactory
        extends SingularFieldValidatorFactory
        implements Logging {

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

    private final NumberKind numberKind;

    NumberFieldValidatorFactory(FieldDeclaration field,
                                JavaType type,
                                Expression fieldAccess,
                                FieldCardinality cardinality) {
        super(field, fieldAccess, cardinality);
        this.numberKind = NumberKind.forField(type);
    }

    @Override
    protected ImmutableList<Rule> rules() {
        NumberBoundaries boundaries = parseBoundaries();
        ImmutableList.Builder<Rule> rules = ImmutableList.builder();
        if (boundaries.hasMin()) {
            Boundary min = boundaries.min();
            @SuppressWarnings("DuplicateStringLiteralInspection") // In tests.
            Constraint rule = boundaryRule(min, "<", "<=", "greater");
            rules.add(rule);
        }
        if (boundaries.hasMax()) {
            Boundary max = boundaries.max();
            Constraint rule = boundaryRule(max, ">", ">=", "less");
            rules.add(rule);
        }
        return rules.build();
    }

    @Override
    public Expression<Boolean> isNotSet() {
        return Expression.of(valueOf(false));
    }

    @Override
    public boolean supportsRequired() {
        return false;
    }

    private Constraint boundaryRule(Boundary boundary,
                                    String exclusiveOperator,
                                    String inclusiveOperator,
                                    String englishDescription) {
        boolean inclusive = boundary.inclusive();
        String operator = inclusive ? exclusiveOperator : inclusiveOperator;
        Constraint rule = new Constraint(
                field -> formatted("%s %s %s", field, operator, boundary.value()),
                field -> violationTemplate()
                        .setMessage(format("Field must be %s than%s %s.",
                                           englishDescription,
                                           inclusive ? " or equal to" : "",
                                           boundary.value()))
                        .setFieldValue(field)
                        .build()
        );
        return rule;
    }

    private NumberBoundaries parseBoundaries() {
        checkNotTooMuchOptions();
        NumberBoundaries boundaries = minMaxBoundaries();
        return boundaries.isBound()
               ? boundaries
               : rangeBoundaries();
    }

    private void checkNotTooMuchOptions() {
        FieldDeclaration field = field();
        checkState(!(field.hasOption(range) && (field.hasOption(min) || field.hasOption(max))),
                   "Detected usage of (range) alongside with (min) or (max) at `%s`.", field);
    }

    private NumberBoundaries minMaxBoundaries() {
        Boundary low = null;
        MinOption minOption = field().findOption(min);
        if (isNotDefault(minOption)) {
            low = boundary(minOption.getValue(), minOption.getExclusive());

        }
        Boundary high = null;
        MaxOption maxOption = field().findOption(max);
        if (isNotDefault(maxOption)) {
            high = boundary(maxOption.getValue(), maxOption.getExclusive());
        }
        return new NumberBoundaries(low, high);
    }

    private Boundary boundary(String value, boolean exclusive) {
        return new Boundary(numberKind.parse(value), !exclusive);
    }

    private NumberBoundaries rangeBoundaries() {
        String rangeOption = field().findOption(range);
        Matcher rangeMatcher = NUMBER_RANGE.matcher(rangeOption.trim());
        if (!rangeOption.isEmpty()) {
            checkState(rangeMatcher.matches(),
                       "Range '%s' on field %s is invalid.", rangeOption, field());
            boolean minInclusive = rangeMatcher.group(1)
                                               .equals("[");
            Number minValue = numberKind.parse(rangeMatcher.group(2));
            Number maxValue = numberKind.parse(rangeMatcher.group(3));
            boolean maxInclusive = rangeMatcher.group(4)
                                               .equals("]");
            return new NumberBoundaries(
                    new Boundary(minValue, minInclusive),
                    new Boundary(maxValue, maxInclusive)
            );
        } else {
            return NumberBoundaries.unbound();
        }
    }

}
