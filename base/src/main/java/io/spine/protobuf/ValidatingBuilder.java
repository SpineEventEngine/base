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

package io.spine.protobuf;

import com.google.protobuf.Message;
import io.spine.annotation.GeneratedMixin;
import io.spine.validate.NotValidated;
import io.spine.validate.Validate;
import io.spine.validate.Validated;
import io.spine.validate.ValidationException;

/**
 * A message builder which calls validation.
 *
 * @param <M>
 *         the type of the message to build
 */
@GeneratedMixin
public interface ValidatingBuilder<M extends Message> extends Message.Builder {

    /**
     * Constructs the message with the given fields.
     *
     * <p>Users should not call this method directly. Instead, call {@link #vBuild()} for
     * a validated message or {@link #buildPartial()} to skip message validation.
     */
    @Override
    @NotValidated M build();

    /**
     * Constructs the message with the given fields without validation.
     *
     * <p>Users should prefer {@link #vBuild()} over this method. However, in cases, when validation
     * is not required, call this method instead of {@link #build()}.
     *
     * @return the build message, potentially invalid
     */
    @Override
    @NotValidated M buildPartial();

    /**
     * Constructs the message and {@linkplain Validate validates} it according to the constraints
     * declared in Protobuf.
     *
     * @return the built message
     * @throws ValidationException
     *         if the message is invalid
     */
    default @Validated M vBuild() throws ValidationException {
        M message = build();
        Validate.checkValid(message);
        return message;
    }
}
