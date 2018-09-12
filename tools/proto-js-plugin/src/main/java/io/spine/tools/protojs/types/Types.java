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

package io.spine.tools.protojs.types;

import com.google.common.annotations.VisibleForTesting;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.EnumDescriptor;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A helper tool for working with protobuf types.
 *
 * @author Dmytro Kuzmin
 */
public final class Types {

    /**
     * The prefix which is added to all proto types in the JS generated code.
     *
     * <p>For example, the "spine.web.test.CreateTask" command becomes the
     * "proto.spine.web.test.CreateTask" in the generated code.
     */
    @VisibleForTesting
    static final String PREFIX = "proto.";

    /** Prevents instantiation of this utility class. */
    private Types() {
    }

    /**
     * Obtains {@code message}'s type prepended with the {@code proto.} prefix.
     *
     * @param message
     *         the descriptor of the {@code message} whose type to obtain
     * @return the type of the {@code message} with the {@code proto.} prefix
     */
    public static String typeWithProtoPrefix(Descriptor message) {
        checkNotNull(message);
        String typeName = message.getFullName();
        String nameWithPrefix = PREFIX + typeName;
        return nameWithPrefix;
    }

    /**
     * Obtains {@code enum}'s type prepended with the {@code proto.} prefix.
     *
     * @param enumDescriptor
     *         the descriptor of the {@code enum} whose type to obtain
     * @return the type of the {@code enum} with the {@code proto.} prefix
     */
    public static String typeWithProtoPrefix(EnumDescriptor enumDescriptor) {
        checkNotNull(enumDescriptor);
        String typeName = enumDescriptor.getFullName();
        String nameWithPrefix = PREFIX + typeName;
        return nameWithPrefix;
    }
}
