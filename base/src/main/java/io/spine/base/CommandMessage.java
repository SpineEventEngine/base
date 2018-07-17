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

import com.google.common.base.Predicate;
import com.google.protobuf.Descriptors.FileDescriptor;
import com.google.protobuf.Message;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A common interface for command messages.
 *
 * <p>This interface is used by the Model Compiler for marking command messages.
 * By convention, command messages are defined in a proto file, which name ends
 * with {@code commands.proto}.
 *
 * @author Alexander Yevsyukov
 */
@SuppressWarnings("InterfaceNeverImplemented") /* See Javadoc */
public interface CommandMessage extends Message {

    /**
     * Constants and utilities for working with proto command files.
     */
    class File {

        /** Prevents instantiation of this utility class. */
        private File() {
        }

        /**
         * The name suffix for proto files containing command message declarations.
         */
        public static final String SUFFIX = "commands.proto";

        //TODO:2018-02-12:alexander.yevsyukov: Replace usages of this predicate with cast to
        // `CommandMessage` after code generation is updated.
        /**
         * Returns {@code true} if the passed file defines command messages,
         * {@code false} otherwise.
         */
        public static final Predicate<FileDescriptor> PREDICATE = file -> {
            checkNotNull(file);

            String fqn = file.getName();
            boolean result = fqn.endsWith(SUFFIX);
            return result;
        };
    }
}
