/*
 * Copyright 2020, TeamDev. All rights reserved.
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
import com.google.errorprone.annotations.Immutable;
import io.spine.code.proto.FieldDeclaration;
import io.spine.option.MinOption;
import io.spine.validate.ComparableNumber;
import io.spine.validate.ConstraintTranslator;
import io.spine.validate.NumberText;
import io.spine.validate.diags.ViolationText;

import static java.lang.String.format;

/**
 * A constraint that, when applied to a numeric field, checks whether the value of that field is
 * greater than (or equal to, if specified by the value of the respective option) a min value.
 */
@Immutable
public final class MinConstraint extends RangedConstraint<MinOption> {

    MinConstraint(MinOption optionValue, FieldDeclaration field) {
        super(optionValue, minRange(optionValue), field);
    }

    private static Range<ComparableNumber> minRange(MinOption option) {
        boolean inclusive = !option.getExclusive();
        NumberText minValue = new NumberText(option.getValue());
        return inclusive
               ? Range.atLeast(minValue.toNumber())
               : Range.greaterThan(minValue.toNumber());
    }

    @Override
    protected String compileErrorMessage(Range<ComparableNumber> range) {
        MinOption min = optionValue();
        String template = ViolationText.errorMessage(min, min.getMsgFormat());
        return format(template, orEqualTo(range.lowerBoundType()), range.lowerEndpoint());
    }

    @Override
    public void accept(ConstraintTranslator<?> visitor) {
        visitor.visitRange(this);
    }
}
