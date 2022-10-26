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

@file:JvmName("Strings")

package io.spine.string

/**
 * Obtains the same string but with the first capital letter.
 *
 * If the first char of the string cannot be capitalized (e.g. is not a letter, is already
 * capitalized, etc.), obtains the same string.
 */
public fun String.titleCase(): String =
    replaceFirstChar { it.titlecase() }

/**
 * Obtains the same string but in camel case instead of snake case.
 *
 * The string will start with the first capital letter if possible.
 *
 * Examples:
 *  - `"aaa"` becomes `"Aaa"`;
 *  - `"field_name"` becomes `"FieldName"`;
 *  - `"TypeName"` stays `"TypeName"`;
 *  - `"___u_ri____"` becomes `"URi"`.
 */
public fun String.camelCase(): String =
    split("_").camelCase()

/**
 * Joins these strings into a camel case string.
 *
 * The string will start with the first capital letter if possible.
 *
 * Each element is capitalized and joined in the order of appearance in this `Iterable`.
 * The elements are never changed except for the first char.
 */
public fun Iterable<String>.camelCase(): String =
    filter { it.isNotBlank() }.joinToString(separator = "") { it.titleCase() }

/**
 * Trims the common indent from all the lines, as well as the trailing whitespace.
 *
 * Same as [String.trimIndent] but also removes the trailing whitespace characters.
 */
internal fun String.trimWhitespace(): String {
    val noIndent = trimIndent()
    val lines = noIndent.lines()
    val trimmedLines = lines.map {
        it.trimEnd()
    }
    return trimmedLines.joinToString(System.lineSeparator())
}
