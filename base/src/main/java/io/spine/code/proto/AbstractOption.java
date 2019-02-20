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

import java.util.Optional;

/**
 * An abstract base for all types of Protobuf options: {@link FieldOption}, {@link MessageOption},
 * {@link FileOption} and a Service Option.
 *
 * @param <T>
 *         the type of value held by this option
 * @param <K>
 *         the type of values that this option is applied to
 * @param <E>
 *         the type of object that holds all options of {@code K}
 */
public abstract class AbstractOption<T, K extends GenericDescriptor, E extends ExtendableMessage<E>>
        implements Option<T, K> {

    private final GeneratedExtension<E, T> extension;

    /** Creates a new instance of the option using the specified extension. */
    AbstractOption(GeneratedExtension<E, T> extension) {
        this.extension = extension;
    }

    /**
     * Returns an option object of the specified {@code K}.
     *
     * <p>Examples of option objects include
     * {@link com.google.protobuf.DescriptorProtos.FieldOptions} for fields,
     * {@link com.google.protobuf.DescriptorProtos.FileOptions} for files, etc.
     */
    protected abstract E optionsFrom(K object);

    /** Returns extension that represents this option. */
    protected GeneratedExtension<E, T> extension() {
        return extension;
    }

    @Override
    public Optional<T> valueFrom(K object) {
        E options = optionsFrom(object);
        return options.hasExtension(extension)
               ? Optional.of(options.getExtension(this.extension))
               : Optional.empty();

    }
}
