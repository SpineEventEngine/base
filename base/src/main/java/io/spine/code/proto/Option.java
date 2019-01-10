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

/**
 * An option of a Protobuf declaration.
 *
 * @param <T>
 *         the type of the option value
 */
public class Option<T> {

    private final T value;
    private final boolean explicitlySet;

    private Option(T value, boolean explicitlySet) {
        this.value = value;
        this.explicitlySet = explicitlySet;
    }

    /**
     * Creates the option from the value.
     *
     * @param value
     *         the value of the option
     * @param <T>
     *         the type of the option value
     * @return a new option, which is considered explicitly set
     */
    public static <T> Option<T> explicitlySet(T value) {
        return new Option<>(value, true);
    }

    /**
     * Creates the options by obtaining it from the descriptor.
     *
     * @param field
     *         the descriptor of the field
     * @param option
     *         the option to extract
     * @param <T>
     *         the type of the option value
     * @return a new option
     */
    public static <T> Option<T> from(FieldDescriptor field,
                                     GeneratedExtension<FieldOptions, T> option) {
        FieldOptions options = field.getOptions();
        T value = options.getExtension(option);
        boolean explicitlySet = options.hasExtension(option);
        return new Option<>(value, explicitlySet);
    }

    /**
     * Obtains the value of the option.
     *
     * @return the value of the option
     */
    public T value() {
        return value;
    }

    /**
     * Determines whether the option was explicitly set.
     *
     * @return {@code true} if the option was explicitly set, {@code false} otherwise
     */
    public boolean isExplicitlySet() {
        return explicitlySet;
    }
}
