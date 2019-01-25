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

import com.google.protobuf.DescriptorProtos.FieldOptions;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.GeneratedMessage.GeneratedExtension;
import io.spine.code.proto.Option;

import java.util.Optional;

/**
 * An option that validates a field.
 *
 * <p>Validating options impose constraint on fields that they are applied to.
 *
 * @param <T>
 *         type of information that this option holds
 */
abstract class FieldValidatingOption<T, F> extends ValidatingOption<T, FieldValue<F>> {

    /** An extension that represents this option. */
    private final GeneratedExtension<FieldOptions, T> optionExtension;

    /** Specifies the extension that corresponds to this option. */
    FieldValidatingOption(GeneratedExtension<FieldOptions, T> extension) {
        this.optionExtension = extension;
    }

    /** Returns {@code true} if this option exists for the specified field, {@code false} otherwise. */
    boolean shouldValidate(FieldValue<F> value) {
        return valueFrom(value).isPresent();
    }

    @Override
    public Optional<T> valueFrom(FieldValue<F> bearer) {
        T option = bearer.valueOf(optionExtension);
        return isDefault(bearer)
               ? Optional.empty()
               : Optional.of(option);
    }

    /**
     * Obtains a value of this option from the specified field.
     *
     * @param value
     *         a field to obtain an option value for
     * @return raw option value
     */
    io.spine.code.proto.Option<T> optionValue(FieldValue<F> value) {
        FieldDescriptor descriptor = value.declaration()
                                          .descriptor();
        return Option.from(descriptor, optionExtension);
    }

    abstract boolean isDefault(FieldValue<F> value);

    /** Returns the extension that corresponds to this option. */
    final GeneratedExtension<FieldOptions, T> optionExtension() {
        return optionExtension;
    }
}
