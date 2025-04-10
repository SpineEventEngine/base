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

import io.spine.format.Format.entries
import io.spine.io.Glob
import java.io.File
import java.nio.file.Path
import kotlin.io.path.name

/**
 * Formats supported for parsing by the Spine Format library.
 *
 * @param extensions The extensions of the files storing data in the corresponding format.
 */
public enum class Format(vararg extensions: String) {

    /**
     * A Protobuf message encoded in binary.
     *
     * @see <a href="https://protobuf.dev/programming-guides/techniques/#suffixes">
     *     Common Filename Suffixes</a>
     * @see <a href="https://buf.build/docs/reference/inputs#binpb">Buf Docs: binpb</a>
     */
    PROTO_BINARY("binpb", "pb", "bin"),

    /**
     * A Protobuf message encoded in Protobuf JSON.
     *
     * Use this item instead of [JSON] for Protobuf messages stored in
     * JSON format so that the correct parser is selected for the file.
     */
    PROTO_JSON("pb.json"),

    /**
     * A plain [JSON](https://www.json.org/) value.
     */
    JSON("json"),

    /**
     * A plain [YAML](https://yaml.org/) value.
     */
    YAML("yml", "yaml"),

    /**
     * A plain text value.
     */
    TEXT("txt");

    /**
     * Checks if the given file matches this format.
     */
    public fun matches(file: Path): Boolean =
        extensions
            .map { Glob.extension(it) }
            .any { it.matches(file) }

    /**
     * Obtains file extensions of this format.
     *
     * Extensions are given without leading dots.
     */
    public val extensions: List<String> = extensions.toList()

    public companion object {

        /**
         * Obtains a [Format] from the extension of the given file.
         *
         * @throws IllegalStateException If the format is not recognized.
         */
        @JvmStatic
        public fun of(file: File): Format = of(file.toPath())

        /**
         * Obtains a [Format] from the extension of the given file.
         *
         * @throws IllegalStateException If the format is not recognized.
         */
        @JvmStatic
        public fun of(file: Path): Format =
            entries.find { it.matches(file) }
                ?: error("The file is not of recognized format: `${file.name}`.")
    }
}

/**
 * Tells if this file is of one of the supported [formats][Format].
 */
public fun Path.hasSupportedFormat(): Boolean =
    entries.any { it.matches(this) }

/**
 * Tells if this file is of one of the supported [formats][Format].
 */
public fun File.hasSupportedFormat(): Boolean = toPath().hasSupportedFormat()
