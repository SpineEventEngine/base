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

import static java.lang.String.format;

/**
 * A constraint that checks whether a value fits the ranged described by expressions such as
 * {@code int32 value = 5 [(range) = "[3..5)]}, describing a value that is at least 3 and less
 * than 5.
 */
@Immutable
public final class RangeConstraint extends RangedConstraint<String> {

    RangeConstraint(String optionValue, FieldDeclaration field) {
        super(optionValue, rangeFromOption(optionValue, field), field);
    }

    @VisibleForTesting
    static Range<ComparableNumber> rangeFromOption(String rangeOption, FieldDeclaration field) {
        return !rangeOption.isEmpty()
               ? RangeDecl.compile(rangeOption, field)
               : Range.all();
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
