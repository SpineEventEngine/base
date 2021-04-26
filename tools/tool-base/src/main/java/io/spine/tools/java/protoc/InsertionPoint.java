/*
 * Copyright 2021, TeamDev. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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

package io.spine.tools.java.protoc;

import io.spine.type.Type;

import static java.lang.String.format;

/**
 * Enumeration of
 * <a href="https://developers.google.com/protocol-buffers/docs/reference/java-generated#plugins">
 * insertion points</a> available for the Java Protobuf code generation plugins.
 *
 * @see <a href="https://developers.google.com/protocol-buffers/docs/reference/cpp/google.protobuf.compiler.plugin.pb">
 *         plugin.pb.h</a>
 */
public enum InsertionPoint {

    /**
     * Member declarations that belong in a message class interface implementations.
     *
     * <p>Use this insertion point to make the message class implement more interfaces.
     *
     * <p>Uses {@code @@protoc_insertion_point(message_implements:TYPENAME)}.
     */
    message_implements,

    /**
     * Member declarations that belong in a message's builder class interface implementations.
     *
     * <p>Use this insertion point to make the message's builder class implement more interfaces.
     *
     * <p>Uses {@code @@protoc_insertion_point(builder_implements:TYPENAME)}.
     */
    builder_implements,

    /**
     * Member declarations that belong in a message interface.
     *
     * <p>Use this insertion point to make message's interface extend more interfaces.
     *
     * <p>Uses {@code @@protoc_insertion_point(interface_extends:TYPENAME)}.
     */
    interface_extends,

    /**
     * Member declarations that belong in a message class.
     *
     * <p>Use this insertion point to alter message class with e.g. new methods.
     *
     * <p>Uses {@code @@protoc_insertion_point(class_scope:TYPENAME)}.
     */
    class_scope,

    /**
     * Member declarations that belong in a message's builder class.
     *
     * <p>Use this insertion point to alter message's builder class with e.g. new methods.
     *
     * <p>Uses {@code @@protoc_insertion_point(builder_scope:TYPENAME)}.
     */
    builder_scope,

    /**
     * Member declarations that belong in an enum class.
     *
     * <p>Use this insertion point to alter enum class with e.g. new methods.
     *
     * <p>Uses {@code @@protoc_insertion_point(enum_scope:TYPENAME)}.
     */
    enum_scope,

    /**
     * Member declarations that belong in the file's outer class.
     *
     * <p>Use this insertion point to alter Proto class with e.g. new methods.
     *
     * <p>Uses {@code @@protoc_insertion_point(outer_class_scope)}.
     */
    outer_class_scope {
        @Override
        public String forType(Type<?, ?> ignored) {
            return getDefinition();
        }
    };

    /**
     * Returns current insertion point definition.
     */
    public String getDefinition() {
        return name();
    }

    /**
     * Creates Protoc insertion point for the supplied type.
     */
    public String forType(Type<?, ?> protobufType) {
        String result = format("%s:%s", getDefinition(), protobufType.name());
        return result;
    }
}
