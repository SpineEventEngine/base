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

package io.spine.string

/**
 * ASCII control characters escaped by Protobuf compiler when parsing string literals.
 *
 * Escaping rules may differ from compiler to compiler, so a more reliable approach is
 * to re-escape strings in accordance to the specs of a particular tool that previously
 * performed this escaping.
 *
 * Note we don't touch question marks because Protobuf compiler actually accepts both `?`
 * and `\?` as a question mark. So, it is unclear when we should prepend the leading `/`
 * to restore the original literal.
 *
 * Source: [Text Format Langauge Specification | String Literals](https://protobuf.dev/reference/protobuf/textformat-spec/#string).
 */
@Suppress("MagicNumber") // ASCII codes.
private val ProtobufEscapeSequences = mapOf(
    7 to "\\a",
    8 to "\\b",
    12 to "\\f",
    10 to "\\n",
    13 to "\\r",
    9 to "\\t",
    11 to "\\v",
    92 to "\\\\",
    39 to "\\'",
    34 to "\\\""
)

/**
 * Restores the original literal as it was defined in a source file.
 *
 * The method restores [escape sequences](https://protobuf.dev/reference/protobuf/textformat-spec/#string),
 * which Protobuf compiler substituted with ASCII control characters during the compilation.
 *
 * The control characters are unprintable, thus cannot be typed directly from the keyboard.
 * Most compilers, including Protobuf, use escape sequences to represent such symbols
 * in string literals (i.e., `\n`). An escape symbol consists of a backslash and another
 * one or more symbols. The backslash is a printable character itself, but since it is used
 * to introduce an escape sequence, it requires escaping itself.
 *
 * For code generation tasks, this can come handy in the following cases:
 *
 * 1. Report the problematic literal in compilation or runtime error messages as it was
 *    passed by the user. It will help the user to locate the problematic declaration.
 * 2. Use the passed Regex expressions as literals in the generated code.
 *    If the passed literal was pre-processed by the compiler, it cannot be longer
 *    rendered in a source file as literal. Now, it contains unprintable characters instead
 *    of printable escape sequences, and may contain a non-escaped backslash, which is
 *    prohibited for literals.
 *
 * Note that this method does not roll back Unicode codes, octal and hexadecimal byte values.
 * Only ASCII control characters are re-escaped.
 */
public fun restoreEscapedSymbols(value: String): String =
    buildString {
        value.forEach {
            append(ProtobufEscapeSequences[it.code] ?: it)
        }
    }
