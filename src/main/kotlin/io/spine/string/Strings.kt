/*
 * Copyright 2024, TeamDev. All rights reserved.
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
@file:Suppress("TooManyFunctions") // Extension functions for `String` are grouped here.

package io.spine.string

import java.util.Base64
import kotlin.text.Charsets.UTF_8

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
 * A diagnostic extension allowing to print an `Iterable` into a string enclosing
 * the elements with backticks and separating them with commas.
 */
public fun <T> Iterable<T>.joinBackticked(): String =
    joinToString(separator = "`, `", prefix = "`", postfix = "`")

/**
 * Obtains the same string but with the first capital letter.
 *
 * If the first char of the string cannot be capitalized (e.g., is not a letter,
 * is already capitalized, etc.), obtains the same string.
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
 * System-dependent line separator.
 */
private val NL: String = Separator.nl()

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
    return trimmedLines.joinToString(NL)
}

/**
 * Replaces [Separator.LF] with [Separator.system].
 *
 * [Separator.LF] is used by Kotlin string utilities for splitting by lines.
 * This may cause issues when writing generated texts under Windows.
 *
 * If you could not find a replacement for system-dependent line separation in
 * `io.spine.string` package, please use this function after a Kotlin string utility call.
 *
 * @see String.pi
 * @see String.tm
 * @see String.ti
 */
public fun String.naturalizeEndings(): String = replace(Separator.LF.value, NL)

/**
 * Trims indentation similarly to [String.trimIndent] but preserving system line separators.
 */
public fun String.ti(): String = trimIndent().naturalizeEndings()

/**
 * The same as [trimMargin] but with system-dependent line separator.
 */
public fun String.tm(): String = trimMargin().naturalizeEndings()

/**
 *  Prepends indentation similarly to [String.prependIndent] but preserving system line separators.
 *
 *  @see Iterable.indent
 */
public fun String.pi(indent: String = Indent.defaultJavaIndent.value): String =
    prependIndent(indent).naturalizeEndings()

/**
 * Joins the elements of this `Iterable` into a single string having each item on a separate line.
 *
 * The lines are delimited with system line separator.
 */
public fun Iterable<*>.joinByLines(): String =
    joinToString(separator = NL)

/**
 * Joins these lines of code into a code block, accounting for extra indent.
 *
 * Similar to [prependIndent] but with system-dependent line separator.
 *
 * @param step
 *         the indentation of each level.
 * @param level
 *         the number of indentation levels to add. If zero, no indentation would be added.
 * @see String.pi
 */
public fun Iterable<String>.indent(step: Indent, level: Int): String {
    val indentation = step.atLevel(level)
    return joinToString(NL) {
        indentation + it
    }
}

/**
 * Converts this string to Base64-encoded version using UTF-8 charset.
 *
 * @see Base64
 */
public fun String.toBase64Encoded(): String {
    val encoder = Base64.getEncoder()
    val valueBytes: ByteArray = toByteArray(UTF_8)
    return encoder.encodeToString(valueBytes)
}

/**
 * Decodes Base64-encoded value into a string with UTF-8 charset.
 *
 * @see Base64
 */
public fun String.decodeBase64(): String {
    val decoder = Base64.getDecoder()
    val decodedBytes = decoder.decode(this)
    return String(decodedBytes, UTF_8)
}

/**
 * Counts a number of times the given [substring] appears in this one.
 *
 * The function counts non-overlapping occurrences.
 * For example, the result for the `"aba"` substring in `"ababababa"` would be 2.
 *
 * @param substring
 *         the substring to look for in this one.
 */
public fun String.count(substring: String): Int {
    var count = 0
    var startIndex = 0
    while (startIndex < length) {
        val index = indexOf(substring, startIndex)
        if (index >= 0) {
            count++
            startIndex = index + substring.length
        } else {
            break
        }
    }
    return count
}

/**
 * Ensures that this string starts with the given prefix.
 */
public fun String.ensurePrefix(prefix: String): String {
    require(prefix.isNotEmpty()) {
        "The prefix must not be empty."
    }
    return if (startsWith(prefix)) {
        this
    } else {
        prefix + this
    }
}
