/*
 * Copyright 2020, TeamDev. All rights reserved.
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

package io.spine.code.gen.js;

import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.GenericDescriptor;
import io.spine.value.StringTypeValue;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * The name of a type in the JavaScript code.
 */
public final class TypeName extends StringTypeValue {

    private static final long serialVersionUID = 0L;

    /**
     * The prefix which is added to all proto types in the JS generated code.
     */
    private static final String PREFIX = "proto.";

    private TypeName(String value) {
        super(value);
    }

    /**
     * Obtains the type name of the specified Protobuf declaration.
     *
     * <p>All Protobuf types in JS are prepended with {@code proto.} prefix.
     */
    public static TypeName from(GenericDescriptor descriptor) {
        checkNotNull(descriptor);
        String typeName = descriptor.getFullName();
        String nameWithPrefix = PREFIX + typeName;
        return new TypeName(nameWithPrefix);
    }

    /**
     * Obtains the type name of the parser of the specified message.
     *
     * <p>The parser is a static property on the corresponding message type.
     */
    public static TypeName ofParser(Descriptor message) {
        checkNotNull(message);
        TypeName messageType = from(message);
        return new TypeName(messageType + ".Parser");
    }
}
