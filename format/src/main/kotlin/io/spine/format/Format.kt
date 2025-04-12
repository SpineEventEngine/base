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

package io.spine.format

import com.google.protobuf.Message
import io.spine.format.parse.JsonParser
import io.spine.format.parse.Parser
import io.spine.format.parse.ProtoBinaryParser
import io.spine.format.parse.ProtoJsonParser
import io.spine.format.parse.YamlParser
import io.spine.format.write.JsonWriter
import io.spine.format.write.ProtoBinaryWriter
import io.spine.format.write.ProtoJsonWriter
import io.spine.format.write.Writer
import io.spine.format.write.YamlWriter
import io.spine.io.Glob
import java.io.File

/**
 * Formats supported for I/O operations supported by the [io.spine.format] package.
 *
 * A format of a file can be obtained using the [Format.of] function.
 *
 * You can check if a file is in the supported format using the function
 * [File.hasSupportedFormat].
 *
 * @param extensions One or more extensions of the files that are conventionally used
 *   for files in the corresponding format. If there is a format has multiple file extensions,
 *   the one coming fist in the list is the primary one.
 *   This [extension] will be used for writing and other operations that need to match
 *   a file name to its format.
 * @see parse
 * @see write
 */
public sealed class Format<T : Any>(
    internal val writer: Writer<T>,
    internal val parser: Parser<T>,
    vararg extensions: String
) {
    /**
     * A Protobuf message encoded in binary.
     *
     * @see <a href="https://protobuf.dev/programming-guides/techniques/#suffixes">
     *     Common Filename Suffixes</a>
     * @see <a href="https://buf.build/docs/reference/inputs#binpb">Buf Docs: binpb</a>
     */
    public data object ProtoBinary : Format<Message>(
        ProtoBinaryWriter,
        ProtoBinaryParser,
        "binpb", "pb", "bin"
    )

    /**
     * A Protobuf message encoded in Protobuf JSON.
     *
     * Use this item instead of [Json] for Protobuf messages stored in
     * JSON format so that the correct parser is selected for the file.
     */
    public data object ProtoJson : Format<Message>(
        ProtoJsonWriter,
        ProtoJsonParser,
        "pb.json"
    )

    /**
     * A plain [JSON](https://www.json.org/) value.
     */
    public data object Json : Format<Any>(
        JsonWriter,
        JsonParser,
        "json") {

        /**
         * Avoids the clash with [ProtoJson] format extension, which
         * ends on `.json` too.
         */
        override fun matches(file: File): Boolean {
            if (ProtoJson.matches(file)) {
                return false
            }
            return super.matches(file)
        }
    }

    /**
     * A plain [YAML](https://yaml.org/) value.
     */
    public data object Yaml : Format<Any>(
        YamlWriter,
        YamlParser,
        "yml", "yaml"
    )

    /**
     * Checks if the given file matches this format.
     */
    public open fun matches(file: File): Boolean =
        extensions
            .map { Glob.extension(it) }
            .any { it.matches(file.toPath()) }

    /**
     * Obtains file extensions of this format.
     *
     * Extensions are given without leading dots.
     */
    public val extensions: List<String>

    /**
     * The primary file extension of this format.
     */
    public val extension: String by lazy { extensions[0] }

    init {
        val list = extensions.toList()
        require(list.isNotEmpty()) {
            "The file format `${this::class.simpleName}` must have at least one extension."
        }
        this.extensions = list
    }

    public companion object {

        internal val entries: List<Format<*>> = listOf(
            ProtoBinary,
            ProtoJson,
            Json,
            Yaml,
        )

        /**
         * Obtains a [Format] from the extension of the given file.
         *
         * @throws IllegalStateException If the format is not recognized.
         * @see File.hasSupportedFormat
         */
        @JvmStatic
        public fun of(file: File): Format<*> =
            entries.find { it.matches(file) }
                ?: error("Unsupported file format: `${file.name}`.")
    }
}
