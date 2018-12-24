/*
 * Copyright 2018, TeamDev. All rights reserved.
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

import com.google.errorprone.annotations.Immutable;
import com.google.protobuf.Message;
import io.spine.code.proto.MessageDeclaration;

import java.util.function.Predicate;

import static io.spine.base.UuidMessage.isUuidMessage;

/**
 * A common interface for the {@code string}-based unique identifiers.
 *
 * <p>The messages of suitable format are spotted by the Spine Model Compiler and marked with this
 * interface automatically.
 *
 * <p>By convention, a {@code string}-based identifier should have exactly one {@code string} field
 * named 'uuid':
 * <pre>
 *     {@code
 *
 *         message ProjectId {
 *             // UUID-based generated value.
 *             string uuid = 1;
 *         }
 *     }
 * </pre>
 *
 * @param <I>
 *         the type of the message
 */
@SuppressWarnings({"unchecked" /* Class cast correctness guaranteed by the interface contract. */,
        "InterfaceNeverImplemented" /* Used by the Protobuf Compiler plugin. */})
@Immutable
public interface UuidValue<I extends Message> extends SerializableMessage {

    /**
     * Provides a predicate which checks whether the given {@code MessageDeclaration} represents an
     * {@code string}-based unique identifier.
     *
     * @return the predicate to distinguish {@code string}-based unique identifiers
     */
    static Predicate<MessageDeclaration> predicate() {
        return messageDeclaration -> isUuidMessage(messageDeclaration.getMessage());
    }

    /**
     * Generates a new identifier instance using a random {@code String}.
     */
    default I generate() {
        Class<I> thisClass = (Class<I>) this.getClass();
        UuidMessage<I> uuidMessage = UuidMessage.of(thisClass);
        return uuidMessage.generate();
    }

    /**
     * Generates a new identifier instance from the passed value.
     */
    default I of(String value) {
        Class<I> thisClass = (Class<I>) this.getClass();
        UuidMessage<I> uuidMessage = UuidMessage.of(thisClass);
        return uuidMessage.create(value);
    }
}
