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

package io.spine.time.temporal;

import com.google.protobuf.Any;
import com.google.protobuf.Message;
import io.spine.protobuf.AnyPacker;

/**
 * A {@link Temporal} implemented with a message.
 *
 * <p>Messages marked with the {@code Temporal} interface should use this type instead of using
 * the {@code Temporal} directly.
 *
 * <p>To create a temporal message type:
 * <ol>
 *     <li>create a new interface derived from this one;
 *     <li>specify the target message type as the type parameter;
 *     <li>implement leftover abstract methods inherited from {@link Temporal};
 *     <li>mark the target message with the {@code (is)} option.
 * </ol>
 *
 * @param <T>
 *         the type of itself
 */
@SuppressWarnings("InterfaceNeverImplemented")
    // See SpineEventEngine/time for canonical implementations.
public interface TemporalMessage<T extends TemporalMessage<T>> extends Temporal<T>, Message {

    /**
     * Packs this message into an {@code Any}.
     *
     * @return this message as an {@code Any}
     */
    @Override
    default Any toAny() {
        Any any = AnyPacker.pack(this);
        return any;
    }
}
