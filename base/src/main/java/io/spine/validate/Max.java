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
import io.spine.option.MaxOption;
import io.spine.option.OptionsProto;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import static java.lang.Double.parseDouble;

/**
 * An option that defines a maximum value for a numeric field.
 */
public class Max extends FieldValidatingOption<MaxOption, Double> {

    private Max(){
    }

    /** Returns a new instance of this option. */
    static Max create(){
        return new Max();
    }

    private final Predicate<FieldValue<Double>> exceeds = doubleFieldValue -> {
        MaxOption option = getOption(doubleFieldValue);
        double parsedValue = parseDouble(option.getValue());
        Double actualValue = doubleFieldValue.singleValue();
        Predicate<Double> exceeds = option.getExclusive() ?
                                    value -> value > parsedValue :
                                    value -> value >= parsedValue;
        return exceeds.test(actualValue);
    };

    @Override
    public Optional<MaxOption> valueFrom(FieldValue<Double> bearer) {
        return optionPresentAt(bearer) ?
               Optional.of(bearer.valueOf(OptionsProto.max)) :
               Optional.empty();
    }

    private List<ConstraintViolation> maxFieldViolated(FieldValue<Double> fieldValue) {
        MaxOption option = getOption(fieldValue);
        double parsedMaxValue = parseDouble(option.getValue());
        boolean isExclusive = option.getExclusive();
        String fieldName = fieldValue.declaration()
                                     .name()
                                     .value();
        String format = "Actual value of field %s exceeds maximum value of %s, %s.";
        ConstraintViolation violation = ConstraintViolation
                .newBuilder()
                .setMsgFormat(format)
                .addParam(fieldName)
                .addParam(String.valueOf(parsedMaxValue))
                .addParam(isExclusive ? "exclusive" : "inclusive")
                .build();
        return ImmutableList.of(violation);
    }

    @Override
    ValidationRule<FieldValue<Double>> validationRule() {
        return new ValidationRule<>(exceeds, this::maxFieldViolated);
    }

    private MaxOption getOption(FieldValue<Double> doubleFieldValue) {
        return valueFrom(doubleFieldValue).orElseThrow(() -> null);
    }
}
