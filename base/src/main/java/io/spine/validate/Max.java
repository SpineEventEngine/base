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
import com.google.common.collect.ImmutableSet;
import com.google.protobuf.Descriptors.FieldDescriptor.JavaType;
import io.spine.code.proto.FieldDeclaration;
import io.spine.option.MaxOption;
import io.spine.option.OptionsProto;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.google.protobuf.Descriptors.FieldDescriptor.JavaType.DOUBLE;
import static com.google.protobuf.Descriptors.FieldDescriptor.JavaType.FLOAT;
import static com.google.protobuf.Descriptors.FieldDescriptor.JavaType.INT;
import static com.google.protobuf.Descriptors.FieldDescriptor.JavaType.LONG;

public class Max<V extends Number> extends NumberFieldValidatingOption<MaxOption, V> {

    private Max() {
        super();
    }

    public static <V extends Number> Max<V> create(){
        return new Max<>();
    }

    @Override
    boolean applicableTo(FieldDeclaration field) {
        return true;
    }

    @Override
    OptionInapplicableException onInapplicable(FieldDeclaration declaration) {
        return null;
    }

    @Override
    List<ConstraintViolation> applyValidatingRules(FieldValue<V> value) {
        MaxOption option = valueFrom(value).get();
        double maxValue = Double.parseDouble(option.getValue());
        Predicate<Double> exceedsMax = option.getExclusive() ?
                                       input -> input > maxValue :
                                       input -> input >= maxValue;
        double fieldValue = value.singleValue().doubleValue();
        if (exceedsMax.test(fieldValue)) {
            return ImmutableList.of(exceedsMaxConstraint(value));
        }
        return ImmutableList.of();
    }

    private ConstraintViolation exceedsMaxConstraint(FieldValue<V> value) {
        // TODO: 2019-01-21:serhii.lekariev: xd
        return ConstraintViolation.getDefaultInstance();
    }

    @Override
    public Optional<MaxOption> valueFrom(FieldValue<V> bearer) {
        MaxOption option = bearer.valueOf(OptionsProto.max);
        return option.getValue()
                     .isEmpty() ?
               Optional.empty() :
               Optional.of(option);
    }

}
