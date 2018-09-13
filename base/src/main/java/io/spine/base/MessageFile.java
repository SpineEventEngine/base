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
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import com.google.protobuf.Descriptors.FileDescriptor;
import io.spine.code.proto.FileName;
import io.spine.value.StringTypeValue;

import java.io.Serializable;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Describes a file containing proto message declarations.
 *
 * @author Alexander Yevsyukov
 */
public abstract class MessageFile extends StringTypeValue {

    private static final long serialVersionUID = 0L;

    MessageFile(String name) {
        super(checkNotNull(name) + FileName.EXTENSION);
    }

    /**
     * Obtains the predicate for filtering files containing message declarations
     * of the required type.
     */
    public final Predicate predicate() {
        String suffix = value();
        return new Predicate(suffix);
    }

    /**
     * A Protobuf file predicate.
     *
     * <p>Tests if a given file matches a {@linkplain MessageFile file type}.
     */
    @Immutable
    public static final class Predicate implements Serializable {

        private static final long serialVersionUID = 0L;

        private final String suffix;

        /**
         * Creates a new instance of {@code Predicate}.
         *
         * <p>A file matches this predicate if the file name ends with the given {@code suffix}.
         *
         * @param suffix the suffix of a Protobuf file name
         */
        private Predicate(String suffix) {
            this.suffix = suffix;
        }

        /**
         * Checks if given file upon this predicate.
         *
         * @param file
         *         the file descriptor message
         * @return {@code true} if the file name ends with the {@code suffix},
         *         {@code false} otherwise
         */
        public boolean test(FileDescriptorProto file) {
            String name = file.getName();
            return name.endsWith(suffix);
        }

        /**
         * Checks if given file upon this predicate.
         *
         * @param file
         *         the file descriptor
         * @return {@code true} if the file name ends with the {@code suffix},
         *         {@code false} otherwise
         */
        public boolean test(FileDescriptor file) {
            FileDescriptorProto protoDescriptor = file.toProto();
            return test(protoDescriptor);
        }
    }
}
