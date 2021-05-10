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

package io.spine.code.proto;

import com.google.protobuf.DescriptorProtos.FileDescriptorSet;
import com.google.protobuf.ExtensionRegistry;
import com.google.protobuf.InvalidProtocolBufferException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

import static com.google.protobuf.DescriptorProtos.FileDescriptorSet.parseFrom;
import static io.spine.util.Exceptions.illegalArgumentWithCauseOf;

/**
 * Static factory methods for creating instances of {@link FileDescriptorSet}
 * which wrap handling of checked {@link InvalidProtocolBufferException}.
 */
public final class FileDescriptorSetReader {

    /** Prevents instantiation of this utility class. */
    private FileDescriptorSetReader() {
    }

    /** The extension registry used when parsing. */
    private static ExtensionRegistry registry() {
        return OptionExtensionRegistry.instance();
    }

    /**
     * Parses a descriptor set from the given byte array.
     *
     * @param bytes
     *         the data to parse
     * @return instance of {@code FileDescriptorSet} encoded in the bytes
     * @throws IllegalArgumentException
     *         if parsing fails
     */
    public static FileDescriptorSet parse(byte[] bytes) {
        try {
            FileDescriptorSet result = parseFrom(bytes, registry());
            return result;
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
            FileDescriptorSet result = parseFrom(bytes, registry());
            return Optional.of(result);
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
     * @throws IllegalArgumentException
     *         if parsing fails or if stream cannot be read
     */
    public static FileDescriptorSet parse(InputStream stream) {
        try {
            FileDescriptorSet result = parseFrom(stream, registry());
            return result;
        } catch (IOException e) {
            throw illegalArgumentWithCauseOf(e);
        }
    }
}
