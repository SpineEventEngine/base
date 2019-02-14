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

package io.spine.code.proto.ref;

import com.google.errorprone.annotations.Immutable;
import com.google.protobuf.Descriptors.Descriptor;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * Provides typical type references used for referencing types in proto definitions.
 *
 * <p>A type reference is usually used in a field reference.
 */
@Immutable
enum BuiltIn implements TypeRef {

    SELF("") {

        /**
         * Accepts all message types.
         *
         * <p>Since any message can reference its field, this method always returns {@code true}
         * since validity of a field reference for such a message is outside of scope of
         * responsibilities of {@link TypeRef}.
         * @see FieldRef
         */
        @Override
        public boolean test(Descriptor message) {
            return true;
        }
    },

    /**
     * A reference to an event context.
     */
    EVENT_CONTEXT("context") {

        @Override
        Optional<TypeRef> parse(String value) {
            if (value.startsWith(value())) {
                return Optional.of(this);
            }
            return Optional.empty();
        }

        /**
         * Accepts a message which type name is {@code EventContext}.
         */
        @Override
        public boolean test(Descriptor message) {
            return "EventContext".equals(message.getName());
        }
    };

    /** The value of the reference. */
    private final String value;

    BuiltIn(String value) {
        this.value = value;
    }

    /**
     * Matches the passed string to see if it represents this type reference.
     *
     * @return {@code this} if the string matches this type reference,
     *         empty {@code Optional} otherwise
     */
    Optional<TypeRef> parse(String value) {
        if (value().equals(value)) {
            return Optional.of(this);
        }
        return Optional.empty();
    }

    /** Obtains the value of the reference. */
    @Override
    public String value() {
        return this.value;
    }

    /**
     * Finds a value matching the passed string.
     */
    static Optional<TypeRef> parseAll(String value) {
        Optional<TypeRef> result =
                Stream.of(values())
                      .map(v -> v.parse(value))
                      .filter(Optional::isPresent)
                      .findFirst()
                      .orElse(Optional.empty());
        return result;
    }
}
