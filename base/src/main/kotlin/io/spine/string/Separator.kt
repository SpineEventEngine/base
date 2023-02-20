/*
 * Copyright 2023, TeamDev. All rights reserved.
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

package io.spine.string

/**
 * Constants for line separators.
 */
public enum class Separator(public val value: String) {

    /**
     * The line separator used by Unix-like systems (including Linux and macOS).
     *
     * This line separator is used by Kotlin string utilities in
     * [String.trimIndent] and [String.replaceIndent].
     */
    LF("\n"),

    /**
     * The line separator used by the Classic Mac OS.
     */
    CR("\r"),

    /**
     * Windows line separator.
     */
    CRLF("\r\n");

    /**
     * Tells if this separator is used by the current operating system.
     */
    public fun isSystem(): Boolean = value == nl()

    public companion object {

        /**
         * The shortcut for [System.lineSeparator].
         */
        @JvmStatic
        public fun nl(): String = System.lineSeparator()

        /**
         * Obtains line separators that are not used by the current operating system.
         */
        public fun nonSystem(): Iterable<Separator> =
            values().filter { !it.isSystem() }

        /**
         * Finds a separator which value is equal to the given string.
         */
        internal fun findMatching(str: String): Separator? =
            values().find { str == it.value }
    }
}

/**
 * Tells if this char sequence contains the given separator.
 */
public fun CharSequence.contains(s: Separator): Boolean =
    contains(s.value)

/**
 * Obtains all line separators found it this char sequence.
 */
public fun CharSequence.findLineSeparators(): List<Separator> {
    val allSeparators = Regex("\\R")
    val separators = allSeparators.findAll(this)
    val matchingSeparators = separators.mapNotNull {
        Separator.findMatching(it.value)
    }
    return matchingSeparators.toList()
}

/**
 * Tells if this char sequence contain at least one non-system line separator.
 */
public fun CharSequence.containsNonSystemLineSeparator(): Boolean {
    val found = findLineSeparators().any { !it.isSystem() }
    return found
}
