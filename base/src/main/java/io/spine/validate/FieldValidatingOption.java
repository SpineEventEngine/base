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
import com.google.protobuf.GeneratedMessage.GeneratedExtension;
import io.spine.code.proto.Option;

/**
 * An option that validates a field.
 *
 * Validating options impose constraint on fields that they are applied to.
 *
 * @param <T>
 *         type of information that this option holds
 */
abstract class FieldValidatingOption<T, F> extends ValidatingOption<T, FieldValue<F>> {

    /** An extension that represents this option. */
    private final GeneratedExtension<FieldOptions, T> optionExtension;

    /** Specifies the extension that corresponds to this option.*/
    FieldValidatingOption(GeneratedExtension<FieldOptions, T> extension) {
        this.optionExtension = extension;
    }

    /** Returns {@code true} if this option exists for the specified field, {@code false} otherwise. */
    boolean optionPresentAt(FieldValue<F> value) {
        return Option.from(value.declaration()
                                .descriptor(), optionExtension)
                     .isExplicitlySet();
    }

    /** Returns the extension that corresponds to this option.*/
    final GeneratedExtension<FieldOptions, T> optionExtension() {
        return optionExtension;
    }
}
