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

package io.spine.code.proto;

import com.google.protobuf.DescriptorProtos.FieldOptions;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.GeneratedMessage.GeneratedExtension;
import io.spine.validate.FieldValue;

import java.util.Optional;

/**
 * A Protobuf option that is applied to fields in Protobuf messages.
 *
 * @param <O>
 *         value of this option
 * @param <T>
 *         type of field that this option is applied to
 */
public class FieldOption<O, T> implements Option<O, FieldValue<T>> {

    private final GeneratedExtension<FieldOptions, O> optionExtension;

    /** Specifies the extension that corresponds to this option. */
    protected FieldOption(GeneratedExtension<FieldOptions, O> optionExtension) {
        this.optionExtension = optionExtension;
    }

    /**
     * Returns a new {@code FieldOption} instance based on the specified option.
     *
     * @param extension
     *         extensions that corresponds to this option
     * @param <O>
     *         value of the option
     * @param <T>
     *         type of field that the returned option can be applied to
     * @return a new instance
     */
    public static <O, T> FieldOption<O, T> someOption(
            GeneratedExtension<FieldOptions, O> extension) {
        return new FieldOption<>(extension);
    }

    @Override
    public Optional<O> valueFrom(FieldValue<T> bearer) {
        FieldDescriptor descriptor = bearer.context()
                                           .getTarget();
        FieldOptions options = descriptor.getOptions();
        boolean explicitlySet = options.hasExtension(optionExtension);
        O value = options.getExtension(optionExtension);
        return explicitlySet
               ? Optional.of(value)
               : Optional.empty();
    }
}
