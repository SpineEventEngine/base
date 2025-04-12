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

@file:JvmName("Parsers")

package io.spine.format.parse

import com.google.common.io.Files
import io.spine.format.Format
import io.spine.format.Format.JSON
import io.spine.format.Format.PROTO_BINARY
import io.spine.format.Format.PROTO_JSON
import io.spine.format.Format.YAML
import java.io.File

/**
 * Parses the given file loading the instance of the given class.
 *
 * The format of the file is determined by the extension of the file.
 *
 * @param T The type of the class stored in the file.
 * @param file The file to parse.
 * @throws IllegalStateException if the file is not of the supported [format][Format].
 * @throws java.io.IOException or its subclass, if the parsing of the file fails.
 */
public inline fun <reified T : Any> parse(file: File): T =
    parse(file, T::class.java)

/**
 * Parses the given file loading the instance of the given class.
 *
 * This function provides the [format] parameter to cover the cases
 * of custom file extensions that are not available from
 * the items of the [Format] enumeration.
 *
 * @param T The type of the class stored in the file.
 * @param file The file to parse.
 * @param format The format of the file.
 * @throws IllegalStateException if the file is not of the supported [format][Format].
 * @throws java.io.IOException or its subclass, if the parsing of the file fails.
 */
public inline fun <reified T : Any> parse(file: File, format: Format): T =
    parse(file, format, T::class.java)

/**
 * Parses the given file loading the instance of the given class.
 *
 * The format of the file is determined by the extension of the file.
 *
 * @param T The type of the class stored in the file.
 * @param file The file to parse.
 * @param cls The class of the instance stored in the file.
 * @throws IllegalStateException if the file is not of the supported [format][Format].
 * @throws java.io.IOException or its subclass, if the parsing of the file fails.
 */
public fun <T : Any> parse(file: File, cls: Class<T>): T {
    val format = Format.of(file)
    return parse(file, format, cls)
}

/**
 * Parses the given file loading the instance of the given class.
 *
 * This function provides the [format] parameter to cover the cases
 * of custom file extensions that are not available from
 * the items of the [Format] enumeration.
 *
 * @param T The type of the class stored in the file.
 * @param file The file to parse.
 * @param format The format of the file.
 * @param cls The class of the instance stored in the file.
 * @throws IllegalStateException if the file is not of the supported [format][Format].
 * @throws java.io.IOException or its subclass, if the parsing of the file fails.
 */
public fun <T : Any> parse(
    file: File,
    format: Format,
    cls: Class<T>
): T {
    val bytes = Files.asByteSource(file)
    return format.parser.parse(bytes, cls)
}

/**
 * Obtains a [Parser] for this [format][Format].
 */
private val Format.parser: Parser
    get() = when(this) {
        PROTO_BINARY -> ProtoBinaryParser
        PROTO_JSON -> ProtoJsonParser
        JSON -> JsonParser
        YAML -> YamlParser
    }
