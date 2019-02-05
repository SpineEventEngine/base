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
import io.spine.option.MinOption;

/**
 * A constraint that, when applied to a numeric field, checks whether the value of that field is
 * greater than (or equal to, if specified by the value of the respective option) a min value.
 */
final class MinConstraint<V extends Number & Comparable> extends RangedConstraint<V, MinOption> {

    MinConstraint(MinOption optionValue) {
        super(optionValue, minRange(optionValue));
    }

    @SuppressWarnings({
            "unchecked",
            "WrapperTypeMayBePrimitive" // primitive is uncastable
    })
        // is safe because Double is a subtype of both Number and Comparable
    private static <V extends Number & Comparable> Range<V> minRange(MinOption option) {
        boolean inclusive = !option.getExclusive();
        Double minValue = Double.parseDouble(option.getValue());
        return inclusive
               ? Range.atLeast( (V) minValue)
               : Range.greaterThan((V) minValue);
    }
}
