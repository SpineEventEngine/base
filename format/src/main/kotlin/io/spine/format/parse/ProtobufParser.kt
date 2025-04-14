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
import io.spine.type.fromJson
import io.spine.type.parse
import java.nio.charset.Charset.defaultCharset

/**
 * The abstract base for parsers of files containing Protobuf messages.
 */
internal sealed class ProtobufParser : Parser<Message> {

    override fun <M : Message> parse(source: ByteSource, cls: Class<out M>): M {
        val parsed = doParse(source, cls)
        @Suppress("UNCHECKED_CAST")
        return parsed as M
    }

    /**
     * Deserializes the given bytes into a message of the specified class.
     */
    abstract fun doParse(source: ByteSource, cls: Class<out Message>): Message
}

/**
 * Settings parser for Protobuf messages encoded in the Protobuf binary format.
 *
 * @see io.spine.format.write.ProtoBinaryWriter
 * @see ProtoJsonParser
 */
internal data object ProtoBinaryParser : ProtobufParser() {

    /**
     * Parses the [source] for obtaining the message of the specified class.
     */
    override fun doParse(source: ByteSource, cls: Class<out Message>): Message =
        source.openStream().use {
            return cls.parse(it)
        }
}

/**
 * Settings parser for Protobuf messages encoded in the Protobuf JSON format.
 *
 * @see io.spine.format.write.ProtoJsonWriter
 * @see ProtoBinaryParser
 */
internal data object ProtoJsonParser : ProtobufParser() {

    override fun doParse(source: ByteSource, cls: Class<out Message>): Message {
        val charSource = source.asCharSource(defaultCharset())
        val json = charSource.read()
        return cls.fromJson(json)
    }
}
