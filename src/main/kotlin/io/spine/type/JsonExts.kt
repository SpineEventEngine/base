/*
 * Copyright 2024, TeamDev. All rights reserved.
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

@file:JvmName("Json")

package io.spine.type

import com.google.common.base.Throwables
import com.google.protobuf.InvalidProtocolBufferException
import com.google.protobuf.Message
import com.google.protobuf.MessageOrBuilder
import com.google.protobuf.util.JsonFormat
import com.google.protobuf.util.JsonFormat.Parser
import com.google.protobuf.util.JsonFormat.Printer
import io.spine.protobuf.builderFor
import io.spine.type.TypeRegistryHolder.typeRegistry
import io.spine.util.Exceptions.newIllegalArgumentException

/**
 * Utilities for working with JSON representation of Protobuf [Message] types.
 *
 * Both [parsing][Class.fromJson] and [printing][Message.toJson] functions assume
 * the presence of the custom Protobuf message types relying on [KnownTypes] for this.
 *
 * The parsing functionality follows the default Protobuf strategy for
 * [ignoring][Parser.ignoringUnknownFields] unknown fields when a JSON string is parsed.
 *
 * @see <a href="https://developers.google.com/protocol-buffers/docs/proto3#unknowns">
 *         Protobuf Unknown Fields</a>
 */
@Suppress("unused")
private const val ABOUT = ""

/**
 * Holds lazily evaluated properties related to generating and parsing JSON.
 */
private object JsonOutput {

    val printer: Printer by lazy {
        JsonFormat.printer()
            .usingTypeRegistry(typeRegistry)
    }

    val compactPrinter: Printer by lazy {
        printer.omittingInsignificantWhitespace()
    }

    val parser: Parser by lazy {
        JsonFormat.parser()
            .ignoringUnknownFields()
            .usingTypeRegistry(typeRegistry)
    }
}

/**
 * Converts this message or builder to JSON using the given [printer].
 *
 * The default instance of the [Printer] produces multi-line output.
 *
 * @see [toCompactJson]
 */
@JvmOverloads
public fun MessageOrBuilder.toJson(printer: Printer = JsonOutput.printer): String {
    val result: String?
    try {
        result = printer.print(this)
    } catch (e: InvalidProtocolBufferException) {
        val rootCause = Throwables.getRootCause(e)
        throw UnknownTypeException(rootCause)
    }
    check(result != null)
    return result
}

/**
 * Converts this message into compact JSON representation.
 *
 * <p>The resulted JSON does not contain the line separators.
 *
 * @see [toJson]
 */
public fun MessageOrBuilder.toCompactJson(): String =
    toJson(JsonOutput.compactPrinter)

/**
 * Parses a message of the type [T] from the given [json] representation.
 *
 * @throws IllegalArgumentException
 *          if the message of this type cannot be parsed from the given string.
 */
public fun <T : Message> Class<T>.fromJson(json: String): T {
    try {
        val messageBuilder = builderFor(this)
        JsonOutput.parser.merge(json, messageBuilder)
        @Suppress("UNCHECKED_CAST") // The type is ensured by `builderFor()`.
        val result = messageBuilder.build() as T
        return result
    } catch (e: InvalidProtocolBufferException) {
        throw newIllegalArgumentException(
            e,
            "The JSON text (`$json`) cannot be parsed to an instance of the class `$name`."
        )
    }
}
