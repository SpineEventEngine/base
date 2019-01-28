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
import io.spine.code.proto.FieldOption;

/**
 * An option that validates a field.
 *
 * <p>Validating options impose constraint on fields that they are applied to.
 *
 * @param <O>
 *         type of information that this option holds
 */
abstract class FieldValidatingOption<O, F> extends FieldOption<O, F>
                                           implements ValidatingOption<O, FieldValue<F>> {

    /** Specifies the extension that corresponds to this option. */
    protected FieldValidatingOption(GeneratedExtension<FieldOptions, O> optionExtension) {
        super(optionExtension);
    }

    /** Returns {@code true} if this option exists for the specified field, {@code false} otherwise. */
    boolean shouldValidate(FieldValue<F> fieldValue) {
        return valueFrom(fieldValue).isPresent();
    }
}
