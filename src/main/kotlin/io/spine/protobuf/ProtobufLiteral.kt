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
 * ASCII control characters escaped by the Protobuf compiler when parsing string literals.
 *
 * Escaping rules may differ from one compiler to another, so a more reliable approach
 * is to re-escape strings according to the specifications of the tool that originally
 * performed the escaping.
 *
 * Note we don't modify question marks because the Protobuf compiler actually accepts
 * both `?` and `\?` as a question mark. Therefore, it is unclear when to prepend
 * the leading `/` to restore the original literal.
 *
 * Also, we are not restoring Unicode codes, or octal and hexadecimal byte values.
 * The reasons are as follows:
 *
 * 1) They do not appear to be expected in our literals.
 * 2) Their escaping is more complex because they have variable length.
 *
 * Source: [Text Format Langauge Specification | String Literals](https://protobuf.dev/reference/protobuf/textformat-spec/#string).
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
 * Restores the original literal as it was defined in the source file.
 *
 * The method reverses the [escape sequences](https://protobuf.dev/reference/protobuf/textformat-spec/#string)
 * that the Protobuf compiler substituted with ASCII control characters during compilation.
 *
 * Because control characters are unprintable and cannot be directly typed from the keyboard,
 * most compilers (including Protobuf) use escape sequences (e.g., `\n`) to represent them.
 * An escape sequence consists of a backslash followed by one or more symbols. Note that while
 * the backslash is a printable character, it must be escaped too because it introduces
 * an escape sequence.
 *
 * Restoring the escaped symbols can be useful for code generation tasks.
 * For example:
 *
 * 1. Report a problematic literal in an error message exactly as the user provided it.
 * 2. Using a provided Regex expression as a literal in generated code. If the literal is
 *    pre-processed by the Protobuf compiler, it may no longer be renderable as intended,
 *    potentially containing unprintable characters instead of the escape sequences.
 *
 * Note that this method does not reverse the escaping for Unicode codes, octal and hexadecimal
 * byte values. It only restores ASCII control characters to their printable escape sequences.
 */
public fun restoreProtobufEscapes(value: String): String =
    buildString {
        value.forEach {
            append(ProtobufEscapeSequences[it.code] ?: it)
        }
    }
