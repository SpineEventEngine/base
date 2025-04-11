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

package io.spine.protobuf

/**
 * A map of ASCII control characters to their escaping sequences.
 */
@Suppress("MagicNumber") // ASCII codes.
private val ProtobufEscapeSequences = mapOf(
    7 to "\\a",
    8 to "\\b",
    9 to "\\t",
    10 to "\\n",
    11 to "\\v",
    12 to "\\f",
    13 to "\\r",
    34 to "\\\"",
    39 to "\\'",
    92 to "\\\\",
)

/**
 * Restores the original literal as it was defined in the Protobuf source file.
 *
 * Since control characters are unprintable and cannot be directly typed from the keyboard,
 * most compilers (including Protobuf) use escape sequences (e.g., `\n`) to represent them.
 * An escape sequence consists of a backslash followed by one or more symbols. Note that while
 * the backslash is a printable character, it must be escaped too because it introduces
 * an escape sequence.
 *
 * This method reverses the escape sequences that the Protobuf compiler substituted with
 * ASCII control characters during the compilation. The substitution [rules][ProtobufEscapeSequences]
 * are expected to be consistent across all target languages. However, no official documentation
 * exists on this behavior, aside from a reverse-engineered
 * [draft specification](https://protobuf.dev/reference/protobuf/textformat-spec/#string)
 * that includes the following disclaimer:
 *
 * ```
 * While an effort has been made to keep text formats consistent across supported languages,
 * incompatibilities are likely to exist.
 * ```
 *
 * Restoring the escaped symbols can be useful for code generation tasks, as it makes a string
 * literal printable in generated source files (e.g., Java, Kotlin). For example:
 *
 * 1. Using a Regex expression from a protobuf source file as a literal in generated code.
 *    If the literal is pre-processed by the Protobuf compiler, it may no longer be printable
 *    as intended, potentially containing unprintable characters.
 * 2. When reporting problematic Regex expressions in error messages exactly as the user
 *    provided them.
 *
 * Please note that a string literal cannot be fully reconstructed after being processed by
 * the Protobuf compiler due to the following limitations:
 *
 * 1. The question mark `?` always remains as is, since Protobuf accepts both `?` and `\?`.
 * 2. Characters specified using Unicode codes, or as octal or hexadecimal byte values,
 *    remain as text; the method cannot determine whether these were originally specified
 *    as text or as escape sequences.
 */
public fun restoreProtobufEscapes(value: String): String =
    buildString {
        value.forEach {
            append(ProtobufEscapeSequences[it.code] ?: it)
        }
    }
