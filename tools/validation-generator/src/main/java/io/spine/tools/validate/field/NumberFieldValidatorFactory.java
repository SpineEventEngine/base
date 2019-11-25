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
import static io.spine.util.Exceptions.newIllegalArgumentException;
import static java.lang.String.format;
import static java.lang.String.valueOf;

public final class NumberFieldValidatorFactory extends AbstractFieldValidatorFactory {

    private static final Pattern NUMBER_RANGE =
            Pattern.compile("([\\[(])\\s*([+\\-]?[\\d.]+)\\s*\\.\\.\\s*([+\\-]?[\\d.]+)\\s*([])])");

    private final NumberKind numberKind;

    NumberFieldValidatorFactory(FieldDeclaration field,
                                Expression fieldAccess,
                                FieldCardinality cardinality) {
        super(field, fieldAccess, cardinality);
        this.numberKind = NumberKind.forField(field);
    }

    @Override
    protected ImmutableList<Rule> rules() {
        NumberBoundaries boundaries = parseBoundaries();
        ImmutableList.Builder<Rule> rules = ImmutableList.builder();
        if (boundaries.hasMin()) {
            Boundary min = boundaries.min();
            Rule rule = boundaryRule(min, "<", "<=");
            rules.add(rule);
        }
        if (boundaries.hasMax()) {
            Boundary max = boundaries.max();
            Rule rule = boundaryRule(max, ">", ">=");
            rules.add(rule);
        }
        return rules.build();
    }

    @Override
    public Expression<Boolean> isNotSet() {
        return Expression.of(valueOf(false));
    }

    private Rule boundaryRule(Boundary boundary,
                              String exclusiveOperator,
                              String inclusiveOperator) {
        String operator = boundary.inclusive() ? exclusiveOperator : inclusiveOperator;
        Rule rule = new Rule(
                field -> formatted("%s %s %s", field, operator, boundary.value()),
                field -> violationTemplate()
                        .setMessage(format("Field must be %s %s.", operator, boundary.value()))
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
            low = new Boundary(numberKind.parse(minOption.getValue()), !minOption.getExclusive());

        }
        Boundary high = null;
        MaxOption maxOption = field().findOption(max);
        if (isNotDefault(maxOption)) {
            high = new Boundary(numberKind.parse(maxOption.getValue()), !maxOption.getExclusive());
        }
        return new NumberBoundaries(low, high);
    }

    private NumberBoundaries rangeBoundaries() {
        String rangeOption = field().findOption(range);
        Matcher rangeMatcher = NUMBER_RANGE.matcher(rangeOption);
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

    private enum NumberKind {

        INTEGER {
            @Override
            Number parse(String value) {
                return Long.parseLong(value);
            }
        },
        FLOAT {
            @Override
            Number parse(String value) {
                return Double.parseDouble(value);
            }
        };

        abstract Number parse(String value);

        @SuppressWarnings("EnumSwitchStatementWhichMissesCases")
        // `default` covers everything else.
        static NumberKind forField(FieldDeclaration field) {
            JavaType type = field.javaType();
            switch (type) {
                case INT:
                case LONG:
                    return INTEGER;
                case FLOAT:
                case DOUBLE:
                    return FLOAT;
                default:
                    throw newIllegalArgumentException("Unexpected type of field: %s.", type);
            }
        }
    }
}
