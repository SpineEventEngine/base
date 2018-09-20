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

package io.spine.code.js;

import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.EnumDescriptor;
import io.spine.value.StringTypeValue;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Represents the Protobuf type in the JavaScript code.
 *
 * <p>All Protobuf types in JS are prepended with {@code proto.} prefix.
 *
 * @author Dmytro Kuzmin
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

    public static TypeName from(Descriptor messageDescriptor) {
        checkNotNull(messageDescriptor);
        String typeName = messageDescriptor.getFullName();
        String nameWithPrefix = PREFIX + typeName;
        return new TypeName(nameWithPrefix);
    }

    public static TypeName from(EnumDescriptor enumDescriptor) {
        checkNotNull(enumDescriptor);
        String typeName = enumDescriptor.getFullName();
        String nameWithPrefix = PREFIX + typeName;
        return new TypeName(nameWithPrefix);
    }
}
