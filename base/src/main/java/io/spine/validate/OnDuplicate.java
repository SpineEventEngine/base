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

import io.spine.option.OptionsProto;

import java.util.Optional;

/**
 * An option that can be applied to {@code repeated} Protobuf fields to specify that values
 * represented by that {@code repeated} field don't contain duplicates.
 *
 * @param <T>
 *         type fields that can be checked against this option
 */
final class OnDuplicate<T> extends FieldValidatingOption<io.spine.option.OnDuplicate, T> {

    private OnDuplicate() {
        super(OptionsProto.onDuplicate);
    }

    /**
     * Returns a new instance of this option.
     *
     * @param <T> type of fields that can be checked against this option
     */
    static <T> OnDuplicate<T> create() {
        return new OnDuplicate<>();
    }

    @Override
    public Optional<io.spine.option.OnDuplicate> valueFrom(FieldValue<T> fieldValue) {
        io.spine.option.OnDuplicate option = fieldValue.valueOf(optionExtension());
        boolean isDefault = option.getNumber() == 0;
        return isDefault ? Optional.empty() : Optional.of(option);
    }

    @Override
    Constraint<FieldValue<T>> constraint() {
        return new DistinctConstraint<>();
    }
}
