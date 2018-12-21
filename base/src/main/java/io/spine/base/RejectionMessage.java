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
import io.spine.code.proto.MessageDeclaration;

import java.util.function.Predicate;

/**
 * A common interface for rejection messages.
 *
 * <p>This interface is used by the Model Compiler for marking rejection messages.
 * By convention, rejection messages are defined in a proto file, which name ends
 * with {@code rejections.proto}.
 */
@Immutable
public interface RejectionMessage extends EventMessage {

    /**
     * Provides a predicate which checks whether the given {@code MessageDeclaration} represents a
     * rejection message.
     *
     * @return the predicate to distinguish rejection messages
     */
    static Predicate<MessageDeclaration> predicate() {
        return messageDeclaration -> File.INSTANCE.predicate()
                                                  .test(messageDeclaration.getFile());
    }

    /**
     * Provides the predicate for finding proto files with rejection message declarations.
     */
    class File {
        @SuppressWarnings("DuplicateStringLiteralInspection") // Used in other contexts.
        private static final MessageFile INSTANCE = new MessageFile("rejections") {
            private static final long serialVersionUID = 0L;
        };

        /** Prevents instantiation of this utility class. */
        private File() {
        }

        /**
         * Obtains the suffix common for proto files containing command message declarations.
         */
        public static String suffix() {
            return INSTANCE.value();
        }
    }
}
