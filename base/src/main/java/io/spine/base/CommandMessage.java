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
import com.google.protobuf.DescriptorProtos.DescriptorProto;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;

import java.util.function.BiPredicate;

/**
 * A common interface for command messages.
 *
 * <p>This interface is used by the Model Compiler for marking command messages.
 * By convention, command messages are defined in a proto file, which name ends
 * with {@code commands.proto}.
 */
@Immutable
public interface CommandMessage extends SerializableMessage {

    /**
     * Provides a predicate which checks whether a given message declaration represents a
     * command message.
     *
     * <p>The predicate accepts a message descriptor and the declaring file descriptor.
     *
     * @return the predicate to distinguish command messages
     */
    static BiPredicate<DescriptorProto, FileDescriptorProto> predicate() {
        return (message, file) -> File.predicate()
                                      .test(file);
    }

    /**
     * Provides the predicate for finding proto files with command message declarations.
     */
    class File {

        private static final MessageFile INSTANCE = new MessageFile("commands") {
            private static final long serialVersionUID = 0L;
        };

        /** Prevents instantiation of this utility class. */
        private File() {
        }

        public static MessageFile.Predicate predicate() {
            return INSTANCE.predicate();
        }

        /**
         * Obtains the suffix common for proto files containing command message declarations.
         */
        public static String suffix() {
            return INSTANCE.value();
        }
    }
}
