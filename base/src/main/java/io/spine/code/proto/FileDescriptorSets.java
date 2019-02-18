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

package io.spine.code.proto;

import com.google.protobuf.DescriptorProtos.FileDescriptorSet;
import com.google.protobuf.InvalidProtocolBufferException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

import static io.spine.option.Options.registry;
import static io.spine.util.Exceptions.illegalArgumentWithCauseOf;

/**
 * A factory of {@code FileDescriptorSet} instances.
 */
public final class FileDescriptorSets {

    /**
     * Prevents the utility class instantiation.
     */
    private FileDescriptorSets() {
    }

    /**
     * Parses a descriptor set from the given byte array.
     *
     * @param bytes
     *         raw data to parse
     * @return instance of {@code FileDescriptorSet} encoded in the bytes
     * @throws java.lang.IllegalArgumentException
     *         if parsing fails
     */
    public static FileDescriptorSet parse(byte[] bytes) {
        try {
            return FileDescriptorSet.parseFrom(bytes, registry());
        } catch (InvalidProtocolBufferException e) {
            throw illegalArgumentWithCauseOf(e);
        }
    }

    /**
     * Attempts to parse a descriptor set from the given byte array.
     *
     * @param bytes
     *         raw data to parse
     * @return instance of {@code FileDescriptorSet} encoded in the bytes or
     *         {@code Optional.empty()} if parsing fails
     */
    public static Optional<FileDescriptorSet> tryParse(byte[] bytes) {
        try {
            FileDescriptorSet descriptorSet = FileDescriptorSet.parseFrom(bytes, registry());
            return Optional.of(descriptorSet);
        } catch (InvalidProtocolBufferException e) {
            return Optional.empty();
        }
    }

    /**
     * Parses a descriptor set from the given {@code InputStream}.
     *
     * @param stream
     *         byte stream of data to parse
     * @return instance of {@code FileDescriptorSet} encoded in the bytes
     * @throws java.lang.IllegalArgumentException
     *         if parsing fails or if stream cannot be read
     */
    public static FileDescriptorSet parse(InputStream stream) {
        try {
            return FileDescriptorSet.parseFrom(stream, registry());
        } catch (IOException e) {
            throw illegalArgumentWithCauseOf(e);
        }
    }
}
