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

import com.google.common.collect.Range;
import com.google.errorprone.annotations.Immutable;
import io.spine.code.proto.FieldDeclaration;
import io.spine.option.MaxOption;
import io.spine.validate.ComparableNumber;
import io.spine.validate.ConstraintTranslator;
import io.spine.validate.NumberText;

import static io.spine.validate.FieldValidator.errorMsgFormat;
import static java.lang.String.format;

/**
 * A constraint, which checks whether a numeric field value exceeds a max value, when applied.
 */
@Immutable
public final class MaxConstraint
        extends RangedConstraint<MaxOption> {

    MaxConstraint(MaxOption optionValue, FieldDeclaration field) {
        super(optionValue, maxRange(optionValue), field);
    }

    private static Range<ComparableNumber> maxRange(MaxOption option) {
        boolean inclusive = !option.getExclusive();
        NumberText maxValue = new NumberText(option.getValue());
        return inclusive
               ? Range.atMost(maxValue.toNumber())
               : Range.lessThan(maxValue.toNumber());
    }

    @Override
    protected String compileErrorMessage(Range<ComparableNumber> range) {
        MaxOption max = optionValue();
        String template = errorMsgFormat(max, max.getMsgFormat());
        return format(template, orEqualTo(range.upperBoundType()), range.upperEndpoint());
    }

    @Override
    public void accept(ConstraintTranslator<?> visitor) {
        visitor.visitRange(this);
    }
}
