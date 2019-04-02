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
import com.google.protobuf.ByteString;
import com.google.protobuf.Descriptors.EnumValueDescriptor;
import com.google.protobuf.Message;
import io.spine.annotation.SPI;

import java.util.ServiceLoader;
import java.util.Set;
import java.util.Spliterator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@SPI
public interface ValidatorFactory {

    static Stream<ValidatorFactory> all() {
        ServiceLoader<ValidatorFactory> loader = ServiceLoader.load(ValidatorFactory.class);
        Spliterator<ValidatorFactory> spliterator = loader.spliterator();
        return StreamSupport.stream(spliterator, false);
    }

    default Set<FieldValidatingOption<?, Boolean>> optionsForBoolean(FieldValue<Boolean> value) {
        return ImmutableSet.of();
    }

    default Set<FieldValidatingOption<?, ByteString>>
    optionsForByteString(FieldValue<ByteString> value) {
        return ImmutableSet.of();
    }

    default Set<FieldValidatingOption<?, Double>> optionsForDouble(FieldValue<Double> value) {
        return ImmutableSet.of();
    }

    default Set<FieldValidatingOption<?, EnumValueDescriptor>>
    optionsForEnum(FieldValue<EnumValueDescriptor> value) {
        return ImmutableSet.of();
    }

    default Set<FieldValidatingOption<?, Float>> optionsForFloat(FieldValue<Float> value) {
        return ImmutableSet.of();
    }

    default Set<FieldValidatingOption<?, Integer>> optionsForInt(FieldValue<Integer> value) {
        return ImmutableSet.of();
    }

    default Set<FieldValidatingOption<?, Long>> optionsForLong(FieldValue<Long> value) {
        return ImmutableSet.of();
    }

    default <T extends Message> Set<FieldValidatingOption<?, T>>
    optionsForMessage(FieldValue<T> value) {
        return ImmutableSet.of();
    }

    default Set<FieldValidatingOption<?, String>> optionsForString(FieldValue<String> value) {
        return ImmutableSet.of();
    }
}
