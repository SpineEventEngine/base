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

import com.google.protobuf.Descriptors.GenericDescriptor;
import com.google.protobuf.GeneratedMessage.GeneratedExtension;
import com.google.protobuf.GeneratedMessageV3.ExtendableMessage;

/**
 * A Protobuf option with a {@code String value}.
 *
 * <p>Some options are defined as having a single {@code String} value, this value might bear
 * more sense and require additional handling, thus, this class adds a
 * {@linkplain this#parsedValueFrom(GenericDescriptor) bridge method} to avoid working with plain
 * {@code Strings} in user code.
 *
 * @param <V>
 *         value that is obtained from the Protobuf string option value
 * @param <K>
 *         the type of values that this option is applied to
 * @param <E>
 *         the type of object that holds all options of {@code K}
 */
public abstract class StringOption<V, K extends GenericDescriptor, E extends ExtendableMessage<E>>
        extends ProtobufOption<String, K, E> {

    protected StringOption(GeneratedExtension<E, String> extension) {
        super(extension);
    }

    /**
     * Obtains a value that is represented by a {@code String} option value.
     *
     * @param object
     *         value that this option is applied to
     */
    protected abstract V parsedValueFrom(K object);
}
