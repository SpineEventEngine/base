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

import com.google.common.collect.ImmutableList;
import io.spine.code.proto.FieldDeclaration;
import io.spine.option.MinOption;
import io.spine.option.OptionsProto;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class Min<V extends Number> extends NumberFieldValidatingOption<MinOption, V> {

    private Min() {
    }

    static <V extends Number> Min<V> create() {
        return new Min<>();
    }

    @Override
    OptionInapplicableException onInapplicable(FieldDeclaration declaration) {
        return null;
    }

    @Override
    List<ConstraintViolation> applyValidatingRules(FieldValue<V> value) {
        MinOption option = valueFrom(value).get();
        double maxValue = Double.parseDouble(option.getValue());
        Predicate<Double> exceedsMax = option.getExclusive() ?
                                       input -> input < maxValue :
                                       input -> input <= maxValue;
        double fieldValue = value.singleValue().doubleValue();
        if (exceedsMax.test(fieldValue)) {
            return ImmutableList.of(exceedsMinConstraint(value));
        }
        return ImmutableList.of();
    }

    private ConstraintViolation exceedsMinConstraint(FieldValue<V> value) {
        return null;
    }

    @Override
    public Optional<MinOption> valueFrom(FieldValue<V> bearer) {
        MinOption option = bearer.valueOf(OptionsProto.min);
        return option.getValue()
                     .isEmpty() ?
               Optional.empty() :
               Optional.of(option);
    }
}
