/*
 * Copyright 2022, TeamDev. All rights reserved.
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
package io.spine.code.proto

import com.google.protobuf.DescriptorProtos.FileDescriptorSet
import com.google.protobuf.DescriptorProtos.FileDescriptorSet.parseFrom
import com.google.protobuf.ExtensionRegistry
import com.google.protobuf.InvalidProtocolBufferException
import io.spine.option.OptionsProvider
import io.spine.util.Exceptions
import java.io.IOException
import java.io.InputStream
import java.util.*

/**
 * Static factory methods for creating instances of [FileDescriptorSet]
 * which wrap handling of checked [InvalidProtocolBufferException].
 *
 * If an error occurs, the methods throw [IllegalArgumentException] with the checked exception
 * as the cause, or return empty [Optional].
 */
public object FileDescriptorSetReader {

    /** The extension registry used when parsing.  */
    private fun registry(): ExtensionRegistry = OptionsProvider.registryWithAllOptions()

    /** Parses a descriptor set from the given byte array. */
    @JvmStatic
    public fun parse(bytes: ByteArray): FileDescriptorSet = try {
        parseFrom(bytes, registry())
    } catch (e: InvalidProtocolBufferException) {
        throw Exceptions.illegalArgumentWithCauseOf(e)
    }

    /** Attempts to parse a descriptor set from the given byte array. */
    @JvmStatic
    public fun tryParse(bytes: ByteArray): Optional<FileDescriptorSet> = try {
        val result = parseFrom(bytes, registry())
        Optional.of(result)
    } catch (e: InvalidProtocolBufferException) {
        Optional.empty()
    }

    /** Parses a descriptor set from the given stream. */
    @JvmStatic
    public fun parse(stream: InputStream): FileDescriptorSet = try {
        parseFrom(stream, registry())
    } catch (e: IOException) {
        throw Exceptions.illegalArgumentWithCauseOf(e)
    }
}
