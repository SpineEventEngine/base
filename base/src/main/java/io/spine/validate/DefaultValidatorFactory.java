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

import com.google.common.collect.ImmutableSet;

import java.util.Set;

public final class DefaultValidatorFactory implements ValidatorFactory {

    private static final ImmutableSet<FieldValidatingOption<?, String>> STRING_OPTIONS =
            ImmutableSet.of(Pattern.create());

    @Override
    public Set<FieldValidatingOption<?, String>> optionsForString(FieldValue<String> value) {
        return STRING_OPTIONS;
    }

    @Override
    public Set<FieldValidatingOption<?, Integer>> optionsForInt(FieldValue<Integer> value) {
        return numberOptions();
    }

    @Override
    public Set<FieldValidatingOption<?, Long>> optionsForLong(FieldValue<Long> value) {
        return numberOptions();
    }

    @Override
    public Set<FieldValidatingOption<?, Float>> optionsForFloat(FieldValue<Float> value) {
        return numberOptions();
    }

    @Override
    public Set<FieldValidatingOption<?, Double>> optionsForDouble(FieldValue<Double> value) {
        return numberOptions();
    }

    private static <N extends Number & Comparable<N>> Set<FieldValidatingOption<?, N>>
    numberOptions() {
        return ImmutableSet.of(Max.create(),
                               Min.create(),
                               Range.create(),
                               Digits.create());
    }
}
