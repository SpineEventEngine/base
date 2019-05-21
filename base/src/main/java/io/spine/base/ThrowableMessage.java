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
package io.spine.base;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.protobuf.Any;
import com.google.protobuf.Message;
import com.google.protobuf.Timestamp;
import io.spine.annotation.Internal;
import io.spine.string.Stringifiers;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;

import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.base.Time.currentTime;
import static io.spine.util.Exceptions.newIllegalStateException;

/**
 * A {@code Throwable}, which state is a {@link Message}.
 *
 * <p>Typically used to signalize about a command rejection, occurred in a system. In which case
 * the {@code message} thrown is a detailed description of the rejection reason.
 */
public abstract class ThrowableMessage extends Throwable {

    private static final long serialVersionUID = 0L;

    private final RejectionMessage message;

    /** The moment of creation of this object. */
    private final Timestamp timestamp;

    /** Optional ID of the entity which thrown the message. */
    private @MonotonicNonNull Any producerId;

    protected ThrowableMessage(RejectionMessage message) {
        super();
        this.message = checkNotNull(message);
        this.timestamp = currentTime();
    }

    /**
     * Obtains the thrown rejection message.
     */
    public RejectionMessage messageThrown() {
        return message;
    }

    /**
     * Obtains the time when the rejection message was created.
     */
    public Timestamp timestamp() {
        return timestamp;
    }

    /**
     * Initializes the ID of the entity that has thrown the message.
     *
     * <p>This internal API method can be called only once. It is supposed to be used by
     * the framework and must not be called by the user's code.
     *
     * @param  producerId the ID of the entity packed into {@code Any}
     * @return a reference to this {@code ThrowableMessage} instance
     */
    @Internal
    @CanIgnoreReturnValue
    public synchronized ThrowableMessage initProducer(Any producerId) {
        checkNotNull(producerId);
        if (this.producerId != null) {
            Object unpackedId = Identifier.unpack(producerId);
            String stringId = Stringifiers.toString(unpackedId);
            throw newIllegalStateException("Producer already initialized: %s", stringId);
        }
        this.producerId = producerId;
        return this;
    }

    /**
     * Obtains ID of the entity that has thrown the message.
     */
    public synchronized Optional<Any> producerId() {
        return Optional.ofNullable(producerId);
    }
}
