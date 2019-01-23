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
import com.google.protobuf.DescriptorProtos.FieldOptions;
import com.google.protobuf.GeneratedMessage.GeneratedExtension;
import io.spine.option.OnDuplicate;
import io.spine.option.OptionsProto;

import java.util.List;
import java.util.Optional;

/**
 * An option that can be applied to {@code repeated} Protobuf fields to specify that values
 * represented by that {@code repeated} field don't contain duplicates.
 *
 * @param <T>
 *         type fields that can be checked against this option
 */
final class DistinctFieldOption<T> extends FieldValidatingOption<OnDuplicate, T> {

    private DistinctFieldOption() {
    }

    /**
     * Returns a new instance of this option.
     *
     * @param <T> type of fields that can be checked against this option
     */
    static <T> DistinctFieldOption<T> distinctFieldOption() {
        return new DistinctFieldOption<>();
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static List<ConstraintViolation> duplicateFound(FieldValue value) {
        String fieldName = value.declaration()
                                .name()
                                .value();
        String msg = "Found a duplicate element in a `distinct` field %s. ";
        ConstraintViolation.Builder duplicatesBuilder = ConstraintViolation
                .newBuilder()
                .setMsgFormat(msg)
                .addParam(fieldName);
        return ImmutableList.of(duplicatesBuilder.build());
    }

    @Override
    public Optional<OnDuplicate> valueFrom(FieldValue<T> fieldValue) {
        return Optional.of(fieldValue.valueOf(optionExtension()));
    }

    @Override
    Constraint<FieldValue<T>> constraint() {
        return new DistinctConstraint<>();
    }

    @Override
    GeneratedExtension<FieldOptions, OnDuplicate> optionExtension() {
        return OptionsProto.onDuplicate;
    }
}
