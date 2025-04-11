/*
 * Copyright 2025, TeamDev. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
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

package io.spine.format.parse

import com.google.common.io.ByteSource
import com.google.protobuf.Message
import io.spine.protobuf.defaultInstance
import io.spine.type.fromJson
import java.nio.charset.Charset.defaultCharset

/**
 * The abstract base for parsers of files storing Protobuf messages.
 */
internal sealed class ProtobufParser : Parser {

    final override fun <T> parse(source: ByteSource, cls: Class<T>): T {
        require(Message::class.java.isAssignableFrom(cls)) {
            "Expected a message class but got `${cls.canonicalName}`."
        }
        @Suppress("UNCHECKED_CAST")
        return doParse(source, cls as Class<out Message>) as T
    }

    /**
     * Deserializes the given bytes into a message with the given class.
     */
    abstract fun doParse(source: ByteSource, cls: Class<out Message>): Message
}

/**
 * Settings parser for Protobuf messages encoded in the Protobuf binary format.
 */
internal data object ProtoBinaryParser : ProtobufParser() {

    override fun doParse(source: ByteSource, cls: Class<out Message>): Message {
        val builder = cls.defaultInstance.toBuilder()
        builder.mergeFrom(source.read())
        return builder.build()
    }
}

/**
 * Settings parser for Protobuf messages encoded in the Protobuf JSON format.
 */
internal data object ProtoJsonParser : ProtobufParser() {

    override fun doParse(source: ByteSource, cls: Class<out Message>): Message {
        val charSource = source.asCharSource(defaultCharset())
        val json = charSource.read()
        return cls.fromJson(json)
    }
}
