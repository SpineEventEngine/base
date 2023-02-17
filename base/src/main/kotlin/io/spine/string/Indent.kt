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
 * An increment of an indentation to be used for formatting texts.
 */
public data class Indent(

    /**
     * A positive number of space characters to be used for the indentation increment.
     */
    public val size: Int = DEFAULT_SIZE
) {

    init {
        require(size > 0) { "The `size` must be positive, but was $size."}
    }

    /**
     * The value of the indentation increment.
     */
    public val value: String = " ".repeat(size)

    /**
     * Obtains the value of this indentation.
     */
    override fun toString(): String = value

    public companion object {

        /**
         * The default indentation, which is primarily used in the generated Java code.
         */
        public const val DEFAULT_SIZE: Int = 4
    }
}

/**
 * Repeats this indentation [n] times.
 *
 * @throws [IllegalArgumentException] when [n] < 0.
 */
public fun Indent.repeat(n: Int): String {
    require(size >= 0) { "Count `n` must be non-negative, but was $size."}
    return value.repeat(n)
}

/**
 * Same as [repeat].
 */
public fun Indent.atLevel(l: Int): String = repeat(l)
