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

import io.spine.string.Indent.Companion.DEFAULT_SIZE

/**
 * Obtains the same string but with the first capital letter.
 *
 * If the first char of the string cannot be capitalized (e.g. is not a letter, is already
 * capitalized, etc.), obtains the same string.
 */
public fun String.titleCase(): String =
    replaceFirstChar { it.titlecase() }

/**
 * Obtains the same string but in `CamelCase` instead of `snake_case`.
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
 * Joins these strings into a `CamelCase` string.
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
 * This method is similar to [String.trimIndent], but unlike it:
 *    1) preserves system line separators;
 *    2) also removes the trailing whitespace characters.
 */
public fun String.trimWhitespace(): String {
    val noIndent = trimIndent()
    val lines = noIndent.lines()
    val trimmedLines = lines.map {
        it.trimEnd()
    }
    return trimmedLines.joinToString(Separator.nl())
}

/**
 * Replaces [Separator.LF] used by Kotlin string utilities for splitting by lines,
 * with [Separator.nl] so that we don't have issues when writing generated texts under Windows.
 */
private fun String.fixLineEndings(): String = replace(Separator.LF.value, Separator.nl())

/**
 * Trims indentation similarly to [String.trimIndent] but preserving system line separators.
 */
public fun String.ti(): String = trimIndent().fixLineEndings()

/**
 *  Prepends indentation similarly to [String.prependIndent] but preserving system line separators.
 */
public fun String.pi(indent: String = Indent(DEFAULT_SIZE).value): String =
    prependIndent(indent).fixLineEndings()
